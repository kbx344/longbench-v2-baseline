package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按实体切分：材料里并排放着几份文档时，按题干点名的实体把材料切开
 *
 * FLOW_QA_SPLIT_ENTITY
 *
 * 为什么需要它：Q05 的 context 是 Lilium 的 F-1 和 Archer 的 S-1 拼在一起（1,842,906 字符），
 * 题干问"两家公司的哪项不是共同特征"。人不会通读两份招股书，会先按公司分成两摞再对照。
 * 这一步是纯字符串定位，零 token：题干里的大写实体（Archer Aviation / Lilium）在材料里
 * 首次出现的位置就是天然分界点。
 *
 * 与 FLOW_QA_SPLIT_CHUNKS 的分工：那个按字数均匀切，不知道文档语义边界；
 * 这个按实体出现位置切，每段带 book 标签，后续取证能按文档分桶。
 *
 * 认不出两个以上实体时退回"整份一段"，不会把流程搞挂。
 *
 * @param qid 题号
 * @result question 题干
 * @result choices 选项
 * @result answerKey 标准答案
 * @result segments 按实体切开的段，每段带 book 标签
 * @result books 命中的实体名
 */
@FlowComponent
public class FlowCodeQaSplitEntity {

    /** 题干里的大写实体：一个或多个以大写字母开头的词 */
    private static final Pattern ENTITY =
            Pattern.compile("[A-Z][A-Za-z&'.]*(?:\\s+[A-Z][A-Za-z&'.]*)*");

    /** 这些词长得像实体但不是实体，出现在题干开头多半是疑问词 */
    private static final String[] STOP = {"Which", "What", "The", "A", "An", "Following",
            "Of", "In", "Is", "Are", "Not", "And", "Can", "You", "Tell", "Me", "Option", "Options",
            "According", "Above", "Data", "Based"};

    @Flow("FLOW_QA_SPLIT_ENTITY")
    public void flowCodeQaSplitEntity(
            String qid
    ) {
        String question = "";
        String choices = "{}";
        String answerKey = "";
        String segments = "[]";
        String books = "";
        try {
            int no = QaSupport.parseNo(qid);
            JsonNode item = QaSupport.loadItem(no);
            if (item == null) {
                throw new IllegalStateException("数据集中没有第 " + no + " 条");
            }
            question = item.path("question").asText("");
            answerKey = item.path("answer").asText("");
            ObjectNode c = QaSupport.JSON.createObjectNode();
            for (String k : new String[]{"A", "B", "C", "D"}) {
                c.put(k, item.path("choice_" + k).asText(""));
            }
            choices = c.toString();

            String ctx = item.path("context").asText("");
            segments = split(ctx, question);
            books = lastBooks;
        } catch (Exception e) {
            question = "[SPLIT_FAILED] " + e.toString();
        }
        FlowEngine.instance().register("question", question);
        FlowEngine.instance().register("choices", choices);
        FlowEngine.instance().register("answerKey", answerKey);
        FlowEngine.instance().register("segments", segments);
        FlowEngine.instance().register("books", books);
    }

    /** 最近一次切出的实体名 */
    private String lastBooks = "";

    private String split(String ctx, String question) {
        lastBooks = "";
        ArrayNode arr = QaSupport.JSON.createArrayNode();

        List<String> ents = entities(question);
        List<int[]> at = new ArrayList<int[]>();
        List<String> keep = new ArrayList<String>();
        for (String e : ents) {
            int p = ctx.indexOf(e);
            if (p > 0) {
                at.add(new int[]{p});
                keep.add(e);
            }
        }

        // 只有一个实体、或者压根没有：整份当一段，交给后续取证按全文检索
        if (keep.size() < 2) {
            ObjectNode o = QaSupport.JSON.createObjectNode();
            o.put("i", 0);
            o.put("book", "ALL");
            o.put("start", 0);
            o.put("end", ctx.length());
            o.put("text", ctx);
            arr.add(o);
            lastBooks = "ALL";
            return arr.toString();
        }

        // 按实体首次出现位置排序，位置就是文档边界
        Integer[] idx = new Integer[at.size()];
        for (int i = 0; i < idx.length; i++) {
            idx[i] = i;
        }
        java.util.Arrays.sort(idx, new Comparator<Integer>() {
            public int compare(Integer x, Integer y) {
                return at.get(x)[0] - at.get(y)[0];
            }
        });

        // 相邻实体必须离得够远，否则不是"两份文档"而是同一份里的两个关键词。
        // 实测题干里的 eVTOL 会被大写片段正则捕到 VTOL，它落在 Lilium 那份中间，
        // 若不设间距就会把 Lilium 文档切碎成三段，Lilium 的取证随之全空。
        int minGap = ctx.length() / 10;
        List<int[]> cut = new ArrayList<int[]>();
        List<String> names = new ArrayList<String>();
        for (Integer i : idx) {
            if (!cut.isEmpty() && at.get(i)[0] - cut.get(cut.size() - 1)[0] < minGap) {
                continue;
            }
            cut.add(at.get(i));
            names.add(keep.get(i));
        }
        if (cut.size() < 2) {
            ObjectNode o = QaSupport.JSON.createObjectNode();
            o.put("i", 0);
            o.put("book", "ALL");
            o.put("start", 0);
            o.put("end", ctx.length());
            o.put("text", ctx);
            arr.add(o);
            lastBooks = "ALL";
            return arr.toString();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cut.size(); i++) {
            if (i > 0) {
                sb.append("/");
            }
            sb.append(names.get(i));
        }
        lastBooks = sb.toString();

        for (int i = 0; i < cut.size(); i++) {
            int start = i == 0 ? 0 : cut.get(i)[0];
            int end = i + 1 < cut.size() ? cut.get(i + 1)[0] : ctx.length();
            ObjectNode o = QaSupport.JSON.createObjectNode();
            o.put("i", i);
            o.put("book", names.get(i));
            o.put("start", start);
            o.put("end", end);
            o.put("text", ctx.substring(start, end));
            arr.add(o);
        }
        return arr.toString();
    }

    /** 抽出题干里的大写实体，去掉疑问词与重复 */
    private List<String> entities(String question) {
        Set<String> out = new LinkedHashSet<String>();
        if (question == null) {
            return new ArrayList<String>();
        }
        Matcher m = ENTITY.matcher(question);
        while (m.find()) {
            String e = m.group().trim();
            if (e.split("\\s+").length == 1 && isStop(e)) {
                continue;
            }
            if (e.length() < 3) {
                continue;
            }
            out.add(e);
        }
        List<String> list = new ArrayList<String>(out);
        // 长实体优先，避免 "Archer" 把 "Archer Aviation" 抢掉
        Collections.sort(list, new Comparator<String>() {
            public int compare(String a, String b) {
                return b.length() - a.length();
            }
        });
        return list;
    }

    private boolean isStop(String w) {
        for (String s : STOP) {
            if (s.equalsIgnoreCase(w)) {
                return true;
            }
        }
        return false;
    }
}

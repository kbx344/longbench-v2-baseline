package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 表格列投影：只保留题目要用的列，再按窗口切段
 *
 * FLOW_QA_PROJECT_COLS
 *
 * 与 FLOW_QA_SPLIT_CHUNKS 的分工：那个对整份材料原样切；这个先认表头、再按题干选列，
 * 把宽表压成窄表。实测一张 4,952,558 字符的行情表，只留 时间/最高/最低 三列后降到 102 万字符，
 * 段数从 34 降到 7，答案不受影响——因为题目只用得到这三列。
 *
 * 认不出表格时退回原样切段，不会把流程搞挂。
 *
 * @param qid 题号
 * @result question 题干
 * @result choices 选项
 * @result answerKey 标准答案
 * @result segments 投影后的顺序分段
 * @result colInfo 实际保留的列
 */
@FlowComponent
public class FlowCodeQaProjectCols {

    private static final int CHUNK = 150_000;

    /** 判定"这是表格"的最低解析成功率 */
    private static final double MIN_PARSE_RATE = 0.8;

    private static final String COL_SYS =
            "你在判断一张表格里回答某道选择题所必需的最小列集合。"
            + "只保留题目直接需要比较、计算或定位的列，其余一律不选；时间或编号列必须保留，用来定位行。"
            + "列名必须与给定表头逐字一致。输出 JSON 数组，最多 4 个列名，只输出 JSON。";

    @Flow("FLOW_QA_PROJECT_COLS")
    public void flowCodeQaProjectCols(
            String qid
    ) {
        String question = "";
        String choices = "{}";
        String answerKey = "";
        String segments = "[]";
        String colInfo = "";
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
            String text = project(ctx, question, choices);
            colInfo = lastColInfo;
            segments = splitByLength(text);
        } catch (Exception e) {
            question = "[PROJECT_FAILED] " + e.toString();
        }
        FlowEngine.instance().register("question", question);
        FlowEngine.instance().register("choices", choices);
        FlowEngine.instance().register("answerKey", answerKey);
        FlowEngine.instance().register("segments", segments);
        FlowEngine.instance().register("colInfo", colInfo);
    }

    /** 最近一次投影保留的列，跨方法传递用 */
    private String lastColInfo = "";

    private String project(String ctx, String question, String choices) {
        lastColInfo = "";
        String[] lines = ctx.split("\n", -1);
        if (lines.length < 3) {
            return ctx;
        }
        String head = lines[0].trim();
        String[] heads = head.split("\\s{2,}");
        if (heads.length < 3) {
            return ctx;
        }

        List<String[]> all = new ArrayList<String[]>();
        int tried = 0;
        for (int i = 1; i < lines.length; i++) {
            String ln = lines[i];
            if (ln == null || ln.trim().isEmpty()) {
                continue;
            }
            tried++;
            String[] parts = align(ln.trim().split("\\s{2,}"), heads.length);
            if (parts != null) {
                all.add(parts);
            }
        }
        if (tried == 0 || all.size() * 1.0 / tried < MIN_PARSE_RATE || all.size() < 10) {
            return ctx;
        }

        int[] cols = pickCols(heads, question, choices);
        if (cols == null || cols.length < 2) {
            return ctx;
        }

        StringBuilder sb = new StringBuilder(all.size() * 40);
        sb.append("# 列：");
        for (int j = 0; j < cols.length; j++) {
            if (j > 0) {
                sb.append(" | ");
            }
            sb.append(heads[cols[j]]);
        }
        sb.append('\n');
        for (String[] r : all) {
            for (int j = 0; j < cols.length; j++) {
                if (j > 0) {
                    sb.append(" | ");
                }
                sb.append(r[cols[j]]);
            }
            sb.append('\n');
        }
        lastColInfo = sb.substring(0, Math.min(sb.length(), sb.indexOf("\n")));
        return sb.toString();
    }

    /**
     * 把按空白切出来的字段补齐到表头列数。
     * 这张表的首行是 "T.CFE 2022-03-04 09:30:00"，代码与时间在一个字段里，
     * 而表头把 Code 和 Date 分成了两列，所以要按第一个空格拆开。
     */
    private String[] align(String[] parts, int want) {
        if (parts.length == want) {
            return parts;
        }
        if (parts.length + 1 == want && parts[0].indexOf(' ') > 0) {
            String[] out = new String[want];
            int sp = parts[0].indexOf(' ');
            out[0] = parts[0].substring(0, sp);
            out[1] = parts[0].substring(sp + 1).trim();
            for (int i = 1; i < parts.length; i++) {
                out[i + 1] = parts[i];
            }
            return out;
        }
        return null;
    }

    /** 让模型从表头里挑列，挑不出来就用全部列 */
    private int[] pickCols(String[] heads, String question, String choices) {
        try {
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < heads.length; i++) {
                if (i > 0) {
                    names.append(" | ");
                }
                names.append(heads[i]);
            }
            String user = "表头列名：\n" + names + "\n\n题目：\n" + question
                    + "\n\n选项：\n" + choices
                    + "\n\n请输出必需的最小列集合（JSON 数组）。";
            String out = QaSupport.callLlm(COL_SYS, user, 1000);
            int a = out.indexOf('[');
            int b = out.lastIndexOf(']');
            if (a < 0 || b <= a) {
                return null;
            }
            JsonNode arr = QaSupport.JSON.readTree(out.substring(a, b + 1));
            List<Integer> idx = new ArrayList<Integer>();
            for (JsonNode n : arr) {
                String want = n.asText("").trim();
                for (int i = 0; i < heads.length; i++) {
                    if (heads[i].equals(want) && !idx.contains(i)) {
                        idx.add(i);
                        break;
                    }
                }
            }
            if (idx.isEmpty()) {
                return null;
            }
            int[] out2 = new int[idx.size()];
            for (int i = 0; i < idx.size(); i++) {
                out2[i] = idx.get(i);
            }
            return out2;
        } catch (Exception e) {
            return null;
        }
    }

    private String splitByLength(String ctx) {
        ArrayNode arr = QaSupport.JSON.createArrayNode();
        if (ctx == null || ctx.isEmpty()) {
            return arr.toString();
        }
        StringBuilder cur = new StringBuilder(CHUNK);
        int start = 0, pos = 0, idx = 0;
        String head = headerLine(ctx);
        for (String ln : ctx.split("\n", -1)) {
            if (cur.length() > 0 && cur.length() + ln.length() + 1 > CHUNK) {
                addSeg(arr, idx++, start, pos, join(head, cur.toString()));
                start = pos;
                cur.setLength(0);
            }
            cur.append(ln).append('\n');
            pos += ln.length() + 1;
        }
        if (cur.length() > 0) {
            addSeg(arr, idx, start, Math.min(pos, ctx.length()), join(head, cur.toString()));
        }
        return arr.toString();
    }

    private String headerLine(String ctx) {
        int nl = ctx.indexOf('\n');
        if (nl <= 0 || nl > 1000) {
            return "";
        }
        return ctx.substring(0, nl).trim();
    }

    private String join(String head, String body) {
        return head.isEmpty() ? body : head + "\n" + body;
    }

    private void addSeg(ArrayNode arr, int i, int start, int end, String text) {
        ObjectNode o = QaSupport.JSON.createObjectNode();
        o.put("i", i);
        o.put("start", start);
        o.put("end", end);
        o.put("text", text);
        arr.add(o);
    }
}

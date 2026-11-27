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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 载入并按文档分册切段
 *
 * FLOW_QA_SPLIT_DOCS
 *
 * 与 FLOW_QA_SPLIT 的分工：那个按"书的标题"切、强制 60 万字符上限；
 * 这个按"重复出现的页眉"切，每份文档本身能进窗口时不设上限。
 *
 * @param qid 题号
 * @result question 题干
 * @result choices 选项
 * @result answerKey 标准答案
 * @result segments 分段结果（每项带 doc 标记与文档顺序）
 */
@FlowComponent
public class FlowCodeQaSplitDocs {

    /** 年报页眉，形如 Annual Report on International Commercial Arbitration in China (2016) */
    private static final Pattern HEADER = Pattern.compile(
            "Annual Report on International Commercial Arbitration in China \\((20\\d\\d)\\)");

    @Flow("FLOW_QA_SPLIT_DOCS")
    public void flowCodeQaSplitDocs(
            String qid
    ) {
        String question = "";
        String choices = "{}";
        String answerKey = "";
        String segments = "[]";
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

            segments = splitByDoc(item.path("context").asText(""));
        } catch (Exception e) {
            question = "[SPLIT_FAILED] " + e.toString();
        }
        FlowEngine.instance().register("question", question);
        FlowEngine.instance().register("choices", choices);
        FlowEngine.instance().register("answerKey", answerKey);
        FlowEngine.instance().register("segments", segments);
    }

    private String splitByDoc(String ctx) {
        ArrayNode arr = QaSupport.JSON.createArrayNode();
        if (ctx == null || ctx.isEmpty()) {
            return arr.toString();
        }

        Map<String, Integer> first = new LinkedHashMap<String, Integer>();
        Matcher m = HEADER.matcher(ctx);
        while (m.find()) {
            String y = m.group(1);
            if (!first.containsKey(y)) {
                first.put(y, m.start());
            }
        }
        if (first.isEmpty()) {
            ObjectNode o = QaSupport.JSON.createObjectNode();
            o.put("i", 0);
            o.put("doc", "ALL");
            o.put("start", 0);
            o.put("end", ctx.length());
            o.put("text", ctx);
            arr.add(o);
            return arr.toString();
        }

        List<Map.Entry<String, Integer>> es =
                new ArrayList<Map.Entry<String, Integer>>(first.entrySet());
        Collections.sort(es, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return a.getValue() - b.getValue();
            }
        });

        int n = es.size();
        for (int k = 0; k < n; k++) {
            int st = (k == 0) ? 0 : es.get(k).getValue();
            int en = (k + 1 < n) ? es.get(k + 1).getValue() : ctx.length();
            if (en <= st) {
                continue;
            }
            ObjectNode o = QaSupport.JSON.createObjectNode();
            o.put("i", k);
            o.put("doc", es.get(k).getKey() + " 年报");
            o.put("start", st);
            o.put("end", en);
            o.put("text", ctx.substring(st, en));
            arr.add(o);
        }
        return arr.toString();
    }
}

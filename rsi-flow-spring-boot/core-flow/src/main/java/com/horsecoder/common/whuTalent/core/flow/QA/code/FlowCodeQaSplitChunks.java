package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 按窗口长度把整份 context 顺序切段，不认任何文档边界
 *
 * FLOW_QA_SPLIT_CHUNKS
 *
 * 与 FLOW_QA_SPLIT_DOCS 的分工：那个按页眉认文档、切完还要逐份摘要；
 * 这个只做一件事——把长文本按 15 万字符切成可以逐段读完的序列。
 * 代码仓库这类材料没有稳定的文档边界，按长度切比按标题切更可靠。
 *
 * @param qid 题号
 * @result question 题干
 * @result choices 选项
 * @result answerKey 标准答案
 * @result segments 顺序分段（每项 i / start / end / text）
 */
@FlowComponent
public class FlowCodeQaSplitChunks {

    /** 单段字符上限：15 万字符是实测不触发网关 524 的稳定线 */
    private static final int CHUNK = 150_000;

    @Flow("FLOW_QA_SPLIT_CHUNKS")
    public void flowCodeQaSplitChunks(
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

            com.fasterxml.jackson.databind.node.ObjectNode c = QaSupport.JSON.createObjectNode();
            for (String k : new String[]{"A", "B", "C", "D"}) {
                c.put(k, item.path("choice_" + k).asText(""));
            }
            choices = c.toString();

            segments = splitByLength(item.path("context").asText(""));
        } catch (Exception e) {
            question = "[SPLIT_FAILED] " + e.toString();
        }
        FlowEngine.instance().register("question", question);
        FlowEngine.instance().register("choices", choices);
        FlowEngine.instance().register("answerKey", answerKey);
        FlowEngine.instance().register("segments", segments);
    }

    private String splitByLength(String ctx) {
        com.fasterxml.jackson.databind.node.ArrayNode arr = QaSupport.JSON.createArrayNode();
        if (ctx == null || ctx.isEmpty()) {
            return arr.toString();
        }

        StringBuilder cur = new StringBuilder(CHUNK);
        int start = 0;
        int pos = 0;
        int idx = 0;
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

    /** 材料首行。表格题的列名只在这一行出现，切段后每段都得带上，否则列含义丢失 */
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

    private void addSeg(com.fasterxml.jackson.databind.node.ArrayNode arr,
                        int i, int start, int end, String text) {
        com.fasterxml.jackson.databind.node.ObjectNode o = QaSupport.JSON.createObjectNode();
        o.put("i", i);
        o.put("start", start);
        o.put("end", end);
        o.put("text", text);
        arr.add(o);
    }
}

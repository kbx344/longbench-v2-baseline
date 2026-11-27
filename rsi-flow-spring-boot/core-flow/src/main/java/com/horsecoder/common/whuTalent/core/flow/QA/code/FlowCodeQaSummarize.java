package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 逐份文档做结构化摘要
 *
 * FLOW_QA_SUMMARIZE
 *
 * 一份文档单独可能仍会超过网关的处理时间，所以内部按 15 万字符再切块，
 * 逐块摘要后按块拼回，一块也不丢。这一步是"分段摘要"真正落地的地方。
 *
 * @param segments 分段结果
 * @result bookSummaries 每份文档的摘要（JSON 数组）
 * @result digest 合成后的摘要正文
 */
@FlowComponent
public class FlowCodeQaSummarize {

    /** 单次送进网关的字符上限：实测 15 万字符以内不触发网关超时 */
    private static final int CHUNK = 150_000;

    private static final String SUMSYS =
            "你在做文献分段摘要。只摘你看到的这部分内容，输出中文，不超过 700 字。"
            + "必须覆盖四项：1) 出现的章/节标题及其主题；2) 是否涉及特别观察专章及讲什么；"
            + "3) 司法审查、仲裁协议、争端解决机制方面的具体进展或新规定；"
            + "4) 案件数量与标的额数据（有就写，没有就写无）。"
            + "只写原文确实有的内容，不要推测、不要补全。";

    @Flow("FLOW_QA_SUMMARIZE")
    public void flowCodeQaSummarize(
            String segments
    ) {
        String bookSummaries = "[]";
        String digest = "";
        try {
            JsonNode segs = QaSupport.JSON.readTree(segments == null ? "[]" : segments);
            ArrayNode arr = QaSupport.JSON.createArrayNode();
            StringBuilder sb = new StringBuilder();
            for (JsonNode s : segs) {
                String doc = s.path("doc").asText(s.path("book").asText("?"));
                String text = s.path("text").asText("");
                String summary = summarizeOne(doc, text);
                ObjectNode o = QaSupport.JSON.createObjectNode();
                o.put("i", s.path("i").asInt());
                o.put("doc", doc);
                o.put("summary", summary);
                arr.add(o);
                sb.append("【").append(doc).append("】\n").append(summary).append("\n\n");
            }
            bookSummaries = arr.toString();
            digest = sb.toString();
        } catch (Exception e) {
            bookSummaries = QaSupport.errorJson(e);
        }
        FlowEngine.instance().register("bookSummaries", bookSummaries);
        FlowEngine.instance().register("digest", digest);
    }

    private String summarizeOne(String doc, String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        int chunkCount = (len + CHUNK - 1) / CHUNK;
        int i = 0;
        int idx = 0;
        while (i < len) {
            int end = Math.min(len, i + CHUNK);
            if (end < len) {
                int k = text.lastIndexOf("\n\n", end);
                if (k > i + CHUNK / 2) {
                    end = k;
                }
            }
            String part = text.substring(i, end);
            idx++;
            String t = callWithRetry(doc, idx, chunkCount, part);
            sb.append("[第").append(idx).append("/").append(chunkCount).append("部分] ")
                    .append(t).append("\n\n");
            i = end;
        }
        return sb.toString();
    }

    private String callWithRetry(String doc, int idx, int total, String part) {
        String user = "这是《" + doc + "》的第 " + idx + "/" + total + " 部分正文：\n\n" + part;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return QaSupport.callLlm(SUMSYS, user, 1200);
            } catch (Exception e) {
                if (attempt == 3) {
                    return "[摘要失败：" + e.getMessage() + "]";
                }
                try {
                    Thread.sleep(6000L * attempt);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return "";
    }
}

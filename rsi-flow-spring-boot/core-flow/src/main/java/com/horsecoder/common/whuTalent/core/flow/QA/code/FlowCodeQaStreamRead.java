package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 顺序逐段读完整份材料，只维护一份滚动笔记
 *
 * FLOW_QA_STREAM_READ
 *
 * 不带任何检索、定位、统计逻辑：第 i 段读进来，与已有笔记合并，输出新笔记，
 * 交给第 i+1 段。读完最后一段时，笔记就是"人把这堆材料从头看到尾会记住的东西"。
 *
 * 与 Q02 的 FLOW_QA_SUMMARIZE 的区别：那个是逐份文档各出摘要、互不相干；
 * 这个是全篇一条线，后面的段落能看到前面留了什么。
 *
 * @param segments 顺序分段
 * @param question 题干
 * @param choices 选项
 * @result memo 滚动笔记
 * @result segCount 实际读过的段数
 */
@FlowComponent
public class FlowCodeQaStreamRead {

    /** 连续这么多段笔记没有变化，就认为后面的段落都是噪声，提前收尾 */
    private static final int IDLE_LIMIT = 3;

    private static final String READ_SYS =
            "你在逐段阅读一份长材料，目标是回答用户给出的选择题。"
            + "每收到一段，只做两件事：摘出与选项直接相关的原文（原样引用，每处不超过三行），"
            + "并把它并进已有笔记；然后输出更新后的完整笔记，不超过 800 字。"
            + "笔记只写事实和原文引用，不写评价、不下结论、不猜选项。"
            + "若材料是表格或数值序列、题目要求比较或统计，就按题目的口径逐段计算本段的结果"
            + "（时间或行标识 + 数值），与笔记里已有的最优比较后只留更优的那一条，不要罗列整段数据。"
            + "本段确实没有相关内容时，原样输出已有笔记，不要为了填满而补充。";

    @Flow("FLOW_QA_STREAM_READ")
    public void flowCodeQaStreamRead(
            String segments,
            String question,
            String choices,
            String idleLimit
    ) {
        String memo = "（空）";
        int readCount = 0;
        String segInfo = "";
        int idleGate = IDLE_LIMIT;
        try {
            if (idleLimit != null && !idleLimit.trim().isEmpty()) {
                idleGate = Integer.parseInt(idleLimit.trim());
            }
        } catch (Exception ignored) {
            idleGate = IDLE_LIMIT;
        }
        try {
            JsonNode segs = QaSupport.JSON.readTree(segments == null ? "[]" : segments);
            int total = segs.size();
            int idle = 0;
            String prev = null;
            for (JsonNode s : segs) {
                readCount++;
                memo = readOne(memo, question, choices, s.path("text").asText(""), readCount, total);
                if (prev != null && prev.equals(memo)) {
                    idle++;
                } else {
                    idle = 0;
                }
                prev = memo;
                if (idleGate > 0 && idle >= idleGate) {
                    break;
                }
            }
            segInfo = readCount + "/" + total;
        } catch (Exception e) {
            memo = "[STREAM_FAILED] " + e.toString();
        }
        FlowEngine.instance().register("memo", memo);
        FlowEngine.instance().register("segCount", String.valueOf(readCount));
        FlowEngine.instance().register("segInfo", segInfo);
    }

    private String readOne(String memo, String question, String choices,
                           String body, int idx, int total) {
        String user = "题目：\n" + question + "\n\n四个选项：\n" + choices
                + "\n\n已有笔记：\n" + memo
                + "\n\n本段是第 " + idx + "/" + total + " 段，正文如下：\n" + body;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return QaSupport.callLlm(READ_SYS, user, 2000).trim();
            } catch (Exception e) {
                if (attempt == 3) {
                    return memo;
                }
                try {
                    Thread.sleep(6000L * attempt);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return memo;
    }
}

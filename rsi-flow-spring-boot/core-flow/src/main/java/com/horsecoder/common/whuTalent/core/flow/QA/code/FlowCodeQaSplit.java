package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 载入并切段
 *
 * FLOW_QA_SPLIT
 *
 * @param qid 题号
 * @result question 题干
 * @result choices 选项
 * @result answerKey 标准答案
 * @result segments 分段结果
 */
@FlowComponent
public class FlowCodeQaSplit {

    @Flow("FLOW_QA_SPLIT")
    public void flowCodeQaSplit(
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

            segments = QaSupport.buildSegments(item.path("context").asText(""));
        } catch (Exception e) {
            question = "[SPLIT_FAILED] " + e.toString();
        }
        FlowEngine.instance().register("question", question);
        FlowEngine.instance().register("choices", choices);
        FlowEngine.instance().register("answerKey", answerKey);
        FlowEngine.instance().register("segments", segments);
    }
}

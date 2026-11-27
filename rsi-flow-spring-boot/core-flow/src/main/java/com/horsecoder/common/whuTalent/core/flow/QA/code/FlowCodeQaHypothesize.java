package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 选项压成命题
 *
 * FLOW_QA_HYPOTHESIZE
 *
 * @param question 题干
 * @param choices 选项
 * @result propositions 四条独立命题
 */
@FlowComponent
public class FlowCodeQaHypothesize {

    private static final String SYSTEM = "你是命题化改写器。用户给一道选择题，请把 A/B/C/D 四个选项各改写成一条独立、"
            + "可证伪的命题，并给出用于在英文原文中检索这条命题的关键词短语。要求："
            + "1) probes 必须是原文里可能出现的英文词或短语，2 到 5 个，不要臆造人名或生造词组；"
            + "2) 选项里若有 mainly / most / primary 这类限定词，改写时要保住这个限定，不要扩成一般陈述；"
            + "3) 每条命题要能说清'在哪段原文被推翻就为假'；"
            + "4) 只输出 JSON 数组，不要任何解释文字。格式："
            + "[{\"id\":\"A\",\"claim\":\"...\",\"probes\":[\"...\",\"...\"]}]";

    @Flow("FLOW_QA_HYPOTHESIZE")
    public void flowCodeQaHypothesize(
            String question,
            String choices
    ) {
        String props = "[]";
        try {
            StringBuilder usr = new StringBuilder();
            usr.append("题干：").append(question).append("\n\n选项：\n").append(choices);

            String out = QaSupport.callLlm(SYSTEM, usr.toString());
            String cleaned = QaSupport.stripFence(out);
            JsonNode node = QaSupport.JSON.readTree(cleaned);
            if (node.isArray()) {
                props = node.toString();
            } else {
                props = "{\"error\":" + QaSupport.jstr("模型返回不是数组：" + QaSupport.head(out, 200)) + "}";
            }
        } catch (Exception e) {
            props = QaSupport.errorJson(e);
        }
        FlowEngine.instance().register("propositions", props);
    }
}

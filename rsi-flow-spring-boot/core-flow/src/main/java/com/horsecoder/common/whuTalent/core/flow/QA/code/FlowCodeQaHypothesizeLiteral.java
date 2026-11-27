package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 选项压成命题（字面措辞版）
 *
 * FLOW_QA_HYPOTHESIZE_LITERAL
 *
 * 和 FLOW_QA_HYPOTHESIZE 的区别只有一条：probes 必须照抄选项原文的名词短语，
 * 不许替换成同义词。
 *
 * 通用版不需要这条：Q01 的答案句靠"sacrifice"这种选项里没有、原文里才有的词才能命中，
 * 一旦禁止模型补充同义表述，Q01 的证据会被砍掉。
 * 字面版需要这条：Q02 的 D 选项写的是 subject matter has continued to increase，
 * 若被改写成 amount in dispute，原文里"这个名词从不表示增长"的破绽就被抹平了。
 *
 * @param question 题干
 * @param choices 选项
 * @result propositions 四条独立命题
 */
@FlowComponent
public class FlowCodeQaHypothesizeLiteral {

    private static final String SYSTEM = "你是命题化改写器。用户给一道选择题，请把 A/B/C/D 四个选项各改写成一条独立、"
            + "可证伪的命题，并给出用于在英文原文中检索这条命题的关键词短语。要求："
            + "1) probes 必须是原文里可能出现的英文词或短语，2 到 6 个，不要臆造人名或生造词组；"
            + "2) 选项里若有 mainly / most / primary 这类限定词，改写时要保住这个限定，不要扩成一般陈述；"
            + "3) 每条命题要能说清'在哪段原文被推翻就为假'；"
            + "4) 区分两种情况，处理方式相反："
            + "(a) 概念不许替换：选项写 subject matter 就查 subject matter，不能改写成概念相近的 amount in dispute；"
            + "选项写 continued to increase 就查 continued to increase，不能拆成 increase；"
            + "(b) 同一实体的不同写法必须列全：选项写 Hong Kong, Macao and Taiwan 这类全称时，"
            + "原文常用缩写 HMT、HMT-related，全称和缩写都要给；机构名、规则名、条约名同理，"
            + "正式全称和通行缩写一并列出；"
            + "5) 只输出 JSON 数组，不要任何解释文字。格式："
            + "[{\"id\":\"A\",\"claim\":\"...\",\"probes\":[\"...\",\"...\"]}]";

    @Flow("FLOW_QA_HYPOTHESIZE_LITERAL")
    public void flowCodeQaHypothesizeLiteral(
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

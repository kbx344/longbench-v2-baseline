package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;

/**
 * 裁决与判分
 *
 * FLOW_QA_DECIDE
 *
 * @param question 题干
 * @param choices 选项
 * @param evidence 命题对应原文片段
 * @param answerKey 标准答案
 * @result answer 模型给出的答案
 * @result correct 是否正确
 * @result verdict 模型裁决原文
 */
@FlowComponent
public class FlowCodeQaDecide {

    private static final String SYSTEM = "你是严格的事实裁决者。只依据用户给出的原文片段，判断 A/B/C/D 四个选项中哪一个为真。"
            + "注意三点："
            + "1) 字面与原文高度重合、只替换了一个关键概念的选项，通常为假；"
            + "2) 同一措辞在两本书里含义可能不同，要看它出现在哪一本的语境里；"
            + "3) 两个分句组成的选项，只要有一个分句被推翻，整条即为假。"
            + "若片段不足以定论，answer 填 null。只输出 JSON：{\"answer\":\"A\",\"reason\":\"一句话\"}";

    @Flow("FLOW_QA_DECIDE")
    public void flowCodeQaDecide(
            String question,
            String choices,
            String evidence,
            String answerKey
    ) {
        String answer = "?";
        String correct = "false";
        String verdict = "";
        try {
            StringBuilder usr = new StringBuilder();
            usr.append("题干：").append(question).append("\n\n");
            usr.append("选项：\n").append(choices).append("\n\n");
            usr.append("原文片段：\n").append(QaSupport.truncate(evidence, QaSupport.MAX_EVIDENCE_CHARS + 3000));

            String out = QaSupport.callLlm(SYSTEM, usr.toString(), 4096);
            verdict = out;
            answer = QaSupport.parseAnswer(out);
            if (answer != null && answer.length() == 1
                    && answerKey != null && answer.equalsIgnoreCase(answerKey.trim())) {
                correct = "true";
            }
        } catch (Exception e) {
            answer = "ERR:" + e.getClass().getSimpleName();
            verdict = e.getMessage() == null ? "" : e.getMessage();
        }
        FlowEngine.instance().register("answer", answer);
        FlowEngine.instance().register("correct", correct);
        FlowEngine.instance().register("verdict", verdict);
    }
}

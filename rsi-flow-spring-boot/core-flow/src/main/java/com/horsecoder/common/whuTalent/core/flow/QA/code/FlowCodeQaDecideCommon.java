package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;

/**
 * 多实体对照的裁决：判断哪个维度不是两家的共同特征
 *
 * FLOW_QA_DECIDE_COMMON
 *
 * 与 FLOW_QA_DECIDE_MEMO 的区别只在一处口径，但这一处决定对错：
 * 原版判"选被笔记支持且最贴题干的"，多文档题里会退化成"两家都出现过这个词就算共同"。
 * Q05 实测就是这样错的——四个维度里两家都命中了 electric propulsion / battery packs，
 * 于是"四维全共同"，最后只能乱挑一个（选了 C）。
 * 但真正不该共同的是动力构型：Lilium 用 electric jet engines（涵道风扇），
 * Archer 用 distributed electric propulsion（分布式电动推进）。都叫电推进，做法不同。
 *
 * 所以这里的口径是：共同 = 两家在同一做法、同一技术路线上一致，而不是共享一个上位词。
 * 若某维度下两家走的是不同子路线（涵道风扇 / 分布式倾转、自研 / 外采、直营 / 加盟），
 * 该项就不算共同，即使两家都出现了同一个上位词。
 *
 * 不替换原版：FLOW_QA_DECIDE_MEMO 仍供 Q03 与文本题使用，改它会扰动已跑通的结果。
 *
 * @param memo 维度 × 文档的对照片段表
 * @param question 题干
 * @param choices 选项
 * @param answerKey 标准答案
 * @result answer 选项
 * @result correct 是否正确
 * @result verdict 裁决原文
 */
@FlowComponent
public class FlowCodeQaDecideCommon {

    private static final String SYS =
            "你在对照两份公司文档，判断四个维度里哪一个不是两家公司的共同特征。"
            + "判共同的标准不是'两家都出现过这个词'，而是'两家在同一做法、同一技术路线上一致'。"
            + "逐项检查两件事：1) 该维度在两家文档里是否都有实质对应的原文；"
            + "2) 该维度下的具体做法是否相同——若一家走的是子路线甲、另一家走的是子路线乙"
            + "（例如一家用涵道风扇、一家用分布式倾转；一家自研、一家外采；一家直营、一家加盟），"
            + "则这一项不属于共同特征，即使两家都出现了同一个上位词。"
            + "四个选项必须逐项判，最后只选那个'两家做法实质不同'的维度；"
            + "若多项只是措辞不同而做法一致，选差异最实质的一项。"
            + "输出 JSON：{\"answer\":\"A|B|C|D\",\"reason\":\"...\"}，"
            + "reason 必须分别引用两家在该维度上的具体原文用词。只输出 JSON。";

    @Flow("FLOW_QA_DECIDE_COMMON")
    public void flowCodeQaDecideCommon(
            String memo,
            String question,
            String choices,
            String answerKey
    ) {
        String answer = "?";
        String correct = "false";
        String verdict = "";
        try {
            String user = "题目：\n" + question + "\n\n四个选项：\n" + choices
                    + "\n\n两家文档的对照片段：\n" + QaSupport.truncate(memo, 30_000);
            String out = QaSupport.callLlm(SYS, user, 3500);
            answer = QaSupport.parseAnswer(out);
            verdict = QaSupport.stripFence(out);
            correct = String.valueOf(answer.equals(answerKey == null ? "" : answerKey.trim()));
        } catch (Exception e) {
            verdict = QaSupport.errorJson(e);
        }
        FlowEngine.instance().register("answer", answer);
        FlowEngine.instance().register("correct", correct);
        FlowEngine.instance().register("verdict", verdict);
    }
}

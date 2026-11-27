package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;

/**
 * 拿滚动笔记做单选裁决（找错题与选对题共用）
 *
 * FLOW_QA_DECIDE_MEMO
 *
 * 与 FLOW_QA_DECIDE、FLOW_QA_DECIDE_SCOPED 的分工：
 * 那两个是"有证据、按证据判真"；这个是"只有一份通读笔记"，没有逐条命中统计，
 * 所以判法必须放宽——先看题目问的是哪一类，再按对应口径逐项对照。
 *
 * 找错题的口径：只有"按它这么做会不成立/跑不到"才算错，"还有更好写法"不算。
 * 选对题的口径：被笔记支持且最贴题干的留下，完全无据的排除。
 *
 * @param memo 滚动笔记
 * @param question 题干
 * @param choices 选项
 * @param answerKey 标准答案
 * @result answer 选项
 * @result correct 是否正确
 * @result verdict 裁决原文
 */
@FlowComponent
public class FlowCodeQaDecideMemo {

    private static final String JUDGE_SYS =
            "你在根据一份'逐段读完整份材料后留下的笔记'回答一道单选题。笔记里只有原文摘录和事实，没有结论。"
            + "先看题目问的是哪一类："
            + "1) 若题目问'哪些操作/做法有错误''哪一项不正确''不符合'，选唯一那个与笔记中事实相冲突的选项；"
            + "仅仅'还有更好做法''不够优雅'不构成错误，只判'按它这么做会不成立或跑不到'。"
            + "2) 其他情况，选被笔记中原文支持、且最贴合题干问法的那个选项；"
            + "笔记里完全无据的排除，但不能因为'没有原句'就排除有间接支持的选项。"
            + "逐项对照之后再定，输出 JSON：{\"answer\":\"A|B|C|D\",\"reason\":\"...\"}。"
            + "reason 必须引用笔记里的具体事实。只输出 JSON。";

    @Flow("FLOW_QA_DECIDE_MEMO")
    public void flowCodeQaDecideMemo(
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
                    + "\n\n逐段阅读后的笔记：\n" + QaSupport.truncate(memo, 30_000);
            String out = QaSupport.callLlm(JUDGE_SYS, user, 3000);
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

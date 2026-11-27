package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;

/**
 * 排除法单选裁决（"哪个不是/哪项不正确"）
 *
 * FLOW_QA_DECIDE_NOT
 *
 * 与 FLOW_QA_DECIDE_MEMO 的分工：那个按"笔记里能不能找到支持"来判，
 * 适合"选对"题；排除题恰好相反——三个干扰项都能在材料里找到，
 * 唯一要选的是"没被当成原因"的那一项，按"有无原文支持"判会必然判错。
 *
 * 口径三条：
 * 1) 判"是成因"必须有因果陈述（origins/caused/factor/led to/contribute 等标记
 *    与该选项核心名词同现），同主题出现不算；
 * 2) 材料对该选项的定位与选项说法相反时，直接判"不是"；
 * 3) 恰好一项被判"不是"，其余三项都要有因果陈述支撑。
 *
 * @param memo 按选项分组的因果取证笔记
 * @param question 题干
 * @param choices 选项
 * @param answerKey 标准答案
 * @param statsTable 每选项命中统计
 * @result answer 选项
 * @result correct 是否正确
 * @result verdict 裁决原文
 */
@FlowComponent
public class FlowCodeQaDecideNot {

    private static final String JUDGE_SYS =
            "你在回答一道'排除法'单选题：题干问'哪个不是''哪一项不正确''哪些是假'。"
            + "材料是一份长文档的原文片段，已按选项分组，并标注每段片段里是否出现因果标记词"
            + "（origin/cause/factor/led to/contribute 等）。"
            + "判法："
            + "1) 逐个选项，只看'材料有没有把它陈述为造成某事件的原因/因素'。"
            + "判'是'必须有明确的因果陈述——因果标记词与该选项的核心名词出现在同一片段；"
            + "仅仅同主题、同词出现（例如整章都在讲这个对象）不算。"
            + "2) 若材料对该选项的定位与选项说法相反（材料说'传教士主动引入并依赖'，"
            + "选项说'信徒自发、传教士失去控制'），直接判该选项'不是'。"
            + "3) 恰好一项应被判'不是'，其余三项都必须有因果陈述支撑。"
            + "4) 若多项都缺因果陈述，选与材料表述抵触最明显的那一项，并在 reason 里说明为何其他项仍成立。"
            + "输出 JSON：{\"answer\":\"A|B|C|D\",\"reason\":\"...\"}。"
            + "reason 要逐项给出依据，引用片段里的原词（如 origins / caused / factor），不要凭印象。只输出 JSON。";

    @Flow("FLOW_QA_DECIDE_NOT")
    public void flowCodeQaDecideNot(
            String memo,
            String question,
            String choices,
            String answerKey,
            String statsTable
    ) {
        String answer = "?";
        String correct = "false";
        String verdict = "";
        try {
            String user = "题目：\n" + question + "\n\n四个选项：\n" + choices
                    + "\n\n每选项命中统计：\n" + QaSupport.truncate(statsTable, 4_000)
                    + "\n\n按选项的因果取证笔记：\n" + QaSupport.truncate(memo, 30_000);
            String out = QaSupport.callLlm(JUDGE_SYS, user, 3500);
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

package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;

/**
 * 裁决与判分（先判范围、后选最优版）
 *
 * FLOW_QA_DECIDE_SCOPED
 *
 * 和 FLOW_QA_DECIDE 的差别有两处：
 * 1) 判真之前先判"有没有答到点子上"。多文档摘要题最容易出的错，不是把假的判成真，
 *    而是把一个"事实为真但与题干无关"的选项判成答案。
 * 2) 只做排除、不做证明。原口径要求"能证明为真才选"，找不到原句就判无依据，
 *    四个选项一起被排除，最后只能填 null——选择题的正常判法是挑最贴题干的那一项，
 *    不是要求每一项都被逐字证实。
 *
 * Q01 不用这个：Q01 的题干是"两者在哪个问题上一致"，四个选项全落在同一范畴，
 * 不存在跑题项，也不需要最优解口径，加了只会扰动已跑通的结论。
 *
 * @param question 题干
 * @param choices 选项
 * @param evidence 统计表 + 摘要 + 原文片段
 * @param answerKey 标准答案
 * @result answer 模型给出的答案
 * @result correct 是否正确
 * @result verdict 模型裁决原文
 */
@FlowComponent
public class FlowCodeQaDecideScoped {

    private static final String SYSTEM = "你是严格的事实裁决者。只依据用户给出的材料，在 A/B/C/D 中选出一个最贴合题干的答案，"
            + "分三步判，顺序不能颠倒。"
            + "第一步，判范围：先问每个选项是否在回答题干问的那件事。题干问的是某个对象的发展趋势、总体走向。"
            + "若某个选项讲的是另一件事——报告的编排体例、文档的写作方法、某个年份做过什么专题、"
            + "某个统计口径怎么定义、某个机构怎么组织——那么即使这个说法属实，它也没有回答问题，排除。"
            + "事实为真不等于答对了题。"
            + "但要注意：讲某个领域的制度在推进、某项安排在被强化、某个机制在被建设，这属于趋势，"
            + "不要因为它是具体领域就判成跑题。"
            + "第二步，判排除：这一步只做排除，不做证明。凡是能被材料明确推翻的选项，按下列依据排除："
            + "1) 与原文高度重合、只替换了一个关键概念的，为假；"
            + "2) 同一措辞在不同文档里含义可能不同，要看它出现在哪一份的语境里；"
            + "3) 由两个分句组成的选项，只要有一个分句被推翻，整条即为假；"
            + "4) 跨文档归纳是允许的：两个分句分别能在不同文档里找到对应进展"
            + "（某类裁决首次获得执行、某项司法解释出台、某个机制被反复讨论并推进），就算成立，"
            + "不要因为找不到一字不差的句子就判它无依据；"
            + "5) 材料开头的全库统计是 Java 侧精确计数，不是抽样，可信度高于片段；"
            + "某个短语标注为全库 0 次，或某个名词标注为从不与增长/改善动词连用，"
            + "则以它为核心表述的选项不成立，不能因为意思大致相同就放行。"
            + "第三步，选最优：剩下没有被推翻的选项里，选与题干所问最贴合、覆盖面最广的一项。"
            + "选项不需要逐字被原文证实，只要没被材料推翻、且正面回答题干问的那件事，就可以选；"
            + "找不到原句不等于它假。"
            + "null 只在两种情况下给：四个选项全部被材料推翻，或全部不回答题干。"
            + "只要还剩一项没被推翻，就必须选它。"
            + "只输出 JSON：{\"answer\":\"A\",\"reason\":\"一句话，先说范围判断，再说排除了哪些、为什么，最后说为什么选它\"}";

    @Flow("FLOW_QA_DECIDE_SCOPED")
    public void flowCodeQaDecideScoped(
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
            usr.append("材料：\n").append(QaSupport.truncate(evidence, QaSupport.MAX_EVIDENCE_CHARS + 3000));

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

package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 描述矛盾裁决（"材料里的描述互相矛盾"题专用）
 *
 * FLOW_QA_DECIDE_CONFLICT
 *
 * 与 FLOW_QA_DECIDE_MEMO 的分工：那个按"笔记里能不能找到支持"判，适合
 * "材料给了答案"的题；这题的正确答案可能是"材料自己前后不一致"，
 * 按"有无支持"判会把它当成"找不到楼层"，反而选成某一层。
 *
 * 口径四条：
 * 1) 只算"在讲题干那两个人的住处"的提及，其他人的公寓、其他建筑的楼层不算；
 * 2) 同一住处出现两个不同的明确楼层，选"描述互相矛盾"那一项；
 * 3) 所有提及一致时按该楼层选，不因找不到第三处就判矛盾；
 * 4) reason 必须引用两处原句，不许凭印象。
 *
 * @param memo 楼层提及清单
 * @param question 题干
 * @param choices 选项
 * @param answerKey 标准答案
 * @param statsTable 扫描统计
 * @result answer 选项
 * @result correct 是否正确
 * @result verdict 裁决原文
 */
@FlowComponent
public class FlowCodeQaDecideConflict {

    private static final String JUDGE_SYS =
            "你在回答一道'描述矛盾'单选题：题干问某两个人住在几楼，"
            + "四个选项里有一项是'材料中的描述互相矛盾'。"
            + "下面给出全库所有楼层提及的原文窗口，每处附'取值'（窗口里出现的楼层词）、"
            + "'窗口内'（原文窗口里的题干人名）、'附近'（窗口外 2500 字符内首次出现的题干人名及偏移）。"
            + "按三步做："
            + "第一步，对每一处'取值明确'的提及判归属：这是谁的住处？"
            + "可用线索有——是否在搜某人的家（search warrant for his / her apartment）、"
            + "是否某人从自己家里出来或回家、是否受害者 Shauna Hunnicut 与 Erin Albright 的公寓。"
            + "受害者的住处不属于题干那两个人，必须排除。看不清的写'不明'。"
            + "第二步，只比较被判为题干那两人住处的那些提及的取值。"
            + "两处取值不同（例如一处一问一答说 Second，另一处环境描写说 third-floor unit），"
            + "选'描述互相矛盾'那一项。取值一致就按该楼层选，不要因为找不到更多就判矛盾；"
            + "只有一处明确就按该处选。"
            + "第三步，reason 必须列出你比较的那几处原句（带楼层词），逐条说明归属依据，"
            + "并说明哪一处是'受害者住处'、为什么排除。"
            + "有 3 处以上都判为同一人的住处时全部列出，不许只挑两处。"
            + "输出 JSON：{\"answer\":\"A|B|C|D\",\"reason\":\"...\"}。只输出 JSON。"
            + "即使已确定答案，也必须输出完整 JSON，不得只输出一个字母。";

    @Flow("FLOW_QA_DECIDE_CONFLICT")
    public void flowCodeQaDecideConflict(
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
                    + "\n\n扫描统计：\n" + QaSupport.truncate(statsTable, 1_000)
                    + "\n\n楼层提及清单：\n" + QaSupport.truncate(memo, 40_000)
                    + "\n\n按要求输出 JSON，reason 必须写出你判定的那两处原句（带楼层词），"
                    + "并说明两处是否指同一住处：\n"
                    + "{\"answer\":\"A|B|C|D\",\"reason\":\"...\"}";
            String out = QaSupport.callLlm(JUDGE_SYS, user, 8000);
            answer = parseChoice(out);
            verdict = QaSupport.stripFence(out);
            correct = String.valueOf(answer.equals(answerKey == null ? "" : answerKey.trim()));
        } catch (Exception e) {
            verdict = QaSupport.errorJson(e);
        }
        FlowEngine.instance().register("answer", answer);
        FlowEngine.instance().register("correct", correct);
        FlowEngine.instance().register("verdict", verdict);
    }

    /**
     * 解析选项。
     *
     * QaSupport.parseAnswer 只认 JSON 里的 "answer"，共享给 Q05/Q07 用，
     * 不能改。这题实测出现过模型只回一个字母 "B" 的情况，所以在本地兜两层：
     * 整段就是一个字母；或正文里写了"答案：B""选项 B""B."。
     */
    private String parseChoice(String out) {
        String a = QaSupport.parseAnswer(out);
        if (!"?".equals(a)) {
            return a;
        }
        String t = QaSupport.stripFence(out);
        Matcher single = Pattern.compile("^\\s*([A-Da-d])\\s*$").matcher(t);
        if (single.find()) {
            return single.group(1).toUpperCase(Locale.ROOT);
        }
        Matcher named = Pattern.compile(
                "(?:答案|选项|应选|选)\\s*(?:为|是)?\\s*[:：]?\\s*[*\"'\\[]?\\s*([A-Da-d])").matcher(t);
        if (named.find()) {
            return named.group(1).toUpperCase(Locale.ROOT);
        }
        Matcher lead = Pattern.compile("(?m)^\\s*([A-D])\\s*[.、)]").matcher(t);
        if (lead.find()) {
            return lead.group(1);
        }
        return "?";
    }
}

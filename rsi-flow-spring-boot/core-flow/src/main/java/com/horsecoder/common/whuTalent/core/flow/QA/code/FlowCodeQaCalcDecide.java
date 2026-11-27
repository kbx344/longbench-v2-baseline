package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 差值裁决（跨年数值题专用）
 *
 * FLOW_QA_CALC_DECIDE
 *
 * 与 FLOW_QA_DECIDE_MEMO 的分工：那个靠笔记里的文字支持判选项；这题的
 * 选项是四个百分比，没有"支持"可找，只有"算出来的差值是几"。
 * 让模型读四个数字去猜，等于让它做算术——这题不需要模型。
 *
 * 做法：取 FLOW_QA_NUM_SCAN 给出的各年剩余占比，后一年减前一年得差值，
 * 再与四个选项里的百分比逐一比对，唯一相等者即答案。
 * 解析或比对失败才退回模型。
 *
 * @param stats 年度 -> 剩余占比
 * @param memo 扫描记录
 * @param question 题干
 * @param choices 选项
 * @param answerKey 标准答案
 * @result answer 选项
 * @result correct 是否正确
 * @result verdict 算式与依据
 */
@FlowComponent
public class FlowCodeQaCalcDecide {

    private static final Pattern PCT = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*%");

    private static final String FALLBACK_SYS =
            "你在做一道跨年占比变化的单选题。下面给出扫描到的各年数据与选项。"
                    + "请按'后一年减前一年'算差值，选数值相等的那个选项。"
                    + "输出 JSON：{\"answer\":\"A|B|C|D\",\"reason\":\"算式\"}。只输出 JSON。";

    @Flow("FLOW_QA_CALC_DECIDE")
    public void flowCodeQaCalcDecide(
            String stats,
            String memo,
            String question,
            String choices,
            String answerKey
    ) {
        String answer = "?";
        String correct = "false";
        String verdict = "";
        try {
            JsonNode st = QaSupport.JSON.readTree(stats == null || stats.trim().isEmpty() ? "{}" : stats);
            List<String> years = new ArrayList<>();
            Iterator<String> fit = st.fieldNames();
            while (fit.hasNext()) {
                years.add(fit.next());
            }

            if (years.size() >= 2) {
                years.sort(null);
                String y0 = years.get(0);
                String y1 = years.get(years.size() - 1);
                int v0 = st.path(y0).asInt();
                int v1 = st.path(y1).asInt();
                int diff = v1 - v0;

                JsonNode ch = QaSupport.JSON.readTree(choices == null || choices.isEmpty() ? "{}" : choices);
                Map<String, Double> nums = new LinkedHashMap<>();
                for (String k : new String[]{"A", "B", "C", "D"}) {
                    Matcher m = PCT.matcher(ch.path(k).asText(""));
                    if (m.find()) {
                        nums.put(k, Double.parseDouble(m.group(1)));
                    }
                }

                List<String> hit = new ArrayList<>();
                for (Map.Entry<String, Double> e : nums.entrySet()) {
                    if (Math.abs(Math.abs(e.getValue()) - Math.abs(diff)) < 0.05) {
                        hit.add(e.getKey());
                    }
                }

                StringBuilder sb = new StringBuilder();
                sb.append(y0).append(" 其余国家 ").append(v0).append("%；")
                        .append(y1).append(" 其余国家 ").append(v1).append("%；")
                        .append(y1).append(" - ").append(y0).append(" = ")
                        .append(diff > 0 ? "+" : "").append(diff).append("%");
                if (hit.size() == 1) {
                    answer = hit.get(0);
                    sb.append("，与选项 ").append(answer).append("（")
                            .append(nums.get(answer)).append("%）相等");
                } else {
                    sb.append("；候选 ").append(hit).append(" 不唯一");
                }
                verdict = sb.toString();
                correct = String.valueOf(answer.equals(answerKey == null ? "" : answerKey.trim()));

                if (hit.size() != 1) {
                    answer = "?";
                    correct = "false";
                    verdict = fallback(question, choices, memo, verdict);
                    answer = lastAnswer;
                    correct = String.valueOf(answer.equals(answerKey == null ? "" : answerKey.trim()));
                }
            } else {
                verdict = fallback(question, choices, memo, "扫描到的年份不足两个，算不了差值。");
                answer = lastAnswer;
                correct = String.valueOf(answer.equals(answerKey == null ? "" : answerKey.trim()));
            }
        } catch (Exception e) {
            verdict = QaSupport.errorJson(e);
        }
        FlowEngine.instance().register("answer", answer);
        FlowEngine.instance().register("correct", correct);
        FlowEngine.instance().register("verdict", verdict == null ? "" : verdict);
    }

    private String lastAnswer = "?";

    private String fallback(String question, String choices, String memo, String note) {
        try {
            String user = "题目：\n" + question + "\n\n四个选项：\n" + choices
                    + "\n\n扫描到的数据：\n" + QaSupport.truncate(memo, 8_000)
                    + "\n\n注意：" + note;
            String out = QaSupport.callLlm(FALLBACK_SYS, user, 3000);
            String a = QaSupport.parseAnswer(out);
            if ("?".equals(a)) {
                Matcher m = Pattern.compile("(?m)^\\s*([A-D])\\s*[.、)]").matcher(out);
                if (m.find()) {
                    a = m.group(1);
                }
            }
            lastAnswer = a;
            return QaSupport.stripFence(out);
        } catch (Exception e) {
            lastAnswer = "?";
            return QaSupport.errorJson(e);
        }
    }

    private String up(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }
}

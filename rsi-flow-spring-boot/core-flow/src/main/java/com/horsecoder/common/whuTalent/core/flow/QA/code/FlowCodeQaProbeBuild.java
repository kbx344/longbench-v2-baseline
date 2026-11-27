package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 把四个选项翻译成原文检索词
 *
 * FLOW_QA_PROBE_BUILD
 *
 * 为什么需要它：选项是人写的概括语（"High R&D investment"），原文用的是
 * "research and development expenses" 这类具体表述，直接拿选项原文去检索命中率很低。
 * Q02 的教训是可读性强的词（subject matter、dispute resolution）全库几十次却全不对题，
 * 真正有用的是原文里逐字出现的短语，所以这一步要做的是"翻译成原文用词"，不是"分词"。
 *
 * 输出直接喂给 FLOW_QA_GATHER_BUCKETS：每个选项一个命题，每个命题一组探针。
 * 一次调用、输入只有四个选项，成本可忽略。
 *
 * @param question 题干
 * @param choices 选项
 * @result propositions 命题 + 探针组
 */
@FlowComponent
public class FlowCodeQaProbeBuild {

    private static final String SYS =
            "你在把单选题的四个选项翻译成原文里的检索短语，供全文精确检索使用。"
            + "每个选项给出 3 到 6 个英文短语，要求："
            + "1) 必须是该领域文档（招股书、年报、手册、法律文件）里会逐字出现的说法，"
            + "优先给名词短语和固定搭配，不要给整句；"
            + "2) 覆盖同义说法与常见缩写（如 research and development / R&D，"
            + "initial public offering / IPO / listing）；"
            + "3) 不要给过于宽泛的词（business、company、product 这种几乎每页都有的不算）；"
            + "4) 短语全部小写。"
            + "只输出 JSON 数组：[{\"id\":\"A\",\"claim\":\"选项原文\",\"probes\":[\"...\",\"...\"]}, ...]，四个元素，只输出 JSON。";

    @Flow("FLOW_QA_PROBE_BUILD")
    public void flowCodeQaProbeBuild(
            String question,
            String choices
    ) {
        String propositions = "[]";
        try {
            String user = "题目：\n" + question + "\n\n四个选项：\n" + choices
                    + "\n\n请为每个选项输出 3-6 个原文检索短语。";
            // 推理模型的 content 在 max_tokens 偏小时会整段为空，直接回落到选项原文兜底，
            // 这点在 Q01 已经踩过一次（1024 时返回空，4096 才稳定），这里同样给足
            String out = QaSupport.callLlm(SYS, user, 3500);
            int a = out.indexOf('[');
            int b = out.lastIndexOf(']');
            if (a >= 0 && b > a) {
                propositions = out.substring(a, b + 1);
            }
            JsonNode arr = QaSupport.JSON.readTree(propositions);
            if (!arr.isArray() || arr.size() == 0) {
                propositions = fallback(choices);
            }
        } catch (Exception e) {
            propositions = fallback(choices);
        }
        FlowEngine.instance().register("propositions", propositions);
    }

    /** 模型挂了就拿选项原文兜底，至少能跑 */
    private String fallback(String choices) {
        try {
            JsonNode c = QaSupport.JSON.readTree(choices == null ? "{}" : choices);
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (String k : new String[]{"A", "B", "C", "D"}) {
                String v = c.path(k).asText("");
                if (v.isEmpty()) {
                    continue;
                }
                if (!first) {
                    sb.append(",");
                }
                sb.append("{\"id\":\"").append(k).append("\",\"claim\":\"")
                        .append(v.replace("\"", "'")).append("\",\"probes\":[\"")
                        .append(v.toLowerCase().replace("\"", "'")).append("\"]}");
                first = false;
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            return "[]";
        }
    }
}

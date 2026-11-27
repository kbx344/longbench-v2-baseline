package com.horsecoder.common.whuTalent.core.flow.Run.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@FlowComponent
public class FlowCodeRun {

    /**
     * 运行
     * FLOW_CODE_RUN
     *
     * @param structure 结构
     * @result result 运行结果
     */
    @Flow("FLOW_CODE_RUN")
    public void flowCodeRun(String structure) {
        // 初始化流程出参
        String result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(structure);

            List<String> names = new ArrayList<String>();
            JsonNode elements = root.get("elements");
            if (elements != null && elements.isArray()) {
                for (JsonNode el : elements) {
                    JsonNode nameNode = el.get("name");
                    names.add(nameNode == null ? el.path("id").asText() : nameNode.asText());
                }
            }

            int linkCount = root.path("links").size();
            StringBuilder sb = new StringBuilder();
            sb.append("编排解析成功：节点 ").append(names.size())
                    .append(" 个，连线 ").append(linkCount).append(" 条。执行顺序：");
            for (int i = 0; i < names.size(); i++) {
                if (i > 0) sb.append(" -> ");
                sb.append(names.get(i));
            }
            result = sb.toString();
        } catch (Exception e) {
            result = "结构解析失败：" + e.getMessage();
        }

        // 结束流程
        FlowEngine.instance().register("result", result);
    }
}

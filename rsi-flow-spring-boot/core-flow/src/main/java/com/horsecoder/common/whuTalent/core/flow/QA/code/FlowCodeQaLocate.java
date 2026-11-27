package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体定位尝试
 *
 * FLOW_QA_LOCATE
 *
 * @param question 题干
 * @param segments 分段结果
 * @result locateReport 定位报告
 * @result usable 人名入口是否可用
 * @result propositions 命题（可用时写实体名）
 */
@FlowComponent
public class FlowCodeQaLocate {

    @Flow("FLOW_QA_LOCATE")
    public void flowCodeQaLocate(
            String question,
            String segments
    ) {
        String usable = "false";
        String report = "{}";
        String props = "[]";
        try {
            JsonNode segs = QaSupport.JSON.readTree(segments == null ? "[]" : segments);
            List<String> names = QaSupport.extractNames(question);
            int segCount = segs.size();

            ArrayNode nameStats = QaSupport.JSON.createArrayNode();
            Map<String, int[]> bodyPerSeg = new HashMap<>();
            Map<String, Map<Integer, List<Integer>>> argPos = new HashMap<>();

            for (String nm : names) {
                int[] hits = new int[segCount];
                int[] body = new int[segCount];
                Map<Integer, List<Integer>> posMap = new HashMap<>();
                for (int s = 0; s < segCount; s++) {
                    String t = segs.get(s).path("text").asText("");
                    String tl = t.toLowerCase(java.util.Locale.ROOT);
                    String nl = nm.toLowerCase(java.util.Locale.ROOT);
                    int from = 0;
                    while (true) {
                        int pos = tl.indexOf(nl, from);
                        if (pos < 0) {
                            break;
                        }
                        hits[s]++;
                        if (!QaSupport.isScholarly(t, pos)) {
                            body[s]++;
                            posMap.computeIfAbsent(s, k -> new ArrayList<>()).add(pos);
                        }
                        from = pos + nl.length();
                    }
                }
                bodyPerSeg.put(nm, body);
                argPos.put(nm, posMap);

                ObjectNode o = QaSupport.JSON.createObjectNode();
                o.put("name", nm);
                ArrayNode hl = o.putArray("hitsPerSeg");
                ArrayNode bl = o.putArray("argHitsPerSeg");
                for (int v : hits) {
                    hl.add(v);
                }
                for (int v : body) {
                    bl.add(v);
                }
                o.put("hitsTotal", QaSupport.sum(hits));
                o.put("argTotal", QaSupport.sum(body));
                nameStats.add(o);
            }

            ArrayNode pairs = QaSupport.JSON.createArrayNode();
            boolean pairOk = false;
            for (int i = 0; i < names.size(); i++) {
                for (int j = i + 1; j < names.size(); j++) {
                    String a = names.get(i);
                    String b = names.get(j);
                    int co = 0;
                    List<Integer> segHit = new ArrayList<>();
                    for (int s = 0; s < segCount; s++) {
                        List<Integer> la = argPos.get(a).getOrDefault(s, java.util.Collections.emptyList());
                        List<Integer> lb = argPos.get(b).getOrDefault(s, java.util.Collections.emptyList());
                        int c = 0;
                        for (int pa : la) {
                            for (int pb : lb) {
                                if (Math.abs(pa - pb) <= QaSupport.COOCCUR_RADIUS) {
                                    c++;
                                }
                            }
                        }
                        if (c > 0) {
                            co += c;
                            segHit.add(s);
                        }
                    }
                    ObjectNode po = QaSupport.JSON.createObjectNode();
                    po.put("a", a);
                    po.put("b", b);
                    po.put("cooccur", co);
                    ArrayNode sl = po.putArray("segs");
                    for (int s : segHit) {
                        sl.add(s);
                    }
                    pairs.add(po);

                    if (co >= 1 && QaSupport.sum(bodyPerSeg.get(a)) >= 1
                            && QaSupport.sum(bodyPerSeg.get(b)) >= 1) {
                        pairOk = true;
                    }
                }
            }

            boolean singleOk = names.size() == 1 && QaSupport.sum(bodyPerSeg.get(names.get(0))) >= 3;

            usable = (pairOk || singleOk) ? "true" : "false";

            ObjectNode rep = QaSupport.JSON.createObjectNode();
            rep.set("names", nameStats);
            rep.set("pairs", pairs);
            rep.put("usable", usable);
            rep.put("criteria", "argHits 已剔除引文层命中：序言/致谢、注释编号、cf./op. cit/loc. cit、索引条目");
            if (pairOk) {
                rep.put("reason", "存在实体对在正文同段同一语境内共现，人名入口可用");
            } else if (singleOk) {
                rep.put("reason", "单一实体在正文多次出现，人名入口可用");
            } else {
                rep.put("reason", "题面实体从未在正文同一语境内共同出现；命中都落在序言/注释/索引，人名入口不可用");
            }
            report = rep.toString();

            if ("true".equals(usable)) {
                ArrayNode pj = QaSupport.JSON.createArrayNode();
                for (String nm : names) {
                    pj.add(nm);
                }
                props = pj.toString();
            }
        } catch (Exception e) {
            report = QaSupport.errorJson(e);
        }
        FlowEngine.instance().register("locateReport", report);
        FlowEngine.instance().register("usable", usable);
        FlowEngine.instance().register("propositions", props);
    }
}

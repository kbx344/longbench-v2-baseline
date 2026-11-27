package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 分区取证
 *
 * FLOW_QA_GATHER
 *
 * @param propositions 命题
 * @param segments 分段结果
 * @result evidence 命题对应原文片段
 */
@FlowComponent
public class FlowCodeQaGather {

    /** 命中点前后各取多少字符 */
    private static final int WINDOW = 400;

    /** 每个检索词最多留几条：高频词很容易把配额吃光，必须单独限额 */
    private static final int MAX_PER_PROBE = 3;

    /** 每条命题最多留几条 */
    private static final int MAX_PER_PROP = 9;

    @Flow("FLOW_QA_GATHER")
    public void flowCodeQaGather(
            String propositions,
            String segments
    ) {
        String evidence = "{}";
        try {
            JsonNode props = QaSupport.JSON.readTree(
                    propositions == null || propositions.trim().isEmpty() ? "[]" : propositions);
            JsonNode segs = QaSupport.JSON.readTree(segments == null ? "[]" : segments);

            ObjectNode ev = QaSupport.JSON.createObjectNode();
            int budget = QaSupport.MAX_EVIDENCE_CHARS;

            if (props.isArray()) {
                for (JsonNode p : props) {
                    List<String> probes = new ArrayList<>();
                    String pid;
                    if (p.isTextual()) {
                        pid = "E" + ev.size();
                        probes.add(p.asText());
                    } else {
                        pid = p.path("id").asText("P" + ev.size());
                        JsonNode pr = p.path("probes");
                        if (pr.isArray()) {
                            for (JsonNode x : pr) {
                                if (x.isTextual() && !x.asText().trim().isEmpty()) {
                                    probes.add(x.asText().trim());
                                }
                            }
                        }
                        if (probes.isEmpty()) {
                            probes.addAll(QaSupport.keywords(p.path("claim").asText("")));
                        }
                    }

                    // 稀有词优先：命中越少的词越有区分度，先占配额
                    Collections.sort(probes, new Comparator<String>() {
                        @Override
                        public int compare(String a, String b) {
                            return countIn(segs, a) - countIn(segs, b);
                        }
                    });

                    ObjectNode po = QaSupport.JSON.createObjectNode();
                    po.put("id", pid);
                    ArrayNode hits = po.putArray("hits");
                    int cnt = 0;

                    for (String probe : probes) {
                        if (probe.isEmpty() || cnt >= MAX_PER_PROP) {
                            continue;
                        }
                        if (countIn(segs, probe) == 0) {
                            ObjectNode ho = hits.addObject();
                            ho.put("at", -1);
                            ho.put("probe", probe);
                            ho.put("text", "[原文 0 命中：该表述在正文中不存在]");
                            cnt++;
                            continue;
                        }
                        int taken = 0;
                        for (int s = 0; s < segs.size() && taken < MAX_PER_PROBE; s++) {
                            String t = segs.get(s).path("text").asText("");
                            String tl = t.toLowerCase(Locale.ROOT);
                            String pl = probe.toLowerCase(Locale.ROOT);
                            int from = 0;
                            while (taken < MAX_PER_PROBE && cnt < MAX_PER_PROP) {
                                int pos = tl.indexOf(pl, from);
                                if (pos < 0) {
                                    break;
                                }
                                int lo = Math.max(0, pos - WINDOW);
                                int hi = Math.min(t.length(), pos + pl.length() + WINDOW);
                                String snippet = t.substring(lo, hi);
                                if (budget - snippet.length() < 0) {
                                    break;
                                }
                                budget -= snippet.length();
                                ObjectNode ho = hits.addObject();
                                ho.put("seg", s);
                                ho.put("book", segs.get(s).path("book").asText(""));
                                ho.put("at", pos);
                                ho.put("probe", probe);
                                ho.put("text", snippet);
                                taken++;
                                cnt++;
                                from = pos + pl.length();
                            }
                        }
                    }
                    po.put("hitCount", cnt);
                    ev.set(pid, po);
                }
            }
            ev.put("budgetLeft", budget);
            evidence = ev.toString();
        } catch (Exception e) {
            evidence = QaSupport.errorJson(e);
        }
        FlowEngine.instance().register("evidence", evidence);
    }

    /** 某个检索词在所有段里的总命中数，用来排稀有度 */
    private static int countIn(JsonNode segs, String probe) {
        int n = 0;
        String pl = probe.toLowerCase(Locale.ROOT);
        for (int s = 0; s < segs.size(); s++) {
            String tl = segs.get(s).path("text").asText("").toLowerCase(Locale.ROOT);
            int from = 0;
            while (true) {
                int p = tl.indexOf(pl, from);
                if (p < 0) {
                    break;
                }
                n++;
                from = p + pl.length();
            }
        }
        return n;
    }
}

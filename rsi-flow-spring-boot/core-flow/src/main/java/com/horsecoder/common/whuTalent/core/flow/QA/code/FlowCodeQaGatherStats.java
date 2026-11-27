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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 分区取证（带全库统计版）
 *
 * FLOW_QA_GATHER_STATS
 *
 * 和 FLOW_QA_GATHER 的区别在两处：
 * 1) 统计不抽样：每个检索词输出全库精确次数、各文档分布、与趋势动词的连用次数。
 *    配额能砍掉片段，但砍不掉计数，"某短语全库 0 次"这种事必须让裁决环节看见。
 * 2) 配额按轮分配：先保证每个检索词都拿到 1 条，再按稀有顺序补第 2、3 条。
 *    原实现是一个词吃满 9 条后剩下的词完全没检索，高频词反而挤掉低频词。
 *
 * 通用版不用这个：Q01 靠 LOCATE 已筛出论证性命中，片段量小，改配额反而扰动了
 * 已跑通的证据集；Q01 用 FLOW_QA_GATHER 原版。
 *
 * @param propositions 命题
 * @param segments 分段结果
 * @result evidence 命题对应原文片段
 * @result statsTable 检索词全库统计表
 */
@FlowComponent
public class FlowCodeQaGatherStats {

    /** 命中点前后各取多少字符 */
    private static final int WINDOW = 400;

    /** 每个检索词最多留几条：高频词很容易把配额吃光，必须单独限额 */
    private static final int MAX_PER_PROBE = 3;

    /** 每条命题最多留几条：一轮一条保底最多 5 条，再补两轮 */
    private static final int MAX_PER_PROP = 12;

    /** 命中点之后多长范围内找趋势动词，"X 增长"这种主谓关系就靠它区分 */
    private static final int VERB_REACH = 150;

    /** 表达趋势的动词：一个名词若从不和这些词连用，就不能拿它描述趋势 */
    private static final String[] TREND = {"increase", "increased", "increasing", "grow", "grew",
            "growing", "rise", "rose", "rising", "improve", "improved", "improving",
            "perfect", "perfected", "strengthen", "enhance", "expand"};

    @Flow("FLOW_QA_GATHER_STATS")
    public void flowCodeQaGatherStats(
            String propositions,
            String segments
    ) {
        String evidence = "{}";
        String table = "";
        try {
            JsonNode props = QaSupport.JSON.readTree(
                    propositions == null || propositions.trim().isEmpty() ? "[]" : propositions);
            JsonNode segs = QaSupport.JSON.readTree(segments == null ? "[]" : segments);

            int segCount = segs.size();
            ObjectNode ev = QaSupport.JSON.createObjectNode();
            int budget = QaSupport.MAX_EVIDENCE_CHARS;
            StringBuilder sb = new StringBuilder();
            sb.append("【检索词全库统计】Java 侧精确计数，非抽样。格式：全库次数 / 覆盖文档数 / 与趋势动词连用数 / 各文档分布\n");

            if (props.isArray()) {
                for (JsonNode p : props) {
                    Set<String> probes = new LinkedHashSet<>();
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
                    List<String> order = new ArrayList<>(probes);

                    // 全量扫描：每个检索词的所有命中位置，只扫一遍，统计和取证共用
                    Map<String, List<int[]>> all = new LinkedHashMap<>();
                    for (String probe : order) {
                        List<int[]> ps = new ArrayList<>();
                        String pl = probe.toLowerCase(Locale.ROOT);
                        if (!pl.isEmpty()) {
                            for (int s = 0; s < segCount; s++) {
                                String tl = segs.get(s).path("text").asText("").toLowerCase(Locale.ROOT);
                                int from = 0;
                                while (true) {
                                    int q = tl.indexOf(pl, from);
                                    if (q < 0) {
                                        break;
                                    }
                                    ps.add(new int[]{s, q});
                                    from = q + pl.length();
                                }
                            }
                        }
                        all.put(probe, ps);
                    }

                    // 稀有词优先：命中越少的词越有区分度，排在前面
                    Collections.sort(order, new Comparator<String>() {
                        @Override
                        public int compare(String a, String b) {
                            return all.get(a).size() - all.get(b).size();
                        }
                    });

                    ObjectNode po = QaSupport.JSON.createObjectNode();
                    po.put("id", pid);
                    ArrayNode stats = po.putArray("stats");
                    ArrayNode hits = po.putArray("hits");

                    sb.append("[").append(pid).append("]\n");
                    for (String probe : order) {
                        List<int[]> ps = all.get(probe);
                        int[] dist = new int[Math.max(segCount, 1)];
                        for (int[] at : ps) {
                            if (at[0] < dist.length) {
                                dist[at[0]]++;
                            }
                        }
                        int covered = 0;
                        for (int d : dist) {
                            if (d > 0) {
                                covered++;
                            }
                        }

                        // 主谓关系检测：命中点之后若从不出现趋势动词，说明这个词只当名词用，
                        // 不能拿来描述"增长/改善"，这是判措辞失真的硬依据
                        int verb = 0;
                        for (int[] at : ps) {
                            String t = segs.get(at[0]).path("text").asText("");
                            int from = at[1] + probe.length();
                            int to = Math.min(t.length(), from + VERB_REACH);
                            String w = t.substring(from, to).toLowerCase(Locale.ROOT);
                            for (String v : TREND) {
                                if (w.contains(v)) {
                                    verb++;
                                    break;
                                }
                            }
                        }

                        ObjectNode st = stats.addObject();
                        st.put("probe", probe);
                        st.put("total", ps.size());
                        st.put("docs", covered);
                        ArrayNode dArr = st.putArray("dist");
                        for (int d : dist) {
                            dArr.add(d);
                        }
                        st.put("verbHits", verb);
                        st.put("zero", ps.isEmpty());

                        StringBuilder line = new StringBuilder();
                        line.append(String.format("  %-32s 全库 %-4d 覆盖 %d/%d 连用趋势动词 %d 处  分布 ",
                                probe, ps.size(), covered, segCount, verb));
                        for (int s = 0; s < segCount; s++) {
                            String bk = segs.get(s).path("book").asText("S" + s).replace(" 年报", "");
                            if (s > 0) {
                                line.append("/");
                            }
                            line.append(bk).append(":").append(s < dist.length ? dist[s] : 0);
                        }
                        if (ps.isEmpty()) {
                            line.append("  ⚠ 该短语全库 0 次：原文从未这样表述，选项依赖它即不成立");
                        } else if (verb == 0) {
                            line.append("  ⚠ 该词从不与增长/改善动词连用（全部为名词性用法）");
                        }
                        sb.append(line.toString()).append("\n");
                    }

                    // 一轮一条保底：先让每个检索词都露脸，再按稀有顺序补第 2、3 条
                    int cnt = 0;
                    for (int round = 0; round < MAX_PER_PROBE && cnt < MAX_PER_PROP; round++) {
                        for (String probe : order) {
                            if (cnt >= MAX_PER_PROP) {
                                break;
                            }
                            List<int[]> ps = all.get(probe);
                            if (round >= ps.size()) {
                                continue;
                            }
                            int[] at = ps.get(round);
                            String t = segs.get(at[0]).path("text").asText("");
                            int lo = Math.max(0, at[1] - WINDOW);
                            int hi = Math.min(t.length(), at[1] + probe.length() + WINDOW);
                            String snippet = t.substring(lo, hi);
                            if (budget - snippet.length() < 0) {
                                break;
                            }
                            budget -= snippet.length();
                            ObjectNode ho = hits.addObject();
                            ho.put("seg", at[0]);
                            ho.put("book", segs.get(at[0]).path("book").asText(""));
                            ho.put("at", at[1]);
                            ho.put("probe", probe);
                            ho.put("text", snippet);
                            cnt++;
                        }
                    }
                    // 0 命中单独列一条，不占配额
                    for (String probe : order) {
                        if (all.get(probe).isEmpty()) {
                            ObjectNode ho = hits.addObject();
                            ho.put("at", -1);
                            ho.put("probe", probe);
                            ho.put("text", "[原文 0 命中：该短语在全库中不存在]");
                        }
                    }
                    po.put("hitCount", cnt);
                    ev.set(pid, po);
                }
            }
            ev.put("budgetLeft", budget);
            ev.put("statsTable", sb.toString());
            evidence = ev.toString();
            table = sb.toString();
        } catch (Exception e) {
            evidence = QaSupport.errorJson(e);
        }
        FlowEngine.instance().register("evidence", evidence);
        FlowEngine.instance().register("statsTable", table);
    }
}

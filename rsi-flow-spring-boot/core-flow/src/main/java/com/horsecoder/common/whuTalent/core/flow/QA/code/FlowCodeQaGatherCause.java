package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

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
 * 按选项收集"因果句"（排除题专用）
 *
 * FLOW_QA_GATHER_CAUSE
 *
 * 与 FLOW_QA_GATHER_STATS 的分工：那个为"某说法是否在增长/改善"设计，
 * 统计里带趋势动词连用数，趋势词对排除题是噪声；排除题问的是
 * "材料有没有把这个选项陈述为造成某事件的原因"，需要的是因果标记词共现。
 *
 * 做法：每个选项的检索短语扫全库，取命中点 ±400 字符窗口；窗口内出现
 * 因果标记词（origin / cause / factor / led to / contribute ...）的片段
 * 记为"因果句"并优先进入笔记，其余只留少量作对照。
 * 某探针全库 0 次时显式写出——排除题里"从没提过"本身就是判据。
 *
 * @param propositions 命题 + 探针组
 * @param segments 分段结果
 * @result memo 按选项分组的取证笔记
 * @result statsTable 每选项命中统计
 */
@FlowComponent
public class FlowCodeQaGatherCause {

    /** 命中点前后各取多少字符 */
    private static final int WINDOW = 400;

    /** 每个选项最多留几条因果句 */
    private static final int MAX_CAUSAL = 4;

    /** 每个选项最多留几条普通命中（做对照） */
    private static final int MAX_PLAIN = 2;

    /** 同一段内位置相近的两条命中只留一条 */
    private static final int MERGE_GAP = 800;

    /** 整份笔记的字符预算 */
    private static final int BUDGET = 45_000;

    /** 因果标记词：出现即说明该处把前面的名词当成原因/因素在讲 */
    private static final String[] CAUSE = {
            "origin", "origins", "originated", "originating",
            "cause", "caused", "causes", "causing",
            "because", "due to", "owing to", "as a result of",
            "led to", "lead to", "leading to", "resulted in", "result of",
            "factor", "factors", "root", "roots", "stemmed", "stem from",
            "reason", "reasons", "provoked", "triggered", "brought about",
            "contribut", "attribut", "gave rise to", "explain"
    };

    @Flow("FLOW_QA_GATHER_CAUSE")
    public void flowCodeQaGatherCause(
            String propositions,
            String segments
    ) {
        String memo = "";
        String table = "";
        try {
            JsonNode props = QaSupport.JSON.readTree(
                    propositions == null || propositions.trim().isEmpty() ? "[]" : propositions);
            JsonNode segs = QaSupport.JSON.readTree(segments == null ? "[]" : segments);
            int segCount = segs.size();

            StringBuilder sb = new StringBuilder();
            sb.append("【按选项的因果取证】片段按选项分组。标[因果]的片段里出现了因果标记词"
                    + "（origin/cause/factor/led to/contribute 等），标[普通]的只是同主题出现，"
                    + "不能据此认为材料把该选项当成原因。\n\n");
            StringBuilder tb = new StringBuilder();
            int budget = BUDGET;

            if (props.isArray()) {
                for (JsonNode p : props) {
                    String id = p.path("id").asText("?");
                    String claim = p.path("claim").asText("");
                    Set<String> probes = new LinkedHashSet<>();
                    JsonNode pr = p.path("probes");
                    if (pr.isArray()) {
                        for (JsonNode x : pr) {
                            if (x.isTextual() && !x.asText().trim().isEmpty()) {
                                probes.add(x.asText().trim().toLowerCase(Locale.ROOT));
                            }
                        }
                    }
                    if (probes.isEmpty()) {
                        probes.add(claim.toLowerCase(Locale.ROOT));
                    }
                    List<String> order = new ArrayList<>(probes);

                    // 每个探针扫一遍全库，命中位置一次拿全
                    Map<String, List<int[]>> byProbe = new LinkedHashMap<>();
                    for (String probe : order) {
                        List<int[]> ps = new ArrayList<>();
                        for (int s = 0; s < segCount; s++) {
                            String t = segs.get(s).path("text").asText("").toLowerCase(Locale.ROOT);
                            int from = 0;
                            while (true) {
                                int q = t.indexOf(probe, from);
                                if (q < 0) {
                                    break;
                                }
                                ps.add(new int[]{s, q});
                                from = q + probe.length();
                            }
                        }
                        byProbe.put(probe, ps);
                    }

                    // 汇总成片段，标因果，去重
                    List<String[]> entries = new ArrayList<>(); // {seg, pos, probe, text, causal}
                    int covered = 0;
                    int causalCount = 0;
                    int[] segDist = new int[Math.max(segCount, 1)];
                    for (String probe : order) {
                        for (int[] at : byProbe.get(probe)) {
                            String t = segs.get(at[0]).path("text").asText("");
                            int lo = Math.max(0, at[1] - WINDOW);
                            int hi = Math.min(t.length(), at[1] + probe.length() + WINDOW);
                            String snip = t.substring(lo, hi);
                            boolean causal = hasCause(snip);
                            if (causal) {
                                causalCount++;
                            }
                            if (at[0] < segDist.length) {
                                segDist[at[0]]++;
                            }
                            entries.add(new String[]{String.valueOf(at[0]), String.valueOf(at[1]),
                                    probe, snip, causal ? "1" : "0"});
                        }
                    }

                    // 去重：同段且位置相距不足 MERGE_GAP 的只留因果性更强的那条
                    Collections.sort(entries, new Comparator<String[]>() {
                        @Override
                        public int compare(String[] a, String[] b) {
                            int s = Integer.parseInt(a[0]) - Integer.parseInt(b[0]);
                            return s != 0 ? s : Integer.parseInt(a[1]) - Integer.parseInt(b[1]);
                        }
                    });
                    List<String[]> kept = new ArrayList<>();
                    for (String[] e : entries) {
                        String[] last = kept.isEmpty() ? null : kept.get(kept.size() - 1);
                        if (last != null && last[0].equals(e[0])
                                && Integer.parseInt(e[1]) - Integer.parseInt(last[1]) < MERGE_GAP) {
                            if (e[4].equals("1") && last[4].equals("0")) {
                                kept.set(kept.size() - 1, e);
                            }
                            continue;
                        }
                        kept.add(e);
                    }
                    for (int d : segDist) {
                        if (d > 0) {
                            covered++;
                        }
                    }

                    // 因果句优先，其次普通命中，各自限额
                    List<String[]> causal = new ArrayList<>();
                    List<String[]> plain = new ArrayList<>();
                    for (String[] e : kept) {
                        if (e[4].equals("1")) {
                            causal.add(e);
                        } else {
                            plain.add(e);
                        }
                    }

                    sb.append("【").append(id).append("】").append(claim).append("\n");
                    StringBuilder cnt = new StringBuilder();
                    for (String probe : order) {
                        int n = byProbe.get(probe).size();
                        if (cnt.length() > 0) {
                            cnt.append(" / ");
                        }
                        cnt.append(probe).append(":").append(n);
                    }
                    sb.append("  探针命中：").append(cnt.length() == 0 ? "（无）" : cnt.toString()).append("\n");

                    int used = 0;
                    String[] tags = {"[因果]", "[普通]"};
                    List<List<String[]>> groups = new ArrayList<>();
                    groups.add(causal);
                    groups.add(plain);
                    int[] limit = {MAX_CAUSAL, MAX_PLAIN};
                    for (int g = 0; g < 2; g++) {
                        int n = 0;
                        for (String[] e : groups.get(g)) {
                            if (n >= limit[g] || budget <= 0) {
                                break;
                            }
                            String line = "  · " + tags[g] + "[S" + e[0] + "] " + oneLine(e[3]);
                            if (budget - line.length() < 0) {
                                break;
                            }
                            budget -= line.length();
                            sb.append(line).append("\n");
                            n++;
                            used++;
                        }
                    }
                    if (used == 0) {
                        sb.append("  · 两个探针全部 0 命中：材料从未出现该选项的说法\n");
                    }
                    // 0 命中的探针单独点名
                    for (String probe : order) {
                        if (byProbe.get(probe).isEmpty()) {
                            sb.append("  · 探针 \"").append(probe).append("\" 全库 0 次\n");
                        }
                    }

                    if (tb.length() > 0) {
                        tb.append("\n");
                    }
                    tb.append(String.format("%s | 命中 %d 处，覆盖 %d/%d 段，含因果标记 %d 处",
                            id, entries.size(), covered, segCount, causalCount));
                    for (String probe : order) {
                        tb.append(" | ").append(probe).append(":").append(byProbe.get(probe).size());
                    }
                }
            }
            memo = sb.toString();
            table = tb.toString();
        } catch (Exception e) {
            memo = QaSupport.errorJson(e);
        }
        FlowEngine.instance().register("memo", memo);
        FlowEngine.instance().register("statsTable", table);
    }

    private boolean hasCause(String snip) {
        String w = snip.toLowerCase(Locale.ROOT);
        for (String c : CAUSE) {
            if (w.contains(c)) {
                return true;
            }
        }
        return false;
    }

    private String oneLine(String s) {
        return s.replace("\n", " ").replaceAll("\\s+", " ").trim();
    }
}

package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 分桶取证：按"命题 × 文档"两维配额检索，用于多文档对照题
 *
 * FLOW_QA_GATHER_BUCKETS
 *
 * 与 FLOW_QA_GATHER_STATS 的关系：检索、取窗、稀有优先这一整套方法照搬，
 * 只改配额维度。原版是"每命题 12 条、每探针 3 条"，在多文档题里会出问题——
 * Q05 的材料是 Lilium 与 Archer 两份招股书，若按原配额，第一份文档的命中会把配额吃满，
 * 第二份文档一条都取不到，而题目恰恰问"两家的共同特征"，缺半边就无法对照。
 *
 * 所以这里改成：每个"命题 × 文档"格子内，每探针取 2 条、总数上限 8 条。
 * 四个维度 × 两家 = 八个格子，每格都有片段，裁决时能并排看两家的原话。
 *
 * 输出不是 JSON 而是直接可读的对照文本，喂给 FLOW_QA_DECIDE_MEMO 当笔记用。
 * 命中 0 处会显式写出来——"某维度在某家文档里完全不存在"本身就是判"不是共同特征"的硬依据。
 *
 * @param propositions 命题 + 探针组
 * @param segments 按实体切开的段（带 book 标签）
 * @result memo 维度 × 文档的对照片段表
 * @result statsTable 每格命中数
 */
@FlowComponent
public class FlowCodeQaGatherBuckets {

    /** 命中点前后各取多少字符 */
    private static final int WINDOW = 400;

    /** 每探针在每个文档格子内最多留几条 */
    private static final int MAX_PER_PROBE = 2;

    /** 每命题在每个文档格子内的总上限：裁决环节对笔记只读前 3 万字符，四格加起来别超 */
    private static final int MAX_PER_BUCKET = 4;

    /** memo 总预算，必须小于裁决环节的 30,000 截断，否则后面的维度会被切掉 */
    private static final int MEMO_BUDGET = 28_000;

    @Flow("FLOW_QA_GATHER_BUCKETS")
    public void flowCodeQaGatherBuckets(
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

            int budget = MEMO_BUDGET;
            StringBuilder sb = new StringBuilder(8192);
            StringBuilder tb = new StringBuilder(4096);
            sb.append("【维度 × 文档 对照片段】每格独立配额，两家都有片段才能判断是否共同\n");
            tb.append("【每格命中数】\n");

            for (JsonNode p : props) {
                String pid = p.path("id").asText("?");
                String claim = p.path("claim").asText("");
                List<String> probes = new ArrayList<String>();
                JsonNode pr = p.path("probes");
                if (pr.isArray()) {
                    for (JsonNode x : pr) {
                        String s = x.asText("").trim().toLowerCase(Locale.ROOT);
                        if (!s.isEmpty() && !probes.contains(s)) {
                            probes.add(s);
                        }
                    }
                }
                if (probes.isEmpty()) {
                    continue;
                }

                sb.append("\n【").append(pid).append("】").append(claim).append('\n');
                sb.append("探针：");
                for (int i = 0; i < probes.size(); i++) {
                    if (i > 0) {
                        sb.append(" / ");
                    }
                    sb.append(probes.get(i));
                }
                sb.append('\n');

                for (int s = 0; s < segCount; s++) {
                    String text = segs.get(s).path("text").asText("");
                    String book = segs.get(s).path("book").asText("S" + s);
                    String lower = text.toLowerCase(Locale.ROOT);

                    // 每探针在本文档的所有命中位置
                    Map<String, List<Integer>> hits = new LinkedHashMap<String, List<Integer>>();
                    for (String probe : probes) {
                        List<Integer> ps = new ArrayList<Integer>();
                        int from = 0;
                        while (true) {
                            int q = lower.indexOf(probe, from);
                            if (q < 0) {
                                break;
                            }
                            ps.add(q);
                            from = q + probe.length();
                        }
                        hits.put(probe, ps);
                    }

                    int total = 0;
                    for (List<Integer> ps : hits.values()) {
                        total += ps.size();
                    }
                    tb.append(pid).append(" × ").append(book).append("：").append(total).append(" 处");

                    sb.append("- ").append(book).append("：命中 ").append(total).append(" 处\n");
                    if (total == 0) {
                        sb.append("    ⚠ 该维度在本文档中完全无对应表述\n");
                        tb.append("  ⚠ 完全无对应表述\n");
                        continue;
                    }
                    StringBuilder dist = new StringBuilder();
                    for (Map.Entry<String, List<Integer>> e : hits.entrySet()) {
                        if (e.getValue().isEmpty()) {
                            continue;
                        }
                        if (dist.length() > 0) {
                            dist.append(", ");
                        }
                        dist.append(e.getKey()).append(" ").append(e.getValue().size());
                    }
                    tb.append("（").append(dist).append("）\n");

                    // 每探针先保底 1 条，再补第 2 条，避免单个高频探针吃满配额
                    int cnt = 0;
                    for (int round = 0; round < MAX_PER_PROBE && cnt < MAX_PER_BUCKET; round++) {
                        for (Map.Entry<String, List<Integer>> e : hits.entrySet()) {
                            if (cnt >= MAX_PER_BUCKET) {
                                break;
                            }
                            List<Integer> ps = e.getValue();
                            if (round >= ps.size()) {
                                continue;
                            }
                            int at = ps.get(round);
                            int lo = Math.max(0, at - WINDOW);
                            int hi = Math.min(text.length(), at + e.getKey().length() + WINDOW);
                            String snip = text.substring(lo, hi).replace("\n", " ").trim();
                            if (budget - snip.length() < 0) {
                                break;
                            }
                            budget -= snip.length();
                            sb.append("    · [").append(e.getKey()).append("] …").append(snip).append("…\n");
                            cnt++;
                        }
                    }
                }
            }
            memo = sb.toString();
            table = tb.toString();
        } catch (Exception e) {
            memo = "[GATHER_BUCKETS_FAILED] " + e.toString();
            table = memo;
        }
        FlowEngine.instance().register("memo", memo);
        FlowEngine.instance().register("statsTable", table);
    }
}

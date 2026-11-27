package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 表格数值扫描：把"逐行相减找极值"这类算术从模型手里拿走，交给代码做
 *
 * FLOW_QA_TABLE_SCAN
 *
 * 为什么需要它：列投影把窗口压到 9 段之后，让模型在每段 3550 行里逐行相减找最大差值，
 * 实测会漏——Q04 真正的冠军 0.32（2022-08-15 09:30:00）被漏掉，模型报了第二名 0.205，
 * 因为 memo 每轮都被重写成一句"当前最优"，漏掉就再也纠不回来。
 * 这类比较大小、求和、计数的活本来就该由代码做：同样一张 31951 行的表，
 * 代码扫一遍是毫秒级且必然正确，模型扫一遍是几十万 token 还可能错。
 *
 * 通用性：不写死任何业务算子。对每个数值列求 max/min（带时间），
 * 对每对相邻数值列求差值的 max/min（带时间），再汇总一个差值 top-K 榜。
 * 所以"哪天最高/最低""哪一分钟涨最多/跌最多""价差最大"都能直接用，换题不用改代码。
 *
 * 输出是一张不超过 40 行的小事实表，后续裁决只需读这一张，token 从几十万降到千级。
 *
 * @param segments 投影后的分段（每段自带 "# 列：..." 表头前缀）
 * @result memo 事实表（供 FLOW_QA_DECIDE_MEMO 直接复用）
 * @result facts 与 memo 同内容，语义更明确
 * @result segInfo 行数与列数
 */
@FlowComponent
public class FlowCodeQaTableScan {

    /** 差值排行榜取前几名 */
    private static final int TOP_K = 5;

    /** 数值列判定：可解析成数字的行占比下限 */
    private static final double NUM_RATE = 0.8;

    /** 时间列判定：像日期或时刻的行占比下限 */
    private static final double TIME_RATE = 0.8;

    @Flow("FLOW_QA_TABLE_SCAN")
    public void flowCodeQaTableScan(
            String segments
    ) {
        String memo = "";
        String facts = "";
        String segInfo = "";
        try {
            List<String[]> rows = readRows(segments);
            String[] heads = lastHeads;
            int cols = heads == null ? 0 : heads.length;
            segInfo = rows.size() + " 行 / " + cols + " 列";
            facts = scan(heads, rows);
            memo = facts;
        } catch (Exception e) {
            memo = "[SCAN_FAILED] " + e.toString();
            facts = memo;
        }
        FlowEngine.instance().register("memo", memo);
        FlowEngine.instance().register("facts", facts);
        FlowEngine.instance().register("segInfo", segInfo);
    }

    /** 最近一次解析出的列名 */
    private String[] lastHeads = new String[0];

    /** 把各段里的数据行拼起来，段内重复的表头注释跳过 */
    private List<String[]> readRows(String segments) throws Exception {
        List<String[]> rows = new ArrayList<String[]>();
        JsonNode arr = QaSupport.JSON.readTree(segments);
        for (JsonNode seg : arr) {
            String text = seg.path("text").asText("");
            String headMark = null;
            for (String ln : text.split("\n", -1)) {
                String t = ln.trim();
                if (t.isEmpty()) {
                    continue;
                }
                if (t.startsWith("#")) {
                    if (t.startsWith("# 列：") && headMark == null) {
                        headMark = t.substring(4).trim();
                    }
                    continue;
                }
                if (headMark == null) {
                    continue;
                }
                if (lastHeads.length == 0) {
                    lastHeads = headMark.split("\\s*\\|\\s*");
                }
                String[] parts = t.split("\\|", -1);
                if (parts.length != lastHeads.length) {
                    continue;
                }
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim();
                }
                rows.add(parts);
            }
        }
        return rows;
    }

    /** 扫出极值事实表 */
    private String scan(String[] heads, List<String[]> rows) {
        StringBuilder sb = new StringBuilder(2048);
        if (heads.length == 0 || rows.isEmpty()) {
            sb.append("[SCAN] 没有可解析的表格行");
            return sb.toString();
        }

        int timeCol = pickTimeCol(rows);
        List<Integer> numCols = pickNumCols(rows);

        String first = timeCol >= 0 ? rows.get(0)[timeCol] : "(无时间列)";
        String last = timeCol >= 0 ? rows.get(rows.size() - 1)[timeCol] : "(无时间列)";
        sb.append("# 数据范围 ").append(first).append(" ~ ").append(last)
                .append("，共 ").append(rows.size()).append(" 行\n");
        sb.append("# 列：");
        for (int i = 0; i < heads.length; i++) {
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append(heads[i]);
        }
        sb.append('\n');

        if (numCols.isEmpty()) {
            sb.append("[SCAN] 没有可解析的数值列\n");
            return sb.toString();
        }

        sb.append("\n[单列极值]\n");
        for (int c : numCols) {
            double[] best = new double[]{Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
            String[] at = new String[]{"", ""};
            for (String[] r : rows) {
                double v = num(r[c]);
                if (Double.isNaN(v)) {
                    continue;
                }
                if (v > best[0]) {
                    best[0] = v;
                    at[0] = timeCol >= 0 ? r[timeCol] : "";
                }
                if (v < best[1]) {
                    best[1] = v;
                    at[1] = timeCol >= 0 ? r[timeCol] : "";
                }
            }
            sb.append(heads[c]).append(" 最大 ").append(fmt(best[0]))
                    .append(" @ ").append(at[0]).append('\n');
            sb.append(heads[c]).append(" 最小 ").append(fmt(best[1]))
                    .append(" @ ").append(at[1]).append('\n');
        }

        sb.append("\n[列差值极值]\n");
        List<double[]> rank = new ArrayList<double[]>();
        List<String> rankAt = new ArrayList<String>();
        for (int k = 0; k < numCols.size(); k++) {
            for (int m = k + 1; m < numCols.size(); m++) {
                int ca = numCols.get(k);
                int cb = numCols.get(m);
                double hi = Double.NEGATIVE_INFINITY, lo = Double.POSITIVE_INFINITY;
                String hiAt = "", loAt = "";
                for (String[] r : rows) {
                    double a = num(r[ca]);
                    double b = num(r[cb]);
                    if (Double.isNaN(a) || Double.isNaN(b)) {
                        continue;
                    }
                    double d = a - b;
                    if (d > hi) {
                        hi = d;
                        hiAt = timeCol >= 0 ? r[timeCol] : "";
                    }
                    if (d < lo) {
                        lo = d;
                        loAt = timeCol >= 0 ? r[timeCol] : "";
                    }
                }
                if (hi == Double.NEGATIVE_INFINITY) {
                    continue;
                }
                String name = heads[ca] + " - " + heads[cb];
                sb.append(name).append(" 最大 ").append(fmt(hi))
                        .append(" @ ").append(hiAt).append('\n');
                sb.append(name).append(" 最小 ").append(fmt(lo))
                        .append(" @ ").append(loAt).append('\n');
                if (hi > 0) {
                    rank.add(new double[]{hi});
                    rankAt.add(name + " 最大差 @ " + hiAt);
                }
            }
        }

        if (!rank.isEmpty()) {
            sb.append("\n[差值 top").append(Math.min(TOP_K, rank.size())).append("]\n");
            Integer[] idx = new Integer[rank.size()];
            for (int i = 0; i < idx.length; i++) {
                idx[i] = i;
            }
            java.util.Arrays.sort(idx, new java.util.Comparator<Integer>() {
                public int compare(Integer x, Integer y) {
                    return Double.compare(rank.get(y)[0], rank.get(x)[0]);
                }
            });
            int n = Math.min(TOP_K, idx.length);
            for (int i = 0; i < n; i++) {
                sb.append(i + 1).append(". ").append(fmt(rank.get(idx[i])[0]))
                        .append("  ").append(rankAt.get(idx[i])).append('\n');
            }
        }
        return sb.toString();
    }

    /** 找像日期/时刻的那一列，找不到返回 -1 */
    private int pickTimeCol(List<String[]> rows) {
        int best = -1;
        int bestN = 0;
        int sample = Math.min(rows.size(), 2000);
        for (int c = 0; c < rows.get(0).length; c++) {
            int hit = 0;
            for (int i = 0; i < sample; i++) {
                String v = rows.get(i)[c];
                if (v.indexOf('-') > 0 || v.indexOf(':') > 0) {
                    hit++;
                }
            }
            if (hit * 1.0 / sample >= TIME_RATE && hit > bestN) {
                bestN = hit;
                best = c;
            }
        }
        return best;
    }

    /** 找数值列（可解析成数字的行占比达标的列，且排除时间列） */
    private List<Integer> pickNumCols(List<String[]> rows) {
        List<Integer> out = new ArrayList<Integer>();
        int sample = Math.min(rows.size(), 2000);
        for (int c = 0; c < rows.get(0).length; c++) {
            int hit = 0;
            for (int i = 0; i < sample; i++) {
                if (!Double.isNaN(num(rows.get(i)[c]))) {
                    hit++;
                }
            }
            if (hit * 1.0 / sample >= NUM_RATE) {
                out.add(c);
            }
        }
        return out;
    }

    /** 宽松数值解析，解析不了返回 NaN */
    private double num(String s) {
        if (s == null) {
            return Double.NaN;
        }
        String t = s.trim().replace(",", "").replace("%", "");
        if (t.isEmpty()) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(t);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    /** 去掉多余尾零，便于模型对齐选项 */
    private String fmt(double v) {
        String s = String.format(java.util.Locale.US, "%.4f", v);
        while (s.indexOf('.') >= 0 && s.endsWith("0")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.endsWith(".")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}

package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全库"楼层提及"扫描（属性矛盾题专用）
 *
 * FLOW_QA_ENTITY_SCAN
 *
 * 与 FLOW_QA_GATHER_CAUSE 的分工：那个按选项探针收集因果句，适合排除题；
 * 这题问"某两个人住在几楼"，材料对这个属性有两处说法。
 *
 * 为什么不按题干检索：按 "Which floor" 的字面去搜，命中的永远是最像问句
 * 的那一处（"What floor are they on?" "Second."），后一处 "third-floor unit"
 * 藏在环境描写里，字面相关性低，检索式 QA 读完第一处就收口了。
 *
 * 做法：不按题干检索，而是把全库所有 floor / upstairs / downstairs 的提及
 * 都列出来，每处取 ±600 字符窗口，标出窗口内出现的题干人名与楼层取值。
 * "这处在讲谁"交给下一步的模型判，代词指代也由它处理。
 *
 * @param question 题干
 * @param segments 分段结果
 * @result memo 楼层提及清单
 * @result statsTable 扫描统计
 */
@FlowComponent
public class FlowCodeQaEntityScan {

    /** 命中点前后各取多少字符 */
    private static final int WINDOW = 450;

    /** 判指代时向外找题干人名的半径。窗口里常常只有 they/he/she，名字在这一圈之外 */
    private static final int NEARBY = 2_500;

    /** 同段内相距不足此值的两处只留第一处 */
    private static final int MERGE_GAP = 300;

    /** 整份清单的字符预算 */
    private static final int BUDGET = 40_000;

    /** 楼层类词 */
    private static final Pattern FLOOR_HIT = Pattern.compile(
            "\\bfloor\\b|\\bupstairs\\b|\\bdownstairs\\b", Pattern.CASE_INSENSITIVE);

    /** 窗口里的楼层取值 */
    private static final Pattern FLOOR_VALUE = Pattern.compile(
            "\\b(first|second|third|fourth|fifth|sixth|top|ground)\\s*[- ]?\\s*(?:floor|storey|story)\\b"
                    + "|\\b\\d{1,2}(?:st|nd|rd|th)\\s*[- ]?floor\\b", Pattern.CASE_INSENSITIVE);

    /** 引号里单独回一个楼层词，如 "Second." */
    private static final Pattern FLOOR_REPLY = Pattern.compile(
            "[\"\u201c]\\s*(first|second|third|fourth|fifth)\\b", Pattern.CASE_INSENSITIVE);

    /** 住处/建筑类词：窗口里出现才认为这处在讲"谁住哪层" */
    private static final String[] DWELL = {
            "apartment", "unit", "residence", "building", "home", "live", "lived", "lives",
            "living", "cohab", "address", "neighbor", "tenant", "stair", "elevator"
    };

    /** 非住处的 floor：舞池、摔倒、地面材质 */
    private static final String[] BAD = {
            "dance floor", "on the floor", "to the floor", "tile floor",
            "shop floor", "laminate", "concrete floor", "floor of the"
    };

    @Flow("FLOW_QA_ENTITY_SCAN")
    public void flowCodeQaEntityScan(
            String question,
            String segments
    ) {
        String memo = "";
        String table = "";
        try {
            JsonNode segs = QaSupport.JSON.readTree(segments == null ? "[]" : segments);
            int segCount = segs.size();
            List<String> names = QaSupport.extractNames(question);

            List<String[]> hits = new ArrayList<>(); // {seg, pos, probe}
            int[] segDist = new int[Math.max(segCount, 1)];
            for (int s = 0; s < segCount; s++) {
                String t = segs.get(s).path("text").asText("");
                Matcher m = FLOOR_HIT.matcher(t);
                while (m.find()) {
                    int p = m.start();
                    if (!isDwelling(t, p)) {
                        continue;
                    }
                    hits.add(new String[]{String.valueOf(s), String.valueOf(p), m.group()});
                    if (s < segDist.length) {
                        segDist[s]++;
                    }
                }
            }

            Collections.sort(hits, new Comparator<String[]>() {
                @Override
                public int compare(String[] a, String[] b) {
                    int s = Integer.parseInt(a[0]) - Integer.parseInt(b[0]);
                    return s != 0 ? s : Integer.parseInt(a[1]) - Integer.parseInt(b[1]);
                }
            });
            List<String[]> kept = new ArrayList<>();
            for (String[] e : hits) {
                String[] last = kept.isEmpty() ? null : kept.get(kept.size() - 1);
                if (last != null && last[0].equals(e[0])
                        && Integer.parseInt(e[1]) - Integer.parseInt(last[1]) < MERGE_GAP) {
                    continue;
                }
                kept.add(e);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("【全库楼层提及】共 ").append(kept.size())
                    .append(" 处（已剔除舞池、摔倒在地、地面材质这类不是住处的 floor）。")
                    .append("每处给命中点前后 ").append(WINDOW).append(" 字符原文。")
                    .append("\"窗口内\"是原文窗口里出现的题干人名；\"附近\"是窗口外 ").append(NEARBY)
                    .append(" 字符内首次出现的题干人名，数字为相对命中点的偏移（+ 在后、- 在前）。")
                    .append("两栏都是 — 才说明这一带完全没提这两个人。\n\n");

            int budget = BUDGET;
            int named = 0;
            for (int i = 0; i < kept.size(); i++) {
                String[] e = kept.get(i);
                String t = segs.get(Integer.parseInt(e[0])).path("text").asText("");
                int p = Integer.parseInt(e[1]);
                int lo = Math.max(0, p - WINDOW);
                int hi = Math.min(t.length(), p + WINDOW);
                String win = t.substring(lo, hi);

                Set<String> vals = new LinkedHashSet<>();
                Matcher v = FLOOR_VALUE.matcher(win);
                while (v.find()) {
                    vals.add(v.group().toLowerCase(Locale.ROOT));
                }
                Matcher r = FLOOR_REPLY.matcher(win);
                while (r.find()) {
                    vals.add(r.group(1).toLowerCase(Locale.ROOT) + "(回复)");
                }

                StringBuilder who = new StringBuilder();
                for (String n : names) {
                    if (win.toLowerCase(Locale.ROOT).contains(n.toLowerCase(Locale.ROOT))) {
                        if (who.length() > 0) {
                            who.append(" ");
                        }
                        who.append(n);
                    }
                }
                if (who.length() > 0) {
                    named++;
                }

                // 窗口里只有代词时，靠附近的人名判这处在讲谁：给出人名与相对偏移
                StringBuilder near = new StringBuilder();
                int nlo = Math.max(0, p - NEARBY);
                int nhi = Math.min(t.length(), p + NEARBY);
                String ntxt = t.substring(nlo, nhi).toLowerCase(Locale.ROOT);
                for (String n : names) {
                    int idx = ntxt.indexOf(n.toLowerCase(Locale.ROOT));
                    if (idx >= 0) {
                        int off = idx + nlo - p;
                        if (near.length() > 0) {
                            near.append(" ");
                        }
                        near.append(n).append(off >= 0 ? "+" : "-").append(Math.abs(off));
                    }
                }

                String line = "[" + (i + 1) + "] S" + e[0] + "@" + p
                        + " | 取值: " + (vals.isEmpty() ? "—" : join(vals))
                        + " | 窗口内: " + (who.length() == 0 ? "—" : who.toString())
                        + " | 附近(±" + NEARBY + "): "
                        + (near.length() == 0 ? "—" : near.toString()) + "\n";
                String body = "    " + oneLine(win) + "\n\n";
                if (budget - line.length() - body.length() < 0) {
                    sb.append("...余下 ").append(kept.size() - i).append(" 处超出预算未列出\n");
                    break;
                }
                budget -= line.length() + body.length();
                sb.append(line).append(body);
            }

            int covered = 0;
            for (int d : segDist) {
                if (d > 0) {
                    covered++;
                }
            }
            StringBuilder tb = new StringBuilder();
            tb.append("原始命中 ").append(hits.size()).append(" 处，去重后 ").append(kept.size())
                    .append(" 处，覆盖 ").append(covered).append("/").append(segCount).append(" 段");
            tb.append(" | 窗口内含题干人名的 ").append(named).append(" 处");
            for (int s = 0; s < segDist.length; s++) {
                if (segDist[s] > 0) {
                    tb.append(" | S").append(s).append(":").append(segDist[s]);
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

    private boolean isDwelling(String text, int pos) {
        int lo = Math.max(0, pos - 60);
        int hi = Math.min(text.length(), pos + 60);
        String near = text.substring(lo, hi).toLowerCase(Locale.ROOT);
        for (String b : BAD) {
            if (near.contains(b)) {
                return false;
            }
        }
        int wlo = Math.max(0, pos - WINDOW);
        int whi = Math.min(text.length(), pos + WINDOW);
        String win = text.substring(wlo, whi).toLowerCase(Locale.ROOT);
        for (String d : DWELL) {
            if (win.contains(d)) {
                return true;
            }
        }
        if (win.contains("what floor")) {
            return true;
        }
        return FLOOR_VALUE.matcher(text.substring(wlo, whi)).find();
    }

    private String join(Set<String> vals) {
        StringBuilder sb = new StringBuilder();
        for (String x : vals) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(x);
        }
        return sb.toString();
    }

    private String oneLine(String s) {
        return s.replace("\n", " ").replaceAll("\\s+", " ").trim();
    }
}

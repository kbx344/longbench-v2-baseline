package com.horsecoder.common.whuTalent.core.flow.QA.code;

import cc.eamon.open.flow.core.FlowEngine;
import cc.eamon.open.flow.core.stereotype.Flow;
import cc.eamon.open.flow.spring.FlowComponent;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 占比三元组扫描（跨年数值题专用）
 *
 * FLOW_QA_NUM_SCAN
 *
 * 与 FLOW_QA_TABLE_SCAN 的分工：那个在表格题里扫单个数值并定位行号；
 * 这题要的是"同一种表述在两个年度各出现一次"，再把两次的三元组各自
 * 换算成剩余占比。两份年报的句式完全一样，只有年份和数字不同，
 * 所以按句式扫比按关键词扫更可靠。
 *
 * 做法：把每段压成单行后用"factories in Vietnam, China and Cambodia
 * manufactured approximately X%, Y% and Z%"匹配，向前 800 字符找最近的
 * fiscal 20XX 作年份，算 other = 100 - (X+Y+Z)。另扫工厂家数、
 * 所在国数作旁证。
 *
 * @param segments 分段结果
 * @result stats 年度 -> 剩余占比 的 JSON
 * @result memo 可读的扫描记录
 */
@FlowComponent
public class FlowCodeQaNumScan {

    /** 三元组句式。两份年报只差年份与数字，句式一致 */
    private static final Pattern TRIPLE = Pattern.compile(
            "factories in Vietnam, China and Cambodia manufactured approximately "
                    + "(\\d+)%, (\\d+)% and (\\d+)%", Pattern.CASE_INSENSITIVE);

    /** 旁证：工厂家数与所在国数 */
    private static final Pattern COUNT = Pattern.compile(
            "operated (\\d+) finished goods apparel factories located in (\\d+) countries",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern YEAR = Pattern.compile(
            "(?:fiscal|As of May 31,|FY)\\s*(20\\d\\d)", Pattern.CASE_INSENSITIVE);

    /** 向前找年份的半径 */
    private static final int YEAR_BACK = 800;

    @Flow("FLOW_QA_NUM_SCAN")
    public void flowCodeQaNumScan(String segments) {
        String stats = "{}";
        String memo = "";
        try {
            JsonNode segs = QaSupport.JSON.readTree(segments == null ? "[]" : segments);
            int segCount = segs.size();

            com.fasterxml.jackson.databind.node.ObjectNode byYear = QaSupport.JSON.createObjectNode();
            StringBuilder sb = new StringBuilder();
            sb.append("【占比三元组扫描】句式：factories in Vietnam, China and Cambodia "
                    + "manufactured approximately X%, Y% and Z%\n\n");

            int found = 0;
            for (int s = 0; s < segCount; s++) {
                String t = segs.get(s).path("text").asText("");
                String flat = t.replaceAll("[\\r\\n]+", " ").replaceAll("\\s+", " ");
                Matcher m = TRIPLE.matcher(flat);
                while (m.find()) {
                    int vn = Integer.parseInt(m.group(1));
                    int cn = Integer.parseInt(m.group(2));
                    int kh = Integer.parseInt(m.group(3));
                    int other = 100 - (vn + cn + kh);

                    String year = yearBefore(flat, m.start());
                    found++;
                    byYear.put(year, other);

                    sb.append("[").append(found).append("] S").append(s).append("@").append(m.start())
                            .append(" | 年份: ").append(year)
                            .append(" | 越南 ").append(vn).append("% 中国 ").append(cn)
                            .append("% 柬埔寨 ").append(kh).append("%")
                            .append(" | 三项合计 ").append(vn + cn + kh)
                            .append("% | 其余国家 ").append(other).append("%\n");

                    String ctx = flat.substring(Math.max(0, m.start() - 320),
                            Math.min(flat.length(), m.end() + 120));
                    sb.append("    ").append(ctx.trim()).append("\n\n");
                }
            }

            // 旁证：工厂家数与国数
            sb.append("【旁证：成品服装工厂家数与所在国数】\n");
            int cnt = 0;
            for (int s = 0; s < segCount; s++) {
                String t = segs.get(s).path("text").asText("");
                String flat = t.replaceAll("[\\r\\n]+", " ").replaceAll("\\s+", " ");
                Matcher m = COUNT.matcher(flat);
                while (m.find()) {
                    cnt++;
                    sb.append("  · S").append(s).append(" | 年份: ").append(yearBefore(flat, m.start()))
                            .append(" | 工厂 ").append(m.group(1))
                            .append(" 家，分布于 ").append(m.group(2)).append(" 国\n");
                }
            }
            if (cnt == 0) {
                sb.append("  · 全库未出现该句式\n");
            }

            if (found == 0) {
                sb.append("\n【警告】三元组句式 0 命中：这份材料可能不是 NIKE 年报，或句式已改\n");
            }
            stats = byYear.toString();
            memo = sb.toString();
        } catch (Exception e) {
            memo = QaSupport.errorJson(e);
        }
        FlowEngine.instance().register("stats", stats);
        FlowEngine.instance().register("memo", memo);
    }

    private String yearBefore(String flat, int pos) {
        String head = flat.substring(Math.max(0, pos - YEAR_BACK), pos);
        Matcher m = YEAR.matcher(head);
        String y = "";
        while (m.find()) {
            y = m.group(1);
        }
        return y.isEmpty() ? "未标年" : y;
    }
}

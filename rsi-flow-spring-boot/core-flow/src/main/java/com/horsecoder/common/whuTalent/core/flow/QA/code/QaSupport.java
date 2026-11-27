package com.horsecoder.common.whuTalent.core.flow.QA.code;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Q01 五个原子能力共用的支撑代码。
 *
 * 不放 @Flow：这里只是工具，不对应任何节点。
 */
final class QaSupport {

    static final ObjectMapper JSON = new ObjectMapper();

    /** 数据集路径，可用 -Dqa.data.path=... 覆盖 */
    static final String DATA_PATH =
            System.getProperty("qa.data.path", "D:\\rsi-flow-paper\\notes\\repair-set.json");

    /** 单段字符上限，约 11.5 万词（按 5.24 字符/词估算），保证每段都能整段进窗口 */
    static final int SEG_MAX_CHARS = 600_000;

    /** 切段时允许向前回退找句子边界的最大距离，避免把检索词切断 */
    static final int BACKOFF_LIMIT = 20_000;

    /** 判定"正文区"时，段首和段尾各留多少比例不算正文 */
    static final double EDGE_RATIO = 0.02;

    /** 判断某一处命中是否落在引文层时，向前后各看多少字符 */
    static final int SCHOLARLY_RADIUS = 2000;

    /** 实体共现的上下文半径 */
    static final int COOCCUR_RADIUS = 3000;

    static final int MAX_HIT_PER_PROP = 10;
    static final int MAX_EVIDENCE_CHARS = 80_000;

    static final String LLM_BASE = envOr("QA_LLM_BASE", "https://intlapi.thingo.com.cn/v1");
    static final String LLM_MODEL = envOr("QA_LLM_MODEL", "gpt-5.6-sol");

    private static String envOr(String name, String dft) {
        String v = System.getenv(name);
        return (v == null || v.trim().isEmpty()) ? dft : v.trim();
    }
    static final String KEY_FILE = "D:\\rsi-flow-paper\\notes\\.llm-key";

    static final Pattern NAME = Pattern.compile("\\b[A-Z][a-zA-Z]{2,}\\b");

    /** 注释区行首编号，如换行后的 "26. " */
    static final Pattern NOTE_MARK = Pattern.compile("\\n\\s*\\d{1,3}[.\u00b7]\\s");

    /** 索引条目，如 "219 n. 102" */
    static final Pattern INDEX_ENTRY = Pattern.compile("\\d{1,3}\\s*n\\.\\s*\\d");

    static final Set<String> STOP = new HashSet<>(Arrays.asList(
            "on", "which", "what", "who", "whom", "whose", "how", "why", "when", "where",
            "the", "it", "its", "is", "are", "was", "were", "be", "been", "being",
            "do", "does", "did", "in", "of", "and", "or", "for", "to", "with", "by", "from",
            "as", "at", "that", "this", "these", "those", "based", "according", "most",
            "some", "all", "many", "both", "each", "other", "such", "same", "true", "false",
            "if", "then", "than", "can", "could", "should", "would", "may", "might", "must",
            "not", "no", "yes", "there", "their", "they", "them", "his", "her", "he", "she",
            "one", "two", "first", "second", "main", "mainly", "primary"
    ));

    private QaSupport() {
    }

    // ------------------------------------------------------------------ 取数

    static int parseNo(String qid) {
        String d = qid == null ? "" : qid.replaceAll("[^0-9]", "");
        if (d.isEmpty()) {
            throw new IllegalArgumentException("题号里没有数字：" + qid);
        }
        return Integer.parseInt(d);
    }

    /**
     * 逐条流式读数据集，取到目标那一条就返回。
     * 不整体 parse：数据集 24MB，其中 context 动辄 2MB，整棵树建起来内存翻十倍。
     */
    static JsonNode loadItem(int no) throws Exception {
        File f = new File(DATA_PATH);
        if (!f.exists()) {
            throw new IllegalStateException("数据集不存在：" + DATA_PATH);
        }
        JsonParser p = JSON.getFactory().createParser(f);
        try {
            if (p.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("数据集顶层不是数组");
            }
            while (p.nextToken() == JsonToken.START_OBJECT) {
                JsonNode node = JSON.readTree(p);
                if (node.path("no").asInt() == no) {
                    return node;
                }
            }
        } finally {
            p.close();
        }
        return null;
    }

    // ------------------------------------------------------------------ 切段

    static String buildSegments(String ctx) {
        com.fasterxml.jackson.databind.node.ArrayNode arr = JSON.createArrayNode();
        if (ctx == null || ctx.isEmpty()) {
            return arr.toString();
        }
        int len = ctx.length();
        int cut = ctx.indexOf("The Elementary Forms");
        boolean twoBooks = cut > 0 && cut < len - 10_000;

        List<int[]> ranges = new ArrayList<>();
        List<String> books = new ArrayList<>();

        if (twoBooks) {
            ranges.add(new int[]{0, cut});
            books.add("BL");
            int s = cut;
            while (s < len) {
                int e = Math.min(s + SEG_MAX_CHARS, len);
                e = backoff(ctx, s, e);
                ranges.add(new int[]{s, e});
                books.add("DK");
                s = e;
            }
        } else {
            int s = 0;
            while (s < len) {
                int e = Math.min(s + SEG_MAX_CHARS, len);
                e = backoff(ctx, s, e);
                ranges.add(new int[]{s, e});
                books.add("ALL");
                s = e;
            }
        }

        for (int i = 0; i < ranges.size(); i++) {
            int st = ranges.get(i)[0];
            int en = ranges.get(i)[1];
            ObjectNode o = JSON.createObjectNode();
            o.put("i", i);
            o.put("book", books.get(i));
            o.put("start", st);
            o.put("end", en);
            o.put("text", ctx.substring(st, en));
            arr.add(o);
        }
        return arr.toString();
    }

    static int backoff(String ctx, int start, int end) {
        if (end >= ctx.length()) {
            return ctx.length();
        }
        int floor = Math.max(start + SEG_MAX_CHARS / 2, end - BACKOFF_LIMIT);
        for (int i = end; i > floor; i--) {
            if (ctx.charAt(i - 1) == '\n' && ctx.charAt(i - 2) == '\n') {
                return i;
            }
            if (ctx.charAt(i - 1) == '.' && ctx.charAt(i) == ' ') {
                return i;
            }
        }
        return end;
    }

    // ------------------------------------------------------------ 实体与共现

    /**
     * 这一处命中是不是落在引文层（序言致谢、注释、索引）。
     *
     * 只看"离段首段尾多远"不够：注释区往往是一整块，从段落 88% 一直铺到 96%，
     * 按比例切会把它算进正文。改成看周围文本的特征。
     */
    static boolean isScholarly(String text, int pos) {
        int len = text.length();
        if (pos < len * EDGE_RATIO || pos > len * (1 - EDGE_RATIO)) {
            return true;
        }
        int lo = Math.max(0, pos - SCHOLARLY_RADIUS);
        int hi = Math.min(len, pos + SCHOLARLY_RADIUS);
        String w = text.substring(lo, hi).toLowerCase(Locale.ROOT);

        if (w.contains("acknowledg") || w.contains("preface")) {
            return true;
        }
        int cites = countOccurrences(w, "cf.") + countOccurrences(w, "op. cit")
                + countOccurrences(w, "loc. cit") + countOccurrences(w, "ibid");
        if (cites >= 2) {
            return true;
        }
        if (countMatches(NOTE_MARK, w) >= 3) {
            return true;
        }
        if (INDEX_ENTRY.matcher(w).find()) {
            return true;
        }
        return w.contains("name index") || w.contains("subject index");
    }

    static boolean isBody(int segLen, int pos) {
        return pos > segLen * EDGE_RATIO && pos < segLen * (1 - EDGE_RATIO);
    }

    private static int countOccurrences(String s, String sub) {
        int n = 0;
        int from = 0;
        while (true) {
            int i = s.indexOf(sub, from);
            if (i < 0) {
                return n;
            }
            n++;
            from = i + sub.length();
        }
    }

    private static int countMatches(Pattern p, String s) {
        Matcher m = p.matcher(s);
        int n = 0;
        while (m.find()) {
            n++;
        }
        return n;
    }

    static List<String> extractNames(String q) {
        List<String> out = new ArrayList<>();
        if (q == null || q.isEmpty()) {
            return out;
        }
        Matcher m = NAME.matcher(q);
        while (m.find()) {
            String w = m.group();
            if (STOP.contains(w.toLowerCase(Locale.ROOT))) {
                continue;
            }
            boolean dup = false;
            for (String x : out) {
                if (x.equalsIgnoreCase(w)) {
                    dup = true;
                    break;
                }
            }
            if (!dup) {
                out.add(w);
            }
        }
        return out;
    }

    /** probes 缺失时的兜底：从命题句子里挑实词当检索词 */
    static List<String> keywords(String claim) {
        List<String> out = new ArrayList<>();
        if (claim == null) {
            return out;
        }
        for (String w : claim.split("[^A-Za-z]+")) {
            String lw = w.toLowerCase(Locale.ROOT);
            if (w.length() > 4 && !STOP.contains(lw) && !out.contains(w)) {
                out.add(w);
                if (out.size() >= 6) {
                    break;
                }
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ 模型

    static String callLlm(String system, String user) throws Exception {
        return callLlm(system, user, 4096);
    }

    static String callLlm(String system, String user, int maxTokens) throws Exception {
        String key = llmKey();
        if (key.isEmpty()) {
            throw new IllegalStateException("缺少 LLM key：设置环境变量 THINGO_API_KEY，或写入 " + KEY_FILE);
        }

        ObjectNode body = JSON.createObjectNode();
        body.put("model", LLM_MODEL);
        body.put("temperature", 0.1);
        body.put("max_tokens", maxTokens);
        com.fasterxml.jackson.databind.node.ArrayNode msgs = body.putArray("messages");
        ObjectNode m1 = msgs.addObject();
        m1.put("role", "system");
        m1.put("content", system);
        ObjectNode m2 = msgs.addObject();
        m2.put("role", "user");
        m2.put("content", user);

        HttpURLConnection conn = (HttpURLConnection) new URL(LLM_BASE + "/chat/completions").openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(240_000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + key);

        OutputStream os = conn.getOutputStream();
        try {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
        } finally {
            os.close();
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
        } finally {
            is.close();
        }
        String resp = new String(bos.toByteArray(), StandardCharsets.UTF_8);

        if (code < 200 || code >= 300) {
            throw new IllegalStateException("模型接口 HTTP " + code + "：" + head(resp, 300));
        }
        JsonNode root = JSON.readTree(resp);
        JsonNode choice = root.path("choices").path(0);
        String content = choice.path("message").path("content").asText("");
        if (content.isEmpty()) {
            throw new IllegalStateException("模型返回空内容（finish_reason="
                    + choice.path("finish_reason").asText("?") + "），检查 max_tokens 是否被推理占满");
        }
        return content;
    }

    static String llmKey() {
        String k = System.getenv("THINGO_API_KEY");
        if (k != null && !k.trim().isEmpty()) {
            return k.trim();
        }
        try {
            File f = new File(KEY_FILE);
            if (f.exists()) {
                return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8).trim();
            }
        } catch (Exception ignore) {
            // 读不到就按缺失处理
        }
        return "";
    }

    static String stripFence(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        if (t.startsWith("```")) {
            int nl = t.indexOf('\n');
            if (nl > 0) {
                t = t.substring(nl + 1);
            }
            int end = t.lastIndexOf("```");
            if (end > 0) {
                t = t.substring(0, end);
            }
        }
        return t.trim();
    }

    static String parseAnswer(String out) {
        if (out == null) {
            return "?";
        }
        Matcher m = Pattern.compile("\"answer\"\\s*:\\s*\"([A-Da-d])\"").matcher(out);
        if (m.find()) {
            return m.group(1).toUpperCase(Locale.ROOT);
        }
        Matcher m2 = Pattern.compile("\"answer\"\\s*:\\s*([A-Da-d])").matcher(out);
        if (m2.find()) {
            return m2.group(1).toUpperCase(Locale.ROOT);
        }
        return "?";
    }

    // ------------------------------------------------------------------ 杂项

    static String jstr(String s) {
        return JSON.getNodeFactory().textNode(s == null ? "" : s).toString();
    }

    static String errorJson(Exception e) {
        ObjectNode o = JSON.createObjectNode();
        o.put("error", e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage()));
        return o.toString();
    }

    static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "\n...[已截断]";
    }

    static String head(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    static int sum(int[] a) {
        int t = 0;
        for (int v : a) {
            t += v;
        }
        return t;
    }
}

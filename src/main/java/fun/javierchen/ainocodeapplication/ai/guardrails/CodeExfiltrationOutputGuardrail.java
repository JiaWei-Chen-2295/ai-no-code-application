package fun.javierchen.ainocodeapplication.ai.guardrails;

import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码数据窃取检测 Guardrail。
 * 检测 LLM 生成代码中的恶意模式，包括：
 *   - eval() 调用
 *   - document.cookie 窃取
 *   - localStorage/sessionStorage 本地存储外泄
 *   - fetch/XHR 请求非白名单域名
 *   - 加密挖矿脚本
 *   - Base64 混淆载荷
 *   - 可疑 script src 引用
 * v1 策略：命中直接 fatal，不做自动剥离。
 */
public class CodeExfiltrationOutputGuardrail implements OutputGuardrail {

    /** CDN 白名单域名，script src 或 fetch URL 匹配白名单则放行 */
    private static final List<String> CDN_WHITELIST = List.of(
            "picsum.photos",
            "unpkg.com",
            "cdnjs.cloudflare.com",
            "cdn.jsdelivr.net",
            "fonts.googleapis.com",
            "fonts.gstatic.com"
    );

    /** 高威胁代码模式，命中直接 fatal */
    private static final List<Pattern> THREAT_PATTERNS = List.of(
            // eval() 调用（排除 JSON.parse 场景中的误识别降低误杀）
            Pattern.compile("\\beval\\s*\\((?!\\s*JSON)", Pattern.CASE_INSENSITIVE),
            // document.cookie 窃取
            Pattern.compile("document\\.cookie\\s*(?:\\+?=|\\+)", Pattern.CASE_INSENSITIVE),
            // 加密挖矿脚本
            Pattern.compile("\\b(?:coinhive|cryptonight|minero|cryptoloot|coin-hive)\\b", Pattern.CASE_INSENSITIVE),
            // Base64 解码后立即执行（混淆载荷）
            Pattern.compile("\\batob\\s*\\([^)]*\\)\\s*[;,]?\\s*(?:eval|Function)\\s*\\(", Pattern.CASE_INSENSITIVE),
            // localStorage/sessionStorage 读取后外泄到 fetch/XHR/axios
            Pattern.compile(
                    "(?:localStorage|sessionStorage)\\.getItem\\([^)]*\\)[\\s\\S]{0,200}(?:fetch|XMLHttpRequest|axios|\\.open\\s*\\()",
                    Pattern.CASE_INSENSITIVE
            )
    );

    /** 匹配 <script src="..."> 中的外部 URL */
    private static final Pattern SCRIPT_SRC_PATTERN = Pattern.compile(
            "<script[^>]+src\\s*=\\s*[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );

    /** 匹配 fetch() 或 XHR.open() 中的 URL */
    private static final Pattern FETCH_URL_PATTERN = Pattern.compile(
            "(?:fetch|new\\s+XMLHttpRequest[^;]*\\.open)\\s*\\(?\\s*[\"']?(https?://[^\"'\\s)]+)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Check plain text for security violations without needing OutputGuardrailRequest.
     * Used for post-stream validation in reactive pipelines where output guardrails
     * would otherwise buffer the entire response before emitting any tokens.
     *
     * @return violation message if a threat is detected, null if safe
     */
    public String checkText(String text) {
        if (text == null || text.isBlank()) return null;

        // 第一层：高威胁模式检测
        for (Pattern pattern : THREAT_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return "生成的代码包含不安全的模式，已拦截";
            }
        }

        // 第二层：script src 白名单检查
        Matcher scriptMatcher = SCRIPT_SRC_PATTERN.matcher(text);
        while (scriptMatcher.find()) {
            String src = scriptMatcher.group(1);
            if (isExternalUrl(src) && !isWhitelisted(src)) {
                return "生成的代码包含不安全的模式，已拦截";
            }
        }

        // 第三层：fetch/XHR URL 白名单检查
        Matcher fetchMatcher = FETCH_URL_PATTERN.matcher(text);
        while (fetchMatcher.find()) {
            String url = fetchMatcher.group(1);
            if (!isWhitelisted(url)) {
                return "生成的代码包含不安全的模式，已拦截";
            }
        }

        return null;
    }

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest request) {
        String text = request.responseFromLLM().aiMessage().text();
        String violation = checkText(text);
        return violation != null ? fatal(violation) : success();
    }

    private boolean isExternalUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private boolean isWhitelisted(String url) {
        return CDN_WHITELIST.stream().anyMatch(url::contains);
    }
}

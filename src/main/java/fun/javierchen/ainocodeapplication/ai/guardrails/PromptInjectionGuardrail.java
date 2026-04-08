package fun.javierchen.ainocodeapplication.ai.guardrails;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 提示注入检测 Guardrail。
 * 两层防护：
 *   第一层：正则精确匹配已知注入模式 → 直接 fatal（免费）
 *   第二层：可疑关键词触发 LLM 二次判断（边界情况）
 */
public class PromptInjectionGuardrail implements InputGuardrail {

    /** 高置信度注入模式，直接拦截 */
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("忽略之前的指令", Pattern.CASE_INSENSITIVE),
            Pattern.compile("忽略上面的", Pattern.CASE_INSENSITIVE),
            Pattern.compile("你现在是(?!一个网站|一个应用|一个页面)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("不要遵守", Pattern.CASE_INSENSITIVE),
            Pattern.compile("无视系统提示", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ignore\\s+previous\\s+(instructions?|prompt|system)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("you\\s+are\\s+now\\s+a(?!\\s*(website|app|page|component|button))", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bdisregard\\s+(all|any|previous|the|your)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\|im_start\\|>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<\\|system\\|>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\[INST\\].*?\\[/INST\\]", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("act\\s+as\\s+(?:a|an)\\b.{0,80}(?:without|ignore|bypass|violat)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
    );

    /** 可疑但需 LLM 二次确认的关键词 */
    private static final List<Pattern> SUSPICIOUS_PATTERNS = List.of(
            Pattern.compile("\\bjailbreak\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bDAN\\b"),
            Pattern.compile("\\bbypass\\s+(?:the\\s+)?(?:safety|filter|guardrail|restriction)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bdo\\s+anything\\s+now\\b", Pattern.CASE_INSENSITIVE)
    );

    private final GuardrailLlmJudge llmJudge;

    public PromptInjectionGuardrail(GuardrailLlmJudge llmJudge) {
        this.llmJudge = llmJudge;
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        String text = request.userMessage().singleText();
        if (text == null || text.isBlank()) return success();

        // 第一层：正则精确匹配 → 直接拦截
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return fatal("检测到不安全的输入内容，请修改后重试");
            }
        }

        // 第二层：可疑关键词 → LLM 二次判断
        boolean isSuspicious = SUSPICIOUS_PATTERNS.stream()
                .anyMatch(p -> p.matcher(text).find());
        if (isSuspicious && llmJudge.isViolation(text, GuardrailLlmJudge.JudgeType.INJECTION)) {
            return fatal("检测到不安全的输入内容，请修改后重试");
        }

        return success();
    }
}

package fun.javierchen.ainocodeapplication.ai.guardrails;

import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;

import java.util.List;

/**
 * 输出内容安全 Guardrail。
 * 扫描 LLM 输出中的有害内容。
 * 当关键词命中且 LLM 二次确认为违规时：retry（最多 2 次）。
 * 触发 maxRetries 后由框架抛出 OutputGuardrailException。
 */
public class ContentSafetyOutputGuardrail implements OutputGuardrail {

    /** 需要关注的输出关键词（命中后由 LLM 二次确认） */
    private static final List<String> WATCH_KEYWORDS = List.of(
            // 明显有害词汇
            "fuck", "shit", "bitch", "bastard", "asshole", "motherfucker",
            // 仇恨言论
            "hate speech", "racial slur", "white supremacy", "nazi",
            // 非法指令类
            "how to make a bomb", "how to synthesize", "how to hack into",
            // 竞品引用（可选扩展）
            "chatgpt is better", "use openai instead"
    );

    private final GuardrailLlmJudge llmJudge;

    public ContentSafetyOutputGuardrail(GuardrailLlmJudge llmJudge) {
        this.llmJudge = llmJudge;
    }

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest request) {
        String text = request.responseFromLLM().aiMessage().text();
        if (text == null || text.isBlank()) return success();

        String lower = text.toLowerCase();

        boolean flagged = WATCH_KEYWORDS.stream()
                .anyMatch(k -> lower.contains(k.toLowerCase()));

        if (flagged && llmJudge.isViolation(text, GuardrailLlmJudge.JudgeType.MODERATION)) {
            return retry("生成内容不符合安全规范，正在重新生成...");
        }

        return success();
    }
}

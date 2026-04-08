package fun.javierchen.ainocodeapplication.ai.guardrails;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;

/**
 * 输入长度限制 Guardrail。
 * 硬性字符数上限，最先执行以拦截 token 滥用请求，零成本。
 * HTML: 5000 字符，Vue: 8000 字符（由调用方配置）。
 */
public class InputLengthGuardrail implements InputGuardrail {

    private final int maxLength;

    public InputLengthGuardrail(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        String text = request.userMessage().singleText();
        if (text != null && text.length() > maxLength) {
            return fatal("输入内容过长，请精简后重试（最大 " + maxLength + " 字符）");
        }
        return success();
    }
}

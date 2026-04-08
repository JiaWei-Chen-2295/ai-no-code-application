package fun.javierchen.ainocodeapplication.ai.guardrails;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.List;

/**
 * 输入内容审核 Guardrail。
 * 两层防护：
 *   第一层：中英文关键词黑名单（高置信度违规）→ 直接 fatal
 *   第二层：不确定关键词 → LLM 分类器判断
 */
public class ContentModerationInputGuardrail implements InputGuardrail {

    /** 高置信度违规关键词，直接拦截（不依赖上下文） */
    private static final List<String> CLEAR_BLACKLIST = List.of(
            // 中文
            "色情", "裸体", "性爱", "强奸", "制毒", "合成毒品", "炸弹制作", "如何制作炸药",
            "枪支改装", "人口贩卖", "洗钱教程", "暗网购买", "恐怖袭击",
            // 英文
            "child pornography", "cp porn", "make a bomb", "bomb making",
            "how to make drugs", "synthesize meth", "human trafficking tutorial",
            "terrorist attack plan", "white supremacy manifesto"
    );

    /** 需要结合上下文判断的不确定关键词 */
    private static final List<String> UNCERTAIN_KEYWORDS = List.of(
            "暴力", "自杀", "武器", "黑客攻击", "入侵", "渗透测试", "漏洞利用",
            "violence", "suicide", "weapon", "hacking", "exploit", "crack", "malware",
            "phishing", "ransomware"
    );

    private final GuardrailLlmJudge llmJudge;

    public ContentModerationInputGuardrail(GuardrailLlmJudge llmJudge) {
        this.llmJudge = llmJudge;
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        String text = request.userMessage().singleText();
        if (text == null || text.isBlank()) return success();

        String lower = text.toLowerCase();

        // 第一层：黑名单关键词 → 直接拦截
        for (String keyword : CLEAR_BLACKLIST) {
            if (lower.contains(keyword.toLowerCase())) {
                return fatal("输入包含不合规内容，无法处理此请求");
            }
        }

        // 第二层：不确定关键词 → LLM 分类器
        boolean hasUncertain = UNCERTAIN_KEYWORDS.stream()
                .anyMatch(k -> lower.contains(k.toLowerCase()));
        if (hasUncertain && llmJudge.isViolation(text, GuardrailLlmJudge.JudgeType.MODERATION)) {
            return fatal("输入包含不合规内容，无法处理此请求");
        }

        return success();
    }
}

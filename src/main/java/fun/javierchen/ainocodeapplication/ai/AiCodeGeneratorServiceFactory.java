package fun.javierchen.ainocodeapplication.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.config.OutputGuardrailsConfig;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import fun.javierchen.ainocodeapplication.ai.guardrails.CodeExfiltrationOutputGuardrail;
import fun.javierchen.ainocodeapplication.ai.guardrails.ContentModerationInputGuardrail;
import fun.javierchen.ainocodeapplication.ai.guardrails.ContentSafetyOutputGuardrail;
import fun.javierchen.ainocodeapplication.ai.guardrails.GuardrailLlmJudge;
import fun.javierchen.ainocodeapplication.ai.guardrails.InputLengthGuardrail;
import fun.javierchen.ainocodeapplication.ai.guardrails.PromptInjectionGuardrail;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import fun.javierchen.ainocodeapplication.ai.tools.AiTool;
import fun.javierchen.ainocodeapplication.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.List;

@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {
    @Resource
    private ChatModel chatModel;
    @Resource
    private StreamingChatModel streamingChatModel;
    @Resource
    private StreamingChatModel reasoningStreamingChatModel;
    @Resource
    @Lazy
    private ChatHistoryService chatHistoryService;
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;
    
    // AI 工具数组注入 - Spring 自动收集所有 AiTool 接口的实现类
    @Resource
    private AiTool[] aiTools;

    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            // 有缓存就要设置过期时间
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.info("缓存被删除了，key: {}, value: {}, cause: {}", key, value, cause);
            })
            .build();

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return createAiCodeGeneratorService(0L, CodeGenTypeEnum.HTML);
    }

    /**
     * 兼容老逻辑的获取服务
     *
     * @param appId
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据 appId 获取服务
     *
     * @param appId       应用 id
     * @param codeGenType 生成类型
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 创建专用于元素建议生成的轻量服务实例（无记忆、无流式、带输入守卫）。
     * 使用独立接口 {@link ElementSuggestionAiService} 避免 @MemoryId 触发
     * LangChain4j 对 ChatMemoryProvider 的强制校验。
     */
    public ElementSuggestionAiService createSuggestionService() {
        var llmJudge = new GuardrailLlmJudge(chatModel);
        List<InputGuardrail> inputGuardrails = List.of(
                new InputLengthGuardrail(8000),
                new PromptInjectionGuardrail(llmJudge),
                new ContentModerationInputGuardrail(llmJudge)
        );
        return AiServices.builder(ElementSuggestionAiService.class)
                .chatModel(chatModel)
                .inputGuardrails(inputGuardrails.toArray(new InputGuardrail[0]))
                .build();
    }


    public AiCodeGeneratorService createAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenType) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                // 通过 appId 隔离会话 实现会话记忆
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 从数据库加载记忆
        chatHistoryService.loadHistoryToMemory(appId, chatMemory, 20);
        
        // 记录收集到的 AI 工具
        if (aiTools != null && aiTools.length > 0) {
            log.info("已收集到 {} 个 AI 工具: {}", aiTools.length, 
                java.util.Arrays.stream(aiTools)
                    .map(tool -> tool.getClass().getSimpleName())
                    .collect(java.util.stream.Collectors.joining(", ")));
        } else {
            log.warn("未收集到任何 AI 工具");
        }

        // 根据代码生成类型选择不同的模型配置，再统一叠加 Guardrails
        return switch (codeGenType) {
            // Vue 项目生成使用推理模型
            case VUE_PROJECT -> applyCommonGuardrails(
                    AiServices.builder(AiCodeGeneratorService.class)
                            .streamingChatModel(reasoningStreamingChatModel)
                            .chatMemoryProvider(memoryId -> chatMemory)
                            .tools(aiTools)
                            .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                                    toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
                            )),
                    codeGenType
            ).build();
            // HTML 和多文件生成使用默认模型
            case HTML, MUTI_FILE -> applyCommonGuardrails(
                    AiServices.builder(AiCodeGeneratorService.class)
                            .chatModel(chatModel)
                            .streamingChatModel(streamingChatModel)
                            .chatMemory(chatMemory),
                    codeGenType
            ).build();
        };
    }

    /**
     * 向任意 builder 分支追加公共安全 Guardrails。
     * 不替换原有模型 / Memory / Tool 配置，仅叠加安全层。
     *
     * <p><b>Output guardrails 与流式响应的兼容性说明</b>：<br>
     * LangChain4j 的 OutputGuardrail 机制会在流式输出期间
     * 将所有 token 缓存到 {@code responseBuffer}，等待完整响应生成后才统一执行校验，
     * 校验通过后再一次性将所有缓存 token 推送给订阅者。
     * 这导致 SSE 推流时所有消息几乎同时到达，失去实时性。<br>
     * 因此，对 HTML/MUTI_FILE 类型（纯文本流式输出），不在此处挂载 Output Guardrails；
     * 改为在 {@code codeGenerateAndSaveStream()} 的 {@code concatWith(Flux.defer(...))} 中
     * 对完整积累文本执行 post-stream 安全校验，兼顾安全与流式实时性。<br>
     * VUE_PROJECT 主要依赖工具调用（文件读写），文本 token 极少，保留 Output Guardrails 无影响。
     *
     * @param builder     已配置好业务能力的 AiServices builder
     * @param codeGenType 生成类型（用于确定 InputLengthGuardrail 的长度上限）
     * @return 追加 Guardrails 后的 builder（仍需调用 .build()）
     */
    @SuppressWarnings({"unchecked", "varargs"})
    private AiServices<AiCodeGeneratorService> applyCommonGuardrails(
            AiServices<AiCodeGeneratorService> builder,
            CodeGenTypeEnum codeGenType
    ) {
        int maxInputLength = (codeGenType == CodeGenTypeEnum.VUE_PROJECT) ? 8000 : 5000;
        var llmJudge = new GuardrailLlmJudge(chatModel);

        List<InputGuardrail> inputGuardrails = List.of(
                new InputLengthGuardrail(maxInputLength),
                new PromptInjectionGuardrail(llmJudge),
                new ContentModerationInputGuardrail(llmJudge)
        );

        builder = builder.inputGuardrails(inputGuardrails.toArray(new InputGuardrail[0]));

        // HTML/MUTI_FILE: skip output guardrails here — post-stream check is done in
        // AiGenerateServiceFacade.codeGenerateAndSaveStream() to preserve real-time SSE streaming.
        if (codeGenType == CodeGenTypeEnum.VUE_PROJECT) {
            List<OutputGuardrail> outputGuardrails = List.of(
                    new ContentSafetyOutputGuardrail(llmJudge),
                    new CodeExfiltrationOutputGuardrail()
            );
            builder = builder
                    .outputGuardrails(outputGuardrails.toArray(new OutputGuardrail[0]))
                    .outputGuardrailsConfig(OutputGuardrailsConfig.builder().maxRetries(2).build());
        }

        return builder;
    }

    /**
     * 构建缓存键
     *
     * @param appId
     * @param codeGenType
     * @return
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getType();
    }


}

package fun.javierchen.ainocodeapplication.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
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


        // 根据代码生成类型选择不同的模型配置
        return switch (codeGenType) {
            // Vue 项目生成使用推理模型
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(reasoningStreamingChatModel)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .tools(aiTools)
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                            toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
                    ))
                    .build();
            // HTML 和多文件生成使用默认模型
            case HTML, MUTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(streamingChatModel)
                    .chatMemory(chatMemory)
                    .build();
        };
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

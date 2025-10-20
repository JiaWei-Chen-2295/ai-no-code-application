package fun.javierchen.ainocodeapplication.core;

import cn.hutool.json.JSONUtil;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import fun.javierchen.ainocodeapplication.ai.model.AiResponseMessage;
import fun.javierchen.ainocodeapplication.ai.model.ToolExecutedMessage;
import fun.javierchen.ainocodeapplication.ai.model.ToolRequestMessage;
import reactor.core.publisher.Flux;

public class TokenStream2FluxAdaptor {

    /**
     * 将 TokenStream 转换为 Flux
     * @param tokenStream
     * @return
     */
    public static Flux<String> adapt(TokenStream tokenStream) {
        return Flux.<String>create(
                sink -> {
                    tokenStream.onPartialResponse((String partialResponse) -> {
                                // 过滤空响应，避免发送无效数据
                                if (partialResponse != null && !partialResponse.isEmpty()) {
                                    AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                                    sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                                }
                            })
                            .onPartialToolExecutionRequest(
                                    (index, toolExecutionRequest) -> {
                                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                                    }
                            ).onToolExecuted(
                                    (ToolExecution toolExecution) -> {
                                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                                    }
                            ).onCompleteResponse((ChatResponse resp) ->
                                    sink.complete()
                            ).onError((Throwable e) -> {
                                e.printStackTrace();
                            }).start();
                }
        );

    }
}

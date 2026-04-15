package fun.javierchen.ainocodeapplication.ai;

import dev.langchain4j.service.SystemMessage;
import fun.javierchen.ainocodeapplication.ai.model.ElementSuggestionsResult;

/**
 * 专用于元素修改建议生成的 AI 服务接口（无 @MemoryId，无流式）。
 * 与 {@link AiCodeGeneratorService} 分离，避免 LangChain4j 在构建时
 * 因 @MemoryId 存在而强制要求 ChatMemoryProvider。
 */
public interface ElementSuggestionAiService {

    @SystemMessage(fromResource = "prompt/element-suggestion-prompt.txt")
    ElementSuggestionsResult generateElementSuggestions(String userMessage);
}

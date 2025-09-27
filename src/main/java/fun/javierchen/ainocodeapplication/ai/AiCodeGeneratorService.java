package fun.javierchen.ainocodeapplication.ai;

import dev.langchain4j.service.SystemMessage;
import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    /**
     * 生成单网页代码
     */
    @SystemMessage(fromResource = "prompt/singleton-html-prompt.txt")
    HtmlCodeResult generateSingleHTMLCode(String userMessage);

    /**
     * 生成多文件网页
     */
    @SystemMessage(fromResource = "prompt/muti-file-origin-prompt.txt")
    MultiFileCodeResult generateMultiHTMLCode(String userMessage);

    /**
     * 使用流式输出生成单网页代码
     */
    @SystemMessage(fromResource = "prompt/singleton-html-prompt.txt")
    Flux<String> generateSingleHTMLCodeStream(String userMessage);

    /**
     * 使用流式输出生成多文件网页
     */
    @SystemMessage(fromResource = "prompt/muti-file-origin-prompt.txt")
    Flux<String> generateMultiHTMLCodeStream(String userMessage);

}

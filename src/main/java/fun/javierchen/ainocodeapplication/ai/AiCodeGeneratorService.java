package fun.javierchen.ainocodeapplication.ai;

import dev.langchain4j.service.SystemMessage;
import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;

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
}

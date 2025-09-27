package fun.javierchen.ainocodeapplication.ai;

import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void testGenerateSingletonHTMLCode() {
        String userMessage = "请生成一个网页，让用户进行登陆，行数不超过三十行";
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateSingleHTMLCode(userMessage);
        System.out.println(htmlCodeResult);
    }

    @Test
    void testGenerateMutiHTMLCode() {
        String userMessage = "请生成一个网页，让用户进行登陆，行数不超过三十行";
        MultiFileCodeResult mutiFIleCodeResult = aiCodeGeneratorService.generateMultiHTMLCode(userMessage);
        System.out.println(mutiFIleCodeResult);
    }

}
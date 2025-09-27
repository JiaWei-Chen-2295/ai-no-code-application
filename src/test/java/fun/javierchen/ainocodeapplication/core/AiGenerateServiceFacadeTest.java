package fun.javierchen.ainocodeapplication.core;

import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiGenerateServiceFacadeTest {

    @Resource
    private AiGenerateServiceFacade aiGenerateServiceFacade;

    @Test
    void generateHtmlCode() {
        File file = aiGenerateServiceFacade.generateAndSaveFile("生成一个注册页面 行数小于30行", CodeGenTypeEnum.HTML);
        assertNotNull(file);
    }

    @Test
    void generateMutiFileCode() {
        File result = aiGenerateServiceFacade.generateAndSaveFile("生成一个注册页面 行数小于30行", CodeGenTypeEnum.MUTI_FILE);
        assertNotNull(result);
    }


    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiGenerateServiceFacade.generateAndSaveFileStream("任务记录网站", CodeGenTypeEnum.MUTI_FILE);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }


}
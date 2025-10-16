package fun.javierchen.ainocodeapplication.core;

import cn.hutool.core.util.IdUtil;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiGenerateServiceFacadeTest {

    @Resource
    private AiGenerateServiceFacade aiGenerateServiceFacade;



    @Test
    void generateHtmlCode() {
        Long appId = new Random().nextLong();
        File file = aiGenerateServiceFacade.generateAndSaveFile("生成一个注册页面 行数小于30行", CodeGenTypeEnum.HTML, appId, 1);
        assertNotNull(file);
    }

    @Test
    void generateMutiFileCode() {
        Long appId = new Random().nextLong();
        File result = aiGenerateServiceFacade.generateAndSaveFile("生成一个注册页面 行数小于30行", CodeGenTypeEnum.MUTI_FILE, appId, 2);
        assertNotNull(result);
    }


    @Test
    void generateAndSaveSingleCodeStream() {
        Long appId = new Random().nextLong();
        Flux<String> codeStream = aiGenerateServiceFacade.generateAndSaveFileStream("任务记录网站", CodeGenTypeEnum.HTML, appId);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

    @Test
    void generateAndSaveMutiCodeStream() {
        Long appId = new Random().nextLong();
        Flux<String> codeStream = aiGenerateServiceFacade.generateAndSaveFileStream("任务记录网站", CodeGenTypeEnum.MUTI_FILE, appId);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiGenerateServiceFacade.generateAndSaveFileStream(
                "生成一个好习惯记录网站，支持倒计时，计时功能，简单实现，代码不超过300行",
                CodeGenTypeEnum.VUE_PROJECT, 8L, (codeParseResult, progress) -> {});
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }



}
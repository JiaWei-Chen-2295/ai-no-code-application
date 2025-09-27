package fun.javierchen.ainocodeapplication.core;

import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

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

}
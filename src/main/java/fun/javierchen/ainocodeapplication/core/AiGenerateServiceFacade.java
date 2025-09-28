package fun.javierchen.ainocodeapplication.core;

import fun.javierchen.ainocodeapplication.ai.AiCodeGeneratorService;
import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import fun.javierchen.ainocodeapplication.core.parser.CodeParserExecutor;
import fun.javierchen.ainocodeapplication.core.saver.CodeFileSaverExecutor;
import fun.javierchen.ainocodeapplication.exceptiom.BusinessException;
import fun.javierchen.ainocodeapplication.exceptiom.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 门面模式
 * 封装了所有服务，提供统一入口
 */
@Slf4j
@AllArgsConstructor
@Component
public class AiGenerateServiceFacade {
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 根据用户的输入生成代码并保存 返回代码保存的目录
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    File generateAndSaveFile(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的内容为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateSingleHTMLCode(userMessage);
                yield CodeFileSaverExecutor.saveCodeFile(htmlCodeResult, CodeGenTypeEnum.HTML, appId);
            }
            case MUTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiHTMLCode(userMessage);
                yield CodeFileSaverExecutor.saveCodeFile(multiFileCodeResult, CodeGenTypeEnum.MUTI_FILE, appId);
            }
        };
    }

    /**
     * 根据代码类型流式生成代码并保存
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public Flux<String> generateAndSaveFileStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的内容为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveSingleHTMLCodeStream(userMessage, appId);
            case MUTI_FILE -> generateAndSaveMultiHTMLCodeStream(userMessage, appId);
        };
    }

    /**
     * 流式生成单文件代码并保存
     *
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveSingleHTMLCodeStream(String userMessage, Long appId) {
        Flux<String> result = aiCodeGeneratorService.generateMultiHTMLCodeStream(userMessage);
        return codeGenerateAndSaveStream(result, CodeGenTypeEnum.HTML, appId);
    }

    /**
     * 流式生成多文件代码并保存
     *
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveMultiHTMLCodeStream(String userMessage, Long appId) {
        Flux<String> result = aiCodeGeneratorService.generateMultiHTMLCodeStream(userMessage);
        return codeGenerateAndSaveStream(result, CodeGenTypeEnum.MUTI_FILE, appId);
    }


    /**
     * 统一流式处理类
     *
     * @param result
     * @param codeGenType
     * @return
     */
    private Flux<String> codeGenerateAndSaveStream(Flux<String> result, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder sb = new StringBuilder();
        return result.doOnNext(
                        chunk -> sb.append(chunk)
                )
                .doOnComplete(() -> {
                    String strResult = sb.toString();
                    Object executeResult = CodeParserExecutor.execute(strResult, codeGenType);
                    File file = CodeFileSaverExecutor.saveCodeFile(executeResult, codeGenType, appId);
                    log.info("保存成功：{}", file.getAbsolutePath());
                });
    }


}

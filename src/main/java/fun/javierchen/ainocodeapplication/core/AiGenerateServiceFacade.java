package fun.javierchen.ainocodeapplication.core;

import fun.javierchen.ainocodeapplication.ai.AiCodeGeneratorService;
import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import fun.javierchen.ainocodeapplication.exceptiom.BusinessException;
import fun.javierchen.ainocodeapplication.exceptiom.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

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
    File generateAndSaveFile(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的内容为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateSingleHTMLCode(userMessage);
            case MUTI_FILE -> generateMultiHTMLCode(userMessage);
        };
    }

    /**
     * 根据代码类型流式生成代码并保存
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public Flux<String> generateAndSaveFileStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的内容为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveSingleHTMLCodeStream(userMessage);
            case MUTI_FILE -> generateAndSaveMultiHTMLCodeStream(userMessage);
        };
    }

    /**
     * 流式生成单文件代码并保存
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveSingleHTMLCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiHTMLCodeStream(userMessage);
        StringBuilder sb = new StringBuilder();
        return result.doOnNext(
                        chunk -> sb.append(chunk)
                )
                .doOnComplete(() -> {
                    String strResult = sb.toString();
                    HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(strResult);
                    File file = CodeFileSaver.saveSingletonHtmlCode(htmlCodeResult);
                    log.info("保存成功：{}", file.getAbsolutePath());
                });
    }

    /**
     * 流式生成多文件代码并保存
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveMultiHTMLCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiHTMLCodeStream(userMessage);
        StringBuilder sb = new StringBuilder();
        return result.doOnNext(
                chunk -> sb.append(chunk)
                )
                .doOnComplete(() -> {
                    String strResult = sb.toString();
                    MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(strResult);
                    File file = CodeFileSaver.saveMutiFileCode(multiFileCodeResult);
                    log.info("保存成功：{}", file.getAbsolutePath());
                });
    }

    /**
     * 生成并保存单文件代码
     *
     * @param userMessage
     * @return
     */
    private File generateSingleHTMLCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateSingleHTMLCode(userMessage);
        return CodeFileSaver.saveSingletonHtmlCode(htmlCodeResult);
    }

    /**
     * 生成并保存多文件代码
     *
     * @param userMessage
     * @return
     */
    private File generateMultiHTMLCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiHTMLCode(userMessage);
        return CodeFileSaver.saveMutiFileCode(multiFileCodeResult);
    }


}

package fun.javierchen.ainocodeapplication.core;

import com.mybatisflex.core.query.QueryWrapper;
import fun.javierchen.ainocodeapplication.ai.AiCodeGeneratorService;
import fun.javierchen.ainocodeapplication.ai.AiCodeGeneratorServiceFactory;
import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import fun.javierchen.ainocodeapplication.core.model.CodeParseResult;
import fun.javierchen.ainocodeapplication.core.parser.CodeParserExecutor;
import fun.javierchen.ainocodeapplication.core.saver.CodeFileSaverExecutor;
import fun.javierchen.ainocodeapplication.exceptiom.BusinessException;
import fun.javierchen.ainocodeapplication.exceptiom.ErrorCode;
import fun.javierchen.ainocodeapplication.model.entity.ChatHistory;
import fun.javierchen.ainocodeapplication.service.ChatHistoryService;
import fun.javierchen.ainocodeapplication.service.AppService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static com.mybatisflex.core.query.QueryMethods.max;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 门面模式
 * 封装了所有服务，提供统一入口
 */
@Slf4j
@Component
@AllArgsConstructor
public class AiGenerateServiceFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;
    @Lazy
    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;
    @Resource
    private AppService appService;

    /**
     * 根据用户的输入生成代码并保存 返回代码保存的目录
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    File generateAndSaveFile(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId, int version) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的内容为空");
        }
        var aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateSingleHTMLCode(userMessage);
                yield CodeFileSaverExecutor.saveCodeFile(htmlCodeResult, CodeGenTypeEnum.HTML, appId, version);
            }
            case MUTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiHTMLCode(userMessage);
                yield CodeFileSaverExecutor.saveCodeFile(multiFileCodeResult, CodeGenTypeEnum.MUTI_FILE, appId, version);
            }
            case VUE_PROJECT -> {
                // Vue项目使用工具调用方式，AI直接操作文件系统
                // 这里创建项目目录结构，让AI工具在其中工作
                yield appService.saveVueProject(appId, version);
            }
        };
    }

    /**
     * 根据代码类型流式生成代码并保存
     *
     * @param userMessage
     * @param codeGenTypeEnum
     * @param appId
     * @param onCompleteCallback 完成时的回调，传入解析结果和版本号
     * @return
     */
    public Flux<String> generateAndSaveFileStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,
                                                  Long appId, BiConsumer<CodeParseResult, Integer> onCompleteCallback) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的内容为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveSingleHTMLCodeStream(userMessage, appId, onCompleteCallback);
            case MUTI_FILE -> generateAndSaveMultiHTMLCodeStream(userMessage, appId, onCompleteCallback);
            case VUE_PROJECT -> generateAndSaveVueProjectCodeStream(userMessage, appId, onCompleteCallback);
        };
    }

    /**
     * 根据代码类型流式生成代码并保存（兼容方法）
     *
     * @deprecated 使用带回调的方法替代
     */
    @Deprecated
    public Flux<String> generateAndSaveFileStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        return generateAndSaveFileStream(userMessage, codeGenTypeEnum, appId, null);
    }

    /**
     * 支持传入Consumer的兼容方法
     */
//    public Flux<String> generateAndSaveFileStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,
//                                                 Long appId, Consumer<CodeParseResult> onCompleteCallback) {
//        BiConsumer<CodeParseResult, Integer> biConsumer = null;
//        if (onCompleteCallback != null) {
//            biConsumer = (parseResult, version) -> onCompleteCallback.accept(parseResult);
//        }
//        return generateAndSaveFileStream(userMessage, codeGenTypeEnum, appId, biConsumer);
//    }

    /**
     * 流式生成单文件代码并保存
     *
     * @param userMessage
     * @param appId
     * @param onCompleteCallback
     * @return
     */
    private Flux<String> generateAndSaveSingleHTMLCodeStream(String userMessage, Long appId,
                                                             BiConsumer<CodeParseResult, Integer> onCompleteCallback) {
        var aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        Flux<String> result = aiCodeGeneratorService.generateSingleHTMLCodeStream(userMessage);
        return codeGenerateAndSaveStream(result, CodeGenTypeEnum.HTML, appId, onCompleteCallback);
    }

    /**
     * 流式生成多文件代码并保存
     *
     * @param userMessage
     * @param appId
     * @param onCompleteCallback
     * @return
     */
    private Flux<String> generateAndSaveMultiHTMLCodeStream(String userMessage, Long appId,
                                                            BiConsumer<CodeParseResult, Integer> onCompleteCallback) {
        var aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        Flux<String> result = aiCodeGeneratorService.generateMultiHTMLCodeStream(userMessage);
        return codeGenerateAndSaveStream(result, CodeGenTypeEnum.MUTI_FILE, appId, onCompleteCallback);
    }

    /**
     * 流式输出Vue工程代码并保存
     *
     * @param userMessage
     * @param appId
     * @param onCompleteCallback
     * @return
     */
    private Flux<String> generateAndSaveVueProjectCodeStream(String userMessage, Long appId, BiConsumer<CodeParseResult, Integer> onCompleteCallback) {
        try {
            // 1. 先获取版本号并创建项目目录结构（在AI生成之前）
            int version = appService.getNextVersion(appId);
            File projectDir = appService.saveVueProject(appId, version);
            
            log.info("Vue项目目录已准备完成，开始AI生成: {}, 版本: {}", 
                    projectDir.getAbsolutePath(), version);

            // 2. 获取AI服务并开始生成（AI将在正确的项目目录中工作）
            var aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, CodeGenTypeEnum.VUE_PROJECT);
            Flux<String> result = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);

            // 3. 处理生成流程
            StringBuilder sb = new StringBuilder();
            return result.doOnNext(sb::append)
                    .doOnComplete(() -> {
                        try {
                            log.info("Vue项目AI生成完成，项目目录: {}, 版本: {}", 
                                    projectDir.getAbsolutePath(), version);

                            // 通过回调返回结果
                            if (onCompleteCallback != null) {
                                CodeParseResult parseResult = new CodeParseResult(true, true, true, null);
                                onCompleteCallback.accept(parseResult, version);
                            }
                        } catch (Exception e) {
                            log.error("Vue项目生成回调处理失败: {}", e.getMessage(), e);
                            if (onCompleteCallback != null) {
                                CodeParseResult parseResult = new CodeParseResult(false, false, false,
                                        "生成回调处理失败: " + e.getMessage());
                                onCompleteCallback.accept(parseResult, version);
                            }
                        }
                    })
                    .doOnError(error -> {
                        log.error("Vue项目AI生成失败: {}", error.getMessage(), error);
                        if (onCompleteCallback != null) {
                            CodeParseResult parseResult = new CodeParseResult(false, false, false,
                                    "AI生成失败: " + error.getMessage());
                            onCompleteCallback.accept(parseResult, version);
                        }
                    });

        } catch (Exception e) {
            log.error("Vue项目目录创建失败: {}", e.getMessage(), e);
            
            // 返回错误流
            return Flux.error(new RuntimeException("Vue项目目录创建失败: " + e.getMessage(), e));
        }
    }

    /**
     * 统一流式处理类
     *
     * @param result
     * @param codeGenType
     * @param appId
     * @param onCompleteCallback
     * @return
     */
    private Flux<String> codeGenerateAndSaveStream(Flux<String> result, CodeGenTypeEnum codeGenType,
                                                   Long appId, BiConsumer<CodeParseResult, Integer> onCompleteCallback) {
        StringBuilder sb = new StringBuilder();
        return result.doOnNext(
                        sb::append
                )
                .doOnComplete(() -> {
                    String strResult = sb.toString();
                    // 使用新的解析器，一次解析获取所有信息
                    CodeParseResult parseResult = CodeParserExecutor.executeWithResult(strResult, codeGenType);

                    int version = 1; // 默认版本

                    if (parseResult.isParseSuccess() && parseResult.isHasValidCode() && parseResult.getParsedCode() != null) {
                        // 计算新版本号：查询该应用下最新的代码版本号
                        version = getNextCodeVersion(appId);

                        // 保存代码文件，使用版本号
                        File file = CodeFileSaverExecutor.saveCodeFile(parseResult.getParsedCode(), codeGenType, appId, version);
                        log.info("代码解析成功，保存到：{}，版本号：{}，包含有效代码：{}",
                                file.getAbsolutePath(), version, parseResult.isHasValidCode());
                    } else {
                        log.warn("代码解析失败或无有效代码：{}", parseResult.getParseError());
                    }

                    // 通过回调返回解析结果和版本号，避免重复解析
                    if (onCompleteCallback != null) {
                        onCompleteCallback.accept(parseResult, version);
                    }
                });
    }

    /**
     * 获取下一个代码版本号
     *
     * @param appId 应用ID
     * @return 下一个版本号
     */
    private int getNextCodeVersion(Long appId) {
        try {
            // 查询该应用下最新的代码版本号
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .select(max("codeVersion").as("maxVersion"))
                    .from(ChatHistory.class)
                    .where(ChatHistory::getAppId).eq(appId)
                    .and(ChatHistory::getIsCode).eq(1)
                    .and(ChatHistory::getIsDelete).eq(0);
            int maxVersion = 0;
            Integer result = chatHistoryService.getOneAs(queryWrapper, Integer.class);
            if (result != null) {
                maxVersion = result;
            }

            return maxVersion + 1;
        } catch (Exception e) {
            log.warn("获取版本号失败，使用默认版本号1：{}", e.getMessage());
            return 1;
        }
    }

    /**
     * 检查AI生成结果是否包含有效代码
     *
     * @param aiResult    AI生成的结果
     * @param codeGenType 代码生成类型
     * @return 是否包含有效代码
     * @deprecated 直接使用CodeParseResult避免重复解析，推荐使用流式生成方法的回调
     */
    @Deprecated
    public boolean hasValidCode(String aiResult, CodeGenTypeEnum codeGenType) {
        CodeParseResult parseResult = CodeParserExecutor.executeWithResult(aiResult, codeGenType);
        return parseResult.isHasValidCode();
    }

    /**
     * 获取代码解析结果（推荐方法）
     *
     * @param aiResult    AI生成的结果
     * @param codeGenType 代码生成类型
     * @return 代码解析结果
     */
    public CodeParseResult getCodeParseResult(String aiResult, CodeGenTypeEnum codeGenType) {
        return CodeParserExecutor.executeWithResult(aiResult, codeGenType);
    }
}
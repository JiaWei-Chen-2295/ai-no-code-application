package fun.javierchen.ainocodeapplication.core;

import com.mybatisflex.core.query.QueryWrapper;
import dev.langchain4j.service.TokenStream;
import fun.javierchen.ainocodeapplication.ai.AiCodeGeneratorService;
import fun.javierchen.ainocodeapplication.ai.AiCodeGeneratorServiceFactory;
import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import fun.javierchen.ainocodeapplication.ai.guardrails.CodeExfiltrationOutputGuardrail;
import fun.javierchen.ainocodeapplication.core.handler.StreamHandlerExecutor;
import fun.javierchen.ainocodeapplication.core.model.CodeParseResult;
import fun.javierchen.ainocodeapplication.core.parser.CodeParserExecutor;
import fun.javierchen.ainocodeapplication.core.saver.CodeFileSaverExecutor;
import fun.javierchen.ainocodeapplication.exceptiom.BusinessException;
import fun.javierchen.ainocodeapplication.exceptiom.ErrorCode;
import fun.javierchen.ainocodeapplication.model.User;
import fun.javierchen.ainocodeapplication.model.entity.ChatHistory;
import fun.javierchen.ainocodeapplication.model.entity.App;
import fun.javierchen.ainocodeapplication.service.ChatHistoryService;
import fun.javierchen.ainocodeapplication.service.AppService;
import fun.javierchen.ainocodeapplication.service.ScreenshotService;
import fun.javierchen.ainocodeapplication.constant.CodeFileConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static com.mybatisflex.core.query.QueryMethods.max;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 门面模式
 * 封装了所有服务，提供统一入口
 */
@Slf4j
@Component
public class AiGenerateServiceFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;


    @Lazy
    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private fun.javierchen.ainocodeapplication.core.builder.VueProjectBuilder vueProjectBuilder;

    private static final int MAX_BUILD_FIX_ATTEMPTS = 1;

    @Resource
    private AppService appService;

    @Resource
    private ScreenshotService screenshotService;

    @Value("${app.screenshot.base-url}")
    private String screenshotBaseUrl;

    @Value("${app.screenshot.max-retries:3}")
    private int maxRetries;

    @Value("${app.screenshot.enabled:true}")
    private boolean screenshotEnabled;

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
                                                  Long appId, BiConsumer<CodeParseResult, Integer> onCompleteCallback, User loginUser) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的内容为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveSingleHTMLCodeStream(userMessage, appId, onCompleteCallback);
            case MUTI_FILE -> generateAndSaveMultiHTMLCodeStream(userMessage, appId, onCompleteCallback);
            case VUE_PROJECT -> generateAndSaveVueProjectCodeStream(userMessage, appId, onCompleteCallback, loginUser);
        };
    }

    /**
     * 根据代码类型流式生成代码并保存（兼容方法）
     *
     * @deprecated 使用带回调的方法替代
     */
    @Deprecated
    public Flux<String> generateAndSaveFileStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        return generateAndSaveFileStream(userMessage, codeGenTypeEnum, appId, null, null);
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
    private Flux<String> generateAndSaveVueProjectCodeStream(String userMessage, Long appId, BiConsumer<CodeParseResult, Integer> onCompleteCallback, User loginUser) {
        // 1. 先获取版本号并创建项目目录结构（在AI生成之前）
        int version = appService.getNextVersion(appId);
        File projectDir = appService.saveVueProject(appId, version);

        log.info("Vue项目目录已准备完成，开始AI生成: {}, 版本: {}",
                projectDir.getAbsolutePath(), version);

        // 2. 获取AI服务并开始生成（AI将在正确的项目目录中工作）
        var aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, CodeGenTypeEnum.VUE_PROJECT);
        TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
        Flux<String> result = TokenStream2FluxAdaptor.adapt(tokenStream);

        // 3. 将版本号传递给处理器，确保数据库版本号与文件系统版本号一致
        Flux<String> aiGenerationStream = streamHandlerExecutor.doExecute(result, chatHistoryService, appId, loginUser, CodeGenTypeEnum.VUE_PROJECT, version);

        // 4. 在AI生成完成后，尝试构建并在失败时自动修复
        Flux<String> buildAndFixStream = Flux.defer(() -> attemptBuildAndFix(projectDir, appId, version, loginUser, 0));

        return Flux.concat(aiGenerationStream, buildAndFixStream)
                .doOnComplete(() -> {
                    if (onCompleteCallback != null) {
                        CodeParseResult parseResult = new CodeParseResult(true, true, true, null);
                        onCompleteCallback.accept(parseResult, version);
                    }
                    // 异步截图保存封面
                    captureAndSaveCoverAsync(appId, version);
                    log.info("Vue项目AI生成完成，项目目录: {}, 版本: {}",
                            projectDir.getAbsolutePath(), version);
                })
                .doOnError(error -> {
                    log.error("Vue项目AI生成失败: {}", error.getMessage(), error);
                    if (onCompleteCallback != null) {
                        CodeParseResult parseResult = new CodeParseResult(false, false, false,
                                "AI生成失败: " + error.getMessage());
                        onCompleteCallback.accept(parseResult, version);
                    }
                });
    }

    /**
     * 尝试构建Vue项目，如果失败则让AI自动修复后重试
     *
     * @param projectDir 项目目录
     * @param appId      应用ID
     * @param version    版本号
     * @param loginUser  当前用户
     * @param attempt    当前尝试次数（从0开始）
     * @return 构建状态消息流
     */
    private Flux<String> attemptBuildAndFix(File projectDir, Long appId, int version, User loginUser, int attempt) {
        return Flux.defer(() -> {
            log.info("尝试构建Vue项目 (第{}次): {}", attempt + 1, projectDir.getAbsolutePath());

            var buildResult = vueProjectBuilder.buildProjectWithResult(projectDir.getAbsolutePath());

            if (buildResult.isSuccess()) {
                log.info("Vue项目构建成功: {}", projectDir.getAbsolutePath());
                return Flux.just("\n\n> ✅ **项目构建成功**\n\n");
            }

            String errorSummary = buildResult.getErrorSummary();
            log.warn("Vue项目构建失败 (第{}次): {}", attempt + 1, errorSummary);

            if (attempt >= MAX_BUILD_FIX_ATTEMPTS) {
                // 达到最大重试次数，先发送提示文本，再发出错误信号让外层 doOnError 正确处理状态
                String msg = "\n\n> ⚠️ **项目构建失败**，已尝试自动修复但未成功。部署时将重新尝试构建。\n\n> 错误摘要: " + truncate(errorSummary, 500) + "\n\n";
                return Flux.just(msg)
                        .concatWith(Flux.error(new BusinessException(ErrorCode.OPERATION_ERROR,
                                "Vue项目构建失败: " + truncate(errorSummary, 500))));
            }

            // 构建失败，让AI修复
            String fixMessage = String.format(
                    "项目构建失败（npm run build），请根据以下错误信息修复代码。只修改有问题的文件，不要重新创建所有文件。\n\n构建错误输出:\n```\n%s\n```",
                    truncate(errorSummary, 1500)
            );

            Flux<String> statusMessage = Flux.just("\n\n> ⚠️ **构建失败，正在自动修复...**\n\n");

            // 调用AI修复
            var aiService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, CodeGenTypeEnum.VUE_PROJECT);
            TokenStream fixTokenStream = aiService.generateVueProjectCodeStream(appId, fixMessage);
            Flux<String> fixResult = TokenStream2FluxAdaptor.adapt(fixTokenStream);
            Flux<String> fixStream = streamHandlerExecutor.doExecute(fixResult, chatHistoryService, appId, loginUser, CodeGenTypeEnum.VUE_PROJECT, version);

            // 修复完成后重新尝试构建
            Flux<String> retryBuildStream = Flux.defer(() -> attemptBuildAndFix(projectDir, appId, version, loginUser, attempt + 1));

            return Flux.concat(statusMessage, fixStream, retryBuildStream);
        });
    }

    /**
     * 截断字符串到指定最大长度
     */
    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() <= maxLen ? str : "..." + str.substring(str.length() - maxLen);
    }

    /**
     * 异步截图并保存封面
     * 使用指数退避重试：1s, 2s, 4s
     */
    private void captureAndSaveCoverAsync(Long appId, int version) {
        if (!screenshotEnabled) return;

        CompletableFuture.runAsync(() -> {
            String previewUrl = screenshotBaseUrl + "/api/static/preview/" + appId + "/" + version + "/";
            String coverDir = CodeFileConstant.CODE_FILE_PATH + File.separator + "covers";
            new File(coverDir).mkdirs();
            File coverFile = new File(coverDir, appId + ".png");

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    long delay = 1000L * (1L << (attempt - 1));
                    Thread.sleep(delay);
                    screenshotService.captureScreenshot(previewUrl, coverFile);

                    App updateApp = new App();
                    updateApp.setId(appId);
                    updateApp.setCover("/api/static/covers/" + appId + ".png");
                    updateApp.setUpdateTime(LocalDateTime.now());
                    appService.updateById(updateApp);

                    log.info("App cover captured: appId={}, version={}, attempt={}", appId, version, attempt);
                    return;
                } catch (Exception e) {
                    log.warn("Screenshot attempt {}/{} failed for app {}: {}", attempt, maxRetries, appId, e.getMessage());
                }
            }
            log.warn("All {} screenshot attempts failed for app {}", maxRetries, appId);
        });
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
        Flux<String> stream = result
                .doOnNext(sb::append)
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

                        // 异步截图保存封面
                        captureAndSaveCoverAsync(appId, version);
                    } else {
                        log.warn("代码解析失败或无有效代码：{}", parseResult.getParseError());
                    }

                    // 通过回调返回解析结果和版本号，避免重复解析
                    if (onCompleteCallback != null) {
                        onCompleteCallback.accept(parseResult, version);
                    }
                });

        // Post-stream security check: OutputGuardrails on AiServices buffer all streaming tokens
        // before validating, breaking real-time SSE. Instead, run CodeExfiltrationOutputGuardrail
        // here after the full response has already been streamed to the client.
        // If a violation is detected, an error frame is sent and the front-end discards the content.
        CodeExfiltrationOutputGuardrail exfiltrationChecker = new CodeExfiltrationOutputGuardrail();
        return stream.concatWith(Flux.defer(() -> {
            String violation = exfiltrationChecker.checkText(sb.toString());
            if (violation != null) {
                log.warn("[post-stream] 输出安全校验未通过 appId={}: {}", appId, violation);
                return Flux.error(new BusinessException(ErrorCode.OPERATION_ERROR, violation));
            }
            return Flux.empty();
        }));
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
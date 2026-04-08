package fun.javierchen.ainocodeapplication.exceptiom;


import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.guardrail.OutputGuardrailException;
import fun.javierchen.ainocodeapplication.common.BaseResponse;
import fun.javierchen.ainocodeapplication.common.ResponseCode;
import fun.javierchen.ainocodeapplication.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理程序
 *
 * @author JavierChen
 * @date 2024/10/31
 */
@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String GUARDRAIL_MSG_PREFIX = "failed with this message: ";

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 输入 Guardrail 拦截 → 业务错误（请求参数/内容违规，HTTP 语义属于客户端错误）
     */
    @ExceptionHandler(InputGuardrailException.class)
    public BaseResponse<?> inputGuardrailExceptionHandler(InputGuardrailException e) {
        String message = extractGuardrailMessage(e.getMessage());
        log.warn("InputGuardrailException blocked request: {}", message);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), message);
    }

    /**
     * 输出 Guardrail 拦截 → 操作失败（生成内容不安全，重试耗尽后仍不合规）
     */
    @ExceptionHandler(OutputGuardrailException.class)
    public BaseResponse<?> outputGuardrailExceptionHandler(OutputGuardrailException e) {
        String message = extractGuardrailMessage(e.getMessage());
        log.warn("OutputGuardrailException blocked response: {}", message);
        return ResultUtils.error(ErrorCode.OPERATION_ERROR.getCode(), message);
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    /**
     * 从框架 guardrail 异常消息中提取用户可见的业务消息。
     * 框架格式: "The guardrail <class> failed with this message: <业务消息>"
     */
    private String extractGuardrailMessage(String raw) {
        if (raw == null) return "请求内容未通过安全检查，请修改后重试";
        int idx = raw.indexOf(GUARDRAIL_MSG_PREFIX);
        if (idx >= 0) {
            return raw.substring(idx + GUARDRAIL_MSG_PREFIX.length()).trim();
        }
        return raw;
    }
}

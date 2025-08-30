package fun.javierchen.ainocodeapplication.common;

import lombok.Getter;

/**
 * 全局错误代码
 *
 * @author JavierChen
 * @date 2024/10/31
 */
@Getter
public enum ResponseCode {

    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "无数据", ""),
    NO_AUTH(40100, "无权限", ""),
    NO_LOGIN(40101, "未登录", ""),
    INVALID_OPERATION(40102, "操作无效", ""),
    FORBIDDEN(40301, "被禁止", ""),
    SYSTEM_BUSY(50010, "系统繁忙，稍后重试", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "");

    private final int code;
    private final String message;
    private final String description;

    ResponseCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

}

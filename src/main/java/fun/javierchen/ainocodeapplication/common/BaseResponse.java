package fun.javierchen.ainocodeapplication.common;

import fun.javierchen.ainocodeapplication.exceptiom.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应类
 *
 * @author JavierChen
 * @date 2024/10/31
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

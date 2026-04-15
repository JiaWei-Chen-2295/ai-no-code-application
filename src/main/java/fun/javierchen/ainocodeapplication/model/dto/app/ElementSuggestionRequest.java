package fun.javierchen.ainocodeapplication.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 元素修改建议请求
 *
 * @author JavierChen
 */
@Data
public class ElementSuggestionRequest implements Serializable {

    /**
     * 应用 ID
     */
    private Long appId;

    /**
     * 用户的修改指令
     */
    private String message;

    /**
     * 目标元素的 CSS 选择器
     */
    private String cssSelector;

    /**
     * 元素上下文（outerHTML 片段或组件文件路径），可选
     */
    private String elementContext;

    /**
     * 代码生成类型（枚举）
     */
    private String codeGenType;

    private static final long serialVersionUID = 1L;
}

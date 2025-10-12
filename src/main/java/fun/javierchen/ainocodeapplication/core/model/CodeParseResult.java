package fun.javierchen.ainocodeapplication.core.model;

import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 代码解析结果包装类
 * 封装解析结果和有效性判断，避免重复解析
 *
 * @author JavierChen
 */
@Getter
@Slf4j
@AllArgsConstructor
public class CodeParseResult {
    
    /**
     * 解析后的代码对象
     */
    private final Object parsedCode;
    
    /**
     * 是否包含有效代码
     */
    private final boolean hasValidCode;
    
    /**
     * 是否解析成功
     */
    private final boolean parseSuccess;
    
    /**
     * 解析错误信息（如果有）
     */
    private final String parseError;

    /**
     * 创建成功的解析结果
     */
    public static CodeParseResult success(Object parsedCode, boolean hasValidCode) {
        return new CodeParseResult(parsedCode, hasValidCode, true, null);
    }

    /**
     * 创建失败的解析结果
     */
    public static CodeParseResult failure(String error) {
        return new CodeParseResult(null, false, false, error);
    }

    /**
     * 判断HTML代码结果是否有效
     */
    public static boolean isValidHtmlCode(HtmlCodeResult htmlResult) {
        if (htmlResult == null) {
            return false;
        }
        
        String htmlCode = htmlResult.getHtmlCode();
        if (htmlCode == null || htmlCode.trim().isEmpty()) {
            return false;
        }
        
        // 简单的HTML有效性检查
        String trimmed = htmlCode.trim();
        return trimmed.contains("<") && trimmed.contains(">") && 
               (trimmed.toLowerCase().contains("<html") || 
                trimmed.toLowerCase().contains("<div") || 
                trimmed.toLowerCase().contains("<body") ||
                trimmed.toLowerCase().contains("<!doctype"));
    }

    /**
     * 判断多文件代码结果是否有效
     */
    public static boolean isValidMultiFileCode(MultiFileCodeResult multiResult) {
        if (multiResult == null) {
            return false;
        }
        
        // 至少需要有HTML代码或其他代码之一不为空
        boolean hasHtml = multiResult.getHtmlCode() != null && 
                         !multiResult.getHtmlCode().trim().isEmpty() &&
                         isValidCodeContent(multiResult.getHtmlCode(), "html");
        
        boolean hasCss = multiResult.getCssCode() != null && 
                        !multiResult.getCssCode().trim().isEmpty() &&
                        isValidCodeContent(multiResult.getCssCode(), "css");
        
        boolean hasJs = multiResult.getJsCode() != null && 
                       !multiResult.getJsCode().trim().isEmpty() &&
                       isValidCodeContent(multiResult.getJsCode(), "js");
        
        return hasHtml || hasCss || hasJs;
    }

    /**
     * 检查代码内容是否有效（不仅仅是非空）
     */
    private static boolean isValidCodeContent(String code, String type) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = code.trim().toLowerCase();
        
        return switch (type) {
            case "html" -> trimmed.contains("<") && trimmed.contains(">");
            case "css" -> trimmed.contains("{") && trimmed.contains("}") || 
                         trimmed.contains(":") && trimmed.contains(";");
            case "js" -> trimmed.contains("function") || trimmed.contains("var") || 
                        trimmed.contains("let") || trimmed.contains("const") ||
                        trimmed.contains("=>") || trimmed.contains("document");
            default -> true;
        };
    }
}

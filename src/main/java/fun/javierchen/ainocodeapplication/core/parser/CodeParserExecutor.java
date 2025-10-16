package fun.javierchen.ainocodeapplication.core.parser;

import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import fun.javierchen.ainocodeapplication.core.model.CodeParseResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodeParserExecutor {

    private static final CodeParser<HtmlCodeResult> HTML_CODE_PARSER = new SingleHTMLCodeParser();

    private static final CodeParser<MultiFileCodeResult> MUTI_FILE_CODE_PARSER = new MutiFileCodeParser();

    /**
     * 根据类型指定解析器（向后兼容的方法）
     * @param codeContent
     * @param codeGenTypeEnum
     * @return
     * @deprecated 使用 executeWithResult 替代
     */
    @Deprecated
    public static Object execute(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        CodeParseResult result = executeWithResult(codeContent, codeGenTypeEnum);
        return result.getParsedCode();
    }

    /**
     * 根据类型指定解析器，返回包装结果
     * @param codeContent 代码内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果包装对象
     */
    public static CodeParseResult executeWithResult(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeContent == null || codeContent.trim().isEmpty()) {
            return CodeParseResult.failure("代码内容为空");
        }
        
        try {
            Object parsedCode = switch (codeGenTypeEnum) {
                case HTML -> HTML_CODE_PARSER.parserCode(codeContent);
                case MUTI_FILE -> MUTI_FILE_CODE_PARSER.parserCode(codeContent);
                case VUE_PROJECT -> null;
            };
            
            // 判断解析结果是否包含有效代码
            boolean hasValidCode = isValidCode(parsedCode);
            
            return CodeParseResult.success(parsedCode, hasValidCode);
            
        } catch (Exception e) {
            log.warn("代码解析失败: {}", e.getMessage());
            return CodeParseResult.failure("代码解析失败: " + e.getMessage());
        }
    }

    /**
     * 判断解析结果是否包含有效代码
     */
    private static boolean isValidCode(Object parsedCode) {
        if (parsedCode instanceof HtmlCodeResult htmlResult) {
            return CodeParseResult.isValidHtmlCode(htmlResult);
        } else if (parsedCode instanceof MultiFileCodeResult multiResult) {
            return CodeParseResult.isValidMultiFileCode(multiResult);
        }
        return false;
    }
}

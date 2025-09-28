package fun.javierchen.ainocodeapplication.core.parser;

import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;

public class CodeParserExecutor {

    private static final CodeParser<HtmlCodeResult> HTML_CODE_PARSER = new SingleHTMLCodeParser();

    private static final CodeParser<MultiFileCodeResult> MUTI_FILE_CODE_PARSER = new MutiFileCodeParser();

    /**
     * 根据类型指定解析器
     * @param codeContent
     * @param codeGenTypeEnum
     * @return
     */
    public static Object execute(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_PARSER.parserCode(codeContent);
            case MUTI_FILE -> MUTI_FILE_CODE_PARSER.parserCode(codeContent);
        };
    }

}

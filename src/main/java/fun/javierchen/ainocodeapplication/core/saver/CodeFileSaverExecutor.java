package fun.javierchen.ainocodeapplication.core.saver;

import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;

import java.io.File;

public class CodeFileSaverExecutor {
    private static final CodeFileSaverTemplate<HtmlCodeResult> htmlCodeFileSaverTemplate =
            new SingleHTMLCodeFileSaverTemplate();

    private static final CodeFileSaverTemplate<MultiFileCodeResult> multiFileCodeFileSaverTemplate =
            new MutiFileCodeFileSaverTemplate();

    /**
     * 保存代码文件
     *
     * @param result
     * @param codeGenTypeEnum
     * @return
     */
    public static File saveCodeFile(Object result, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeFileSaverTemplate.saveCodeFile((HtmlCodeResult) result);
            case MUTI_FILE -> multiFileCodeFileSaverTemplate.saveCodeFile((MultiFileCodeResult) result);
        };
    }
}

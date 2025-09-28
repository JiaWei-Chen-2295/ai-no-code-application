package fun.javierchen.ainocodeapplication.core.saver;

import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;

/**
 * 保存单个的HTML代码
 */
public class SingleHTMLCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    void saveFiles(String uniDir, HtmlCodeResult result) {
        String description = result.getDescription();
        if (description != null) {
            writeFile(uniDir, "README.txt", description);
        }
    }
}

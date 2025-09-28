package fun.javierchen.ainocodeapplication.core.saver;

import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码保存模板实现
 */
public class MutiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.MUTI_FILE;
    }

    @Override
    void saveFiles(String uniDir, MultiFileCodeResult result) {
        String htmlCode = result.getHtmlCode();
        String cssCode = result.getCssCode();
        String jsCode = result.getJsCode();
        String description = result.getDescription();
        if (description != null) {
            writeFile(uniDir, "README.txt", description);
        }
        writeFile(uniDir, "index.html", htmlCode);
        writeFile(uniDir, "style.css", cssCode);
        writeFile(uniDir, "script.js", jsCode);
    }
}

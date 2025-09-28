package fun.javierchen.ainocodeapplication.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import fun.javierchen.ainocodeapplication.ai.model.HtmlCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.MultiFileCodeResult;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import fun.javierchen.ainocodeapplication.constant.CodeFileConstant;

import java.io.File;
import java.nio.charset.StandardCharsets;
@Deprecated
public class CodeFileSaver {
    /**
     * 保存多文件代码
     */
    public static File saveMutiFileCode(MultiFileCodeResult codeResult) {
        String htmlCode = codeResult.getHtmlCode();
        String cssCode = codeResult.getCssCode();
        String jsCode = codeResult.getJsCode();
        String description = codeResult.getDescription();
        String uniDir = createUniDir(CodeGenTypeEnum.MUTI_FILE);
        if (description != null) {
            writeFile(uniDir, "README.txt", description);
        }
        writeFile(uniDir, "index.html", htmlCode);
        writeFile(uniDir, "style.css", cssCode);
        writeFile(uniDir, "script.js", jsCode);
        return new File(uniDir);
    }

    /**
     * 保存单文件web代码
     *
     * @param result
     * @return
     */
    public static File saveSingletonHtmlCode(HtmlCodeResult result) {
        String uniDir = createUniDir(CodeGenTypeEnum.HTML);
        writeFile(uniDir, "index.html", result.getHtmlCode());
        String description = result.getDescription();
        if (description != null) {
            writeFile(uniDir, "README.txt", description);
        }
        writeFile(uniDir, "README.txt", description);
        return new File(uniDir);
    }


    private static String createUniDir(CodeGenTypeEnum codeGenType) {
        String snowflakeStr = IdUtil.getSnowflakeNextIdStr();
        String dirName = CodeFileConstant.CODE_FILE_PATH + File.separator + StrUtil.format("{}_{}", codeGenType.getType(), snowflakeStr);
        FileUtil.mkdir(dirName);
        return dirName;
    }

    /**
     * 将字符串写为文件
     *
     * @param fileName
     * @param content
     * @return
     */
    private static File writeFile(String path, String fileName, String content) {
        String filePath = path + File.separator + fileName;
        return FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }


}

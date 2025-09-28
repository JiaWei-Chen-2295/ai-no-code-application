package fun.javierchen.ainocodeapplication.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import fun.javierchen.ainocodeapplication.constant.CodeFileConstant;
import fun.javierchen.ainocodeapplication.exceptiom.BusinessException;
import fun.javierchen.ainocodeapplication.exceptiom.ErrorCode;

import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class CodeFileSaverTemplate<T> {
    /**
     * 保存代码文件
     * @param result
     * @return
     */
    public final File saveCodeFile(T result, Long appId) {
        // 验证输入参数
        validateInput(result);
        // 新建一个独一无二的目录
        String uniDir = createUniDir(appId);
        // 保存文件到这个目录
        saveFiles(uniDir, result);
        return new File(uniDir);
    }


    /**
     * 验证参数 可以重写
     * @param result
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成的内容为空");
        }
    }

    /**
     * 新建一个独一无二的目录
     */
    private String createUniDir() {
        CodeGenTypeEnum codeGenType = getCodeGenType();
        String snowflakeStr = IdUtil.getSnowflakeNextIdStr();
        String dirName = CodeFileConstant.CODE_FILE_PATH + File.separator + StrUtil.format("{}_{}", codeGenType.getType(), snowflakeStr);
        FileUtil.mkdir(dirName);
        return dirName;
    }

    /**
     * 新建一个独一无二的目录 通过 appId
     */
    private String createUniDir(Long appId) {
        CodeGenTypeEnum codeGenType = getCodeGenType();
        String dirName = CodeFileConstant.CODE_FILE_PATH + File.separator + StrUtil.format("{}_{}", codeGenType.getType(), appId);
        FileUtil.mkdir(dirName);
        return dirName;
    }

    /**
     * 获取代码的类型
     */
    abstract CodeGenTypeEnum getCodeGenType();

    /**
     * 自行实现文件保存逻辑
     * @param uniDir
     * @param result
     * @return
     */
    abstract void saveFiles(String uniDir, T result);


    /**
     * 将字符串写为文件
     *
     * @param fileName
     * @param content
     * @return
     */
    File writeFile(String path, String fileName, String content) {
        String filePath = path + File.separator + fileName;
        return FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }


}

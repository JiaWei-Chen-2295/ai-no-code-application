package fun.javierchen.ainocodeapplication.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import fun.javierchen.ainocodeapplication.constant.AppConstant;
import fun.javierchen.ainocodeapplication.security.DirectorySandboxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件写入工具
 * 支持 AI 通过工具调用的方式写入文件
 */
@Slf4j
@Component
public class FileWriteTool implements AiTool {

    @Tool("写入文件到指定路径")
    public String writeFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @P("要写入文件的内容")
            String content,
            @ToolMemoryId Long appId
    ) {
        try {
            // 使用沙箱安全检查验证路径
            Path safePath = DirectorySandboxUtil.validateAndSecurePath(appId, relativeFilePath);
            
            // 创建父目录（如果不存在）
            Path parentDir = safePath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            
            // 写入文件内容，使用UTF-8编码确保中文正确
            Files.write(safePath, content.getBytes("UTF-8"),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            
            log.info("安全写入文件: {} (沙箱内路径)", safePath.toAbsolutePath());
            return "文件写入成功: " + relativeFilePath;
            
        } catch (SecurityException e) {
            String errorMessage = "安全检查失败: " + e.getMessage();
            log.warn("文件写入被安全策略阻止: {}, 路径: {}", errorMessage, relativeFilePath);
            return errorMessage;
        } catch (IOException e) {
            String errorMessage = "文件写入失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        } catch (Exception e) {
            String errorMessage = "写入文件时发生未知错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}

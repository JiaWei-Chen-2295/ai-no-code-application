package fun.javierchen.ainocodeapplication.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import fun.javierchen.ainocodeapplication.security.DirectorySandboxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件存在检查工具
 * 让AI能够检查文件是否已存在，避免覆盖重要配置文件
 */
@Slf4j
@Component
public class FileExistsTool implements AiTool {

    @Tool("检查文件是否存在")
    public String checkFileExists(
            @P("要检查的文件相对路径")
            String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 使用沙箱安全检查验证路径（但不创建父目录）
            Path targetPath = DirectorySandboxUtil.getVueProjectWorkingDirectory(appId).resolve(relativeFilePath);
            
            // 标准化路径检查
            targetPath = targetPath.normalize();
            
            boolean exists = Files.exists(targetPath);
            boolean isFile = exists && Files.isRegularFile(targetPath);
            boolean isDirectory = exists && Files.isDirectory(targetPath);
            
            if (exists) {
                if (isFile) {
                    long size = Files.size(targetPath);
                    return String.format("文件存在: %s (大小: %d 字节)", relativeFilePath, size);
                } else if (isDirectory) {
                    return String.format("目录存在: %s", relativeFilePath);
                } else {
                    return String.format("路径存在但类型未知: %s", relativeFilePath);
                }
            } else {
                return String.format("文件不存在: %s", relativeFilePath);
            }
            
        } catch (SecurityException e) {
            String errorMessage = "安全检查失败: " + e.getMessage();
            log.warn("文件存在检查被安全策略阻止: {}, 路径: {}", errorMessage, relativeFilePath);
            return errorMessage;
        } catch (Exception e) {
            String errorMessage = "检查文件存在状态失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}

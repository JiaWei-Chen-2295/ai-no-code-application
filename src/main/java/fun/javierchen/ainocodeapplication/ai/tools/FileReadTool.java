package fun.javierchen.ainocodeapplication.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import fun.javierchen.ainocodeapplication.constant.AppConstant;
import fun.javierchen.ainocodeapplication.security.DirectorySandboxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件读取工具
 * 支持 AI 通过工具调用的方式读取文件内容，确保中文编码正确
 */
@Slf4j
@Component
public class FileReadTool implements AiTool {

    @Tool("读取指定路径的文件内容")
    public String readFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 使用沙箱安全检查验证路径
            Path safePath = DirectorySandboxUtil.validateAndSecurePath(appId, relativeFilePath);
            
            // 检查文件是否存在
            if (!Files.exists(safePath)) {
                return "文件不存在: " + relativeFilePath;
            }
            
            // 检查是否为文件（非目录）
            if (!Files.isRegularFile(safePath)) {
                return "路径不是文件: " + relativeFilePath;
            }
            
            // 读取文件内容，使用 UTF-8 编码确保中文正确显示
            String content = Files.readString(safePath, StandardCharsets.UTF_8);
            
            log.info("安全读取文件: {}, 内容长度: {} 字符", safePath.toAbsolutePath(), content.length());
            
            // 如果文件为空
            if (content.isEmpty()) {
                return "文件内容为空: " + relativeFilePath;
            }
            
            return content;
            
        } catch (SecurityException e) {
            String errorMessage = "安全检查失败: " + e.getMessage();
            log.warn("文件读取被安全策略阻止: {}, 路径: {}", errorMessage, relativeFilePath);
            return errorMessage;
        } catch (IOException e) {
            String errorMessage = "读取文件失败: " + relativeFilePath + ", 可能是编码问题或文件损坏";
            log.error(errorMessage, e);
            return errorMessage;
        } catch (Exception e) {
            String errorMessage = "读取文件时发生未知错误: " + relativeFilePath;
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}

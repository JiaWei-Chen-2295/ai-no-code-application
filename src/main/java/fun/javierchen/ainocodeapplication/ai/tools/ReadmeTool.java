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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * README 文档读取工具
 * 专门用于查找和读取项目中的 README 文件，支持多种命名格式
 */
@Slf4j
@Component
public class ReadmeTool implements AiTool {

    // 常见的 README 文件名格式
    private static final List<String> README_PATTERNS = Arrays.asList(
            "README.md", "readme.md", "Readme.md", "README.MD",
            "README.txt", "readme.txt", "Readme.txt", "README.TXT",
            "README.rst", "readme.rst", "Readme.rst", "README.RST",
            "README", "readme", "Readme",
            "说明.md", "说明.txt", "说明文档.md", "使用说明.md"
    );

    @Tool("查找并读取项目的 README 文档")
    public String findAndReadReadme(
            @P("搜索目录的相对路径，默认为根目录")
            String relativeDirectoryPath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 如果路径为空或null，使用根目录
            if (relativeDirectoryPath == null || relativeDirectoryPath.trim().isEmpty()) {
                relativeDirectoryPath = "";
            }
            
            // 使用沙箱安全检查验证路径
            Path searchPath = DirectorySandboxUtil.validateAndSecurePath(appId, relativeDirectoryPath);
            
            if (!Files.exists(searchPath)) {
                return "搜索目录不存在: " + relativeDirectoryPath;
            }
            
            if (!Files.isDirectory(searchPath)) {
                return "搜索路径不是目录: " + relativeDirectoryPath;
            }
            
            // 查找 README 文件
            Path readmeFile = findReadmeFile(searchPath);
            
            if (readmeFile == null) {
                return "未找到 README 文档，搜索目录: " + relativeDirectoryPath + 
                       "\n支持的文件名: " + String.join(", ", README_PATTERNS);
            }
            
            // 读取 README 内容
            String content = Files.readString(readmeFile, StandardCharsets.UTF_8);
            String fileName = readmeFile.getFileName().toString();
            String relativePath = searchPath.relativize(readmeFile).toString();
            
            StringBuilder result = new StringBuilder();
            result.append("📖 找到 README 文档: ").append(relativePath).append("\n");
            result.append("📄 文件大小: ").append(formatFileSize(Files.size(readmeFile))).append("\n");
            result.append("=" .repeat(50)).append("\n");
            result.append(content);
            
            log.info("安全读取 README 文件: {} (沙箱内)", readmeFile.toAbsolutePath());
            return result.toString();
            
        } catch (SecurityException e) {
            String errorMessage = "安全检查失败: " + e.getMessage();
            log.warn("README 读取被安全策略阻止: {}, 路径: {}", errorMessage, relativeDirectoryPath);
            return errorMessage;
        } catch (IOException e) {
            String errorMessage = "读取 README 文档失败: " + relativeDirectoryPath + ", 可能是编码问题";
            log.error(errorMessage, e);
            return errorMessage;
        } catch (Exception e) {
            String errorMessage = "查找 README 文档时发生未知错误: " + relativeDirectoryPath;
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Tool("列出项目中所有可能的 README 文档")
    public String listAllReadmeFiles(
            @P("搜索目录的相对路径，默认为根目录")
            String relativeDirectoryPath,
            @P("是否递归搜索子目录，默认true")
            boolean recursive,
            @ToolMemoryId Long appId
    ) {
        try {
            if (relativeDirectoryPath == null || relativeDirectoryPath.trim().isEmpty()) {
                relativeDirectoryPath = "";
            }
            
            // 使用沙箱安全检查验证路径
            Path searchPath = DirectorySandboxUtil.validateAndSecurePath(appId, relativeDirectoryPath);
            
            if (!Files.exists(searchPath)) {
                return "搜索目录不存在: " + relativeDirectoryPath;
            }
            
            if (!Files.isDirectory(searchPath)) {
                return "搜索路径不是目录: " + relativeDirectoryPath;
            }
            
            List<Path> readmeFiles = new ArrayList<>();
            
            // 根据是否递归选择不同的搜索方式
            if (recursive) {
                try (Stream<Path> paths = Files.walk(searchPath)) {
                    paths.filter(Files::isRegularFile)
                         .filter(this::isReadmeFile)
                         .forEach(readmeFiles::add);
                }
            } else {
                try (Stream<Path> paths = Files.list(searchPath)) {
                    paths.filter(Files::isRegularFile)
                         .filter(this::isReadmeFile)
                         .forEach(readmeFiles::add);
                }
            }
            
            if (readmeFiles.isEmpty()) {
                return "未找到任何 README 文档，搜索目录: " + relativeDirectoryPath + 
                       (recursive ? " (包含子目录)" : " (仅当前目录)");
            }
            
            StringBuilder result = new StringBuilder();
            result.append("📚 找到的 README 文档列表:\n");
            
            for (int i = 0; i < readmeFiles.size(); i++) {
                Path file = readmeFiles.get(i);
                String relativePath = searchPath.relativize(file).toString();
                long size = Files.size(file);
                
                result.append(String.format("%d. 📖 %s (%s)\n", 
                    i + 1, relativePath, formatFileSize(size)));
            }
            
            log.info("安全找到 {} 个 README 文件，搜索目录: {} (沙箱内)", readmeFiles.size(), searchPath.toAbsolutePath());
            return result.toString();
            
        } catch (SecurityException e) {
            String errorMessage = "安全检查失败: " + e.getMessage();
            log.warn("README 列表被安全策略阻止: {}, 路径: {}", errorMessage, relativeDirectoryPath);
            return errorMessage;
        } catch (Exception e) {
            String errorMessage = "列出 README 文档失败: " + relativeDirectoryPath;
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Tool("创建标准的 README.md 模板")
    public String createReadmeTemplate(
            @P("项目名称")
            String projectName,
            @P("项目描述")
            String projectDescription,
            @P("README 文件保存路径，默认为根目录的 README.md")
            String savePath,
            @ToolMemoryId Long appId
    ) {
        try {
            if (savePath == null || savePath.trim().isEmpty()) {
                savePath = "README.md";
            }
            
            // 使用沙箱安全检查验证路径
            Path safePath = DirectorySandboxUtil.validateAndSecurePath(appId, savePath);
            
            // 生成 README 模板内容
            String template = generateReadmeTemplate(projectName, projectDescription);
            
            // 创建父目录（如果不存在）
            Path parentDir = safePath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            
            // 写入模板内容
            Files.write(safePath, template.getBytes(StandardCharsets.UTF_8));
            
            log.info("安全创建 README 模板: {} (沙箱内)", safePath.toAbsolutePath());
            return "✅ README 模板创建成功: " + savePath + "\n内容预览:\n" + 
                   template.substring(0, Math.min(template.length(), 200)) + "...";
            
        } catch (SecurityException e) {
            String errorMessage = "安全检查失败: " + e.getMessage();
            log.warn("README 模板创建被安全策略阻止: {}, 路径: {}", errorMessage, savePath);
            return errorMessage;
        } catch (Exception e) {
            String errorMessage = "创建 README 模板失败: " + savePath;
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 查找 README 文件
     */
    private Path findReadmeFile(Path directory) throws IOException {
        try (Stream<Path> entries = Files.list(directory)) {
            return entries.filter(Files::isRegularFile)
                         .filter(this::isReadmeFile)
                         .findFirst()
                         .orElse(null);
        }
    }

    /**
     * 判断是否为 README 文件
     */
    private boolean isReadmeFile(Path file) {
        String fileName = file.getFileName().toString();
        return README_PATTERNS.stream()
                             .anyMatch(pattern -> pattern.equalsIgnoreCase(fileName));
    }

    /**
     * 生成 README 模板内容
     */
    private String generateReadmeTemplate(String projectName, String projectDescription) {
        StringBuilder template = new StringBuilder();
        
        template.append("# ").append(projectName).append("\n\n");
        template.append("## 项目描述\n\n");
        template.append(projectDescription).append("\n\n");
        
        template.append("## 功能特性\n\n");
        template.append("- 功能1\n");
        template.append("- 功能2\n");
        template.append("- 功能3\n\n");
        
        template.append("## 安装说明\n\n");
        template.append("```bash\n");
        template.append("# 克隆项目\n");
        template.append("git clone [项目地址]\n\n");
        template.append("# 进入项目目录\n");
        template.append("cd ").append(projectName.toLowerCase().replace(" ", "-")).append("\n\n");
        template.append("# 安装依赖\n");
        template.append("npm install\n");
        template.append("```\n\n");
        
        template.append("## 使用方法\n\n");
        template.append("```bash\n");
        template.append("# 启动开发服务器\n");
        template.append("npm run dev\n\n");
        template.append("# 构建生产版本\n");
        template.append("npm run build\n");
        template.append("```\n\n");
        
        template.append("## 项目结构\n\n");
        template.append("```\n");
        template.append("├── src/              # 源代码目录\n");
        template.append("├── public/           # 静态资源目录\n");
        template.append("├── docs/             # 文档目录\n");
        template.append("├── package.json      # 项目配置文件\n");
        template.append("└── README.md         # 项目说明文档\n");
        template.append("```\n\n");
        
        template.append("## 贡献指南\n\n");
        template.append("1. Fork 本项目\n");
        template.append("2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)\n");
        template.append("3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)\n");
        template.append("4. 推送到分支 (`git push origin feature/AmazingFeature`)\n");
        template.append("5. 开启 Pull Request\n\n");
        
        template.append("## 许可证\n\n");
        template.append("本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情\n\n");
        
        template.append("## 联系方式\n\n");
        template.append("- 作者: [你的名字]\n");
        template.append("- 邮箱: [你的邮箱]\n");
        template.append("- 项目链接: [项目地址]\n");
        
        return template.toString();
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        }
    }
}

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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件夹读取工具
 * 支持 AI 通过工具调用的方式读取文件夹结构和文件列表
 */
@Slf4j
@Component
public class FolderReadTool implements AiTool {

    /**
     * 对AI无用或会消耗大量上下文的目录/文件过滤列表
     */
    private static final List<String> FILTERED_DIRECTORIES = List.of(
            "node_modules",        // Node.js依赖包，内容巨大
            ".git",               // Git版本控制，对业务开发无用
            "dist",               // 构建产物
            "build",              // 构建产物  
            "target",             // Java构建产物
            ".idea",              // IntelliJ IDEA配置
            ".vscode",            // VS Code配置
            ".gradle",            // Gradle缓存
            ".maven",             // Maven缓存
            "coverage",           // 测试覆盖率报告
            ".nyc_output",        // 测试覆盖率缓存
            ".DS_Store",          // macOS系统文件
            "Thumbs.db",          // Windows系统文件
            "__pycache__",        // Python缓存
            ".pytest_cache",      // Pytest缓存
            ".cache"              // 通用缓存目录
    );

    private static final List<String> FILTERED_FILE_EXTENSIONS = List.of(
            ".log",               // 日志文件
            ".tmp",               // 临时文件
            ".temp",              // 临时文件
            ".bak",               // 备份文件
            ".swp",               // Vim交换文件
            ".lock"               // 锁文件
    );

    /**
     * 检查文件或目录是否应该被过滤掉
     */
    private boolean shouldFilter(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        
        // 过滤目录
        if (Files.isDirectory(path)) {
            return FILTERED_DIRECTORIES.stream().anyMatch(name::equals);
        }
        
        // 过滤文件扩展名
        return FILTERED_FILE_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    @Tool("列出指定目录下的所有文件和子目录")
    public String listDirectory(
            @P("目录的相对路径")
            String relativeDirectoryPath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 使用沙箱安全检查验证路径
            Path safePath = DirectorySandboxUtil.validateAndSecurePath(appId, relativeDirectoryPath);
            
            // 检查目录是否存在
            if (!Files.exists(safePath)) {
                return "目录不存在: " + relativeDirectoryPath;
            }
            
            // 检查是否为目录
            if (!Files.isDirectory(safePath)) {
                return "路径不是目录: " + relativeDirectoryPath;
            }
            
            // 列出目录内容
            try (Stream<Path> entries = Files.list(safePath)) {
                List<String> fileList = entries
                        .filter(entry -> !shouldFilter(entry))  // 过滤不需要的文件和目录
                        .map(entry -> {
                            String name = entry.getFileName().toString();
                            if (Files.isDirectory(entry)) {
                                return "📁 " + name + "/";
                            } else {
                                try {
                                    long size = Files.size(entry);
                                    return "📄 " + name + " (" + formatFileSize(size) + ")";
                                } catch (IOException e) {
                                    return "📄 " + name + " (大小未知)";
                                }
                            }
                        })
                        .sorted()
                        .collect(Collectors.toList());
                
                // 统计被过滤的项目数量
                long filteredCount = 0;
                try (Stream<Path> allEntries = Files.list(safePath)) {
                    filteredCount = allEntries.filter(this::shouldFilter).count();
                }
                
                if (fileList.isEmpty()) {
                    return "目录为空: " + relativeDirectoryPath;
                }
                
                StringBuilder result = new StringBuilder();
                result.append("目录内容 (").append(relativeDirectoryPath).append("):\n");
                for (String item : fileList) {
                    result.append(item).append("\n");
                }
                
                // 显示过滤信息
                if (filteredCount > 0) {
                    result.append("\n🚫 已过滤 ").append(filteredCount)
                          .append(" 个项目 (node_modules、构建产物、缓存等)\n");
                }
                
                log.info("安全列出目录: {}, 显示 {} 个项目, 过滤 {} 个 (沙箱内)", 
                        safePath.toAbsolutePath(), fileList.size(), filteredCount);
                return result.toString();
            }
            
        } catch (SecurityException e) {
            String errorMessage = "安全检查失败: " + e.getMessage();
            log.warn("目录读取被安全策略阻止: {}, 路径: {}", errorMessage, relativeDirectoryPath);
            return errorMessage;
        } catch (IOException e) {
            String errorMessage = "读取目录失败: " + relativeDirectoryPath + ", 可能权限不足或目录损坏";
            log.error(errorMessage, e);
            return errorMessage;
        } catch (Exception e) {
            String errorMessage = "读取目录时发生未知错误: " + relativeDirectoryPath;
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Tool("递归列出目录结构（树形展示）")
    public String listDirectoryTree(
            @P("目录的相对路径")
            String relativeDirectoryPath,
            @P("最大递归深度（1-5，默认2）")
            int maxDepth,
            @ToolMemoryId Long appId
    ) {
        try {
            // 限制递归深度
            maxDepth = Math.max(1, Math.min(5, maxDepth));
            
            // 使用沙箱安全检查验证路径
            Path safePath = DirectorySandboxUtil.validateAndSecurePath(appId, relativeDirectoryPath);
            
            if (!Files.exists(safePath)) {
                return "目录不存在: " + relativeDirectoryPath;
            }
            
            if (!Files.isDirectory(safePath)) {
                return "路径不是目录: " + relativeDirectoryPath;
            }
            
            StringBuilder result = new StringBuilder();
            result.append("目录树结构 (").append(relativeDirectoryPath).append("):\n");
            buildDirectoryTree(safePath, result, "", maxDepth, 0);
            
            log.info("安全生成目录树: {}, 最大深度: {} (沙箱内)", safePath.toAbsolutePath(), maxDepth);
            return result.toString();
            
        } catch (SecurityException e) {
            String errorMessage = "安全检查失败: " + e.getMessage();
            log.warn("目录树生成被安全策略阻止: {}, 路径: {}", errorMessage, relativeDirectoryPath);
            return errorMessage;
        } catch (Exception e) {
            String errorMessage = "生成目录树失败: " + relativeDirectoryPath;
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Tool("统计目录信息")
    public String getDirectoryStatistics(
            @P("目录的相对路径")
            String relativeDirectoryPath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 使用沙箱安全检查验证路径
            Path safePath = DirectorySandboxUtil.validateAndSecurePath(appId, relativeDirectoryPath);
            
            if (!Files.exists(safePath)) {
                return "目录不存在: " + relativeDirectoryPath;
            }
            
            if (!Files.isDirectory(safePath)) {
                return "路径不是目录: " + relativeDirectoryPath;
            }
            
            int[] stats = new int[]{0, 0}; // [文件数, 目录数]
            int[] filteredStats = new int[]{0, 0}; // [过滤的文件数, 过滤的目录数]
            long[] totalSize = new long[]{0};
            
            try (Stream<Path> entries = Files.walk(safePath)) {
                entries.forEach(entry -> {
                    if (!entry.equals(safePath)) { // 排除根目录本身
                        if (shouldFilter(entry)) {
                            // 统计被过滤的项目
                            if (Files.isDirectory(entry)) {
                                filteredStats[1]++;
                            } else if (Files.isRegularFile(entry)) {
                                filteredStats[0]++;
                            }
                        } else {
                            // 统计有效的项目
                            if (Files.isDirectory(entry)) {
                                stats[1]++;
                            } else if (Files.isRegularFile(entry)) {
                                stats[0]++;
                                try {
                                    totalSize[0] += Files.size(entry);
                                } catch (IOException ignored) {
                                    // 忽略单个文件大小获取失败
                                }
                            }
                        }
                    }
                });
            }
            
            StringBuilder result = new StringBuilder();
            result.append("目录统计信息 (").append(relativeDirectoryPath).append("):\n");
            result.append("📄 文件数量: ").append(stats[0]).append("\n");
            result.append("📁 子目录数量: ").append(stats[1]).append("\n");
            result.append("💾 总大小: ").append(formatFileSize(totalSize[0])).append("\n");
            
            // 显示过滤统计
            if (filteredStats[0] > 0 || filteredStats[1] > 0) {
                result.append("\n🚫 已过滤项目:\n");
                if (filteredStats[0] > 0) {
                    result.append("   📄 过滤文件: ").append(filteredStats[0]).append("\n");
                }
                if (filteredStats[1] > 0) {
                    result.append("   📁 过滤目录: ").append(filteredStats[1]).append("\n");
                }
                result.append("   (包括: node_modules、构建产物、缓存、IDE配置等)\n");
            }
            
            log.info("安全统计目录: {}, 显示文件: {}, 目录: {}, 过滤文件: {}, 过滤目录: {} (沙箱内)", 
                    safePath.toAbsolutePath(), stats[0], stats[1], filteredStats[0], filteredStats[1]);
            return result.toString();
            
        } catch (SecurityException e) {
            String errorMessage = "安全检查失败: " + e.getMessage();
            log.warn("目录统计被安全策略阻止: {}, 路径: {}", errorMessage, relativeDirectoryPath);
            return errorMessage;
        } catch (Exception e) {
            String errorMessage = "统计目录信息失败: " + relativeDirectoryPath;
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 递归构建目录树
     */
    private void buildDirectoryTree(Path dir, StringBuilder result, String prefix, int maxDepth, int currentDepth) 
            throws IOException {
        if (currentDepth >= maxDepth) {
            return;
        }
        
        try (Stream<Path> entries = Files.list(dir)) {
            List<Path> sortedEntries = entries
                    .filter(entry -> !shouldFilter(entry))  // 过滤不需要的文件和目录
                    .sorted()
                    .collect(Collectors.toList());
            
            // 统计被过滤的项目
            long filteredCount = 0;
            try (Stream<Path> allEntries = Files.list(dir)) {
                filteredCount = allEntries.filter(this::shouldFilter).count();
            }
            
            for (int i = 0; i < sortedEntries.size(); i++) {
                Path entry = sortedEntries.get(i);
                boolean isLast = (i == sortedEntries.size() - 1);
                String name = entry.getFileName().toString();
                
                String connector = isLast ? "└── " : "├── ";
                result.append(prefix).append(connector);
                
                if (Files.isDirectory(entry)) {
                    result.append("📁 ").append(name).append("/\n");
                    String newPrefix = prefix + (isLast ? "    " : "│   ");
                    buildDirectoryTree(entry, result, newPrefix, maxDepth, currentDepth + 1);
                } else {
                    try {
                        long size = Files.size(entry);
                        result.append("📄 ").append(name).append(" (").append(formatFileSize(size)).append(")\n");
                    } catch (IOException e) {
                        result.append("📄 ").append(name).append("\n");
                    }
                }
            }
            
            // 在当前目录显示过滤信息
            if (filteredCount > 0 && currentDepth == 0) {
                result.append(prefix).append("🚫 (已过滤 ").append(filteredCount).append(" 个项目)\n");
            }
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}

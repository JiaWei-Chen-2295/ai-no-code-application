package fun.javierchen.ainocodeapplication.security;

import fun.javierchen.ainocodeapplication.constant.AppConstant;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 目录沙箱安全工具类
 * 确保AI工具只能访问指定的安全目录范围
 */
@Slf4j
public class DirectorySandboxUtil {

    /**
     * 允许访问的根目录列表
     */
    private static final List<String> ALLOWED_ROOT_DIRS = List.of(
            AppConstant.CODE_OUTPUT_ROOT_DIR,
            AppConstant.CODE_DEPLOY_ROOT_DIR
    );

    /**
     * 禁止访问的目录模式
     */
    private static final List<String> FORBIDDEN_PATTERNS = List.of(
            "system32", "windows", "program files", "users", 
            "etc", "usr", "var", "bin", "sbin", "root",
            ".git", ".ssh", ".env", "node_modules"
    );

    /**
     * 危险文件扩展名
     */
    private static final List<String> DANGEROUS_EXTENSIONS = List.of(
            ".exe", ".bat", ".cmd", ".ps1", ".sh", ".dll", ".so", ".app"
    );

    /**
     * 为指定appId创建安全的项目沙箱目录
     *
     * @param appId 应用ID
     * @return 安全的项目根目录路径
     */
    public static Path createSafeProjectDirectory(Long appId) {
        if (appId == null || appId <= 0) {
            throw new SecurityException("无效的应用ID: " + appId);
        }

        String projectDirName = "vue_project_" + appId;
        Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
        
        try {
            // 创建目录（如果不存在）
            Files.createDirectories(projectRoot);
            log.info("创建安全项目目录: {}", projectRoot.toAbsolutePath());
            return projectRoot;
        } catch (IOException e) {
            throw new SecurityException("创建项目目录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证并安全化文件路径
     *
     * @param appId 应用ID
     * @param relativeFilePath 相对文件路径
     * @return 验证后的安全路径
     */
    public static Path validateAndSecurePath(Long appId, String relativeFilePath) {
        if (relativeFilePath == null || relativeFilePath.trim().isEmpty()) {
            throw new SecurityException("文件路径不能为空");
        }

        // 标准化路径，去除危险字符
        String normalizedPath = normalizePath(relativeFilePath);
        
        // 检查路径安全性
        checkPathSecurity(normalizedPath);
        
        // 获取项目沙箱目录 - 现在使用版本化的Vue项目目录
        Path projectRoot = getVueProjectWorkingDirectory(appId);
        
        // 解析相对路径
        Path targetPath = projectRoot.resolve(normalizedPath);
        
        // 验证最终路径在沙箱内
        validatePathInSandbox(targetPath, projectRoot);
        
        log.debug("路径验证通过: {} -> {}", relativeFilePath, targetPath.toAbsolutePath());
        return targetPath;
    }

    /**
     * 获取Vue项目的当前工作目录（最新版本的项目目录）
     * 这个方法专门为AI工具服务，确保AI在正确的项目目录中工作
     */
    public static Path getVueProjectWorkingDirectory(Long appId) {
        if (appId == null || appId <= 0) {
            throw new SecurityException("无效的应用ID: " + appId);
        }
        
        // 获取应用的沙箱根目录
        Path appSandboxRoot = getProjectDirectoryPath(appId);
        
        try {
            // 如果目录不存在，返回基础目录（兼容旧逻辑）
            if (!Files.exists(appSandboxRoot)) {
                return createSafeProjectDirectory(appId);
            }
            
            // 查找最新的版本目录
            int latestVersion = Files.list(appSandboxRoot)
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.startsWith("v") && name.length() > 1)
                    .mapToInt(name -> {
                        try {
                            return Integer.parseInt(name.substring(1));
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(0);
            
            if (latestVersion > 0) {
                // 返回最新版本的Vue项目目录
                Path vueProjectDir = appSandboxRoot.resolve("v" + latestVersion).resolve("vue-project");
                log.debug("AI工作目录: {} (版本: {})", vueProjectDir.toAbsolutePath(), latestVersion);
                return vueProjectDir;
            } else {
                // 没有版本目录，返回基础目录（兼容旧逻辑）
                return createSafeProjectDirectory(appId);
            }
            
        } catch (IOException e) {
            log.warn("获取Vue项目工作目录失败，使用基础目录: {}", e.getMessage());
            return createSafeProjectDirectory(appId);
        }
    }

    /**
     * 标准化路径，移除危险字符
     */
    private static String normalizePath(String path) {
        return path
                // 移除危险的路径遍历字符
                .replaceAll("\\.\\./", "")
                .replaceAll("\\.\\.", "")
                // 统一路径分隔符
                .replace("\\", "/")
                // 去除开头的斜杠
                .replaceAll("^/+", "")
                // 去除多余的斜杠
                .replaceAll("/+", "/")
                .trim();
    }

    /**
     * 检查路径安全性
     */
    private static void checkPathSecurity(String path) {
        String lowerPath = path.toLowerCase();
        
        // 检查绝对路径
        if (lowerPath.startsWith("/") || lowerPath.matches("^[a-z]:.*")) {
            throw new SecurityException("不允许使用绝对路径: " + path);
        }
        
        // 检查路径遍历
        if (lowerPath.contains("../") || lowerPath.contains("..\\")) {
            throw new SecurityException("检测到路径遍历攻击: " + path);
        }
        
        // 检查禁止的目录模式
        for (String forbidden : FORBIDDEN_PATTERNS) {
            if (lowerPath.contains(forbidden)) {
                throw new SecurityException("路径包含禁止访问的目录: " + forbidden);
            }
        }
        
        // 检查危险文件扩展名
        for (String ext : DANGEROUS_EXTENSIONS) {
            if (lowerPath.endsWith(ext)) {
                throw new SecurityException("禁止访问的文件类型: " + ext);
            }
        }
    }

    /**
     * 验证路径是否在沙箱内
     */
    private static void validatePathInSandbox(Path targetPath, Path sandboxRoot) {
        try {
            Path normalizedTarget = targetPath.toRealPath();
            Path normalizedSandbox = sandboxRoot.toRealPath();
            
            if (!normalizedTarget.startsWith(normalizedSandbox)) {
                throw new SecurityException("路径超出沙箱范围: " + targetPath);
            }
        } catch (IOException e) {
            // 如果文件不存在，检查父目录
            Path parent = targetPath.getParent();
            if (parent != null) {
                try {
                    Path normalizedParent = parent.toAbsolutePath().normalize();
                    Path normalizedSandbox = sandboxRoot.toAbsolutePath().normalize();
                    
                    if (!normalizedParent.startsWith(normalizedSandbox)) {
                        throw new SecurityException("路径超出沙箱范围: " + targetPath);
                    }
                } catch (Exception ex) {
                    throw new SecurityException("路径验证失败: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 检查目录是否在允许的根目录下
     */
    public static boolean isAllowedRootDirectory(Path path) {
        try {
            Path normalizedPath = path.toAbsolutePath().normalize();
            for (String allowedRoot : ALLOWED_ROOT_DIRS) {
                Path allowedPath = Paths.get(allowedRoot).toAbsolutePath().normalize();
                if (normalizedPath.startsWith(allowedPath)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("检查根目录权限失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取应用的安全项目目录路径（不创建）
     */
    public static Path getProjectDirectoryPath(Long appId) {
        if (appId == null || appId <= 0) {
            throw new SecurityException("无效的应用ID: " + appId);
        }
        
        String projectDirName = "vue_project_" + appId;
        return Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
    }

    /**
     * 验证文件名安全性
     */
    public static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new SecurityException("文件名不能为空");
        }
        
        String normalized = fileName.trim();
        
        // 检查文件名长度
        if (normalized.length() > 255) {
            throw new SecurityException("文件名过长");
        }
        
        // 检查非法字符
        if (normalized.matches(".*[<>:\"|?*].*")) {
            throw new SecurityException("文件名包含非法字符: " + fileName);
        }
        
        // 检查保留名称
        List<String> reservedNames = List.of("CON", "PRN", "AUX", "NUL", "COM1", "COM2", "LPT1", "LPT2");
        if (reservedNames.contains(normalized.toUpperCase())) {
            throw new SecurityException("文件名为系统保留名称: " + fileName);
        }
    }
}

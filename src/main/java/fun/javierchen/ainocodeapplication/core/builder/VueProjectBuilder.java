package fun.javierchen.ainocodeapplication.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

/**
 * 构建 Vue 项目
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 异步构建 Vue 项目
     *
     * @param projectPath
     */
    public void buildProjectAsync(String projectPath) {
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis())
                .start(() -> {
                    try {
                        buildProject(projectPath);
                    } catch (Exception e) {
                        log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
                    }
                });
    }

    /**
     * 构建 Vue 项目
     *
     * @param projectPath 项目根目录路径
     * @return 是否构建成功
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在：{}", projectPath);
            return false;
        }
        // 检查是否有 package.json 文件
        File packageJsonFile = new File(projectDir, "package.json");
        if (!packageJsonFile.exists()) {
            log.error("项目目录中没有 package.json 文件：{}", projectPath);
            return false;
        }
        log.info("开始构建 Vue 项目：{}", projectPath);
        // 执行 npm install
//        if (!executeNpmInstall(projectDir)) {
//            log.error("npm install 执行失败：{}", projectPath);
//            return false;
//        }
        // 执行 npm run build
        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build 执行失败：{}", projectPath);
            return false;
        }
        // 验证 dist 目录是否生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("构建完成但 dist 目录未生成：{}", projectPath);
            return false;
        }
        
        // 修复生成的HTML文件中的资源路径
        // 注意：新的Vite配置已禁用代码分割，减少了JS路径问题
        if (!fixHtmlResourcePaths(distDir)) {
            log.warn("修复HTML资源路径失败，但不影响构建结果");
        }
        
        // 仍然修复JS文件路径以确保兼容性
        if (!fixJavaScriptResourcePaths(distDir)) {
            log.warn("修复JavaScript资源路径失败，但不影响构建结果");
        }
        
        log.info("Vue 项目构建成功，dist 目录：{}", projectPath);
        return true;
    }

    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300); // 5分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }

    /**
     * 根据操作系统构造命令
     *
     * @param baseCommand
     * @return
     */
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 操作系统检测
     *
     * @return
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 执行命令
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }
    
    /**
     * 修复HTML文件中的资源路径
     * 将绝对路径转换为相对路径，并添加版本兼容处理
     * 
     * @param distDir 构建输出目录
     * @return 是否修复成功
     */
    private boolean fixHtmlResourcePaths(File distDir) {
        try {
            File indexFile = new File(distDir, "index.html");
            if (!indexFile.exists()) {
                log.warn("index.html 文件不存在：{}", indexFile.getAbsolutePath());
                return false;
            }
            
            // 读取原始HTML内容
            String originalContent = Files.readString(indexFile.toPath(), StandardCharsets.UTF_8);
            log.debug("原始HTML内容长度：{}", originalContent.length());
            
            // 修复资源路径：将绝对路径转换为相对路径
            String fixedContent = originalContent
                    // 修复JavaScript文件路径
                    .replaceAll("src=\"/assets/", "src=\"./assets/")
                    // 修复CSS文件路径
                    .replaceAll("href=\"/assets/", "href=\"./assets/")
                    // 修复favicon路径
                    .replaceAll("href=\"/favicon\\.ico\"", "href=\"./favicon.ico\"")
                    // 修复其他可能的绝对路径资源
                    .replaceAll("src=\"/([^\"]+)\"", "src=\"./$1\"")
                    .replaceAll("href=\"/([^\"]+\\.(css|js|ico|png|jpg|svg))\"", "href=\"./$1\"");
            
            // 添加base标签来确保相对路径正确解析（可选的额外保护）
            if (!fixedContent.contains("<base")) {
                // 在head标签后添加base标签的注释说明，但不实际添加以避免影响Vue Router
                fixedContent = fixedContent.replaceFirst(
                    "(<head[^>]*>)", 
                    "$1\n    <!-- Base URL will be handled by preview controller redirection -->");
            }
            
            // 检查是否有修改
            if (!originalContent.equals(fixedContent)) {
                // 写入修复后的内容
                Files.writeString(indexFile.toPath(), fixedContent, StandardCharsets.UTF_8);
                log.info("成功修复HTML资源路径：{}", indexFile.getAbsolutePath());
                log.debug("修复后的内容长度：{}", fixedContent.length());
                return true;
            } else {
                log.debug("HTML文件无需修复：{}", indexFile.getAbsolutePath());
                return true;
            }
            
        } catch (IOException e) {
            log.error("修复HTML资源路径时发生IO异常：{}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("修复HTML资源路径时发生未知异常：{}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 修复JavaScript文件中的资源路径
     * 处理代码分割产生的动态导入路径
     * 
     * @param distDir 构建输出目录
     * @return 是否修复成功
     */
    private boolean fixJavaScriptResourcePaths(File distDir) {
        try {
            File assetsDir = new File(distDir, "assets");
            if (!assetsDir.exists() || !assetsDir.isDirectory()) {
                log.debug("assets 目录不存在，跳过JS路径修复：{}", assetsDir.getAbsolutePath());
                return true;
            }
            
            File[] jsFiles = assetsDir.listFiles((dir, name) -> name.endsWith(".js"));
            if (jsFiles == null || jsFiles.length == 0) {
                log.debug("未找到需要修复的JS文件");
                return true;
            }
            
            int fixedCount = 0;
            for (File jsFile : jsFiles) {
                if (fixSingleJavaScriptFile(jsFile)) {
                    fixedCount++;
                }
            }
            
            log.info("成功修复 {} 个JavaScript文件的资源路径", fixedCount);
            return true;
            
        } catch (Exception e) {
            log.error("修复JavaScript资源路径时发生异常：{}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 修复单个JavaScript文件中的资源路径
     * 
     * @param jsFile JS文件
     * @return 是否修复成功
     */
    private boolean fixSingleJavaScriptFile(File jsFile) {
        try {
            // 读取原始JS内容
            String originalContent = Files.readString(jsFile.toPath(), StandardCharsets.UTF_8);
            log.debug("处理JS文件：{}, 内容长度：{}", jsFile.getName(), originalContent.length());
            
            // 修复JavaScript中的资源路径
            String fixedContent = originalContent
                    // 修复 __vite__mapDeps 中的静态资源路径 - 更精确的匹配
                    .replaceAll("\"assets/([^\"]+\\.(js|css|png|jpg|svg|woff|woff2|ttf))\"", "\"./assets/$1\"")
                    // 修复单引号包围的资源路径
                    .replaceAll("'assets/([^']+\\.(js|css|png|jpg|svg|woff|woff2|ttf))'", "'./assets/$1'")
                    // 修复可能的动态import路径
                    .replaceAll("import\\s*\\(\\s*['\"]assets/([^'\"]+)['\"]", "import(\"./assets/$1\"")
                    // 修复其他可能的绝对路径引用
                    .replaceAll("([^.])/assets/([^'\"\\s)]+)", "$1./assets/$2");
            
            // 检查是否有修改
            if (!originalContent.equals(fixedContent)) {
                // 写入修复后的内容
                Files.writeString(jsFile.toPath(), fixedContent, StandardCharsets.UTF_8);
                log.debug("修复JS文件资源路径：{}", jsFile.getName());
                return true;
            } else {
                log.debug("JS文件无需修复：{}", jsFile.getName());
                return true;
            }
            
        } catch (Exception e) {
            log.error("修复单个JavaScript文件失败：{}, 错误：{}", jsFile.getName(), e.getMessage());
            return false;
        }
    }

}
package fun.javierchen.ainocodeapplication.core.builder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
     * 构建结果
     */
    @Getter
    public static class BuildResult {
        private final boolean success;
        private final String errorOutput;
        private final int exitCode;

        public BuildResult(boolean success, String errorOutput, int exitCode) {
            this.success = success;
            this.errorOutput = errorOutput;
            this.exitCode = exitCode;
        }

        /**
         * 获取适合展示给AI的简洁错误摘要（最多2000字符）
         */
        public String getErrorSummary() {
            if (success || errorOutput == null || errorOutput.isBlank()) {
                return "";
            }
            String output = errorOutput.strip();
            if (output.length() > 2000) {
                output = output.substring(output.length() - 2000);
            }
            return output;
        }
    }

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
        return buildProjectWithResult(projectPath).isSuccess();
    }

    /**
     * 构建 Vue 项目并返回详细结果
     *
     * @param projectPath 项目根目录路径
     * @return 构建结果，包含成功/失败状态和错误输出
     */
    public BuildResult buildProjectWithResult(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在：{}", projectPath);
            return new BuildResult(false, "项目目录不存在: " + projectPath, -1);
        }
        // 检查是否有 package.json 文件
        File packageJsonFile = new File(projectDir, "package.json");
        if (!packageJsonFile.exists()) {
            log.error("项目目录中没有 package.json 文件：{}", projectPath);
            return new BuildResult(false, "项目目录中没有 package.json 文件", -1);
        }
        log.info("开始构建 Vue 项目：{}", projectPath);
        
        // 执行 npm run build
        BuildResult buildResult = executeNpmBuildWithResult(projectDir);
        if (!buildResult.isSuccess()) {
            log.error("npm run build 执行失败：{}，错误输出：{}", projectPath, buildResult.getErrorSummary());
            return buildResult;
        }
        
        // 验证 dist 目录是否生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("构建完成但 dist 目录未生成：{}", projectPath);
            return new BuildResult(false, "构建命令执行成功但dist目录未生成", 0);
        }
        
        // 修复生成的HTML文件中的资源路径
        if (!fixHtmlResourcePaths(distDir)) {
            log.warn("修复HTML资源路径失败，但不影响构建结果");
        }
        
        // 修复JS文件路径
        if (!fixJavaScriptResourcePaths(distDir)) {
            log.warn("修复JavaScript资源路径失败，但不影响构建结果");
        }
        
        log.info("Vue 项目构建成功，dist 目录：{}", projectPath);
        return new BuildResult(true, null, 0);
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
     * 执行 npm run build 命令并返回详细结果
     */
    private BuildResult executeNpmBuildWithResult(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommandWithResult(projectDir, command, 180); // 3分钟超时
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
        return executeCommandWithResult(workingDir, command, timeoutSeconds).isSuccess();
    }

    /**
     * 执行命令并捕获输出
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 构建结果，包含成功/失败状态和错误输出
     */
    private BuildResult executeCommandWithResult(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            
            ProcessBuilder pb = new ProcessBuilder(command.split("\\s+"));
            pb.directory(workingDir);
            pb.redirectErrorStream(true); // 合并stdout和stderr
            
            Process process = pb.start();
            
            // 在后台线程中读取输出，避免缓冲区满导致进程阻塞
            StringBuilder outputBuilder = new StringBuilder();
            Thread outputReader = Thread.ofVirtual().start(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputBuilder.append(line).append("\n");
                    }
                } catch (IOException e) {
                    log.warn("读取命令输出失败: {}", e.getMessage());
                }
            });
            
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return new BuildResult(false, "构建超时（" + timeoutSeconds + "秒）", -1);
            }
            
            // 等待输出读取完成
            outputReader.join(5000);
            
            int exitCode = process.exitValue();
            String output = outputBuilder.toString();
            
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return new BuildResult(true, null, 0);
            } else {
                log.error("命令执行失败，退出码: {}，输出:\n{}", exitCode, output);
                return new BuildResult(false, output, exitCode);
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return new BuildResult(false, "执行命令异常: " + e.getMessage(), -1);
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
package fun.javierchen.ainocodeapplication.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 模板项目复制工具类
 * 支持保留符号链接、跳过指定目录、灵活配置
 * 
 * @author javierchen
 */
public class TemplateCopyUtil {
    
    /**
     * 默认跳过的目录
     */
    private static final Set<String> DEFAULT_SKIP_DIRS = new HashSet<>();
    
    static {
        DEFAULT_SKIP_DIRS.add(".git");
        DEFAULT_SKIP_DIRS.add(".idea");
        DEFAULT_SKIP_DIRS.add(".vscode");
        DEFAULT_SKIP_DIRS.add("dist");
        DEFAULT_SKIP_DIRS.add("build");
        DEFAULT_SKIP_DIRS.add("target");
        DEFAULT_SKIP_DIRS.add(".DS_Store");
        DEFAULT_SKIP_DIRS.add("Thumbs.db");
    }
    
    /**
     * 复制配置类
     */
    public static class CopyConfig {
        private boolean preserveSymlinks = true;
        private boolean skipNodeModules = false;
        private Set<String> skipDirectories = new HashSet<>(DEFAULT_SKIP_DIRS);
        private Set<String> skipFileExtensions = new HashSet<>();
        private Predicate<Path> customFilter = null;
        private boolean createParentDirs = true;
        private boolean replaceExisting = true;
        private boolean verbose = false;
        
        public CopyConfig preserveSymlinks(boolean preserve) {
            this.preserveSymlinks = preserve;
            return this;
        }
        
        public CopyConfig skipNodeModules(boolean skip) {
            this.skipNodeModules = skip;
            if (skip) {
                skipDirectories.add("node_modules");
            } else {
                skipDirectories.remove("node_modules");
            }
            return this;
        }
        
        public CopyConfig skipDirectory(String dirName) {
            skipDirectories.add(dirName);
            return this;
        }
        
        public CopyConfig skipDirectories(Set<String> dirs) {
            skipDirectories.addAll(dirs);
            return this;
        }
        
        public CopyConfig skipFileExtension(String extension) {
            skipFileExtensions.add(extension.startsWith(".") ? extension : "." + extension);
            return this;
        }
        
        public CopyConfig customFilter(Predicate<Path> filter) {
            this.customFilter = filter;
            return this;
        }
        
        public CopyConfig verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }
        
        public CopyConfig replaceExisting(boolean replace) {
            this.replaceExisting = replace;
            return this;
        }
        
        // Getters
        public boolean isPreserveSymlinks() { return preserveSymlinks; }
        public boolean isSkipNodeModules() { return skipNodeModules; }
        public Set<String> getSkipDirectories() { return skipDirectories; }
        public Set<String> getSkipFileExtensions() { return skipFileExtensions; }
        public Predicate<Path> getCustomFilter() { return customFilter; }
        public boolean isCreateParentDirs() { return createParentDirs; }
        public boolean isReplaceExisting() { return replaceExisting; }
        public boolean isVerbose() { return verbose; }
    }
    
    /**
     * 复制结果类
     */
    public static class CopyResult {
        private int copiedFiles = 0;
        private int copiedDirectories = 0;
        private int createdSymlinks = 0;
        private int skippedItems = 0;
        private long totalSize = 0;
        private long duration = 0;
        
        public void incrementFiles() { copiedFiles++; }
        public void incrementDirectories() { copiedDirectories++; }
        public void incrementSymlinks() { createdSymlinks++; }
        public void incrementSkipped() { skippedItems++; }
        public void addSize(long size) { totalSize += size; }
        public void setDuration(long duration) { this.duration = duration; }
        
        public int getCopiedFiles() { return copiedFiles; }
        public int getCopiedDirectories() { return copiedDirectories; }
        public int getCreatedSymlinks() { return createdSymlinks; }
        public int getSkippedItems() { return skippedItems; }
        public long getTotalSize() { return totalSize; }
        public long getDuration() { return duration; }
        
        @Override
        public String toString() {
            return String.format(
                "复制完成 - 文件: %d, 目录: %d, 符号链接: %d, 跳过: %d, 总大小: %s, 耗时: %dms",
                copiedFiles, copiedDirectories, createdSymlinks, skippedItems,
                formatSize(totalSize), duration
            );
        }
        
        private String formatSize(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * 使用默认配置复制模板项目
     * @param sourcePath 源路径
     * @param targetPath 目标路径
     * @return 复制结果
     * @throws IOException IO异常
     */
    public static CopyResult copyTemplate(String sourcePath, String targetPath) throws IOException {
        return copyTemplate(Paths.get(sourcePath), Paths.get(targetPath), new CopyConfig());
    }
    
    /**
     * 复制模板项目，跳过node_modules
     * @param sourcePath 源路径
     * @param targetPath 目标路径
     * @return 复制结果
     * @throws IOException IO异常
     */
    public static CopyResult copyTemplateWithoutNodeModules(String sourcePath, String targetPath) throws IOException {
        CopyConfig config = new CopyConfig().skipNodeModules(true);
        return copyTemplate(Paths.get(sourcePath), Paths.get(targetPath), config);
    }
    
    /**
     * 复制模板项目（核心方法）
     * @param source 源路径
     * @param target 目标路径
     * @param config 复制配置
     * @return 复制结果
     * @throws IOException IO异常
     */
    public static CopyResult copyTemplate(Path source, Path target, CopyConfig config) throws IOException {
        if (!Files.exists(source)) {
            throw new IOException("源目录不存在: " + source);
        }
        
        if (!Files.isDirectory(source)) {
            throw new IOException("源路径不是目录: " + source);
        }
        
        // 检查符号链接权限
        if (config.isPreserveSymlinks() && !SymlinkCopyUtil.canCreateSymlinks()) {
            System.out.println("⚠️  警告: 无法创建符号链接，将复制实际内容");
            config.preserveSymlinks(false);
        }
        
        CopyResult result = new CopyResult();
        long startTime = System.currentTimeMillis();
        
        // 创建目标目录
        if (!Files.exists(target)) {
            Files.createDirectories(target);
            result.incrementDirectories();
        }
        
        // 执行复制
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
                    throws IOException {
                
                String dirName = dir.getFileName().toString();
                
                // 应用跳过规则
                if (shouldSkip(dir, config)) {
                    if (config.isVerbose()) {
                        System.out.println("跳过目录: " + dir);
                    }
                    result.incrementSkipped();
                    return FileVisitResult.SKIP_SUBTREE;
                }
                
                Path relativePath = source.relativize(dir);
                Path targetPath = target.resolve(relativePath);
                
                // 处理符号链接目录
                if (Files.isSymbolicLink(dir)) {
                    if (config.isPreserveSymlinks()) {
                        Path linkTarget = Files.readSymbolicLink(dir);
                        createSymlinkSafely(targetPath, linkTarget, config);
                        result.incrementSymlinks();
                        if (config.isVerbose()) {
                            System.out.println("创建目录符号链接: " + targetPath + " -> " + linkTarget);
                        }
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        // 不保留符号链接，继续遍历
                        if (!Files.exists(targetPath)) {
                            Files.createDirectories(targetPath);
                            result.incrementDirectories();
                        }
                        return FileVisitResult.CONTINUE;
                    }
                }
                
                // 创建普通目录
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                    result.incrementDirectories();
                    if (config.isVerbose()) {
                        System.out.println("创建目录: " + targetPath);
                    }
                }
                
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
                    throws IOException {
                
                // 应用跳过规则
                if (shouldSkip(file, config)) {
                    if (config.isVerbose()) {
                        System.out.println("跳过文件: " + file);
                    }
                    result.incrementSkipped();
                    return FileVisitResult.CONTINUE;
                }
                
                Path relativePath = source.relativize(file);
                Path targetPath = target.resolve(relativePath);
                
                // 确保父目录存在
                Path parent = targetPath.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                
                // 处理符号链接文件
                if (Files.isSymbolicLink(file)) {
                    if (config.isPreserveSymlinks()) {
                        Path linkTarget = Files.readSymbolicLink(file);
                        createSymlinkSafely(targetPath, linkTarget, config);
                        result.incrementSymlinks();
                        if (config.isVerbose()) {
                            System.out.println("创建文件符号链接: " + targetPath + " -> " + linkTarget);
                        }
                    } else {
                        // 复制符号链接指向的实际文件
                        copyFileSafely(file, targetPath, config);
                        result.incrementFiles();
                        result.addSize(Files.size(file));
                    }
                } else {
                    // 复制普通文件
                    copyFileSafely(file, targetPath, config);
                    result.incrementFiles();
                    result.addSize(Files.size(file));
                    if (config.isVerbose()) {
                        System.out.println("复制文件: " + targetPath);
                    }
                }
                
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.err.println("访问失败: " + file + " - " + exc.getMessage());
                result.incrementSkipped();
                return FileVisitResult.CONTINUE;
            }
        });
        
        result.setDuration(System.currentTimeMillis() - startTime);
        
        if (config.isVerbose()) {
            System.out.println("\n" + result);
        }
        
        return result;
    }
    
    /**
     * 检查是否应该跳过某个路径
     */
    private static boolean shouldSkip(Path path, CopyConfig config) {
        String fileName = path.getFileName().toString();
        
        // 检查目录跳过规则
        if (Files.isDirectory(path) && config.getSkipDirectories().contains(fileName)) {
            return true;
        }
        
        // 检查文件扩展名跳过规则
        if (Files.isRegularFile(path)) {
            for (String ext : config.getSkipFileExtensions()) {
                if (fileName.endsWith(ext)) {
                    return true;
                }
            }
        }
        
        // 检查自定义过滤器
        if (config.getCustomFilter() != null && !config.getCustomFilter().test(path)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 安全地创建符号链接
     */
    private static void createSymlinkSafely(Path link, Path target, CopyConfig config) throws IOException {
        if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) {
            if (config.isReplaceExisting()) {
                Files.delete(link);
            } else {
                return; // 不替换已存在的文件
            }
        }
        
        Files.createSymbolicLink(link, target);
    }
    
    /**
     * 安全地复制文件
     */
    private static void copyFileSafely(Path source, Path target, CopyConfig config) throws IOException {
        if (Files.exists(target) && !config.isReplaceExisting()) {
            return; // 不替换已存在的文件
        }
        
        if (config.isReplaceExisting()) {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(source, target);
        }
    }
    
    /**
     * 复制Vue模板项目并设置node_modules符号链接
     * @param templatePath 模板路径
     * @param targetPath 目标路径
     * @param sharedNodeModulesPath 共享node_modules路径
     * @return 复制结果
     * @throws IOException IO异常
     */
    public static CopyResult copyVueTemplateWithSharedNodeModules(
            String templatePath, 
            String targetPath, 
            String sharedNodeModulesPath) throws IOException {
        
        Path template = Paths.get(templatePath);
        Path target = Paths.get(targetPath);
        Path sharedNodeModules = Paths.get(sharedNodeModulesPath);
        
        // 1. 复制模板（跳过node_modules）
        CopyConfig config = new CopyConfig()
                .skipNodeModules(true)
                .verbose(true);
        
        CopyResult result = copyTemplate(template, target, config);
        
        // 2. 创建node_modules符号链接
        try {
            Path nodeModulesLink = target.resolve("node_modules");
            
            if (Files.exists(nodeModulesLink)) {
                Files.delete(nodeModulesLink);
            }
            
            // 计算相对路径
            Path relativePath = nodeModulesLink.getParent().relativize(sharedNodeModules);
            Files.createSymbolicLink(nodeModulesLink, relativePath);
            
            result.incrementSymlinks();
            
            System.out.println("✅ Vue模板复制完成！");
            System.out.println("📦 node_modules -> " + relativePath);
            System.out.println("💡 提示：如果node_modules不存在，请运行 npm install");
            
        } catch (IOException e) {
            System.out.println("⚠️  无法创建node_modules符号链接: " + e.getMessage());
            System.out.println("💡 请手动运行: npm install");
        }
        
        return result;
    }
}

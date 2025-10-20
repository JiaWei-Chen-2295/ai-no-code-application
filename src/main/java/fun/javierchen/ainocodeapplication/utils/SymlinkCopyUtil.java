package fun.javierchen.ainocodeapplication.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * 符号链接复制工具类
 * 解决Java复制文件时符号链接变成普通目录的问题
 * 
 * @author javierchen
 */
public class SymlinkCopyUtil {
    
    /**
     * 复制目录，保留符号链接
     * @param source 源目录
     * @param target 目标目录
     * @throws IOException IO异常
     */
    public static void copyDirectoryWithSymlinks(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            throw new IOException("源目录不存在: " + source);
        }
        
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
                    throws IOException {
                // 检查是否是符号链接
                if (Files.isSymbolicLink(dir)) {
                    // 读取符号链接的目标
                    Path linkTarget = Files.readSymbolicLink(dir);
                    Path targetPath = target.resolve(source.relativize(dir));
                    
                    // 创建父目录（如果不存在）
                    Path parent = targetPath.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    
                    // 创建符号链接
                    Files.createSymbolicLink(targetPath, linkTarget);
                    
                    // 跳过此目录的遍历
                    return FileVisitResult.SKIP_SUBTREE;
                }
                
                // 创建普通目录
                Path targetPath = target.resolve(source.relativize(dir));
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
                    throws IOException {
                Path targetPath = target.resolve(source.relativize(file));
                
                // 确保父目录存在
                Path parent = targetPath.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                
                // 检查文件是否是符号链接
                if (Files.isSymbolicLink(file)) {
                    Path linkTarget = Files.readSymbolicLink(file);
                    Files.createSymbolicLink(targetPath, linkTarget);
                } else {
                    // 复制普通文件
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.err.println("访问文件失败: " + file + " - " + exc.getMessage());
                // 继续处理其他文件
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * 复制单个文件或符号链接
     * @param source 源文件
     * @param target 目标文件
     * @throws IOException IO异常
     */
    public static void copyWithSymlink(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            throw new IOException("源文件不存在: " + source);
        }
        
        // 确保父目录存在
        Path parent = target.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        
        if (Files.isSymbolicLink(source)) {
            // 读取并创建符号链接
            Path linkTarget = Files.readSymbolicLink(source);
            Files.createSymbolicLink(target, linkTarget);
        } else if (Files.isDirectory(source)) {
            // 递归复制目录
            copyDirectoryWithSymlinks(source, target);
        } else {
            // 复制普通文件
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    /**
     * 复制目录，跳过指定的目录
     * @param source 源目录
     * @param target 目标目录
     * @param skipDirs 要跳过的目录名集合
     * @throws IOException IO异常
     */
    public static void copyDirectorySkipDirs(Path source, Path target, Set<String> skipDirs) throws IOException {
        if (!Files.exists(source)) {
            throw new IOException("源目录不存在: " + source);
        }
        
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
                    throws IOException {
                String dirName = dir.getFileName().toString();
                
                // 跳过指定目录
                if (skipDirs.contains(dirName)) {
                    System.out.println("跳过目录: " + dir);
                    return FileVisitResult.SKIP_SUBTREE;
                }
                
                // 检查是否是符号链接
                if (Files.isSymbolicLink(dir)) {
                    Path linkTarget = Files.readSymbolicLink(dir);
                    Path targetPath = target.resolve(source.relativize(dir));
                    
                    // 创建父目录
                    Path parent = targetPath.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    
                    Files.createSymbolicLink(targetPath, linkTarget);
                    return FileVisitResult.SKIP_SUBTREE;
                }
                
                // 创建普通目录
                Path targetPath = target.resolve(source.relativize(dir));
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                }
                
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
                    throws IOException {
                Path targetPath = target.resolve(source.relativize(file));
                
                // 确保父目录存在
                Path parent = targetPath.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                
                if (Files.isSymbolicLink(file)) {
                    Path linkTarget = Files.readSymbolicLink(file);
                    Files.createSymbolicLink(targetPath, linkTarget);
                } else {
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    /**
     * 检查是否可以创建符号链接
     * 在Windows上需要管理员权限或开发者模式
     * @return true如果可以创建符号链接
     */
    public static boolean canCreateSymlinks() {
        try {
            Path tempDir = Files.createTempDirectory("symlink_test");
            Path tempLink = tempDir.resolve("test_link");
            Path tempTarget = tempDir.resolve("target");
            
            Files.createDirectory(tempTarget);
            Files.createSymbolicLink(tempLink, tempTarget);
            
            // 清理
            Files.delete(tempLink);
            Files.delete(tempTarget);
            Files.delete(tempDir);
            
            return true;
        } catch (IOException | UnsupportedOperationException e) {
            return false;
        }
    }
    
    /**
     * 检查路径是否为符号链接
     * @param path 要检查的路径
     * @return true如果是符号链接
     */
    public static boolean isSymbolicLink(Path path) {
        return Files.isSymbolicLink(path);
    }
    
    /**
     * 读取符号链接的目标路径
     * @param symlink 符号链接路径
     * @return 目标路径
     * @throws IOException 如果不是符号链接或读取失败
     */
    public static Path readSymbolicLink(Path symlink) throws IOException {
        if (!Files.isSymbolicLink(symlink)) {
            throw new IOException("不是符号链接: " + symlink);
        }
        return Files.readSymbolicLink(symlink);
    }
    
    /**
     * 创建符号链接
     * @param link 链接路径
     * @param target 目标路径
     * @throws IOException IO异常
     */
    public static void createSymbolicLink(Path link, Path target) throws IOException {
        // 确保父目录存在
        Path parent = link.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        
        // 如果链接已存在，先删除
        if (Files.exists(link, LinkOption.NOFOLLOW_LINKS)) {
            Files.delete(link);
        }
        
        Files.createSymbolicLink(link, target);
    }
    
    /**
     * 复制模板并重新创建符号链接到共享目录
     * @param templateSource 模板源目录
     * @param newProject 新项目目录
     * @param sharedNodeModules 共享的node_modules目录
     * @throws IOException IO异常
     */
    public static void copyTemplateAndSetupSymlink(
            Path templateSource, 
            Path newProject,
            Path sharedNodeModules) throws IOException {
        
        // 1. 复制模板（跳过 node_modules）
        Set<String> skipDirs = new HashSet<>();
        skipDirs.add("node_modules");
        skipDirs.add(".git");
        skipDirs.add("dist");
        skipDirs.add("target");
        
        copyDirectorySkipDirs(templateSource, newProject, skipDirs);
        
        // 2. 在新项目中创建 node_modules 符号链接
        Path nodeModulesLink = newProject.resolve("node_modules");
        
        if (Files.exists(nodeModulesLink)) {
            Files.delete(nodeModulesLink);
        }
        
        // 创建符号链接
        createSymbolicLink(nodeModulesLink, sharedNodeModules);
        
        System.out.println("✅ 项目复制完成，符号链接已创建！");
        System.out.println("📦 node_modules -> " + sharedNodeModules);
    }
}

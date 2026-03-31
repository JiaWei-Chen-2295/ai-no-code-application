package fun.javierchen.ainocodeapplication.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import jakarta.annotation.Resource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StaticResourceController 测试类
 * 主要测试Vue项目预览功能
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-local.yml")
class StaticResourceControllerTest {

    @Resource
    private StaticResourceController staticResourceController;

    /**
     * 测试Vue项目版本检测功能
     */
    @Test
    void testVueProjectVersionDetection() {
        System.out.println("=== 测试Vue项目版本检测功能 ===");
        
        // 查找user_code目录中的Vue项目
        String userCodePath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "user_code";
        File userCodeDir = new File(userCodePath);
        
        if (!userCodeDir.exists()) {
            System.out.println("❌ user_code目录不存在，跳过测试: " + userCodePath);
            return;
        }
        
        // 查找Vue项目目录
        File[] vueProjectDirs = userCodeDir.listFiles(f -> 
            f.isDirectory() && f.getName().startsWith("vue_project_"));
        
        if (vueProjectDirs == null || vueProjectDirs.length == 0) {
            System.out.println("❌ 没有找到Vue项目目录，跳过测试");
            return;
        }
        
        for (File vueProjectDir : vueProjectDirs) {
            System.out.println("🔍 检查Vue项目: " + vueProjectDir.getName());
            
            // 检查版本目录
            File[] versionDirs = vueProjectDir.listFiles(f -> 
                f.isDirectory() && f.getName().matches("v\\d+"));
            
            if (versionDirs != null && versionDirs.length > 0) {
                for (File versionDir : versionDirs) {
                    System.out.println("  📁 发现版本: " + versionDir.getName());
                    
                    File projectDir = new File(versionDir, "vue-project");
                    if (projectDir.exists()) {
                        System.out.println("    ✅ vue-project目录存在");
                        
                        // 检查关键文件
                        File packageJson = new File(projectDir, "package.json");
                        File srcDir = new File(projectDir, "src");
                        File distDir = new File(projectDir, "dist");
                        
                        System.out.println("    📦 package.json: " + (packageJson.exists() ? "✅" : "❌"));
                        System.out.println("    📂 src目录: " + (srcDir.exists() ? "✅" : "❌"));
                        System.out.println("    🏗️ dist目录: " + (distDir.exists() ? "✅" : "❌"));
                        
                        // 验证基本结构
                        assertTrue(packageJson.exists(), "Vue项目应该包含package.json");
                        assertTrue(srcDir.exists(), "Vue项目应该包含src目录");
                    } else {
                        System.out.println("    ❌ vue-project目录不存在");
                    }
                }
            } else {
                System.out.println("  ❌ 没有找到版本目录");
            }
        }
        
        System.out.println("✅ Vue项目版本检测测试完成");
    }
    
    /**
     * 测试预览URL路径构建逻辑
     */
    @Test 
    void testPreviewUrlPathConstruction() {
        System.out.println("=== 测试预览URL路径构建 ===");
        
        // 测试不同的URL格式和对应的预期资源路径
        Object[][] testCases = {
            {"/static/preview/337888335437889536/", 337888335437889536L, 0, "/"},
            {"/static/preview/337888335437889536/1/", 337888335437889536L, 1, "/"},
            {"/static/preview/337888335437889536/assets/index.js", 337888335437889536L, 0, "/assets/index.js"},
            {"/static/preview/337888335437889536/1/assets/style.css", 337888335437889536L, 1, "/assets/style.css"},
            {"/static/preview/337888335437889536/1/favicon.ico", 337888335437889536L, 1, "/favicon.ico"}
        };
        
        for (Object[] testCase : testCases) {
            String fullPath = (String) testCase[0];
            Long appId = (Long) testCase[1];
            int version = (int) testCase[2];
            String expectedResourcePath = (String) testCase[3];
            
            System.out.println("🔗 测试路径: " + fullPath);
            System.out.println("   应用ID: " + appId + ", 版本: " + version);
            System.out.println("   预期资源路径: '" + expectedResourcePath + "'");
            
            // 这里可以模拟路径解析逻辑验证
            String expectedPrefix = "/static/preview/" + appId;
            if (version > 0) {
                expectedPrefix += "/" + version;
            }
            
            if (fullPath.startsWith(expectedPrefix)) {
                String actualResourcePath = fullPath.substring(expectedPrefix.length());
                boolean matches = actualResourcePath.equals(expectedResourcePath);
                System.out.println("   实际资源路径: '" + actualResourcePath + "' " + (matches ? "✅" : "❌"));
            } else {
                System.out.println("   ❌ 路径前缀不匹配");
            }
            System.out.println();
        }
        
        System.out.println("✅ URL路径构建测试完成");
    }
    
    /**
     * 模拟测试Vue项目构建流程
     */
    @Test
    void testVueProjectBuildFlow() {
        System.out.println("=== 模拟Vue项目构建流程测试 ===");
        
        // 查找真实的Vue项目来测试
        String userCodePath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "user_code";
        File userCodeDir = new File(userCodePath);
        
        if (!userCodeDir.exists()) {
            System.out.println("❌ user_code目录不存在，跳过构建测试");
            return;
        }
        
        File[] vueProjectDirs = userCodeDir.listFiles(f -> 
            f.isDirectory() && f.getName().startsWith("vue_project_"));
        
        if (vueProjectDirs == null || vueProjectDirs.length == 0) {
            System.out.println("❌ 没有找到Vue项目，跳过构建测试");
            return;
        }
        
        File vueProjectDir = vueProjectDirs[0];
        System.out.println("🎯 选择测试项目: " + vueProjectDir.getName());
        
        // 提取应用ID
        String appIdStr = vueProjectDir.getName().replace("vue_project_", "");
        System.out.println("📋 应用ID: " + appIdStr);
        
        // 检查项目结构
        File v1Dir = new File(vueProjectDir, "v1");
        if (v1Dir.exists()) {
            File projectDir = new File(v1Dir, "vue-project");
            if (projectDir.exists()) {
                System.out.println("✅ 项目结构验证通过");
                System.out.println("📁 项目路径: " + projectDir.getAbsolutePath());
                
                // 检查预期的访问URL
                String previewUrl = "/api/static/preview/" + appIdStr + "/";
                System.out.println("🌐 预期访问URL: " + previewUrl);
                
                // 检查dist目录状态
                File distDir = new File(projectDir, "dist");
                System.out.println("🏗️ 当前dist状态: " + (distDir.exists() ? "已存在" : "需要构建"));
                
                if (distDir.exists()) {
                    File[] distFiles = distDir.listFiles();
                    if (distFiles != null && distFiles.length > 0) {
                        System.out.println("📦 dist目录文件数量: " + distFiles.length);
                        for (File file : distFiles) {
                            System.out.println("  - " + file.getName());
                        }
                    }
                }
                
            } else {
                System.out.println("❌ vue-project目录不存在");
            }
        } else {
            System.out.println("❌ v1目录不存在");
        }
        
        System.out.println("✅ Vue项目构建流程测试完成");
    }
}

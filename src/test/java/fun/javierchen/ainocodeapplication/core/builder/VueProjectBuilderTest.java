////package fun.javierchen.ainocodeapplication.core.builder;
////
////import org.junit.jupiter.api.Test;
////import org.springframework.boot.test.context.SpringBootTest;
////import org.springframework.test.context.TestPropertySource;
////import jakarta.annotation.Resource;
////
////import java.io.File;
////import java.nio.file.Files;
////import java.nio.file.Path;
////import java.nio.file.Paths;
////
////import static org.junit.jupiter.api.Assertions.*;
////
/////**
//// * VueProjectBuilder 测试类
//// * 验证Vue项目构建功能
//// */
////@SpringBootTest
////@TestPropertySource(locations = "classpath:application-local.yml")
////class VueProjectBuilderTest {
////
////    @Resource
////    private VueProjectBuilder vueProjectBuilder;
////
////    /**
////     * 测试Vue项目构建器的基本功能（不实际执行npm命令）
////     * 主要测试路径验证和逻辑判断
////     */
////    @Test
////    void testBuildProjectValidation() {
////        // 测试空路径
////        assertFalse(vueProjectBuilder.buildProject(null));
////
////        // 测试不存在的路径
////        assertFalse(vueProjectBuilder.buildProject("/non/existent/path"));
////
////        // 测试现有的Vue模板目录（应该包含package.json）
////        String templatePath = System.getProperty("user.dir") + File.separator
////                + "front" + File.separator + "ai-no-code" + File.separator + "template_vue";
////
////        File templateDir = new File(templatePath);
////        if (templateDir.exists() && templateDir.isDirectory()) {
////            // 注意：这里不实际执行构建，因为npm install会很慢
////            // 只验证目录和package.json存在
////            File packageJson = new File(templateDir, "package.json");
////            assertTrue(packageJson.exists(), "Vue模板应该包含package.json文件");
////
////            System.out.println("Vue模板路径验证通过: " + templatePath);
////        } else {
////            System.out.println("Vue模板目录不存在，跳过验证: " + templatePath);
////        }
////    }
////
////    /**
////     * 测试异步构建方法不会抛出异常
////     */
////    @Test
////    void testBuildProjectAsync() {
////        // 测试异步方法不会抛出异常（即使路径不存在）
////        assertDoesNotThrow(() -> {
////            vueProjectBuilder.buildProjectAsync("/non/existent/path");
////            // 等待一下异步线程启动
////            Thread.sleep(100);
////        });
////    }
////
////    /**
////     * 测试真实Vue项目的构建过程（使用已生成的项目）
////     * 这个测试会检查是否能找到真实的Vue项目并验证构建逻辑
////     */
////    @Test
////    void testRealVueProjectBuild() {
////        // 查找tmp目录下的Vue项目
////        String projectBasePath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "user_code";
////        File userCodeDir = new File(projectBasePath);
////
////        if (!userCodeDir.exists()) {
////            System.out.println("用户代码目录不存在，跳过真实项目测试: " + projectBasePath);
////            return;
////        }
////
////        // 查找Vue项目目录
////        File[] vueProjectDirs = userCodeDir.listFiles(f -> f.isDirectory() && f.getName().startsWith("vue_project_"));
////
////        if (vueProjectDirs == null || vueProjectDirs.length == 0) {
////            System.out.println("没有找到Vue项目目录，跳过真实项目测试");
////            return;
////        }
////
////        // 使用第一个Vue项目进行测试
////        File vueProjectDir = vueProjectDirs[0];
////        File v1Dir = new File(vueProjectDir, "v1");
////        File projectDir = new File(v1Dir, "vue-project");
////
////        if (!projectDir.exists()) {
////            System.out.println("Vue项目结构不完整，跳过测试: " + projectDir.getAbsolutePath());
////            return;
////        }
////
////        System.out.println("找到真实Vue项目，开始测试: " + projectDir.getAbsolutePath());
////
////        // 验证项目结构
////        File packageJson = new File(projectDir, "package.json");
////        assertTrue(packageJson.exists(), "Vue项目应该包含package.json");
////
////        File srcDir = new File(projectDir, "src");
////        assertTrue(srcDir.exists(), "Vue项目应该包含src目录");
////
////        // 注意：这里不实际执行npm install和npm run build
////        // 因为测试环境可能没有安装Node.js或依赖包
////        // 只验证VueProjectBuilder能正确识别Vue项目结构
////
////        System.out.println("✅ Vue项目结构验证通过");
////        System.out.println("📁 项目路径: " + projectDir.getAbsolutePath());
////        System.out.println("📦 package.json: " + packageJson.exists());
////        System.out.println("📂 src目录: " + srcDir.exists());
////
////        // 模拟验证构建会检查的关键文件
////        File[] requiredFiles = {
////                new File(projectDir, "vite.config.ts"),
////                new File(projectDir, "tsconfig.json"),
////                new File(projectDir, "src/App.vue"),
////                new File(projectDir, "src/main.ts")
////        };
////
////        for (File file : requiredFiles) {
////            if (file.exists()) {
////                System.out.println("✅ " + file.getName() + " 存在");
////            } else {
////                System.out.println("⚠️ " + file.getName() + " 不存在");
////            }
////        }
////    }
//
//    /**
//     * 测试Vue项目资源路径修复功能
//     */
//    @Test
//    void testVueProjectPathFixing() {
//        System.out.println("=== 测试Vue项目资源路径修复 ===");
//
//        // 查找真实的Vue项目来测试
//        String userCodePath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "user_code";
//        File userCodeDir = new File(userCodePath);
//
//        if (!userCodeDir.exists()) {
//            System.out.println("❌ user_code目录不存在，跳过路径修复测试");
//            return;
//        }
//
//        File[] vueProjectDirs = userCodeDir.listFiles(f ->
//            f.isDirectory() && f.getName().startsWith("vue_project_"));
//
//        if (vueProjectDirs == null || vueProjectDirs.length == 0) {
//            System.out.println("❌ 没有找到Vue项目，跳过路径修复测试");
//            return;
//        }
//
//        File vueProjectDir = vueProjectDirs[0];
//        File v1Dir = new File(vueProjectDir, "v1");
//        File projectDir = new File(v1Dir, "vue-project");
//
//        if (!projectDir.exists()) {
//            System.out.println("❌ Vue项目结构不完整");
//            return;
//        }
//
//        System.out.println("🔧 测试项目: " + projectDir.getAbsolutePath());
//
//        // 检查dist目录状态
//        File distDir = new File(projectDir, "dist");
//        boolean needsBuild = !distDir.exists();
//
//        System.out.println("📦 Dist目录状态: " + (distDir.exists() ? "存在" : "需要构建"));
//
//        if (needsBuild) {
//            System.out.println("ℹ️ 下次预览访问时将触发自动构建和路径修复");
//        } else {
//            // 检查已构建的文件
//            File indexFile = new File(distDir, "index.html");
//            if (indexFile.exists()) {
//                System.out.println("✅ index.html 存在");
//            }
//
//            File assetsDir = new File(distDir, "assets");
//            if (assetsDir.exists()) {
//                File[] jsFiles = assetsDir.listFiles((dir, name) -> name.endsWith(".js"));
//                System.out.println("📄 找到 " + (jsFiles != null ? jsFiles.length : 0) + " 个JS文件");
//            }
//        }
//
//        System.out.println("✅ 资源路径修复测试完成");
//        System.out.println("💡 提示: 访问预览URL将触发自动构建和路径修复");
//    }
//}

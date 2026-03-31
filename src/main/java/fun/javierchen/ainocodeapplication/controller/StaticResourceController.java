package fun.javierchen.ainocodeapplication.controller;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import fun.javierchen.ainocodeapplication.constant.CodeFileConstant;
import fun.javierchen.ainocodeapplication.core.builder.VueProjectBuilder;
import fun.javierchen.ainocodeapplication.model.entity.App;
import fun.javierchen.ainocodeapplication.model.entity.ChatHistory;
import fun.javierchen.ainocodeapplication.security.DirectorySandboxUtil;
import fun.javierchen.ainocodeapplication.service.AppService;
import fun.javierchen.ainocodeapplication.service.ChatHistoryService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.nio.file.Path;

@Slf4j
@RestController
@RequestMapping("/static")
public class StaticResourceController {

    // 应用生成根目录（用于浏览）
    private static final String PREVIEW_ROOT_DIR = System.getProperty("user.dir") + "/tmp/user_code";
    
    @Resource
    private AppService appService;
    
    @Resource
    private ChatHistoryService chatHistoryService;
    
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 预览应用最新版本
     * 访问格式：http://localhost:port/api/static/preview/{appId}[/{fileName}]
     */
    @GetMapping("/preview/{appId}/**")
    public ResponseEntity<org.springframework.core.io.Resource> previewLatestVersion(
            @PathVariable Long appId,
            HttpServletRequest request) {
        
        // 对于Vue项目，重定向到带版本号的URL，确保相对路径正确解析
        String vueProjectDirName = "vue_project_" + appId;
        File vueProjectBaseDir = new File(PREVIEW_ROOT_DIR, vueProjectDirName);
        boolean isVueProject = vueProjectBaseDir.exists() && vueProjectBaseDir.isDirectory();
        
        if (isVueProject) {
            // 获取最新版本号
            int latestVersion = detectVueProjectVersion(vueProjectBaseDir, null);
            if (latestVersion > 0) {
                // 获取当前请求的资源路径
                String resourcePath = getResourcePathFromRequest(request, appId, 0);
                
                // 构建带版本号的重定向URL
                String baseUrl = "/api/static/preview/" + appId + "/" + latestVersion;
                String redirectUrl = baseUrl + resourcePath;
                
                log.info("Vue项目重定向: {} -> {}", request.getRequestURI(), redirectUrl);
                
                // 返回重定向响应
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", redirectUrl);
                headers.add("X-Vue-Version-Redirect", String.valueOf(latestVersion));
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
        }
        
        return previewAppVersion(appId, null, request);
    }

    /**
     * 预览应用指定版本
     * 访问格式：http://localhost:port/api/static/preview/{appId}/{version}[/{fileName}]
     */
    @GetMapping("/preview/{appId}/{version}/**")
    public ResponseEntity<org.springframework.core.io.Resource> previewSpecificVersion(
            @PathVariable Long appId,
            @PathVariable Integer version,
            HttpServletRequest request) {
        return previewAppVersion(appId, version, request);
    }

    /**
     * 处理Vue项目不带版本号的所有资源请求  
     * 自动映射到最新版本的对应资源
     * 这个方法会捕获所有不符合带版本号格式的资源请求
     */
    @GetMapping("/preview/{appId}/{resource:(?!\\d+$).*}")
    public ResponseEntity<org.springframework.core.io.Resource> previewVueResourcesWithoutVersion(
            @PathVariable Long appId,
            @PathVariable String resource,
            HttpServletRequest request) {
        
        // 检查是否为Vue项目
        String vueProjectDirName = "vue_project_" + appId;
        File vueProjectBaseDir = new File(PREVIEW_ROOT_DIR, vueProjectDirName);
        boolean isVueProject = vueProjectBaseDir.exists() && vueProjectBaseDir.isDirectory();
        
        if (!isVueProject) {
            // 不是Vue项目，返回404
            log.debug("应用{}不是Vue项目，无法处理资源请求: {}", appId, resource);
            return ResponseEntity.notFound().build();
        }
        
        // 获取最新版本号
        int latestVersion = detectVueProjectVersion(vueProjectBaseDir, null);
        if (latestVersion == 0) {
            log.warn("Vue项目未找到有效版本: {}", appId);
            return ResponseEntity.notFound().build();
        }
        
        log.info("Vue项目资源请求自动映射: appId={}, resource={} -> version={}", 
                appId, resource, latestVersion);
        
        // 委托给带版本号的处理方法
        return previewAppVersion(appId, latestVersion, request);
    }

    /**
     * 预览应用版本的通用方法
     */
    private ResponseEntity<org.springframework.core.io.Resource> previewAppVersion(
            Long appId, Integer version, HttpServletRequest request) {
        try {
            // 1. 首先检查user_code目录中是否有Vue项目
            String vueProjectDirName = "vue_project_" + appId;
            File vueProjectBaseDir = new File(PREVIEW_ROOT_DIR, vueProjectDirName);
            
            boolean isVueProject = vueProjectBaseDir.exists() && vueProjectBaseDir.isDirectory();
            
            String projectPath;
            int targetVersion;
            
            if (isVueProject) {
                log.info("检测到Vue项目: {}", vueProjectBaseDir.getAbsolutePath());
                
                // 2. 自动检测Vue项目版本
                targetVersion = detectVueProjectVersion(vueProjectBaseDir, version);
                if (targetVersion == 0) {
                    log.warn("未找到有效的Vue项目版本: {}", vueProjectBaseDir.getAbsolutePath());
                    return ResponseEntity.notFound().build();
                }
                
                // 3. 构建Vue项目路径
                File versionDir = new File(vueProjectBaseDir, "v" + targetVersion);
                File vueProjectDir = new File(versionDir, "vue-project");
                
                if (!vueProjectDir.exists()) {
                    log.warn("Vue项目目录不存在: {}", vueProjectDir.getAbsolutePath());
                    return ResponseEntity.notFound().build();
                }
                
                // 4. 检查并构建Vue项目
                File distDir = new File(vueProjectDir, "dist");
                if (!distDir.exists() || !distDir.isDirectory()) {
                    log.info("Vue项目dist目录不存在，开始构建: {}", vueProjectDir.getAbsolutePath());
                    boolean buildSuccess = vueProjectBuilder.buildProject(vueProjectDir.getAbsolutePath());
                    if (!buildSuccess) {
                        log.error("Vue项目构建失败: {}", vueProjectDir.getAbsolutePath());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                    log.info("Vue项目构建成功，生成dist目录: {}", distDir.getAbsolutePath());
                } else {
                    log.debug("Vue项目dist目录已存在: {}", distDir.getAbsolutePath());
                    // 验证dist目录内容
                    File[] distFiles = distDir.listFiles();
                    log.debug("dist目录包含 {} 个文件", distFiles != null ? distFiles.length : 0);
                }
                
                // 5. Vue项目从dist目录提供文件
                projectPath = distDir.getAbsolutePath();
                log.info("Vue项目预览路径: {}", projectPath);
                
            } else {
                // 6. 传统HTML/多文件项目逻辑
                App app = appService.getById(appId);
                if (app == null) {
                    return ResponseEntity.notFound().build();
                }
                
                // 确定版本号
                if (version != null && version > 0) {
                    targetVersion = version;
                } else {
                    targetVersion = getLatestCodeVersion(appId);
                    if (targetVersion == 0) {
                        return ResponseEntity.notFound().build();
                    }
                }
                
                String codeGenType = app.getCodeGenType();
                String appDirName = codeGenType + "_" + appId;
                projectPath = CodeFileConstant.CODE_FILE_PATH + File.separator + appDirName + File.separator + targetVersion;
            }
            
            // 7. 获取资源路径
            String resourcePath = getResourcePathFromRequest(request, appId, targetVersion);
            log.debug("解析得到的资源路径: '{}'", resourcePath);
            
            // 8. 处理目录访问重定向
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            
            // 9. 默认返回 index.html
            if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }
            
            // 10. 构建完整文件路径
            String filePath = projectPath + resourcePath.replace("/", File.separator);
            File file = new File(filePath);
            log.debug("最终文件路径: {}", filePath);
            
            // 11. 检查文件是否存在
            if (!file.exists()) {
                log.warn("预览文件不存在: {} (项目类型: {})", filePath, isVueProject ? "Vue" : "HTML");
                log.warn("请求资源路径: '{}', 项目路径: '{}', 应用ID: {}, 版本: {}", 
                         resourcePath, projectPath, appId, targetVersion);
                
                // 列出项目目录内容以便调试
                if (isVueProject) {
                    File projectDir = new File(projectPath);
                    if (projectDir.exists()) {
                        File[] files = projectDir.listFiles();
                        log.debug("项目目录内容 ({}): {}", projectDir.getAbsolutePath(), 
                                 files != null ? java.util.Arrays.toString(files) : "null");
                    }
                }
                
                // 对于Vue项目，如果请求的资源不存在，尝试多种回退策略
                if (isVueProject) {
                    // 策略1: 尝试重定向到最新版本
                    ResponseEntity<org.springframework.core.io.Resource> latestVersionResponse = 
                            tryRedirectToLatestVersion(appId, targetVersion, resourcePath, request);
                    if (latestVersionResponse != null) {
                        return latestVersionResponse;
                    }
                    
                    // 策略2: 对于非index.html的请求，回退到index.html
                    if (!resourcePath.equals("/index.html")) {
                        log.info("Vue项目文件不存在，尝试回退到index.html: {}", resourcePath);
                        String indexPath = projectPath + File.separator + "index.html";
                        File indexFile = new File(indexPath);
                        if (indexFile.exists()) {
                            log.info("成功回退到index.html: {}", indexPath);
                            org.springframework.core.io.Resource resource = new FileSystemResource(indexFile);
                            return ResponseEntity.ok()
                                    .header("Content-Type", "text/html; charset=UTF-8")
                                    .body(resource);
                        }
                    }
                }
                
                return ResponseEntity.notFound().build();
            }
            
            // 12. 返回文件资源
            log.debug("提供预览文件: {} (版本: {}, 类型: {})", filePath, targetVersion, isVueProject ? "Vue" : "HTML");
            org.springframework.core.io.Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(filePath))
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("预览失败: appId={}, version={}, error={}", appId, version, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 检测Vue项目版本
     * @param vueProjectBaseDir Vue项目基础目录（vue_project_xxx）
     * @param specifiedVersion 指定的版本号，如果为null则返回最新版本
     * @return 版本号，0表示未找到
     */
    private int detectVueProjectVersion(File vueProjectBaseDir, Integer specifiedVersion) {
        try {
            File[] versionDirs = vueProjectBaseDir.listFiles(f -> 
                f.isDirectory() && f.getName().matches("v\\d+"));
            
            if (versionDirs == null || versionDirs.length == 0) {
                log.warn("未找到版本目录: {}", vueProjectBaseDir.getAbsolutePath());
                return 0;
            }
            
            if (specifiedVersion != null && specifiedVersion > 0) {
                // 检查指定版本是否存在
                File specifiedVersionDir = new File(vueProjectBaseDir, "v" + specifiedVersion);
                if (specifiedVersionDir.exists() && specifiedVersionDir.isDirectory()) {
                    File vueProjectDir = new File(specifiedVersionDir, "vue-project");
                    if (vueProjectDir.exists()) {
                        log.info("找到指定版本: v{}", specifiedVersion);
                        return specifiedVersion;
                    }
                }
                log.warn("指定版本v{}不存在，将使用最新版本", specifiedVersion);
            }
            
            // 寻找最新版本
            int latestVersion = 0;
            for (File versionDir : versionDirs) {
                String versionName = versionDir.getName();
                try {
                    int versionNum = Integer.parseInt(versionName.substring(1));
                    File vueProjectDir = new File(versionDir, "vue-project");
                    if (vueProjectDir.exists() && versionNum > latestVersion) {
                        latestVersion = versionNum;
                    }
                } catch (NumberFormatException e) {
                    log.warn("无效的版本目录名: {}", versionName);
                }
            }
            
            if (latestVersion > 0) {
                log.info("找到最新版本: v{}", latestVersion);
            }
            return latestVersion;
            
        } catch (Exception e) {
            log.error("检测Vue项目版本失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 尝试重定向到Vue项目的最新版本
     * 当当前版本的资源不存在时，检查最新版本是否有对应资源
     * 
     * @param appId 应用ID
     * @param currentVersion 当前请求的版本号
     * @param resourcePath 资源路径
     * @param request HTTP请求
     * @return 如果找到最新版本的资源则返回响应，否则返回null
     */
    private ResponseEntity<org.springframework.core.io.Resource> tryRedirectToLatestVersion(
            Long appId, int currentVersion, String resourcePath, HttpServletRequest request) {
        try {
            String vueProjectDirName = "vue_project_" + appId;
            File vueProjectBaseDir = new File(PREVIEW_ROOT_DIR, vueProjectDirName);
            
            if (!vueProjectBaseDir.exists()) {
                return null;
            }
            
            // 查找最新版本
            int latestVersion = detectVueProjectVersion(vueProjectBaseDir, null);
            if (latestVersion == 0 || latestVersion == currentVersion) {
                // 没有找到更新的版本或已经是最新版本
                log.debug("当前已是最新版本或无其他版本可用: 当前v{}, 最新v{}", currentVersion, latestVersion);
                return null;
            }
            
            log.info("尝试从最新版本v{}获取资源 (当前版本v{}): {}", latestVersion, currentVersion, resourcePath);
            
            // 检查最新版本中是否存在对应的资源
            File latestVersionDir = new File(vueProjectBaseDir, "v" + latestVersion);
            File latestVueProjectDir = new File(latestVersionDir, "vue-project");
            
            if (!latestVueProjectDir.exists()) {
                log.warn("最新版本项目目录不存在: {}", latestVueProjectDir.getAbsolutePath());
                return null;
            }
            
            // 构建并获取最新版本的dist目录
            File latestDistDir = new File(latestVueProjectDir, "dist");
            if (!latestDistDir.exists()) {
                log.info("最新版本需要构建，开始构建: {}", latestVueProjectDir.getAbsolutePath());
                boolean buildSuccess = vueProjectBuilder.buildProject(latestVueProjectDir.getAbsolutePath());
                if (!buildSuccess) {
                    log.error("最新版本构建失败");
                    return null;
                }
                log.info("最新版本构建成功");
            }
            
            // 检查最新版本中是否存在请求的资源
            String latestProjectPath = latestDistDir.getAbsolutePath();
            String latestFilePath = latestProjectPath + resourcePath.replace("/", File.separator);
            File latestFile = new File(latestFilePath);
            
            if (latestFile.exists()) {
                log.info("✅ 在最新版本v{}中找到资源: {}", latestVersion, latestFilePath);
                org.springframework.core.io.Resource resource = new FileSystemResource(latestFile);
                
                // 返回资源，并添加版本重定向头部信息
                return ResponseEntity.ok()
                        .header("Content-Type", getContentTypeWithCharset(latestFilePath))
                        .header("X-Redirected-From-Version", String.valueOf(currentVersion))
                        .header("X-Served-From-Version", String.valueOf(latestVersion))
                        .header("X-Resource-Redirect", "true")
                        .body(resource);
            }
            
            // 对于页面路由请求（如 /about, /user等），尝试返回最新版本的index.html
            if (!resourcePath.equals("/index.html") && !resourcePath.startsWith("/assets/") 
                    && !resourcePath.contains(".")) {
                String latestIndexPath = latestProjectPath + File.separator + "index.html";
                File latestIndexFile = new File(latestIndexPath);
                if (latestIndexFile.exists()) {
                    log.info("✅ 为页面路由请求返回最新版本的index.html: {} -> v{}", resourcePath, latestVersion);
                    org.springframework.core.io.Resource resource = new FileSystemResource(latestIndexFile);
                    return ResponseEntity.ok()
                            .header("Content-Type", "text/html; charset=UTF-8")
                            .header("X-Redirected-From-Version", String.valueOf(currentVersion))
                            .header("X-Served-From-Version", String.valueOf(latestVersion))
                            .header("X-SPA-Fallback", "true")
                            .body(resource);
                }
            }
            
            log.debug("最新版本v{}中也未找到资源: {}", latestVersion, resourcePath);
            return null;
            
        } catch (Exception e) {
            log.error("重定向到最新版本时发生错误", e);
            return null;
        }
    }

    /**
     * 处理Vue项目的全局assets请求
     * 当HTML中使用绝对路径(/assets/xxx)时，自动映射到正确的预览路径
     * 访问格式：http://localhost:port/assets/{fileName}
     */
    @GetMapping("/assets/**")
    public ResponseEntity<org.springframework.core.io.Resource> handleGlobalAssetsRequest(
            HttpServletRequest request) {
        
        log.info("收到全局assets请求: {}", request.getRequestURI());
        
        // 从请求的Referer头获取原始预览页面URL
        String referer = request.getHeader("Referer");
        log.info("Referer: {}", referer);
        
        if (referer != null && referer.contains("/api/static/preview/")) {
            // 解析Referer URL提取应用ID和版本
            try {
                // 提取应用ID和版本信息
                String[] urlParts = referer.split("/");
                Long appId = null;
                Integer version = null;
                
                for (int i = 0; i < urlParts.length; i++) {
                    if ("preview".equals(urlParts[i]) && i + 1 < urlParts.length) {
                        appId = Long.parseLong(urlParts[i + 1]);
                        if (i + 2 < urlParts.length && urlParts[i + 2].matches("\\d+")) {
                            version = Integer.parseInt(urlParts[i + 2]);
                        }
                        break;
                    }
                }
                
                if (appId != null) {
                    log.info("从Referer解析出应用ID: {}, 版本: {}", appId, version);
                    
                    // 构建正确的预览URL并重定向
                    String correctUrl = "/api/static/preview/" + appId + 
                        (version != null ? "/" + version : "") + 
                        request.getRequestURI();
                    
                    log.info("重定向assets请求: {} -> {}", request.getRequestURI(), correctUrl);
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Location", correctUrl);
                    headers.add("X-Assets-Redirect", "true");
                    return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
                }
                
            } catch (Exception e) {
                log.error("解析Referer URL失败", e);
            }
        }
        
        log.warn("无法处理全局assets请求，Referer无效或不包含预览URL: {}", referer);
        return ResponseEntity.notFound().build();
    }

    /**
     * 提供静态资源访问，支持目录重定向（部署后访问）
     * 访问格式：http://localhost:port/api/static/{deployKey}[/{fileName}]
     */
    @GetMapping("/{deployKey}/**")
    public ResponseEntity<org.springframework.core.io.Resource> serveStaticResource(
            @PathVariable String deployKey,
            HttpServletRequest request) {
        try {
            // 获取资源路径
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/static/" + deployKey).length());
            // 如果是目录访问（不带斜杠），重定向到带斜杠的URL
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            // 默认返回 index.html
            if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }
            // 构建文件路径
            String filePath = PREVIEW_ROOT_DIR + "/" + deployKey + resourcePath;
            File file = new File(filePath);
            // 检查文件是否存在
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            // 返回文件资源
            org.springframework.core.io.Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(filePath))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 从请求中提取资源路径
     */
    private String getResourcePathFromRequest(HttpServletRequest request, Long appId, int version) {
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        log.debug("完整请求路径: {}", fullPath);
        
        // 构建预期的前缀路径
        String expectedPrefix = "/static/preview/" + appId;
        if (version > 0) {
            expectedPrefix += "/" + version;
        }
        
        // 特殊处理：如果没有找到精确匹配，尝试不带版本号的匹配
        boolean exactMatch = fullPath.startsWith(expectedPrefix);
        if (!exactMatch && version > 0) {
            String fallbackPrefix = "/static/preview/" + appId;
            if (fullPath.startsWith(fallbackPrefix)) {
                expectedPrefix = fallbackPrefix;
                exactMatch = true;
                log.debug("使用回退前缀: {}", expectedPrefix);
            }
        }
        
        log.debug("预期前缀: {}", expectedPrefix);
        
        // 检查路径是否以预期前缀开始
        if (exactMatch || fullPath.startsWith(expectedPrefix)) {
            String resourcePath = fullPath.substring(expectedPrefix.length());
            log.debug("提取的资源路径: '{}'", resourcePath);
            return resourcePath;
        } else {
            log.warn("路径前缀不匹配: 完整路径='{}', 预期前缀='{}'", fullPath, expectedPrefix);
            return "";
        }
    }
    
    /**
     * 从请求中提取资源路径（兼容旧方法）
     */
    private String getResourcePathFromRequest(HttpServletRequest request, String prefix) {
        String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        return resourcePath.substring(("/static" + prefix).length());
    }

    /**
     * 获取应用的最新代码版本号
     */
    private int getLatestCodeVersion(Long appId) {
        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .select("codeVersion")
                    .from(ChatHistory.class)
                    .where(ChatHistory::getAppId).eq(appId)
                    .and(ChatHistory::getIsCode).eq(1)
                    .and(ChatHistory::getIsDelete).eq(0)
                    .orderBy(ChatHistory::getCodeVersion, false) // 按版本号降序
                    .limit(1);
            
            ChatHistory latestCodeHistory = chatHistoryService.getOne(queryWrapper);
            return latestCodeHistory != null && latestCodeHistory.getCodeVersion() != null 
                    ? latestCodeHistory.getCodeVersion() : 0;
        } catch (Exception e) {
            log.warn("获取最新版本号失败：{}", e.getMessage());
            return 0;
        }
    }

    /**
     * 根据文件扩展名返回带字符编码的 Content-Type
     */
    private String getContentTypeWithCharset(String filePath) {
        String lowerPath = filePath.toLowerCase();
        
        // HTML 文件
        if (lowerPath.endsWith(".html") || lowerPath.endsWith(".htm")) {
            return "text/html; charset=UTF-8";
        }
        
        // CSS 文件
        if (lowerPath.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        
        // JavaScript 文件
        if (lowerPath.endsWith(".js") || lowerPath.endsWith(".mjs")) {
            return "application/javascript; charset=UTF-8";
        }
        
        // TypeScript 文件
        if (lowerPath.endsWith(".ts")) {
            return "application/javascript; charset=UTF-8";
        }
        
        // JSON 文件
        if (lowerPath.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }
        
        // 图片文件
        if (lowerPath.endsWith(".png")) return "image/png";
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) return "image/jpeg";
        if (lowerPath.endsWith(".gif")) return "image/gif";
        if (lowerPath.endsWith(".svg")) return "image/svg+xml";
        if (lowerPath.endsWith(".ico")) return "image/x-icon";
        if (lowerPath.endsWith(".webp")) return "image/webp";
        
        // 字体文件
        if (lowerPath.endsWith(".woff")) return "font/woff";
        if (lowerPath.endsWith(".woff2")) return "font/woff2";
        if (lowerPath.endsWith(".ttf")) return "font/ttf";
        if (lowerPath.endsWith(".otf")) return "font/otf";
        if (lowerPath.endsWith(".eot")) return "application/vnd.ms-fontobject";
        
        // 其他常见文件类型
        if (lowerPath.endsWith(".xml")) return "application/xml; charset=UTF-8";
        if (lowerPath.endsWith(".txt")) return "text/plain; charset=UTF-8";
        if (lowerPath.endsWith(".pdf")) return "application/pdf";
        if (lowerPath.endsWith(".zip")) return "application/zip";
        
        // 默认类型
        return "application/octet-stream";
    }
}

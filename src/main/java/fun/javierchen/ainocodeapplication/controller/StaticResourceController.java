package fun.javierchen.ainocodeapplication.controller;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import fun.javierchen.ainocodeapplication.constant.CodeFileConstant;
import fun.javierchen.ainocodeapplication.model.entity.App;
import fun.javierchen.ainocodeapplication.model.entity.ChatHistory;
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

    /**
     * 预览应用最新版本
     * 访问格式：http://localhost:port/api/static/preview/{appId}[/{fileName}]
     */
    @GetMapping("/preview/{appId}/**")
    public ResponseEntity<org.springframework.core.io.Resource> previewLatestVersion(
            @PathVariable Long appId,
            HttpServletRequest request) {
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
     * 预览应用版本的通用方法
     */
    private ResponseEntity<org.springframework.core.io.Resource> previewAppVersion(
            Long appId, Integer version, HttpServletRequest request) {
        try {
            // 1. 验证应用是否存在
            App app = appService.getById(appId);
            if (app == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. 确定要预览的版本号
            int targetVersion;
            if (version != null && version > 0) {
                targetVersion = version;
            } else {
                // 获取最新版本
                targetVersion = getLatestCodeVersion(appId);
                if (targetVersion == 0) {
                    return ResponseEntity.notFound().build();
                }
            }

            // 3. 构建版本化的文件路径
            String codeGenType = app.getCodeGenType();
            String appDirName = codeGenType + "_" + appId;
            
            // 4. 获取资源路径
            String resourcePath = getResourcePathFromRequest(request, "/preview/" + appId + (version != null ? "/" + version : ""));
            
            // 5. 处理目录访问重定向
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            
            // 6. 默认返回 index.html
            if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }
            
            // 7. 构建完整文件路径
            String filePath = CodeFileConstant.CODE_FILE_PATH + File.separator + appDirName + File.separator + targetVersion + resourcePath;
            File file = new File(filePath);
            
            // 8. 检查文件是否存在
            if (!file.exists()) {
                log.warn("预览文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 9. 返回文件资源
            org.springframework.core.io.Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(filePath))
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("预览失败: appId={}, version={}, error={}", appId, version, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg")) return "image/jpeg";
        return "application/octet-stream";
    }
}

package fun.javierchen.ainocodeapplication.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import fun.javierchen.ainocodeapplication.ai.model.enums.CodeGenTypeEnum;
import fun.javierchen.ainocodeapplication.constant.AppConstant;
import fun.javierchen.ainocodeapplication.constant.CodeFileConstant;
import fun.javierchen.ainocodeapplication.constant.CommonConstant;
import fun.javierchen.ainocodeapplication.core.AiGenerateServiceFacade;
import fun.javierchen.ainocodeapplication.exceptiom.BusinessException;
import fun.javierchen.ainocodeapplication.exceptiom.ErrorCode;
import fun.javierchen.ainocodeapplication.mapper.AppMapper;
import fun.javierchen.ainocodeapplication.model.User;
import fun.javierchen.ainocodeapplication.model.entity.App;
import fun.javierchen.ainocodeapplication.model.dto.app.AppQueryRequest;
import fun.javierchen.ainocodeapplication.model.entity.ChatHistory;
import fun.javierchen.ainocodeapplication.model.vo.AppVO;
import fun.javierchen.ainocodeapplication.model.vo.AppVersionVO;
import fun.javierchen.ainocodeapplication.model.vo.UserVO;
import fun.javierchen.ainocodeapplication.service.AppService;
import fun.javierchen.ainocodeapplication.service.ChatHistoryService;
import fun.javierchen.ainocodeapplication.service.UserService;
import fun.javierchen.ainocodeapplication.utils.ThrowUtils;
import fun.javierchen.ainocodeapplication.utils.TemplateCopyUtil;
import fun.javierchen.ainocodeapplication.security.DirectorySandboxUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用服务实现类
 *
 * @author JavierChen
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiGenerateServiceFacade aiGenerateServiceFacade;

    @Lazy
    @Resource
    private ChatHistoryService chatHistoryService;

    @Override
    public void validApp(App app, boolean add) {
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String appName = app.getAppName();
        String initPrompt = app.getInitPrompt();
        String codeGenType = app.getCodeGenType();

        // 创建时，参数不能为空
        if (add) {
            if (StringUtils.isAnyBlank(appName, initPrompt)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称和初始化提示不能为空");
            }
            if (StringUtils.isBlank(codeGenType) && CodeGenTypeEnum.getEnumByType(codeGenType) == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
            }
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(appName) && appName.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称过长");
        }
        if (StringUtils.isNotBlank(initPrompt) && initPrompt.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用描述过长");
        }
        if (StringUtils.isNotBlank(codeGenType) && CodeGenTypeEnum.getEnumByType(codeGenType) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        }
    }

    @Override
    public Flux<String> chatGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByType(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }

        // 5. 完成基础的校验后 保存用户的历史记录
        chatHistoryService.saveUserMessage(appId, loginUser.getId(), message);

        // 6. 调用 AI 生成代码，使用带回调的方法避免重复解析
        StringBuilder aiGenResult = new StringBuilder();
        Flux<String> contentFlux = aiGenerateServiceFacade.generateAndSaveFileStream(
                message, 
                codeGenTypeEnum,
                appId,
                // 7. 使用回调保存AI对话，避免重复解析
                (parseResult, version) -> {
                    String result = aiGenResult.toString();
                    if (StrUtil.isNotBlank(result)) {
                        // 使用新的方法，传入解析结果而不是重复解析
                        chatHistoryService.saveAiMessageWithParseResult(
                                appId, 
                                loginUser.getId(), 
                                result, 
                                parseResult, 
                                null
                        );
                        log.info("保存AI对话历史成功，应用ID：{}，版本号：{}，是否包含代码：{}", 
                                appId, version, parseResult.isHasValidCode());
                    }
                },
                loginUser
        );
        
        return contentFlux.map(chunk -> {
                    aiGenResult.append(chunk);
                    return chunk;
                })
                .doOnError(
                        // 有错误也要保存到历史中
                        error -> {
                            String errorMessage = "【AI生成有误】" + error.getMessage();
                            // 错误情况下使用原方法，因为没有解析结果
                            chatHistoryService.saveAiMessage(appId, loginUser.getId(), 
                                    aiGenResult.toString(), false, errorMessage);
                        }
                );
    }


    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);

        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }

        return appVO;
    }

    @Override
    public List<AppVO> getAppVO(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }

        // 关联查询用户信息
        Set<Long> userIdSet = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        return appList.stream().map(app -> {
            AppVO appVO = new AppVO();
            BeanUtils.copyProperties(app, appVO);
            Long userId = app.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            appVO.setUser(userService.getUserVO(user));
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            return new QueryWrapper();
        }

        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String codeGenType = appQueryRequest.getCodeGenType();
        Long userId = appQueryRequest.getUserId();
        Integer priority = appQueryRequest.getPriority();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create().from("app");

        // 添加各种条件
        if (id != null && id > 0) {
            queryWrapper.eq("id", id);
        }
        if (StringUtils.isNotBlank(appName)) {
            queryWrapper.like("appName", appName);
        }
        if (StringUtils.isNotBlank(codeGenType)) {
            queryWrapper.eq("codeGenType", codeGenType);
        }
        if (userId != null && userId > 0) {
            queryWrapper.eq("userId", userId);
        }
        if (priority != null) {
            queryWrapper.eq("priority", priority);
        }

        // 默认按创建时间倒序
        if (StringUtils.isBlank(sortField)) {
            queryWrapper.orderBy("createTime", false);
        } else {
            // 排序
            if (CommonConstant.SORT_ORDER_ASC.equals(sortOrder)) {
                queryWrapper.orderBy(sortField, true);
            } else if (CommonConstant.SORT_ORDER_DESC.equals(sortOrder)) {
                queryWrapper.orderBy(sortField, false);
            }
        }

        return queryWrapper;
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        return deployApp(appId, loginUser, null); // 部署最新版本
    }

    @Override
    public String deployApp(Long appId, User loginUser, Integer version) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        
        // 4. 获取要部署的版本号
        int deployVersion;
        if (version != null && version > 0) {
            deployVersion = version;
        } else {
            // 获取最新版本
            deployVersion = getLatestCodeVersion(appId);
            if (deployVersion == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用尚未生成任何代码，请先生成代码");
            }
        }
        
        // 5. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        
        // 6. 获取代码生成类型，构建版本化的源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = CodeFileConstant.CODE_FILE_PATH + File.separator + sourceDirName + File.separator + deployVersion;
        
        // 7. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, 
                    String.format("版本 %d 的代码不存在，请检查版本号或重新生成代码", deployVersion));
        }
        
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            // 清理旧的部署文件
            File deployDir = new File(deployDirPath);
            if (deployDir.exists()) {
                FileUtil.del(deployDir);
            }
            // 复制新版本
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
            log.info("成功部署应用 {}，版本：{}，部署路径：{}", appId, deployVersion, deployDirPath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        
        // 9. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        
        // 10. 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

    /**
     * 获取应用的最新代码版本号
     * @param appId 应用ID
     * @return 最新版本号，如果没有代码则返回0
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

    @Override
    public boolean deleteAppAndChatHistory(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 验证用户是否有权限删除该应用，仅本人或管理员可以删除
        if (!app.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该应用");
        }

        // 4. 删除应用关联的对话历史
        boolean deleteChatHistoryResult = chatHistoryService.deleteChatHistoryByAppId(appId);
        if (!deleteChatHistoryResult) {
            log.warn("删除应用关联的对话历史失败，appId = {}", appId);
        }

        // 5. 删除应用
        return this.removeById(appId);
    }

    @Override
    public List<AppVersionVO> getAppVersions(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 验证权限（仅应用创建者可查看版本）
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的版本信息");
        }

        // 4. 查询所有代码版本的对话历史
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ChatHistory.class)
                .where(ChatHistory::getAppId).eq(appId)
                .and(ChatHistory::getIsCode).eq(1)
                .and(ChatHistory::getIsDelete).eq(0)
                .orderBy(ChatHistory::getCodeVersion, false); // 按版本号降序

        List<ChatHistory> codeHistories = chatHistoryService.list(queryWrapper);
        
        // 5. 转换为版本VO
        return codeHistories.stream()
                .map(history -> convertToVersionVO(history, app))
                .toList();
    }

    @Override
    public AppVersionVO getAppVersion(Long appId, Integer version, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(version == null || version <= 0, ErrorCode.PARAMS_ERROR, "版本号不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 验证权限
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的版本信息");
        }

        // 4. 查询特定版本的对话历史
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ChatHistory.class)
                .where(ChatHistory::getAppId).eq(appId)
                .and(ChatHistory::getCodeVersion).eq(version)
                .and(ChatHistory::getIsCode).eq(1)
                .and(ChatHistory::getIsDelete).eq(0);

        ChatHistory codeHistory = chatHistoryService.getOne(queryWrapper);
        ThrowUtils.throwIf(codeHistory == null, ErrorCode.NOT_FOUND_ERROR, "版本不存在");

        return convertToVersionVO(codeHistory, app);
    }

    @Override
    public boolean deleteAppVersion(Long appId, Integer version, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(version == null || version <= 0, ErrorCode.PARAMS_ERROR, "版本号不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 验证权限（仅应用创建者可删除版本）
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该应用的版本");
        }

        // 4. 检查是否是最后一个版本（至少保留一个版本）
        long versionCount = chatHistoryService.count(QueryWrapper.create()
                .from(ChatHistory.class)
                .where(ChatHistory::getAppId).eq(appId)
                .and(ChatHistory::getIsCode).eq(1)
                .and(ChatHistory::getIsDelete).eq(0));
        
        if (versionCount <= 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能删除最后一个版本");
        }

        // 5. 删除对话历史记录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ChatHistory.class)
                .where(ChatHistory::getAppId).eq(appId)
                .and(ChatHistory::getCodeVersion).eq(version)
                .and(ChatHistory::getIsCode).eq(1);

        ChatHistory updateEntity = new ChatHistory();
        updateEntity.setIsDelete(1);
        updateEntity.setUpdateTime(LocalDateTime.now());

        boolean historyDeleted = chatHistoryService.update(updateEntity, queryWrapper);

        // 6. 删除文件系统中的版本文件
        if (historyDeleted) {
            try {
                String codeGenType = app.getCodeGenType();
                String versionDirPath = CodeFileConstant.CODE_FILE_PATH + File.separator + 
                                       codeGenType + "_" + appId + File.separator + version;
                File versionDir = new File(versionDirPath);
                if (versionDir.exists()) {
                    FileUtil.del(versionDir);
                    log.info("删除版本文件成功：{}", versionDirPath);
                }
            } catch (Exception e) {
                log.warn("删除版本文件失败，但数据库记录已删除：{}", e.getMessage());
            }
        }

        return historyDeleted;
    }

    /**
     * 将ChatHistory转换为AppVersionVO
     */
    private AppVersionVO convertToVersionVO(ChatHistory codeHistory, App app) {
        AppVersionVO versionVO = new AppVersionVO();
        versionVO.setVersion(codeHistory.getCodeVersion());
        versionVO.setCreateTime(codeHistory.getCreateTime());
        versionVO.setChatHistoryId(codeHistory.getId());
        
        // 截取消息内容作为摘要（最多200字符）
        String message = codeHistory.getMessage();
        if (message != null && message.length() > 200) {
            message = message.substring(0, 200) + "...";
        }
        versionVO.setMessage(message);
        
        // 判断是否为当前部署版本
        versionVO.setIsDeployed(false); // 暂时设为false，可以后续优化
        
        // 设置预览URL
        versionVO.setPreviewUrl(String.format("/api/static/preview/%d/%d/", app.getId(), codeHistory.getCodeVersion()));
        
        // 计算文件大小
        try {
            String codeGenType = app.getCodeGenType();
            String versionDirPath = CodeFileConstant.CODE_FILE_PATH + File.separator + 
                                   codeGenType + "_" + app.getId() + File.separator + codeHistory.getCodeVersion();
            File versionDir = new File(versionDirPath);
            if (versionDir.exists()) {
                versionVO.setFileSize(calculateDirectorySize(versionDir));
            } else {
                versionVO.setFileSize(0L);
            }
        } catch (Exception e) {
            log.warn("计算版本文件大小失败：{}", e.getMessage());
            versionVO.setFileSize(0L);
        }
        
        return versionVO;
    }

    /**
     * 计算目录大小
     */
    private long calculateDirectorySize(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return 0L;
        }
        
        long size = 0L;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += calculateDirectorySize(file);
                }
            }
        }
        return size;
    }

    @Override
    public String copyVueTemplateWithSymlinks(String targetPath) {
        try {
            // 验证目标路径
            if (StringUtils.isBlank(targetPath)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "目标路径不能为空");
            }

            // 确保路径安全
            Path target = Paths.get(targetPath).toAbsolutePath().normalize();
            
            // 定义模板路径和共享node_modules路径
            Path templatePath = Paths.get("front/ai-no-code/template_vue").toAbsolutePath();
            Path sharedNodeModulesPath = Paths.get("front/ai-no-code/node_modules").toAbsolutePath();
            
            log.info("开始为Vue项目复制模板到: {}", target);
            log.debug("模板路径: {}", templatePath);
            log.debug("共享node_modules路径: {}", sharedNodeModulesPath);
            
            // 使用符号链接复制工具复制Vue模板
            TemplateCopyUtil.CopyResult result = TemplateCopyUtil.copyVueTemplateWithSharedNodeModules(
                templatePath.toString(),
                target.toString(),
                sharedNodeModulesPath.toString()
            );
            
            // 记录复制结果
            log.info("Vue模板复制完成: {}", result);
            
            // 返回成功信息
            return String.format("Vue项目模板复制成功！复制文件：%d个，目录：%d个，符号链接：%d个，耗时：%dms",
                result.getCopiedFiles(),
                result.getCopiedDirectories(), 
                result.getCreatedSymlinks(),
                result.getDuration()
            );
            
        } catch (IOException e) {
            log.error("Vue模板复制失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue模板复制失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Vue模板复制过程中发生未知错误: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue模板复制失败: " + e.getMessage());
        }
    }

    @Override
    public int getNextVersion(Long appId) {
        try {
            // 验证参数
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
            
            // 获取应用的沙箱根目录
            Path appSandboxRoot = DirectorySandboxUtil.getProjectDirectoryPath(appId);
            
            if (!Files.exists(appSandboxRoot)) {
                return 1; // 如果目录不存在，返回1作为第一个版本
            }
            
            // 查找最大的版本号
            int maxVersion = Files.list(appSandboxRoot)
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
                    
            return maxVersion + 1;
            
        } catch (IOException e) {
            log.error("获取下一个版本号失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取版本号失败: " + e.getMessage());
        }
    }

    @Override
    public File saveVueProject(Long appId, int version) {
        try {
            // 验证参数
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
            ThrowUtils.throwIf(version <= 0, ErrorCode.PARAMS_ERROR, "版本号必须大于0");
            
            // 获取应用的沙箱根目录
            Path appSandboxRoot = DirectorySandboxUtil.getProjectDirectoryPath(appId);
            
            // 在沙箱根目录下创建版本目录
            Path versionDirectory = appSandboxRoot.resolve("v" + version);
            
            // 创建Vue项目目录
            Path vueProjectDir = versionDirectory.resolve("vue-project");
            Files.createDirectories(vueProjectDir);
            
            // 使用Vue项目符号链接复制方法
            String result = copyVueTemplateWithSymlinks(vueProjectDir.toString());
            
            log.info("Vue项目目录已创建: {} (应用ID: {}, 版本: {}), 结果: {}", 
                    vueProjectDir.toAbsolutePath(), appId, version, result);
            
            // 返回项目根目录
            return vueProjectDir.toFile();

        } catch (IOException e) {
            log.error("保存Vue项目失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存Vue项目失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("保存Vue项目过程中发生未知错误: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存Vue项目失败: " + e.getMessage());
        }
    }
}

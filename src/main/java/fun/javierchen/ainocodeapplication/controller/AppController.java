package fun.javierchen.ainocodeapplication.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import fun.javierchen.ainocodeapplication.annotation.AuthCheck;
import fun.javierchen.ainocodeapplication.common.BaseResponse;
import fun.javierchen.ainocodeapplication.constant.UserConstant;
import fun.javierchen.ainocodeapplication.exceptiom.BusinessException;
import fun.javierchen.ainocodeapplication.exceptiom.ErrorCode;
import fun.javierchen.ainocodeapplication.model.User;
import fun.javierchen.ainocodeapplication.model.entity.App;
import fun.javierchen.ainocodeapplication.model.dto.app.*;
import fun.javierchen.ainocodeapplication.model.vo.AppVO;
import fun.javierchen.ainocodeapplication.service.AppService;
import fun.javierchen.ainocodeapplication.service.UserService;
import fun.javierchen.ainocodeapplication.utils.ResultUtils;
import fun.javierchen.ainocodeapplication.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用 控制层。
 *
 * @author 16010
 * @since 2025-09-28
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    // region 用户功能

    /**
     * 创建应用
     *
     * @param appAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        if (appAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = new App();
        BeanUtils.copyProperties(appAddRequest, app);
        appService.validApp(app, true);

        User loginUser = userService.getLoginUser(request);
        app.setUserId(loginUser.getId());
        app.setCreateTime(LocalDateTime.now());
        app.setUpdateTime(LocalDateTime.now());
        app.setEditTime(LocalDateTime.now());
        app.setIsDelete(0);

        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

    /**
     * 删除自己的应用
     *
     * @param deleteId
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteMyApp(@RequestParam Long deleteId, HttpServletRequest request) {
        if (deleteId == null || deleteId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        // 判断是否存在
        App oldApp = appService.getById(deleteId);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);

        // 仅本人或管理员可删除
        if (!oldApp.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        boolean b = appService.removeById(deleteId);
        return ResultUtils.success(b);
    }

    /**
     * 更新自己的应用（仅支持修改应用名称）
     *
     * @param appUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyApp(@RequestBody AppUpdateMyRequest appUpdateMyRequest,
                                             HttpServletRequest request) {
        if (appUpdateMyRequest == null || appUpdateMyRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = appUpdateMyRequest.getId();

        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);

        // 仅本人可修改
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        App app = new App();
        BeanUtils.copyProperties(appUpdateMyRequest, app);
        app.setEditTime(LocalDateTime.now());
        app.setUpdateTime(LocalDateTime.now());

        // 参数校验
        appService.validApp(app, false);

        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取应用详情
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = appService.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 分页获取自己的应用列表
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest,
                                                       HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        appQueryRequest.setUserId(loginUser.getId());
        long current = appQueryRequest.getCurrent();
        long size = appQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<App> appPage = appService.page(new Page<>(current, size),
                appService.getQueryWrapper(appQueryRequest));
        return getAppVOPage(appPage, request);
    }

    /**
     * 分页获取精选应用列表（优先级大于0的应用）
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/featured/list/page/vo")
    public BaseResponse<Page<AppVO>> listFeaturedAppVOByPage(@RequestBody AppQueryRequest appQueryRequest,
                                                             HttpServletRequest request) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = appQueryRequest.getCurrent();
        long size = appQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 查询优先级大于0的应用（精选应用）
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        queryWrapper.gt("priority", 0);

        Page<App> appPage = appService.page(new Page<>(current, size), queryWrapper);
        return getAppVOPage(appPage, request);
    }

    // endregion

    // region 管理员功能

    /**
     * 管理员删除应用
     *
     * @param deleteId
     * @param request
     * @return
     */
    @PostMapping("/delete/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteApp(@RequestParam Long deleteId, HttpServletRequest request) {
        if (deleteId == null || deleteId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = appService.removeById(deleteId);
        return ResultUtils.success(b);
    }

    /**
     * 管理员更新应用
     *
     * @param appUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest,
                                           HttpServletRequest request) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = new App();
        BeanUtils.copyProperties(appUpdateRequest, app);
        app.setEditTime(LocalDateTime.now());
        app.setUpdateTime(LocalDateTime.now());

        // 参数校验
        appService.validApp(app, false);

        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员根据 id 获取应用（包含详细信息）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<App> getAppById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(app);
    }

    /**
     * 管理员分页获取应用列表
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<App>> listAppByPage(@RequestBody AppQueryRequest appQueryRequest,
                                                 HttpServletRequest request) {
        long current = appQueryRequest.getCurrent();
        long size = appQueryRequest.getPageSize();
        Page<App> appPage = appService.page(new Page<>(current, size),
                appService.getQueryWrapper(appQueryRequest));
        return ResultUtils.success(appPage);
    }

    // endregion

    /**
     * 封装获取 AppVO Page 的逻辑
     */
    private BaseResponse<Page<AppVO>> getAppVOPage(Page<App> appPage, HttpServletRequest request) {
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        List<AppVO> appVO = appService.getAppVO(appPage.getRecords());
        appVOPage.setRecords(appVO);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 聊天生成代码
     *
     * @param appId
     * @param message
     * @return
     */
    @GetMapping(value = "chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatGenCode(@RequestParam Long appId,
                                    @RequestParam String message,
                                    HttpServletRequest request) {

        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        User loginUser = userService.getLoginUser(request);
        return appService.chatGenCode(appId, message, loginUser);
    }

}

package fun.javierchen.ainocodeapplication.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import fun.javierchen.ainocodeapplication.model.User;
import fun.javierchen.ainocodeapplication.model.entity.App;
import fun.javierchen.ainocodeapplication.model.dto.app.AppQueryRequest;
import fun.javierchen.ainocodeapplication.model.vo.AppVO;
import fun.javierchen.ainocodeapplication.model.vo.AppVersionVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author 16010
 * @since 2025-09-28
 */
public interface AppService extends IService<App> {

    /**
     * 获取脱敏的应用信息
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏的应用信息
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVO(List<App> appList);

    /**
     * 获取 QueryWrapper
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 校验应用
     */
    void validApp(App app, boolean add);

    /**
     * 根据提示词生成代码
     */
    Flux<String> chatGenCode(Long appId, String message, User loginUser);

    /**
     * 用户部署 APP（部署最新版本）
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 用户部署 APP（部署指定版本）
     */
    String deployApp(Long appId, User loginUser, Integer version);

    /**
     * 删除应用时，同时删除关联的对话历史
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    boolean deleteAppAndChatHistory(Long appId, User loginUser);

    /**
     * 获取应用的所有版本列表
     *
     * @param appId 应用ID
     * @param loginUser 当前用户
     * @return 版本列表
     */
    List<AppVersionVO> getAppVersions(Long appId, User loginUser);

    /**
     * 获取应用特定版本的详情
     *
     * @param appId 应用ID
     * @param version 版本号
     * @param loginUser 当前用户
     * @return 版本详情
     */
    AppVersionVO getAppVersion(Long appId, Integer version, User loginUser);

    /**
     * 删除应用的特定版本
     *
     * @param appId 应用ID
     * @param version 版本号
     * @param loginUser 当前用户
     * @return 是否删除成功
     */
    boolean deleteAppVersion(Long appId, Integer version, User loginUser);
}

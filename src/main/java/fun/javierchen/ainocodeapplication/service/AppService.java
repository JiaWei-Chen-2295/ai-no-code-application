package fun.javierchen.ainocodeapplication.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import fun.javierchen.ainocodeapplication.model.entity.App;
import fun.javierchen.ainocodeapplication.model.dto.app.AppQueryRequest;
import fun.javierchen.ainocodeapplication.model.vo.AppVO;

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

}

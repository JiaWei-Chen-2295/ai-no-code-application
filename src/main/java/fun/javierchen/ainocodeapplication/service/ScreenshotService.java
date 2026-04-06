package fun.javierchen.ainocodeapplication.service;

import java.io.File;

/**
 * 截图服务接口，允许后续替换实现（本地浏览器 → 外部API → OSS服务）
 */
public interface ScreenshotService {

    /**
     * 对指定URL进行截图并保存到outputFile
     *
     * @param url        要截图的页面URL
     * @param outputFile 输出文件
     */
    void captureScreenshot(String url, File outputFile);
}

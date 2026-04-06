package fun.javierchen.ainocodeapplication.service.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import fun.javierchen.ainocodeapplication.service.ScreenshotService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 基于 Playwright 的截图服务实现
 * 使用 bundled Chromium 进行无头浏览器截图
 */
@Service
@Slf4j
public class PlaywrightScreenshotService implements ScreenshotService {

    private Playwright playwright;
    private Browser browser;

    private synchronized void ensureBrowserInitialized() {
        if (browser == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );
        }
    }

    @Override
    public void captureScreenshot(String url, File outputFile) {
        ensureBrowserInitialized();
        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setViewportSize(1280, 720)
        );
        Page page = context.newPage();
        try {
            page.navigate(url, new Page.NavigateOptions()
                    .setTimeout(10000)
                    .setWaitUntil(WaitUntilState.NETWORKIDLE));
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(outputFile.toPath())
                    .setFullPage(false));
        } finally {
            page.close();
            context.close();
        }
    }

    @PreDestroy
    public void cleanup() {
        if (browser != null) {
            try {
                browser.close();
            } catch (Exception e) {
                log.warn("Failed to close Playwright browser: {}", e.getMessage());
            }
        }
        if (playwright != null) {
            try {
                playwright.close();
            } catch (Exception e) {
                log.warn("Failed to close Playwright: {}", e.getMessage());
            }
        }
    }
}

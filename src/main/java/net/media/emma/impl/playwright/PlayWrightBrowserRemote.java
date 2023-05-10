package net.media.emma.impl.playwright;

import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import net.media.emma.service.BrowserRemote;

public class PlayWrightBrowserRemote implements BrowserRemote {

    public void openUrl(String url){

        // playwright = Playwright.create();

        // browserType = playwright.chromium();
        // browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));

        Browser browser = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        Page page = browser.newPage();
        page.navigate(url);
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
    }

    @Override
    public void mockRequest() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mockRequest'");
    }

    @Override
    public void mockResponse() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mockResponse'");
    }

    @Override
    public void injectScript(String script) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'injectScript'");
    }

    @Override
    public void injectDom(String xPath, Boolean append) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'injectDom'");
    }
    
}

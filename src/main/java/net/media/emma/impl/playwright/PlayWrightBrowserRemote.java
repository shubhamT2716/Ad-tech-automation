package net.media.emma.impl.playwright;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import net.media.emma.service.BrowserRemote;

public class PlayWrightBrowserRemote implements BrowserRemote {

    // Browser browser = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    Page page = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newPage();
    @Autowired Logger logger;

    public void openUrl(String url){

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
    public void injectDom(String xPath, String htmlElement, Boolean append) {

        logger.info("sadf");

        page.onResponse(
           response -> {

                System.out.println(response.body());
            }
        );
     
    }
    
}

package net.media.emma.impl.playwright;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;

import net.media.emma.service.BrowserRemote;

public class PlayWrightBrowserRemote implements BrowserRemote {

    // Browser browser = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
  
    @Autowired Logger logger;

    public void openUrl(String url){
        Page page = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newPage();

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



    // My code

// ---------------------------------------------------------------------------------------------
// Working only on https://www.engadget.com/?headerbidding=1

    @Override
    public void injectDom(String url, String xPath, String htmlElement, Boolean append) {


        Page page = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newPage();


        page.route(url + "/**",  route -> {

            logger.info(url);
            logger.info(xPath);

            String filePath = "src/main/resources/templates/domInject.html";

            Path path = Paths.get(filePath);

            route.fulfill(new Route.FulfillOptions().setPath(path));
            
        });

        page.navigate(url);
        //page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
     
    }
    

    // ------------------------------------------------My code end ------------------------------------------------------------------------
}

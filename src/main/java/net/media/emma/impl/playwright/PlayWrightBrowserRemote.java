package net.media.emma.impl.playwright;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.management.RuntimeErrorException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;

import ch.qos.logback.core.joran.conditional.ElseAction;
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
                        // To append or override a creatin xpath with our html
                        // v1.1-latest


    @Override
    public void injectDom(String url, String xPath, String forceHtmlElement, Boolean append) {

        logger.info("Inside Dom Inject");
        Page page = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newPage();
        logger.info(" FInished Launching Browser");

        page.route(url,  route -> {
            String fileContent = null;

            logger.info("Starting route");

            APIResponse apiResponse = route.fetch();

            String pageContent = new String (apiResponse.body());

            logger.info("old response body is "+ pageContent);
            logger.info("Ended old response body");

            try {
                fileContent = new String(Files.readAllBytes(Paths.get(forceHtmlElement)));

                logger.info("filecontent to append/overirde is "+ fileContent);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            

            Document document = Jsoup.parse(pageContent);
            if (append==false){
                logger.info("Inside If case");
                document.select(xPath).html(fileContent);
            }
            else{
                document.select(xPath).append(fileContent);

            }
            String modifiedPageContent = document.html();

            logger.info("Modified Page Content is " + modifiedPageContent);

            route.fulfill(new Route.FulfillOptions().setBody(modifiedPageContent));

            logger.info("Finished Dom Injection");




            // To override the entire html page via our html code.
            // v1.0


            // // logger.info(url);
            // // logger.info(xPath);


            // String filePath = "src/main/resources/templates/domInject.html";

            // Path path = Paths.get(filePath);

            // route.fulfill(new Route.FulfillOptions().setPath(path));
            
        });

        page.navigate(url);
        //page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
     
    }

    
    
}
    

    // ------------------------------------------------My code end ------------------------------------------------------------------------


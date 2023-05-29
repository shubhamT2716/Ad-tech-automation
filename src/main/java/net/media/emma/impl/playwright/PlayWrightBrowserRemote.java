package net.media.emma.impl.playwright;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.service.BrowserRemote;


@Slf4j
public class PlayWrightBrowserRemote implements BrowserRemote {

    // Browser browser = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
  
    // @Autowired Logger logger;
    String jsExtraction = "";

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

        log.info("Inside Dom Inject");
        Page page = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newPage();
        log.info(" FInished Launching Browser");

        page.route(url,  route -> {
            String fileContent = null;

            log.info("Starting route");

            APIResponse apiResponse = route.fetch();

            String pageContent = new String (apiResponse.body());

            log.info("old response body is "+ pageContent);
            log.info("Ended old response body");

            try {
                fileContent = new String(Files.readAllBytes(Paths.get(forceHtmlElement)));

                log.info("filecontent to append/overirde is "+ fileContent);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            

            Document document = Jsoup.parse(pageContent);
            if (append==false){
                log.info("Inside If case");
                document.select(xPath).html(fileContent);
            }
            else{
                document.select(xPath).append(fileContent);

            }
            String modifiedPageContent = document.html();

            log.info("Modified Page Content is " + modifiedPageContent);

            route.fulfill(new Route.FulfillOptions().setBody(modifiedPageContent));

            log.info("Finished Dom Injection");




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


    //Scraping pbjs 


    @Override
    public String scrapeContent(String url){

        log.info("inside func");
        try {
            

            Page page = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newPage();


            // page.setDefaultTimeout(1000000);

            page.onConsoleMessage(consoleMessage -> {
                log.info("Console message: " + consoleMessage.text());
                jsExtraction = consoleMessage.text();
            });
            page.navigate(url);

            

            String scrapeJsContent = """
                console.log(JSON.stringify(_apPbJs, null, 4));
                    """;
            page.waitForTimeout(2000);
            JSHandle content= page.evaluateHandle(scrapeJsContent);
           log.info(content.toString());

            return jsExtraction;

        } catch (PlaywrightException  e) {
                e.printStackTrace();
            // TODO: handle exception
        }

        return "Error";
        
    }
    
    
}
    

    // ------------------------------------------------My code end ------------------------------------------------------------------------


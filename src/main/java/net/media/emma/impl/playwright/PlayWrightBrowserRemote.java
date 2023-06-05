package net.media.emma.impl.playwright;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.microsoft.playwright.*;

import com.microsoft.playwright.options.Cookie;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.impl.playwright.network.PlaywrightMockRequest;
import net.media.emma.impl.playwright.network.PlaywrightMockResponse;
import net.media.emma.impl.playwright.network.RequestUrls;
import net.media.emma.service.BrowserRemote;


@Slf4j
public class PlayWrightBrowserRemote implements BrowserRemote {

    // Browser browser = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
  
    // @Autowired Logger logger;
    String jsExtraction = "";

    private HashMap<String, Browser> browserPool = new HashMap<String, Browser>();
    static Playwright playwright;
    static BrowserType browserType;
    static Browser browser;
    static BrowserContext browserContext;
    static Page page;

    public void openMultiBrowser(String url1, String url2) {

        browserPool.get("chrome.desktop").newPage().navigate(url1);
        browserPool.get("firefox.desktop").newPage().navigate(url2);
    }

    @PostConstruct
    public void loadBrowsers() {

        browserPool.put("chrome.desktop", launchBrowserWithContext("chrome.desktop"));
        browserPool.put("firefox.desktop", launchBrowserWithContext("firefox.desktop"));
    }

    private Browser launchBrowserWithContext(String context) {

        if (context.equals("chrome.desktop"))
            return Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

        if (context.equals("firefox.desktop"))
            return Playwright.create().firefox().launch(new BrowserType.LaunchOptions().setHeadless(false));

        return null;
    }

    @Override
    public boolean mockRequest(String urlPattern, String replaceHostName, Set<String> bidExHosts) {
        return new PlaywrightMockRequest(page).mockRequest(urlPattern, replaceHostName, bidExHosts);
    }

    @Override
    public void mockResponse(String urlPattern, String jsonPath, String newValue) {
        new PlaywrightMockResponse(page).mockResponse(urlPattern, jsonPath, newValue);
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

        // log.info("Inside Dom Inject");
        Page page = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newPage();
        // log.info(" FInished Launching Browser");

        page.route(url,  route -> {
            String fileContent = null;

            // log.info("Starting route");

            APIResponse apiResponse = route.fetch();

            String pageContent = new String (apiResponse.body());

            // log.info("old response body is "+ pageContent);
            // log.info("Ended old response body");

            try {
                fileContent = new String(Files.readAllBytes(Paths.get(forceHtmlElement)));

                log.info("filecontent to append/overirde is "+ fileContent);
            } catch (Exception e) {
                // throw new RuntimeException(e);
                log.error("Dom inject error", e);
            }
            

            Document document = Jsoup.parse(pageContent);
            if (append==false){
                // log.info("Inside If case");
                document.select(xPath).html(fileContent);
            }
            else{
                document.select(xPath).append(fileContent);

            }
            String modifiedPageContent = document.html();

            // log.info("Modified Page Content is " + modifiedPageContent);

            route.fulfill(new Route.FulfillOptions().setBody(modifiedPageContent));

            // log.info("Finished Dom Injection");




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

        // log.info("inside func");
        try {
            

            Page page = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newPage();


            // page.setDefaultTimeout(1000000);

            page.onConsoleMessage(consoleMessage -> {
                // log.info("Console message: " + consoleMessage.text());
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
        }

        return "Error";
        
    }

    @Override
    public Playwright createPlaywrightInstance() {
        playwright = Playwright.create();
        return playwright;
    }

    @Override
    public BrowserType initializeBrowser(String userAgent) {
        switch (userAgent.split("\\.")[0]) {
            case "chrome":
            case "edge":
                browserType = playwright.chromium();
                break;
            case "firefox":
                browserType = playwright.firefox();
                break;
        }
        return browserType;
    }

    @Override
    public Browser launchBrowser(String userAgent) {
        switch (userAgent.split("\\.")[0]) {
            case "edge":
                browser = browserType.launch(new BrowserType.LaunchOptions().setChannel("msedge").setHeadless(false));
                break;
            default:
                browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));
        }
        return browser;
    }

    @Override
    public BrowserContext setBrowserContext(String userAgent, List<Cookie> cookie) {
        switch (userAgent.split("\\.")[1]) {
            case "mobile":
                browserContext = browser.newContext(new Browser.NewContextOptions().setIsMobile(true).setViewportSize(360, 640));
                browserContext.addCookies(cookie);
                break;
            default:
                browserContext = browser.newContext(new Browser.NewContextOptions());
                browserContext.addCookies(cookie);
                break;
        }
        return browserContext;
    }

    @Override
    public Page initiatePage() {
        page = browserContext.newPage();
        return page;
    }

    @Override
    public void openUrl(String url) {
        page.navigate(url);
    }

    @Override
    public Page getPage(){
        return page;
    }

    @Override
    public List<String> getRequestUrls(String reqUrl) {
        return new RequestUrls(page).getRequestUrls(reqUrl);
    }

    public BrowserContext getBrowserContext(){
        return browserContext;
    }

    @Override
    public List<String> verifyConsoleError(String errorFilePath) {
        try {
            System.out.println("eorororor");
            List<String> consoleErrorUrl = new ArrayList<>();
            page.onConsoleMessage(consoleMessage ->
            {
                if (consoleMessage.type().equals("error") && consoleMessage.location().contains(errorFilePath)) {
                    consoleErrorUrl.add(consoleMessage.location());
                }
            });
            return consoleErrorUrl;
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getURLAnalyticsFor(String url, String[] urlPatterns) {

        Browser browser = Playwright.create().chromium().launch(
                    new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    );

        //Hash Map of useragent to useragent-string
        HashMap<String, String> ua = new HashMap<>(){{
            put("catchpoint", "Mozilla/5.0 (X11; Linux x86_64; Catchpoint) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
            put("google-bot", "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/W.X.Y.Z Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
            put("iPhone", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_5_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 EdgiOS/46.3.30 Mobile/15E148 Safari/605.1.15");
            put("iPad", "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148");
            put("rubbish", "Googlebot/2.1 (+http://www.google.com/bot.html)");
        }};

        BrowserContext newBC = browser.newContext(new Browser.NewContextOptions().setUserAgent(ua.get("rubbish")));
        Page page = newBC.newPage();
        //set user agent

        //list of arrays initialization
        List<String> urlFromNetworkTab = new ArrayList<>();

        page.onRequest(request -> {

            if (isUrlPatternMatched( request.url(), urlPatterns)){
                urlFromNetworkTab.add(request.url());
            }
        });

        page.navigate(url);
        // log.info(urlFromNetworkTab.toString());
        return urlFromNetworkTab;
    }

    private Boolean isUrlPatternMatched(String url, String[] urlPatterns){

        for(String urlPattern : urlPatterns){

            if(url.startsWith(urlPattern))
                return true;
        }

        return false;
    }

}
    

    // ------------------------------------------------My code end ------------------------------------------------------------------------


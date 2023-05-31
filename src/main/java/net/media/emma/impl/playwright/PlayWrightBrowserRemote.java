package net.media.emma.impl.playwright;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.testng.internal.junit.ArrayAsserts;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.service.BrowserRemote;

@Slf4j
public class PlayWrightBrowserRemote implements BrowserRemote {

    private HashMap<String, Browser> browserPool = new HashMap<String, Browser>();

    public void openMultiBrowser(String url1, String url2) {

        browserPool.get("chrome.desktop").newPage().navigate(url1);
        browserPool.get("firefox.desktop").newPage().navigate(url2);
    }

    @PostConstruct
    public void loadBrowsers(){

        browserPool.put("chrome.desktop", launchBrowserWithContext("chrome.desktop"));
        browserPool.put("firefox.desktop", launchBrowserWithContext("firefox.desktop"));
    }

    private Browser launchBrowserWithContext(String context){

        if(context.equals("chrome.desktop"))
            return Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

        if(context.equals("firefox.desktop"))
            return Playwright.create().firefox() .launch(new BrowserType.LaunchOptions().setHeadless(false));

        return null;
    }

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
    public void injectDom(String xPath, String htmlElement, Boolean append) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'injectDom'");
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
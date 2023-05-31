package net.media.emma.impl.playwright;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.microsoft.playwright.*;

import com.microsoft.playwright.options.Cookie;
import jakarta.annotation.PostConstruct;
import net.media.emma.impl.playwright.network.PlaywrightMockRequest;
import net.media.emma.impl.playwright.network.PlaywrightMockResponse;
import net.media.emma.impl.playwright.network.RequestUrls;
import net.media.emma.service.BrowserRemote;

public class PlayWrightBrowserRemote implements BrowserRemote {

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

    @Override
    public void injectDom(String xPath, String htmlElement, Boolean append) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'injectDom'");
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

}

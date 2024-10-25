package net.media.emma.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface BrowserRemote {

    boolean mockRequest(String urlPattern, String replaceHostName, Set<String> bidExHosts);

    void mockResponse(String urlPattern, String jsonPath, String newValue);

    void injectScript(String script);

    void injectDom(String url, String xPath, String htmlElement, Boolean append);

    String scrapeContent(String url);

    Playwright createPlaywrightInstance();

    BrowserType initializeBrowser(String userAgent);

    Browser launchBrowser(String userAgent);

    BrowserContext setBrowserContext(String userAgent, List<Cookie> cookie);

    Page initiatePage();

    void openUrl(String url);

    Page getPage();

    List<String> getRequestUrls(String reqUrl);

    List<String> verifyConsoleError(String errorFilePath);

    @Async("browserPool")
    void openMultiBrowser(String url1, String url2);

    List<String> getURLAnalyticsFor(String url, String[] urlPatterns);

}

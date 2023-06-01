package net.media.emma.service;


import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface BrowserRemote {

    public boolean mockRequest(String urlPattern, String replaceHostName, Set<String> bidExHosts );
    public void mockResponse(String urlPattern, String jsonPath, String newValue);

    public void injectScript(String script);
    public void injectDom(String xPath, String htmlElement, Boolean append);

    public Playwright createPlaywrightInstance();
    public BrowserType initializeBrowser(String userAgent);
    public Browser launchBrowser(String userAgent);
    public BrowserContext setBrowserContext(String userAgent, List<Cookie> cookie);
    public Page initiatePage();
    public void openUrl(String url);
    public Page getPage();
    public List<String> getRequestUrls(String reqUrl);
    public List<String> verifyConsoleError(String errorFilePath);

    @Async("browserPool")
    public void openMultiBrowser(String url1, String url2);

    public List<String> getURLAnalyticsFor(String url, String[] urlPatterns);

}

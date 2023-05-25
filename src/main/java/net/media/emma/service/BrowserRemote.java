package net.media.emma.service;


import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public interface BrowserRemote {

    public void openUrl(String url);

    public void mockRequest();
    public void mockResponse();

    public void injectScript(String script);
    public void injectDom(String xPath, String htmlElement, Boolean append);

    @Async("browserPool")
    public void openMultiBrowser(String url1, String url2);

    public List<String> getURLAnalyticsFor(String url, String[] urlPatterns);

}

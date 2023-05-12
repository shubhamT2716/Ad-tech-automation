package net.media.emma.service;


import org.springframework.stereotype.Service;

@Service
public interface BrowserRemote {

    public void openUrl(String url);

    public void mockRequest();
    public void mockResponse();

    public void injectScript(String script);
    public void injectDom(String url, String xPath, String htmlElement, Boolean append);

}

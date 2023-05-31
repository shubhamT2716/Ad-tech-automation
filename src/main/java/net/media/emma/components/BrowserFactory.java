package net.media.emma.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.options.HarMode;

public class BrowserFactory {

    private Playwright playwright;
    
    //Map of Browser Type with Device and User Agents
    private Map<String, String> browserContexts = new HashMap<String, String>() {{
        put("Chrome.Mobile.iPhone", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/114.0.5735.50 Mobile/15E148 Safari/604.1");
        put("Chrome.Desktop.Mac", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36");
    }};

    private static NewContextOptions getCommonBrowserContext(String userAgent, Boolean isMobile) {

        NewContextOptions newContextOptions = new NewContextOptions()
            .setUserAgent(userAgent)
            // .setRecordHarPath(Paths.get(GCPConfig.getHARFilePath()))
            .setRecordHarMode(HarMode.FULL)
            .setIgnoreHTTPSErrors(true);

        if(isMobile){
            newContextOptions
                .setViewportSize(360, 640)
                .setIsMobile(true)
                .setHasTouch(true);
        }

        return newContextOptions;
    }

    private LaunchOptions getBrowserLaunchOptions(String channel){
        LaunchOptions lo = new LaunchOptions().setHeadless(false);
        
        if(channel.length() > 0)
            lo.setChannel(channel);

        return lo;
    }

    private Browser getBrowserByType(String browserType){

        if(browserType.contains("chrome")){
            return playwright.chromium().launch(getBrowserLaunchOptions("chrome"));
        }

        if(browserType.contains("firefox")){
            return playwright.firefox().launch(getBrowserLaunchOptions(""));
        }

        if(browserType.contains("edge")){
            return playwright.chromium().launch(getBrowserLaunchOptions("msedge"));
        }

        return playwright.webkit().launch(getBrowserLaunchOptions(""));
    }

    //Returns Playwright's Page obj with given browserType and cookies
    // @Bean(name = "getPageWithNewContext")
    // @Scope("prototype")
    public Page getPageWithNewContext(String browserType, List<Map<String, String>> cookies) {

        return getBrowserByType(browserType)
            .newContext(getCommonBrowserContext(browserContexts.get(browserType), browserType.contains("Mobile")))
            // .addCookies(cookies)
            .newPage();
        
    }
}
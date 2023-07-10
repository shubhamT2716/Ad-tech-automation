package net.media.emma.cop.components;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.playwright.*;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.options.HarMode;
import org.springframework.beans.factory.annotation.Value;

public class BrowserFactory {

    private Playwright playwright = Playwright.create();

    @Value("${browserPath}")
    private static String chromeBrowserPath;

    //Map of Browser Type with Device and User Agents
    private static Map<String, String> browserContexts = new HashMap<String, String>() {{
        put("Chrome.Mobile.iPhone", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/114.0.5735.50 Mobile/15E148 Safari/604.1");
        put("Chrome.Desktop.Mac", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36");
        put("Chrome.Desktop.Catchpoint", "Mozilla/5.0 (X11; Linux x86_64; Catchpoint) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        put("Safari.Mobile.google-bot", "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/W.X.Y.Z Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
        put("Webkit.Tablet.iPad", "Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148");
        put("rubbish", "Googlebot/2.1 (+http://www.google.com/bot.html)");
        }};

    private static String getFileNameForHar(){
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());
        String random = String.valueOf((int)(Math.random() * 10000));
        return "emma-working-directory/har-files/generated-har-file-" + timeStamp + "-" + random + ".har";
    }

    private static NewContextOptions getCommonBrowserContext(String userAgent, Boolean isMobile) {

        NewContextOptions newContextOptions = new NewContextOptions()
            .setUserAgent(userAgent)
            .setRecordHarPath(Paths.get(getFileNameForHar()))
            .setRecordHarMode(HarMode.FULL)
            .setIgnoreHTTPSErrors(true);
            // .setStorageStatePath(Paths.get("emma-working-directory/browser-context-storage/state.json"));

        if(isMobile){
            newContextOptions
                .setViewportSize(360, 640)
                .setIsMobile(true)
                .setHasTouch(true);
        }

        return newContextOptions;
    }

    private static BrowserType.LaunchPersistentContextOptions getSystemBrowserContextOptions(String userAgent, Boolean isMobile, List<String> extension) {

        BrowserType.LaunchPersistentContextOptions newContextOptions = new BrowserType.LaunchPersistentContextOptions()
                .setUserAgent(userAgent)
                .setRecordHarPath(Paths.get(getFileNameForHar()))
                .setRecordHarMode(HarMode.FULL)
                .setIgnoreHTTPSErrors(true).setHeadless(false).setArgs(extension);

        if(isMobile){
            newContextOptions
                    .setViewportSize(360, 640)
                    .setIsMobile(true)
                    .setHasTouch(true);
        }

        return newContextOptions;
    }

    private static LaunchOptions getBrowserLaunchOptions(String channel){
        LaunchOptions lo = new LaunchOptions().setHeadless(false);

        if(channel.length() > 0)
            lo.setChannel(channel);

        return lo;
    }

    private static Playwright getPlaywrightObj(){
        // return playwright;
        return Playwright.create();
    }

    private static Browser getBrowserByType(String browserType){

        if(browserType.contains("Chrome")){
            return getPlaywrightObj().chromium().launch(getBrowserLaunchOptions("chrome"));
        }

        if(browserType.contains("Firefox")){
            return getPlaywrightObj().firefox().launch();
        }

        if(browserType.contains("Edge")){
            return getPlaywrightObj().chromium().launch(getBrowserLaunchOptions("msedge"));
        }

        return getPlaywrightObj().webkit().launch();
    }

    private static BrowserContext getSystemBrowserByType(String browserType,List<Map<String, String>> cookies, List<String> extension) {
        if(browserType.contains("Chrome")){
            return getPlaywrightObj().chromium().launchPersistentContext(Paths.get(chromeBrowserPath),getSystemBrowserContextOptions(browserContexts.get(browserType),browserType.contains("Mobile"),extension));
        }

        return getPlaywrightObj().webkit().launchPersistentContext(Paths.get(chromeBrowserPath));
    }

    //Returns Playwright's Page obj with given browserType and cookies
    // @Bean(name = "getPageWithNewContext")
    // @Scope("prototype")
    public static Page getPageWithNewContext(String browserType, List<Map<String, String>> cookies) {

        BrowserContext newBc =  getBrowserByType(browserType).newContext(
                                        getCommonBrowserContext(browserContexts.get(browserType), browserType.contains("Mobile")));
        newBc.setDefaultNavigationTimeout(120000);
            // .addCookies(cookies)
        return newBc.newPage();
    }

    public static BrowserContext getBc(String browserType, List<Map<String, String>> cookies) {

        BrowserContext newBc =  getBrowserByType(browserType).newContext(
                                        getCommonBrowserContext(browserContexts.get(browserType), browserType.contains("Mobile")));

        BrowserContext.StorageStateOptions storageStateOptions = new BrowserContext.StorageStateOptions();
        newBc.storageState(storageStateOptions.setPath(Paths.get("emma-working-directory/browser-context-storage/state.json")));

        newBc.setDefaultNavigationTimeout(120000);

        return newBc;
    }

    public static BrowserContext getSystemBc(String browserType, List<Map<String, String>> cookies, List<String> extension) {
        BrowserContext newBc = getSystemBrowserByType(browserType,cookies,extension);
        return newBc;
    }

    public static BrowserContext getNewBrowserContext(String browserType, List<Map<String, String>> cookies) {

        BrowserContext newBc =  getBrowserByType(browserType).newContext(
                                        getCommonBrowserContext(browserContexts.get(browserType), browserType.contains("Mobile")));
        newBc.setDefaultNavigationTimeout(120000);
            // .addCookies(cookies)
        return newBc;
    }
}
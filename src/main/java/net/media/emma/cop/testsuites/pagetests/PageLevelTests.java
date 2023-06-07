package net.media.emma.cop.testsuites.pagetests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;

@Service @Slf4j
public class PageLevelTests {

    @Autowired BrowserFactory browserFactory;
    static String jsExtraction = "";

    public String scrapeContent(String url){

        try {
            
            Page page = browserFactory.getPageWithNewContext("Chrome.Desktop.Mac", null);

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
}
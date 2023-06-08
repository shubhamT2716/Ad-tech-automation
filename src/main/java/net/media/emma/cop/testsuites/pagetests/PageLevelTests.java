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
     
    String jsExtraction = "";

    private String pbjsVarExtrationLogic(){
        return """
            debugger;
            if(window._pbjsGlobals){
            var pbjsVar = window._pbjsGlobals[0];
            console.log(JSON.stringify(window[pbjsVar], null , 4));
            }
                """;
    }

     // Scraping ad unit detials
// Scraping only adunit path for now, can be modified as per requirements

private String adUnitExtractionLogic(){
    return """
        googletag.pubads().getSlots().map(s => {
            console.log(s.getAdUnitPath(), JSON.stringify(s.getSizes(), null, 4), JSON.stringify(s.getTargetingMap(), null, 4));
        });;""";
                
                
}
    public String scrapeContent(String url){

        try {
            
            Page page = browserFactory.getPageWithNewContext("Chrome.Desktop.Mac", null);


             page.onConsoleMessage(consoleMessage -> {
                 log.info("Console message: " + consoleMessage.text());
                jsExtraction = consoleMessage.text();
            });
            page.navigate(url);
            page.waitForTimeout(2000);
            JSHandle testPbjs = page.evaluateHandle(pbjsVarExtrationLogic());

            // log.info(testPbjs.toString());

                JSHandle adUnits= page.evaluateHandle(adUnitExtractionLogic());
            

            // log.info(adUnits.toString() + "this is ad units");

            return jsExtraction;

        } catch (PlaywrightException  e) {
                e.printStackTrace();
        }

        return "Error";
        
    }   
} 
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
        let adSlotArray=[];
        
        var adSlots = googletag.pubads().getSlots();

        if(adSlots.length > 0){
      
          for (i=0;i<adSlots.length;i++) {
          
          var res = 'slotPath : '+ adSlots[i].getAdUnitPath();
              
          adSlotArray.push(res);
        }

    }
        console.log(adSlotArray.join(", "));
        
            """;
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
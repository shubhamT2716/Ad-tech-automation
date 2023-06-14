package net.media.emma.cop.testsuites.pagetests;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;

@Service @Slf4j
public class PageLevelTests {

    @Autowired BrowserFactory browserFactory;
    @Autowired URLDataProcessing urlDataProcessing;

    List<String> jsExtraction = new ArrayList<>();
    List<String> knownBidderCallPatterns = Arrays.asList("https://c2shb-oao.ssp.yahoo.com/admax/bid/partners/YPBJS", //yahoo ssp
                                                                 "https://y.com/");

    //Open the URL using Playwright Page
    //Scrape Content (as your doing right now)
    //capture all network calls
    //find out bidder calls (keep a list of known bidder URL formats and filter network calls against them)
    //bidder calls request data (QA Params / Request Body) should have expeted values only.
    //???
    //build a map<string,string> bidder key-val data from the URL
    public String getPageLevelTestsData(String url){

        Page page = browserFactory.getPageWithNewContext("Chrome.Desktop.Mac", Arrays.asList());

        //capturing all network calls
        //list of arrays initialization
        List<Request> urlFromNetworkTab = new ArrayList<>();

        page.onRequest(request -> {

            urlFromNetworkTab.add(request);
        });

        String scrapeContentData = scrapeContent(page, url);

        Request sampleRequest = urlDataProcessing.getRequestFor("https://c2shb-oao.ssp.yahoo.com/admax/bid/partners/YPBJS", urlFromNetworkTab);

        URL urlData;
        try {

            // sampleRequest.postData()

            urlData = new URL(sampleRequest.url().toString());
            HashMap<String, String> queryStringMap = urlDataProcessing.queryStringToMap(urlData.getQuery());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return  urlFromNetworkTab.toString() + "<br>\n" + scrapeContentData;
    }


    private String scrapeContent(Page page, String url){

        try {
            
           

             page.onConsoleMessage(consoleMessage -> {
                 log.info("Console message: " + consoleMessage.text());
                jsExtraction.add("\n" + consoleMessage.text() + "\n");
            });

            page.navigate(url);
            page.waitForTimeout(500);
            JSHandle testPbjs = page.evaluateHandle(pbjsVarExtrationLogic());

            // log.info(testPbjs.toString());
            
                JSHandle adUnits= page.evaluateHandle(adUnitExtractionLogic());
            
                String concatenatedString = String.join(" ", jsExtraction);
            // log.info(adUnits.toString() + "this is ad units");

            return concatenatedString;

        } catch (PlaywrightException  e) {
            log.error("Error in scraping content", e);
        }

        return "Error";
    }   

    private String pbjsVarExtrationLogic(){
        return """
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
            if(googletag.pubads()){
            googletag.pubads().getSlots().map(s => {
                console.log(s.getAdUnitPath(), JSON.stringify(s.getSizes(), null, 4), JSON.stringify(s.getTargetingMap(), null, 4));
            });
        }""";              
    }
} 
package net.media.emma.cop.testsuites.pagetests;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;

@Service
@Slf4j
public class PageLevelTests {

    @Autowired
    BrowserFactory browserFactory;
    @Autowired
    URLDataProcessing urlDataProcessing;

    List<String> jsExtraction = new ArrayList<>();
    // List of urls to scrape the data from, basically bidder calls
    List<String> knownBidderCallPatterns = Arrays.asList("https://c2shb-oao.ssp.yahoo.com/admax/bid/partners/YPBJS");

    // Open the URL using Playwright Page - Done
    // Scrape Content (as your doing right now) - Done
    // capture all network calls
    // find out bidder calls (keep a list of known bidder URL formats and filter
    // network calls against them) - In progress
    // bidder calls request data (QA Params / Request Body) should have expeted
    // values only. - In Progress
    // ???
    // build a map<string,string> bidder key-val data from the URL
    public String getPageLevelTestsData(String url) {


        Page page = browserFactory.getPageWithNewContext("Chrome.Desktop.Mac", Arrays.asList());

        // capturing all network calls
        // list of arrays initialization
        List<Request> urlFromNetworkTab = new ArrayList<>();
        List<String> requestUrls = new ArrayList<>();

        page.onRequest(request -> {

            urlFromNetworkTab.add(request);
            requestUrls.add(request.url());
        });

        String scrapeContentData = scrapeContent(page, url);

        Request sampleRequest = urlDataProcessing
                .getRequestFor("https://c2shb-oao.ssp.yahoo.com/admax/bid/partners/YPBJS", urlFromNetworkTab);

        URL urlData;
        String postData = sampleRequest.postData();

        

        try {
            urlData = new URL(sampleRequest.url().toString());

            // Can be used for deubbing
            // fetches url
            // log.info("urlData " + urlData);
            // post request data of the said url
            // log.info("postData " + postData);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // HashMap<String, String> queryStringMap =
        // urlDataProcessing.queryStringToMap(urlData.getQuery());
        // log.info("testing queryStringMap" + queryStringMap);


        
        // creating hashmap of known Key values

        HashMap<String, String> knownBidderCallsDataMap = new HashMap<>();

        knownBidderCallsDataMap.put("dfp_ad_unit_code", "us_eng_main_dt_top_center");
        knownBidderCallsDataMap.put("cobrand", "none");
        knownBidderCallsDataMap.put("page", "https://www.engadget.com/?headerbidding=1");


        // Logic for checking post data with hard coded known values

        for (Entry<String, String> entry : knownBidderCallsDataMap.entrySet()) {
            String keyOfHashMap = entry.getKey();
            String valueOfHashMap = entry.getValue();

            Boolean keyPostData = postData.contains(keyOfHashMap);
            Boolean valuePostData = postData.contains(valueOfHashMap);
            log.info("key " + keyOfHashMap + " and key matching is " + keyPostData + " value is " + valueOfHashMap
                    + " and value matching is " + valuePostData);
        }

        return "Post Data of YPBJS request " + postData + "\n  \n" + urlFromNetworkTab.toString() + "\n"
                + scrapeContentData + "Request Urls " + requestUrls;
    }

    private String scrapeContent(Page page, String url) {

        try {

            page.onConsoleMessage(consoleMessage -> {
                jsExtraction.add("\n" + consoleMessage.text() + "\n");
            });

            page.navigate(url);
            page.waitForTimeout(500);
            JSHandle testPbjs = page.evaluateHandle(pbjsVarExtrationLogic());

            // log.info(testPbjs.toString());

            JSHandle adUnits = page.evaluateHandle(adUnitExtractionLogic());

            String concatenatedString = String.join(" ", jsExtraction);
            // log.info(adUnits.toString() + "this is ad units");
            page.close();
            return concatenatedString;

        } catch (PlaywrightException e) {
            log.error("Error in scraping content", e);
        }

        return "Error";
    }

    private String pbjsVarExtrationLogic() {
        return """
                if(window._pbjsGlobals){
                var pbjsVar = window._pbjsGlobals[0];
                console.log(JSON.stringify(window[pbjsVar], null , 4));
                }
                    """;
    }

    // Scraping ad unit detials
    private String adUnitExtractionLogic() {
        return """
                    if(googletag.pubads()){
                    googletag.pubads().getSlots().map(s => {
                        console.log(s.getAdUnitPath(), JSON.stringify(s.getSizes(), null, 4), JSON.stringify(s.getTargetingMap(), null, 4));
                    });
                }""";
    }
}
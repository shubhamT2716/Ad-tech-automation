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
import java.util.stream.Collectors;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.JsonObject;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONUtil;

/*  ***************************************************************************************************
    // Open the URL using Playwright Page - Done
    // Scrape Content (as your doing right now) - Done
    // capture all network calls
    // find out bidder calls (keep a list of known bidder URL formats and filter
    // network calls against them) - In progress
    // bidder calls request data (QA Params / Request Body) should have expeted
    // values only. - In Progress
    // ???
    // build a map<string,string> bidder key-val data from the URL


 ********************************************************************************************* */

@Service
@Slf4j
public class PageLevelTests {

    @Autowired
    BrowserFactory browserFactory;
    @Autowired
    URLDataProcessing urlDataProcessing;

    @Autowired ObjectMapper jsonReader;

    List<String> jsExtraction = new ArrayList<>();
    // List of urls to scrape the data from, basically bidder calls
    HashMap<String, String> knownBidderCallPatterns = new HashMap<>() {
        {
            put("YPBJS", "https://c2shb-oao.ssp.yahoo.com/admax/bid/partners/YPBJS");
            put("RTBSCall", "https://xch.media.net/AdExchange/rtbsspub?");
        }
    };

    public String getPageLevelTestsData(TestCriteria yaml) {

        Page page = browserFactory.getPageWithNewContext(yaml.getUserAgent(), Arrays.asList());

        // list of arrays initialization
        List<Request> urlFromNetworkTab = new ArrayList<>();
        List<String> requestUrls = new ArrayList<>();

        page.onRequest(request -> {

            urlFromNetworkTab.add(request);
            requestUrls.add(request.url());
        });

        String scrapeContentData = scrapeContent(page, yaml.getPublisherUrl());
        String networkCallDataValidation = networkCallDataChecks(
                knownBidderCallPatterns.get("YPBJS"), urlFromNetworkTab, yaml); // Yahoo SSP Call

        String networkCalls = requestUrls.stream().collect(Collectors.joining("\n"));

        page.close();

        return "Post Data of YPBJS request " + networkCallDataValidation + "\n  \n" + urlFromNetworkTab.toString()
                + "\n"
                + scrapeContentData + "Request Urls " + networkCalls;
    }

    private String networkCallDataChecks(String networkCallUrl, List<Request> urlFromNetworkTab, TestCriteria yaml) {
        String urlPassed = yaml.getPublisherUrl();
        String resultsText = "";
        Map<String, String> knownBidderCallsDataMap = yaml.getYpbjs();

        Request sampleRequest = urlDataProcessing.getRequestFor(networkCallUrl, urlFromNetworkTab);

        if (sampleRequest == null) {
            return "No network call found for: " + networkCallUrl;
        }

        URL urlData;

        String postData = sampleRequest.postData();




        // Logic to pretty print

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject jsonObject = gson.fromJson(postData, JsonObject.class);

        log.info("Json object" + jsonObject);


        try{
            String valueYpbjsPostData = jsonObject.get("id").getAsString();

       log.info("Scarping from ypbjs " + valueYpbjsPostData);
        } catch(Exception e){
            e.printStackTrace();
        }


        String prettyPrint = gson.toJson(jsonObject);

        try {
            urlData = new URL(sampleRequest.url().toString());

            resultsText += prettyPrint + "\n";

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // creating hashmap of known Key values
        // if conditons for various sites and KVs for yahoo, hard coded right now
        // if (url.contains("www.engadget.com")) {
            

        //     knownBidderCallsDataMap.put("dfp_ad_unit_code", "us_eng_main_dt_top_center");
        //     knownBidderCallsDataMap.put("cobrand", "none");
        //     knownBidderCallsDataMap.put("page", urlPassed);
        //     knownBidderCallsDataMap.put("publisherblob",
        //             "lu:0;pt:home;site:engadget;ver:carbon||1197802876||6jljbcpi8oe6m");
        //     knownBidderCallsDataMap.put("bucket", "eng-nl-hp-dis,ed-gam-migration1-1");

        // } else if (url.contains("sports.yahoo.com")) {

        //     knownBidderCallsDataMap.put("dfp_ad_unit_code", "us_ysports_main_dt_top_right");
        //     knownBidderCallsDataMap.put("cobrand", "none");
        //     knownBidderCallsDataMap.put("page", urlPassed);
        //     knownBidderCallsDataMap.put("publisherblob",
        //             "lu:0;pct:;pd:;pt:home;site:sports;ver:y20||25664825||028fg2di95m9p");
        //     knownBidderCallsDataMap.put("bucket", "JARVISUSSPORTSDESKCSMIGRATION000");

        // }

        // Logic for checking post data with hard coded known values

        for (Entry<String, String> entry : knownBidderCallsDataMap.entrySet()) {
            String keyOfHashMap = entry.getKey();
            String valueOfHashMap = entry.getValue();

            Boolean keyPostData = postData.contains(keyOfHashMap);
            Boolean valuePostData = postData.contains(valueOfHashMap);

            resultsText += "\n Key to check is " + keyOfHashMap + " and matching is " + keyPostData
                    + " and refrence value is " + valueOfHashMap
                    + " and matching is " + valuePostData + "\n" + "Sample value " + knownBidderCallsDataMap.get("$.imp.[0].ext.dfp_ad_unit_code");

        }

        return resultsText;
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

            // https://stackoverflow.com/questions/34111276/jsonpath-with-jackson-or-gson
            // String json = "...";

            // Map<String, Object> book = JsonPath.read(json, "$.store.book[0]");
            // System.out.println(book)
            Object adUnitDataEval = page.evaluate(adUnitDataCheck());

            log.info("JSHandle evaluate "+ adUnitDataEval);

            ObjectMapper objectMapper =new ObjectMapper();
            try{
            String test = objectMapper.writeValueAsString(adUnitDataEval);
            // JsonNode testJson = objectMapper.readTree(adUnitDataEval);
            log.info("test value "+ test);
            } catch ( JsonProcessingException e) {
                 log.error("Json Parsing error", e);
             }

            
            

            // try {
            //     JsonNode adUnitsInJsonFormat = jsonReader.readTree(adUnits.toString());

            //     log.info("Parsed Json Adunits data: ", adUnitsInJsonFormat);

                

            // } catch ( JsonProcessingException e) {
            //     log.error("Json Parsing error", e);
            // }

            String concatenatedString = String.join(" ", jsExtraction);
            // log.info(adUnits.toString() + "this is ad units");
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
                        console.log('List of Ad units and Targeting: ');
                    googletag.pubads().getSlots().map(s => {
                        console.log(s.getAdUnitPath(), JSON.stringify(s.getSizes(), null, 4), JSON.stringify(s.getTargetingMap(), null, 4));
                    });
                }""";
    }



    private String adUnitDataCheck(){
        return """
            var jsonObj = [];
            function adUnitJs(){
                return JSON.stringify(jsonObj);
            }
            
                   if(googletag.pubads()){
                    googletag.pubads().getSlots().map(s => {
                    jsonObj.push(s.getAdUnitPath(), s.getSizes(), s.getTargetingMap())
                    });
                    console.log(JSON.stringify(jsonObj));
                }
                adUnitJs();
                """;
    }
}
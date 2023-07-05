package net.media.emma.cop.testsuites.pagetests;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.mongodb.util.JSON;

import ch.qos.logback.core.net.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;

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

    public List<JsonNode> getPageLevelTestsData(TestCriteria yaml) {

        Page page = browserFactory.getPageWithNewContext(yaml.getUserAgent(), Arrays.asList());

        // list of arrays initialization
        List<Request> urlsFromNetworkTab = new ArrayList<>();

        page.onRequest(request -> {

            urlsFromNetworkTab.add(request);
        });

        JsonNode slotsData = extractSlotsDataFromPage(page, yaml.getPublisherUrl());
        JsonNode networkCallDataValidation = networkCallDataChecks(knownBidderCallPatterns.get("YPBJS"), urlsFromNetworkTab, yaml); // Yahoo SSP Call
        page.close();

        return matchSlotsDataWithBidderRequestData(slotsData, networkCallDataValidation);

    }

    private List<JsonNode> matchSlotsDataWithBidderRequestData(JsonNode slotsData, JsonNode networkCallDataValidation){

        JsonNode bidderCallDataNode;
        String bidderCallDataNodeTarget = "";
        String bidderCallDataNodeTargetReplace = "";

        JsonNode slotsDataPathNode = slotsData.get(0);
        String slotsDataPath = slotsDataPathNode.get("path").asText();
        log.info("Slots Data from Page: " + slotsData.toPrettyString());
        log.info("Target: " + slotsDataPath);


        try {
            log.info("Bidder Call POST Data: " + networkCallDataValidation.toPrettyString());
            bidderCallDataNode= networkCallDataValidation.get("imp");
            bidderCallDataNodeTarget = bidderCallDataNode.get(0).get("ext").get("dfp_ad_unit_code").toString();
            bidderCallDataNodeTargetReplace = bidderCallDataNodeTarget.replace('"', ' ').trim();
            log.info("Replaced string " + bidderCallDataNodeTargetReplace);
            log.info("Target value "+ bidderCallDataNodeTarget);
            log.info("Bidder call data target " + bidderCallDataNode);

        }catch (Exception e){
            log.info("Got null");
        }

        String pageLeveldataAssertionResult = pageLeveldataAssertion(slotsDataPath, bidderCallDataNodeTargetReplace);

        log.info("result matching " + pageLeveldataAssertionResult);

        return Arrays.asList(slotsData, networkCallDataValidation);



    }

    private String pageLeveldataAssertion(String slotsDataPath, String bidderCallDataNodeTargetReplace){
    String result = "Didnt match";
    Boolean flag = false;
        String extractedValueFromSLotsData = slotsDataPath.substring(slotsDataPath.lastIndexOf("/") + 1);
        log.info("Exact value of dfp path " + extractedValueFromSLotsData);
        if(extractedValueFromSLotsData.equals(bidderCallDataNodeTargetReplace)) {
            flag = true;
            result = "The value of Dfp path in defined slots is " + extractedValueFromSLotsData + "and Value of Dfp Path in passed in bidder call " + bidderCallDataNodeTargetReplace + "and Matching lookup is " + flag;
        }else{
            log.info("Dfp slot extracted is " + extractedValueFromSLotsData);
            log.info("Bidder call extracted is " + bidderCallDataNodeTargetReplace);
        }
        return result;
    }










   private JsonNode networkCallDataChecks(String networkCallUrl, List < Request > urlFromNetworkTab, TestCriteria yaml) {
     Request sampleRequest = urlDataProcessing.getRequestFor(networkCallUrl, urlFromNetworkTab);

     if (sampleRequest != null) {

       URL urlData;

       String postData = sampleRequest.postData();

       // Logic to pretty print

       Gson gson = new GsonBuilder().setPrettyPrinting().create();

       JsonObject jsonObject = gson.fromJson(postData, JsonObject.class);

       log.info("Json object" + jsonObject);

       //logic to get any object from YPBJS post data


         //Check to see if YPBJS request post data has imp object
         //jsonObject(""ext.dp_ad_)
         if (jsonObject.has("imp") && jsonObject.get("imp").isJsonArray()) {
           JsonArray impArray = jsonObject.getAsJsonArray("imp");

           //Check if imp size != 0
           if (impArray.size() != 0) {
             JsonObject firstImpObject = impArray.get(0).getAsJsonObject();

             //check if imp has ext object
             if (firstImpObject.has("ext") && firstImpObject.get("ext").isJsonObject() && firstImpObject.size() != 0) {
               JsonObject extObject = firstImpObject.getAsJsonObject("ext");

               //check if ext has dfpadunit key present
               if (extObject.has("dfp_ad_unit_code")) {
                 String dfpAdUnitCode = extObject.get("dfp_ad_unit_code").getAsString();
                 log.info("dfpAdUnitCode = " + dfpAdUnitCode);
               }
             }
           }
         } else {
           log.info("Didnt find");
         }

     }
       try {
           return jsonReader.readTree(sampleRequest.postData());
       } catch (JsonProcessingException e) {
           log.info("Scraping ypbjs failed");
       }
       return null;
   }



    private JsonNode extractSlotsDataFromPage(Page page, String url) {

        try {

            page.onConsoleMessage(consoleMessage -> {
                jsExtraction.add("\n" + consoleMessage.text() + "\n");
            });

            page.navigate(url);
            page.waitForTimeout(500);

            try{
                JsonNode slotsData;
                Object adUnitDataEval = page.evaluate(adUnitDataCheckV2());
                // log.info("Extracted json data from the page: " + adUnitDataEval.toString());
                slotsData = jsonReader.readTree(adUnitDataEval.toString());
                // log.info("JSON Node as pretty print: " + jsonReader.writerWithDefaultPrettyPrinter().writeValueAsString(slotsData));
                return slotsData;
    
            } catch ( JsonProcessingException e) {
                 log.error("Json Parsing error", e);
            }

        } catch (PlaywrightException e) {
            log.error("Error in scraping content", e);
        }

        return null;
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
            // function adUnitJs(){
            //     return JSON.stringify(jsonObj);
            // }
            
                   if(googletag.pubads()){
                    googletag.pubads().getSlots().map(s => {
                    jsonObj.push(
                            new Map([
                                    ["adunitPath", s.getAdUnitPath()], ["sizes", s.getSizes()], ["targetting", s.getTargetingMap()]
                                    ])
                        )
                    });
                    console.log(JSON.stringify(jsonObj));
                    //console.dir(jsonObj)
                }
                //adUnitJs();
                // console.dir(jsonObj);
                """;
    }

    private String adUnitDataCheckV2(){
        return """
            class AdUnitsData {
                constructor(path, sizes, targetting) {
                    this.path = path;
                    this.sizes = sizes;
                  this.targetting = targetting;
                }
              }
            
            slotsData = [];

            function adUnitJs(){
                     return JSON.stringify(slotsData);
                 }

            if(googletag.pubads()){
                googletag.pubads().getSlots().map(s => {
        
                    // console.log(s.getAdUnitPath(), JSON.stringify(s.getSizes(), null, 4), JSON.stringify(s.getTargetingMap(), null, 4));
                    slotsData.push( new AdUnitsData(s.getAdUnitPath(), s.getSizes(), s.getTargetingMap()));
            
                });
                
            }
        
            //console.log(JSON.stringify(slotsData, null, 4));

            adUnitJs();
            """;
    }
}
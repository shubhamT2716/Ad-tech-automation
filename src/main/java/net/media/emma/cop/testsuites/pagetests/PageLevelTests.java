package net.media.emma.cop.testsuites.pagetests;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.cop.testsuites.pagetests.SlotsDataUtil.SlotsData;
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

        // SlotsDataUtil slotsData2 = extractSlotsDataFromPage2(page, yaml.getPublisherUrl());


        JsonNode networkCallDataValidation = networkCallDataChecks(knownBidderCallPatterns.get("YPBJS"), urlsFromNetworkTab, yaml); // Yahoo SSP Call
        page.close();

        return matchSlotsDataWithBidderRequestData(slotsData, networkCallDataValidation);

    }

    private List<String> filterRequestsByTypeAndReturnPostData(String urlPattern, List<Request> urlsFromNetworkTab) {

        List<String> postData = new ArrayList<>();

        for (Request request : urlsFromNetworkTab) {

            if (request.url().startsWith(urlPattern)) {

                try {
                    postData.add(request.postData());
                } catch (PlaywrightException e) {
                    log.error("Error while getting post data from request: " + request.url());
                }
            }
               
            }
    
        return postData;
    }

    private String verifyYPBJSCallData(SlotsDataUtil slotsData, List<Request> urlsFromNetworkTab){

        List<YPBJSUtil> ypbjsCallData = filterRequestsByTypeAndReturnPostData(knownBidderCallPatterns.get("YPBJS"), urlsFromNetworkTab)
                                        .stream().map(data -> {
                                            try {
                                                
                                                return YPBJSUtil.builder().ypbjsPostData(jsonReader.readValue(data, JsonNode.class)).build();
                                                        
                                            } catch (JsonProcessingException e) {
                                                log.error("Error while converting YPBJS call data to JsonNode: " + data);
                                                return null;
                                            }
                                        }).filter( data -> data !=null ) .collect(Collectors.toList());

        for (SlotsData slot : slotsData.getSlots()) {

            Optional<YPBJSUtil> ypbjsdata = ypbjsCallData.stream().findAny().filter(data -> data.getAdUnitPath().equals(slot.getAdUnitPath()));

            if (ypbjsdata.isPresent()) {


               if( ypbjsdata.get().getSizes().equals(slot.getSetOfSizes())){
                     log.info("Sizes matched for slot: " + slot.getAdUnitPath());
               }else{
                     log.info("Sizes did not match for slot: " + slot.getAdUnitPath());
               }

            } else {
                log.info("Slots Data with ad unit path: " + slot.getAdUnitPath() + " not found in YPBJS call data");
            }
           
        }

        return "WIP --rats";
    }


    private List<JsonNode> matchSlotsDataWithBidderRequestData(JsonNode slotsData, JsonNode networkCallDataValidation){
//----------------------------------------------
        //      Json path to extract data directly

//        To get ad slot path from YPBJS call (Post Data) :   $.imp[0].ext.dfp_ad_unit_code
//        To get width from from YPBJS call (Post Data) :     $.imp[0].banner.format[0].w     (If there are two sizes, use [0] to get first, [1] to get second)
//        To get height from from YPBJS call (Post Data) :     $.imp[0].banner.format[0].h    (If there are two sizes, use [0] to get first, [1] to get second)
//        To get adslot path as Json path : $[0].path
//        To get width in ad slot as Json object : $[0].sizes[0].width  (If there are two sizes, use [0] to get first, [1] to get second)
//        To get width in ad slot as Json object : $[0].sizes[0].height  (If there are two sizes, use [0] to get first, [1] to get second)
//------------------------------------------------

        JsonNode bidderCallDataNode;
        String bidderCallDataNodeTarget = "";
        String bidderCallDataNodeTargetReplace = "";
        JsonNode slotsDataSizeNodeWidth = slotsData.get(0).get("sizes").get(0).get("width");
        JsonNode slotsDataSizeNodeHeight = slotsData.get(0).get("sizes").get(0).get("height");
        JsonNode slotsDataPathNode = slotsData.get(0);
        JsonNode bidderCallDataSizesNodeWidth;
        JsonNode bidderCallDataSizesNodeHeight;

        String slotsDataPath = slotsDataPathNode.get("path").asText();
        log.info("Slots Data from Page: " + slotsData.toPrettyString());
        log.info("Target: " + slotsDataPath);
        log.info("size from slots " + slotsDataSizeNodeWidth + " " + slotsDataSizeNodeHeight);

        bidderCallDataNode= networkCallDataValidation.get("imp");

        try {
            log.info("Bidder Call POST Data: " + networkCallDataValidation.toPrettyString());
            bidderCallDataNodeTarget = bidderCallDataNode.get(0).get("ext").get("dfp_ad_unit_code").toString();
            bidderCallDataNodeTargetReplace = bidderCallDataNodeTarget.replace('"', ' ').trim();
            log.info("Replaced string " + bidderCallDataNodeTargetReplace);
            log.info("Target value "+ bidderCallDataNodeTarget);
            log.info("Bidder call data target " + bidderCallDataNode);

        }catch (Exception e){
            log.info("Got null");
        }

        bidderCallDataSizesNodeWidth = bidderCallDataNode.get(0).get("banner").get("format").get(0).get("w");
        bidderCallDataSizesNodeHeight = bidderCallDataNode.get(0).get("banner").get("format").get(0).get("h");
        log.info("sizes " + bidderCallDataSizesNodeWidth + " " + bidderCallDataSizesNodeHeight);


        if(slotsDataSizeNodeWidth.equals(bidderCallDataSizesNodeWidth) && slotsDataSizeNodeHeight.equals(bidderCallDataSizesNodeHeight)){
            Boolean _flag = true;
            log.info("Size matching is "+ _flag + " Slots size width " + slotsDataSizeNodeWidth + " Slots size height " + slotsDataSizeNodeHeight +" Bidder call width is " + bidderCallDataSizesNodeWidth +" Bidder call height is " + bidderCallDataSizesNodeHeight);
        }else {

            log.info("Sizes didnt match");
        }

        String pageLeveldataAssertionResult = pageLeveldataAssertion(slotsDataPath, bidderCallDataNodeTargetReplace);

        log.info("Result " + pageLeveldataAssertionResult);

        return Arrays.asList(networkCallDataValidation, slotsData);
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

   private SlotsDataUtil extractSlotsDataFromPage2(Page page, String url) {

    try {

        page.onConsoleMessage(consoleMessage -> {
            jsExtraction.add("\n" + consoleMessage.text() + "\n");
        });

        page.navigate(url);
        page.waitForTimeout(500);
            
        Object adUnitDataEval = page.evaluate(adUnitDataCheckV2());
        // log.info("Extracted json data from the page: " + adUnitDataEval.toString());
        SlotsDataUtil slotsDataUtil = jsonReader.readValue(adUnitDataEval.toString(), SlotsDataUtil.class);
        // log.info("JSON Node as pretty print: " + jsonReader.writerWithDefaultPrettyPrinter().writeValueAsString(slotsData));
        return slotsDataUtil;


    } catch (Exception e) {
        log.error("Error in scraping content", e);
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
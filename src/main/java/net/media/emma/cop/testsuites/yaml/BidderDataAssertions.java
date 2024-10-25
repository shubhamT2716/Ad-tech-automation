package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.ReadContext;
import com.microsoft.playwright.Request;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BidderDataAssertions {

//	List<String> listOfKeys=Arrays.asList("s");

    List<String> listOfKeys = Arrays.asList("site", "ext", "id", "imp", "ortb2", "tmax");

    public Map<String, Boolean> bidderDataAssertions(URLChecksReportModel model, List<Request> requests, TestCriteriaBo testData) {

        Map<String, Boolean> bidderkeyVerification = new HashMap<>();

        try {


            Map<String, List<TestCriteriaBo.BidderDataAssertions>> bidderDataAssertions = testData.getBidderDataAssertions();
            Map<String, String> bidderDataUrls = testData.getBidderDataUrls();


            for (Map.Entry<String, List<TestCriteriaBo.BidderDataAssertions>> entry : bidderDataAssertions.entrySet()) {
                String bidder = entry.getKey();
                String bidderUrl = bidderDataUrls.get(bidder);
                if (bidderUrl == null) continue;

                List<Request> matchingRequests = filterMatchingRequests(requests, bidderUrl);
                if (matchingRequests.isEmpty()) continue;

                for (Request request : matchingRequests) {
                    String postData = request.postData();

//	            	 String responseDate=request.response().body().toString();


                    ReadContext readContext = com.jayway.jsonpath.JsonPath.parse(postData);


                    String jsonString = readContext.jsonString();

                    ObjectMapper mapper = new ObjectMapper();

                    JsonNode jsonNode = mapper.readValue(jsonString, JsonNode.class);

                    for (String key : listOfKeys) {
//	            		String data=jsonNode.get(keys).toString();

                        if (key != "id" || key != "imp" || key != "tmax") {


                            for (String name : getKeyFromPayload(key)) {
                                JsonNode value = jsonNode.get(key).get(name);
                                if (value == null) {
                                    bidderkeyVerification.put(name, false);
                                }
                            }
                        }


                    }

                }

            }


        } catch (Exception e) {
            e.printStackTrace();


        }


        return bidderkeyVerification;
    }


    private List<String> getKeyFromPayload(String payload) {
        Map<String, List<String>> keyData = new HashMap<>();

        List<String> listOfSiteData = Arrays.asList("domain", "page", "ref", "topMostLocation", "isTop", "canonical_url", "og_url");

        List<String> listOfExtData = Arrays.asList("abte", "customer_id", "prebid_version", "gdpr_applies", "usp_applies", "coppa_applies", "screen");

        List<String> listOfOrtb2 = List.of("regs");

//	    	keyData.put("site", listOfSiteData);
//	    	keyData.put("ext", listOfExtData);
//	    	keyData.put("ortb2", listOfOrtb2);

        keyData.put("ext", listOfExtData);
        keyData.put("ortb2", listOfOrtb2);
        keyData.put("site", listOfSiteData);

        List<String> result = keyData.get(payload);

        System.out.println(payload + " " + result);


        return result;

    }


    private List<Request> filterMatchingRequests(List<Request> requestList, String bidderUrl) {
        return requestList.stream().filter(request -> request.url().contains(bidderUrl)).collect(Collectors.toList());
    }

}

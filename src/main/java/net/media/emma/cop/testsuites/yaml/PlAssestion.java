package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.microsoft.playwright.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlAssestion {

    @Autowired
    private JsonPath jsonPath;
    
    List<String> listOfpayload=Arrays.asList("site","ext","id","imp","ortb2","tmax");
    
   

    HashMap<String, Object> errorMap = new HashMap<>();

    
    
    public void verifyBidderDataAssertions(URLChecksReportModel urlChecksReportModel, List<Request> requestList, TestCriteriaBo testData) {
        Map<String, List<TestCriteriaBo.BidderDataAssertions>> bidderDataAssertions = testData.getBidderDataAssertions();
        Map<String, String> bidderDataUrls = testData.getBidderDataUrls();

        for (Map.Entry<String, List<TestCriteriaBo.BidderDataAssertions>> entry : bidderDataAssertions.entrySet()) {
            String bidder = entry.getKey();
            

            
            String bidderUrl = bidderDataUrls.get(bidder);

            
            if (bidderUrl == null) continue;

            List<Request> matchingRequests = filterMatchingRequests(requestList, bidderUrl);
            System.err.println("Matching request: "+matchingRequests);
            
            if (matchingRequests.isEmpty()) continue;

            Map<String , Map<String, Object> > bidderDataKeyVerification=new HashMap<>();
            
            // Collect all imp objects and their corresponding responses for matching requests
            List<Pair<Object, String>> allImpAndResponsePairs = new ArrayList<>();

            
            for (Request request : matchingRequests) {
                String postData = getPostData(request);
                
                String responseData =null;
                	responseData= getResponseData(request);
                	

                
                ReadContext readContext = com.jayway.jsonpath.JsonPath.parse(postData);
               
                try {
                    List<Object> impObjects = readContext.read("$.imp");
                    
                    
                  String jsonString=  readContext.jsonString();
                  
                  
                  ObjectMapper mapper=new ObjectMapper();
                  
                  try {
					JsonNode jsonData=mapper.readValue(jsonString,JsonNode.class);
					
					for (String payloadName : listOfpayload) {
						
//						System.err.println(payloadName=="site"||payloadName=="ext"||payloadName=="ortb2");
//						
//						System.out.println("data:"+getKeyFromPayload(payloadName) );
						
						if(payloadName=="site"||payloadName=="ext"||payloadName=="ortb2")
						{
							for(String name:getKeyFromPayload(payloadName))
							{
								
							
								try {
									JsonNode value=jsonData.get(payloadName).get(name);
								
									if(value==null)
									{
										

										bidderDataKeyVerification.computeIfAbsent(payloadName, k->new HashMap<>()).put(name, false);
									}else {

										bidderDataKeyVerification.computeIfAbsent(payloadName, k-> new HashMap<>()).put(name, value);
									}
									
//									addDataToReport(urlChecksReportModel, bidderDataKeyVerification);
								}
								
								
								catch (Exception e) {
									e.printStackTrace();
									continue;
									
									}
								
							}
						}

					}
					
					
					
					

					
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                  
                 
   
                    for (Object imp : impObjects) {
           
                        allImpAndResponsePairs.add(new Pair<>(imp, responseData));
                    }
                } catch (PathNotFoundException e) {
                    log.error("No imp object found in request postData " + e);
                }
            }

            List<TestCriteriaBo.BidderDataAssertions> assertionsList = entry.getValue();

            for (TestCriteriaBo.BidderDataAssertions assertion : assertionsList) {
                boolean passedForOneImpObject = false;
                for (Pair<Object, String> pair : allImpAndResponsePairs) {
                    String impData = null;
                    try {
                        impData = jsonPath.convertObjectToJson(pair.getKey());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    String correspondingResponseData = pair.getValue();

                    if (isPathPresent(assertion, impData)) {
                        if (verifyDataAssertionsRequest(assertion.getRequestData(), impData) &&
                                verifyDataAssertionsResponse(assertion.getResponseData(), correspondingResponseData)) {
                            passedForOneImpObject = true;
                            break;
                        }
                    }
                }
                if (passedForOneImpObject) {
                	
                    createReport(urlChecksReportModel, assertion,bidder, bidderUrl, "All assertions passed", URLChecksReportModel.TestResult.PASS,bidderDataKeyVerification);
                } else {
                
                	String message="bidderAssertion fail:  "+"Expected Value : "+errorMap.get("expected")+ "  Actual Value : "+errorMap.get("actualValue")+"  JsonPath: "+errorMap.get("jsonPath");
           
                	
                    createReport(urlChecksReportModel, assertion,bidder, bidderUrl, message , URLChecksReportModel.TestResult.FAIL,bidderDataKeyVerification);
                }
            }
        }
    }
    private List<Request> filterMatchingRequests(List<Request> requestList, String bidderUrl) {
    	
        return requestList.stream().filter(request -> request.url().contains(bidderUrl)).collect(Collectors.toList());
    }
    
    
    private List<String> getKeyFromPayload(String payload)
    {
    	Map<String, List<String>> keyData=new HashMap<>();
    	
    	List<String> listOfSiteData=Arrays.asList("domain","page","ref","topMostLocation","isTop","canonical_url","og_url");
    	
    	List<String> listOfExtData=Arrays.asList("abte","customer_id","prebid_version","gdpr_applies","usp_applies","coppa_applies","screen");
    	
    	List<String> listOfOrtb2=Arrays.asList("regs");
    	
    	keyData.put("ext", listOfExtData);
    	keyData.put("ortb2", listOfOrtb2);
    	keyData.put("site", listOfSiteData);
    	
    	List<String> result=keyData.get(payload);
    	
    	
    	
    	return result;
    	
    }

    private boolean isPathPresent(TestCriteriaBo.BidderDataAssertions assertion, String postData) {
        TestCriteriaBo.MatchingJsonPath matchingJsonPath = assertion.getMatchingJsonPath();
        String path = matchingJsonPath.getJsonPath().replaceFirst("x", Matcher.quoteReplacement("$"));
        String expectedValue = matchingJsonPath.getExpectedValue();
        if(jsonPath.compareValueInJson(postData, removeJsonPathIndexForImp(path), expectedValue)) {
            return true;
        }
        else{
            errorMap.put("jsonPath", path);
            errorMap.put("data", "Matching Json Path");
            errorMap.put("actualValue", jsonPath.readValueFromJson(postData,removeJsonPathIndexForImp(path)));
//            errorMap.put("actualValue", path);
            errorMap.put("expected", expectedValue);
            return false;
        }
    }

    private boolean verifyDataAssertionsRequest(List<TestCriteriaBo.RequestData> dataAssertionList, String parsedData) {
        boolean passForAllRequest = true;
        for (TestCriteriaBo.RequestData dataAssertion : dataAssertionList) {
            String path = dataAssertion.getJsonPath().replaceFirst("x", Matcher.quoteReplacement("$"));
            String expectedValue = dataAssertion.getValue();
            if(!jsonPath.compareValueInJson(parsedData, removeJsonPathIndexForImp(path), expectedValue)) {
                passForAllRequest = false;
                errorMap.put("jsonPath", path);
                errorMap.put("data", "Request Data");
                errorMap.put("actualValue", jsonPath.readValueFromJson(parsedData,removeJsonPathIndexForImp(path)));
//                errorMap.put("actualValue", path);
                errorMap.put("expected", expectedValue);
                break;
            }
        }
        return passForAllRequest;
    }

    private boolean verifyDataAssertionsResponse(List<TestCriteriaBo.ResponseData> dataAssertionList, String parsedData) {
        boolean passForAllResponse = true;
        for (TestCriteriaBo.ResponseData dataAssertion : dataAssertionList) {
            String path = dataAssertion.getJsonPath().replaceFirst("x", Matcher.quoteReplacement("$"));
            String expectedValue = dataAssertion.getValue();
            if(!jsonPath.compareValueInJson(parsedData, removeJsonPathIndexForImp(path), expectedValue)) {
                passForAllResponse = false;
                errorMap.put("jsonPath", path);
                errorMap.put("data", "Response Data");
                errorMap.put("expectedValue ", expectedValue);
                errorMap.put("actualValue", jsonPath.readValueFromJson(parsedData,removeJsonPathIndexForImp(path)));
                
                break;
            }
        }
        return passForAllResponse;

    }
    
//    private void addDataToReport(URLChecksReportModel urlChecksReportModel,Map<String, Map<String, Object>> bidderKeyVerification)
//    {
////    	System.out.println("Data Set to report");
////    	
////    	urlChecksReportModel.getBidderDataAssertionKeyVerification().putAll(bidderKeyVerification);	
//    }
    
    

    private void createReport(URLChecksReportModel urlChecksReportModel, TestCriteriaBo.BidderDataAssertions bidderDataAssertions , String bidder, String url, String message, URLChecksReportModel.TestResult result, Map<String, Map<String, Object>> bidderKeyVerification) {

    	
    	
        URLChecksReportModel.BidderDataAssertion assertionReport = URLChecksReportModel.BidderDataAssertion.builder()
                .message(message)
                .testResult(result)
                .url(url).matchingJson(URLChecksReportModel.MatchingJsonPath.builder()
                        .jsonPath(bidderDataAssertions.getMatchingJsonPath().getJsonPath())
                        .Value(bidderDataAssertions.getMatchingJsonPath().getExpectedValue())
                        .build()).build();
        

        // Get the existing list or create a new one
        List<URLChecksReportModel.BidderDataAssertion> existingReports = urlChecksReportModel.getBidderDataAssertions().getOrDefault(bidder, new ArrayList<>());
        // Add the new report to the list
        existingReports.add(assertionReport);
        
//     // Get the existing list or create a new one
//        List<URLChecksReportModel.bid> existingReports = urlChecksReportModel.getBidderDataAssertions().getOrDefault(bidder, new ArrayList<>());
//        // Add the new report to the list
//        existingReports.add(assertionReport);
        
        urlChecksReportModel.getBidderDataAssertions().put(bidder, existingReports);
        urlChecksReportModel.getBidderDataAssertionKeyVerification().put(bidder,bidderKeyVerification);
    }

    public String removeJsonPathIndexForImp(String jsonPath) {
    	
        return jsonPath.replaceFirst("imp\\[\\d+\\].", "");
    }

    public String getResponseData(Request request) {
    	
//    	try {
    		 return new String(request.response().body(), StandardCharsets.UTF_8);
//    	}
//    	catch (Exception e) {
//			return  request.response().body().toString();
//		}
       
    }

    public String getPostData(Request request) {
        return request.postData();
    }

    class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}




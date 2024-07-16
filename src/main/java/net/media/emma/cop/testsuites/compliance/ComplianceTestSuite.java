package net.media.emma.cop.testsuites.compliance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.Cookie;



import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.cop.testsuites.compliance.ComplianceTestReport.BidExController;
import net.media.emma.cop.testsuites.compliance.ComplianceTestReport.TestData;
import net.media.emma.cop.testsuites.compliance.ComplianceURLChecksReportModel.NegativeURLChecks;

import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.URLDataProcessing;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class ComplianceTestSuite {
    @Autowired
    Logger logger;
    @Autowired
    URLDataProcessing urlDataProcessing;
    @Autowired
    ObjectWriter jsonWriter;

    // @Autowired
    // private CommonBrowserUtil browserUtil;

    @Value("${harFileBasePath}")
	private String harFileBasePath;

    @Value("${browserPath}")
    private String browserPath;
   
    
    @Autowired
    private BidexOrClientagData data;
    
    private static String windscribeLogin = "xpath=//button[text()='Login']";
    private static String windscribeUsername = "xpath=//input[@name='username']";
    private static String windscribepassword = "xpath=//input[@name='password']";
    private static String windscribeSkip = "xpath=//div[text()='Skip']";
    private static String username = "mohib1233";
    private static String password = "mohib123";
//    private static String locationParis = "Paris";
    private static String windscribeLocations = "xpath=//button[@aria-label='Locations']";
    private static String windscribeSearchLocation = "xpath=//div//i";
    private static String windscribeSearchbox = "xpath=//input[contains(@class,'css-1c1txoj')]";
    
//    private static String acceptAllBtn = "xpath=//button[text()='I Accept']";
    
    private static String acceptAllBtn="xpath=//*[@id=\"onetrust-accept-btn-handler\"]";
    
  //*[@id="onetrust-accept-btn-handler"]


    String url = "chrome-extension://hnmpcagpplmpfojmgmnngilcnanddlhb/popup.html";
    String windScribeExtensionPath = System.getProperty("user.dir") + "/extensions/hnmpcagpplmpfojmgmnngilcnanddlhb/3.4.6_0";

    // List of strings "1", "2"
    List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net",
            "hbx-stage.media.net","hbx-stage-2.media.net","hbx-stage-3.media.net","hbx-stage-4.media.net","hbx-stage-1.media.net","ssp-stage-hb.media.net","ssp-stage-hb-1.media.net","ssp-stage-hb-2.media.net","ssp-stage-hb-3.media.net","ssp-stage-hb-4.media.net");
    List<String> bidExControlers = Arrays.asList("bidexchange.js", "clientag.js", "js/tags/clientag.js","ss/nes/bidexl2.js");// , "catchpoint_content.js",
    // "topics.js");
    
    
    public ComplianceTestReport getTestResults(ComplianceTestCriteria testData) throws InterruptedException {

        String harFileName = harFileBasePath + CommonBrowserUtil.getFileNameForHar();
        BrowserContext browserContext = BrowserFactory.getSystemBc(testData.getUserAgent(), testData.getCookies(), getExtensionList(), harFileName, browserPath);
        browserContext.clearCookies();
        
        //list of arrays initialization
        List<String> urlFromNetworkTab = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();
        
        
        browserContext.onRequest(request -> {

            urlFromNetworkTab.add(request.url());
            requestList.add(request);
        });
        
        browserContext.setDefaultNavigationTimeout(90000);//90 s

        Page page = browserContext.newPage();
        openWindscribe(page, testData.getLocation());
        page.close();
        page = browserContext.newPage();
        page.navigate(testData.getPublisherUrl());


        try {
        	
        	
        	try {
        		page.locator(acceptAllBtn).click();
        	}catch (Exception e) {
				
        		System.out.println(e.getMessage());
			}

        
//        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(10000);

        List<Cookie> allCookies = browserContext.cookies();
       Object tcfResponse = getTcfApiResponse(page);
       
//       System.err.println("tcfresponse :  "+tcfResponse);
       
       log.info("tcf Response: "+tcfResponse);
       
      Map<String, String> map=(Map<String, String>)tcfResponse;
       
        String gdprString = map.get("tcString");


//        pubsRefresh(page);
//        page.waitForLoadState(LoadState.NETWORKIDLE);

        updateTestCriteriaGdprString(testData,gdprString);
        
       Thread.sleep(30000);        
        BidExController  bidExController=getBidData(requestList,testData);

        ComplianceURLChecksReportModel urlCheck = getNetworkCallAssertions3(allCookies,urlFromNetworkTab, requestList,testData);
        
        ComplianceTestReport results=ComplianceTestReport.builder().testMetaData(TestData.builder()
        		.environment(testData.getEnvironment()).publisherUrl(testData.getPublisherUrl()).testTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
        		.userAgent(testData.getUserAgent()).build()).bidExController(bidExController)
        		.checksReportModel(urlCheck).build();
        
        return results;
        
        }
        catch (Exception e) {
        	
        	e.printStackTrace();
        	
        	return null;
			
		}
        finally {
        	 page.close();
             browserContext.close();
		}
       
        
       

       
    }

    private BidExController getBidData(List<Request> requestList, ComplianceTestCriteria testData) {
		
    	
    	BidExController bidExController=BidExController.builder().build();
    	List<Request> listOfUrls=getBidexUrls(requestList, testData.getEnvironment());
    	
    	for(Request request:listOfUrls)
    	{
    		
    		bidExController.getMatchingUrls().add(request.url());
    		
    		if(request.failure()!=null)
    		{
    			bidExController.getConsoleMessages().add("bidex or clinetag request failed"+request.failure());
    		}
    		
    		
    		
    		if(request.timing()!=null)
    		{
    			Date  date=new Date((long) (request.timing().responseEnd-request.timing().responseStart));
    			bidExController.setTotalResponseTime(date.getTime()+" ms");
    			
    		}else {
    			
    			bidExController.setTotalResponseTime("not available");
    		}
    		
    		
    		String uSize=getBidexSize(request);
    		
    		System.err.println(uSize);
    		
    		bidExController.setTotalSizeDownloaded(uSize); 
    		
//    		bidExController.setTotalSizeDownloaded(getBidexSize(request));
    		
    		bidExController.setBidexData(this.data.getBidExData(request));
    		
    	}
    	
    	size.setLength(0);
    	
    	if(listOfUrls.size()==0)
    	{
    		bidExController.getConsoleMessages().add("bidex or clientag data not found");
    	}
    	
    	
    	return bidExController;
    	
    	
	}
    
    
    
StringBuilder size =new StringBuilder();
	
	public String getBidexSize(Request request)
	{
		List<String> bidex2=Arrays.asList("ss/nes/bidexl2.js?");
		String regexForBidex2=".*/(" + String.join("|", bidExHosts) + ")/("
                + String.join("|", bidex2) + ").*";
		
		List<String> bibex1=Arrays.asList("bidexchange.js");
		String regexForBidex1=".*/(" + String.join("|", bidExHosts) + ")/("
                + String.join("|", bibex1) + ").*";
		
		if(request.url().matches(regexForBidex1))
		{
//			size.c("bidexchange.js", request.sizes().s + "b");
			
			System.err.println(" Size :  "+request.sizes().responseBodySize+" :  "+request.sizes().requestBodySize+" :   "+request.sizes());
			size.append("bidexchange.js : "+ request.sizes().responseBodySize + " b");
		
			
		}
		if(request.url().matches(regexForBidex2))
		{
			System.err.println(" Size :  "+request.sizes().responseBodySize);
			System.err.println(" Size :  "+request.sizes().responseBodySize+" :  "+request.sizes().requestBodySize+" :   "+request.sizes());

			size.append("  "+"bidexl2.js : "+ request.sizes().responseBodySize + " b");
		}
		else {
			
			return request.sizes().responseBodySize + "b";
			
			
		}
		
		
		return size.toString();		
		
		
	}
	
    
    
    private List<Request> getBidexUrls(List<Request> requestList, String env)
    {
    	
    	 String regEx = ".*/(" + String.join("|", bidExHosts) + "|" + env + ")/("
                 + String.join("|", bidExControlers) + ").*";
    	
    	 List<Request> listOfUrls=new ArrayList<>();
    	 
    	for(Request request:requestList)
    	{
    		if(request.url().matches(regEx))
    		{
    			listOfUrls.add(request);
    		}
    	}
    	
    	return listOfUrls;
    }

	public List<String> getExtensionList() {
        List<String> ext = new ArrayList<>();
        ext.add("--disable-extensions-except=" + windScribeExtensionPath);
        ext.add("--load-extension=" + windScribeExtensionPath);
        return ext;
    }

    public void openWindscribe(Page page , String location) throws InterruptedException
    {
        page.navigate(url);
        page.waitForTimeout(2000);
//        if (page.isVisible(windscribeLogin))
//        {
//            try
//            {
//                loginToWindscribe(page);
//                setLocationInWindscribe(page , location);
//            } catch (TimeoutError e)
//            {
//                log.error("Element not found ", e);
//            }
//        }
//        else
//        {
            setLocationInWindscribe(page, location);
//        }
    }

    private static void loginToWindscribe(Page page)
    {
        page.locator(windscribeLogin).click();
        page.locator(windscribeUsername).click();
        page.locator(windscribeUsername).fill(username);
        page.locator(windscribepassword).click();
        page.locator(windscribepassword).fill(password);
        page.locator(windscribeLogin).click();
        //page.locator(windscribeSkip).click();
    }

    private static void setLocationInWindscribe(Page page , String location)
    {
    	
    	String windscribeParisLoc = "xpath=(//div[@aria-label='"+location+"'])[1]";
    	
        page.locator(windscribeLocations).click();
        page.locator(windscribeSearchLocation).click();
        page.locator(windscribeSearchbox).click();
        page.locator(windscribeSearchbox).fill(location);
        page.locator(windscribeParisLoc).click();
        page.close();
    }

    private ComplianceURLChecksReportModel getNetworkCallAssertions3(List<Cookie> allCookies,List<String> requestList, List<Request> requests, ComplianceTestCriteria testData) {

        ComplianceURLChecksReportModel urlChecks = ComplianceURLChecksReportModel.builder().build();

        for (String requestUrl : requestList) {

            Map.Entry<String, String> urlData = positiveURLPatternExists(requestUrl, testData);

            if (urlData != null) {

                ComplianceURLChecksReportModel.PositiveURLChecks labelExists = urlChecks.getPositiveUrlChecks().get(urlData.getKey());

                if (labelExists == null) {

                    ComplianceURLChecksReportModel.PositiveURLChecks puc = ComplianceURLChecksReportModel.PositiveURLChecks.builder().label(urlData.getKey())
                            .urlPattern(urlData.getValue())
                            .matchingUrls(new ArrayList<String>(Arrays.asList(requestUrl)))
                            .queryStringMap(new ArrayList<HashMap<String, String>>(Arrays.asList(urlDataProcessing.getQueryStringMapFromUrl(requestUrl,requests))))
                            //  .negativeKeyValChecks(testData.getKeysToVerifyNegative().get(urlData.getKey()))
                            .positiveKeyValChecks(testData.getKeysToVerifyPositive().get(urlData.getKey()))
                            .anyOneOfKeyValChecks(testData.getKeysToVerifyAnyOneOf().get(urlData.getKey()))
                            .build();

                    urlChecks.getPositiveUrlChecks().put(urlData.getKey(), puc);
                } else {
                    labelExists.getMatchingUrls().add(requestUrl);
                    labelExists.getQueryStringMap().add(urlDataProcessing.getQueryStringMapFromUrl(requestUrl,requests));
                }

                continue;
            }

            urlData = negativeURLPatternExists(requestUrl, testData);

            if(urlData != null){
                NegativeURLChecks nuc = urlChecks.getNegativeUrlChecks().get(urlData.getKey());
                if(nuc == null){
//                    urlChecks.getNegativeUrlChecks().put(urlData.getKey(), URLChecks.NegativeURLChecks.builder().label(urlData.getKey())
//                            .urlPattern(urlData.getValue())
//                            .matchingUrls(new ArrayList<String>((Arrays.asList(requestUrl))))
//                            .build());
                	
                	urlChecks.getNegativeUrlChecks().put(urlData.getKey(),
							NegativeURLChecks.builder().label(urlData.getKey()).urlPattern(urlData.getValue())
									.matchingUrls(new ArrayList<String>((Arrays.asList(requestUrl)))).build());
                }else{
                    nuc.getMatchingUrls().add(requestUrl);
                }
            }
        }

        urlChecks.updateMatchingUrlsCount();
        urlChecks.updateNetworkCallErrors(testData);
        urlChecks.verifyPositiveKeyValChecks();
        urlChecks.verifyCookies(allCookies,testData.getCookiesToVerify());

//        try {
//            return jsonWriter.writeValueAsString(urlChecks);
//        } catch (JsonProcessingException e) {
//            log.error("json processing error", e);
//        }

        return urlChecks;
    }

    private Map.Entry<String, String> positiveURLPatternExists(String currentUrl, ComplianceTestCriteria testData) {

        for (Map.Entry<String, String> urlData : testData.getNetworkCallsPositive().entrySet()) {

            if (currentUrl.startsWith(urlData.getValue())) {

                return urlData;
            }
        }

        return null;
    }

    private Map.Entry<String, String> negativeURLPatternExists(String currentUrl, ComplianceTestCriteria testData){

        for(Map.Entry<String, String> urlData : testData.getNetworkCallsNegative().entrySet()){

            if(currentUrl.startsWith(urlData.getValue())){

                return urlData;
            }
        }


        return null;
    }

    private Object getTcfApiResponse(Page page) {
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
//    	Object d = page.waitForFunction("() => {\n" +
//                "  return new Promise((resolve) => {\n" +
//                "    window.__tcfapi(\"addEventListener\", 2, (tcfApiInfo, success) => {\n" +
//                "      if (success && isSet(tcfApiInfo)) {\n" +
//                "        resolve(tcfApiInfo);\n" +
//                "      }\n" +
//                "    });\n" +	
//                "  });\n" +
//                "}");
    	
        Object tcData = page.evaluate("() => {" +
                "return new Promise((resolve, reject) => {" +
                "    __tcfapi('addEventListener', 2, (tcData, success) => {" +
                "        if (success) {" +
                "            console.log(tcData);" +
                "            resolve(tcData);" +
                "        } else {" +
                "            reject('Failed to retrieve tcData');" +
                "        }" +
                "    });" +
                "});" +
                "}");

//        Object d = page.evaluate("() => {\n" +
//                "  return new Promise((resolve) => {\n" +
//                "    window.__tcfapi(\"addEventListener\", 2, (tcfApiInfo, success) => {\n" +
//                "      if (success && isSet(tcfApiInfo)) {\n" +
//                "        resolve(tcfApiInfo);\n" +
//                "      }\n" +
//                "    });\n" +
//                "  });\n" +
//                "}");
        return  tcData;
    }

    private void pubsRefresh(Page page) {
//        page.evaluate("googletag.pubads().refresh(); timeFunction(); timeFunction(); function timeFunction() {setTimeout(function(){ googletag.pubads().refresh();}, 10000);}");
   
    page.evaluate("googletag.pubads().refresh()");
    	
    }

    private void updateTestCriteriaGdprString(ComplianceTestCriteria testData, String updatedValue) {
        Map<String, Map<String, Set<String>>> keysToVerifyPositiveMap = testData.getKeysToVerifyPositive();
        if (keysToVerifyPositiveMap != null) {
            // Iterate over the entries of keysToVerifyPositive map
            for (Map.Entry<String, Map<String, Set<String>>> entry : keysToVerifyPositiveMap.entrySet()) {
                Map<String, Set<String>> networkCallMap = entry.getValue();

                // Check if gdprString key is present in the networkCallMap
                if (networkCallMap.containsKey("gdprstring")) {
                    // Set the value of gdprString
                    networkCallMap.get("gdprstring").clear(); // Clear the existing value if needed
                    networkCallMap.get("gdprstring").add(updatedValue);
                }
            }
        }
    }
}

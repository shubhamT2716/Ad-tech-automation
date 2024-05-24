package net.media.emma.cop.testsuites.yaml;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.testng.internal.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.Geolocation;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Timing;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo.DomInjectParams;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo.ResponseSimulateParams;
import net.media.emma.cop.testsuites.yaml.TestReportModel.BidExController;
import net.media.emma.cop.testsuites.yaml.TestReportModel.BidexData;
import net.media.emma.cop.testsuites.yaml.TestReportModel.TestMetaData;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.NegativeURLChecks;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.PositiveURLChecks;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.DomJsonUtils;
import net.media.emma.utils.ResponseMocking;
import net.media.emma.utils.URLDataProcessing;

@Service
@Slf4j
public class TestSuite implements GenericTestSuite {

    @Autowired
    Configuration jsonModelMapper;
    @Autowired
    DomJsonUtils domUtils;
    @Autowired
    ObjectWriter jsonWriter;
	@Autowired
	ObjectMapper jsonReader;
    @Autowired
    URLDataProcessing urlDataProcessing;
    @Autowired
    PlAssestion payloadAsserstions;
    @Autowired
    HeadersAssertion headersAssertion;
    
    @Autowired
    private ResponseMocking mocking;
    
  

    @Value("${harFileBasePath}")
    private String harFileBasePath;
    @Value("${browserPath}")
    private String browserPath;
    
    @Autowired
    private FileHandling fileHandling;

    private static String windscribeLogin = "xpath=//button[text()='Login']";
    private static String windscribeUsername = "xpath=//input[@name='username']";
    private static String windscribepassword = "xpath=//input[@name='password']";
    private static String windscribeSkip = "xpath=//div[text()='Skip']";
    private static String username = "mohib1233";
    private static String password = "mohib123";
    private static String locationParis = "Paris";
    private static String location = "Paris";
    private static String windscribeLocations = "xpath=//button[@aria-label='Locations']";
    private static String windscribeSearchLocation = "xpath=//div//i";
    private static String windscribeSearchbox = "xpath=//input[contains(@class,'css-1c1txoj')]";
    private static String windscribeLoc = "xpath=(//div[@aria-label='Loc'])[1]";
    private static String acceptAllBtn = "xpath=//span[text()='Accept all']";

//    private static BrowserContext browserContext;

    String url = "chrome-extension://hnmpcagpplmpfojmgmnngilcnanddlhb/popup.html";
    String windScribeExtensionPath = System.getProperty("user.dir") + "/extensions/hnmpcagpplmpfojmgmnngilcnanddlhb/3.4.6_0";

    // List of strings "1", "2"
    List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net",
            "hbx-stage.media.net");
    List<String> bidExControlers = Arrays.asList("bidexchange.js", "clientag.js", "js/tags/clientag.js");// , "catchpoint_content.js",
    // "topics.js");
    
    
    
    Object id;
	
	JSONArray jsonArray;

    private BrowserContext doLogin(BrowserContext bc, TestCriteriaBo testData) {

        try {

            Page page = bc.newPage();
            page.navigate(testData.getLoginUrl());
            page.locator("//*[@id=\"login-username\"]").fill(testData.getUserName());
            page.locator("//*[@id=\"login-signin\"]").click();
            page.locator("//*[@id=\"login-passwd\"]").fill(testData.getPassword());
            page.locator("//*[@id=\"login-signin\"]").click();
            page.waitForLoadState();

        } catch (Exception e) {

            log.error("Error while logging in: {}", e.getMessage());
        }

        return bc;
    }

    // async with future string
    @Async("yamlTestSuitePool")
    public CompletableFuture<TestReportModel> getTestResultsThreadPool( TestCriteriaBo testData) {

    	CompletableFuture<TestReportModel> future = new CompletableFuture<>();
        try {
            TestReportModel testReport = getTestResults(testData, null,null); // Create a new browser context for each task
            future.complete(testReport);
        } catch (Exception e) {
        	
        	e.printStackTrace();         
        	log.error("Error executing test: {}", e.getMessage());
            future.completeExceptionally(e);
        }
        return future;




    }

    @Override
    public TestReportModel getTestResults(TestCriteriaBo testData, String url, String response) {

        String harFilePath = CommonBrowserUtil.getFileNameForHar();
        String location = testData.getLocation();
        
     
		BrowserContext browserContext=null;
		
        
        try {

        if (location != null) {
            browserContext = BrowserFactory.getSystemBc(testData.getUserAgent(), testData.getCookies(), getExtensionList(),
                    harFileBasePath + harFilePath, browserPath);
            browserContext.clearCookies();
        } else {
            browserContext = BrowserFactory.getBc(testData.getUserAgent(), testData.getCookies(),
                    harFileBasePath + harFilePath);
        }

        if (testData.getLoginUrl() != null && testData.getUserName() != null && testData.getPassword() != null) {

            doLogin(browserContext, testData);

        }

        browserContext = replaceBidExUrl(browserContext, testData);

		if(testData.getResponseSimulate()!=null)
		{
			browserContext = attachResponseMocking(browserContext, testData);
		}

       
        // browserContext = injectDOM(browserContext, testData);

        // list of arrays initialization
        List<String> urlFromNetworkTab = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();
        
       

        if (location == null) {
            browserContext.onRequest(request -> {
            	
            	urlFromNetworkTab.add(request.url());
            	requestList.add(request);
            	
            	
            	if(request.url().equals(url))
            	{
            		String postData=request.postData();
    				
    				JSONObject jsonObject=new JSONObject(postData);
    				
    				id=jsonObject.get("id");
    				
    				jsonArray =jsonObject.getJSONArray("imp");
            	}
            
            	
            	
            	
            });
		
        }
        
        

		List<String> consoleMessage = new ArrayList<>();
		
		
		
		JsonNode pbjsData=null;
		JsonNode pbjsEventsData=null;
		
		JsonNode pbjsConfigData=null;
		

		try {

			
			
        
			browserContext.setDefaultTimeout(95000); // 70 seconds
			
			

			
			Page page = browserContext.newPage();
			
			if(url!=null&& response!=null)
			{
				
				page=mocking.responseMocking(page, url, response);
				
			}
			
			try {
				
				pbjsData=getPbjsData(page);
			}
			catch (Exception e) {
				pbjsData=null;
			}
			
			consoleMessage = getConsoleErrors(page);
			
			page.navigate(testData.getPublisherUrl());
			page.evaluate(googleRefresh());
			
			 pbjsConfigData = getPbjsData(page);
			
			if(testData.getPbjsEvents()!=null|| !testData.getPbjsEvents().isEmpty())
			{
				
				 pbjsEventsData = getPbjsEvents(page ,testData.getPbjsEvents());		
			}
			
			
			System.err.println("pbjsEventsData: "+pbjsEventsData);
//			 page.waitForLoadState(LoadState.NETWORKIDLE,new
//			 Page.WaitForLoadStateOptions().setTimeout(5000));
//			 page.waitForFunction("() => performance.getEntriesByType('resource').length >
//			 0", null,new Page.WaitForFunctionOptions().setTimeout(5));
			
			
//			page.wait(100000);
			
			Thread.sleep(60000);
        }
        catch (Exception e) {
        	log.error("Error while navigating to publisher url: {}", e.getMessage());
		}

		
		BidExController bidExController = getBidExData(requestList, testData);
		
		bidExController.setConsoleMessages(consoleMessage);
		
		
		Map<String, List<Cookie>>  getCookies	=getCookies(browserContext, testData.getPublisherUrl());
		
		Map<String, Map<String, Map<String,String>>> cookies=displayCookies(getCookies);
		
		

		TestReportModel testReport = TestReportModel.builder().testMetaData(TestMetaData.builder()
				.dateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).testName("YAML Test Suite")
				.publisherUrl(testData.getPublisherUrl()).environment(testData.getEnvironment())
				.userAgent(
						testData.getUserAgent() + " - " + BrowserFactory.browserContexts.get(testData.getUserAgent()))
				.harFileDownloadLink("harDownload/" + harFilePath).build()).build();
		
		
		
		
		testReport.setCookie(cookies);
		testReport.setBidExController(bidExController);
		testReport.setPbjs(pbjsConfigData);
		testReport.setPbjsEvents(pbjsEventsData);
try {
			
			URLChecksReportModel  model=getUrlAssertionsData(urlFromNetworkTab,requestList, testData);
			

			
			testReport.setUrlChecks(model);
			testReport.setTestSummery(YamlTestSuiteScheduling.testResultSummery(model));
			
		
			
			String fileName=this.fileHandling.setReportToFile(testData, model);
			
			String downloadUrl = "/downloadFile?fileName=" + URLEncoder.encode(fileName, "UTF-8");
			
			testReport.getTestMetaData().setDownloadReport(downloadUrl);

			
		}catch (Exception e) {
			
			testReport.setUrlChecks(null);
			
			testReport.getTestMetaData().setDownloadReport(null);
		}
		
		
		
		
		
		
		return testReport;
		
        }
        catch (Exception e) {
        	
        	e.printStackTrace();
			return null;
		}
		finally {
			browserContext.close();
		}
		
	}

    
    public String googleRefresh()
    {
    	return "googletag.pubads().refresh()";
    }


	public String returnPbjsConfig() {
			return """
				var pbjsVar = "";
				function getPbjsData(){
				if(window._pbjsGlobals){
					pbjsVar = window._pbjsGlobals[0];
					console.log(JSON.stringify(window[pbjsVar], null , 4));
					return JSON.stringify(window[pbjsVar]);
					}
				}
				getPbjsData();
				
					""";
	}
	
	public String getPbjsEventsData(String pbjsEvent) {
		
		return """
			getPbjsEvents();
			function getPbjsEvents(){
				if(window._pbjsGlobals){
					var pbjsVar = window._pbjsGlobals[0];
					var pbjsEventValue = "%s"; // Assigning the value of pbjsEvent
					var pbjsEvents = window[pbjsVar].getEvents().filter(e => e.eventType === pbjsEventValue);
					console.log(JSON.stringify(pbjsEvents, null , 4));
					return JSON.stringify(pbjsEvents, null , 4);
				}
			}
		""".formatted(pbjsEvent);
		
		
	}


	public JsonNode getPbjsEvents(Page page, String pbjsEvent){
		try{
		
		Object eventsDataEVal=page.evaluate(getPbjsEventsData(pbjsEvent));
		JsonNode eventsDataNode=jsonReader.readTree(eventsDataEVal.toString());
		log.info("pbjsEvents: ", eventsDataNode.toString());
		return eventsDataNode;
		}catch(Exception e){
			
			e.printStackTrace();
			log.error("Error while getting pbjs events: ", e.getMessage());
			return null;
		}

	}


	
	private JsonNode getPbjsData(Page page){
		try{
		Object pbjsConfigEval = page.evaluate(returnPbjsConfig());
		JsonNode pbjsDataNode = jsonReader.readTree(pbjsConfigEval.toString()); 
		log.info("pbjsConfig: ", pbjsDataNode.toString());
		return pbjsDataNode;
	}catch(Exception e){
		log.error("Error while getting pbjs data: ", e.getMessage());
		return null;
	}
	}
	


	public Map<String, List<Cookie>> getCookies(BrowserContext page, String publisher)
    {
    	List<String> url=new ArrayList<>();
    	
    	url.add("https://hbx.media.net");
    	url.add("https://hb.yahoo.net");
    	url.add("https://contextual.media.net");
    	url.add(publisher);
    	
    	
    	Map<String, List<Cookie>> mapOfCookie=new HashMap<>();
    	
    	 for(String li : url)
    	 {
    		List<Cookie> cookie= page.cookies(li);
    		 mapOfCookie.put(li, cookie);
    		 
    	 }
    return  mapOfCookie;
    
    	
    }
	
	
	
	 public Map<String, Map<String, Map<String,String>>> displayCookies(Map<String, List<Cookie>> cookies)
	    {
	    	Map<String, Map<String, Map<String,String>>> listOfCookies=new HashMap<>();

	    	cookies.entrySet().forEach(entry->{

	    		Map<String, Map<String,String>> allcookiesData = new HashMap<>();
	            
	    		for(Cookie cookie : entry.getValue()){

	                allcookiesData.put(cookie.name, new HashMap<>());
	                allcookiesData.get(cookie.name).put("value", cookie.value);
	                allcookiesData.get(cookie.name).put("domain", cookie.domain);
	                allcookiesData.get(cookie.name).put("path", cookie.path);
	                allcookiesData.get(cookie.name).put("expires", cookie.expires.toString());
	                allcookiesData.get(cookie.name).put("httpOnly", cookie.httpOnly.toString());
	                allcookiesData.get(cookie.name).put("secure", cookie.secure.toString());
	                allcookiesData.get(cookie.name).put("sameSite", cookie.sameSite.toString());
	                //log.info("Cookie Name : "+cookie.name+" Cookie Domain : "+cookie.domain);

	            }

	    		listOfCookies.put(entry.getKey(), allcookiesData);

	    	});



	    	return listOfCookies;
	    }
	
	

		//get all pbjs features and events fired


	

    private List<Request> getBidExRequestObjects(List<Request> listOfPwRequestObjects, String environmentHost) {

        // get bidex url's Request object from listOfPwRequestObjects
        // assumer the pattern like this
        // /**hbx.media.net|warp.media.net|hb.yahoo.net|contextual.media.net**bidexchange.js|clienttag.js**
        // from pw req object will have actual url filed, size, time taken to load
        // js errors arising from this controller

        String regEx = ".*/(" + String.join("|", bidExHosts) + "|" + environmentHost + ")/("
                + String.join("|", bidExControlers) + ").*";

        List<Request> bidExRequestObjects = new ArrayList<>();

        for (Request pwReq : listOfPwRequestObjects) {

            // URL GLOBAL MATCHING
            if (pwReq.url().matches(regEx)) {

                bidExRequestObjects.add(pwReq);
            }

        }

        return bidExRequestObjects;
    }

    private List<String> getConsoleErrors(Page page) {
        List<String> consoleErrors = new ArrayList<>();

        // String controllerRegex = "**/" + bidExControlers + "/**";
        String controllerRegex = ".*/(" + String.join("|", bidExControlers) + ").*";

        page.onConsoleMessage(consoleMessage -> {

            if (consoleMessage.type().equals("error") && consoleMessage.location().matches(controllerRegex)) {
                consoleErrors.add(consoleMessage.text());
            }
        });
        return consoleErrors;
    }


	private BidExController getBidExData(List<Request> listOfPwRequestObjects, TestCriteriaBo testData) {

		// get bidex url's Request object from listOfPwRequestObjects
		// assumer the pattern like this
		// /**hbx.media.net|warp.media.net|hb.yahoo.net|contextual.media.net**bidexchange.js|clienttag.js**
		// from pw req object will have actual url filed, size, time taken to load
		// js errors arising from this controller

		BidExController bidexReport = BidExController.builder().build();
		List<Request> bidExRequestObjects = getBidExRequestObjects(listOfPwRequestObjects, testData.getEnvironment());

		// get matching urls
		for (Request request : bidExRequestObjects) {
			bidexReport.getMatchingUrls().add(request.url());

			if (null != request.failure()) {
				bidexReport.getConsoleMessages().add("Request has failed. " + request.failure().toString());
				continue;
			}

			Timing timing = request.timing();

			if (null != timing) {
				Date date = new Date((long) (timing.responseEnd - timing.responseStart));
				bidexReport.setTotalResponseTime(date.getTime() + "ms");
			} else {
				bidexReport.setTotalResponseTime("Not Available");
			}

			bidexReport.setTotalSizeDownloaded(request.sizes().responseBodySize + "b");
			
			bidexReport.setBidexData(getBidExData(request));
			
		}

		if (bidExRequestObjects.size() == 0) {
			bidexReport.getConsoleMessages().add("No bidex request found");
		}

		if (bidExRequestObjects.size() > 1) {
			bidexReport.getConsoleMessages().add("Multiple bidex request found");
		}

		return bidexReport;

	}
	
	
	
	
	

	private BidexData getBidExData(Request request) {

		BidexData bidexData = BidexData.builder().build();
		
		ObjectMapper mapper=new ObjectMapper();

		//get data from request
		// convert into request into json
		String response=request.response().text();
		
	
		
		try {
		
			
			if(request.url().startsWith("https://warp.media.net/js/tags/clientag.js"))
			{
				
				String target="new s.a({";
				
				if(response.contains(target))
				{
			
					int in=response.indexOf(target);
			
					response=response.substring(in);
					
					Pattern pattern=Pattern.compile("new\\s+s\\.a\\((.*?)\\)");
					
					Matcher matcher=pattern.matcher(response);
					
					if(matcher.find())
					{
						String extractedData=matcher.group(1);
						
						JsonNode bidData=mapper.readValue(extractedData, JsonNode.class);
						
						bidexData.setCid(bidData.get("cid").asText());
						bidexData.setSvr(bidData.get("sver").asText());
						bidexData.setSugd(bidData.get("sugd").asText());
//						bidexData.setItype(bidData.get("itype").asText());
						
						
						return bidexData;
					}
		
				}
					
			}

		
			 int startIndex=0;


			 String startTarget = "{\"AaxDpCeiling\"";
			if (response.contains(startTarget)) {
				startIndex = response.indexOf(startTarget);
				startIndex += 12;

			}

			//control in Bidexconfig.java
			String finalString = response.substring(startIndex-12);
			 JsonNode bidexJson= mapper.readValue(finalString, JsonNode.class);

			
			JsonNode envData=bidexJson.get("Environment");
			
			bidexData.setCid(envData.get("cid").asText());
			bidexData.setSvr(envData.get("sver").asText());
			bidexData.setSugd(envData.get("sugd").asText());
			bidexData.setItype(envData.get("itype").asText());
			bidexData.setCt(envData.get("ip2ct").asText())	;		
		}
		catch (Exception e) {
			
			e.printStackTrace();
			
		}
		

		return bidexData;
	}

	private Map.Entry<String, String> positiveURLPatternExists(String currentUrl, TestCriteriaBo testData) {

		for (Map.Entry<String, String> urlData : testData.getNetworkCallsPositive().entrySet()) {

			if (currentUrl.startsWith(urlData.getValue())) {

				return urlData;
			}
		}

		return null;
	}

	private Map.Entry<String, String> negativeURLPatternExists(String currentUrl, TestCriteriaBo testData) {

		for (Map.Entry<String, String> urlData : testData.getNetworkCallsNegative().entrySet()) {

			if (urlData.getValue().startsWith("pattern: ")) { // pattern: .*\.media\.net.*
				if (currentUrl.matches(urlData.getValue().substring(9))) { // substring
					return urlData;
				}
			}

			else if (currentUrl.startsWith(urlData.getValue())) {

				return urlData;
			}
		}

		return null;
	}

	private URLChecksReportModel getUrlAssertionsData(List<String> requestList,List<Request> requests, TestCriteriaBo testData) {

		URLChecksReportModel urlChecks = URLChecksReportModel.builder().build();

		for (String requestUrl : requestList) {
			
			
			
//			if(!URLDataProcessing.isValidUrl(requestUrl))
//			{
//				System.err.println();
//				
//				continue;
//			}
			
			Map.Entry<String, String> urlData = positiveURLPatternExists(requestUrl, testData);

			if (urlData != null) {

				PositiveURLChecks labelExists = urlChecks.getPositiveUrlChecks().get(urlData.getKey());

				if (labelExists == null) {

					PositiveURLChecks puc = PositiveURLChecks.builder().label(urlData.getKey())
							.urlPattern(urlData.getValue())
							.matchingUrls(new ArrayList<String>(Arrays.asList(requestUrl)))
							.queryStringMap(new ArrayList<HashMap<String, String>>(
									Arrays.asList(urlDataProcessing.getQueryStringMapFromUrl(requestUrl, requests))))
							.negativeKeyValChecks(testData.getKeysToVerifyNegative().get(urlData.getKey()))
							.positiveKeyValChecks(testData.getKeysToVerifyPositive().get(urlData.getKey()))
							.anyOneOfKeyValChecks(testData.getKeysToVerifyAnyOneOf().get(urlData.getKey())).build();

					urlChecks.getPositiveUrlChecks().put(urlData.getKey(), puc);
				} else {
					labelExists.getMatchingUrls().add(requestUrl);
					labelExists.getQueryStringMap().add(urlDataProcessing.getQueryStringMapFromUrl(requestUrl,requests));
				}

				continue;
			}

			urlData = negativeURLPatternExists(requestUrl, testData);

			if (urlData != null) {
				NegativeURLChecks nuc = urlChecks.getNegativeUrlChecks().get(urlData.getKey());
				if (nuc == null) {
					urlChecks.getNegativeUrlChecks().put(urlData.getKey(),
							NegativeURLChecks.builder().label(urlData.getKey()).urlPattern(urlData.getValue())
									.matchingUrls(new ArrayList<String>((Arrays.asList(requestUrl)))).build());
				} else {
					nuc.getMatchingUrls().add(requestUrl);
				}
			}
		}

		urlChecks.updateMatchingUrlsCount();
		urlChecks.updateNetworkCallErrors(testData);
		urlChecks.verifyPositiveKeyValChecks();
		if(testData.getBidderDataAssertions()!=null && testData.getBidderDataUrls()!=null)
		{
			try {
				payloadAsserstions.verifyBidderDataAssertions(urlChecks,requests,testData);
			}
			catch (Exception e) {
				
				e.printStackTrace();
//				payloadAsserstions.verifyBidderDataAssertions(urlChecks,requests,testData);
			}
			
		}
		
		return urlChecks;
	}
	

	public String getMockingRule()
	{
		return "function modifyResponse(args) {\n"
				+ "  const {\n"
				+ "    method,\n"
				+ "    url,\n"
				+ "    response,\n"
				+ "    responseType,\n"
				+ "    requestHeaders,\n"
				+ "    requestData,\n"
				+ "    responseJSON,\n"
				+ "  } = args;\n"
				+ "  // Change response below depending upon request attributes received in args\n"
				+ "  const resp = {\n"
				+ "    id: requestData.id,\n"
				+ "    seatbid: [\n"
				+ "      {\n"
				+ "        bid: [\n"
				+ "          {\n"
				+ "            id: requestData.id,\n"
				+ "            impid: requestData.imp[0].id,\n"
				+ "            price: 20,\n"
				+ "            adm: '<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><VAST version=\"2.0\">\\n    <Ad id=\"Vidible\">\\n        <InLine>\\n            <AdSystem>Vidible</AdSystem>\\n            <AdTitle>\\n                <![CDATA[ SSP Passback VAST Test Creative [DO NOT DELETE] ]]>\\n            </AdTitle>\\n            <Description/>\\n            <Impression><![CDATA[https://ap-southeast-1-web-oao.ssp.yahoo.com/admax/adEvent.do?tidi=770927458&dcn=8a969516017a7a396ec539d97f540011&posi=1544503&grp=%3F%3F%3F&nl=1697627333264&rts=1697627333239&pix=1&et=1&a=8187ab43-b46b-45ca-bab7-d4e2238b844a&m=aXAtMTAtMjItMTMxLTE5OA..&b=MTMxMDc7UHVibGlzaGVyIFBhc3NiYWNrIFNlYXQ7Pz8_Ozs7OzAyZjFlMGE3Y2E2YzRjZDZiYzgwYjNmZWJjNjMwNTMxOzMwNTI2OTU1Oy0xOzswOzswOztwYXNzYmFjay0xMjU2Mzs7MjsxOw..&uid=y-Z2kr3XpE2rNrYmGbrsFmf0BB4IaCi0KVLdDcEHg9QrkO2XC6TAR9ZrrmOObczd3PvjtmA9w9gXkLIgTM%7EA&xdi=Pz8_fD8_P3w_Pz98N3xEZXNrdG9w&xoi=MHxJTkQ.&hb=true&type=2&hbp=252&af=7&dety=5]]></Impression><Impression>\\n                <![CDATA[ https://trk.vidible.tv/trk/vast-impression.gif?vid=60f689d84d33307c015c051a&bcid=56d5921ce4b0f5991ad44713&vtid=60f68a56b881e366382a3e2a ]]>\\n            </Impression>\\n            <Creatives>\\n                <Creative>\\n                    <Linear>\\n                        <Duration>00:00:17</Duration>\\n                        <TrackingEvents>\\n                            <Tracking event=\"creativeView\">\\n                                <![CDATA[ https://trk.vidible.tv/trk/vast-view.gif?vid=60f689d84d33307c015c051a&bcid=56d5921ce4b0f5991ad44713&vtid=60f68a56b881e366382a3e2a ]]>\\n                            </Tracking>\\n                            <Tracking event=\"start\">\\n                                <![CDATA[ https://trk.vidible.tv/trk/vast-play.gif?vid=60f689d84d33307c015c051a&bcid=56d5921ce4b0f5991ad44713&vtid=60f68a56b881e366382a3e2a ]]>\\n                            </Tracking>\\n                            <Tracking event=\"firstQuartile\">\\n                                <![CDATA[ https://trk.vidible.tv/trk/vast-quartile.gif?vid=60f689d84d33307c015c051a&q=1&bcid=56d5921ce4b0f5991ad44713&vtid=60f68a56b881e366382a3e2a ]]>\\n                            </Tracking>\\n                            <Tracking event=\"midpoint\">\\n                                <![CDATA[ https://trk.vidible.tv/trk/vast-quartile.gif?vid=60f689d84d33307c015c051a&q=2&bcid=56d5921ce4b0f5991ad44713&vtid=60f68a56b881e366382a3e2a ]]>\\n                            </Tracking>\\n                            <Tracking event=\"thirdQuartile\">\\n                                <![CDATA[ https://trk.vidible.tv/trk/vast-quartile.gif?vid=60f689d84d33307c015c051a&q=3&bcid=56d5921ce4b0f5991ad44713&vtid=60f68a56b881e366382a3e2a ]]>\\n                            </Tracking>\\n                            <Tracking event=\"complete\">\\n                                <![CDATA[ https://trk.vidible.tv/trk/vast-quartile.gif?vid=60f689d84d33307c015c051a&q=4&bcid=56d5921ce4b0f5991ad44713&vtid=60f68a56b881e366382a3e2a ]]>\\n                            </Tracking>\\n                        <Tracking event=\"start\"><![CDATA[https://ap-southeast-1-web-oao.ssp.yahoo.com/admax/adEngEvent?tidi=770927458&dcn=8a969516017a7a396ec539d97f540011&posi=1544503&uid=y-Z2kr3XpE2rNrYmGbrsFmf0BB4IaCi0KVLdDcEHg9QrkO2XC6TAR9ZrrmOObczd3PvjtmA9w9gXkLIgTM%7EA&xdi=Pz8_fD8_P3w_Pz98N3xEZXNrdG9w&xoi=MHxJTkQ.&b=MTMxMDc7UHVibGlzaGVyIFBhc3NiYWNrIFNlYXQ7Pz8_Ozs7OzAyZjFlMGE3Y2E2YzRjZDZiYzgwYjNmZWJjNjMwNTMxOzMwNTI2OTU1Oy0xOzswOzswOztwYXNzYmFjay0xMjU2Mzs7MjsxOw..&hb=true&type=2&hbp=252&a=8187ab43-b46b-45ca-bab7-d4e2238b844a&rts=1697627333239&eet=1]]></Tracking><Tracking event=\"firstQuartile\"><![CDATA[https://ap-southeast-1-web-oao.ssp.yahoo.com/admax/adEngEvent?tidi=770927458&dcn=8a969516017a7a396ec539d97f540011&posi=1544503&uid=y-Z2kr3XpE2rNrYmGbrsFmf0BB4IaCi0KVLdDcEHg9QrkO2XC6TAR9ZrrmOObczd3PvjtmA9w9gXkLIgTM%7EA&xdi=Pz8_fD8_P3w_Pz98N3xEZXNrdG9w&xoi=MHxJTkQ.&b=MTMxMDc7UHVibGlzaGVyIFBhc3NiYWNrIFNlYXQ7Pz8_Ozs7OzAyZjFlMGE3Y2E2YzRjZDZiYzgwYjNmZWJjNjMwNTMxOzMwNTI2OTU1Oy0xOzswOzswOztwYXNzYmFjay0xMjU2Mzs7MjsxOw..&hb=true&type=2&hbp=252&a=8187ab43-b46b-45ca-bab7-d4e2238b844a&rts=1697627333239&eet=2]]></Tracking><Tracking event=\"midpoint\"><![CDATA[https://ap-southeast-1-web-oao.ssp.yahoo.com/admax/adEngEvent?tidi=770927458&dcn=8a969516017a7a396ec539d97f540011&posi=1544503&uid=y-Z2kr3XpE2rNrYmGbrsFmf0BB4IaCi0KVLdDcEHg9QrkO2XC6TAR9ZrrmOObczd3PvjtmA9w9gXkLIgTM%7EA&xdi=Pz8_fD8_P3w_Pz98N3xEZXNrdG9w&xoi=MHxJTkQ.&b=MTMxMDc7UHVibGlzaGVyIFBhc3NiYWNrIFNlYXQ7Pz8_Ozs7OzAyZjFlMGE3Y2E2YzRjZDZiYzgwYjNmZWJjNjMwNTMxOzMwNTI2OTU1Oy0xOzswOzswOztwYXNzYmFjay0xMjU2Mzs7MjsxOw..&hb=true&type=2&hbp=252&a=8187ab43-b46b-45ca-bab7-d4e2238b844a&rts=1697627333239&eet=3]]></Tracking><Tracking event=\"thirdQuartile\"><![CDATA[https://ap-southeast-1-web-oao.ssp.yahoo.com/admax/adEngEvent?tidi=770927458&dcn=8a969516017a7a396ec539d97f540011&posi=1544503&uid=y-Z2kr3XpE2rNrYmGbrsFmf0BB4IaCi0KVLdDcEHg9QrkO2XC6TAR9ZrrmOObczd3PvjtmA9w9gXkLIgTM%7EA&xdi=Pz8_fD8_P3w_Pz98N3xEZXNrdG9w&xoi=MHxJTkQ.&b=MTMxMDc7UHVibGlzaGVyIFBhc3NiYWNrIFNlYXQ7Pz8_Ozs7OzAyZjFlMGE3Y2E2YzRjZDZiYzgwYjNmZWJjNjMwNTMxOzMwNTI2OTU1Oy0xOzswOzswOztwYXNzYmFjay0xMjU2Mzs7MjsxOw..&hb=true&type=2&hbp=252&a=8187ab43-b46b-45ca-bab7-d4e2238b844a&rts=1697627333239&eet=4]]></Tracking><Tracking event=\"complete\"><![CDATA[https://ap-southeast-1-web-oao.ssp.yahoo.com/admax/adEngEvent?tidi=770927458&dcn=8a969516017a7a396ec539d97f540011&posi=1544503&uid=y-Z2kr3XpE2rNrYmGbrsFmf0BB4IaCi0KVLdDcEHg9QrkO2XC6TAR9ZrrmOObczd3PvjtmA9w9gXkLIgTM%7EA&xdi=Pz8_fD8_P3w_Pz98N3xEZXNrdG9w&xoi=MHxJTkQ.&b=MTMxMDc7UHVibGlzaGVyIFBhc3NiYWNrIFNlYXQ7Pz8_Ozs7OzAyZjFlMGE3Y2E2YzRjZDZiYzgwYjNmZWJjNjMwNTMxOzMwNTI2OTU1Oy0xOzswOzswOztwYXNzYmFjay0xMjU2Mzs7MjsxOw..&hb=true&type=2&hbp=252&a=8187ab43-b46b-45ca-bab7-d4e2238b844a&rts=1697627333239&eet=5]]></Tracking></TrackingEvents>\\n                        <VideoClicks>\\n                            <ClickTracking>\\n                                <![CDATA[ https://trk.vidible.tv/trk/vast-click.gif?vid=60f689d84d33307c015c051a&bcid=56d5921ce4b0f5991ad44713&vtid=60f68a56b881e366382a3e2a ]]>\\n                            </ClickTracking>\\n                        </VideoClicks>\\n                        <MediaFiles>\\n                            <MediaFile bitrate=\"300\" height=\"270\" type=\"video/mp4\" width=\"480\">\\n                                <![CDATA[ https://cdn.vidible.tv/prod/2021-07/22/60f935c0ffa52f1fafd58132_v1.mp4 ]]>\\n                            </MediaFile>\\n                        </MediaFiles>\\n                    </Linear>\\n                </Creative>\\n            </Creatives>\\n        <Error><![CDATA[https://ap-southeast-1-web-oao.ssp.yahoo.com/admax/adEngEvent?tidi=770927458&dcn=8a969516017a7a396ec539d97f540011&posi=1544503&uid=y-Z2kr3XpE2rNrYmGbrsFmf0BB4IaCi0KVLdDcEHg9QrkO2XC6TAR9ZrrmOObczd3PvjtmA9w9gXkLIgTM%7EA&xdi=Pz8_fD8_P3w_Pz98N3xEZXNrdG9w&xoi=MHxJTkQ.&b=MTMxMDc7UHVibGlzaGVyIFBhc3NiYWNrIFNlYXQ7Pz8_Ozs7OzAyZjFlMGE3Y2E2YzRjZDZiYzgwYjNmZWJjNjMwNTMxOzMwNTI2OTU1Oy0xOzswOzswOztwYXNzYmFjay0xMjU2Mzs7MjsxOw..&hb=true&type=2&hbp=252&a=8187ab43-b46b-45ca-bab7-d4e2238b844a&rts=1697627333239&eet=6&vec=[ERRORCODE]]]></Error></InLine>\\n    </Ad>\\n</VAST>',\n"
				+ "            adomain: [\"www.demoads.co\"],\n"
				+ "            w: 300,\n"
				+ "            h: 250,\n"
				+ "            crid: \"473512742\",\n"
				+ "            creativeType: \"VIDEO\",\n"
				+ "            nurl: \"https://ap-southeast-1-web-oao.ssp.yahoo.com/admax/adEngEvent?tidi=770927458&dcn=8a969516017a7a396ec539d97f540011&posi=1544503&uid=y-Z2kr3XpE2rNrYmGbrsFmf0BB4IaCi0KVLdDcEHg9QrkO2XC6TAR9ZrrmOObczd3PvjtmA9w9gXkLIgTM~A&xdi=Pz8_fD8_P3w_Pz98N3xEZXNrdG9w&xoi=MHxJTkQ.&b=MTMxMDc7UHVibGlzaGVyIFBhc3NiYWNrIFNlYXQ7Pz8_Ozs7OzAyZjFlMGE3Y2E2YzRjZDZiYzgwYjNmZWJjNjMwNTMxOzMwNTI2OTU1Oy0xOzswOzswOztwYXNzYmFjay0xMjU2Mzs7MjsxOw..&hb=true&type=2&hbp=252&a=8187ab43-b46b-45ca-bab7-d4e2238b844a&rts=1697627333239&eet=7\",\n"
				+ "          },\n"
				+ "        ],\n"
				+ "        seat: \"13107\",\n"
				+ "      },\n"
				+ "    ],\n"
				+ "  };\n"
				+ "  console.log(\n"
				+ "    \"25014\",\n"
				+ "    resp,\n"
				+ "    \"25014\"\n"
				+ "  );\n"
				+ "  return JSON.stringify(resp);\n"
				+ "}";
	}
	
	

	/*private void verifyBidderDataValuesComparisons(URLChecksReportModel urlChecks, List<Request> requestList, TestCriteriaBo testData) {
		Map<String, Map<String, List<URLChecksReportModel.DataAssertion>>> bidderDataAssertions = testData.getBidderDataAssertions();

		for (Map.Entry<String, Map<String, List<URLChecksReportModel.DataAssertion>>> entry : bidderDataAssertions.entrySet()) {
			String bidder = entry.getKey();
			Map<String, List<URLChecksReportModel.DataAssertion>> assertionMap = entry.getValue();

			URLChecksReportModel.BidderDataAssertionsCheck bidderDataAssertionsCheck = URLChecksReportModel.BidderDataAssertionsCheck.builder()
					.label(bidder)
					.urlPattern(testData.getBidderDataUrls().get(bidder))
					.build();

			for (Request request : requestList) {
				String requestBody = request.postData();
				boolean allAssertionMatched = true;

				List<URLChecksReportModel.DataAssertion> assertions = assertionMap.get(bidder);

				for (URLChecksReportModel.DataAssertion assertion : assertions) {
					URLChecksReportModel.MatchingJsonPath matchingJsonPath = assertion.getMatchingJsonPath();
					String matchingJsonPathValue = matchingJsonPath.getJsonPath();
					String matchingJsonPathExpectedValue = matchingJsonPath.getExpectedValue();

					JsonNode matchingJsonNode = domUtils.getJsonNode(requestBody,matchingJsonPathValue);

					if (domUtils.verifyJsonNodeValue(matchingJsonNode,matchingJsonPathExpectedValue)) {
						List<URLChecksReportModel.DataPathValue> requestDataAssertions = assertion.getRequestData();

						for (URLChecksReportModel.DataPathValue requestData : requestDataAssertions) {
							String requestDataJsonPath = requestData.getJsonPath();
							String requestDataExpectedValue = requestData.getValue();

							JsonNode requestDataNode = domUtils.getJsonNode(requestBody,requestDataJsonPath);

							if (!domUtils.verifyJsonNodeValue(requestDataNode,requestDataExpectedValue)) {
								allAssertionMatched = false;
								break;
							}
						}
					} else {
						allAssertionMatched = false;
					}

					if (!allAssertionMatched) {
						bidderDataAssertionsCheck.getTestMessages().add("Assertion failed for " + matchingJsonPathValue + " " + matchingJsonPathExpectedValue);
						break;
					}
				}

				if (allAssertionMatched) {
					bidderDataAssertionsCheck.setTestResult(URLChecksReportModel.TestResult.PASS);
					bidderDataAssertionsCheck.getMatchingUrls().add(request.url());
					break; // Found a matching request, no need to check others
				}
			}

			if (!bidderDataAssertionsCheck.getMatchingUrls().isEmpty()) {
				urlChecks.getBidderDataAssertionsChecks().put(bidder, bidderDataAssertionsCheck);
			}
		}
	}*/


    // private String getNetworkCallAssertions2(List<String> requestList,
    // TestCriteriaBo testData){

    // TestReportBo trb = ReportBuilder.getNewReport(testData);

    // for(Map<String, String> networkCalls: testData.getNetworkCallsPositive()){

    // networkCalls.entrySet().forEach(entry -> {

    // String matchedUrl = urlDataProcessing.getRequestFor2(entry.getValue(),
    // requestList);

    // if(matchedUrl == null){

    // trb.getUrlChecks().add(ReportBuilder.logError(entry.getKey(),
    // CheckType.POSSITIVE, entry.getValue()));

    // }else{

    // UrlChecks uc = ReportBuilder.urlCheckExists(entry.getKey(), trb);

    // if(uc != null){
    // uc.getUrlData().getMatchingUrls().add(matchedUrl);
    // }else{
    // trb.getUrlChecks().add(ReportBuilder.logSuccess(entry.getKey(),
    // CheckType.POSSITIVE, entry.getValue(), matchedUrl));
    // }
    // }
    // });
    // }

    // try {
    // return jsonWriter.writeValueAsString(trb);
    // } catch (JsonProcessingException e) {
    // log.error("json processing error", e);
    // }

    // return "json error";
    // }

    // private String getNetworkCallAssertions(List<Request> requestList,
    // TestCriteriaBo testData){

    // TestReportBo trb = TestReportBo.builder().build();

    // for(Map<String, String> networkCalls: testData.getNetworkCallsPositive()){

    // networkCalls.entrySet().forEach(entry -> {

    // Request request = urlDataProcessing.getRequestFor(entry.getValue(),
    // requestList);

    // if(request == null || request.failure() == null){

    // logError(trb, entry.getKey(), CheckType.POSSITIVE, request);
    // }else{
    // logLogSuccess(trb, entry.getKey(), CheckType.POSSITIVE, request);
    // }

    // });
    // }

    // for(Map<String, String> networkCalls: testData.getNetworkCallsNegative()){

    // networkCalls.entrySet().forEach(entry -> {

    // Request request = urlDataProcessing.getRequestFor(entry.getValue(),
    // requestList);

    // if(request == null || request.failure() == null){

    // logLogSuccess(trb, entry.getKey(), CheckType.NEGERIVE, request);

    // }else{

    // logError(trb, entry.getKey(), CheckType.NEGERIVE, request);
    // }
    // });
    // }

    // try {
    // return jsonWriter.writeValueAsString(trb);
    // } catch (JsonProcessingException e) {
    // log.error("json processing error", e);
    // }

    // return "json error";
    // }

//	private String getIStageParam(String url, String environment) {
//		if (environment.matches(".*.hbistage.internal.ad.tech.*")) {
//			String a[] = environment.split("\\.");
//			String username = a[0];
//			String addToUrl = "&username=" + username;
//			String replaceUrl = url.concat(addToUrl);
//
//			return replaceUrl;
//		}
//		return url;
//	}
	
	
	


    private BrowserContext replaceBidExUrl(BrowserContext browserContext, TestCriteriaBo testData) {

        for (String currentHost : bidExHosts) {
            for (String currentController : bidExControlers) {

                browserContext.route("**/" + currentHost + "/" + currentController + "*", route -> {

                    String replacedURL = "";
                    String originalURl = route.request().url();

                    for (String currentHostInner : bidExHosts) {
                        if (originalURl.contains(currentHostInner)) {
                            replacedURL = originalURl.replace(currentHost, testData.getEnvironment());
                            break;
                        }
                    }

					if(testData.getBidexReplaceString()!=null)
					{
						for (Entry<String, String> entry : testData.bidexReplaceString.entrySet()) {
							
							if(entry==null)
							{
								continue;
							}
							
							else  if (originalURl.contains(entry.getKey())) {
								replacedURL = replacedURL.replace(entry.getKey(), entry.getValue());
							}
						}
					}
					
					if(testData.getBidexAppendString() !=null)
					{
						replacedURL = replacedURL.concat(testData.getBidexAppendString());
					}

					

                    if (testData.getEnvironment().matches(".*.hbistage.internal.ad.tech.*")) {

                        String splitUrl[] = testData.getEnvironment().split("\\.");
                        String username = splitUrl[0];
                        String addToUrl = "&ad_username=" + username;
                        replacedURL = replacedURL.concat(addToUrl);
                    }

//					log.info("replace Url  =  " + replacedURL);
					route.resume(new Route.ResumeOptions().setUrl(replacedURL));
				});
			}
		}

        return browserContext;
    }

    private BrowserContext attachResponseMocking(BrowserContext browserContext, TestCriteriaBo testData) {

		
		for (ResponseSimulateParams responseSim : testData.getResponseSimulate()) {

            browserContext.route(responseSim.getUrlPattern(), route -> {

                // String originalURl = route.request().url();

                String responseBody = new String(route.fetch().body(), StandardCharsets.UTF_8);
                String newResponseBody = domUtils.replaceInJson(responseBody, responseSim.getJsonPath(),
                        responseSim.getOverrideValue());

                if (newResponseBody == responseBody) {
                    route.resume();
                } else {
                    route.fulfill(new Route.FulfillOptions().setBody(newResponseBody));
                }
            });
        }

        return browserContext;
    }

    private BrowserContext injectDOM(BrowserContext browserContext, TestCriteriaBo testData) {

        browserContext.route(testData.getPublisherUrl() + "**", route -> {

            String pageContent = new String(route.fetch().body());

            for (DomInjectParams domInject : testData.getDomInject()) {

                pageContent = domUtils.domManipulate(pageContent, domInject);
            }

            route.fulfill(new Route.FulfillOptions().setBody(pageContent));

        });

        return browserContext;
    }

    public List<String> getExtensionList() {
        List<String> ext = new ArrayList<>();
        ext.add("--disable-extensions-except=" + windScribeExtensionPath);
        ext.add("--load-extension=" + windScribeExtensionPath);
        return ext;
    }

    public void openWindscribe(Page page,String location) throws InterruptedException {
        page.navigate(url);
        page.waitForTimeout(2000);
        if (page.isVisible(windscribeLogin)) {
            try {
                loginToWindscribe(page);
                setLocationInWindscribe(page, location);
            } catch (TimeoutError e) {
                log.error("Element not found ", e);
            }
        } else {
            setLocationInWindscribe(page, location);
        }
    }

    private static void loginToWindscribe(Page page) {
        page.locator(windscribeLogin).click();
        page.locator(windscribeUsername).click();
        page.locator(windscribeUsername).fill(username);
        page.locator(windscribepassword).click();
        page.locator(windscribepassword).fill(password);
        page.locator(windscribeLogin).click();
        //page.locator(windscribeSkip).click();
    }

    private static void setLocationInWindscribe(Page page,String location) {
        page.locator(windscribeLocations).click();
        page.locator(windscribeSearchLocation).click();
        page.locator(windscribeSearchbox).click();
        page.locator(windscribeSearchbox).fill(location);
        page.locator(getWindscribeLocation(location)).click();
        page.close();
    }

    public static String getWindscribeLocation(String location) {
        return windscribeLoc.replace("Loc", location);
    }

    public HashMap<String,Object> getRequestHeaders(Request request) {
        return new HashMap<String,Object>(request.allHeaders());
    }

    public HashMap<String,Object> getResponseHeaders(Request request) {
        return new HashMap<String,Object>(request.response().allHeaders());
    }

// public List<String> getURLAnalyticsFor(Page page, YamlTestCriteria testData){

// //list of arrays initialization
// List<String> urlFromNetworkTab = new ArrayList<>();

// page.onRequest(request -> {

// urlFromNetworkTab.add(request.url());
// });

// page.navigate(testData.getPublisherUrl());
// // log.info(urlFromNetworkTab.toString());
// // return urlFromNetworkTab;

// return urlFromNetworkTab;
// }

// private void buildTestReport(List<String> networkCalls, TestCriteriaBo
// testData) {

// TestReportBo tbo = TestReportBo.builder().build();

// tbo.setTestMetaData(TestReportBo.TestData.builder().environment(testData.getEnvironment())
// .publisherUrl(testData.getPublisherUrl()).userAgent(testData.getUserAgent()).build());

// for (String networkcall : networkCalls) {

// }

// tbo.toString();
// }
}
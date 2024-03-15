package net.media.emma.cop.testsuites.yaml;
import java.awt.Desktop;
import java.net.URI;
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

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
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
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Timing;

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
    URLDataProcessing urlDataProcessing;
    @Autowired
    PlAssestion payloadAsserstions;
    @Autowired
    HeadersAssertion headersAssertion;

    @Value("${harFileBasePath}")
    private String harFileBasePath;
    @Value("${browserPath}")
    private String browserPath;

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
    List<String> bidExControlers = Arrays.asList("bidexchange.js", "clienttag.js");// , "catchpoint_content.js",
    // "topics.js");

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
    public CompletableFuture<TestReportModel> getTestResultsThreadPool(TestCriteriaBo testData) {

    	CompletableFuture<TestReportModel> future = new CompletableFuture<>();
        try {
            TestReportModel testReport = getTestResults(testData); // Create a new browser context for each task
            future.complete(testReport);
        } catch (Exception e) {
        	
        	e.printStackTrace();         
        	log.error("Error executing test: {}", e.getMessage());
            future.completeExceptionally(e);
        }
        return future;




    }

    @Override
    public TestReportModel getTestResults(TestCriteriaBo testData) {

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
            });
		
        }
        
      

		List<String> consoleMessage = new ArrayList<>();
		
		
		

		try {

        
			browserContext.setDefaultTimeout(90000); // 70 seconds
			
			
			
			
			Page page = browserContext.newPage();
			
			consoleMessage = getConsoleErrors(page);
			page.navigate(testData.getPublisherUrl());

//			 page.waitForLoadState(LoadState.NETWORKIDLE,new
//			 Page.WaitForLoadStateOptions().setTimeout(5000));
//			 page.waitForFunction("() => performance.getEntriesByType('resource').length >
//			 0", null,new Page.WaitForFunctionOptions().setTimeout(5));
			Thread.sleep(10000);
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
		try {
		
			URLChecksReportModel  model=getUrlAssertionsData(urlFromNetworkTab,requestList, testData);
			
//			getFeatureReport(harFilePath, urlFromNetworkTab);
			
			testReport.setUrlChecks(model);
		}catch (Exception e) {
			
			testReport.setUrlChecks(null);
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
	
	
	
	public Map<String, List<Cookie>> getCookies(BrowserContext page, String publisher)
    {
    	List<String> url=new ArrayList<>();
    	
    	url.add("https://hbx.media.net");
    	url.add("https://hb.yhoo.net");
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
		String reponse=request.response().text();
		
		try {
			
		
			 int startIndex=0;


			 String startTarget = "{\"AaxDpCeiling\"";
			if (reponse.contains(startTarget)) {
				startIndex = reponse.indexOf(startTarget);
				startIndex += 12;

			}

			//control in Bidexconfig.java
			String finalString = reponse.substring(startIndex-12);
			 JsonNode bidexJson= mapper.readValue(finalString, JsonNode.class);

			
			JsonNode envData=bidexJson.get("Environment");
			
			bidexData.setCid(envData.get("cid").asText());
			bidexData.setSvr(envData.get("sver").asText());
			bidexData.setSugd(envData.get("sugd").asText());
			bidexData.setItype(envData.get("itype").asText());
			
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
									Arrays.asList(urlDataProcessing.getQueryStringMapFromUrl(requestUrl))))
							.negativeKeyValChecks(testData.getKeysToVerifyNegative().get(urlData.getKey()))
							.positiveKeyValChecks(testData.getKeysToVerifyPositive().get(urlData.getKey()))
							.anyOneOfKeyValChecks(testData.getKeysToVerifyAnyOneOf().get(urlData.getKey())).build();

					urlChecks.getPositiveUrlChecks().put(urlData.getKey(), puc);
				} else {
					labelExists.getMatchingUrls().add(requestUrl);
					labelExists.getQueryStringMap().add(urlDataProcessing.getQueryStringMapFromUrl(requestUrl));
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
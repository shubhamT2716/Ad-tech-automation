package net.media.emma.cop.testsuites.yaml;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.playwright.*;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.Geolocation;
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
import net.media.emma.utils.FileOperations;
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

	@Value("${harFileBasePath}")
	private String harFileBasePath;
	@Value("${browserPath}")
	private String browserPath;
	
	@Value("${reportFileBasePath}")
	private String reportFilePath;
	
	@Autowired
	private FileOperations fileOperations;

	@Autowired
	private FileHandling fileHandling;

	@Autowired
	private ResponseMocking mocking;

	BidexData bd = new BidexData();

	private static String windscribeLogin = "xpath=//button[text()='Login']";
	private static String windscribeUsername = "xpath=//input[@name='username']";
	private static String windscribepassword = "xpath=//input[@name='password']";
//    private static String windscribeSkip = "xpath=//div[text()='Skip']";
	private static String username = "mohib1233";
	private static String password = "mohib123";
//    private static String locationParis = "Paris";
//    private static String location = "Paris";
	private static String windscribeLocations = "xpath=//button[@aria-label='Locations']";
	private static String windscribeSearchLocation = "xpath=//div//i";
	private static String windscribeSearchbox = "xpath=//input[contains(@class,'css-1c1txoj')]";
	private static String windscribeLoc = "xpath=(//div[@aria-label='Loc'])[1]";
//    private static String acceptAllBtn = "xpath=//span[text()='Accept all']";

//    private static BrowserContext browserContext;

	String url = "chrome-extension://hnmpcagpplmpfojmgmnngilcnanddlhb/popup.html";
	String windScribeExtensionPath = System.getProperty("user.dir")
			+ "/extensions/hnmpcagpplmpfojmgmnngilcnanddlhb/3.4.6_0";

	// List of strings "1", "2"
	List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net",
			"hbx-stage.media.net", "hbx-stage-2.media.net", "hbx-stage-3.media.net", "hbx-stage-4.media.net",
			"hbx-stage-1.media.net", "ssp-stage-hb.media.net", "ssp-stage-hb-1.media.net", "ssp-stage-hb-2.media.net",
			"ssp-stage-hb-3.media.net", "ssp-stage-hb-4.media.net");
	List<String> bidExControlers = Arrays.asList("bidexchange.js", "clientag.js", "js/tags/clientag.js",
			"ss/nes/bidexl2.js");
	
	List<String> getConsoleMessageOfUrl=Arrays.asList("checksync.php?","bidexchange.js", "clientag.js", "js/tags/clientag.js",
			"ss/nes/bidexl2.js","cksync?");
	
	
	List<String> urlController=Arrays.asList("checksync");

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
	public CompletableFuture<TestReportModel> getTestResultsThreadPool(TestCriteriaBo testData) {

		CompletableFuture<TestReportModel> future = new CompletableFuture<>();
		try {
			TestReportModel testReport = getTestResults(testData, null, null); // Create a new browser context for each
																				// task
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

		
		Page page=null;
		
		String harFilePath = CommonBrowserUtil.getFileNameForHar();
		String location = testData.getLocation();

		BrowserContext browserContext = null;

		
		
		if (location != null) {
			browserContext = BrowserFactory.getSystemBc(testData.getUserAgent(), testData.getCookies(),
					getExtensionList(), harFileBasePath + harFilePath, browserPath, null);
			browserContext.clearCookies();
		} else {
			browserContext = BrowserFactory.getBc(testData.getUserAgent(), testData.getCookies(),
					harFileBasePath + harFilePath);
		}

		try {

			if (testData.getLoginUrl() != null && testData.getUserName() != null && testData.getPassword() != null) {

				doLogin(browserContext, testData);

			}

			browserContext = replaceBidExUrl(browserContext, testData);

			if (testData.getResponseSimulate() != null) {
				browserContext = attachResponseMocking(browserContext, testData);
			}

			// browserContext = injectDOM(browserContext, testData);

			List<String> urlFromNetworkTab = new ArrayList<>();
			List<Request> requestList = new ArrayList<>();

			if (location == null) {
				browserContext.onRequest(request -> {

					urlFromNetworkTab.add(request.url());
					requestList.add(request);

					if (request.url().equals(url)) {
						String postData = request.postData();

						JSONObject jsonObject = new JSONObject(postData);

						id = jsonObject.get("id");

						jsonArray = jsonObject.getJSONArray("imp");
					}

				});

			}

			List<String> consoleMessage = new ArrayList<>();

			JsonNode pbjsData = null;
			JsonNode pbjsEventsData = null;

			JsonNode pbjsConfigData = null;

			try {

				browserContext.setDefaultTimeout(3000); // 30 seconds
				Geolocation geolocation = new Geolocation(48.8575, 2.3514);// latitude and logitude
				log.info("location:  " + geolocation);
				browserContext.setGeolocation(geolocation);
//			browserContext.grantPermissions(new String[]{"geolocation"});

				 page = browserContext.newPage();


				if (url != null && response != null) {

					page = mocking.addResponseMocking(page, url, response);

				}

				try {

					pbjsData = getPbjsData(page);
				} catch (Exception e) {
					pbjsData = null;
				}

				consoleMessage = getConsoleErrors(page);

				page.navigate(testData.getPublisherUrl());
		
//				page.evaluate(googleRefresh());
				
				pbjsConfigData = getPbjsData(page);

				if (testData.getPbjsEvents() != null || !testData.getPbjsEvents().isEmpty()) {

					pbjsEventsData = getPbjsEvents(page, testData.getPbjsEvents());
				}

//			 page.waitForLoadState(LoadState.NETWORKIDLE,new
//			 Page.WaitForLoadStateOptions().setTimeout(5000));
//			 page.waitForFunction("() => performance.getEntriesByType('resource').length >
//			 0", null,new Page.WaitForFunctionOptions().setTimeout(5));

				Thread.sleep(2000);
				
			
			} catch (Exception e) {
//				e.printStackTrace();
				log.error("Error while navigating to publisher url: {}", e.getMessage());
			}
			finally {		
				page.close();

			}

			
			
			BidExController bidExController = getBidExData(requestList, testData);

			bidExController.setConsoleMessages(consoleMessage);

			Map<String, List<Cookie>> getCookies = getCookies(browserContext, testData.getPublisherUrl());

			Map<String, Map<String, Map<String, String>>> cookies = displayCookies(getCookies);

			String userAgent = BrowserFactory.browserContexts.get(testData.getUserAgent());

			TestReportModel testReport = TestReportModel.builder()
					.testMetaData(TestMetaData.builder()
							.dateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
							.testName("YAML Test Suite").publisherUrl(testData.getPublisherUrl())
							.environment(testData.getEnvironment())
//				.userAgent(
//						testData.getUserAgent() + " - " + BrowserFactory.browserContexts.get(testData.getUserAgent()))
							.harFileDownloadLink("/yamlTestSuite/harDownload/" + harFilePath).build())
					.build();

			if (userAgent != null) {
				testReport.getTestMetaData().setUserAgent(
						testData.getUserAgent() + " - " + BrowserFactory.browserContexts.get(testData.getUserAgent()));
			} else {
				testReport.getTestMetaData().setUserAgent(testData.getUserAgent());
			}

			testReport.setGamSlots(addGamAssertion(requestList));
			testReport.setCookie(cookies);
			testReport.setBidExController(bidExController);
			testReport.setPbjs(pbjsConfigData);
			testReport.setPbjsEvents(pbjsEventsData);
			try {

				URLChecksReportModel model = getUrlAssertionsData(urlFromNetworkTab, requestList, testData);
				testReport.setUrlChecks(model);
				testReport.setTestSummery(YamlTestSuiteScheduling.testResultSummery(model));

				try {

					String fileName = this.fileHandling.setReportToFile(testData, model, testReport);

					String downloadUrl = "/downloadFile?fileName=" + URLEncoder.encode(fileName, "UTF-8");

					testReport.getTestMetaData().setDownloadReport(downloadUrl);

				} catch (Exception e) {

					log.info("ExcelReport Error: " + e.getMessage());
					

					testReport.getTestMetaData().setDownloadReport(null);
				}

			} catch (Exception e) {
				
				e.printStackTrace();

				testReport.setUrlChecks(null);

			}

			return testReport;

		} catch (Exception e) {

			e.printStackTrace();
			return null;
		} finally {

			browserContext.close();
		}

	}

	public Map<String, String> addGamAssertion(List<Request> urls) {
		try {
			Map<String, String> mapOfSlotsData = new HashMap<>();

			for (Request request : urls) {

				if (request.url().contains("https://securepubads.g.doubleclick.net/gampad/ads?")) {

					String gamUrl;
					try {
						gamUrl = URLDecoder.decode(request.url(), "UTF-8");

						int as = gamUrl.indexOf("prev_scp");

						gamUrl = gamUrl.substring(as);

						gamUrl = gamUrl.substring(9);

						String slots[] = gamUrl.split("\\|");

						for (String slot : slots) {
							String getValue[] = slot.split("&");

							for (String slotsVal : getValue) {

								String keyVal[] = slotsVal.split("=");
								if (keyVal.length == 2) {
									mapOfSlotsData.put(keyVal[0], keyVal[1]);
								}
							}
						}

					} catch (UnsupportedEncodingException e) {
						
						e.printStackTrace();
					}

				}

			}
			return mapOfSlotsData;
		} catch (Exception e) {
			return null;
		}

	}
	
//	public Page getAwlog(String urls, Page page, String pub)
//	{
//		
//	
//		System.err.println("Function called...");
//		
//		 Page.NavigateOptions navigateOptions = new Page.NavigateOptions()
//		            .setTimeout(240000); // 240 seconds (4 minutes)
//
//		 page.waitForLoadState(LoadState.DOMCONTENTLOADED);
//		 
//		    // Ensure the page navigation completes before proceeding
//		    page.navigate(pub, navigateOptions);
//		
////		page.navigate(pub);
//		
//	    boolean isUrlFired = false;
//	    int maxTries = 5;
//	    int retries = 0;
//
//	    while (!isUrlFired && retries < maxTries) {
//	    	
//	    	System.err.println("lopp started proecessing...");
//	        CompletableFuture<Boolean> urlFiredFuture = new CompletableFuture<>();
//
//	        // Set up the request listener for each attempt
//	        page.onRequest(request -> {
//	            if (request.url().equalsIgnoreCase(urls)) {
//	                System.err.println("URL Fired: " + urls);
//	                urlFiredFuture.complete(true); // Mark the future as completed
//	            }
//	        });
//
//	        try {
//	            // Wait for the URL to be fired or timeout after 10 seconds
//	            urlFiredFuture.get(10, TimeUnit.SECONDS);
//	            isUrlFired = true;  // URL fired successfully, exit the loop
//	        } catch (Exception e) {
//	            // If the URL was not fired within 10 seconds, reload the page and retry
//	            System.err.println("URL not fired, reloading the page... Retry #" + (retries + 1));
//	            page.reload();
//	            retries++;
//	        }
//
//	        // Remove the request listener after each reload to avoid duplicate listeners
//	        page.offRequest(null);
//	    }
//	
//	 return page;
//		
//	}

	public String googleRefresh() {
		return "googletag.pubads().refresh();";
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

	public JsonNode getPbjsEvents(Page page, String pbjsEvent) {
		try {

			Object eventsDataEVal = page.evaluate(getPbjsEventsData(pbjsEvent));
			JsonNode eventsDataNode = jsonReader.readTree(eventsDataEVal.toString());
			log.info("pbjsEvents: ", eventsDataNode.toString());
			return eventsDataNode;
		} catch (Exception e) {

			e.printStackTrace();
			log.error("Error while getting pbjs events: ", e.getMessage());
			return null;
		}

	}

	private JsonNode getPbjsData(Page page) {
		try {
			Object pbjsConfigEval = page.evaluate(returnPbjsConfig());
			JsonNode pbjsDataNode = jsonReader.readTree(pbjsConfigEval.toString());
			log.info("pbjsConfig: ", pbjsDataNode.toString());
			return pbjsDataNode;
		} catch (Exception e) {
			log.error("Error while getting pbjs data: ", e.getMessage());
			return null;
		}
	}

	public Map<String, List<Cookie>> getCookies(BrowserContext page, String publisher) {
		List<String> url = new ArrayList<>();

		url.add("https://hbx.media.net");
//    	url.add("https://hb.yahoo.net");
		url.add("https://contextual.media.net");
		url.add(publisher);

		Map<String, List<Cookie>> mapOfCookie = new HashMap<>();

		for (String li : url) {
			List<Cookie> cookie = page.cookies(li);
			mapOfCookie.put(li, cookie);

		}
		return mapOfCookie;

	}

	public Map<String, Map<String, Map<String, String>>> displayCookies(Map<String, List<Cookie>> cookies) {
		Map<String, Map<String, Map<String, String>>> listOfCookies = new HashMap<>();

		cookies.entrySet().forEach(entry -> {

			Map<String, Map<String, String>> allcookiesData = new HashMap<>();

			for (Cookie cookie : entry.getValue()) {

				allcookiesData.put(cookie.name, new HashMap<>());
				allcookiesData.get(cookie.name).put("value", cookie.value);
				allcookiesData.get(cookie.name).put("domain", cookie.domain);
				allcookiesData.get(cookie.name).put("path", cookie.path);
				allcookiesData.get(cookie.name).put("expires", cookie.expires.toString());
				allcookiesData.get(cookie.name).put("httpOnly", cookie.httpOnly.toString());
				allcookiesData.get(cookie.name).put("secure", cookie.secure.toString());
				allcookiesData.get(cookie.name).put("sameSite", cookie.sameSite.toString());
				// log.info("Cookie Name : "+cookie.name+" Cookie Domain : "+cookie.domain);

			}

			listOfCookies.put(entry.getKey(), allcookiesData);

		});

		return listOfCookies;
	}

	// get all pbjs features and events fired

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
		String controllerRegex = ".*/(" + String.join("|", getConsoleMessageOfUrl) + ").*";
		
		Pattern pattern=Pattern.compile(controllerRegex);
		
		

		page.onConsoleMessage(consoleMessage -> {
			
			Matcher matcher=pattern.matcher(consoleMessage.location());
			
			if ((consoleMessage.type().equals("error") || consoleMessage.type().equals("warning")|| consoleMessage.type().equals("debug")|| consoleMessage.type().equals("startGroupCollapsed")
					|| consoleMessage.type().equals("clear")|| consoleMessage.type().equals("assert")|| consoleMessage.type().equals("log")) && consoleMessage.location().matches(controllerRegex)) {
		

				
				consoleErrors.add(consoleMessage.type()+" : "+consoleMessage.location().trim()+" : "+consoleMessage.text());
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

//			bidexReport.setTotalSizeDownloaded(request.sizes().responseBodySize + "b");
			bidexReport.setTotalSizeDownloaded(getBidexSize(request));

			bidexReport.setBidexData(getBidExData(request));

		}
		size.setLength(0);

		if (bidExRequestObjects.size() == 0) {
			bidexReport.getConsoleMessages().add("No bidex request found");
		}

		if (bidExRequestObjects.size() > 1) {
			bidexReport.getConsoleMessages().add("Multiple bidex request found");
		}

		return bidexReport;

	}

	StringBuilder size = new StringBuilder();

	public String getBidexSize(Request request) {
		List<String> bidex2 = Arrays.asList("ss/nes/bidexl2.js?");
		String regexForBidex2 = ".*/(" + String.join("|", bidExHosts) + ")/(" + String.join("|", bidex2) + ").*";

		List<String> bibex1 = Arrays.asList("bidexchange.js");
		String regexForBidex1 = ".*/(" + String.join("|", bidExHosts) + ")/(" + String.join("|", bibex1) + ").*";

		if (request.url().matches(regexForBidex1)) {
//			size.c("bidexchange.js", request.sizes().responseBodySize + "b");
			
			int responseSize=request.sizes().responseBodySize;
			
			
			
			size.append("bidexchange.js : " + responseSize/1000 + " kb");

		}
		if (request.url().matches(regexForBidex2)) {
			
			int responseSize=request.sizes().responseBodySize;
			
			size.append("  " + "bidexl2.js : " + responseSize/1000 + " kb");
		} else {

			int responseSize=request.sizes().responseBodySize;
			
			return responseSize/1000 + "kb";

		}

		return size.toString();

	}

	public BidexData getBidExData(Request request) {

		BidexData bidexData = BidexData.builder().build();

		ObjectMapper mapper = new ObjectMapper();

		// get data from request
		// convert into request into json
		String response = request.response().text();

		try {

			List<String> clientagController = Arrays.asList("js/tags/clientag.js", "clientag.js");
			String regexForMp = ".*/(" + String.join("|", bidExHosts) + ")/(" + String.join("|", clientagController)
					+ ").*";

			List<String> bibex1 = Arrays.asList("bidexchange.js");
			String regexForBidex1 = ".*/(" + String.join("|", bidExHosts) + ")/(" + String.join("|", bibex1) + ").*";

			List<String> bidex2 = Arrays.asList("ss/nes/bidexl2.js?");
			String regexForBidex2 = ".*/(" + String.join("|", bidExHosts) + ")/(" + String.join("|", bidex2) + ").*";

			if (request.url().matches(regexForMp)) {

				String responseMP = request.response().text();

				List<String> sspMp = Arrays.asList("ssp-stage-hb.media.net", "ssp-stage-hb-1.media.net",
						"ssp-stage-hb-2.media.net", "ssp-stage-hb-3.media.net", "ssp-stage-hb-4.media.net");

				String regexForSSPMp = ".*/(" + String.join("|", sspMp) + ")/(" + String.join("|", clientagController)
						+ ").*";

				if (request.url().matches(regexForSSPMp)) {

					boolean svrFind = false;

//					String t = "new o.a({";
//
//					System.err.println(responseMP.contains(t));
//					
//					if (responseMP.contains(t)) {
//						int index = responseMP.indexOf(t);
//
//						responseMP = responseMP.substring(index);
//
//						Pattern pattern = Pattern.compile("new\\s+o\\.a\\((.*?)\\)");
//
//						Matcher matcher = pattern.matcher(responseMP);
//
//						if (matcher.find()) {
//
//							String extractedData = matcher.group(1);
//
//							String svr = null;
//
//							if (responseMP.contains("var i")) {
//								int svrIndex = responseMP.indexOf("var i");
//
//								responseMP = responseMP.substring(svrIndex);
//
//								Pattern pattern2 = Pattern.compile("var i=\"([^\"]+)\"");
//
//								Matcher matcher2 = pattern2.matcher(responseMP);
//
//								if (matcher2.find()) {
//
//									System.out.println("match found");
//
//									svr = matcher2.group(1);
//
//								}
//
//							}
//
//							JsonNode bidData = mapper.readValue(extractedData, JsonNode.class);
//
//							bidexData.setCid(bidData.get("cid").asText());
//
//							if (svr == null) {
//
//								System.out.println(" data 1: " + bidData.get("sver").asText());
//								bidexData.setSvr(bidData.get("sver").asText());
//							} else {
//
//								System.out.println(" data 2: " + svr);
//
//								bidexData.setSvr(svr);
//							}
//
//							bidexData.setSugd(bidData.get("sugd").asText());
//							bidexData.setCt(bidData.get("ip2ct").asText());
//
//							return bidexData;
//						}
//
//					}

					if (svrFind == false) {
						
						
						String target = "\"Environment\":{";

						
						String regex="\"Environment\":\\{([^}]*)\\}";
				        
						
		

						if (responseMP.contains(target)) {
							
							String res=responseMP;

							int in = responseMP.indexOf(target);

							responseMP = responseMP.substring(in);

							

							try {
								Pattern pattern = Pattern.compile(regex);
								
								Matcher matcher = pattern.matcher(responseMP);
														
								if(matcher.find())
								{
									
								String extractedData=matcher.group();
								
								String svr=null;
								
//								if (matcher.find()) {
//
//									
//
//									String extractedData = matcher.group();
//									
//									System.out.println(extractedData);
//									
//									
//
//								String svr = null;
//								
//								if(responseMP.contains("const i"))
//								{
//									int svrIndex=responseMP.indexOf("const i");
//									
//									responseMP=responseMP.substring(svrIndex);
//									
////									System.out.println(responseMP);
//									
//									Pattern pattern2=Pattern.compile("const i=\"([^\"]+)\"");
//									
//									Matcher matcher2=pattern2.matcher(responseMP);
//									
//									if(matcher2.find())
//									{
//									
//										 svr = matcher2.group(1);
//				
//									}
//									
//								}

								System.out.println("Data:   : "+extractedData);
								
								
								JSONObject jsonObject=new JSONObject("{"+extractedData+"}");
								
								System.err.println(jsonObject);
								
								JSONObject  object= jsonObject.getJSONObject("Environment");
								
								System.out.println(object.get("cid").toString());
								
//									JsonNode bidData = mapper.readValue(object, JsonNode.class);
									
									
									bidexData.setCid(object.get("cid").toString());

								if(svr==null)
								{
									String targetSvr = "function createErrorObject(error) {";

									int i = res.indexOf(targetSvr);

									res = res.substring(i);

									Pattern pattern1 = Pattern.compile("svr\\s*:\\s*\"([^\"]*)\"");

									Matcher matcher1 = pattern1.matcher(res);
									
									if (matcher1.find()) {
								
										 svr = matcher1.group(1);

										bidexData.setSvr(svr);

									}
									
								}

//								bidexData.setSvr(bidData.get("sver").asText());
									bidexData.setSugd(object.get("sugd").toString());
									bidexData.setCt(object.get("ip2ct").toString());
									bidexData.setServname(object.get("servname").toString());
									bidexData.setBrowser(object.get("browser").toString());
									bidexData.setLw(object.get("lw").toString());
									
									
									return bidexData;
								}
							} catch (Exception e) {
								log.info("getting error at " + e.getMessage());

							}
						}
						
						

						String targetsvr = "function createErrorObject(error) {";

						int in = responseMP.indexOf(targetsvr);

						responseMP = responseMP.substring(in);

						Pattern pattern1 = Pattern.compile("svr\\s*:\\s*\"([^\"]*)\"");

						Matcher matcher1 = pattern1.matcher(responseMP);

						if (matcher1.find()) {

							String svr = matcher1.group();

							System.out.println(svr);

							String svr1 = matcher1.group(1);

//							System.out.println(svr);

							

							bidexData.setSvr(svr1);

						}

					}

				} else {

//				String target="new r.a({";  String regex="new\\s+r\\.a\\((.*?)\\)";

					String target = "\"Environment\":{";

					
					String regex="\"Environment\":\\{([^}]*)\\}";
					
					if (responseMP.contains(target)) {
						
						String res=responseMP;

						int in = responseMP.indexOf(target);

						responseMP = responseMP.substring(in);

						

						try {
							Pattern pattern = Pattern.compile(regex);
							
							Matcher matcher = pattern.matcher(responseMP);
				
							
							if (matcher.find()) {

							String extractedData = matcher.group();

							String svr = null;
//							
//							if(responseMP.contains("const i"))
//							{
//								int svrIndex=responseMP.indexOf("const i");
//								
//								responseMP=responseMP.substring(svrIndex);
//								
////								System.out.println(responseMP);
//								
//								Pattern pattern2=Pattern.compile("const i=\"([^\"]+)\"");
//								
//								Matcher matcher2=pattern2.matcher(responseMP);
//								
//								if(matcher2.find())
//								{
//								
//									 svr = matcher2.group(1);
//			
//								}
//								
//							}
							

							JSONObject jsonObject=new JSONObject("{"+extractedData+"}");
							
							JSONObject  object= jsonObject.getJSONObject("Environment");
							
														
								bidexData.setCid(object.get("cid").toString());

							if(svr==null)
							{
								String targetSvr = "function createErrorObject(error) {";

								int i = res.indexOf(targetSvr);

								res = res.substring(i);

								Pattern pattern1 = Pattern.compile("svr\\s*:\\s*\"([^\"]*)\"");

								Matcher matcher1 = pattern1.matcher(res);
								
								if (matcher1.find()) {

									
									 svr = matcher1.group(1);

									System.err.println(svr);

									bidexData.setSvr(svr);

								}
								
							}

								bidexData.setSugd(object.get("sugd").toString());
								bidexData.setCt(object.get("ip2ct").toString());
								bidexData.setServname(object.get("servname").toString());
								bidexData.setBrowser(object.get("browser").toString());
								bidexData.setLw(object.get("lw").toString());

								return bidexData;
							}
						} catch (Exception e) {
							log.info("getting error at " + e.getMessage());

						}

						String targetSvr = "function createErrorObject(error) {";

						int i = responseMP.indexOf(targetSvr);

						responseMP = responseMP.substring(i);

						Pattern pattern1 = Pattern.compile("svr\\s*:\\s*\"([^\"]*)\"");

						Matcher matcher1 = pattern1.matcher(responseMP);

						if (matcher1.find()) {

							log.info(matcher1.toString());

							String svr = matcher1.group();

							log.info(svr);

							String svr1 = matcher1.group(1);



							log.info(svr1);

							bidexData.setSvr(svr1);

							

						}

					}
				}

			}
//			else if(request.url().contains("**/bidexchange.js?")|| request.url().startsWith("https://hbx-stage-2.media.net//bidexchange.js")|| request.url().startsWith("https://hbx-stage-3.media.net//bidexchange.js")|| request.url().startsWith("https://hbx-stage-4.media.net//bidexchange.js")|| request.url().startsWith("https://hbx-stage.media.net//bidexchange.js")|| request.url().startsWith("https://hbx.media.net//bidexchange.js?")|| request.url().startsWith("https://hbx.media.net/bidexchange.js?")|| request.url().startsWith("https://ssp-stage-hb.media.net/bidexchange.js?")|| request.url().startsWith("https://ssp-stage-hb-1.media.net//bidexchange.js")|| request.url().startsWith("https://ssp-stage-hb-2.media.net//bidexchange.js")|| request.url().startsWith("https://ssp-stage-hb-3.media.net//bidexchange.js")|| request.url().startsWith("https://ssp-stage-hb-4.media.net//bidexchange.js")){
			else if (request.url().matches(regexForBidex1)) {

				int startIndex = 0;

				String startTarget = "{\"AaxDpCeiling\"";
				if (response.contains(startTarget)) {
					startIndex = response.indexOf(startTarget);
					startIndex += 12;

				}

				// control in Bidexconfig.java
				String finalString = response.substring(startIndex - 12);
				JsonNode bidexJson = mapper.readValue(finalString, JsonNode.class);

				JsonNode envData = bidexJson.get("Environment");

				bidexData.setCid(envData.get("cid").asText());
				bidexData.setSvr(envData.get("sver").asText());
				bidexData.setSugd(envData.get("sugd").asText());
//				bidexData.setItype(envData.get("itype").asText());
				bidexData.setCt(envData.get("ip2ct").asText());
				bidexData.setServname(envData.get("servname").toString());
				bidexData.setBrowser(envData.get("browser").toString());
				bidexData.setLw(envData.get("lw").toString());

				bd.setCid(envData.get("cid").asText());
				bd.setCt(envData.get("ip2ct").asText());
				bd.setSvr(envData.get("sver").asText());
				bd.setSugd(envData.get("sugd").asText());
				bd.setServname(envData.get("servname").toString());
				bd.setBrowser(envData.get("browser").toString());
				bd.setLw(envData.get("lw").toString());
				
//				bd.setItype(envData.get("itype").asText());
			}

			if (request.url().matches(regexForBidex2)) {

				bidexData.setCid(bd.getCid());
				bidexData.setSvr(bd.getSvr());
				bidexData.setCt(bd.getCt());
//				bidexData.setItype(bd.getItype());
				bidexData.setSugd(bd.getSugd());
				bidexData.setServname(bd.getServname());
				bidexData.setBrowser(bd.getBrowser());
				bidexData.setLw(bd.getLw());
				

			}

		} catch (Exception e) {

			log.error("Error in fetch Svr: " + e.getMessage());

			e.printStackTrace();

		}

		return bidexData;
	}

	private List<Map.Entry<String, String>> positiveURLPatternExists(Request currentUrl, TestCriteriaBo testData) {
		
		
		 List<Map.Entry<String, String>> matchingEntries = new ArrayList<>();

		    // Collect all matching entries
		    for (Map.Entry<String, String> entry : testData.getNetworkCallsPositive().entrySet()) {
		        if (currentUrl.url().startsWith(entry.getValue())) {
		            matchingEntries.add(entry);  // Store all matches
		        }
		    }

		    return matchingEntries; // Return the list of matching entries
		
		
		
//		for (Map.Entry<String, String> urlData : testData.getNetworkCallsPositive().entrySet()) {
//
//			if (currentUrl.url().startsWith(urlData.getValue())) {
//
//				return urlData;
//			}
//		}
//
//		return null;
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

	private URLChecksReportModel getUrlAssertionsData(List<String> requestList, List<Request> requests,
			TestCriteriaBo testData) {

		URLChecksReportModel urlChecks = URLChecksReportModel.builder().build();

		for (Request requestUrl : requests) {
			
			

			List<Map.Entry<String, String>> urls = positiveURLPatternExists(requestUrl, testData);

			for(Map.Entry<String, String> urlData: urls)
			{
				if (urlData != null) {

					PositiveURLChecks labelExists = urlChecks.getPositiveUrlChecks().get(urlData.getKey());

					if (labelExists == null) {

						PositiveURLChecks puc = PositiveURLChecks.builder().label(urlData.getKey())
								.urlPattern(urlData.getValue())
								.matchingUrls(new ArrayList<String>(Arrays.asList(requestUrl.url())))
								.queryStringMap(new ArrayList<Map<String, String>>(
										Arrays.asList(urlDataProcessing.getQueryStringMapFromUrl(requestUrl, requests))))
//								.queryStringMap(urlDataProcessing.getQueryStringMapFromUrl(url, requests))
								.negativeKeyValChecks(testData.getKeysToVerifyNegative().get(urlData.getKey()))
								.positiveKeyValChecks(testData.getKeysToVerifyPositive().get(urlData.getKey()))
								.anyOneOfKeyValChecks(testData.getKeysToVerifyAnyOneOf().get(urlData.getKey())).build();

						urlChecks.getPositiveUrlChecks().put(urlData.getKey(), puc);
					} else {
						labelExists.getMatchingUrls().add(requestUrl.url());
						labelExists.getQueryStringMap().add(urlDataProcessing.getQueryStringMapFromUrl(requestUrl, requests));
					}

//					continue;
				}
			}


			Map.Entry<String, String>	urlData = negativeURLPatternExists(requestUrl.url(), testData);

			if (urlData != null) {
				NegativeURLChecks nuc = urlChecks.getNegativeUrlChecks().get(urlData.getKey());
				if (nuc == null) {
					urlChecks.getNegativeUrlChecks().put(urlData.getKey(),
							NegativeURLChecks.builder().label(urlData.getKey()).urlPattern(urlData.getValue())
									.matchingUrls(new ArrayList<String>((Arrays.asList(requestUrl.url())))).build());
				} else {
					nuc.getMatchingUrls().add(requestUrl.url());
				}
			}
		}
		
//		urlChecks.setGetpayloadDetails(getPayload(testData, requests));
		urlChecks.updateMatchingUrlsCount();
		urlChecks.updateNetworkCallErrors(testData);
		urlChecks.verifyPositiveKeyValChecks();
		
		if (testData.getBidderDataAssertions() != null && testData.getBidderDataUrls() != null) {
			try {
				payloadAsserstions.verifyBidderDataAssertions(urlChecks, requests, testData);
			
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		

		return urlChecks;
	}
	
	
	public List<Map<String, String>> getPayload(TestCriteriaBo testData, List<Request> requestlist)
	{
		List<Map<String, String>> queryParam=new ArrayList<>();
		
		try {
		
			testData.getNetworkCallsPositive().entrySet().forEach(entry->{
				
				
				if(entry.getKey().equals("apprlog"))
				{
					System.out.println("in process start");

					boolean temp=false;
					for(Request  req: requestlist)
					{
						if(req.url().equalsIgnoreCase(entry.getValue()))
						{
							
//							if(temp==false)
//							{
								queryParam.add(urlDataProcessing.queryStringToMap(req.postData()));
								temp=true;
//							}
							
						}
					}
				}
				
			});		
			
		}
		catch (Exception e) {
			
			System.err.println(e.getMessage());
			
		}
		
		
		
		return queryParam;
	}
	

	/*
	 * private void verifyBidderDataValuesComparisons(URLChecksReportModel
	 * urlChecks, List<Request> requestList, TestCriteriaBo testData) { Map<String,
	 * Map<String, List<URLChecksReportModel.DataAssertion>>> bidderDataAssertions =
	 * testData.getBidderDataAssertions();
	 * 
	 * for (Map.Entry<String, Map<String, List<URLChecksReportModel.DataAssertion>>>
	 * entry : bidderDataAssertions.entrySet()) { String bidder = entry.getKey();
	 * Map<String, List<URLChecksReportModel.DataAssertion>> assertionMap =
	 * entry.getValue();
	 * 
	 * URLChecksReportModel.BidderDataAssertionsCheck bidderDataAssertionsCheck =
	 * URLChecksReportModel.BidderDataAssertionsCheck.builder() .label(bidder)
	 * .urlPattern(testData.getBidderDataUrls().get(bidder)) .build();
	 * 
	 * for (Request request : requestList) { String requestBody =
	 * request.postData(); boolean allAssertionMatched = true;
	 * 
	 * List<URLChecksReportModel.DataAssertion> assertions =
	 * assertionMap.get(bidder);
	 * 
	 * for (URLChecksReportModel.DataAssertion assertion : assertions) {
	 * URLChecksReportModel.MatchingJsonPath matchingJsonPath =
	 * assertion.getMatchingJsonPath(); String matchingJsonPathValue =
	 * matchingJsonPath.getJsonPath(); String matchingJsonPathExpectedValue =
	 * matchingJsonPath.getExpectedValue();
	 * 
	 * JsonNode matchingJsonNode =
	 * domUtils.getJsonNode(requestBody,matchingJsonPathValue);
	 * 
	 * if
	 * (domUtils.verifyJsonNodeValue(matchingJsonNode,matchingJsonPathExpectedValue)
	 * ) { List<URLChecksReportModel.DataPathValue> requestDataAssertions =
	 * assertion.getRequestData();
	 * 
	 * for (URLChecksReportModel.DataPathValue requestData : requestDataAssertions)
	 * { String requestDataJsonPath = requestData.getJsonPath(); String
	 * requestDataExpectedValue = requestData.getValue();
	 * 
	 * JsonNode requestDataNode =
	 * domUtils.getJsonNode(requestBody,requestDataJsonPath);
	 * 
	 * if (!domUtils.verifyJsonNodeValue(requestDataNode,requestDataExpectedValue))
	 * { allAssertionMatched = false; break; } } } else { allAssertionMatched =
	 * false; }
	 * 
	 * if (!allAssertionMatched) {
	 * bidderDataAssertionsCheck.getTestMessages().add("Assertion failed for " +
	 * matchingJsonPathValue + " " + matchingJsonPathExpectedValue); break; } }
	 * 
	 * if (allAssertionMatched) {
	 * bidderDataAssertionsCheck.setTestResult(URLChecksReportModel.TestResult.PASS)
	 * ; bidderDataAssertionsCheck.getMatchingUrls().add(request.url()); break; //
	 * Found a matching request, no need to check others } }
	 * 
	 * if (!bidderDataAssertionsCheck.getMatchingUrls().isEmpty()) {
	 * urlChecks.getBidderDataAssertionsChecks().put(bidder,
	 * bidderDataAssertionsCheck); } } }
	 */

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

	private BrowserContext replaceBidExUrl(BrowserContext browserContext, TestCriteriaBo testData) {

		List<String> allUrls=new ArrayList<>();
		allUrls.addAll(bidExControlers);
//		allUrls.addAll(urlController);
		
		
		for (String currentHost : bidExHosts) {
			for (String currentController : allUrls) {

				browserContext.route("**/" + currentHost + "/" + currentController + "*", route -> {

					String replacedURL = "";
					String originalURl = route.request().url();

					for (String currentHostInner : bidExHosts) {
						if (originalURl.contains(currentHostInner)) {
							replacedURL = originalURl.replace(currentHost, testData.getEnvironment());
							break;
						}
					}

					if (testData.getBidexReplaceString() != null) {
						for (Entry<String, String> entry : testData.bidexReplaceString.entrySet()) {

							if (entry == null) {
								continue;
							}

							else if (originalURl.contains(entry.getKey())) {
								replacedURL = replacedURL.replace(entry.getKey(), entry.getValue());
							}
						}
					}

					if (testData.getBidexAppendString() != null) {
						replacedURL = replacedURL.concat(testData.getBidexAppendString());
					}
					
					

					if (testData.getEnvironment().contains("hbistage.srv.media.net")) {

					String splitUrl[] = testData.getEnvironment().split("\\.");
					String username = splitUrl[0];
//						String addToUrl = "&ad_username=" + username;
						
						replacedURL = replacedURL+"&ad_username=" + username;
					
				
						
					}

					log.info("replace Url  =  " + replacedURL);
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

	public void openWindscribe(Page page, String location) throws InterruptedException {
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
		// page.locator(windscribeSkip).click();
	}

	private static void setLocationInWindscribe(Page page, String location) {
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

	public HashMap<String, Object> getRequestHeaders(Request request) {
		return new HashMap<String, Object>(request.allHeaders());
	}

	public HashMap<String, Object> getResponseHeaders(Request request) {
		return new HashMap<String, Object>(request.response().allHeaders());
	}

	@Override
	public List<String> getReportFiles() {

		List<String> listOfFiles=this.fileOperations.listOfDownloadFiles(reportFilePath);		
		
		return listOfFiles;
		
	}

}
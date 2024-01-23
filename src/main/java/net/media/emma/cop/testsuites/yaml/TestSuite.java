package net.media.emma.cop.testsuites.yaml;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Timing;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo.DomInjectParams;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo.ResponseSimulateParams;
import net.media.emma.cop.testsuites.yaml.TestReportModel.BidExController;
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

	@Value("${harFileBasePath}")
	private String harFileBasePath;

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

		future.complete(getTestResults(testData));
		return future;

	}

	@Override
	public TestReportModel getTestResults(TestCriteriaBo testData) {

		String harFilePath = CommonBrowserUtil.getFileNameForHar();

		BrowserContext browserContext = BrowserFactory.getBc(testData.getUserAgent(), testData.getCookies(),
				harFileBasePath + harFilePath);

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

		browserContext.onRequest(request -> {

			urlFromNetworkTab.add(request.url());
			requestList.add(request);

		});

		// log.info(urlFromNetworkTab.toString());
		// return urlFromNetworkTab;

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

		} catch (Exception e) {

			log.error("Error while navigating to publisher url: {}", e.getMessage());

		}

		// return Arrays.toString(urlFromNetworkTab.toArray()).replace(", ", "<br>\n");
		// return getNetworkCallAssertions(requestList, testData);

		BidExController bidExController = getBidExData(requestList, testData);
		bidExController.setConsoleMessages(consoleMessage);

		TestReportModel testReport = TestReportModel.builder().testMetaData(TestMetaData.builder()
				.dateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).testName("YAML Test Suite")
				.publisherUrl(testData.getPublisherUrl()).environment(testData.getEnvironment())
				.userAgent(
						testData.getUserAgent() + " - " + BrowserFactory.browserContexts.get(testData.getUserAgent()))
				.harFileDownloadLink("harDownload/" + harFilePath).build()).build();

		testReport.setBidExController(bidExController);
		try {
		
			testReport.setUrlChecks(getUrlAssertionsData(urlFromNetworkTab,requestList, testData));
		}catch (Exception e) {
			
			e.printStackTrace();
			
			testReport.setUrlChecks(null);
		}
		

		browserContext.close();
//		try {
//
//			return jsonWriter.writeValueAsString(testReport);
//
//		} catch (JsonProcessingException e) {
//
//			log.error("Error while converting test report to json: {}", e.getMessage());
//			return "Error while converting test report to json";
//		}

		return testReport;
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
		}

		if (bidExRequestObjects.size() == 0) {
			bidexReport.getConsoleMessages().add("No bidex request found");
		}

		if (bidExRequestObjects.size() > 1) {
			bidexReport.getConsoleMessages().add("Multiple bidex request found");
		}

		return bidexReport;

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
package net.media.emma.cop.testsuites.yaml;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo.DomInjectParams;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo.ResponseSimulateParams;
import net.media.emma.cop.testsuites.yaml.URLChecks.NegativeURLChecks;
import net.media.emma.cop.testsuites.yaml.URLChecks.PositiveURLChecks;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.CommanBrowserUtil;
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
	private CommanBrowserUtil browserUtil;

	// List of strings "1", "2"
	List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net");
	List<String> bidExControlers = Arrays.asList("bidexchange.js", "clienttag.js");

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

	@Override
	public String getTestResults(TestCriteriaBo testData) {

		String fileName = browserUtil.getFileNameForHar();

		BrowserContext browserContext = BrowserFactory.getBc(testData.getUserAgent(), testData.getCookies(), fileName);

		if (testData.getLoginUrl() != null && testData.getUserName() != null && testData.getPassword() != null) {

			doLogin(browserContext, testData);

		}

		browserContext = replaceBidExUrl(browserContext, testData);
		browserContext = attachResponseMocking(browserContext, testData);
		browserContext = injectDOM(browserContext, testData);

		// list of arrays initialization
		List<String> urlFromNetworkTab = new ArrayList<>();
		List<Request> requestList = new ArrayList<>();

		browserContext.onRequest(request -> {

			urlFromNetworkTab.add(request.url());
			requestList.add(request);
		});

		// log.info(urlFromNetworkTab.toString());
		// return urlFromNetworkTab;

		try {
			Page page = browserContext.newPage();
			page.navigate(testData.getPublisherUrl());
			// page.waitForLoadState(LoadState.NETWORKIDLE,new
			// Page.WaitForLoadStateOptions().setTimeout(5000));
			// page.waitForFunction("() => performance.getEntriesByType('resource').length >
			// 0", null,new Page.WaitForFunctionOptions().setTimeout(5));

			Page page2 = browserContext.newPage();
			Thread.sleep(1000);

		} catch (Exception e) {

			log.error("Error while navigating to publisher url: {}", e.getMessage());

		} finally {

			browserContext.close();
		}

		// return Arrays.toString(urlFromNetworkTab.toArray()).replace(", ", "<br>\n");
		// return getNetworkCallAssertions(requestList, testData);
		return getNetworkCallAssertions3(urlFromNetworkTab, testData, fileName);
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

			if (currentUrl.startsWith(urlData.getValue())) {

				return urlData;
			}
		}

		return null;
	}

	private String getNetworkCallAssertions3(List<String> requestList, TestCriteriaBo testData, String file) {

		URLChecks urlChecks = URLChecks.builder().build();

		for (String requestUrl : requestList) {

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

		Pattern pattern = Pattern.compile("([^/]+)\\.har");

		Matcher matcher = pattern.matcher(file);

		String updatedFileName = null;

		if (matcher.find()) {

			String fileName = matcher.group();
			updatedFileName = "harDownload/" + fileName;
		}

		urlChecks.updateMatchingUrlsCount();
		urlChecks.updateNetworkCallErrors(testData);
		urlChecks.verifyPositiveKeyValChecks();
		urlChecks.setHarFileDownloadLink(updatedFileName);

		try {
			return jsonWriter.writeValueAsString(urlChecks);
		} catch (JsonProcessingException e) {
			log.error("json processing error", e);
		}

		return "json error";
	}

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

	private void buildTestReport(List<String> networkCalls, TestCriteriaBo testData) {

		TestReportBo tbo = TestReportBo.builder().build();

		tbo.setTestMetaData(TestReportBo.TestData.builder().environment(testData.getEnvironment())
				.publisherUrl(testData.getPublisherUrl()).userAgent(testData.getUserAgent()).build());

		for (String networkcall : networkCalls) {

		}

		tbo.toString();
	}
}
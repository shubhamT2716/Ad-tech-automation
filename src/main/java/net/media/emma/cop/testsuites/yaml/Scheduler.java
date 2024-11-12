package net.media.emma.cop.testsuites.yaml;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.cop.testsuites.yaml.TestReportModel.Controller;
import net.media.emma.cop.testsuites.yaml.TestReportModel.TestMetaData;
import net.media.emma.utils.CommonBrowserUtil;

@Service
@Slf4j
@EnableAsync
public class Scheduler {

	@Autowired
	private ControllerDataConvey controllerDataConvey;

	@Value("${harFileBasePath}")
	private String harFileBasePath;

	List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net",
			"hbx-stage.media.net", "hbx-stage-2.media.net", "hbx-stage-3.media.net", "hbx-stage-4.media.net",
			"hbx-stage-1.media.net", "ssp-stage-hb.media.net", "ssp-stage-hb-1.media.net", "ssp-stage-hb-2.media.net",
			"ssp-stage-hb-3.media.net", "ssp-stage-hb-4.media.net");
	List<String> bidExControlers = Arrays.asList("bidexchange.js", "clientag.js", "js/tags/clientag.js",
			"ss/nes/bidexl2.js");
	List<String> getConsoleMessageOfUrl = Arrays.asList("checksync.php?", "bidexchange.js", "clientag.js",
			"js/tags/clientag.js", "ss/nes/bidexl2.js", "cksync?", "clientag.js?cid");
	List<String> urlController = List.of("checksync");

	@Async("yamlTestSuitePool")
	public CompletableFuture<TestReportModel> getSchedulerThread(String env, String userAgent, String publisher) {

		CompletableFuture<TestReportModel> future = new CompletableFuture<>();

		try {

			TestReportModel model = generateSchedulerReport(env, userAgent, publisher);

			future.complete(model);

		} catch (Exception e) {

			e.printStackTrace();
			log.info("error while running scheduler: " + e.getMessage());
			future.completeExceptionally(e);

		}

		return future;

	}

	public TestReportModel generateSchedulerReport(String environment, String userAgent, String publisher) {

		String harFilePath = CommonBrowserUtil.getFileNameForHar();

		BrowserContext browserContext = BrowserFactory.getBc(userAgent, harFileBasePath + harFilePath);

		browserContext = replaceBidExUrl(browserContext, environment);

		List<String> urlFromNatworktab = new ArrayList<>();
		List<Request> requestUrls = new ArrayList<>();
		List<String> consoleError = new ArrayList<>();

		browserContext.onRequest(request -> {

			urlFromNatworktab.add(request.url());
			requestUrls.add(request);

		});

		try {

			browserContext.setDefaultTimeout(35000);

			Page page = browserContext.newPage();

			consoleError = this.controllerDataConvey.getConsoleErrors(page);

//			page.setDefaultTimeout(10000);

			page.navigate(publisher);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			page.close();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		try {
		
					
		

		Controller bidExController = this.controllerDataConvey.getBidExData(requestUrls, environment);

		bidExController.setConsoleMessages(consoleError);

		TestReportModel testReport = TestReportModel.builder()
				.testMetaData(
						TestMetaData.builder().dateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
								.testName("YAML Test Suite").publisherUrl(publisher).environment(environment)
								.harFileDownloadLink("/yamlTestSuite/harDownload/" + harFilePath).build())
				.build();

		testReport.setController(bidExController);
		testReport.setTestSummery(getSummeryForScheduler(requestUrls));

		return testReport;
		
		}
		catch (Exception e) {
			return null;
		}finally {
			browserContext.close();
		}

	}

	private BrowserContext replaceBidExUrl(BrowserContext browserContext, String environment) {

		List<String> allUrls = new ArrayList<>();
		allUrls.addAll(bidExControlers);
//			allUrls.addAll(urlController);

		for (String currentHost : bidExHosts) {
			for (String currentController : allUrls) {

				browserContext.route("**/" + currentHost + "/" + currentController + "*", route -> {

					String replacedURL = "";
					String originalURl = route.request().url();

					for (String currentHostInner : bidExHosts) {
						if (originalURl.contains(currentHostInner)) {
							replacedURL = originalURl.replace(currentHost, environment);
							break;
						}
					}

					log.info("replace Url  =  " + replacedURL);
					route.resume(new Route.ResumeOptions().setUrl(replacedURL));
				});
			}
		}

		return browserContext;
	}

	private Map<String, Map<String, String>> getSummeryForScheduler(List<Request> requestUrls) {
		String flog = "https://hblg.media.net/log?logid=kfk&evtid=flog";

		String nerrping = "https://hblg.media.net/nerrping.php";

		Map<String, Map<String, String>> mapOfnegativeUrl = new LinkedHashMap<>();

		Map<String, String> negativeUrlscheck = new HashMap<>();

		for (Request request : requestUrls) {
			if (request.url().startsWith(nerrping)) {
				negativeUrlscheck.put(request.url(), "PASS");
			}
			if (request.url().startsWith(flog)) {
				negativeUrlscheck.put(request.url(), "PASS");
			}
		}

		if (negativeUrlscheck.isEmpty()) {
			negativeUrlscheck.put("No Flog", "No nerrping");
		}

		mapOfnegativeUrl.put("negativeUrlChecks", negativeUrlscheck);

		return mapOfnegativeUrl;

	}

	public String testResultForScheduler(Map<String, TestReportModel> result) {
		boolean hasError = false;

		for (Map.Entry<String, TestReportModel> testResult : result.entrySet()) {

			for (Map.Entry<String, Map<String, String>> summery : testResult.getValue().getTestSummery().entrySet()) {

				for (Map.Entry<String, String> error : summery.getValue().entrySet()) {
					if (error.getValue().equalsIgnoreCase("PASS")) {
						hasError = true;

						break;

					}
				}

				if (hasError == true) {
					break;
				}

			}

			if (hasError == true) {
				break;
			}

		}

		if (hasError == false) {
			return "pass";
		} else {
			return "fail";
		}

	}

	public String getHostFromUrl(String url) {

		String host = null;

		try {

			URL ur = new URL(url);

			host = ur.getHost();

			if (host.contains("www.")) {
				host = host.substring(4);

			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return host;

	}

}

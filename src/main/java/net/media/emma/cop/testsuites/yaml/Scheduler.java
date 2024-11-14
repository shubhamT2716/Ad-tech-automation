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
import java.util.Map.Entry;
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

	@Async("schedulerThreadPool")
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

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			Controller controller = this.controllerDataConvey.getBidExData(requestUrls, environment);

			controller.setConsoleMessages(consoleError);

			TestReportModel testReport = TestReportModel.builder()
					.testMetaData(TestMetaData.builder()
							.dateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
							.testName("YAML Test Suite").publisherUrl(publisher).environment(environment).userAgent(userAgent)
							.harFileDownloadLink("/yamlTestSuite/harDownload/" + harFilePath).build())
					.build();

			testReport.setController(controller);

			testReport.setTestSummery(getSummeryForScheduler(requestUrls));

			return testReport;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
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
					
					
					
//					if (testData != null) {
//                        for (Entry<String, String> entry : testData.entrySet()) {
//
//                            if (entry == null) {
//                                continue;
//                            } else if (originalURl.contains(entry.getKey())) {
//                                replacedURL = replacedURL.replace(entry.getKey(), entry.getValue());
//                            }
//                        }
//                    }

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
		
		String gpt="https://securepubads.g.doubleclick.net/tag/js/gpt.js";
		
		String gampad="https://securepubads.g.doubleclick.net/gampad/ads";
		
		Map<String, Map<String, String>> mapOfnegativeUrl = new LinkedHashMap<>();

//		List<Map<String, String> positveUrlscheck=new ArrayList<>();
		
		Map<String, String> positivUrls=new HashMap<>();

		Map<String, String> error=new HashMap<>();

		for (Request request : requestUrls) {
			
			if(isNegativeUrl(request.url()))
			{		
//				error=getNegativecall(request.url());

				if(request.url().startsWith(nerrping))
				{
					error.put("nerrping", request.url());
				}
				if(request.url().startsWith(flog))
				{
					error.put("flog", request.url());
				}
				
			}
			
			if(isPositiveUrl(request.url()))
			{
//				positveUrlscheck.add(getPositivecall(request.url()));
				
				if(request.url().startsWith(gampad))
				{
					positivUrls.put("gampad", "PASS");
				}
				
				if(request.url().startsWith(gpt)) {
					positivUrls.put("gpt.js", "PASS");
				}
		
			}
			
		}

		if (error.isEmpty()) {
			error.put("flog", "No flog");
			error.put("nerrping", "No nerrping");
			
		}
		
		if(positivUrls.isEmpty())
		{
			positivUrls.put("gpt.js","FAIL");
			positivUrls.put("gampade", "FAIL");
		}

		mapOfnegativeUrl.put("negativeUrlChecks", error);
		mapOfnegativeUrl.put("positveUrlscheck", positivUrls);

		return mapOfnegativeUrl;

	}
	
	private boolean isNegativeUrl(String url)
	{
		String flog = "https://hblg.media.net/log?logid=kfk&evtid=flog";

		String nerrping = "https://hblg.media.net/nerrping.php";
	
		
		return  url.startsWith(nerrping)|| url.startsWith(flog);
	}
	
	private Map<String, String> getNegativecall(String url)
	{
		String flog = "https://hblg.media.net/log?logid=kfk&evtid=flog";

		String nerrping = "https://hblg.media.net/nerrping.php";
		
		Map<String, String> error=new HashMap<>();
		
		
		if(url.startsWith(nerrping))
		{
			error.put("nerrping", url);
		}
		
		if(url.startsWith(flog))
		{
			error.put("flog", url);
		}
		
		return error;
	}
	
	private Map<String, String> getPositivecall(String url)
	{
		String gpt="https://securepubads.g.doubleclick.net/tag/js/gpt.js";
		
		String gampad="https://securepubads.g.doubleclick.net/gampad/ads";
		
		Map<String, String> positiveUrl=new HashMap<>();
		
		System.out.println("Url Match: "+url.startsWith(gpt));
		
		if(url.startsWith(gpt))
		{
	    	positiveUrl.put("gpt.js", "PASS");
		}
		
		if(url.startsWith(gampad))
		{
			positiveUrl.put("gampad", "PASS");
		}
		
		System.out.println(positiveUrl);
		
		return positiveUrl;
	}
	
	
	private boolean isPositiveUrl(String url)
	{
		String gpt="https://securepubads.g.doubleclick.net/tag/js/gpt.js";
		
		String gampad="https://securepubads.g.doubleclick.net/gampad/ads";
		
		return  url.startsWith(gampad)|| url.startsWith(gpt);
	}

	public String testResultForScheduler(Map<String, TestReportModel> result) {
	
		for (TestReportModel testResult : result.values()) {

			if (!testResult.getController().getConsoleMessages().isEmpty()) {
				return "fail";
			}			
			
			for(Entry<String, String> negativeChecks:testResult.getTestSummery().get("negativeUrlChecks").entrySet())
			{
				
				if(negativeChecks.getValue().startsWith("https://hblg.media.net"))
				{
					return "fail";	
				}
				
			}
			
			
			for(Entry<String, String> positiveChecks:testResult.getTestSummery().get("positveUrlscheck").entrySet())
			{
				
				if(positiveChecks.getValue().equalsIgnoreCase("FAIL"))
				{
					return "fail";
				}
			}


		}

		return "pass";

	}

	public static String getHostFromUrl(String url) {

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

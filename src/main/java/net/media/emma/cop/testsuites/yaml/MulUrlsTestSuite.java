package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.URLDataProcessing;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class MulUrlsTestSuite {
    @Autowired
    Logger logger;
    @Autowired
    URLDataProcessing urlDataProcessing;
    @Autowired
    ObjectWriter jsonWriter;
    // List of strings "1", "2"
    List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net");
    List<String> bidExControlers = Arrays.asList("bidexchange.js", "clienttag.js");
    @Autowired
    private CommonBrowserUtil browserUtil;

    public String getTestResults(TestCriteriaConsoleError testData) throws InterruptedException {

        String fileName = CommonBrowserUtil.getFileNameForHar();
        BrowserContext browserContext = BrowserFactory.getBcWithUserAgent(testData.getBrowserType(), testData.getUserAgent(), testData.getCookies(), fileName);
        browserContext.clearCookies();
        browserContext = replaceBidExUrl(browserContext, testData);
        //list of arrays initialization
        HashMap<String, String> responseDetails = new HashMap<>();

        Page page = browserContext.newPage();

        browserContext.onRequest(request -> {
            if (request.url().contains(testData.getBidExchangeUrl())) {
                responseDetails.put("url", request.url());
                responseDetails.put("size", Integer.toString(request.sizes().responseBodySize));
                responseDetails.put("time", Double.toString(request.timing().responseEnd - request.timing().requestStart));
            }
        });

        List<String> consoleErrors = getConsoleErrors(page);
        try {
            page.setDefaultTimeout(60000);
            page.navigate(testData.getPublisherUrl());
            page.waitForLoadState();
        } catch (Exception e) {
            logger.error("Error in loading page", e);
        }
        Thread.sleep(1000);

        page.close();
        browserContext.close();

        return getNetworkCallAssertions3(responseDetails, consoleErrors, testData);
    }

    private String getNetworkCallAssertions3(HashMap<String, String> responseDetails, List<String> consoleErrors, TestCriteriaConsoleError testData) {

        ConsoleErrorURLChecksReportModel urlChecks = ConsoleErrorURLChecksReportModel.builder().build();

        String consoleErrorMessage = String.join(", ", consoleErrors);
        urlChecks.setBidExchangeUrl(responseDetails.get("url"));
        urlChecks.setResponseTime(responseDetails.get("time"));
        urlChecks.setResponseSize(responseDetails.get("size"));
        urlChecks.setConsoleErrorMessage(consoleErrorMessage);

        try {
            return jsonWriter.writeValueAsString(urlChecks);
        } catch (JsonProcessingException e) {
            log.error("json processing error", e);
        }

        return "json error";
    }


    private List<String> getConsoleErrors(Page page) {
        List<String> consoleErrors = new ArrayList<>();
        page.onConsoleMessage(consoleMessage -> {
            if (consoleMessage.type().equals("error") && consoleMessage.location().contains("bidexchange.js")) {
                consoleErrors.add(consoleMessage.text());
            }
        });
        return consoleErrors;
    }

    private BrowserContext replaceBidExUrl(BrowserContext browserContext, TestCriteriaConsoleError testData) {

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

}

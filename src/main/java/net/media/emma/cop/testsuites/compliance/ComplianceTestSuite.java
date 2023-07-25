package net.media.emma.cop.testsuites.compliance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.URLDataProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;

import java.util.*;

@Service
@Slf4j
public class ComplianceTestSuite {

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

    private static String windscribeLogin = "xpath=//button[text()='Login']";
    private static String windscribeUsername = "xpath=//input[@name='username']";
    private static String windscribepassword = "xpath=//input[@name='password']";
    private static String windscribeSkip = "xpath=//div[text()='Skip']";
    private static String username = "mohib1233";
    private static String password = "mohib123";
    private static String locationParis = "Paris";
    private static String windscribeLocations = "xpath=//button[@aria-label='Locations']";
    private static String windscribeSearchLocation = "xpath=//div//i";
    private static String windscribeSearchbox = "xpath=//input[contains(@class,'css-1c1txoj')]";
    private static String windscribeParisLoc = "xpath=(//div[@aria-label='Paris'])[1]";
    private static String acceptAllBtn = "xpath=//button[text()='Accept All']";


    String url = "chrome-extension://hnmpcagpplmpfojmgmnngilcnanddlhb/popup.html";
    String windScribeExtensionPath = System.getProperty("user.dir") + "/extensions/hnmpcagpplmpfojmgmnngilcnanddlhb/3.4.6_0";

    public String getTestResults(ComplianceTestCriteria testData) throws InterruptedException {

        String harFileName = harFileBasePath + CommonBrowserUtil.getFileNameForHar();
        BrowserContext browserContext = BrowserFactory.getSystemBc(testData.getUserAgent(), testData.getCookies(), getExtensionList(), harFileName, browserPath);
        browserContext.clearCookies();

        //list of arrays initialization
        List<String> urlFromNetworkTab = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();

        Page page = browserContext.newPage();
        openWindscribe(page);
        page.close();
        page = browserContext.newPage();
        page.navigate(testData.getPublisherUrl());

        browserContext.onRequest(request -> {

            urlFromNetworkTab.add(request.url());
            requestList.add(request);
        });

        page.locator(acceptAllBtn).click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(1000);
        HashMap<String, String> tcfResponse = getTcfApiResponse(page);
        String gdprString = tcfResponse.get("tcString");

        browserContext.onRequest(request -> {

            urlFromNetworkTab.add(request.url());
            requestList.add(request);
        });

        pubsRefresh(page);
        page.waitForLoadState(LoadState.NETWORKIDLE);

        updateTestCriteriaGdprString(testData,gdprString);

        String results = getNetworkCallAssertions3(urlFromNetworkTab, testData);

        page.close();
        browserContext.close();

        return results;
    }

    public List<String> getExtensionList() {
        List<String> ext = new ArrayList<>();
        ext.add("--disable-extensions-except=" + windScribeExtensionPath);
        ext.add("--load-extension=" + windScribeExtensionPath);
        return ext;
    }

    public void openWindscribe(Page page) {
        page.navigate(url);
        try {
            page.locator(windscribeLogin).click();
            page.locator(windscribeUsername).click();
            page.locator(windscribeUsername).fill(username);
            page.locator(windscribepassword).click();
            page.locator(windscribepassword).fill(password);
            page.locator(windscribeLogin).click();
            page.locator(windscribeSkip).click();
        } catch (TimeoutError e) {
            log.error("Element not found ", e);
        }
        page.locator(windscribeLocations).click();
        page.locator(windscribeSearchLocation).click();
        page.locator(windscribeSearchbox).click();
        page.locator(windscribeSearchbox).fill(locationParis);
        page.locator(windscribeParisLoc).click();
        page.close();
    }

    private String getNetworkCallAssertions3(List<String> requestList, ComplianceTestCriteria testData) {

        URLChecks urlChecks = URLChecks.builder().build();

        for (String requestUrl : requestList) {

            Map.Entry<String, String> urlData = positiveURLPatternExists(requestUrl, testData);

            if (urlData != null) {

                URLChecks.PositiveURLChecks labelExists = urlChecks.getPositiveUrlChecks().get(urlData.getKey());

                if (labelExists == null) {

                    URLChecks.PositiveURLChecks puc = URLChecks.PositiveURLChecks.builder().label(urlData.getKey())
                            .urlPattern(urlData.getValue())
                            .matchingUrls(new ArrayList<String>(Arrays.asList(requestUrl)))
                            .queryStringMap(new ArrayList<HashMap<String, String>>(Arrays.asList(urlDataProcessing.getQueryStringMapFromUrl(requestUrl))))
                            //  .negativeKeyValChecks(testData.getKeysToVerifyNegative().get(urlData.getKey()))
                            .positiveKeyValChecks(testData.getKeysToVerifyPositive().get(urlData.getKey()))
                            .anyOneOfKeyValChecks(testData.getKeysToVerifyAnyOneOf().get(urlData.getKey()))
                            .build();

                    urlChecks.getPositiveUrlChecks().put(urlData.getKey(), puc);
                } else {
                    labelExists.getMatchingUrls().add(requestUrl);
                    labelExists.getQueryStringMap().add(urlDataProcessing.getQueryStringMapFromUrl(requestUrl));
                }

                continue;
            }

           /* urlData = negativeURLPatternExists(requestUrl, testData);

            if(urlData != null){
                URLChecks.NegativeURLChecks nuc = urlChecks.getNegativeUrlChecks().get(urlData.getKey());
                if(nuc == null){
                    urlChecks.getNegativeUrlChecks().put(urlData.getKey(), URLChecks.NegativeURLChecks.builder().label(urlData.getKey())
                            .urlPattern(urlData.getValue())
                            .matchingUrls(new ArrayList<String>((Arrays.asList(requestUrl))))
                            .build());
                }else{
                    nuc.getMatchingUrls().add(requestUrl);
                }
            }*/
        }

        urlChecks.updateMatchingUrlsCount();
        urlChecks.updateNetworkCallErrors(testData);
        urlChecks.verifyPositiveKeyValChecks();

        try {
            return jsonWriter.writeValueAsString(urlChecks);
        } catch (JsonProcessingException e) {
            log.error("json processing error", e);
        }

        return "json error";
    }

    private Map.Entry<String, String> positiveURLPatternExists(String currentUrl, ComplianceTestCriteria testData) {

        for (Map.Entry<String, String> urlData : testData.getNetworkCallsPositive().entrySet()) {

            if (currentUrl.startsWith(urlData.getValue())) {

                return urlData;
            }
        }

        return null;
    }

 /*   private Map.Entry<String, String> negativeURLPatternExists(String currentUrl, ComplianceTestCriteria testData){

        for(Map.Entry<String, String> urlData : testData.getNetworkCallsNegative().entrySet()){

            if(currentUrl.startsWith(urlData.getValue())){

                return urlData;
            }
        }

        return null;
    }*/

    private LinkedHashMap<String, String> getTcfApiResponse(Page page) {
        Object d = page.evaluate("() => {\n" +
                "  return new Promise((resolve) => {\n" +
                "    window.__tcfapi(\"addEventListener\", 2, (tcfApiInfo, success) => {\n" +
                "      if (success && isSet(tcfApiInfo)) {\n" +
                "        resolve(tcfApiInfo);\n" +
                "      }\n" +
                "    });\n" +
                "  });\n" +
                "}");
        return (LinkedHashMap<String, String>) d;
    }

    private void pubsRefresh(Page page) {
        page.evaluate("googletag.pubads().refresh(); timeFunction(); timeFunction(); function timeFunction() {setTimeout(function(){ googletag.pubads().refresh();}, 10000);}");
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

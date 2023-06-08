package net.media.emma.cop.testsuites.yaml;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.microsoft.playwright.BrowserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.Configuration;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo.DomInjectParams;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo.ResponseSimulateParams;
import net.media.emma.cop.testsuites.yaml.TestReportBo.CheckType;
import net.media.emma.cop.testsuites.yaml.TestReportBo.TestStatus;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.DomJsonUtils;


@Service @Slf4j
public class TestSuite implements GenericTestSuite {

    @Autowired BrowserFactory browserFactory;
    @Autowired Configuration jsonModelMapper;
    @Autowired DomJsonUtils domUtils;
    
    //List of strings "1", "2"
    List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net");
    List<String> bidExControlers = Arrays.asList("bidexchange.js", "clienttag.js");

    @Override
    public String getTestResults(TestCriteriaBo testData){

        Page page = browserFactory.getPageWithNewContext(testData.getUserAgent(), testData.getCookies());

        // page = replaceBidExUrl(page, testData);
        // page = attachResponseMocking(page, testData);
        // page = injectDOM(page, testData);

        //list of arrays initialization
        List<String> urlFromNetworkTab = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();

        page.onRequest(request -> { 

                urlFromNetworkTab.add(request.url());        
                requestList.add(request);   
        });

        page.navigate(testData.getPublisherUrl());
        // log.info(urlFromNetworkTab.toString());
        // return urlFromNetworkTab;

        // return Arrays.toString(urlFromNetworkTab.toArray()).replace(", ", "<br>\n");

        return getNetworkCallAssertions(requestList, testData);
    }

    private Request getRequestFor(String url, List<Request> requestList){

        for(Request request : requestList){

            log.info(request.url());

            if(request.url().contains(url)){
                log.info(request.toString());
                return request;
            }
        }

        return null;
    }

    private void logError(TestReportBo trb, String label, TestReportBo.CheckType checkType, Request urlData){
        trb.getUrlChecks().add(TestReportBo.UrlChecks.builder()
                                        .urlLabel(label)
                                        .urlCheckType(checkType)
                                        .testStatus(TestStatus.FAIL)
                                        .urlData(
                                            TestReportBo.UrlData.builder().urlPattern(urlData!=null?urlData.url():"X")
                                            .statusCode(urlData!=null?urlData.response().statusText():"X")
                                            .build()).build());

    }

    private void logLogSuccess(TestReportBo trb, String label, TestReportBo.CheckType checkType, Request urlData){
        trb.getUrlChecks().add(TestReportBo.UrlChecks.builder()
                                        .urlLabel(label)
                                        .urlCheckType(checkType)
                                        .testStatus(TestStatus.PASS)
                                        .urlData(
                                            TestReportBo.UrlData.builder().urlPattern(urlData !=null?urlData.url():"Y")
                                            .statusCode(urlData !=null? urlData.response().statusText():"Y")
                                            .size(urlData !=null?urlData.sizes().toString():"Y")
                                            .timeTakenToLoad(urlData !=null?urlData.timing().toString():"Y")
                                            .build()).build());

    }

    private String getNetworkCallAssertions(List<Request> requestList, TestCriteriaBo testData){

        TestReportBo trb = TestReportBo.builder().build();

        for(Map<String, String> networkCalls:   testData.getNetworkCallsPositive()){

            networkCalls.entrySet().forEach(entry -> {

                Request request = getRequestFor(entry.getValue(), requestList);

                if(request == null || request.failure() == null){

                    logError(trb, entry.getKey(), CheckType.POSSITIVE, request);
                }else{
                    logLogSuccess(trb, entry.getKey(), CheckType.POSSITIVE, request);
                }

            });
        }

        for(Map<String, String> networkCalls:   testData.getNetworkCallsNegative()){

            networkCalls.entrySet().forEach(entry -> {

                Request request = getRequestFor(entry.getValue(), requestList);

                if(request == null || request.failure() == null){

                    logLogSuccess(trb, entry.getKey(), CheckType.NEGERIVE, request);

                }else{

                    logError(trb, entry.getKey(), CheckType.NEGERIVE, request);
                }

            });
        }

        return trb.toString();
    }

    @Override
    public String getTestResults_trishul(TestCriteriaBo testData){

        BrowserContext browserContext = browserFactory.getBc(testData.getUserAgent(), testData.getCookies());

        browserContext = replaceBidExUrl(browserContext, testData);
        browserContext = attachResponseMocking(browserContext, testData);
        browserContext = injectDOM(browserContext, testData);

        //list of arrays initialization
        List<String> urlFromNetworkTab = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();

        browserContext.onRequest(request -> {

            urlFromNetworkTab.add(request.url());
            requestList.add(request);
        });

        browserContext.newPage().navigate(testData.getPublisherUrl());
        // log.info(urlFromNetworkTab.toString());
        // return urlFromNetworkTab;
        browserContext.close();

        // return Arrays.toString(urlFromNetworkTab.toArray()).replace(", ", "<br>\n");
        return getNetworkCallAssertions(requestList, testData);
    }

    private Page replaceBidExUrl(Page page, TestCriteriaBo testData){

        for(String currentHost : bidExHosts){
            for(String currentController : bidExControlers){

                page.route("**/" + currentHost + "/" + currentController + "*", route -> {
    
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

        return page;
    }
    private BrowserContext replaceBidExUrl(BrowserContext browserContext, TestCriteriaBo testData){

        for(String currentHost : bidExHosts){
            for(String currentController : bidExControlers){

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

    private Page attachResponseMocking(Page page, TestCriteriaBo testData){

        for (ResponseSimulateParams responseSim : testData.getResponseSimulate()){

                page.route(responseSim.getUrlPattern(), route -> {
    
                // String originalURl = route.request().url();
    
                String responseBody = new String(route.fetch().body(), StandardCharsets.UTF_8);
                String newResponseBody = domUtils.replaceInJson(responseBody, responseSim.getJsonPath(), responseSim.getOverrideValue());

                if(newResponseBody == responseBody){
                    route.resume();
                } else{
                    route.fulfill(new Route.FulfillOptions().setBody(newResponseBody));
                }
            });
        }

        return page;
    }
    private BrowserContext attachResponseMocking(BrowserContext browserContext, TestCriteriaBo testData){

        for (ResponseSimulateParams responseSim : testData.getResponseSimulate()){

                browserContext.route(responseSim.getUrlPattern(), route -> {

                // String originalURl = route.request().url();

                String responseBody = new String(route.fetch().body(), StandardCharsets.UTF_8);
                String newResponseBody = domUtils.replaceInJson(responseBody, responseSim.getJsonPath(), responseSim.getOverrideValue());

                if(newResponseBody == responseBody){
                    route.resume();
                } else{
                    route.fulfill(new Route.FulfillOptions().setBody(newResponseBody));
                }
            });
        }

        return browserContext;
    }

    private Page injectDOM(Page page, TestCriteriaBo testData){

            page.route(testData.getPublisherUrl() + "**", route ->  {

                String pageContent = new String(route.fetch().body());
    
                for (DomInjectParams domInject : testData.getDomInject()) {

                    pageContent = domUtils.domManipulate(pageContent, domInject);
                }

                route.fulfill(new Route.FulfillOptions().setBody(pageContent));
    
            });

        return page;
    }
    private BrowserContext injectDOM(BrowserContext browserContext, TestCriteriaBo testData){

            browserContext.route(testData.getPublisherUrl() + "**", route ->  {

                String pageContent = new String(route.fetch().body());

                for (DomInjectParams domInject : testData.getDomInject()) {

                    pageContent = domUtils.domManipulate(pageContent, domInject);
                }

                route.fulfill(new Route.FulfillOptions().setBody(pageContent));

            });

        return browserContext;
    }

    // public List<String> getURLAnalyticsFor(Page page, YamlTestCriteria testData){

    //     //list of arrays initialization
    //     List<String> urlFromNetworkTab = new ArrayList<>();

    //     page.onRequest(request -> { 

    //             urlFromNetworkTab.add(request.url());           
    //     });

    //     page.navigate(testData.getPublisherUrl());
    //     // log.info(urlFromNetworkTab.toString());
    //     // return urlFromNetworkTab;

    //     return urlFromNetworkTab;
    // }

    private void buildTestReport(List<String> networkCalls, TestCriteriaBo testData){

        TestReportBo tbo = TestReportBo.builder().build();

        tbo.setTestMetaData(TestReportBo.TestData.builder()
            .environment(testData.getEnvironment())
            .publisherUrl(testData.getPublisherUrl())
            .userAgent(testData.getUserAgent()).build()
                );


                for(String networkcall: networkCalls){

                    
                }

        tbo.toString();
    }
}
package net.media.emma.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.Configuration;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;

import net.media.emma.components.BrowserFactory;
import net.media.emma.config.YamlTestCriteria;
import net.media.emma.config.YamlTestCriteria.DomInjectParams;
import net.media.emma.config.YamlTestCriteria.ResponseSimulateParams;
import net.media.emma.utils.DomJsonUtils;

@Service
public class YamlTestSuite {

    @Autowired BrowserFactory browserFactory;
    @Autowired Configuration jsonModelMapper;
    @Autowired DomJsonUtils domJsonUtils;
    
    //List of strings "1", "2"
    List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net");
    List<String> bidExControlers = Arrays.asList("bidexchange.js", "clienttag.js");

    public String getTestResults(YamlTestCriteria testData){

        Page page = browserFactory.getPageWithNewContext(testData.getUserAgent(), testData.getCookies());

        page = replaceBidExUrl(page, testData);
        page = attachResponseMocking(page, testData);
        page = injectDOM(page, testData);

        return "success";

    }

    private Page replaceBidExUrl(Page page, YamlTestCriteria testData){

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

    private Page attachResponseMocking(Page page, YamlTestCriteria testData){

        for (ResponseSimulateParams responseSim : testData.getResponseSimulate()){

                page.route(responseSim.getUrlPattern(), route -> {
    
                // String originalURl = route.request().url();
    
                String responseBody = new String(route.fetch().body(), StandardCharsets.UTF_8);
                String newResponseBody = domJsonUtils.replaceInJson(responseBody, responseSim.getJsonPath(), responseSim.getOverrideValue());

                if(newResponseBody == responseBody){
                    route.resume();
                } else{
                    route.fulfill(new Route.FulfillOptions().setBody(newResponseBody));
                }
            });
        }

        return page;
    }

    private Page injectDOM(Page page, YamlTestCriteria testData){

            page.route(testData.getPublisherUrl() + "**", route ->  {

                String pageContent = new String(route.fetch().body());
    
                for (DomInjectParams domInject : testData.getDomInject()) {

                    pageContent = domJsonUtils.domManipulate(pageContent, domInject);
                }

                route.fulfill(new Route.FulfillOptions().setBody(pageContent));
    
            });

        return page;
    }

    public List<String> getURLAnalyticsFor(Page page, YamlTestCriteria testData){

        //list of arrays initialization
        List<String> urlFromNetworkTab = new ArrayList<>();

        page.onRequest(request -> { 

                urlFromNetworkTab.add(request.url());           
        });

        page.navigate(testData.getPublisherUrl());
        // log.info(urlFromNetworkTab.toString());
        // return urlFromNetworkTab;

        return urlFromNetworkTab;
    }
}
package net.media.emma.cop.testsuites.loggingpixels;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.cop.testsuites.loggingpixels.TestReportV2.KeyData;
import net.media.emma.cop.testsuites.loggingpixels.TestReportV2.TestMetaData;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.URLDataProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class LoggingPixelTestSuite
{
    @Autowired
    URLDataProcessing urlUtil;

    @Autowired
    ObjectMapper genericObjectMapper;

    @Value("${harFileBasePath}")
    private String harFileBasePath;

    List<String> loggingPixelUrlPatterns = new ArrayList<>(Arrays.asList( "https://lg1.hb.yahoo.net/log?logid=prlog",
                                                                              "https://lg1.hb.yahoo.net/log?logid=aplog",
                                                                              "https://lg1.hb.yahoo.net/log?logid=awlog",
                                                                              "https://hblg.media.net/log?logid=prlog",
                                                                              "https://hblg.media.net/log?logid=aplog",
                                                                              "https://hblg.media.net/log?logid=awlog"
                                                                              
                                                                              ));

    List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net");
    List<String> bidExControlers = Arrays.asList("bidexchange.js", "clienttag.js");

    private JsonNode buildReport(LoggingPixelTestDataModel testCriteria, List<String> allUrl,List<Request> requests, String harFilePath)
    {

        // log.info("all Url Data: " + allUrl.toString());

        List<String> matchingUrls = getMatchingNetworkCalls(allUrl);

        // log.info("matching Url Data: " + matchingUrls.toString());

        List<LoggingPixelTestDataModel.DataTypeChecks> testData = testCriteria.getLoggingPixelTests();
        TestReportV2 testReportV2 = TestReportV2.builder()
                                                .testMetaData(TestMetaData.builder()
                                                                    .testName("Logging Pixel Test Suite")
                                                                    .environment(testCriteria.getEnvironment())
                                                                    .dateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                                                                    .publisherUrl(testCriteria.getPublisherUrl())
                                                                    .userAgent(testCriteria.getUserAgent() + " - " + BrowserFactory.browserContexts.get(testCriteria.getUserAgent()))
                                                                    .harFileDownloadLink("harDownload/" + harFilePath)
                                                                  .build())
                                                .build();

        //Loop through matching network calls
        for (String url : matchingUrls)
        {
            Map<String, String> urlParams;
            // get key-val map "urlUtil.getQueryMap(currentUrl.toString());"
            urlParams = urlUtil.getQueryStringMapFromUrl(url,requests);;//  UrlQueryBuild.getQueryMap(url.toString());
            //verify key related data present in yaml against data present in url key query

            testReportV2.getNetworkCalls().put(url, verifyKeyDetails(urlParams, testData));
        }

        return genericObjectMapper.valueToTree(testReportV2);
    }
    
    private boolean dataTypeCheck(String val, String dataType)
    {
        try{
            switch (dataType)
            {
                case "int": Integer.parseInt(val); break;
                case "double": Double.parseDouble(val); break;
                case "long": Long.parseLong(val); break;
                case "short": Short.parseShort(val); break;
                case "byte": Byte.parseByte(val); break;
                case "float": Float.parseFloat(val); break;
                case "string": val.toString(); break;

            }
        }
        catch(Exception e)
        {
            log.error("Data Type Parsing Error", e );
            return false;
        }

        return true;
    }

    private List<KeyData> verifyKeyDetails(Map<String, String> urlParams, List<LoggingPixelTestDataModel.DataTypeChecks> testData)
    {
        List<KeyData> keyDataList = new ArrayList<>();

        for (LoggingPixelTestDataModel.DataTypeChecks data : testData)
        {
            String key = data.getKey();
           
            	 KeyData keyData = KeyData.builder().key(key).build();

                 if (urlParams.containsKey(key) && urlParams.get(key) != null)
                 {
                    String value = urlParams.get(key);
                    String regexFromYaml = data.getValueRegex();
                    List<String> negativeStringChecks = data.getNegativeStrings();
                    boolean notNullValue=data.isNotNull();
                    boolean mandatoryVlaue=data.isMandatory();
                
                    if (!dataTypeCheck(value, data.getValueDataType().toLowerCase()))
                    {
                        log.info("Data Type Check Failed for Key: " + key);
                        keyData.getMessages().add("Data Type Check Failed for Key: " + key + ". Expected Data Type: " + data.getValueDataType() + ", Actual Value: " + value);
                        // continue;
                    }
                    
                    if(!isNotNull(value, notNullValue))
                    {
                        log.info("Not Null Check Failed for key: "+key);
                        keyData.getMessages().add("Not Null check failed for key: "+key+" value not found");
                        // here value of key coming null but not null check is true then message generate
                    }
                    
                    if(!isMandatory(key, mandatoryVlaue))
                    {
                        log.info("Mendatory check Failed ");
                        keyData.getMessages().add("Mendatory check failed  key should not exist"); 
                        //if key should not exist then this check failed.
                    
                    }

                    if (matchRegex(regexFromYaml, value))
                    {
                        log.info("Regex Failed for Key: " + key);
                        keyData.getMessages().add("Regex Failed for Key: " + key + ". Regex: " + regexFromYaml + ", Actual Value: " + value);
                        // continue;
                    }

                    if (doesNegativeStringExists(negativeStringChecks, value))
                    {
                        log.info("Negative string check Failed for Key: " + key);
                        keyData.getMessages().add("Negative string check Failed for Key: " + key + ". Negative String Matches: " + negativeStringChecks + ", Actual Value: " + value);
                    }
                 }

                 if(keyData.getMessages().size() > 0 )
                 {
                     keyData.setTestResult("FAIL");
                 }else
                 {
                     keyData.setTestResult("PASS");
                 }

                 keyDataList.add(keyData);
            }
            
            return keyDataList;
        }

        private List<String> getMatchingNetworkCalls(List < String > allUrl)
        {
            List<String> matchingUrls = new ArrayList<>();

            for (String loggingPixelFormat : loggingPixelUrlPatterns)
            {
                for (String currentUrl : allUrl)
                {
                    if (currentUrl.startsWith(loggingPixelFormat))
                    {
                        matchingUrls.add(currentUrl);
                    }
                }
            }
            return matchingUrls;
        }

        public boolean isNotNull(String valueOfKey, boolean  value)
        {
        	
        	if(valueOfKey==null && value==true)
        	{
        		return false;
        	}
        	else {
        		return true;
        	}
        }
        
        public boolean isMandatory(String key, boolean value)
        {
        	if(key==null && value==true)
        	{
        		return false;
        	}
        	else {
        		return true;
        	}
        }

        private Boolean doesNegativeStringExists (List < String > negativeStringValues, String value)
        {
            return negativeStringValues.contains(value);
        }

        private BrowserContext replaceBidExUrl(BrowserContext browserContext, LoggingPixelTestDataModel testData) {

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

        public JsonNode getLoggingPixelTestResults(LoggingPixelTestDataModel testCriteria)
        {
            String harFilePath = CommonBrowserUtil.getFileNameForHar();

            BrowserContext bc = BrowserFactory.getBc(testCriteria.getUserAgent(), null, harFileBasePath + harFilePath);

            bc = replaceBidExUrl(bc, testCriteria);

            List<String> urlFromNetworkTab = new ArrayList<>();
            List<Request> requestList = new ArrayList<>();

            bc.onRequest(request ->
            {
                //list of arrays initialization

                urlFromNetworkTab.add(request.url());
                requestList.add(request);
            });


            try {
                bc.setDefaultTimeout(90000);
                bc.newPage().navigate(testCriteria.getPublisherUrl());
            } catch (Exception e) {

                log.error("Exception during Playwright Execution", e);
               
            }

            bc.close();

            return buildReport(testCriteria, urlFromNetworkTab, requestList,harFilePath);
        }

        private boolean matchRegex (String regex, String keyValue)
        {
            return Pattern.compile(regex).matcher(keyValue).find();
        }
    }
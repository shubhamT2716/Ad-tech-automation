package net.media.emma.cop.testsuites.loggingpixels;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Request;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.cop.testsuites.loggingpixels.TestReportV2.KeyData;
import net.media.emma.utils.URLDataProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class LoggingPixelTestSuite
{

    @Autowired
    BrowserFactory browserFactory;
    @Autowired
    URLDataProcessing urlUtil;

    @Autowired
    ObjectMapper objectMapper;

    List<String> loggingPixelFormatData = new ArrayList<>(Arrays.asList( "https://lg1.hb.yahoo.net/log?logid=prlog",
                                                                              "https://lg1.hb.yahoo.net/log?logid=aplog",
                                                                              "https://lg1.hb.yahoo.net/log?logid=awlog",
                                                                              "https://hblg.media.net/log?logid=prlog",
                                                                              "https://hblg.media.net/log?logid=aplog",
                                                                              "https://hblg.media.net/log?logid=awlog"
                                                                              
                                                                              ));

    private JsonNode buildReport(LoggingPixelTestDataModel testCriteria, List<String> allUrl)
    {

        // log.info("all Url Data: " + allUrl.toString());

        List<String> matchingUrls = getMatchingNetworkCalls(allUrl);

        // log.info("matching Url Data: " + matchingUrls.toString());

        List<LoggingPixelTestDataModel.DataTypeChecks> testData = testCriteria.getLoggingPixelTests();
        TestReportV2 testReportV2 = TestReportV2.builder().build();

        //Loop through matching network calls
        for (String url : matchingUrls)
        {
            Map<String, String> urlParams;
            // get key-val map "urlUtil.getQueryMap(currentUrl.toString());"
            urlParams = urlUtil.getQueryStringMapFromUrl(url);;//  UrlQueryBuild.getQueryMap(url.toString());
            //verify key related data present in yaml against data present in url key query

            testReportV2.getNetworkCalls().put(url, verifyKeyDetails(urlParams, testData));
        }

        return objectMapper.valueToTree(testReportV2);
    }

    // private void toDel()
    // {
//        if (urlParams.containsKey(key))
//        {
//            String value = null;
//            //value of the key
//            value = urlParams.get(key);
//            if (value != null)
//            {
//                String dataTypeOfKey = data.getValueDataType().toLowerCase();
//                List<Boolean> flags;
//                //  keys.add(key);
//                switch (dataTypeOfKey)
//                {
//                    case "long":
//                        try
//                        {
//                            Long.parseLong(value);
//                            dataTypeMatches.add(true);
//                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
//                            regexMatches.add(flags.get(0));
//                            negativeValues.add(flags.get(1));
//                        } catch (Exception e)
//                        {
//                            dataTypeMatches.add(false);
//                            regexMatches.add(false);
//                            negativeValues.add(false);
//                        }
//                        break;
//                    case "int":
//                        try
//                        {
//                            Integer.parseInt(value);
//                            dataTypeMatches.add(true);
//                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
//                            regexMatches.add(flags.get(0));
//                            negativeValues.add(flags.get(1));
//                        } catch (Exception e)
//                        {
//                            dataTypeMatches.add(false);
//                            regexMatches.add(false);
//                            negativeValues.add(false);
//                        }
//                        break;
//                    case "short":
//                        try
//                        {
//                            Short.parseShort(value);
//                            dataTypeMatches.add(true);
//                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
//                            regexMatches.add(flags.get(0));
//                            negativeValues.add(flags.get(1));
//                        } catch (Exception e)
//                        {
//                            dataTypeMatches.add(false);
//                            regexMatches.add(false);
//                            negativeValues.add(false);
//                        }
//                        break;
//                    case "byte":
//                        try
//                        {
//                            Byte.parseByte(value);
//                            dataTypeMatches.add(true);
//                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
//                            regexMatches.add(flags.get(0));
//                            negativeValues.add(flags.get(1));
//                        } catch (Exception e)
//                        {
//                            dataTypeMatches.add(false);
//                            regexMatches.add(false);
//                            negativeValues.add(false);
//                        }
//                        break;
//                    case "float":
//                        try
//                        {
//                            Float.parseFloat(value);
//                            dataTypeMatches.add(true);
//                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
//                            regexMatches.add(flags.get(0));
//                            negativeValues.add(flags.get(1));
//                        } catch (Exception e)
//                        {
//                            dataTypeMatches.add(false);
//                            regexMatches.add(false);
//                            negativeValues.add(false);
//                        }
//                        break;
//                    case "double":
//                        try
//                        {
//                            Double.parseDouble(value);
//                            dataTypeMatches.add(true);
//                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
//                            regexMatches.add(flags.get(0));
//                            negativeValues.add(flags.get(1));
//                        } catch (Exception e)
//                        {
//                            dataTypeMatches.add(false);
//                            regexMatches.add(false);
//                            negativeValues.add(false);
//                        }
//                        break;
//                    case "string":
//                        try
//                        {
//                            value.toString();
//                            dataTypeMatches.add(true);
//                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
//                            regexMatches.add(flags.get(0));
//                            negativeValues.add(flags.get(1));
//                        } catch (Exception e)
//                        {
//                            dataTypeMatches.add(false);
//                            regexMatches.add(false);
//                            negativeValues.add(false);
//                        }
//                        break;
//                }
//
//
//            }
//        }
//         negetiveCheckFailed
//        testReportModel.setKeys(keys);
//        testReportModel.setDataTypeMatches(dataTypeMatches);
//        testReportModel.setRegexMatches(regexMatches);
// //        testReportModel.setNegativeValues(negativeValues);
//     }

//     public JsonNode getLoggingPixelResults (LoggingPixelTestDataModel testCriteria) throws JsonProcessingException
//     {

//         BrowserContext bc = browserFactory.getBc("Chrome.Desktop.Mac", null);
//         List<String> urlFromNetworkTab = new ArrayList<>();
//         List<Request> requestList = new ArrayList<>();
//         bc.onRequest(request ->
//         {
//             //list of arrays initialization

//             urlFromNetworkTab.add(request.url());
//             requestList.add(request);
//         });
//         bc.setDefaultTimeout(0);
//         bc.newPage().navigate(testCriteria.getPublisherUrl());
//         bc.close();

//         return buildReport(testCriteria, urlFromNetworkTab);
//     }

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

                if (!dataTypeCheck(value, data.getValueDataType().toLowerCase()))
                {
                    log.info("Data Type Check Failed for Key: " + key);
                    keyData.getMessages().add("Data Type Check Failed for Key: " + key + ". Expected Data Type: " + data.getValueDataType() + ", Actual Value: " + value);
                    // continue;
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

            if(keyData.getMessages().size() > 0)
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

            for (String loggingPixelFormat : loggingPixelFormatData)
            {
                for (String currentUrl : allUrl)
                {
                    if (currentUrl.startsWith(loggingPixelFormat))
                    {
                        matchingUrls.add(currentUrl);
                        break;
                    }
                }
            }
            return matchingUrls;
        }


        private Boolean doesNegativeStringExists (List < String > negativeStringValues, String value)
        {
            return negativeStringValues.contains(value);
        }


        public JsonNode getLoggingPixelTestResults(LoggingPixelTestDataModel testCriteria)
        {
            BrowserContext bc = browserFactory.getBc(testCriteria.getUserAgent(), null);
            List<String> urlFromNetworkTab = new ArrayList<>();
            List<Request> requestList = new ArrayList<>();

            bc.onRequest(request ->
            {
                //list of arrays initialization

                urlFromNetworkTab.add(request.url());
                requestList.add(request);
            });


            try {
                bc.setDefaultTimeout(120000);
                bc.newPage().navigate(testCriteria.getPublisherUrl());
                Thread.sleep(1000);
            } catch (Exception e) {

                log.error("Exception during Playwright Execution", e);
               
            }

            bc.close();

            return buildReport(testCriteria, urlFromNetworkTab);
        }

        private boolean matchRegex (String regex, String keyValue)
        {
            return Pattern.compile(regex).matcher(keyValue).find();
        }
    }
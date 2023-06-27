package net.media.emma.cop.testsuites.loggingpixels;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Request;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
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
    ObjectWriter jsonWriter;

    List<String> loggingPixelFormatData = new ArrayList<>(Arrays.asList("https://hblg.media.net/log"));

    private JsonNode buildReport(LoggingPixelTestDataModel testCriteria, List<String> allUrl)
    {

        List<String> matchingUrls = getMatchingNetworkCalls(allUrl);
        List<LoggingPixelTestDataModel.DataTypeChecks> testData = testCriteria.getLoggingPixelTests();


        //Loop through matching network calls
        for (String url : matchingUrls)
        {
            Map<String, String> urlParams;
            // get key-val map "urlUtil.getQueryMap(currentUrl.toString());"
            urlParams = urlUtil.getQueryStringMapFromUrl(url);;//  UrlQueryBuild.getQueryMap(url.toString());
            //verify key related data present in yaml against data present in url key query
            TestReportModel testReportModel = verifyKeyDetails(urlParams, testData);
        }

        // {
               /*  loop through each key in key-val map{
                     every key ---> pull data from testCrtiteria "testCriteria.getDataTypeCheckFor(key);"

                 }*/
        // }
        return null;
    }

    private void toDel()
    {
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
        }catch(Exception e)
        {
            log.error("Data Type Parsing Error", e );
            return false;
        }

        return true;

    }

    private TestReportModel verifyKeyDetails(Map<String, String> urlParams, List<LoggingPixelTestDataModel.DataTypeChecks> testData)
    {
        TestReportModel testReportModel = new TestReportModel();

        for (LoggingPixelTestDataModel.DataTypeChecks data : testData)
        {
            String key = data.getKey();

            if (urlParams.containsKey(key) && urlParams.get(key) != null)
            {
                String value = urlParams.get(key);
                String dataTypeOfKey = data.getValueDataType().toLowerCase();
                List<Boolean> flags;
                //  keys.add(key);
                String regexFromYaml = data.getValueRegex();
                List<String> negativeStringChecks = data.getNegativeStrings();
                if (!dataTypeCheck(value, data.getValueDataType().toLowerCase()))
                {
                    log.info("Data Type Check Failed for Key: " + key);
                    continue;
                }

                if (matchRegex(regexFromYaml, value))
                {
                    log.info("Regex Failed for Key: " + key);
                    continue;

                }

                if (!verifyNegativeString(negativeStringChecks, value))
                {
                    log.info("Negative string check Failed for Key: " + key);
                }
            }
//         negetiveCheckFailed
//        testReportModel.setKeys(keys);
//        testReportModel.setDataTypeMatches(dataTypeMatches);
//        testReportModel.setRegexMatches(regexMatches);
//        testReportModel.setNegativeValues(negativeValues);


        }
        return testReportModel;
    }
        private List<String> getMatchingNetworkCalls(List < String > allUrl)
        {
            List<String> matchingUrls = new ArrayList<>();
            for (String loggingPixelFormat : loggingPixelFormatData)
            {
                for (String currentUrl : allUrl)
                {
                    if (currentUrl.toString().startsWith(loggingPixelFormat))
                    {
                        matchingUrls.add(currentUrl);
                    }

                }
            }
            return matchingUrls;
        }


        private Boolean verifyNegativeString (List < String > negativeStringValues, String value)
        {
            return negativeStringValues.contains(value);
        }

        public JsonNode getLoggingPixelResults (LoggingPixelTestDataModel testCriteria) throws JsonProcessingException
        {

            BrowserContext bc = browserFactory.getBc("Chrome.Desktop.Mac", null);
            List<String> urlFromNetworkTab = new ArrayList<>();
            List<Request> requestList = new ArrayList<>();
            bc.onRequest(request ->
            {
                //list of arrays initialization

                urlFromNetworkTab.add(request.url());
                requestList.add(request);
            });
            bc.setDefaultTimeout(0);
            bc.newPage().navigate(testCriteria.getPubUrl());
            bc.close();

            return buildReport(testCriteria, urlFromNetworkTab);
        }

        private boolean matchRegex (String regex, String keyValue)
        {
            return Pattern.compile(regex).matcher(keyValue).find();
        }

    }
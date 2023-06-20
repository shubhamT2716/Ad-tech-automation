package net.media.emma.cop.testsuites.loggingpixels;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Request;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;
import net.media.emma.utils.UrlQueryBuild;
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

    private String doesTheLoggingPixelFormatMatches(LoggingPixelTestDataModel testCriteria, List<Request> allUrl) throws JsonProcessingException
    {
        TestReportModel testReportModel = new TestReportModel();
        // Create lists for each variable
        List<String> networkCalls = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        List<Boolean> dataTypeMatches = new ArrayList<>();
        List<Boolean> regexMatches = new ArrayList<>();
        List<Boolean> negativeValues = new ArrayList<>();
        for (String loggingPixelFormat : loggingPixelFormatData)
        {
            for (Request currentUrl : allUrl)
            {

                Map<String, String> urlParams;
                if (currentUrl.toString().startsWith(loggingPixelFormat))
                {
                    networkCalls.add(currentUrl.toString());
                    urlParams = UrlQueryBuild.getQueryMap(currentUrl.toString());
                    List<LoggingPixelTestDataModel.DataTypeChecks> testData = testCriteria.getLoggingPixelTests();
                    for (LoggingPixelTestDataModel.DataTypeChecks data : testData)
                    {
                        String key = data.getKey();
                        String regexFromYaml = data.getValueRegex();
                        List<String> negativeStringChecks = data.getNegativeStrings();
                        if (urlParams.containsKey(key))
                        {
                            String value = null;
                            //value of the key
                            value = urlParams.get(key);
                            if (value != null)
                            {
                                String dataTypeOfKey = data.getValueDataType().toLowerCase();
                                List<Boolean> flags;
                                keys.add(key);
                                switch (dataTypeOfKey)
                                {
                                    case "long":
                                        try
                                        {
                                            Long.parseLong(value);
                                            dataTypeMatches.add(true);
                                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
                                            regexMatches.add(flags.get(0));
                                            negativeValues.add(flags.get(1));
                                        } catch (Exception e)
                                        {
                                            dataTypeMatches.add(false);
                                            regexMatches.add(false);
                                            negativeValues.add(false);
                                        }
                                        break;
                                    case "int":
                                        try
                                        {
                                            Integer.parseInt(value);
                                            dataTypeMatches.add(true);
                                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
                                            regexMatches.add(flags.get(0));
                                            negativeValues.add(flags.get(1));
                                        } catch (Exception e)
                                        {
                                            dataTypeMatches.add(false);
                                            regexMatches.add(false);
                                            negativeValues.add(false);
                                        }
                                        break;
                                    case "short":
                                        try
                                        {
                                            Short.parseShort(value);
                                            dataTypeMatches.add(true);
                                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
                                            regexMatches.add(flags.get(0));
                                            negativeValues.add(flags.get(1));
                                        } catch (Exception e)
                                        {
                                            dataTypeMatches.add(false);
                                            regexMatches.add(false);
                                            negativeValues.add(false);
                                        }
                                        break;
                                    case "byte":
                                        try
                                        {
                                            Byte.parseByte(value);
                                            dataTypeMatches.add(true);
                                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
                                            regexMatches.add(flags.get(0));
                                            negativeValues.add(flags.get(1));
                                        } catch (Exception e)
                                        {
                                            dataTypeMatches.add(false);
                                            regexMatches.add(false);
                                            negativeValues.add(false);
                                        }
                                        break;
                                    case "float":
                                        try
                                        {
                                            Float.parseFloat(value);
                                            dataTypeMatches.add(true);
                                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
                                            regexMatches.add(flags.get(0));
                                            negativeValues.add(flags.get(1));
                                        } catch (Exception e)
                                        {
                                            dataTypeMatches.add(false);
                                            regexMatches.add(false);
                                            negativeValues.add(false);
                                        }
                                        break;
                                    case "double":
                                        try
                                        {
                                            Double.parseDouble(value);
                                            dataTypeMatches.add(true);
                                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
                                            regexMatches.add(flags.get(0));
                                            negativeValues.add(flags.get(1));
                                        } catch (Exception e)
                                        {
                                            dataTypeMatches.add(false);
                                            regexMatches.add(false);
                                            negativeValues.add(false);
                                        }
                                        break;
                                    case "string":
                                        try
                                        {
                                            value.toString();
                                            dataTypeMatches.add(true);
                                            flags = verifyRegexAndNegativeString(regexFromYaml, negativeStringChecks, value);
                                            regexMatches.add(flags.get(0));
                                            negativeValues.add(flags.get(1));
                                        } catch (Exception e)
                                        {
                                            dataTypeMatches.add(false);
                                            regexMatches.add(false);
                                            negativeValues.add(false);
                                        }
                                        break;
                                }


                            }
                        }
                    }
                    testReportModel.setNetworkCalls(networkCalls);
                    testReportModel.setKeys(keys);
                    testReportModel.setDataTypeMatches(dataTypeMatches);
                    testReportModel.setRegexMatches(regexMatches);
                    testReportModel.setNegativeValues(negativeValues);
                }
            }
        }

        return jsonWriter.writeValueAsString(testReportModel);
    }


    private Boolean verifyNegativeString(List<String> negativeStringValues, String value)
    {
        return negativeStringValues.contains(value);
    }

    private List<Boolean> verifyRegexAndNegativeString(String regexFromYaml, List<String> negativeStringChecks, String value)
    {
        Boolean regexMatchFlag;
        Boolean negativeStringFlag;
        List<Boolean> flags = new ArrayList<>();
        regexMatchFlag = matchRegex(regexFromYaml, value);
        flags.add(regexMatchFlag);
        if (!negativeStringChecks.isEmpty())
        {
            negativeStringFlag = verifyNegativeString(negativeStringChecks, value);
            flags.add(negativeStringFlag);
        }
        else
        {
            flags.add(false);
        }
        return flags;
    }

    public String getLoggingPixelResults(LoggingPixelTestDataModel testCriteria) throws JsonProcessingException
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

        return doesTheLoggingPixelFormatMatches(testCriteria, requestList);
    }

    private Boolean matchRegex(String regex, String keyValue)
    {
        return Pattern.compile(regex).matcher(keyValue).find();
    }
}
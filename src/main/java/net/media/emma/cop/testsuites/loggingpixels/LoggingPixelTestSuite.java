package net.media.emma.cop.testsuites.loggingpixels;

import com.microsoft.playwright.BrowserContext;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;
import net.media.emma.utils.UrlQueryBuild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Service
@Slf4j
public class LoggingPixelTestSuite
{

    @Autowired
    BrowserFactory browserFactory;
    @Autowired
    URLDataProcessing urlUtil;

    String testDummy;

    List<String> loggingPixelFormatData = new ArrayList<>(Arrays.asList("https://hblg.media.net/log"));

    private TestReportModel doesTheLoggingPixelFormatMatches(LoggingPixelTestDataModel testCriteria, String currentUrl)
    {
        //TODO: initial values overwrite the final ones, need to invoke without passing params
        TestReportModel testReportModel= new TestReportModel(currentUrl,"temp",true,true,true,0);

        for (String loggingPixelFormat : loggingPixelFormatData)
        {

            Map<String, String> urlParams;
            if (currentUrl.startsWith(loggingPixelFormat))
            {
                urlParams = UrlQueryBuild.getQueryMap(currentUrl);
                testReportModel.setNetworkCall(currentUrl);
                List<LoggingPixelTestDataModel.DataTypeChecks> testData = testCriteria.getLoggingPixelTests();
                for (LoggingPixelTestDataModel.DataTypeChecks data : testData)
                {
                    String key = data.getKey();
                    testReportModel.setKey(key);
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
                            Boolean regexMatchFlag;
                            Boolean negativeStringFlag;
                            switch (dataTypeOfKey)
                            {
                                case "long":
                                    try
                                    {
                                        Long.parseLong(value);
                                        testReportModel.setDataTypeMatch(true);
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        negativeStringFlag = verifyNegativeString(negativeStringChecks,value);
                                        testReportModel.setRegexMatch(regexMatchFlag);
                                        testReportModel.setNegativeValues(negativeStringFlag);
                                    } catch (Exception e)
                                    {
                                        testReportModel.setDataTypeMatch(false);
                                        testReportModel.setRegexMatch(false);
                                        testReportModel.setNegativeValues(false);
                                    }
                                    break;
                                case "int":
                                    try
                                    {
                                        Integer.parseInt(value);
                                        testReportModel.setDataTypeMatch(true);
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        negativeStringFlag = verifyNegativeString(negativeStringChecks,value);
                                        testReportModel.setRegexMatch(regexMatchFlag);
                                        testReportModel.setNegativeValues(negativeStringFlag);
                                    } catch (Exception e)
                                    {
                                        testReportModel.setDataTypeMatch(false);
                                        testReportModel.setRegexMatch(false);
                                        testReportModel.setNegativeValues(false);
                                    }
                                    break;
                                case "short":
                                    try
                                    {
                                        Short.parseShort(value);
                                        testReportModel.setDataTypeMatch(true);
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        negativeStringFlag = verifyNegativeString(negativeStringChecks,value);
                                        testReportModel.setRegexMatch(regexMatchFlag);
                                        testReportModel.setNegativeValues(negativeStringFlag);
                                    } catch (Exception e)
                                    {
                                        testReportModel.setDataTypeMatch(false);
                                        testReportModel.setRegexMatch(false);
                                        testReportModel.setNegativeValues(false);
                                    }
                                    break;
                                case "byte":
                                    try
                                    {
                                        Byte.parseByte(value);
                                        testReportModel.setDataTypeMatch(true);
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        negativeStringFlag = verifyNegativeString(negativeStringChecks,value);
                                        testReportModel.setRegexMatch(regexMatchFlag);
                                        testReportModel.setNegativeValues(negativeStringFlag);
                                    } catch (Exception e)
                                    {
                                        testReportModel.setDataTypeMatch(false);
                                        testReportModel.setRegexMatch(false);
                                        testReportModel.setNegativeValues(false);
                                    }
                                    break;
                                case "float":
                                    try
                                    {
                                        Float.parseFloat(value);
                                        testReportModel.setDataTypeMatch(true);
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        negativeStringFlag = verifyNegativeString(negativeStringChecks,value);
                                        testReportModel.setRegexMatch(regexMatchFlag);
                                        testReportModel.setNegativeValues(negativeStringFlag);
                                    } catch (Exception e)
                                    {
                                        testReportModel.setDataTypeMatch(false);
                                        testReportModel.setRegexMatch(false);
                                        testReportModel.setNegativeValues(false);
                                    }
                                    break;
                                case "double":
                                    try
                                    {
                                        Double.parseDouble(value);
                                        testReportModel.setDataTypeMatch(true);
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        negativeStringFlag = verifyNegativeString(negativeStringChecks,value);
                                        testReportModel.setRegexMatch(regexMatchFlag);
                                        testReportModel.setNegativeValues(negativeStringFlag);
                                    } catch (Exception e)
                                    {
                                        testReportModel.setDataTypeMatch(false);
                                        testReportModel.setRegexMatch(false);
                                        testReportModel.setNegativeValues(false);
                                    }
                                    break;
                                case "string":
                                    try
                                    {
                                        value.toString();
                                        testReportModel.setDataTypeMatch(true);
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        negativeStringFlag = verifyNegativeString(negativeStringChecks,value);
                                        testReportModel.setRegexMatch(regexMatchFlag);
                                        testReportModel.setNegativeValues(negativeStringFlag);
                                    } catch (Exception e)
                                    {
                                        testReportModel.setDataTypeMatch(false);
                                        testReportModel.setRegexMatch(false);
                                        testReportModel.setNegativeValues(false);
                                    }
                                    break;
                            }

                        }
                    }
                }
                // TODO append the testReportModel to preserve all values
                //failureLogging.add(logUrlDetails);
            }

        }
        return testReportModel;
    }

    private Boolean verifyNegativeString(List<String> negativeStringChecks, String value)
    {
        //return negativeStringChecks.contains(value);
        //TODO : heena to fix the above
        return true;
    }

    public String getLoggingPixelResults(LoggingPixelTestDataModel testCriteria)
    {
        BrowserContext bc = browserFactory.getBc("Chrome.Desktop.Mac", null);
        AtomicReference<TestReportModel> urlDetails = new AtomicReference<>();
        bc.onRequest(request ->
        {
            String networkCall = request.url();
            urlDetails.set(doesTheLoggingPixelFormatMatches(testCriteria, networkCall));
        });
        bc.newPage().navigate(testCriteria.getPubUrl());
        bc.close();
        return urlDetails.toString();
    }

    private Boolean matchRegex(String regex, String keyValue)
    {
        return Pattern.compile(regex).matcher(keyValue).find();
    }
}
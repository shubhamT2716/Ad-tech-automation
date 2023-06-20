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
        TestReportModel testReportModel= new TestReportModel();
        // Create lists for each variable
        List<String> networkCalls = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        List<Boolean> dataTypeMatches = new ArrayList<>();
        List<Boolean> regexMatches = new ArrayList<>();
        List<Boolean> negativeValues = new ArrayList<>();
        for (String loggingPixelFormat : loggingPixelFormatData)
        {
            Map<String, String> urlParams;
            if (currentUrl.startsWith(loggingPixelFormat))
            {
                if(!networkCalls.contains(loggingPixelFormat))
                {
                    networkCalls.add(currentUrl);
                    urlParams = UrlQueryBuild.getQueryMap(currentUrl);
                    List<LoggingPixelTestDataModel.DataTypeChecks> testData = testCriteria.getLoggingPixelTests();
                    for (LoggingPixelTestDataModel.DataTypeChecks data : testData)
                    {
                        String key = data.getKey();
                        if (!keys.contains(key))
                        {
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
                                    keys.add(key);
                                    switch (dataTypeOfKey)
                                    {
                                        case "long":
                                            try
                                            {
                                                Long.parseLong(value);
                                                dataTypeMatches.add(true);

                                                regexMatchFlag = matchRegex(regexFromYaml, value);
                                                regexMatches.add(regexMatchFlag);
                                                if (!negativeStringChecks.isEmpty())
                                                {
                                                    negativeStringFlag = verifyNegativeString(negativeStringChecks, value);
                                                    negativeValues.add(negativeStringFlag);
                                                }
                                                else negativeValues.add(false);
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
                                                regexMatchFlag = matchRegex(regexFromYaml, value);
                                                regexMatches.add(regexMatchFlag);
                                                if (!negativeStringChecks.isEmpty())
                                                {
                                                    negativeStringFlag = verifyNegativeString(negativeStringChecks, value);
                                                    negativeValues.add(negativeStringFlag);
                                                }else negativeValues.add(false);
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
                                                regexMatchFlag = matchRegex(regexFromYaml, value);
                                                regexMatches.add(regexMatchFlag);
                                                if (!negativeStringChecks.isEmpty())
                                                {
                                                    negativeStringFlag = verifyNegativeString(negativeStringChecks, value);
                                                    negativeValues.add(negativeStringFlag);
                                                }else negativeValues.add(false);
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
                                                regexMatchFlag = matchRegex(regexFromYaml, value);
                                                regexMatches.add(regexMatchFlag);
                                                if (!negativeStringChecks.isEmpty())
                                                {
                                                    negativeStringFlag = verifyNegativeString(negativeStringChecks, value);
                                                    negativeValues.add(negativeStringFlag);
                                                }else negativeValues.add(false);
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
                                                regexMatchFlag = matchRegex(regexFromYaml, value);
                                                regexMatches.add(regexMatchFlag);
                                                if (!negativeStringChecks.isEmpty())
                                                {
                                                    negativeStringFlag = verifyNegativeString(negativeStringChecks, value);
                                                    negativeValues.add(negativeStringFlag);
                                                }else negativeValues.add(false);
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
                                                regexMatchFlag = matchRegex(regexFromYaml, value);
                                                regexMatches.add(regexMatchFlag);
                                                if (!negativeStringChecks.isEmpty())
                                                {
                                                    negativeStringFlag = verifyNegativeString(negativeStringChecks, value);
                                                    negativeValues.add(negativeStringFlag);
                                                }else negativeValues.add(false);
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
                                                regexMatchFlag = matchRegex(regexFromYaml, value);
                                                regexMatches.add(regexMatchFlag);
                                                if (!negativeStringChecks.isEmpty())
                                                {
                                                    negativeStringFlag = verifyNegativeString(negativeStringChecks, value);
                                                    negativeValues.add(negativeStringFlag);
                                                }else negativeValues.add(false);
                                            } catch (Exception e)
                                            {
                                                dataTypeMatches.add(false);
                                                regexMatches.add(false);
                                                negativeValues.add(false);
                                            }
                                            break;
                                    }
                                    log.info("networkCalls to string : "+networkCalls.toString());
                                    log.info("keys to string : "+keys.toString());
                                    log.info("dataTypeMatches to string : "+dataTypeMatches.toString());
                                    log.info("regexMatches to string : "+regexMatches.toString());
                                    log.info("negativeValues to string : "+negativeValues.toString());

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
        return testReportModel;
    }

    private Boolean verifyNegativeString(List<String> negativeStringValues, String value)
    {
        return negativeStringValues.contains(value);
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
        bc.setDefaultTimeout(0);
        bc.newPage().navigate(testCriteria.getPubUrl());
        bc.close();
        return urlDetails.toString();
    }

    private Boolean matchRegex(String regex, String keyValue)
    {
        return Pattern.compile(regex).matcher(keyValue).find();
    }
}
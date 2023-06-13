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

    private ArrayList<HashMap<String, Object>> doesTheLoggingPixelFormatMatches(LoggingPixelTestDataModel testCriteria, String currentUrl)
    {

        ArrayList<HashMap<String, Object>> failureLogging = new ArrayList<>();
        HashMap<String, Object> logUrlDetails = new HashMap<>();

        for (String loggingPixelFormat : loggingPixelFormatData)
        {

            Map<String, String> urlParams;
            if (currentUrl.startsWith(loggingPixelFormat))
            {
                urlParams = UrlQueryBuild.getQueryMap(currentUrl);
                logUrlDetails.put("url", currentUrl);
                List<LoggingPixelTestDataModel.DataTypeChecks> testData = testCriteria.getLoggingPixelTests();
                for (LoggingPixelTestDataModel.DataTypeChecks data : testData)
                {
                    String key = data.getKey();
                    logUrlDetails.put("key", key);
                    String regexFromYaml = data.getValueRegex();
                    if (urlParams.containsKey(key))
                    {
                        String value = null;
                        //value of the key
                        value = urlParams.get(key);
                        if (value != null)
                        {
                            String dataTypeOfKey = data.getValueDataType().toLowerCase();
                            Boolean regexMatchFlag;
                            switch (dataTypeOfKey)
                            {
                                case "long":
                                    try
                                    {
                                        Long.parseLong(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "int":
                                    try
                                    {
                                        Integer.parseInt(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "short":
                                    try
                                    {
                                        Short.parseShort(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "byte":
                                    try
                                    {
                                        Byte.parseByte(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "float":
                                    try
                                    {
                                        Float.parseFloat(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "double":
                                    try
                                    {
                                        Double.parseDouble(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "string":
                                    try
                                    {
                                        value.toString();
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(regexFromYaml, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                            }

                        }
                    }

                }
                failureLogging.add(logUrlDetails);
            }

        }
        return failureLogging;
    }

    public String getLoggingPixelResults(LoggingPixelTestDataModel testCriteria)
    {
        BrowserContext bc = browserFactory.getBc("Chrome.Desktop.Mac", null);
        AtomicReference<ArrayList<HashMap<String, Object>>> urlDetails = new AtomicReference<>(new ArrayList<>());
        bc.onRequest(request ->
        {
            String networkCall = request.url();
            urlDetails.set(doesTheLoggingPixelFormatMatches(testCriteria, networkCall));
        });
        bc.newPage().navigate(testCriteria.getPubUrl());
        bc.close();
        return Arrays.toString(urlDetails.get().toArray()).replace(", ", "<br>\n");
    }

    private Boolean matchRegex(String regex, String keyValue)
    {
        return Pattern.compile(regex).matcher(keyValue).find();
    }
}
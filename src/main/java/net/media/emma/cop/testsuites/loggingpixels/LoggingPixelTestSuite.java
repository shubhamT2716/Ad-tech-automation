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
            if (currentUrl.startsWith(loggingPixelFormat)) //TODO: this needs to be defined by business
            {
                urlParams = UrlQueryBuild.getQueryMap(currentUrl);
                logUrlDetails.put("url", currentUrl);
                List<LoggingPixelTestDataModel.DataTypeChecks> testData = testCriteria.getLoggingPixelTests();
                for (LoggingPixelTestDataModel.DataTypeChecks data : testData)
                {
                    String key = data.getKey();
                    logUrlDetails.put("key", key);
                    if (urlParams.containsKey(key))
                    {
                        String value = null;
                        //value of the key
                        value = urlParams.get(data.getKey()); //TODO run loop for all values
                        if (value != null)
                        {
                            String dataTypeOfKey = data.getValueDataType().toLowerCase();
                            Boolean regexMatchFlag;
                            switch (dataTypeOfKey)
                            {
                                case "long":
                                    try
                                    {
                                        long longValue = Long.parseLong(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(data, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "int":
                                    try
                                    {
                                        int intValue = Integer.parseInt(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(data, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "short":
                                    try
                                    {
                                        short shortValue = Short.parseShort(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(data, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "byte":
                                    try
                                    {
                                        byte byteValue = Byte.parseByte(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(data, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "float":
                                    try
                                    {
                                        float floatValue = Float.parseFloat(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(data, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "double":
                                    try
                                    {
                                        double doubleValue = Double.parseDouble(value);
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(data, value);
                                        logUrlDetails.put("regexMatch", regexMatchFlag);
                                    } catch (Exception e)
                                    {
                                        logUrlDetails.put("dataType", "false");
                                    }
                                    break;
                                case "string":
                                    try
                                    {
                                        String stringValue = value.toString();
                                        logUrlDetails.put("dataType", "true");
                                        regexMatchFlag = matchRegex(data, value);
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

        //   Map<String, String> urlFromNetworkTab = new HashMap<>();
        AtomicReference<ArrayList<HashMap<String, Object>>> urlDetails = new AtomicReference<>(new ArrayList<>());
        bc.onRequest(request ->
        {
            String networkCall = request.url();
            urlDetails.set(doesTheLoggingPixelFormatMatches(testCriteria, networkCall));
            //   urlFromNetworkTab.put("url", networkCall);
            // urlFromNetworkTab.add(networkCall);
        });
        bc.newPage().navigate(testCriteria.getPubUrl());
        bc.close();
        return Arrays.toString(urlDetails.get().toArray()).replace(", ", "<br>\n");
    }

    private Boolean matchRegex(LoggingPixelTestDataModel.DataTypeChecks data, String keyValue)
    {
        return Pattern.compile(data.getValueRegex()).matcher(keyValue).find();
    }
}
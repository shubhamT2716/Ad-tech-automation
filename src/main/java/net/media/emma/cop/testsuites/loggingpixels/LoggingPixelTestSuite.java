package net.media.emma.cop.testsuites.loggingpixels;

import com.microsoft.playwright.BrowserContext;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class LoggingPixelTestSuite {

    @Autowired BrowserFactory browserFactory;
    @Autowired URLDataProcessing urlUtil;


    String testDummy;

    public String getLoggingPixelResults(LoggingPixelTestDataModel testCriteria){

        for (int i=0;i<testCriteria.getLoggingPixelTests().size();i++)
        {
            System.out.println("testCriteriaKeyFromTestSuite = " + testCriteria.getLoggingPixelTests().get(i).getKey());
            System.out.println("testCriteriaValueDataTypeFromTestSuite = " + testCriteria.getLoggingPixelTests().get(i).getValueDataType());
            System.out.println("testCriteriaValueRegexFromTestSuite = " + testCriteria.getLoggingPixelTests().get(i).getValueRegex());
        }
        BrowserContext bc = browserFactory.getBc("Chrome.Desktop.Mac", null);

        List<String> urlFromNetworkTab = new ArrayList<>();

        bc.onRequest(request -> {

            urlFromNetworkTab.add(request.url());
        });

        bc.newPage().navigate(testCriteria.getPubUrl());
        bc.close();
        return Arrays.toString(urlFromNetworkTab.toArray()).replace(", ", "<br>\n");

    }


}

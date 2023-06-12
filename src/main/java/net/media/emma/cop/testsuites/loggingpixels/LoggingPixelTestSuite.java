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

//        testDummy = testCriteria.getLoggingPixelTests().get(0).getKey();

        BrowserContext bc = browserFactory.getBc("Chrome.Desktop.Mac", new ArrayList<>());

        //list of arrays initialization
        List<String> urlFromNetworkTab = new ArrayList<>();

        bc.onRequest(request -> {

            urlFromNetworkTab.add(request.url());
        });

        bc.newPage().navigate("https://www.nutritionix.com/?mnet_test=1&force_hbtest=all");

        return Arrays.toString(urlFromNetworkTab.toArray()).replace(", ", "<br>\n");

    }


}

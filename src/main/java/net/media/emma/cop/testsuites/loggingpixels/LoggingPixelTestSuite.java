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

        BrowserContext bc = browserFactory.getBc("Chrome.Desktop.Mac", null);

        //list of arrays initialization
        List<String> urlFromNetworkTab = new ArrayList<>();

        bc.onRequest(request -> {
            String networkCall = request.url();
            urlFromNetworkTab.add(networkCall);

            List<LoggingPixelTestDataModel.DataTypeChecks> testData =  testCriteria.getLoggingPixelTests();
            for(LoggingPixelTestDataModel.DataTypeChecks data:testData){
                if(networkCall.contains(data.getKey())){
                    networkCall.
                }
            }

            request.url().contains("media.net");
        });


        bc.newPage().navigate("https://www.forbes.com?mnet_test=1&force_hbtest=all");

        return Arrays.toString(urlFromNetworkTab.toArray()).replace(", ", "\n");

    }


}

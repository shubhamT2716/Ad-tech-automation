package net.media.emma.cop.testsuites.loggingpixels;

import com.microsoft.playwright.BrowserContext;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class LoggingPixelTestSuite {

    @Autowired BrowserFactory browserFactory;
    @Autowired URLDataProcessing urlUtil;


    String testDummy;

    public String getLoggingPixelResults(LoggingPixelTestDataModel testCriteria){

     /*   for (int i=0;i<testCriteria.getLoggingPixelTests().size();i++)
        {
            System.out.println("testCriteriaKeyFromTestSuite = " + testCriteria.getLoggingPixelTests().get(i).getKey());
            System.out.println("testCriteriaValueDataTypeFromTestSuite = " + testCriteria.getLoggingPixelTests().get(i).getValueDataType());
            System.out.println("testCriteriaValueRegexFromTestSuite = " + testCriteria.getLoggingPixelTests().get(i).getValueRegex());
        }*/
        BrowserContext bc = browserFactory.getBc("Chrome.Desktop.Mac", null);

        List<String> urlFromNetworkTab = new ArrayList<>();

        bc.onRequest(request -> {
            String networkCall = request.url();
            urlFromNetworkTab.add(networkCall);
            List<String> queryParams = null;

            if(networkCall.split(String.valueOf(Pattern.compile("\\?"))).length>1){
                queryParams = List.of(networkCall.split(String.valueOf(Pattern.compile("\\?")))[1].split(String.valueOf(Pattern.compile("="))));
                System.out.println("queryParams.toString() = " + queryParams.toString());
                List<LoggingPixelTestDataModel.DataTypeChecks> testData =  testCriteria.getLoggingPixelTests();
                for(LoggingPixelTestDataModel.DataTypeChecks data:testData){
                    if(queryParams.contains(data.getKey())){

                    }
                }
            }

        });

        bc.newPage().navigate(testCriteria.getPubUrl());
        bc.close();
        return Arrays.toString(urlFromNetworkTab.toArray()).replace(", ", "<br>\n");

    }


}

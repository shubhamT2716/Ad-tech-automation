package net.media.emma.cop.testsuites.loggingpixels;

import com.microsoft.playwright.BrowserContext;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;
import net.media.emma.utils.UrlQueryBuild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

            urlFromNetworkTab.add(request.url());
            if (request.url().contains("hblg.media.net")) //TODO: this needs to be defined by business
            {
                Map<String, String> urlParams = UrlQueryBuild.getQueryMap(request.url());
                String value = urlParams.get(testCriteria.getLoggingPixelTests().get(0).getKey()); //TODO run loop for all values
                if (value == null)
                {
                    System.out.println("param is null = " + value);
                }
                else
                {
                    System.out.println("value = " + value);
                    if (Objects.equals(testCriteria.getLoggingPixelTests().get(0).getValueDataType(), "long"))
                    {
                        long correctDataTypeValue = Long.parseLong(value);
                        System.out.println("correctDataTypeValue = " + correctDataTypeValue);

                        Boolean regexMatched = Pattern.compile(testCriteria.getLoggingPixelTests().get(0).getValueRegex()).matcher(value).find(); //TODO run loop for all values
                        if (regexMatched)
                        {
                            System.out.println("regexMatched = " + regexMatched);
                        }
                        else
                        {
                            System.out.println("regexMatched = " + regexMatched);
                        }
                    }
                }
            }
        });

        bc.newPage().navigate(testCriteria.getPubUrl());
        bc.close();
        return Arrays.toString(urlFromNetworkTab.toArray()).replace(", ", "<br>\n");

    }


}

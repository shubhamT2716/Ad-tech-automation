package net.media.emma.cop.testsuites.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class URLChecks {
    
    @Builder.Default
    Map<String, PositiveURLChecks> positiveUrlChecks = new HashMap<String, PositiveURLChecks>();
    @Builder.Default
    Map<String, NegativeURLChecks> negativeUrlChecks = new HashMap<String, NegativeURLChecks>();

    enum TestResult{
        PASS,
        FAIL,
        NA
    }

    @Getter @Setter @Builder
    public static class PositiveURLChecks{
        String label;
        String urlPattern;
        
        @Builder.Default
        TestResult testResult = TestResult.PASS;

        @Builder.Default
        List<String>  testMessages = new ArrayList<>();

        @Builder.Default
        List<String> matchingUrls = new ArrayList<>();

        @Builder.Default
        Integer countOfMatchingUrls = 0;
        
        @Builder.Default
        Map<String, List<String>> positiveKeyValChecks = new HashMap<String, List<String>>();
        
        @Builder.Default
        Map<String, List<String>> negativeKeyValChecks = new HashMap<String, List<String>>();
        
        @Builder.Default
        Map<String, List<String>> anyOneOfKeyValChecks = new HashMap<String, List<String>>();
    }

    @Getter @Setter @Builder
    public static class NegativeURLChecks{
        String label;
        String urlPattern;
        @Builder.Default
        TestResult testResult = TestResult.PASS;

        @Builder.Default
        List<String>  matchingUrls = new ArrayList<>();

        @Builder.Default
        Integer countOfMatchingUrls = 0;
    }

    public void updateMatchingUrlsCount(){
        positiveUrlChecks.forEach((key, value) -> {
            value.setCountOfMatchingUrls(value.getMatchingUrls().size());
        });
        negativeUrlChecks.forEach((key, value) -> {
            value.setCountOfMatchingUrls(value.getMatchingUrls().size());
        });
    }

    public void updateNetworkCallErrors(TestCriteriaBo testData ){

        testData.getNetworkCallsPositive().forEach((key, value) -> {
            if(!positiveUrlChecks.containsKey(key)){
                positiveUrlChecks.put(key,  PositiveURLChecks.builder().label(key).testResult(TestResult.FAIL).urlPattern(value).build());
            }
        });

        testData.getNetworkCallsNegative().forEach((key, value) -> {
            if(!negativeUrlChecks.containsKey(key)){
                negativeUrlChecks.put(key,  NegativeURLChecks.builder().label(key).urlPattern(value).build());
            }else{
                negativeUrlChecks.get(key).setTestResult(TestResult.FAIL);
            }
        });
    }

    public void verifyPositiveKeyValChecks(){

        positiveUrlChecks.forEach((key, value) -> {
            value.getPositiveKeyValChecks().forEach((k, v) -> {
                if(v.size() == 0){
                    value.getTestMessages().add("No positive key value checks found for key: " + k);
                    value.setTestResult(TestResult.FAIL);
                }
            });
        });
       
    }
}
package net.media.emma.cop.testsuites.yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.HashSet;

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

        @Builder.Default @JsonIgnore
        List<HashMap<String, String>> queryStringMap = new ArrayList<>();

        @Builder.Default
        Integer countOfMatchingUrls = 0;
        
        @Builder.Default
        Map<String, Set<String>> positiveKeyValChecks = new HashMap<String, Set<String>>();
        
        @Builder.Default
        Map<String, Set<String>> negativeKeyValChecks = new HashMap<String, Set<String>>();
        
        @Builder.Default
        Map<String, Set<String>> anyOneOfKeyValChecks = new HashMap<String, Set<String>>();
    }

    @Getter @Setter @Builder
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
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
                positiveUrlChecks.put(key,  PositiveURLChecks.builder()
                    .label(key)
                    .testResult(TestResult.FAIL)
                    .testMessages(Arrays.asList("Call not fired"))
                    .urlPattern(value)
                    .build());
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

        positiveUrlChecks.forEach((callLabel, callData) -> { //key, value --> lable, PositiveURLChecks obj

            if (callData.getPositiveKeyValChecks() != null)

                callData.getPositiveKeyValChecks().forEach((qsParam, listOfValues) -> {

                    Set<String> actualValues = new HashSet<>();

                    callData.getQueryStringMap().forEach((qsMapFromNetworkCall) -> {
                        if(qsMapFromNetworkCall.containsKey(qsParam)){
                            actualValues.add(qsMapFromNetworkCall.get(qsParam));
                        }
                    });

                    if(!actualValues.equals(listOfValues)){
                        callData.getTestMessages().add("Positive key value check failed for key: " + qsParam + ". Expected: " + listOfValues + " Actual: " + actualValues);
                        callData.setTestResult(TestResult.FAIL);
                    }

                });

            if (callData.getNegativeKeyValChecks() != null) 

                callData.getNegativeKeyValChecks().forEach((qsParam, listOfValues) -> {

                    Set<String> actualValues = new HashSet<>();

                    callData.getQueryStringMap().forEach((qsMapFromNetworkCall) -> {
                        if(qsMapFromNetworkCall.containsKey(qsParam)){
                            actualValues.add(qsMapFromNetworkCall.get(qsParam));
                        }
                    });

                    if(actualValues.retainAll(listOfValues)){
                        callData.getTestMessages().add("Negative key value check failed for key: " + qsParam + ". Found negative matches: " + actualValues);
                        callData.setTestResult(TestResult.FAIL);
                    }

                });

            if (callData.getAnyOneOfKeyValChecks() != null)

                callData.getAnyOneOfKeyValChecks().forEach((qsParam, listOfValues) -> {

                    Set<String> actualValues = new HashSet<>();

                    callData.getQueryStringMap().forEach((qsMapFromNetworkCall) -> {
                        if(qsMapFromNetworkCall.containsKey(qsParam)){
                            actualValues.add(qsMapFromNetworkCall.get(qsParam));
                        }
                    });

                    if(actualValues.retainAll(listOfValues) && actualValues.isEmpty()  ){
                        callData.getTestMessages().add("Any one of key value check failed for key: " + qsParam + ". Expected one of: " + listOfValues + " Actual: " + actualValues);
                        callData.setTestResult(TestResult.FAIL);
                    }

                });
        });
       
    }
}
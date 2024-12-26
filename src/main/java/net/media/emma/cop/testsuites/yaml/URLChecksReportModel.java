package net.media.emma.cop.testsuites.yaml;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class URLChecksReportModel {

    @Builder.Default
    Map<String, PositiveURLChecks> positiveUrlChecks = new HashMap<String, PositiveURLChecks>();
    @Builder.Default
    Map<String, NegativeURLChecks> negativeUrlChecks = new HashMap<String, NegativeURLChecks>();
    @Builder.Default
    Map<String, Map<String, Map<String, Object>>> bidderDataAssertionKeyVerification = new HashMap<>();
    @Builder.Default
    Map<String, List<BidderDataAssertion>> bidderDataAssertions = new HashMap<>();
    @Builder.Default
    Map<String, List<HeadersAssertion>> headerAssertions = new HashMap<>();
//    @Builder.Default
//    List<Map<String, String>> getpayloadDetails=new ArrayList();

    public void updateMatchingUrlsCount() {
        positiveUrlChecks.forEach((key, value) -> {
            value.setCountOfMatchingUrls(value.getMatchingUrls().size());
        });
        negativeUrlChecks.forEach((key, value) -> {
            value.setCountOfMatchingUrls(value.getMatchingUrls().size());
        });
    }

    public void updateNetworkCallErrors(TestCriteriaBo testData) {

        testData.getNetworkCallsPositive().forEach((key, value) -> {
            if (!positiveUrlChecks.containsKey(key)) {
                positiveUrlChecks.put(key, PositiveURLChecks.builder().label(key).urlPattern(value).testResult(TestResult.FAIL)
                        .testMessages(List.of("Call not fired")).build());
            }

        });

        testData.getNetworkCallsNegative().forEach((key, value) -> {
            if (!negativeUrlChecks.containsKey(key)) {
//                negativeUrlChecks.put(key, NegativeURLChecks.builder().label(key).urlPattern(value).build());

                negativeUrlChecks.put(key, NegativeURLChecks.builder().label(key).testResult(TestResult.NOTPASS).urlPattern(value).build());


            } else {
                negativeUrlChecks.get(key).setTestResult(TestResult.FAIL);
                negativeUrlChecks.get(key).setTestResult(TestResult.FAIL);
            }
        });
    }

    public void verifyPositiveKeyValChecks() {

        Set<String> ralog = new HashSet<>();

        ralog.add("RA");

        try {

            positiveUrlChecks.forEach((callLabel, callData) -> {  // key, value --> label, PositiveURLChecks obj

                if (callData.getPositiveKeyValChecks() != null) {
                    callData.getPositiveKeyValChecks().forEach((qsParam, listOfValues) -> {
                        Set<String> actualValues = new HashSet<>();

                        callData.getQueryStringMap().forEach(qsMapFromNetworkCall -> {
                            if (qsMapFromNetworkCall.containsKey(qsParam)) {
                            	String actual=URLDecoder.decode(qsMapFromNetworkCall.get(qsParam), StandardCharsets.UTF_8);
                                actualValues.add(actual);
                            }
                        });
                    

                        log.info(" Expected: " + listOfValues + " Actual: " + actualValues);


                        if (listOfValues.equals(ralog) && !actualValues.equals(ralog) && callData.getLabel().equals("awlog")) {
                            callData.getTestMessages().add("Call not fired");

                        } else if (!actualValues.equals(listOfValues)) {
//                        callData.getTestMessages().add("Positive key value check Pass for key: " + qsParam
//                                + ". Expected: " + listOfValues + " Actual: " + actualValues);
//                    } else {
                            callData.getTestMessages().add("Positive key value check failed for key: " + qsParam
                                    + ". Expected: " + listOfValues + " Actual: " + actualValues);
                            callData.setTestResult(TestResult.FAIL);
                        }
                    });
                }

                // Negative Key Value Checks
                if (callData.getNegativeKeyValChecks() != null) {
                    callData.getNegativeKeyValChecks().forEach((qsParam, listOfValues) -> {
                        Set<String> actualValues = new HashSet<>();

                        callData.getQueryStringMap().forEach(qsMapFromNetworkCall -> {
                            if (qsMapFromNetworkCall.containsKey(qsParam)) {
                                actualValues.add(qsMapFromNetworkCall.get(qsParam));
                            }
                        });

                        if (actualValues.equals(listOfValues)) {
                            callData.getTestMessages().add("Negative key value check Pass for key: " + qsParam
                                    + ". Expected: "+listOfValues+" Actual: " + actualValues );
                            callData.setTestResult(TestResult.FAIL);
                        } //else {
//                            callData.getTestMessages().add("Negative key value check failed for key: " + qsParam
//                                    + ". Expected: "+listOfValues+" Actaul: " + actualValues);
//                        }
                    });
                }

                // Any One Of Key Value Checks
                if (callData.getAnyOneOfKeyValChecks() != null) {
                    callData.getAnyOneOfKeyValChecks().forEach((qsParam, listOfValues) -> {
                        Set<String> actualValues = new HashSet<>();

                        callData.getQueryStringMap().forEach(qsMapFromNetworkCall -> {
                            if (qsMapFromNetworkCall.containsKey(qsParam)) {
                                actualValues.add(qsMapFromNetworkCall.get(qsParam));
                            }
                        });

                        if (!Collections.disjoint(actualValues, listOfValues)) {
                            callData.getTestMessages().add("Any one of key value check Pass for key: " + qsParam
                                    + ". Expected one of: " + listOfValues + " Actual: " + actualValues);
                        } else {
                            callData.getTestMessages().add("Any one of key value check failed for key: " + qsParam
                                    + ". Expected one of: " + listOfValues + " Actual: " + actualValues);
                            callData.setTestResult(TestResult.FAIL);
                        }
                    });
                }

                // Request Headers Checks
                if (callData.getRequestHeadersChecks() != null && !callData.getRequestHeadersChecks().isEmpty()) {
                    boolean allMatched = false;

                    for (Map<String, Object> headerMap : callData.getQueryStringMapRequestHeader()) {
                        allMatched = compareHeaders(callData.getRequestHeadersChecks(), headerMap);
                        if (allMatched)
                            break; // Only break when all headers match
                    }

                    if (!allMatched) {
                        callData.getTestMessages().add("Failed request headers verification");
                        callData.setTestResult(TestResult.FAIL);
                    }
                }

                // Response Headers Checks
                if (callData.getResponseHeadersChecks() != null && !callData.getResponseHeadersChecks().isEmpty()) {
                    boolean allMatched = false;

                    for (Map<String, Object> headerMap : callData.getQueryStringMapResponseHeader()) {
                        allMatched = compareHeaders(callData.getResponseHeadersChecks(), headerMap);
                        if (allMatched)
                            break; // Only break when all headers match
                    }

                    if (!allMatched) {
                        callData.getTestMessages().add("Failed response headers verification");
                        callData.setTestResult(TestResult.FAIL);
                    }
                }

            });

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private boolean compareHeaders(Map<String, Object> expectedHeaders, Map<String, Object> actualHeaders) {
        for (Map.Entry<String, Object> entry : expectedHeaders.entrySet()) {
            String key = entry.getKey().toLowerCase();
            Object expectedValue = entry.getValue();
            Object actualValue = actualHeaders.get(key);

            if (expectedValue instanceof ArrayList && actualValue instanceof String) {
                Map<String, String> actualMap = Arrays.stream(((String) actualValue).split(";"))
                        .map(String::trim)
                        .map(str -> str.split("=", 2))
                        .collect(Collectors.toMap(arr -> arr[0], arr -> arr.length > 1 ? arr[1] : ""));

                for (Map<String, String> expectedMap : (ArrayList<Map<String, String>>) expectedValue) {
                    for (Map.Entry<String, String> expectedEntry : expectedMap.entrySet()) {
                        String expectedKey = expectedEntry.getKey().trim();
                        String expectedVal = expectedEntry.getValue().trim();
                        if (!actualMap.containsKey(expectedKey) || !actualMap.get(expectedKey).equals(expectedVal)) {
                            return false;
                        }
                    }
                }
            } else if (expectedValue instanceof String && actualValue != null) {
                if (!actualValue.toString().equals(expectedValue)) {
                    return false;
                }
            } else if (actualValue == null) {
                return false;
            }
        }
        return true;
    }

    public void updateNetworkCallErrors(net.media.emma.cop.testsuites.pagetests.TestCriteriaBo testData) {
//		testData.getNetworkCallsPositive().forEach((key, value) -> {
//			if (!positiveUrlChecks.containsKey(key)) {
//				positiveUrlChecks.put(key, PositiveURLChecks.builder().label(key).testResult(TestResult.FAIL)
//						.testMessages(Arrays.asList("Call not fired")).urlPattern(value).build());
//			}
//		});
//
//		testData.getNetworkCallsNegative().forEach((key, value) -> {
//			if (!negativeUrlChecks.containsKey(key)) {
//				negativeUrlChecks.put(key, NegativeURLChecks.builder().label(key).urlPattern(value).build());
//			} else {
//				negativeUrlChecks.get(key).setTestResult(TestResult.FAIL);
//			}
//		});


        testData.getNetworkCallsPositive().forEach((key, value) -> {
            if (!positiveUrlChecks.containsKey(key)) {
                positiveUrlChecks.put(key, PositiveURLChecks.builder().label(key).urlPattern(value).testResult(TestResult.FAIL)
                        .testMessages(List.of("call not fired")).build());
            }
        });


        testData.getNetworkCallsNegative().forEach((key, value) -> {
            if (!negativeUrlChecks.containsKey(key)) {
                negativeUrlChecks.put(key, NegativeURLChecks.builder().label(key).urlPattern(value).build());
            } else {
                negativeUrlChecks.get(key).setTestResult(TestResult.FAIL);
            }
        });

    }

    enum TestResult {
        PASS, FAIL, NOTPASS, NA
    }

    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositiveURLChecks {
        String label;
        String urlPattern;

        @Builder.Default
        TestResult testResult = TestResult.PASS;

        @Builder.Default
        List<String> testMessages = new ArrayList<>();

        @Builder.Default
        List<String> matchingUrls = new ArrayList<>();

        @Builder.Default
        @JsonIgnore
        List<Map<String, String>> queryStringMap = new ArrayList<>();

        @Builder.Default
        Integer countOfMatchingUrls = 0;

        @Builder.Default
        Map<String, Set<String>> positiveKeyValChecks = new HashMap<String, Set<String>>();

        @Builder.Default
        Map<String, Set<String>> negativeKeyValChecks = new HashMap<String, Set<String>>();

        @Builder.Default
        Map<String, Set<String>> anyOneOfKeyValChecks = new HashMap<String, Set<String>>();

        @Builder.Default
        Map<String, Object> requestHeadersChecks = new HashMap<>(); //field for request headers checks

        @Builder.Default
        Map<String, Object> responseHeadersChecks = new HashMap<>(); //field for response headers checks

        @JsonIgnore
        @Builder.Default
        List<HashMap<String, Object>> queryStringMapRequestHeader = new ArrayList<>();

        @JsonIgnore
        @Builder.Default
        List<HashMap<String, Object>> queryStringMapResponseHeader = new ArrayList<>();

    }

    @Getter
    @Setter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NegativeURLChecks {
        String label;
        String urlPattern;
        @Builder.Default
        TestResult testResult = TestResult.PASS;

        @Builder.Default
        List<String> matchingUrls = new ArrayList<>();

        @Builder.Default
        Integer countOfMatchingUrls = 0;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    public static class BidderDataAssertion {
        String message;
        TestResult testResult;
        String url;
        MatchingJsonPath matchingJson;

        // ... any other fields required
    }

    @Getter
    @Setter
    @Builder
    @ToString
    public static class MatchingJsonPath {
        String jsonPath;
        String Value;
    }


//    public void verifyPositiveKeyValChecks() {
//
//        positiveUrlChecks.forEach((callLabel, callData) -> { // key, value --> lable, PositiveURLChecks obj
//
//            if (callData.getPositiveKeyValChecks() != null) {
//            	
//         	
//
//            	
//                callData.getPositiveKeyValChecks().forEach((qsParam, listOfValues) -> {
//
//                	Set<String> actualValues = new HashSet<>();
//                	
//                	
//
//                    callData.getQueryStringMap().forEach(qsMapFromNetworkCall -> {
//                    	
//                    	
//                    	
//                        if (qsMapFromNetworkCall.containsKey(qsParam)) {
//                        	
//
//                            actualValues.add(qsMapFromNetworkCall.get(qsParam));
//                        }
//                    });
//                    
//                  if(actualValues.equals(listOfValues))
//                  {
//          	  
//                	  callData.getTestMessages().add("Positive key value check Pass for key: " + qsParam
//                              + ". Expected: " + listOfValues + " Actual: " + actualValues);
//                  }
//                    
//
//                    if (!actualValues.equals(listOfValues)) {
//                    	
//
//                        callData.getTestMessages().add("Positive key value check failed for key: " + qsParam
//                                + ". Expected: " + listOfValues + " Actual: " + actualValues);
//                        callData.setTestResult(TestResult.FAIL);
//                    }
//
//                });
//                
//            }
//
//            if (callData.getNegativeKeyValChecks() != null) {
//
//                callData.getNegativeKeyValChecks().forEach((qsParam, listOfValues) -> {
//
//                    Set<String> actualValues = new HashSet<>();
//
//                    callData.getQueryStringMap().forEach((qsMapFromNetworkCall) -> {
//                        if (qsMapFromNetworkCall.containsKey(qsParam)) {
//
//                            actualValues.add(qsMapFromNetworkCall.get(qsParam));
//
//                        }
//
//                    });
//
////					if (actualValues.retainAll(listOfValues) == false && actualValues.isEmpty()) {
////						callData.getTestMessages().add("Negative key value check failed for key: " + qsParam
////								+ ". Found negative matches: " + actualValues);
////						callData.setTestResult(TestResult.FAIL);
////					}
//
//                    if (actualValues.equals(listOfValues)) {
//                        callData.getTestMessages().add("Negative key value check Pass for key: " + qsParam
//                                + ". Found negative matches: " + actualValues);
//                       
//                    }
//                    
//                    else  {
//                        callData.getTestMessages().add("Negative key value check failed for key: " + qsParam
//                                + ". Found negative matches: " + actualValues);
//                        callData.setTestResult(TestResult.FAIL);
//                    }
//
//                });
//            }
//            if (callData.getAnyOneOfKeyValChecks() != null)
//
//                callData.getAnyOneOfKeyValChecks().forEach((qsParam, listOfValues) -> {
//
//                    Set<String> actualValues = new HashSet<>();
//
//                    callData.getQueryStringMap().forEach((qsMapFromNetworkCall) -> {
//                        if (qsMapFromNetworkCall.containsKey(qsParam)) {
//                            actualValues.add(qsMapFromNetworkCall.get(qsParam));
//                        }
//                    });
//
//                    if (!Collections.disjoint(actualValues, listOfValues)) {
//                        callData.getTestMessages().add("Any one of key value check Pass for key: " + qsParam
//                                + ". Expected one of: " + listOfValues + " Actual: " + actualValues);
//                        callData.setTestResult(TestResult.FAIL);
//                    }
//                    else {
//                    	 callData.getTestMessages().add("Any one of key value check failed for key: " + qsParam
//                                 + ". Expected one of: " + listOfValues + " Actual: " + actualValues);
//                         
//                    }
//
//                });
//
//            if (callData.getRequestHeadersChecks() != null && !callData.getRequestHeadersChecks().isEmpty()) {
//                boolean allMatched = false;
//
//                // For each map in queryStringMapRequestHeader
//                for (Map<String, Object> headerMap : callData.getQueryStringMapRequestHeader()) {
//                    allMatched = true; // Assume all key-value pairs are matched initially
//
//                    // For each key-value pair in requestHeadersChecks
//                    for (Map.Entry<String, Object> entry : callData.getRequestHeadersChecks().entrySet()) {
//                        String key = entry.getKey().toLowerCase();
//                        Object expectedValue = entry.getValue();
//                        Object actualValue = headerMap.get(key);
//
//                        // If the expected value is an ArrayList and the actual value is a String
//                        if (expectedValue instanceof ArrayList && actualValue instanceof String) {
//
//                            // Convert the actual value string into a Map
//                            Map<String, String> actualMap = Arrays.stream(((String) actualValue).split(";"))
//                                    .map(String::trim)
//                                    .map(str -> str.split("=", 2))
//                                    .collect(Collectors.toMap(arr -> arr[0], arr -> arr.length > 1 ? arr[1] : ""));
//
//                            // Iterate over the expected value list and compare with the actual map
//                            ArrayList<Map<String, String>> expectedList = (ArrayList<Map<String, String>>) expectedValue;
//                            for (Map<String, String> expectedMap : expectedList) {
//                                for (Map.Entry<String, String> expectedEntry : expectedMap.entrySet()) {
//                                    String expectedKey = expectedEntry.getKey().trim();
//                                    String expectedVal = expectedEntry.getValue().trim();
//                                    if (!actualMap.containsKey(expectedKey) || !actualMap.get(expectedKey).equals(expectedVal)) {
//                                        allMatched = false;
//                                        break;
//                                    }
//                                }
//                                if (!allMatched) {
//                                    break;
//                                }
//                            }
//                        } else if (expectedValue instanceof String && actualValue != null) {
//                            // Continue with the existing string comparison logic
//                            if (!actualValue.toString().equals(expectedValue)) {
//                                allMatched = false;
//                                break;
//                            }
//                        } else if(actualValue == null) {
//                            allMatched = false;
//                            break;
//                        }
//                    }
//
//                    if (allMatched) {
//                        // If all key-value pairs are matched for the current headerMap, break the outer loop
//                        break;
//                    }
//                }
//
//                // If after iterating over all headerMap in queryStringMapRequestHeader, no match is found, add the failure message
//                if (!allMatched) {
//                    callData.getTestMessages().add("Failed request headers verification");
//                    callData.setTestResult(TestResult.FAIL);
//                }
//            }
//
//            if (callData.getResponseHeadersChecks() != null && !callData.getResponseHeadersChecks().isEmpty()) {
//                boolean allMatched = false;
//
//                // For each map in queryStringMapRequestHeader
//                for (Map<String, Object> headerMap : callData.getQueryStringMapResponseHeader()) {
//                    allMatched = true; // Assume all key-value pairs are matched initially
//
//                    // For each key-value pair in requestHeadersChecks
//                    for (Map.Entry<String, Object> entry : callData.getResponseHeadersChecks().entrySet()) {
//                        String key = entry.getKey().toLowerCase();
//                        Object expectedValue = entry.getValue();
//                        Object actualValue = headerMap.get(key);
//
//                        // If the expected value is an ArrayList and the actual value is a String
//                        if (expectedValue instanceof ArrayList && actualValue instanceof String) {
//
//                            // Convert the actual value string into a Map
//                            Map<String, String> actualMap = Arrays.stream(((String) actualValue).split(";"))
//                                    .map(String::trim)
//                                    .map(str -> str.split("=", 2))
//                                    .collect(Collectors.toMap(arr -> arr[0], arr -> arr.length > 1 ? arr[1] : ""));
//
//                            // Iterate over the expected value list and compare with the actual map
//                            ArrayList<Map<String, String>> expectedList = (ArrayList<Map<String, String>>) expectedValue;
//                            for (Map<String, String> expectedMap : expectedList) {
//                                for (Map.Entry<String, String> expectedEntry : expectedMap.entrySet()) {
//                                    String expectedKey = expectedEntry.getKey().trim();
//                                    String expectedVal = expectedEntry.getValue().trim();
//                                    if (!actualMap.containsKey(expectedKey) || !actualMap.get(expectedKey).equals(expectedVal)) {
//                                        allMatched = false;
//                                        break;
//                                    }
//                                }
//                                if (!allMatched) {
//                                    break;
//                                }
//                            }
//                        } else if (expectedValue instanceof String && actualValue != null) {
//                            // Continue with the existing string comparison logic
//                            if (!actualValue.toString().equals(expectedValue)) {
//                                allMatched = false;
//                                break;
//                            }
//                        } else if(actualValue == null) {
//                            allMatched = false;
//                            break;
//                        }
//                    }
//
//                    if (allMatched) {
//                        // If all key-value pairs are matched for the current headerMap, break the outer loop
//                        break;
//                    }
//                }
//
//                // If after iterating over all headerMap in queryStringMapRequestHeader, no match is found, add the failure message
//                if (!allMatched) {
//                    callData.getTestMessages().add("Failed Response Headers verification");
//                    callData.setTestResult(TestResult.FAIL);
//                }
//            }
//
//        });
//
//    }

    @Getter
    @Setter
    @Builder
    @ToString
    public static class HeadersAssertion {
        String message;
        TestResult testResult;
        String url;
        // ... any other fields required
    }


}
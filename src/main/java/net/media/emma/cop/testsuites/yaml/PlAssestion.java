package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.microsoft.playwright.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.testng.internal.collections.Pair;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
public class PlAssestion {

    @Autowired
    private JsonPath jsonPath;

    public void verifyBidderDataAssertions(URLChecksReportModel urlChecksReportModel, List<Request> requestList, TestCriteriaBo testData) {
        Map<String, List<TestCriteriaBo.BidderDataAssertions>> bidderDataAssertions = testData.getBidderDataAssertions();
        Map<String, String> bidderDataUrls = testData.getBidderDataUrls();

        for (Map.Entry<String, List<TestCriteriaBo.BidderDataAssertions>> entry : bidderDataAssertions.entrySet()) {
            String bidder = entry.getKey();
            String bidderUrl = bidderDataUrls.get(bidder);
            if (bidderUrl == null) continue;

            List<Request> matchingRequests = filterMatchingRequests(requestList, bidderUrl);
            if (matchingRequests.isEmpty()) continue;

            // Collect all imp objects and their corresponding responses for matching requests
            List<Pair<Object, String>> allImpAndResponsePairs = new ArrayList<>();
            for (Request request : matchingRequests) {
                String postData = request.postData();
                String responseData = new String(request.response().body(), StandardCharsets.UTF_8);
                ReadContext readContext = com.jayway.jsonpath.JsonPath.parse(postData);
                try {
                    List<Object> impObjects = readContext.read("$.imp");
                    for (Object imp : impObjects) {
                        allImpAndResponsePairs.add(new Pair<>(imp, responseData));
                    }
                } catch (PathNotFoundException e) {
                    // Handle or log the exception
                }
            }

            List<TestCriteriaBo.BidderDataAssertions> assertionsList = entry.getValue();
            boolean setFailed = true;

            for (TestCriteriaBo.BidderDataAssertions assertion : assertionsList) {
                boolean passedForOneImpObject = false;
                for (Pair<Object, String> pair : allImpAndResponsePairs) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String impData = null;
                    try {
                        impData = objectMapper.writeValueAsString(pair.getKey());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    String correspondingResponseData = pair.getValue();

                    if (isPathPresent(assertion, impData)) {
                        if (verifyDataAssertionsRequest(assertion.getRequestData(), impData) &&
                                verifyDataAssertionsResponse(assertion.getResponseData(), correspondingResponseData)) {
                            passedForOneImpObject = true;
                            break;
                        }
                    }
                }

                if (passedForOneImpObject) {
                    setFailed = false;
                    createReport(urlChecksReportModel, bidder, bidderUrl, "All assertions passed", URLChecksReportModel.TestResult.PASS);
                    break;
                }
            }

            if (setFailed) {
                createReport(urlChecksReportModel, bidder, bidderUrl, "Failed assertions", URLChecksReportModel.TestResult.FAIL);
            }

        }
    }

    private List<Request> filterMatchingRequests(List<Request> requestList, String bidderUrl) {
        return requestList.stream().filter(request -> request.url().contains(bidderUrl)).collect(Collectors.toList());
    }

    private boolean isPathPresent(TestCriteriaBo.BidderDataAssertions assertion, String postData) {
        TestCriteriaBo.MatchingJsonPath matchingJsonPath = assertion.getMatchingJsonPath();
        String path = matchingJsonPath.getJsonPath().replaceFirst("x", Matcher.quoteReplacement("$"));
        String expectedValue = matchingJsonPath.getExpectedValue();
        try {
            Object actualValue = com.jayway.jsonpath.JsonPath.read(postData, removeJsonPathIndexForImp(path));
            if (!actualValue.equals(expectedValue)) {
                return false;
            }
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    private boolean verifyDataAssertionsRequest(List<TestCriteriaBo.RequestData> dataAssertionList, String parsedData) {
        for (TestCriteriaBo.RequestData dataAssertion : dataAssertionList) {
            String jsonPath = dataAssertion.getJsonPath().replaceFirst("x", Matcher.quoteReplacement("$"));
            String expectedValue = dataAssertion.getValue();
            try {
                Object actualValue = com.jayway.jsonpath.JsonPath.read(parsedData, removeJsonPathIndexForImp(jsonPath));
                if (!actualValue.equals(expectedValue)) {
                    return false;
                }
            } catch (PathNotFoundException e) {
                return false;
            }
        }
        return true;
    }

    private boolean verifyDataAssertionsResponse(List<TestCriteriaBo.ResponseData> dataAssertionList, String parsedData) {
        for (TestCriteriaBo.ResponseData dataAssertion : dataAssertionList) {
            String jsonPath = dataAssertion.getJsonPath().replaceFirst("x", Matcher.quoteReplacement("$"));
            String expectedValue = dataAssertion.getValue();
            try {
                Object actualValue = com.jayway.jsonpath.JsonPath.read(parsedData, removeJsonPathIndexForImp(jsonPath));
                if (!actualValue.equals(expectedValue)) {
                    return false;
                }
            } catch (PathNotFoundException e) {
                return false;
            }
        }
        return true;

    }

    private void createReport(URLChecksReportModel urlChecksReportModel, String bidder, String url, String message, URLChecksReportModel.TestResult result) {
        URLChecksReportModel.BidderDataAssertion assertionReport = URLChecksReportModel.BidderDataAssertion.builder()
                .message(message)
                .testResult(result)
                .url(url)
                .build();

        // Get the existing list or create a new one
        List<URLChecksReportModel.BidderDataAssertion> existingReports = urlChecksReportModel.getBidderDataAssertions().getOrDefault(bidder, new ArrayList<>());

        // Add the new report to the list
        existingReports.add(assertionReport);

        // Put it back into the map
        urlChecksReportModel.getBidderDataAssertions().put(bidder, existingReports);

    }

    public String removeJsonPathIndexForImp(String jsonPath) {
        return jsonPath.replaceFirst("imp\\[\\d+\\].", "");
    }

    class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}




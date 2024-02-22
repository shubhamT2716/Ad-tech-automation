package net.media.emma.cop.testsuites.yaml;

import com.microsoft.playwright.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HeadersAssertion {

    static String failedMessage = "";

    public void verifyRequestResponseHeaders(URLChecksReportModel urlChecksReportModel, List<Request> requestList, TestCriteriaBo testData) {
        Map<String, Map<String, Object>> requestHeaders = testData.getRequestHeaders();
        Map<String, Map<String, Object>> responseHeaders = testData.getResponseHeaders();
        Map<String, String> bidderDataUrls = testData.getBidderDataUrls();

        for (Map.Entry<String, Map<String, Object>> entry : requestHeaders.entrySet()) {
            boolean passed = false;
            String bidder = entry.getKey();
            String bidderUrl = bidderDataUrls.get(bidder);
            Map<String, Object> expectedRequestHeaders = entry.getValue();
            Map<String, Object> expectedResponseHeaders = responseHeaders.get(bidder);

            List<Request> matchingRequests = filterMatchingRequests(requestList, bidderUrl);
            if (matchingRequests.isEmpty()) continue;
            for (Request request : matchingRequests) {
                failedMessage = "";

                Map<String, Object> actualRequestHeaders = getRequestHeaders(request);
                Map<String, Object> actualResponseHeaders = getResponseHeaders(request);

                if (verifyHeaders(expectedRequestHeaders, actualRequestHeaders) &&
                        verifyHeaders(expectedResponseHeaders, actualResponseHeaders)) {
                    passed = true;
                    break;
                }
            }
            if (passed) {
                createReport(urlChecksReportModel, bidder, "All headers passed", URLChecksReportModel.TestResult.PASS, bidderUrl);
            } else {
                createReport(urlChecksReportModel, bidder, failedMessage, URLChecksReportModel.TestResult.FAIL, bidderUrl);
            }
        }
    }

    private List<Request> filterMatchingRequests(List<Request> requestList, String bidderUrl) {
        return requestList.stream().filter(request -> request.url().contains(bidderUrl)).collect(Collectors.toList());
    }

    private Map<String, Object> getRequestHeaders(Request request) {
        // Implement logic to extract and return request headers
        return new HashMap<String, Object>(request.allHeaders());
    }

    private Map<String, Object> getResponseHeaders(Request request) {
        // Implement logic to extract and return response headers
        return new HashMap<String, Object>(request.response().allHeaders());
    }

    private boolean verifyHeaders(Map<String, Object> expectedHeaders, Map<String, Object> actualHeaders) {

        boolean isPassed = true;
        for (Map.Entry<String, Object> entry : expectedHeaders.entrySet()) {
            String key = entry.getKey().toLowerCase();
            Object expectedValue = entry.getValue();
            Object actualValue = actualHeaders.get(key);

            if (expectedValue instanceof ArrayList && actualValue instanceof String) {

                // Convert the actual value string into a Map
                Map<String, String> actualMap = Arrays.stream(((String) actualValue).split(";"))
                        .map(String::trim)
                        .map(str -> str.split("=", 2))
                        .collect(Collectors.toMap(arr -> arr[0], arr -> arr.length > 1 ? arr[1] : ""));

                // Iterate over the expected value list and compare with the actual map
                ArrayList<Map<String, String>> expectedList = (ArrayList<Map<String, String>>) expectedValue;
                for (Map<String, String> expectedMap : expectedList) {
                    for (Map.Entry<String, String> expectedEntry : expectedMap.entrySet()) {
                        String expectedKey = expectedEntry.getKey().trim();
                        String expectedVal = expectedEntry.getValue().trim();
                        if (!actualMap.containsKey(expectedKey) || !actualMap.get(expectedKey).equals(expectedVal)) {
                            failedMessage = failedMessage + expectedKey + " Expected Value: " + expectedVal + " Actual Value: " + actualMap.get(expectedKey);
                            isPassed = false;
                            break;
                        }
                    }
                    if (!isPassed) {
                        break;
                    }
                }
            } else if (expectedValue instanceof String && actualValue != null) {
                // Continue with the existing string comparison logic
                if (!actualValue.toString().equals(expectedValue)) {
                    failedMessage = failedMessage + key + " Expected Value: " + expectedValue + " Actual Value: " + actualValue;
                    isPassed = false;
                    break;
                }
            } else if (actualValue == null) {
                failedMessage = failedMessage + key + " Expected Value: " + expectedValue + " Actual Value: " + actualValue;
                isPassed = false;
                break;
            }
        }
        return isPassed;
    }

    private void createReport(URLChecksReportModel urlChecksReportModel, String bidder, String message, URLChecksReportModel.TestResult result, String url) {
        // logic to create a report for request/response header verification
        URLChecksReportModel.HeadersAssertion assertionReport = URLChecksReportModel.HeadersAssertion.builder()
                .message(message)
                .testResult(result)
                .url(url).build();

        // Get the existing list or create a new one
        List<URLChecksReportModel.HeadersAssertion> existingReports = urlChecksReportModel.getHeaderAssertions().getOrDefault(bidder, new ArrayList<>());
        // Add the new report to the list
        existingReports.add(assertionReport);
        urlChecksReportModel.getHeaderAssertions().put(bidder, existingReports);
    }
}

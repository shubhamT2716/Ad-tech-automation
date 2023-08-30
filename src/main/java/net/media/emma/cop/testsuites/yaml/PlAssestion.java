package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.PathNotFoundException;
import com.microsoft.playwright.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.engine.ElementName;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/*@Service
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

            List<TestCriteriaBo.BidderDataAssertions> assertionsList = entry.getValue();

            for (TestCriteriaBo.BidderDataAssertions assertion : assertionsList) {
                checkAssertions(assertion, matchingRequests, urlChecksReportModel, bidder, bidderUrl);
            }
        }
    }

    private List<Request> filterMatchingRequests(List<Request> requestList, String bidderUrl) {
        return requestList.stream()
                .filter(request -> request.url().contains(bidderUrl))
                .collect(Collectors.toList());
    }

    private void checkAssertions(TestCriteriaBo.BidderDataAssertions assertion, List<Request> matchingRequests, URLChecksReportModel urlChecksReportModel, String bidder, String bidderUrl) {
        for (Request request : matchingRequests) {
            JsonNode parsedRequestBody = parseRequestBody(request);
            JsonNode parsedResponseBody = parseResponseBody(request);

            String postData = getPostData(request);
            System.out.println(" Post Data  ------ " + postData);

            if (!isPathPresent(assertion, postData)) continue;
            //   if (!isMatchingJsonNodeValid(assertion, parsedRequestBody)) continue;

            if (checkRequestDataAssertions(assertion.getRequestData(), parsedRequestBody) &&
                    checkResponseDataAssertions(assertion.getResponseData(), parsedResponseBody)) {
                createReport(urlChecksReportModel, bidder, bidderUrl, "All assertions passed", URLChecksReportModel.TestResult.PASS);
            } else {
                createReport(urlChecksReportModel, bidder, bidderUrl, "Failed assertions", URLChecksReportModel.TestResult.FAIL);
            }
        }
    }

    private JsonNode parseRequestBody(Request request) {
        return jsonPath.getJsonNodes(request.postData());
    }

    private String getPostData(Request request) {
        return request.postData();
    }

    private JsonNode parseResponseBody(Request request) {
        String responseBody = new String(request.response().body(), StandardCharsets.UTF_8);
        return jsonPath.getJsonNodes(responseBody);
    }

    private boolean isPathPresent(TestCriteriaBo.BidderDataAssertions assertion, String postData) {
        try {
            TestCriteriaBo.MatchingJsonPath matchingJsonPath = assertion.getMatchingJsonPath();
            String path = matchingJsonPath.getJsonPath();
            String pathUpdated = path.replaceFirst("x", Matcher.quoteReplacement("$"));
            Object result = com.jayway.jsonpath.JsonPath.read(postData, pathUpdated);
            System.out.println("Result  --- " + result);
            return true;
        } catch (PathNotFoundException e) {
            return false;
        }
    }

    private boolean checkRequestDataAssertions(List<TestCriteriaBo.RequestData> requestDataList, JsonNode parsedRequestBody) {
        String postData = parsedRequestBody.toString(); // Assuming parsedRequestBody can be converted to a JSON-formatted String
        for (TestCriteriaBo.RequestData requestData : requestDataList) {
            String jsonPath = requestData.getJsonPath();
            String expectedValue = requestData.getValue();
            try {
                String pathUpdated = jsonPath.replaceFirst("x", Matcher.quoteReplacement("$"));
                Object actualValue = com.jayway.jsonpath.JsonPath.read(postData, pathUpdated);
                if (!actualValue.equals(expectedValue)) {
                    return false;
                }
            } catch (PathNotFoundException e) {
                return false;
            }
        }
        return true;
    }

    private boolean checkResponseDataAssertions(List<TestCriteriaBo.ResponseData> responseDataList, JsonNode parsedResponseBody) {
        String responseBody = parsedResponseBody.toString(); // Assuming parsedResponseBody can be converted to a JSON-formatted String
        for (TestCriteriaBo.ResponseData responseData : responseDataList) {
            String jsonPath = responseData.getJsonPath();
            String expectedValue = responseData.getValue();
            try {
                String pathUpdated = jsonPath.replaceFirst("x", Matcher.quoteReplacement("$"));
                Object actualValue = com.jayway.jsonpath.JsonPath.read(responseBody, pathUpdated);
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
        urlChecksReportModel.getBidderDataAssertions().put(bidder, assertionReport);
    }
}*/

import java.util.Collections;

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

            List<TestCriteriaBo.BidderDataAssertions> assertionsList = entry.getValue();
            boolean setFailed = true;

            for (TestCriteriaBo.BidderDataAssertions assertion : assertionsList) {

                boolean passedForOneRequest = false;
                for (Request request : matchingRequests) {

                    String postData = request.postData();
                    String responseData = new String(request.response().body(), StandardCharsets.UTF_8);

                    if (isPathPresent(assertion, postData))
                    {
                        System.out.println("Path present ----- ");
                        if (verifyDataAssertionsRequest(assertion.getRequestData(), postData) &&
                                verifyDataAssertionsResponse(assertion.getResponseData(), responseData)) {
                            System.out.println("Passed ----- ");
                            passedForOneRequest = true;
                            break;
                        }
                    }
                }

                if (passedForOneRequest) {
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
            Object actualValue = com.jayway.jsonpath.JsonPath.read(postData, path);
            if (!actualValue.equals(expectedValue)) {
                return false;
            }
        } catch (PathNotFoundException e) {
            return false;
        }
        return true;
    }

    private boolean verifyDataAssertionsRequest(List<TestCriteriaBo.RequestData> dataAssertionList, String parsedData) {
        for (TestCriteriaBo.RequestData dataAssertion : dataAssertionList) {
            System.out.println(dataAssertion.getJsonPath().replaceFirst("x", Matcher.quoteReplacement("$")));
            String jsonPath = dataAssertion.getJsonPath().replaceFirst("x", Matcher.quoteReplacement("$"));
            String expectedValue = dataAssertion.getValue();
            try {
                Object actualValue = com.jayway.jsonpath.JsonPath.read(parsedData, jsonPath);
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
                Object actualValue = com.jayway.jsonpath.JsonPath.read(parsedData, jsonPath);
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
}



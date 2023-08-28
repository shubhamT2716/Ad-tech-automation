    package net.media.emma.cop.testsuites.yaml;

    import com.fasterxml.jackson.core.JsonPointer;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.microsoft.playwright.Request;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.nio.charset.StandardCharsets;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    /*@Service
    public class PlAssestion {

        @Autowired
        JsonPath jsonPath;

        public void verifyBidderDataAssertions(URLChecksReportModel urlChecksReportModel,List<Request> requestList, TestCriteriaBo testData) {
            Map<String, List<TestCriteriaBo.BidderDataAssertions>> bidderDataAssertions = testData.getBidderDataAssertions();
            Map<String, String> bidderDataUrls = testData.getBidderDataUrls();

            for (Map.Entry<String, List<TestCriteriaBo.BidderDataAssertions>> entry : bidderDataAssertions.entrySet()) {
                String bidder = entry.getKey();
                List<TestCriteriaBo.BidderDataAssertions> assertionsList = entry.getValue();

                String bidderUrl = bidderDataUrls.get(bidder);
                if (bidderUrl == null) {
                    continue; // Skip if bidder URL is not present
                }

                List<Request> requestListCopy = new ArrayList<>(requestList);

                for (Request request : requestListCopy) {
                    if (!request.url().equals(bidderUrl)) {
                        continue; // Skip if URL doesn't match
                    }

                    String requestBody = request.postData();
                    JsonNode parsedRequestBody = jsonPath.getJsonNodes(requestBody);
                    //     JsonNode impArray = parsedRequestBody.at("/imp");
                    String responseBody = new String(request.response().body(), StandardCharsets.UTF_8);
                    JsonNode parsedResponseBody = jsonPath.getJsonNodes(responseBody);

                    //   if (impArray.isArray()) {
                    //       for (int i = 0; i < impArray.size(); i++) {

                    for (TestCriteriaBo.BidderDataAssertions assertion : assertionsList) {
                        TestCriteriaBo.MatchingJsonPath matchingJsonPath = assertion.getMatchingJsonPath();
                        String matchingJsonPathValue = matchingJsonPath.getJsonPath();
                        //  String dynamicMatchingJsonPathValue = matchingJsonPathValue.replace("/imp", "/imp/" + i); // dynamically adjust the JSON path
                        String matchingJsonPathExpectedValue = matchingJsonPath.getExpectedValue();

                        JsonNode matchingJsonNode = parsedRequestBody.at(JsonPointer.valueOf(jsonPath.toJsonPointer(matchingJsonPathValue)));
                        if (jsonPath.verifyJsonNodeValue(matchingJsonNode, matchingJsonPathExpectedValue)) {
                            List<TestCriteriaBo.RequestData> requestDataAssertions = assertion.getRequestData();
                            List<TestCriteriaBo.ResponseData> responseDataAssertions = assertion.getResponseData();

                            boolean allRequestDataMatched = true;
                            boolean allResponseDataMatched = true;
                            for (TestCriteriaBo.RequestData requestData : requestDataAssertions) {
                                String requestDataJsonPath = requestData.getJsonPath();
                                //           String dynamicRequestDataJsonPath = requestDataJsonPath.replace("/imp", "/imp/" + i);
                                String requestDataExpectedValue = requestData.getValue();

                                JsonNode requestDataNode = parsedRequestBody.at(JsonPointer.valueOf(jsonPath.toJsonPointer(requestDataJsonPath)));

                                if (!jsonPath.verifyJsonNodeValue(requestDataNode, requestDataExpectedValue)) {
                                    System.out.println("Not  Done");
                                    allRequestDataMatched = false;
                                    break; // Break if a request data assertion fails
                                }
                            }

                            if (allRequestDataMatched) {
                                for (TestCriteriaBo.ResponseData responseData : responseDataAssertions) {
                                    String responseDataJsonPath = responseData.getJsonPath();
                                    String responseDataExpectedValue = responseData.getValue();

                                    JsonNode responseDataNode = parsedResponseBody.at(JsonPointer.valueOf(responseDataJsonPath));

                                    if (!jsonPath.verifyJsonNodeValue(responseDataNode, responseDataExpectedValue)) {
                                        System.out.println("Not  Done");
                                        allResponseDataMatched = false;
                                        break;
                                    }
                                }
                            }


                            if (allRequestDataMatched && allResponseDataMatched) {
                                System.out.println("Done Done");
                                URLChecksReportModel.BidderDataAssertion assertionReport = URLChecksReportModel.BidderDataAssertion.builder()
                                        .message("All assertions passed")
                                        .testResult(URLChecksReportModel.TestResult.PASS)
                                        .url(bidderUrl)
                                        .matchingJson(matchingJsonPathValue)
                                        .build();
                                urlChecksReportModel.getBidderDataAssertions().put(bidder, assertionReport);

                            }
                            else {
                                StringBuilder failedMessage = new StringBuilder("Failed assertions: ");
                                if(!allRequestDataMatched) {
                                    failedMessage.append("Request Data Assertion Failed. ");
                                }
                                if(!allResponseDataMatched) {
                                    failedMessage.append("Response Data Assertion Failed. ");
                                }

                                URLChecksReportModel.BidderDataAssertion assertionReport = URLChecksReportModel.BidderDataAssertion.builder()
                                        .message(failedMessage.toString())
                                        .testResult(URLChecksReportModel.TestResult.FAIL)
                                        .url(bidderUrl)
                                        .matchingJson(matchingJsonPathValue)
                                        .build();

                                urlChecksReportModel.getBidderDataAssertions().put(bidder, assertionReport);

                            }
                        }
                    }
                }
            }
            //     }
            //   }

        }
    }*/

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
                for (TestCriteriaBo.BidderDataAssertions assertion : assertionsList) {
                    checkAssertions(assertion, matchingRequests, urlChecksReportModel, bidder, bidderUrl);
                }
            }
        }

        private List<Request> filterMatchingRequests(List<Request> requestList, String bidderUrl) {
            return requestList.stream()
                    .filter(request -> request.url().equals(bidderUrl))
                    .collect(Collectors.toList());
        }

        private void checkAssertions(TestCriteriaBo.BidderDataAssertions assertion, List<Request> matchingRequests, URLChecksReportModel urlChecksReportModel, String bidder, String bidderUrl) {
            for (Request request : matchingRequests) {
                JsonNode parsedRequestBody = parseRequestBody(request);
                JsonNode parsedResponseBody = parseResponseBody(request);

                if (!isMatchingJsonNodeValid(assertion, parsedRequestBody)) continue;

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

        private JsonNode parseResponseBody(Request request) {
            String responseBody = new String(request.response().body(), StandardCharsets.UTF_8);
            return jsonPath.getJsonNodes(responseBody);
        }

        private boolean isMatchingJsonNodeValid(TestCriteriaBo.BidderDataAssertions assertion, JsonNode parsedRequestBody) {
            TestCriteriaBo.MatchingJsonPath matchingJsonPath = assertion.getMatchingJsonPath();
            String matchingJsonPathValue = matchingJsonPath.getJsonPath();
            String matchingJsonPathExpectedValue = matchingJsonPath.getExpectedValue();
            JsonNode matchingJsonNode = parsedRequestBody.at(JsonPointer.valueOf(jsonPath.toJsonPointer(matchingJsonPathValue)));
            return jsonPath.verifyJsonNodeValue(matchingJsonNode, matchingJsonPathExpectedValue);
        }

        private boolean checkRequestDataAssertions(List<TestCriteriaBo.RequestData> requestDataList, JsonNode parsedRequestBody) {
            for (TestCriteriaBo.RequestData requestData : requestDataList) {
                if (!verifyDataAssertion(requestData.getJsonPath(), requestData.getValue(), parsedRequestBody)) {
                    return false;
                }
            }
            return true;
        }

        private boolean checkResponseDataAssertions(List<TestCriteriaBo.ResponseData> responseDataList, JsonNode parsedResponseBody) {
            for (TestCriteriaBo.ResponseData responseData : responseDataList) {
                if (!verifyDataAssertion(responseData.getJsonPath(), responseData.getValue(), parsedResponseBody)) {
                    return false;
                }
            }
            return true;
        }

        private boolean verifyDataAssertion(String jsonPathString, String expectedValue, JsonNode parsedJson) {
            JsonNode node = parsedJson.at(JsonPointer.valueOf(jsonPath.toJsonPointer(jsonPathString)));
            return jsonPath.verifyJsonNodeValue(node, expectedValue);
        }

        private void createReport(URLChecksReportModel urlChecksReportModel, String bidder, String url, String message, URLChecksReportModel.TestResult result) {
            URLChecksReportModel.BidderDataAssertion assertionReport = URLChecksReportModel.BidderDataAssertion.builder()
                    .message(message)
                    .testResult(result)
                    .url(url)
                    .build();
            urlChecksReportModel.getBidderDataAssertions().put(bidder, assertionReport);
        }
    }


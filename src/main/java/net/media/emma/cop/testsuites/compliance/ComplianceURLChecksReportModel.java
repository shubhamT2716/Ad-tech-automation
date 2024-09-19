package net.media.emma.cop.testsuites.compliance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.microsoft.playwright.options.Cookie;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


import java.util.*;

@Getter @Setter @Builder
public class ComplianceURLChecksReportModel {

	@Builder.Default
	Map<String, PositiveURLChecks> positiveUrlChecks = new HashMap<String, PositiveURLChecks>();
	@Builder.Default
	Map<String, NegativeURLChecks> negativeUrlChecks = new HashMap<String, NegativeURLChecks>();
//	@Builder.Default
//	List<HashMap<String, String>> cookiesCheck = new ArrayList<>();


	enum TestResult {
		PASS, FAIL, NA
	}

	@Getter @Setter @Builder
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
		List<Map<String, String>> queryStringParam=new ArrayList<>();

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

	public void updateMatchingUrlsCount() {
		positiveUrlChecks.forEach((key, value) -> {
			value.setCountOfMatchingUrls(value.getMatchingUrls().size());
		});
		negativeUrlChecks.forEach((key, value) -> {
			value.setCountOfMatchingUrls(value.getMatchingUrls().size());
		});
	}

	public void updateNetworkCallErrors(ComplianceTestCriteria testData) {

		testData.getNetworkCallsPositive().forEach((key, value) -> {
			if (!positiveUrlChecks.containsKey(key)) {
				positiveUrlChecks.put(key, PositiveURLChecks.builder().label(key).testResult(TestResult.FAIL)
						.testMessages(Arrays.asList("Call not fired")).urlPattern(value).build());
			}
		});

		/*testData.getNetworkCallsNegative().forEach((key, value) -> {
			if (!negativeUrlChecks.containsKey(key)) {
				negativeUrlChecks.put(key, NegativeURLChecks.builder().label(key).urlPattern(value).build());
			} else {
				negativeUrlChecks.get(key).setTestResult(TestResult.FAIL);
			}
		});*/
	}

	public void verifyPositiveKeyValChecks() {

		 positiveUrlChecks.forEach((callLabel, callData) -> {  // key, value --> label, PositiveURLChecks obj

	            if (callData.getPositiveKeyValChecks() != null) {
	                callData.getPositiveKeyValChecks().forEach((qsParam, listOfValues) -> {
	                    Set<String> actualValues = new HashSet<>();
	                    
	                    callData.getQueryStringParam().forEach(qsMapFromNetworkCall -> {
	                        if (qsMapFromNetworkCall.containsKey(qsParam)) {
	                            actualValues.add(qsMapFromNetworkCall.get(qsParam));
	                        }
	                    });
	                    
	                    
	                    if (!actualValues.equals(listOfValues)) {
//	                        callData.getTestMessages().add("Positive key value check Pass for key: " + qsParam
//	                                + ". Expected: " + listOfValues + " Actual: " + actualValues);
//	                    } else {
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

	                    callData.getQueryStringParam().forEach(qsMapFromNetworkCall -> {
	                        if (qsMapFromNetworkCall.containsKey(qsParam)) {
	                            actualValues.add(qsMapFromNetworkCall.get(qsParam));
	                        }
	                    });

	                    if (actualValues.equals(listOfValues)) {
	                        callData.getTestMessages().add("Negative key value check Pass for key: " + qsParam
	                                + ". Found negative matches: " + actualValues);
	                    } else {
	                        callData.getTestMessages().add("Negative key value check failed for key: " + qsParam
	                                + ". Found negative matches: " + actualValues);
	                        callData.setTestResult(TestResult.FAIL);
	                    }
	                });
	            }

	            // Any One Of Key Value Checks
	            if (callData.getAnyOneOfKeyValChecks() != null) {
	                callData.getAnyOneOfKeyValChecks().forEach((qsParam, listOfValues) -> {
	                    Set<String> actualValues = new HashSet<>();

	                    callData.getQueryStringParam().forEach(qsMapFromNetworkCall -> {
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

	            

	        });

	}
	public void verifyCookies(List<Cookie> allCookies, List<Map<String, String>> cookiesToVerify) {
		List<HashMap<String, String>> cookiesCheckTotal = new ArrayList<>();

		for (Map<String, String> cookieToVerify : cookiesToVerify) {
			HashMap<String, String> cookieCheckIndividual = new HashMap<>();
			Set<String> allcookieName = cookieToVerify.keySet();

			for (String cookieName : allcookieName) {
				boolean foundCookie = false;
				String expectedValue = cookieToVerify.get(cookieName);

				for (Cookie cookie : allCookies) {
					if (cookie.name.equals(cookieName)) {
						foundCookie = true;

						if ("notnull".equalsIgnoreCase(expectedValue)) {
							cookieCheckIndividual.put(cookieName, "VERIFIED");
						} else {
							if (cookie.value != null && !cookie.value.isEmpty() && cookie.value.equals(expectedValue)) {
								cookieCheckIndividual.put(cookieName, "VERIFIED");
							} else {
								cookieCheckIndividual.put(cookieName, "VALUE MISMATCH");
								cookieCheckIndividual.put(cookieName + "_ExpectedValue", expectedValue);
								cookieCheckIndividual.put(cookieName + "_ActualValue", (cookie.value != null) ? cookie.value : "null");
							}
						}

						break;
					}
				}

				if (!foundCookie) {
					cookieCheckIndividual.put(cookieName, "ABSENT");
				}
			}

			cookiesCheckTotal.add(cookieCheckIndividual);
		}
//		setCookiesCheck(cookiesCheckTotal);
	}
}
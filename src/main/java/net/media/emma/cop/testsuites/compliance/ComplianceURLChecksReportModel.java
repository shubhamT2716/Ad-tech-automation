package net.media.emma.cop.testsuites.compliance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.microsoft.playwright.options.Cookie;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo;
import org.springframework.util.Assert;

import java.util.*;

@Getter @Setter @Builder
public class ComplianceURLChecksReportModel {

	@Builder.Default
	Map<String, PositiveURLChecks> positiveUrlChecks = new HashMap<String, PositiveURLChecks>();
	@Builder.Default
	Map<String, NegativeURLChecks> negativeUrlChecks = new HashMap<String, NegativeURLChecks>();
	@Builder.Default
	List<HashMap<String,String>> cookiesCheck = new ArrayList<>();


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
		@JsonIgnore
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

		positiveUrlChecks.forEach((callLabel, callData) -> { // key, value --> lable, PositiveURLChecks obj

			if (callData.getPositiveKeyValChecks() != null)

				callData.getPositiveKeyValChecks().forEach((qsParam, listOfValues) -> {

					Set<String> actualValues = new HashSet<>();

					callData.getQueryStringMap().forEach((qsMapFromNetworkCall) -> {
						if (qsMapFromNetworkCall.containsKey(qsParam)) {
							actualValues.add(qsMapFromNetworkCall.get(qsParam));
						}
					});

					if (!actualValues.equals(listOfValues)) {
						callData.getTestMessages().add("Positive key value check failed for key: " + qsParam
								+ ". Expected: " + listOfValues + " Actual: " + actualValues);
						callData.setTestResult(TestResult.FAIL);
					}

				});

			if (callData.getNegativeKeyValChecks() != null)

				callData.getNegativeKeyValChecks().forEach((qsParam, listOfValues) -> {

					Set<String> actualValues = new HashSet<>();

					callData.getQueryStringMap().forEach((qsMapFromNetworkCall) -> {
						if (qsMapFromNetworkCall.containsKey(qsParam)) {
							actualValues.add(qsMapFromNetworkCall.get(qsParam));
						}
					});

					if (actualValues.retainAll(listOfValues)) {
						callData.getTestMessages().add("Negative key value check failed for key: " + qsParam
								+ ". Found negative matches: " + actualValues);
						callData.setTestResult(TestResult.FAIL);
					}

				});

			if (callData.getAnyOneOfKeyValChecks() != null)

				callData.getAnyOneOfKeyValChecks().forEach((qsParam, listOfValues) -> {

					Set<String> actualValues = new HashSet<>();

					callData.getQueryStringMap().forEach((qsMapFromNetworkCall) -> {
						if (qsMapFromNetworkCall.containsKey(qsParam)) {
							actualValues.add(qsMapFromNetworkCall.get(qsParam));
						}
					});

					if (actualValues.retainAll(listOfValues) && actualValues.isEmpty()) {
						callData.getTestMessages().add("Any one of key value check failed for key: " + qsParam
								+ ". Expected one of: " + listOfValues + " Actual: " + actualValues);
						callData.setTestResult(TestResult.FAIL);
					}

				});
		});

	}

	public void verifyCookies(List<Cookie> allCookies, List<Map<String, String>> cookiesToVerify)
	{
		List<HashMap<String, String>> cookiesCheckTotal = new ArrayList<>();
		HashMap<String,String> cookieCheckIndividual = new HashMap<>();
		for (Map<String, String> cookieToVerify : cookiesToVerify)
		{
			Set<String> allcookieName = cookieToVerify.keySet();
			for (String cookieName : allcookieName)
			{
				for (Cookie cookie : allCookies)
				{
					if (cookie.name.equals(cookieName))
					{
						Assert.notNull(cookie.value, "cookie value should not be null");
						cookieCheckIndividual.put(cookieName,"PRESENT");
						cookiesCheckTotal.add(cookieCheckIndividual);
						System.out.println("cookie present: " + cookieName); //TODO: Add FAILURE to report/result class
						break;
					}
					else
					{
						cookieCheckIndividual.put(cookieName,"ABSENT");
						cookiesCheckTotal.add(cookieCheckIndividual);
						System.out.println("cookie absent: " + cookieName); //TODO: Add FAILURE to report/result class
					}
					setCookiesCheck(cookiesCheckTotal);
				}
			}
		}
	}
}
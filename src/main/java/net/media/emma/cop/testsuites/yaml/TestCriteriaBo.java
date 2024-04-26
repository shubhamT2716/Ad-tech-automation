package net.media.emma.cop.testsuites.yaml;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCriteriaBo {

	String loginUrl;
	String userName;
	String password;

	List<Map<String, String>> cookies;
	String userAgent;
	String publisherUrl;
	String environment;
	String bidexAppendString;
	String location;
	Map<String, String> bidexReplaceString;
	String pbjsEvents;
	

	Map<String, String> networkCallsPositive;
	Map<String, String> networkCallsNegative;

	Map<String, Map<String, Set<String>>> keysToVerifyPositive;
	Map<String, Map<String, Set<String>>> keysToVerifyNegative;
	Map<String, Map<String, Set<String>>> keysToVerifyAnyOneOf;

    Map<String, Map<String, Object>> requestHeaders;  // Use a map with Object type
    Map<String, Map<String, Object>> responseHeaders; // Use a map with Object type

    Map<String, String> bidderDataUrls;
    Map<String, List<BidderDataAssertions>> bidderDataAssertions;
    
	Map<String, String> featureCallsPositive;
	Map<String, Map<String, String>> featureKeysPositive;
	Map<String, Map<String, String>> featureKeysNegative;
    
   

    List<ResponseSimulateParams> responseSimulate;
    List<DomInjectParams> domInject;

	@Getter
	@Setter
	public static class ResponseSimulateParams {
		String urlPattern;
		String jsonPath;
		String overrideValue;
	}

    @Getter @Setter
    public static class DomInjectParams{
        String domSelector;
        String content;
        Boolean append;
    }

    @Getter @Setter
    public static class BidderDataAssertions {
        MatchingJsonPath matchingJsonPath;
        List<RequestData> requestData;
        List<ResponseData> responseData;
    }

    @Getter @Setter
    public static class MatchingJsonPath {
        String jsonPath;
        String expectedValue;
    }

    @Getter @Setter
    public static class RequestData {
        String jsonPath;
        String value;
    }

    @Getter @Setter
    public static class ResponseData {
        String jsonPath;
        String value;
    }
}
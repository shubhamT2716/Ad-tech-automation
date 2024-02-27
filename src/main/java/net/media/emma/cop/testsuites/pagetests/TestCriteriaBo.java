package net.media.emma.cop.testsuites.pagetests;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo.BidderDataAssertions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCriteriaBo {

	String environment;
	String userAgent;
	String publisherUrl;
	List<Map<String, String>> coockies;

	Map<String, String> networkCallsPositive;
	Map<String, String> networkCallsNegative;

	Map<String, Map<String, Set<String>>> keysToVerifyPositive;
	Map<String, Map<String, Set<String>>> keysToVerifyNegative;
	Map<String, Map<String, Set<String>>> keysToVerifyAnyOneOf;

	Map<String, String> featureCallsPositive;
	Map<String, Map<String, Map<String, String>>> featureKeysPositive;
	Map<String, Map<String, Map<String, String>>> featureKeysNegative;

	Map<String, String> bidderDataUrls;
	Map<String, List<BidderDataAssertions>> bidderDataAssertions;

//    public class FeatureKeysToVerify{
//    	
//    	Map<String , Map<String, Object>> featureKeysToVerifyPositive;
//    	
//    }

}

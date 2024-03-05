package net.media.emma.cop.testsuites.pagetests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureReport {

	

	
//	private Map<String, FeatureChecks> featureChecks;
	
	private Map<String, Map<String, FeatureChecks>> featureChecks;
	
//	private Map<String, NegativeFeatureChecks> negativeFeatureChecks;
	
	
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class FeatureChecks{
		
		
		
		@Builder.Default
		TestStatus testResult = TestStatus.PASS;

//		@Builder.Default
//		Set<String> testMessages =new HashSet<>();
		
		@Builder.Default
		Set<String> testMessages=new HashSet<>();
		
//		@Builder.Default
//		List<String> matchingUrls = new ArrayList<>();
		
		
		Map<String, String> featureValuesChecks=new HashMap<>();
		
		
	}
	
	
	

	
	
	public  enum TestStatus {
		PASS, FAIL
	}
	
	
}

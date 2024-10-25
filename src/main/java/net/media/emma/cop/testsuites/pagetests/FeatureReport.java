package net.media.emma.cop.testsuites.pagetests;

import lombok.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureReport {


//	private Map<String, FeatureChecks> featureChecks;

    private Map<String, Map<String, FeatureChecks>> featureChecks;

//	private Map<String, NegativeFeatureChecks> negativeFeatureChecks;


    public enum TestStatus {
        PASS, FAIL
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeatureChecks {


        @Builder.Default
        TestStatus testResult = TestStatus.PASS;

//		@Builder.Default
//		Set<String> testMessages =new HashSet<>();

        @Builder.Default
        Set<String> testMessages = new HashSet<>();

//		@Builder.Default
//		List<String> matchingUrls = new ArrayList<>();


        Map<String, Set<String>> featureValuesChecks = new HashMap<>();


    }


}

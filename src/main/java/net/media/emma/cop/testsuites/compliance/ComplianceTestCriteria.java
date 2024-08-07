package net.media.emma.cop.testsuites.compliance;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter @Setter
public class ComplianceTestCriteria {

    String location;
    String publisherUrl;
    String environment;
    String userAgent;
    List<Integer> consents;
    List<Map<String, String>> cookies;
    List<Map<String, String>> cookiesToVerify;
    Map<String, String> networkCallsPositive;
    Map<String, String> networkCallsNegative;

    Map<String, Map<String, Set<String>>> keysToVerifyPositive;
    Map<String, Map<String, Set<String>>> keysToVerifyNegative;
    Map<String, Map<String, Set<String>>> keysToVerifyAnyOneOf;
    
}

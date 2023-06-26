package net.media.emma.cop.testsuites.compliance;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter @Setter
public class ComplianceTestCriteria {

    String location;
    String publisherUrl;
    String environment;
    String userAgent;
    List<Map<String, String>> cookies;
    
}

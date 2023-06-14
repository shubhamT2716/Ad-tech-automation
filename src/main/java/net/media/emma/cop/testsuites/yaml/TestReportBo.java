package net.media.emma.cop.testsuites.yaml;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @Builder @ToString
public class TestReportBo {

    TestData testMetaData;
    BidExController bidExController;
    @Builder.Default
    List<UrlChecks> urlChecks = new java.util.ArrayList<UrlChecks>();
    NetworkAnalytics networkAnalytics;

    @Getter @Setter @Builder @ToString
    public static class TestData{
        String publisherUrl;
        String environment;
        String userAgent;
        Date testTime;
        TestStatus testStatus;
    }

    @Getter @Setter @Builder @ToString
    public static class BidExController{
        TestStatus testStatus;
        UrlData urlData;
        String jsErrors;
    }

    @Getter @Setter @Builder @ToString
    public static class UrlChecks{

        String urlLabel; //APLOng, pr
        CheckType urlCheckType;
        TestStatus testStatus;
        UrlData urlData;
        List<Map<String, List<String>>> keysToVerifyPositive;
        List<Map<String, List<String>>> keysToVerifyNegative;
        List<Map<String, List<String>>> keysToVerifyAnyOneOf;
    }

    @Getter @Setter @Builder @ToString
    public static class NetworkAnalytics{
        // Logging Calls - Analytics:
        // Total Logging pixels fired (for the host):
        // Total Size of the Logging calls:
        // Duplicate Data (%) :
        
        String urlPattern;
        String size;
        String timeTakenToLoad;
    }

    public static enum TestStatus {
        PASS,
        FAIL,
        OTHER
    }

    public static enum CheckType {
        POSSITIVE,
        NEGERIVE,
        OPTIONAL
    }

    @Getter @Setter @Builder @ToString
    public static class UrlData{
        String urlPattern;
        @Builder.Default
        List<String> matchingUrls = new java.util.ArrayList<String>();
        String size;
        String timeTakenToLoad;
        String statusCode;
    }
}
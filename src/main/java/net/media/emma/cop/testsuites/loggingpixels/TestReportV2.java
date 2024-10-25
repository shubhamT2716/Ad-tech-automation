package net.media.emma.cop.testsuites.loggingpixels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class TestReportV2 {

    TestMetaData testMetaData;

    @Builder.Default
    Map<String, List<KeyData>> networkCalls = new java.util.HashMap<>();

    @Getter
    @Setter
    @Builder
    public static class KeyData {
        @Builder.Default
        List<String> messages = new ArrayList<>();
        private String key;
        private String testResult;
    }

    @Getter
    @Setter
    @Builder
    public static class TestMetaData {
        String testName;
        String dateTime;
        String publisherUrl;
        String environment;
        String userAgent;
        String harFileDownloadLink;
    }
}
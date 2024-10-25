package net.media.emma.cop.testsuites.yaml;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConsoleErrorURLChecksReportModel {

    @Builder.Default
    String consoleErrorMessage = "";

    @Builder.Default
    String bidExchangeUrl = "";

    @Builder.Default
    String responseSize = "";

    @Builder.Default
    String responseTime = "";

    enum TestResult {
        PASS, FAIL, NA
    }
}
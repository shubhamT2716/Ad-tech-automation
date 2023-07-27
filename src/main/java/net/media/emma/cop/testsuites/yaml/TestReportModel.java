package net.media.emma.cop.testsuites.yaml;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TestReportModel {

	TestMetaData testMetaData;
	BidExController bidExController;
	URLChecksReportModel urlChecks;
	NetworkAnalytics networkAnalytics;

	@Getter
	@Setter
	@Builder
	public static class TestMetaData {
		String testName;
		TestStatus testStatus;
		String dateTime;
		String publisherUrl;
		String environment;
		String userAgent;
		String harFileDownloadLink;
	}

	@Getter
	@Setter
	@Builder
	public static class BidExController {
		@Builder.Default
		List<String> matchingUrls = new ArrayList<>();
		String totalSizeDownloaded;
		String totalResponseTime;
		@Builder.Default
		List<String> consoleMessages = new ArrayList<>();
		// List of checks neededd
		// Non 200 response
		// js errors arising from this controller

	}

	@Getter
	@Setter
	@Builder
	public static class NetworkAnalytics {
		// Logging Calls - Analytics:
		// Total Logging pixels fired (for the host):
		// Total Size of the Logging calls:
		// Duplicate Data (%) :

		String urlPattern;
		String size;
		String timeTakenToLoad;
	}

	public static enum TestStatus {
		PASS, FAIL, OTHER
	}

	public static enum CheckType {
		POSSITIVE, NEGERIVE, OPTIONAL
	}
}
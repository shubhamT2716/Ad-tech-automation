package net.media.emma.cop.testsuites.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TestReportModel {

	TestMetaData testMetaData;
	BidExController bidExController;
	Map<String, String> testSummery;
	URLChecksReportModel urlChecks;
	NetworkAnalytics networkAnalytics;
	Map<String, Map<String, Map<String,String>>> cookie;
	JsonNode pbjs;
	JsonNode pbjsEvents;

	@Getter
	@Setter
	@Builder
	@ToString
	public static class TestMetaData {
		String testName;
		TestStatus testStatus;
		String dateTime;
		String publisherUrl;
		String environment;
		String userAgent;
		String harFileDownloadLink;
		String downloadReport;		
	}

	@Getter
	@Setter
	@Builder
	@ToString
	public static class BidExController {
		@Builder.Default
		List<String> matchingUrls = new ArrayList<>();
		String totalSizeDownloaded;
		String totalResponseTime;
		@Builder.Default
		List<String> consoleMessages = new ArrayList<>();
		BidexData bidexData;
		
		
		
		// List of checks neededd
		// Non 200 response
		// js errors arising from this controller

	}
	
	@Getter
	@Setter
	@Builder
	@ToString
	public static class BidexData{
		
		String svr;
		String sugd;
		String cid;
		String itype;
		String ct;
		
	}

	
	@Getter
	@Setter
	@Builder
	@ToString
	public static class BrowserCookie{
		
		Map<String, Map<String, String>> cookie;
		
	}
	
	@Getter
	@Setter
	@Builder
	@ToString
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
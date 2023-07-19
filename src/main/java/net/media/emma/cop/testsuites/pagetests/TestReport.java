package net.media.emma.cop.testsuites.pagetests;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
public class TestReport {

	TestMetaData testMetaData;
	Map<String, List<TestData>> testResult;

	@Getter
	@Setter
	@Builder
	public static class TestMetaData {

		private String testName;
		private String publisherUrl;
		private String environment;
		private String userAgent;
		private String harFileDownloadLink;

	}

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TestData {

		TestStatus testStatus;
		Map<String, String> bidder;

	}

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TempClass {
		TestStatus status;
		String message;

	}

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SlotData {
		String adUnitPath;
	}

//	@Setter
//	@Getter
//	@Builder
//	@NoArgsConstructor
//	@AllArgsConstructor
//	public static class TestResultReport {
//		// List<Map<String, List<Slots>>> Slots;
//
//		Map<String , List<TestData>> ;
//
//	}

	public static enum TestStatus {
		PASS, FAIL
	}

}

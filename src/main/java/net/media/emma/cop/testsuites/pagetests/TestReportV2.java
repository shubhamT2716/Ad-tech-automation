package net.media.emma.cop.testsuites.pagetests;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class TestReportV2 {

	TestMetaData testMetaData;
	@Builder.Default
	List<Slot> slots = new ArrayList<>();


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

	@Getter @Setter @Builder
	public static class Slot{
		String testStatus;
		String slotName; //this will be the slot-name/adunit path that we get from eval-js
		@Builder.Default
		List<Bidder> bidders = new ArrayList<>();
	}

	@Getter @Setter @Builder
	public static class Bidder{

		//this will be the pattern of the bidder url that we use this test case
		String bidderName; 

		 //push message when the test case fails. Make sure message has complete informatin as to why the test case has failed.
		@Builder.Default
		 List<String> testMessages = new ArrayList<>();

	}

}

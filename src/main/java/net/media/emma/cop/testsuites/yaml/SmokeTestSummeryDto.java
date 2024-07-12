package net.media.emma.cop.testsuites.yaml;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.media.emma.cop.testsuites.yaml.TestReportModel.BidexData;

@Component
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmokeTestSummeryDto {

//	private String rawDataLink;

	private List<Testdata> testdatas;
	
	

	@Setter
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public class Testdata {

		private String environment;

		private String userAgent;

		private String publisherUrl;

		private String harFileDownloadLink;

		private String dateTime;

		private String bidExFileSize;

		private String bidExTime;

		private List<String> message;

		private List<String> matchingUrls;
		
		private BidexData bidexData;
		
		private Map<String, Map<String, String>> testResultSummery;
		
		
	}

}

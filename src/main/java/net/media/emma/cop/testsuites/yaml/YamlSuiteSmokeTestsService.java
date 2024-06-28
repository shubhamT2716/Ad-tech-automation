package net.media.emma.cop.testsuites.yaml;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface YamlSuiteSmokeTestsService {

	public SmokeTestRawDataDto getAutoTest(String environment, String userAgent, List<String> selectedFiles,
			List<String> commonFiles, String pubs, String itype,String email,boolean flagForMail);
	
}

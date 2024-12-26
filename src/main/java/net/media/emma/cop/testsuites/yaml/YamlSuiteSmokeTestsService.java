package net.media.emma.cop.testsuites.yaml;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface YamlSuiteSmokeTestsService {

    SmokeTestRawDataDto getAutoTest(String environment, String userAgent, List<String> selectedFiles,
                                    List<String> commonFiles, String pubs, String itype, String email, boolean flagForMail , boolean flagForbot );


    
}

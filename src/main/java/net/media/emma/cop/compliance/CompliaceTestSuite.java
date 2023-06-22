package net.media.emma.cop.compliance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;

@Service @Slf4j
public class CompliaceTestSuite{

    @Autowired BrowserFactory browserFactory;


    public String getTestResults(ComplianceTestCriteria testData){

        log.info("testing");

        return "testResults";
    }
    
}

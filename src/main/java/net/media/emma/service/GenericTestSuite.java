package net.media.emma.service;

import org.springframework.stereotype.Service;

import net.media.emma.config.YamlTestCriteria;

@Service
public interface GenericTestSuite {

    public String getTestResults(YamlTestCriteria testData);
    
}

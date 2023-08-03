package net.media.emma.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import net.media.emma.cop.testsuites.yaml.TestCriteriaBo;

@Service
public interface GenericTestSuite {

    public String getTestResults(TestCriteriaBo testData);
    public CompletableFuture<String> getTestResultsThreadPool(TestCriteriaBo testData);
}

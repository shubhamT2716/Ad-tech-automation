package net.media.emma.service;

import net.media.emma.cop.testsuites.yaml.TestCriteriaBo;
import net.media.emma.cop.testsuites.yaml.TestReportModel;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public interface GenericTestSuite {

    TestReportModel getTestResults(TestCriteriaBo testData, String url, String response);

    CompletableFuture<TestReportModel> getTestResultsThreadPool(TestCriteriaBo testData);


}

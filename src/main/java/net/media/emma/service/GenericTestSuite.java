package net.media.emma.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;


import net.media.emma.cop.testsuites.yaml.TestCriteriaBo;
import net.media.emma.cop.testsuites.yaml.TestReportModel;

@Service
public interface GenericTestSuite {

	public TestReportModel getTestResults(TestCriteriaBo testData, String url, String response);

	public CompletableFuture<TestReportModel> getTestResultsThreadPool(TestCriteriaBo testData);
	
	
}

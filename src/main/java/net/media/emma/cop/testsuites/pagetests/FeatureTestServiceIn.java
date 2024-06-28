package net.media.emma.cop.testsuites.pagetests;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.microsoft.playwright.Request;

import net.media.emma.cop.testsuites.yaml.URLChecksReportModel;

@Component
public interface FeatureTestServiceIn {

	public FeatureReport featureTestCasePositvie(Map.Entry<String, String> urlData, List<Request> requests,
			TestCriteriaBo criteriaBo, URLChecksReportModel reportModel, String feature);
	
	
}

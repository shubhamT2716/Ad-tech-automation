package net.media.emma.cop.testsuites.pagetests;

import com.microsoft.playwright.Request;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface FeatureTestServiceIn {

    FeatureReport featureTestCasePositvie(Map.Entry<String, String> urlData, List<Request> requests,
                                          TestCriteriaBo criteriaBo, URLChecksReportModel reportModel, String feature);


}

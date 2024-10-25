package net.media.emma.cop.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.options.Cookie;
import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport;
import net.media.emma.cop.testsuites.pagetests.TestCriteriaBo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface AutoTesterSreviceIn {

    BidexFeatureReport loadBidExConfig(JsonNode bidexContent, List<String> listOfFeatures, TestCriteriaBo yaml, String harFile, Map<String, List<Cookie>> cookies, String yamlTc, List<String> urls, List<Request> requests, List<String> consoleError, JsonNode slots);


}

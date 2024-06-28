package net.media.emma.cop.components;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.options.Cookie;

import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport;
import net.media.emma.cop.testsuites.pagetests.TestCriteriaBo;

@Service
public interface AutoTesterSreviceIn {

	public BidexFeatureReport loadBidExConfig(JsonNode bidexContent,List<String> listOfFeatures ,TestCriteriaBo yaml, String harFile, Map<String, List<Cookie>> cookies, String yamlTc, List<String> urls,  List<Request> requests, List<String> consoleError) ;

	
}

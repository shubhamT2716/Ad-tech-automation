package net.media.emma.cop.testsuites.pagetests;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
public interface BidexAndFeatureTestsService {
	
	
	public BidexFeatureReport bidexTests(String yamlTc, List<ResponseMocking> mocking) throws JsonMappingException, JsonProcessingException;
	
	

}

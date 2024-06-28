package net.media.emma.cop.testsuites.pagetests;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
public interface BidexAndFeatureTestsService {
	
	
	public BidexFeatureReport bidexTests(String yamlTc) throws JsonMappingException, JsonProcessingException;
	
	

}

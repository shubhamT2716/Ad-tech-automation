package net.media.emma.cop.testsuites.pagetests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BidexAndFeatureTestsService {


    BidexFeatureReport bidexTests(String yamlTc, List<ResponseMocking> mocking) throws JsonProcessingException;


}

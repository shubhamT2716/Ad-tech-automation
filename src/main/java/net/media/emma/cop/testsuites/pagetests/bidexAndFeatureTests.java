package net.media.emma.cop.testsuites.pagetests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.playwright.Page;


import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;


@Service
@Slf4j
public class bidexAndFeatureTests {

	String finalConfig = "";
	String bidexResp = "";
	ObjectMapper mapper = new ObjectMapper(); 
	JsonNode replaced;

    
	@Autowired
	BrowserFactory browserFactory;
	@Autowired
	URLDataProcessing urlDataProcessing;

	@Autowired
	ObjectWriter jsonWriter;

	@Autowired
	ObjectMapper jsonReader;

	@Value("${harFileBasePath}")
	private String harFileBasePath;

    public JsonNode TestFunc(TestCriteria yaml) throws JsonMappingException, JsonProcessingException{

		Page page = BrowserFactory.getPageWithNewContext(yaml.getUserAgent(), null, harFileBasePath);
		try {
		page.onResponse(response -> {
			if(response.url().contains("https://hb.yahoo.net/bidexchange.js?cid")){
			log.info("Response Status: " + response.status() + ", Response Headers: " + response.headers());
			// log.info("Response Body: " + response.text());	
			
			try {
				
				if(response.status()==200){
				bidexResp = response.text();
				}

				// log.info("Bidex Response: " + bidexJsonNode.toPrettyString());
			} catch (Exception e) {
				log.info("Response Json failed");
				// log.info("Json Node value " + bidexJsonNode.toString());

			}
			
			}
        });
 
        page.navigate(yaml.getPublisherUrl());
	
		} catch (Exception e) {
			log.info("Bidex not found");
		}


		// String startTarget  =  ".loadConfig";
		// String endTarget = "}abtest(e,t)";
		// int startIndex=0;
		// int endIndex=0;

		// if(bidexResp.contains(startTarget)){
		// 	 startIndex  = bidexResp.indexOf(startTarget);
		// 	log.info("Start Index : " + startIndex);
		// }
		// if(bidexResp.contains(endTarget)){
		// 	endIndex  = bidexResp.indexOf(endTarget);
		// 	log.info("End Index : " + endIndex);
		// }


		finalConfig = bidexResp.substring(66959, 98446);

		// finalConfig = finalConfig.replaceAll("\""	, "");

		try {
					 replaced = mapper.readTree(finalConfig.toString());
					// log.info("Json :"+ replaced);

		} catch (Exception e) {
			log.info("Converting to json error");
		}

		
		page.close();
		return replaced;
    }
    
}

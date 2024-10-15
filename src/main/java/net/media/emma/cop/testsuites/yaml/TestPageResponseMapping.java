package net.media.emma.cop.testsuites.yaml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class TestPageResponseMapping {

	
	@Value("${responseFilePath}")
	private String responseFilePath;
	
	public String getTestPageUrl(String filename)
	{
		Map<String, String> mapOfurl=new HashMap<>();
		
		mapOfurl.put("emmaTestPage1.yaml", "https://prebid.media.net/rtb/prebid?cid=8CU56755C");
		
		String url=mapOfurl.get(filename);
		
		return url;
		
	}
	
	public String gettestPageResponse(String filename)
	{
		
		
		
		Map<String, String> mapOfResponse=new HashMap<>();
		
		mapOfResponse.put("emmaTestPage1.yaml", "MpBanner.json");
		
		String response=getResponseContent(mapOfResponse.get(filename));
		
		return response;
		
	}
	
	
	private String getResponseContent(String filename)
	{
	
		ClassPathResource classPathResource = new ClassPathResource(responseFilePath+"/" + filename);

		try {
			InputStream inputStream = classPathResource.getInputStream();

			String content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

			inputStream.close();

			return content;
		} catch (Exception e) {
			e.printStackTrace();
			return "Error while reading json" + e.getMessage();
		}
		
	}
	

	
	
	
}



package net.media.emma.cop.testsuites.yaml;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomYamlTestCriteria {

	
	String userAgent;
    String publisherUrl;
    String environment;
    String bidexAppendString;
    List<NetworkCalls> negativeCalls;
    
    
    @Getter
    @Setter
    public class NetworkCalls{
    	private String key;
    	private String value;
    }
	

}

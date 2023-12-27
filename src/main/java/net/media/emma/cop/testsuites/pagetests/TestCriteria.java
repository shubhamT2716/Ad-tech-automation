package net.media.emma.cop.testsuites.pagetests;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestCriteria {
    String environment;
    String userAgent;
    String publisherUrl;
    List<Map<String,String>> coockies;
   
    
    

}

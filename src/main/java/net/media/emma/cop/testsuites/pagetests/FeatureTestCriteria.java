package net.media.emma.cop.testsuites.pagetests;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.pagetests.FeatureReport.FeatureChecks;
import net.media.emma.cop.testsuites.pagetests.FeatureReport.TestStatus;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.PositiveURLChecks;
import net.media.emma.utils.URLDataProcessing;

@Component
@Slf4j
public class FeatureTestCriteria {
	
	
	@Autowired
	private URLDataProcessing dataProcessing;
	
	
	boolean test=true;
	
	public FeatureReport featureTestCasePositvie(Map.Entry<String, String> urlData, TestCriteriaBo criteriaBo, URLChecksReportModel reportModel)
	{
		
		log.info("urlData : "+urlData);
		
		
		
		
		
		
		Map<String,PositiveURLChecks> mapReport=reportModel.getPositiveUrlChecks();
		
		
		
		
		FeatureReport report=FeatureReport.builder().build();
		

		
		Map<String, FeatureChecks> positiveReport=new HashMap<>(); 
		
		Map<String, Map<String, FeatureChecks>> repo=new HashMap<>();
		
		Set<String> message=new HashSet<>();
		
		List<String> matchingUrl=new ArrayList<>();		
//		String s=urlData.getKey();
		
		String v1=urlData.getValue();
		
		log.info("key from urlData : ");
		log.info("value from urlData: "+v1);
		
		
		
//		Map<String, String> urlQuesryStringMap=dataProcessing.getQueryStringMapFromUrl(v1);

		
		criteriaBo.getFeatureKeysNegative().entrySet().forEach(entry->{

			
			String key=entry.getKey();
			
			List<String> matchingUrls=mapReport.get(key).getMatchingUrls();
			
			for(String urls: matchingUrls) {
				Map<String, String> urlQuesryStringMap=dataProcessing.getQueryStringMapFromUrl(urls);
			
			
			
			
		Map<String, String> value =entry.getValue();
		
		
		if(mapReport.containsKey(key))
		{
			FeatureChecks negativechecks= new FeatureReport.FeatureChecks();
		
		value.entrySet().forEach(valueEntry->{
			
			String vKey=valueEntry.getKey();
			
			
			
			String vValue= valueEntry.getValue();   
			
			
			
			
			if(urlQuesryStringMap.containsKey(vKey)&&urlQuesryStringMap.get(vKey).equals(vValue))
			{
//				negativeFeatureChecks.setMatchingUrls(matchingUrl);
				
				
				 String msg="Negative Value Foound: "+vKey+" = "+vValue;
				 
				 
				 log.info("Message  : "+msg);
				 
				 
				 if(vKey.equals("y_dnb"))
				 {
					 message.add(msg);
				 }
				 else {
					 negativechecks.setTestResult(TestStatus.FAIL);
					 
					 negativechecks.setFeatureValuesChecks(value);
					 message.add(msg);
					 
					 test=false;
					 
				 }
		
	    		 
			}
			else {
				
			
			
			try {
				String url =URLDecoder.decode(urls, "UTF-8");
				
				if(url.contains(vKey))
				{
					
					System.err.println(url);
					
					int index=url.indexOf(vKey);
					
					String url1=url.substring(index);
					int sIndex=url1.indexOf(vKey);
					int eIndex=url1.indexOf("&");
					
					url1=url1.substring(sIndex, eIndex);
					
					Map<String, String> m = new HashMap<>();
			        String[] keyValuePairs = url1.split("=");
			        if (keyValuePairs.length == 2) {
			            m.put(keyValuePairs[0], keyValuePairs[1]);
			        }
			        
			        
			      
			        if(m.get(vKey)!=null)
					      {
					    	  
					    	  if(m.get(vKey).equals(vValue))
					    	  {
//					    		 report.getPositiveFeatureChecks().setTestResult(TestStatus.PASS); 
					    		  
					    		  String msg="Negative Value Foound: "+m;
					    		  
					    		  if(vKey.equals("y_dnb"))
									 {
					    			 message.add(msg);
									 }
									 else {
										 negativechecks.setTestResult(TestStatus.FAIL);
										
										 negativechecks.setFeatureValuesChecks(value);
										message.add(msg);
										 test=false;
									 }
					    		  
					    	  }
					    	  
					      }
		
					
				}
	
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			}
			
			negativechecks.setTestMessages(message);
			
			positiveReport.put(key, negativechecks);
	

				});
				
			}
		
			}
			
		});
		
		if(test==true)
		{
			
		
		
		criteriaBo.getFeatureKeysPositive().entrySet().forEach(entry->{

		
			String key=entry.getKey();
			List<String> matchingUrls=mapReport.get(key).getMatchingUrls();
			
			for(String urls: matchingUrls) {
				Map<String, String> urlQuesryStringMap=dataProcessing.getQueryStringMapFromUrl(urls);
			
			
			
		Map<String, String> value =entry.getValue();
		
		
		
		if(mapReport.containsKey(key))
		{
			FeatureChecks positiveFeatureChecks= new FeatureReport.FeatureChecks();
		value.entrySet().forEach(valueEntry->{
			
			String vKey=valueEntry.getKey();
			
			
			String vValue= valueEntry.getValue();   
			
			
			
			
			if(urlQuesryStringMap.containsKey(vKey)&&urlQuesryStringMap.get(vKey).equals(vValue))
			{
				
				
//				 positiveFeatureChecks.setMatchingUrls(matchingUrl);
	    		 positiveFeatureChecks.setTestResult(TestStatus.PASS);
	    		 
	    		 positiveFeatureChecks.setFeatureValuesChecks(value);
	    		
	    		 String msg="Positive Check Pass "+vKey+" "+vValue;
	    		
	    		 
	    		 message.add(msg);
	    		 
			}
			else {
				
			
			
			try {
				String url =URLDecoder.decode(urls, "UTF-8");
				
				if(url.contains(vKey))
				{
					
					int index=url.indexOf(vKey);
					
					String ur=url.substring(index);
					int sIndex=ur.indexOf(vKey);
					int eIndex=ur.indexOf("&");
					
					ur=ur.substring(sIndex, eIndex);
					
					Map<String, String> m = new HashMap<>();
			        String[] keyValuePairs = ur.split("=");
			        if (keyValuePairs.length == 2) {
			            m.put(keyValuePairs[0], keyValuePairs[1]);
			        }
			        
			        
			        
			        if(m.get(vKey)!=null)
					      {
					    	  
					    	  if(m.get(vKey).equals(vValue))
					    	  {
//					    		 report.getPositiveFeatureChecks().setTestResult(TestStatus.PASS); 
					    		  
//					    		 positiveFeatureChecks.setMatchingUrls(matchingUrl);
					    		 positiveFeatureChecks.setTestResult(TestStatus.PASS);
					    		 
					    		 positiveFeatureChecks.setFeatureValuesChecks(value);
					    		 
					    		 
					    		 String msg="Positive Check Pass "+vKey+" "+vValue;
						    		
					    		 
					    		 message.add(msg);
					    		 
					    		 
					    	  }
					    	  
					      }
					
					
					
				}
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			}
			
			log.info("message: "+message);
			
			
			positiveFeatureChecks.setTestMessages(message);
			
			log.info("positiveFeatureCheck: "+positiveFeatureChecks);
			
			positiveReport.put(key, positiveFeatureChecks);

				});
				
			}
			}
		});
		}
		
		
		System.out.println(positiveReport.toString());
		
		repo.put("AdStacking", positiveReport);
		
//		report.setNegativeFeatureChecks(negativeReport);
		report.setFeatureChecks(repo);
		
		
		return report;

	}
	

	

}

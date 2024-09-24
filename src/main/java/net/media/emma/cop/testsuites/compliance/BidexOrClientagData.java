package net.media.emma.cop.testsuites.compliance;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Request;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.yaml.TestReportModel.BidexData;

@Slf4j
@Component
public class BidexOrClientagData {
	
	
	
	 // List of strings "1", "2"
    List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net",
            "hbx-stage.media.net","hbx-stage-2.media.net","hbx-stage-3.media.net","hbx-stage-4.media.net","hbx-stage-1.media.net","ssp-stage-hb.media.net","ssp-stage-hb-1.media.net","ssp-stage-hb-2.media.net","ssp-stage-hb-3.media.net","ssp-stage-hb-4.media.net");
    List<String> bidExControlers = Arrays.asList("bidexchange.js", "clientag.js", "js/tags/clientag.js","ss/nes/bidexl2.js");// , "catchpoint_content.js",
    // "topics.js");
    
    
    BidexData bd=new  BidexData();

	
	public BidexData getBidExData(Request request) {

		BidexData bidexData = BidexData.builder().build();
		
		ObjectMapper mapper=new ObjectMapper();

		//get data from request
		// convert into request into json
		String response=request.response().text();
		
	
		
		try {
			
			List<String> clientagController=Arrays.asList("js/tags/clientag.js","clientag.js");	
			String regexForMp= ".*/(" + String.join("|", bidExHosts) + ")/("
	                + String.join("|", clientagController) + ").*";
			
			List<String> bibex1=Arrays.asList("bidexchange.js");
			String regexForBidex1=".*/(" + String.join("|", bidExHosts) + ")/("
	                + String.join("|", bibex1) + ").*";
			
			List<String> bidex2=Arrays.asList("ss/nes/bidexl2.js?");
			String regexForBidex2=".*/(" + String.join("|", bidExHosts) + ")/("
	                + String.join("|", bidex2) + ").*";
		
			
//			if(request.url().startsWith("https://warp.media.net/js/tags/clientag.js")|| request.url().startsWith("**//**/js/tags/clientag.js")|| request.url().startsWith("https://hbx-stage.media.net/js/tags/clientag.js")|| request.url().startsWith("https://hbx.media.net/js/tags/clientag.js")|| request.url().startsWith("https://hbx-stage-2.media.net/js/tags/clientag.js")|| request.url().startsWith("https://hbx-stage-3.media.net/js/tags/clientag.js")|| request.url().startsWith("https://hbx-stage-4.media.net/js/tags/clientag.js")|| request.url().startsWith("https://ssp-stage-hb.media.net/js/tags/clientag.js")|| request.url().startsWith("https://ssp-stage-hb-1.media.net/js/tags/clientag.js")|| request.url().startsWith("https://ssp-stage-hb-2.media.net/js/tags/clientag.js")|| request.url().startsWith("https://ssp-stage-hb-3.media.net/js/tags/clientag.js")|| request.url().startsWith("https://ssp-stage-hb-4.media.net/js/tags/clientag.js"))
		if(request.url().matches(regexForMp))
			{
			
			
			String responseMP=request.response().text();
			
				List<String> sspMp=Arrays.asList("ssp-stage-hb.media.net","ssp-stage-hb-1.media.net","ssp-stage-hb-2.media.net","ssp-stage-hb-3.media.net","ssp-stage-hb-4.media.net");
			
				String regexForSSPMp= ".*/(" + String.join("|", sspMp) + ")/("
		                + String.join("|", clientagController) + ").*";
				
				
				if(request.url().matches(regexForSSPMp))
				{
					
					boolean svrFind=false;
					
//					String regex = "var i =.*(mp|ssp)";
//					
//					Pattern pattern=Pattern.compile(regex);
//					
//					Matcher matcher=pattern.matcher(responseMP);
					
					String t="new o.a({";
					
					
					if(responseMP.contains(t))
					{
						int index=responseMP.indexOf(t);
						
						responseMP=responseMP.substring(index);
						
						Pattern pattern=Pattern.compile("new\\s+o\\.a\\((.*?)\\)");
						
						Matcher matcher=pattern.matcher(responseMP);
						
						if(matcher.find())
						{
							
							
							String extractedData=matcher.group(1);
							
							String svr = null;
							
							if(responseMP.contains("var i"))
							{
								int svrIndex=responseMP.indexOf("var i");
								
								responseMP=responseMP.substring(svrIndex);
								
//								System.out.println(responseMP);
								
								Pattern pattern2=Pattern.compile("var i=\"([^\"]+)\"");
								
								Matcher matcher2=pattern2.matcher(responseMP);
								
								if(matcher2.find())
								{
									
									System.out.println("match found");
									
									 svr = matcher2.group(1);
									
									
									
								}
								
							}
							
							JsonNode bidData=mapper.readValue(extractedData, JsonNode.class);
							
							bidexData.setCid(bidData.get("cid").asText());
							
							if(svr==null)
							{
								bidexData.setSvr(bidData.get("sver").asText());	
							}else {
								bidexData.setSvr(svr);
							}
							
//							bidexData.setSvr(bidData.get("sver").asText());
							bidexData.setSugd(bidData.get("sugd").asText());
							bidexData.setCt(bidData.get("ip2ct").asText());
							bidexData.setServname(bidData.get("servname").toString());
							bidexData.setBrowser(bidData.get("browser").toString());
							bidexData.setLw(bidData.get("lw").toString());
							
//							bidexData.setItype(bidData.get("itype").asText());
							
							
							return bidexData;
					}
					
					
					}	
					
					
					if(svrFind==false)
					{
						
						String target="function createErrorObject(error) {";
						
						int in=responseMP.indexOf(target);
						
						responseMP=responseMP.substring(in);					
						
						Pattern pattern1=Pattern.compile("svr\\s*:\\s*\"([^\"]*)\"");
						
						Matcher matcher1=pattern1.matcher(responseMP);
						
						if(matcher1.find())
						{
							
							
						System.out.println(responseMP);
//							
						System.err.println(matcher1);
							
						String svr=matcher1.group();
						
						System.out.println(svr);
							
							String svr1=matcher1.group(1);
							
							
//							System.out.println(svr);
							
							System.err.println(svr1);
							
							
							bidexData.setSvr(svr1);
							
						}
						
						
					}
					

							
				}
				else {
					
//				String target="new s.a({";
				String target="new r.a({";
				
		
				if(responseMP.contains(target))
				{
					
		
					int in=responseMP.indexOf(target);
			
					responseMP=responseMP.substring(in);
					
					Pattern pattern=Pattern.compile("new\\s+r\\.a\\((.*?)\\)");
					
					Matcher matcher=pattern.matcher(responseMP);
					
					if(matcher.find())
					{
						
						
						String extractedData=matcher.group(1);
						
						String svr = null;
						
						if(responseMP.contains("const i"))
						{
							int svrIndex=responseMP.indexOf("const i");
							
							responseMP=responseMP.substring(svrIndex);
							
//							System.out.println(responseMP);
							
							Pattern pattern2=Pattern.compile("const i=\"([^\"]+)\"");
							
							Matcher matcher2=pattern2.matcher(responseMP);
							
							if(matcher2.find())
							{
																
								 svr = matcher2.group(1);		
								
							}
							
						}
						
						JsonNode bidData=mapper.readValue(extractedData, JsonNode.class);
						
						bidexData.setCid(bidData.get("cid").asText());
						
						if(svr==null)
						{
							bidexData.setSvr(bidData.get("sver").asText());	
						}else {
							bidexData.setSvr(svr);
						}
						
//						bidexData.setSvr(bidData.get("sver").asText());
						bidexData.setSugd(bidData.get("sugd").asText());
						bidexData.setCt(bidData.get("ip2ct").asText());
						bidexData.setServname(bidData.get("servname").toString());
						bidexData.setBrowser(bidData.get("browser").toString());
						bidexData.setLw(bidData.get("lw").toString());
						
//						bidexData.setItype(bidData.get("itype").asText());
						
						
						return bidexData;
					}
		
				}
			}
					
			}
//			else if(request.url().contains("**/bidexchange.js?")|| request.url().startsWith("https://hbx-stage-2.media.net//bidexchange.js")|| request.url().startsWith("https://hbx-stage-3.media.net//bidexchange.js")|| request.url().startsWith("https://hbx-stage-4.media.net//bidexchange.js")|| request.url().startsWith("https://hbx-stage.media.net//bidexchange.js")|| request.url().startsWith("https://hbx.media.net//bidexchange.js?")|| request.url().startsWith("https://hbx.media.net/bidexchange.js?")|| request.url().startsWith("https://ssp-stage-hb.media.net/bidexchange.js?")|| request.url().startsWith("https://ssp-stage-hb-1.media.net//bidexchange.js")|| request.url().startsWith("https://ssp-stage-hb-2.media.net//bidexchange.js")|| request.url().startsWith("https://ssp-stage-hb-3.media.net//bidexchange.js")|| request.url().startsWith("https://ssp-stage-hb-4.media.net//bidexchange.js")){
		else if(request.url().matches(regexForBidex1))
		{
		
			 int startIndex=0;


			 String startTarget = "{\"AaxDpCeiling\"";
			if (response.contains(startTarget)) {
				startIndex = response.indexOf(startTarget);
				startIndex += 12;

			}

			//control in Bidexconfig.java
			String finalString = response.substring(startIndex-12);
			 JsonNode bidexJson= mapper.readValue(finalString, JsonNode.class);

			
			JsonNode envData=bidexJson.get("Environment");
			
			bidexData.setCid(envData.get("cid").asText());
			bidexData.setSvr(envData.get("sver").asText());
			bidexData.setSugd(envData.get("sugd").asText());
//			bidexData.setItype(envData.get("itype").asText());
			bidexData.setCt(envData.get("ip2ct").asText());
			bidexData.setServname(envData.get("servname").toString());
			bidexData.setBrowser(envData.get("browser").toString());
			bidexData.setLw(envData.get("lw").toString());
			
			bd.setCid(envData.get("cid").asText());
			bd.setCt(envData.get("ip2ct").asText());
			bd.setSvr(envData.get("sver").asText());	
			bd.setSugd(envData.get("sugd").asText());
			bd.setServname(envData.get("servname").toString());
			bd.setBrowser(envData.get("browser").toString());
			bd.setLw(envData.get("lw").toString());
//			bd.setItype(envData.get("itype").asText());
		}
			
//			if(request.url().startsWith("https://hbx-stage-1.media.net/ss/nes/bidexl2.js?")|| request.url().startsWith("https://hbx-stage-2.media.net/ss/nes/bidexl2.js?"))
			
		if(request.url().matches(regexForBidex2))
		{
				
				
				
				bidexData.setCid(bd.getCid());
				bidexData.setSvr(bd.getSvr());
				bidexData.setCt(bd.getCt());
//				bidexData.setItype(bd.getItype());
				bidexData.setSugd(bd.getSugd());
				bidexData.setServname(bd.getServname());
				bidexData.setBrowser(bd.getBrowser());
				bidexData.setLw(bd.getLw());
				
//				bd.setCid();
//				bd.setCt(envData.get("ip2ct").asText());
//				bd.setSvr(envData.get("sver").asText());	
//				bd.setSugd(envData.get("sugd").asText());
//				bd.setItype(envData.get("itype").asText());
			}
		
		
			
		}
		catch (Exception e) {
			
			log.error("Error in fetch Svr: "+e.getMessage());
			
			e.printStackTrace();
			
		}
		

		return bidexData;
	}
	
}

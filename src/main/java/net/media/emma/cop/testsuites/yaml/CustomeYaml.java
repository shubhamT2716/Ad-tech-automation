package net.media.emma.cop.testsuites.yaml;

import java.util.HashMap;
import java.util.Map;


import org.springframework.stereotype.Service;

@Service
public class CustomeYaml implements CustomYamlService {


	public Map<String, String> getPositiveCalls()
	{
		Map<String, String> positiveUrls=new HashMap<>();
		positiveUrls.put("gampad", "https://securepubads.g.doubleclick.net/gampad/ads?");
		positiveUrls.put("checksync", "https://hbx.media.net/checksync.php?");
		positiveUrls.put("APPR", "https://navvy.media.net/log");
		positiveUrls.put("ARlog", "https://hblg.media.net/log?logid=kfk&evtid=arlog");
		positiveUrls.put("RA", "https://navvy.media.net/log");
		positiveUrls.put("Adplog", "https://hblg.media.net/log?logid=kfk&evtid=adplog");
		positiveUrls.put("dfs", "https://warp.media.net/ss/nes/dfs?");
		positiveUrls.put("creative", "https://warp.media.net/js/static/creative.js");
		positiveUrls.put("belog", "https://hbxlp.media.net/log");
		positiveUrls.put("prtl", "https://hbxlp.media.net/log");
		positiveUrls.put("pubmatic", "https://hbopenbid.pubmatic.com/translator");
		positiveUrls.put("medianet", "https://prebid.media.net/rtb/prebid");
		positiveUrls.put("index", "https://htlb.casalemedia.com/openrtb/pbjs");
		positiveUrls.put("Appnexus", "https://ib.adnxs.com/ut/v3/prebid");
		positiveUrls.put("rubicon", "https://fastlane.rubiconproject.com/a/api/fastlane.json");
		positiveUrls.put("kargo", "https://krk2.kargo.com/api/v1/prebid");
		positiveUrls.put("undertone", "https://hb.undertone.com/hb?pid");
		positiveUrls.put("openx", "https://rtb.openx.net");
		positiveUrls.put("triplelift", "https://tlx.3lift.com");
		positiveUrls.put("auction", "https://prebid-s2s.media.net/openrtb2/auction");
		positiveUrls.put("yeildmo", "https://ads.yieldmo.com/exchange/prebid");
		positiveUrls.put("gpt", "https://securepubads.g.doubleclick.net/tag/js/gpt.js");
		
		// hb2 is pending need to add call for HB2
		
		return positiveUrls;
		
		
	}
	
	public Map<String, String> getNegativeCalls()
	{
		Map<String, String> negativeUrl=new HashMap<>();
		
		negativeUrl.put("nerrPing", "https://hblg.media.net/nerrping.php");
		negativeUrl.put("flog", "https://hblg.media.net/log?logid=kfk&evtid=flog");
		
		return negativeUrl;
		
	}
 	
	
}

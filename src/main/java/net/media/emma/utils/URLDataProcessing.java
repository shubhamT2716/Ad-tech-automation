package net.media.emma.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.microsoft.playwright.Request;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class URLDataProcessing {

	public String getRequestFor2(String url, List<String> networkCalls) {

		for (String currentCall : networkCalls) {

			if (currentCall.startsWith(url)) {

				return currentCall;
			}
		}

		return null;
	}

	public Request getRequestFor(String url, List<Request> requestList) {

		for (Request request : requestList) {
			if (request.postData() != null) {
				log.info(request.url());
				log.info(url);
			}
			if (request.url().startsWith(url)) {
				log.info(request.toString());
				return request;
			}
		}

		return null;
	}

	public String printURLDetails(List<String> urlPatterns) {

		StringBuilder sb = new StringBuilder("URL Data: \n");

		int sizeOfURLs = 0;
		Set<String> uniqueKeys = new HashSet<String>();

		for (String urlPattern : urlPatterns) {

			sizeOfURLs += urlPattern.length();

			try {
				URL url = new URL(urlPattern);

				HashMap<String, String> queryStringMap = queryStringToMap(url.getQuery());

				uniqueKeys.addAll(queryStringMap.keySet());

				sb.append(queryStringMap.get("evtid") + "-" + queryStringMap.get("logid") + ".\nCount of key-val pairs:"
						+ queryStringMap.size() + ".\nSize of the URL: " + urlPattern.length() + ".\n"
						+ queryStringMap.toString() + "\n\n");
			} catch (MalformedURLException e) {
				log.error("URL malformed exception");
				continue;
			}
		}

		sb.insert(0, "Total Size of URLs: " + sizeOfURLs + "\n" + "Total Unique Keys: " + uniqueKeys.toString() + "\n");

		return sb.toString();
	}
	
	public static  boolean isValidUrl(String url) {

		try {
			
			URL url1 = new URL(url);

				if(url1==null)
				{
					throw new Exception();
				}
			
			String u = url1.getQuery();
			
			

			return true;
		} catch (Exception e) {
			return false;
		}

	}
	

	public HashMap<String, String> queryStringToMap(String queryString) {
		
		HashMap<String, String> queryStringMap = new HashMap<String, String>();

		
		if(queryString==null)
		{
			return queryStringMap;
		}
		
			String[] queryStringParts = queryString.split("\\&");
			
			

		for (String queryStringPart : queryStringParts) {

			String[] queryStringPartParts = queryStringPart.split("\\="); // "auctionId="

			if (queryStringPartParts.length == 2) {
				queryStringMap.put(queryStringPartParts[0], queryStringPartParts[1]);
				// queryStringMap.put(queryStringPartParts[0], ""); --rats
				// queryStringMap.put(queryStringPartParts[0], null); --rats
			}
//			if (queryStringPartParts.length == 1) {
//				queryStringMap.put(queryStringPartParts[0], "");
//			}

//			if (queryStringPartParts.length == 2 || queryStringPartParts.length == 1) {
//
//				if (queryStringPartParts.length == 2) {
//					queryStringMap.put(queryStringPartParts[0], queryStringPartParts[1]);
//				}
//				queryStringMap.put(queryStringPartParts[0], "");
//			}

		}
		
		

		
		
		return queryStringMap;
	}

	public HashMap<String, String> getQueryStringMapFromUrl(String url, List<Request> requests) {

		try {
			
			URL urlObj = new URL(url);
			
			if(urlObj.getQuery()==null)
			{
				for(Request req:requests)
				{
					if(req.url().startsWith(url))
					{
						return queryStringToMap(req.postData());
					}
				}
			}
			
			
			
			
			return queryStringToMap(urlObj.getQuery());

		} catch (MalformedURLException e) {
			log.error("URL malformed exception");
		}

		return new HashMap<>();
	}
}
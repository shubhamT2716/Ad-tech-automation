package net.media.emma.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;

@Component
public class ResponseMocking {

	
	Object id;
	
	JSONArray jsonArray;
	
	public Page responseMocking(Page page, String url , String response )
	{
		
		
		
		List<Object> listOfImp=new ArrayList<>();
		
		page.onRequest(request->{
			
			if(request.url().equals(url))
			{
				String postData=request.postData();
				
				JSONObject jsonObject=new JSONObject(postData);
				
				id=jsonObject.get("id");
				
				jsonArray =jsonObject.getJSONArray("imp");
				
			}
			
		});
		
		
		page.route(url, route->{
			
			APIResponse apiResponse=route.fetch();
			
			byte [] body=apiResponse.body();
			
			for(int i=0;i<jsonArray.length();i++)
			{
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				
				Object id=jsonObject.get("id");
				
				listOfImp.add(id);
				
			}
			
		
			
			ObjectMapper mapper=new ObjectMapper();
			
			try {
				
				JSONObject jo=new JSONObject(response);
				
				jo.put("id",id);
				
				
				JSONArray bidListArray=jo.getJSONArray("bidList");
				
				Object ext=jo.get("ext");
				
				
				List<Object> li=new ArrayList<>();
				
				for(int i=0;i<bidListArray.length();i++)
				{
					JSONObject bidObject=bidListArray.getJSONObject(i);
					
					bidObject.put("requestId", listOfImp.get(i));
					
					li.add(bidObject);		
					
					
				}
				
				jo.put("bidList", li);
				jo.put("ext", ext);
				

				//convert JSONObject to String
				String message=jo.toString();


				System.err.println(message);
				
				listOfImp.clear();
				
				
//				Thread.sleep(10000);
				
			
				route.fulfill(new Route.FulfillOptions().setResponse(apiResponse).setBody(message));

				
				
			
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			
			
		});
		
	
		return page;
	}
	
}

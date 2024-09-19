package net.media.emma.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.stereotype.Component;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResponseMocking {

	private Object id;

	private JSONArray jsonArray;
	
	boolean temp=false;

	public Page responseMocking(Page page, String url, String response) {

		log.info("added Requestly");

		List<Object> listOfImp = new ArrayList<>();

		page.onRequest(request -> {

			if (request.url().equals(url)) {
				String postData = request.postData();

				JSONObject jsonObject = new JSONObject(postData);

				if(temp==false)
				{
					id = jsonObject.get("id");

					jsonArray = jsonObject.getJSONArray("imp");
				}
			}

		});

		page.route(url, route -> {

			APIResponse apiResponse = route.fetch();

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				Object id = jsonObject.get("id");

				listOfImp.add(id);

			}

			try {

				JSONObject jObject = new JSONObject(response);

				JSONArray bidListArray = jObject.getJSONArray("bidList");

				List<Object> li = new ArrayList<>();

				for (int i = 0; i < bidListArray.length(); i++) {
					JSONObject bidObject = bidListArray.getJSONObject(i);

					bidObject.put("requestId", listOfImp.get(i));

					li.add(bidObject);

				}
				jObject.put("id", id);
				jObject.put("bidList", li);

				String message = jObject.toString();

//				route.fulfill(new Route.FulfillOptions().setResponse(apiResponse).setBody(message));

				route.fulfill(new Route.FulfillOptions().setBody(message));

				listOfImp.clear();

				log.info("response mocking done by emma");

			} catch (Exception e) {
				e.printStackTrace();

				log.info("error while response mocking " + e.getMessage());
			}

		});

		log.info("completed response mocking");

		return page;
	}

}

package net.media.emma.utils;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ResponseMocking {

    boolean temp = false;
    private Object id;
    private JSONArray jsonArray;

    public Page responseMocking(Page page, String url, String response) {

        log.info("added Requestly");

        List<Object> listOfImp = new ArrayList<>();

        page.onRequest(request -> {

            if (request.url().equals(url)) {
                String postData = request.postData();

                JSONObject jsonObject = new JSONObject(postData);

                if (!temp) {
                    id = jsonObject.get("id");

                    jsonArray = jsonObject.getJSONArray("imp");
                }
            }

        });

        page.route(url, route -> {

//			APIResponse apiResponse = route.fetch();

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


    public Page addResponseMocking(Page page, String url, String response) {

        System.err.println("Mocking Start.........");

        page.route(url, route -> {

            List<Object> listOfImp = new ArrayList<>();

            System.out.println("Process start.......");

            if (route.request().url().equalsIgnoreCase(url)) {


                String postData = route.request().postData();

                JSONObject jsonObject = new JSONObject(postData);

                Object id = jsonObject.get("id");

                JSONArray jsonArray = jsonObject.getJSONArray("imp");


                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);

                    Object ids = json.get("id");

                    listOfImp.add(ids);

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


//					route.fulfill(new Route.FulfillOptions().setResponse(apiResponse).setBody(message));

                    route.fulfill(new Route.FulfillOptions().setBody(message));

                    listOfImp.clear();

                    log.info("response mocking done by emma");

                } catch (Exception e) {
                    e.printStackTrace();

                    log.info("error while response mocking " + e.getMessage());
                }


            }


        });

        return page;

    }


}

package net.media.emma.impl.playwright.network;

import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;

public class RequestUrls {

    Page page;

    public RequestUrls(Page page) {
        this.page = page;
    }

    public List<String> getRequestUrls(String reqUrl) {
        List<String> requestUrlDetails = new ArrayList<>();
        page.onRequest(request ->
        {
            if (request.url().contains(reqUrl)) {
                System.out.println("Ending --- ");
                requestUrlDetails.add(request.url());
            }
        });
        return requestUrlDetails;
    }
}

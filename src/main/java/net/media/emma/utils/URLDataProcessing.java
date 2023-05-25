package net.media.emma.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class URLDataProcessing {

    public String printURLDetails(List<String> urlPatterns){

        StringBuilder sb = new StringBuilder("URL Data: \n");

        int sizeOfURLs = 0;

        for(String urlPattern : urlPatterns){

            sizeOfURLs+=urlPattern.length();

            try{
                URL url = new URL(urlPattern);

                HashMap<String, String> queryStringMap = queryStringToMap(url.getQuery());

                sb.append(
                        queryStringMap.get("evtid") + "-" + queryStringMap.get("logid") + 
                        ".\nCount of key-val pairs:" + queryStringMap.size() +
                        ".\nSize of the URL: " + urlPattern.length() +
                        ".\n" + queryStringMap.toString() + "\n\n");
            }
            catch(MalformedURLException e){
                log.error("URL malformed exception");
                continue;
            }
        }

        sb.insert(0, "Total Size of URLs: " + sizeOfURLs + "\n");

        return sb.toString();
    }

    private HashMap<String, String> queryStringToMap(String queryString){

        HashMap<String, String> queryStringMap = new HashMap<String, String>();

        String[] queryStringParts = queryString.split("\\&");

        for(String queryStringPart : queryStringParts){

            String[] queryStringPartParts = queryStringPart.split("\\=");

            if(queryStringPartParts.length == 2){
                queryStringMap.put(queryStringPartParts[0], queryStringPartParts[1]);
            }
        }

        return queryStringMap;
    }
}
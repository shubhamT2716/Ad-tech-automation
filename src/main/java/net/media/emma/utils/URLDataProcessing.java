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

        for(String urlPattern : urlPatterns){

            try{
                URL url = new URL(urlPattern);

                HashMap<String, String> queryStringMap = queryStringToMap(url.getQuery());

                sb.append(queryStringMap.get("evtid") + "-" + queryStringMap.get("logid") + "\n" + queryStringMap.toString() + "\n\n");
            }
            catch(MalformedURLException e){
                log.error("URL malformed exception");
                continue;
            }
        }

        return sb.toString();
    }

    private HashMap<String, String> queryStringToMap(String queryString){

        log.info("\n" + queryString);

        HashMap<String, String> queryStringMap = new HashMap<String, String>();

        String[] queryStringParts = queryString.split("\\&");

        log.info(queryStringParts.toString());

        for(String queryStringPart : queryStringParts){

            String[] queryStringPartParts = queryStringPart.split("\\=");

            log.info(queryStringPartParts.toString());

            if(queryStringPartParts.length == 2)
            queryStringMap.put(queryStringPartParts[0], queryStringPartParts[1]);
        }

        return queryStringMap;
    }
}
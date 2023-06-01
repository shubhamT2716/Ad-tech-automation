package net.media.emma.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class ExtractKeyValueFromLogs {

    public static ArrayList<String> getMultipleValOfSameKey(String url, String key) throws UnsupportedEncodingException {
        ArrayList<String> keyVal = new ArrayList<>();
        String urlPath = url.split("[?]")[1];
        String urlArr[] = urlPath.split("&");
        for (int i = 0; i < urlArr.length; i++) {
            if (urlArr[i].split("=").length > 1) {
                String tempKey = URLDecoder.decode(urlArr[i].split("=")[0], "UTF-8");
                String tempVal = URLDecoder.decode(urlArr[i].split("=")[1], "UTF-8");
                if (tempKey.equals(key)) {
                    keyVal.add(tempVal);
                }
            }
        }
        return keyVal;
    }

    public static List<String> getMulValOfSameKeyFromDiffUrls(List<String> url, String key) throws UnsupportedEncodingException {

        List<String> val = new ArrayList<>();
        for(int i=0;i<url.size();i++) {
            val.addAll(getMultipleValOfSameKey(url.get(i),key));
        }
        return val;
    }
}

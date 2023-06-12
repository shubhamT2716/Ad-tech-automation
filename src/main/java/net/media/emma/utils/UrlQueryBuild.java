package net.media.emma.utils;

//package com.java2s;
/*//  ww w .java  2s  .  co m
 * The MIT License (MIT)
 * Copyright (c) 2016 Fernando Barillas (FBis251)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UrlQueryBuild {
    /**
     * Parses a URLs query parameters into a Map for easier parsing of options.
     * For example, a URL http://example.com?one=1&amp;two=true
     * would return the map:
     * K: "one", V: "1"
     * K: "two", V: "true"
     *
     * @param urlString The URL to attempt to map the query parameters for
     * @return Null if an invalid URL is passed in, otherwise a Map containing all the keys and
     * values contained in the URL query parameters
     */
    public static Map<String, String> getQueryMap(String urlString) {
        return getQueryMap(getUrlObject(urlString));
    }

    /**
     * @param url The URL to attempt to map the query parameters for
     * @return Null if an invalid URL is passed in, otherwise a Map containing all the keys and
     * values contained in the URL query parameters
     * @see #getQueryMap(String)
     */
    public static Map<String, String> getQueryMap(URL url) {
        if (url == null)
            return null;
        return buildQueryMap(url.getQuery());
    }

    /**
     * @param uri The URI to attempt to map the query parameters for
     * @return Null if an invalid URI is passed in, otherwise a Map containing all the keys and
     * values contained in the URI query parameters
     * @see #getQueryMap(String)
     */
    public static Map<String, String> getQueryMap(URI uri) {
        if (uri == null)
            return null;
        return buildQueryMap(uri.getQuery());
    }

    /**
     * Verifies that a URL contains a certain TLD or a subdomain of the passed in TLD. Additionally
     * the passed-in urlString will be converted to a valid URL object
     *
     * @param urlString  A url to attempt to convert to a URL object
     * @param baseDomain The TLD to match with no subdomain prefix or trailing slash, example:
     *                   imgur.com
     * @return A URL Object representation of the passed in urlString
     */
    public static URL getUrlObject(String urlString, String baseDomain) {
        URL url = getUrlObject(urlString);
        if (url == null)
            return null;

        String domain = url.getHost();
        if (domain.equals(baseDomain) || domain.endsWith("." + baseDomain)) {
            return url;
        }

        return null;
    }

    /**
     * Get a URL Object from a passed in String
     *
     * @param urlString The String to attempt to parse as a URL
     * @return A URL representation if the passed in String is a valid URL, null otherwise
     */
    public static URL getUrlObject(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException ignored) {
        }
        return null;
    }

    private static Map<String, String> buildQueryMap(String query) {
        if (query == null)
            return null;

        String[] params = query.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] currentParam = param.split("=");
            if (currentParam.length != 2)
                continue;
            String name = currentParam[0];
            String value = currentParam[1];
            map.put(name, value);
        }
        return map;
    }
}
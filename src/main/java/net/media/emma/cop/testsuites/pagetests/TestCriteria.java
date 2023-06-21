package net.media.emma.cop.testsuites.pagetests;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestCriteria {
    String environment;
    String userAgent;
    String publisherUrl;
    Map<String, String>  ypbjs;
    Map<String, String>  rtbs;

}

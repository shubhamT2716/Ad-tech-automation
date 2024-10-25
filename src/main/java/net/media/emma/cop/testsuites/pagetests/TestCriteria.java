package net.media.emma.cop.testsuites.pagetests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestCriteria {
    String environment;
    String userAgent;
    String publisherUrl;
    List<Map<String, String>> coockies;


}

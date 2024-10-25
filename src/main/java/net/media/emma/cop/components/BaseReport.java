package net.media.emma.cop.components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;

@Getter
@Setter
@Builder
public class BaseReport {

    String environment;
    String userAgent;
    String publisherUrl;
    String harFileDownloadLink;

    @Builder.Default
    String dateTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS").format(new java.util.Date());

}

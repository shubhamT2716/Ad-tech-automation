package net.media.emma.cop.testsuites.loggingpixels;

import lombok.*;

import java.util.List;
@Setter @Getter @Builder  @NoArgsConstructor @AllArgsConstructor
public class LoggingPixelTestDataModel{

    public List<DataTypeChecks> loggingPixelTests;

    @Getter @Setter
    public static class DataTypeChecks{
        public String key;
        public String valueDataType;
        public String valueRegex;
    }
    public String pubUrl;
}

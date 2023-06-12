package net.media.emma.cop.testsuites.loggingpixels;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Setter @Getter @Builder
public class LoggingPixelTestDataModel{

    private List<DataTypeChecks> loggingPixelTests = new ArrayList<>();

    @Getter @Setter
    public static class DataTypeChecks{
        private String key;
        private String valueDataType;
        private String valueRegex;
    }
}

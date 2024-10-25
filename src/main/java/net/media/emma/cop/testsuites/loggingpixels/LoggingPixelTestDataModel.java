package net.media.emma.cop.testsuites.loggingpixels;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoggingPixelTestDataModel {

    String userAgent;
    String environment;
    String publisherUrl;
    List<DataTypeChecks> loggingPixelTests;

    public LoggingPixelTestDataModel(String userAgent, String environment, String publisherUrl,
                                     List<DataTypeChecks> loggingPixelTests) {
        super();
        this.userAgent = userAgent;
        this.environment = environment;
        this.publisherUrl = publisherUrl;
        this.loggingPixelTests = loggingPixelTests;
    }

    public LoggingPixelTestDataModel() {
        super();
        // TODO Auto-generated constructor stub
    }

    public DataTypeChecks getDataTypeCheckFor(String key) {

        for (DataTypeChecks dataTypeChecks : loggingPixelTests) {
            if (dataTypeChecks.key.equals(key)) {
                return dataTypeChecks;
            }
        }

        return null;

    }

    @Getter
    @Setter
    public static class DataTypeChecks {

        public String key;
        public String valueDataType;
        public String valueRegex;
        public List<String> negativeStrings;
        public boolean notNull;
        public boolean mandatory;


    }


}

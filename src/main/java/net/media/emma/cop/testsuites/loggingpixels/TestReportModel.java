package net.media.emma.cop.testsuites.loggingpixels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @Builder @ToString
public class TestReportModel
{


        String networkCall;
        String key;
        Boolean dataTypeMatch;
        Boolean regexMatch;
        Boolean negativeValues;
        int countOfKey;

        @Override
        public String toString() {
                return "URL: " + getNetworkCall() + " KEY: " + getKey() +
                        " DATA TYPE MATCH: " + getDataTypeMatch() + " REGEX MATCH: " + getRegexMatch() + " COUNT: " + getCountOfKey() + " NEGATIVE PRESENT: " + getNegativeValues();
        }
}
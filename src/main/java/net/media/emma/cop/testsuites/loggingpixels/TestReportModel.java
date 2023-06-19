package net.media.emma.cop.testsuites.loggingpixels;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter @Setter @Builder @ToString
public class TestReportModel
{



        private List<String> networkCalls;
        private List<String> keys;
        private List<Boolean> dataTypeMatches;
        private List<Boolean> regexMatches;
        private List<Boolean> negativeValues;
        private List<Integer> countOfKeys;

        // Default constructor
        public TestReportModel() {
        }
        // Parameterized constructor
        public TestReportModel(List<String> networkCalls, List<String> keys, List<Boolean> dataTypeMatches,
                        List<Boolean> regexMatches, List<Boolean> negativeValues, List<Integer> countOfKeys) {
                this.networkCalls = networkCalls;
                this.keys = keys;
                this.dataTypeMatches = dataTypeMatches;
                this.regexMatches = regexMatches;
                this.negativeValues = negativeValues;
                this.countOfKeys = countOfKeys;
        }
        @Override
        public String toString(){
                    return "\nURL: " + getNetworkCalls() + "\nKEY: " + getKeys()  +
                        "\nDATA TYPE MATCH: " + getDataTypeMatches()  + "\nREGEX MATCH: " + getRegexMatches()  +
                        "\nCOUNT: " + getCountOfKeys()  + "\nNEGATIVE PRESENT: " + getNegativeValues()+ "\n-----------";
        }
}
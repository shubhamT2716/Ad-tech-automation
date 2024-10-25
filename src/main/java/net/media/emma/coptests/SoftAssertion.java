package net.media.emma.coptests;

import lombok.Getter;
import org.testng.asserts.SoftAssert;

@Getter
public class SoftAssertion {

    SoftAssert softAssert;

    public SoftAssert softAssert() {
        softAssert = new SoftAssert();
        return softAssert;
    }

}

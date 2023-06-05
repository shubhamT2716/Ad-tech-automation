package net.media.emma.coptests;

import org.testng.annotations.BeforeMethod;
import org.testng.asserts.SoftAssert;

public class SoftAssertion {

    SoftAssert softAssert;
    public SoftAssert softAssert() {
        softAssert = new SoftAssert();
        return softAssert;
    }
    public SoftAssert getSoftAssert(){
        return this.softAssert;
    }

}

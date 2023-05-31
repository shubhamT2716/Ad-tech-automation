package net.media.emma.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

public class ExtentTestManager {
    private static ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();
    private static ExtentReports extent = ExtentManager.getInstance();

    public static synchronized ExtentTest getTest() {
        return extentTestThreadLocal.get();
    }

    public static synchronized void setTest(ExtentTest test) {
        extentTestThreadLocal.set(test);
    }

    public static synchronized void removeTest() {
        extentTestThreadLocal.remove();
    }
}

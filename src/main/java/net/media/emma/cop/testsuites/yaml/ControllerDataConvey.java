package net.media.emma.cop.testsuites.yaml;

import java.util.List;

import org.springframework.stereotype.Service;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;

import net.media.emma.cop.testsuites.yaml.TestReportModel.BidExController;
import net.media.emma.cop.testsuites.yaml.TestReportModel.BidexData;

@Service
public interface ControllerDataConvey {

	public BidExController getBidExData(List<Request> listOfPwRequestObjects, String environment);

	public String getBidexSize(Request request);

	public BidexData getBidExData(Request request);
	
	public List<String> getConsoleErrors(Page page);
	
}

package net.media.emma.cop.testsuites.yaml;

import java.util.List;
import org.springframework.stereotype.Service;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import net.media.emma.cop.testsuites.yaml.TestReportModel.Controller;
import net.media.emma.cop.testsuites.yaml.TestReportModel.ControllerData;

@Service
public interface ControllerDataConvey {

	public Controller getBidExData(List<Request> listOfPwRequestObjects, String environment);

	public String getBidexSize(Request request);

	public ControllerData getBidExData(Request request);
	
	public List<String> getConsoleErrors(Page page);
	
}

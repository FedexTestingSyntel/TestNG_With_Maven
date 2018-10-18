package SupportClasses;

import java.util.ArrayList;    //The below needed for tracking the status of the tests.
import java.util.Properties;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import SupportClasses.ThreadLogger;
import TestingFunctions.Helper_Functions;
//https://www.guru99.com/pdf-emails-and-screenshot-of-test-reports-in-selenium.html

public class TestNG_TestListener implements ITestListener{
	
	@Override
    public void onStart(ITestContext arg0) {
    }
	
	@Override
    public void onTestStart(ITestResult arg0) {
    }

    @Override
    public void onTestSuccess(ITestResult arg0) {
    	TestResults(arg0);
    }

    @Override
    public void onTestFailure(ITestResult arg0) {
    	TestResults(arg0);
    }

    @Override
    public void onTestSkipped(ITestResult arg0) {
    	TestResults(arg0);
    }

    @Override
    public void onFinish(ITestContext arg0) {
    	Helper_Functions.PrintOut("\n\n", false);
		
		for (int i = 0 ; i < ThreadLogger.ThreadLog.size(); i++) {
			Helper_Functions.PrintOut(i + ") " + ThreadLogger.ThreadLog.get(i), false);
		}
		
		try {
			Helper_Functions.MoveOldScreenshots();//cleans up the screenshots.
			Runtime.getRuntime().exec("taskkill /F /IM ChromeDriver.exe");//close out the old processes if still present.
			Helper_Functions.PrintOut("ChromeDriver.exe Cleanup Executed", true);
		} catch (Exception e) {}

		Helper_Functions.MoveOldLogs();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
        // TODO Auto-generated method stub
    }
    
    private void TestResults(ITestResult arg0) {
    	DriverFactory.getInstance().removeDriver();
    	
    	String AttemptLogs = ThreadLogger.getInstance().ReturnLogString();
    	arg0.setAttribute("ExecutionLog", AttemptLogs);// this will save the trace in a collapsable format
    	//arg0.setAttribute("ExecutionLog", ThreadLogger.getInstance().ReturnLogString());
    	
    	ArrayList<String> CurrentLogs = ThreadLogger.getInstance().ReturnLogs();
    	String TestCompleteData = "";		
    	for (int i = 0; i < CurrentLogs.size(); i++){
		    TestCompleteData += CurrentLogs.get(i) + System.lineSeparator();
		}
    	ThreadLogger.ThreadLog.add(TestCompleteData + System.lineSeparator());
    }
}
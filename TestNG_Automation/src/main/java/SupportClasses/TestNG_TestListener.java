package SupportClasses;

import java.util.ArrayList;    //The below needed for tracking the status of the tests.
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import SupportClasses.ThreadLogger;
import TestingFunctions.Helper_Functions;

public class TestNG_TestListener implements ITestListener{
	
	@Override
    public void onStart(ITestContext arg0) {
		ArrayList<String[]> PersonalData = new ArrayList<String[]>();
		PersonalData = Helper_Functions.getExcelData(".\\Data\\Load_Your_UserIds.xls",  "Data");//create your own file with the specific data
		for(String s[]: PersonalData) {
			if(s[0].contentEquals("MYEMAIL")){
				Helper_Functions.MyEmail = s[1];
			}
		}
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
    	try {
			Helper_Functions.takeSnapShot("Failure " + arg0.getName() + " " + arg0.getEndMillis() + ".png");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
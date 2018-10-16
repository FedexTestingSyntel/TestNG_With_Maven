package TestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import jxl.Sheet;
import jxl.Workbook;
import org.testng.annotations.BeforeClass;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hamcrest.CoreMatchers;
import org.json.JSONObject;//json jar needed
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class MFAC_TestNG{
	static String LevelsToTest = "2"; //Can but updated to test multiple levels at once if needed. Setting to "23" will test both level 2 and level 3.
	final boolean TestExpiration = false;//flag to determine if the expiration scenarios should be tested. When set to false those tests will not be executed.
	
	static ArrayList<String> ResultsList = new ArrayList<String>();//Stores the test cases trace. Will be printed at end for easy debug
	static ArrayList<String[]> DataList = new ArrayList<String[]>();//Stores the data for each individual level, please see the setup function below for more details.
	static ArrayList<String> PrintLog = new ArrayList<String>();//Text that is printed to the console, used for creating log after completion.
	private static HttpClient httpclient = HttpClients.createDefault();//made static to speed up socket execution
	private final static Lock lock = new ReentrantLock();//to make sure the httpclient works with the parallel execution
	static Date ClassStart;
	
	@BeforeClass
	public void beforeClass() {		//implemented as a before class so the OAUTH tokens are only generated once.
		ArrayList<String[]> Excel_Data = getExcelData(".\\Data\\MFAC_Properties.xls",  "MFAC");//load the relevant information from excel file.
		ClassStart = new Date();
		for (int i=0; i<LevelsToTest.length(); i++) {
			int ExcelRow = Integer.parseInt(LevelsToTest.charAt(i) + "");//the rows will correspond to the correct level. With the row 0 being the column titles.
			//below is each column that is expected in the excel and will be loaded.    08/24/18
			//OAuthToken (Will be populated within the class)	Level	OAuthToken_URL	Client_ID	Client_Secret	IssuePin_APIGURL	VerifyPin_APIGURL	Velocity_APIGURL	IssuePin_DirectURL	VerifyPin_DirectURL	Velocity_DirectURL	Pin_Velocity_PostCard	Pin_Velocity_Phone	Address_Velocity
			String EnvironmentInformation[] = Excel_Data.get(ExcelRow);
			
			for (int j = 0; j < EnvironmentInformation.length; j++) {//added as a precaution to remove spaces from the excel sheet
				EnvironmentInformation[j] = EnvironmentInformation[j].trim();
			}
			
			EnvironmentInformation[0] = getAuthToken(EnvironmentInformation[2], EnvironmentInformation[3], EnvironmentInformation[4]);//add token to front of new array after it is generated
		    PrintOut(Arrays.toString(EnvironmentInformation), true);//print out all of the urls and date for the level, this is just a reference point to executer
			DataList.add(EnvironmentInformation);
		}
		PrintOut("Thread -- Time (MMDDYY'T'HHMMSS): -- Current progress", false);
	}
	
	@DataProvider (parallel = true) //make sure to add <suite name="..." data-provider-thread-count="12"> to the .xml for speed.
	public static Iterator<Object[]> dp(Method m) {
	    List<Object[]> data = new ArrayList<>();
		
		String Level, OAuth_Token = null, AIssueURL, AVerifyURL, AVelocityURL, DIssueURL, DVerifyURL, DVelocityURL, OrgPostcard = "FDM-POSTCARD-PIN", OrgPhone = "FDM-PHONE-PIN";
		int PinVelocityThresholdPostcard, PinVelocityThresholdPhone, AddressVelocityThreshold;
		for (int i=0; i < DataList.size(); i++) {
			String SetupTesting[] = DataList.get(i);    //This isn't strictly needed but makes easier to understand/trace.
			Level = SetupTesting[1];
			OAuth_Token = SetupTesting[0];
			AIssueURL = SetupTesting[5];
			AVerifyURL = SetupTesting[6];
			AVelocityURL = SetupTesting[7];
			DIssueURL = SetupTesting[8];
			DVerifyURL = SetupTesting[9];
			DVelocityURL = SetupTesting[10];
			PinVelocityThresholdPostcard = Integer.valueOf(SetupTesting[11]);
			PinVelocityThresholdPhone = Integer.valueOf(SetupTesting[12]);
			AddressVelocityThreshold = Integer.valueOf(SetupTesting[13]);
			
			switch (m.getName()) { //Based on the method that is being called the array list will be populated. This will make the TestNG Pass/Fail results more relevant.
    		case "AddressVelocity":
    			data.add(new Object[] {OrgPostcard, OAuth_Token, AVelocityURL, AddressVelocityThreshold});
    			data.add(new Object[] {OrgPhone, OAuth_Token, AVelocityURL, AddressVelocityThreshold});
    			if (!Level.contentEquals("6") && !Level.contentEquals("7")){
    				data.add(new Object[] {OrgPostcard, OAuth_Token, DVelocityURL, AddressVelocityThreshold});
    				data.add(new Object[] {OrgPhone, OAuth_Token, DVelocityURL, AddressVelocityThreshold});
    			}
    			break;
    		case "IssuePin":
    			data.add(new Object[] {OrgPostcard, OAuth_Token, AIssueURL});
    			data.add(new Object[] {OrgPhone, OAuth_Token, AIssueURL});
    			if (!Level.contentEquals("6") && !Level.contentEquals("7")){
    				data.add(new Object[] {OrgPostcard, OAuth_Token, DIssueURL});
    				data.add(new Object[] {OrgPhone, OAuth_Token, DIssueURL});
    			}
    			break;
    		case "DetermineLockoutTime"://only need to test API call as this is a helper test to determine current lockouts set.
    			data.add(new Object[] {OrgPostcard, OAuth_Token, AIssueURL});
    			data.add(new Object[] {OrgPhone, OAuth_Token, AIssueURL});
    			break;
    		case "IssuePinVelocity":
    			data.add(new Object[] {OrgPostcard, OAuth_Token, AIssueURL, PinVelocityThresholdPostcard});
    			data.add(new Object[] {OrgPhone, OAuth_Token, AIssueURL, PinVelocityThresholdPhone});
    			if (!Level.contentEquals("6") && !Level.contentEquals("7")){
    				data.add(new Object[] {OrgPostcard, OAuth_Token, DIssueURL, PinVelocityThresholdPostcard});
    				data.add(new Object[] {OrgPhone, OAuth_Token, DIssueURL, PinVelocityThresholdPhone});
    			}
    			break;
    		case "VerifyPinValid":
    		case "VerifyPinNoLongerValid":
    		case "IssuePinExpiration":
    			data.add(new Object[] {OrgPostcard, OAuth_Token, AIssueURL, AVerifyURL});
    			data.add(new Object[] {OrgPhone, OAuth_Token, AIssueURL, AVerifyURL});
    			if (!Level.contentEquals("6") && !Level.contentEquals("7")){
    				data.add(new Object[] {OrgPostcard, OAuth_Token, DIssueURL, DVerifyURL});
    				data.add(new Object[] {OrgPhone, OAuth_Token, DIssueURL, DVerifyURL});
    			}
    			break;
    		case "VerifyPinVelocity":
    			data.add(new Object[] {OrgPostcard, OAuth_Token, AIssueURL, AVerifyURL, PinVelocityThresholdPostcard});
    			data.add(new Object[] {OrgPhone, OAuth_Token, AIssueURL, AVerifyURL, PinVelocityThresholdPhone});
    			if (!Level.contentEquals("6") && !Level.contentEquals("7")){
    				data.add(new Object[] {OrgPostcard, OAuth_Token, DIssueURL, DVerifyURL, PinVelocityThresholdPostcard});
    				data.add(new Object[] {OrgPhone, OAuth_Token, DIssueURL, DVerifyURL, PinVelocityThresholdPhone});
    			}
    			break;
    		case "AdditionalEnrollmentExpiration":
    			data.add(new Object[] {OrgPostcard, OrgPhone, OAuth_Token, AIssueURL, AVerifyURL});
    			data.add(new Object[] {OrgPhone, OrgPostcard, OAuth_Token, AIssueURL, AVerifyURL});
    			if (!Level.contentEquals("6") && !Level.contentEquals("7")){
    				data.add(new Object[] {OrgPostcard, OrgPhone, OAuth_Token, DIssueURL, DVerifyURL});
    				data.add(new Object[] {OrgPhone, OrgPostcard, OAuth_Token, DIssueURL, DVerifyURL});
    			}
    			break;
			}//end switch MethodName
		}
		return data.iterator();
	}
	
	@Test(dataProvider = "dp")
	public void AddressVelocity(String OrgName, String OAuth_Token, String VelocityURL, int AddressVelocityThreshold) {//220496 Address Velocity
		String Result = "<-- AddressVelocity: Verify that the address velocity of " + AddressVelocityThreshold + " is reached and the correct error code is received.  (Org:" + OrgName + ", URL: "+ VelocityURL + ", Threshold: " + AddressVelocityThreshold + ")";
		String Status = "Scenario_Failed", UserName = UserName(), Buffer[];
		try {
			for (int i = 0; i < AddressVelocityThreshold; i++){
				Buffer = AddressVelocityAPI(UserName, OrgName, VelocityURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], containsString("ALLOW"));
			}
			
			Buffer = AddressVelocityAPI(UserName, OrgName, VelocityURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("DENY"), containsString("Unfortunately, too many failed attempts for registration have occurred. Please try again later.")));

			Status = "Scenario_Passed";
		}catch (Exception e){

		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp")
	public void IssuePin(String OrgName, String OAuth_Token, String IssueURL){//220459 IssuePin
		String Result = "<-- IssuePin: Verify that the user is able to request a pin.  (Org:" + OrgName + ", URL: "+ IssueURL + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName();
		
		try {
			Buffer = IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("pinOTP"), containsString("pinExpirationDate")));//pin should be generated//pin expiration time should be present.
			String Pin = ParsePIN(Buffer[1]);
			Integer.parseInt(Pin);
			Status = "Scenario_Passed";
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp")
	public void IssuePinVelocity(String OrgName, String OAuth_Token, String IssueURL, int PinVelocityThreshold){//220459 IssuePin
		String Result = "<-- IssuePinVelocity: Verify that the user can request up to " + PinVelocityThreshold + " pin numbers before unable to request more.  (Org:" + OrgName + ", URL: " + IssueURL + ", Threshold: " + PinVelocityThreshold + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName();
		
		try {
			for (int i = 0; i < PinVelocityThreshold; i++){
				Buffer = IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], CoreMatchers.allOf(containsString("pinOTP"), containsString("pinExpirationDate")));
			}
			Buffer = IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			//033018 - updated value from DENY to 5700, updated to match with what USRC uses.
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("5700"), containsString("Unfortunately, you have exceeded your attempts for verification. Please try again later.")));
			Status = "Scenario_Passed";
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@Test(dataProvider = "dp")
	public void VerifyPinValid(String OrgName, String OAuth_Token, String IssueURL, String VerifyURL){//220462 Verify Pin
		String Result = "<-- VerifyPinValid: Verify that user is able to request a pin and then verify that can recieve success when using the generated pin.  (Org:" + OrgName + ", URLs: " + IssueURL + ", " + VerifyURL + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName();
		
		try {
			Buffer =  IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinExpirationDate"));
			String Pin = ParsePIN(Buffer[1]);
			
			//Test verify pin on valid request
			Buffer = VerifyPinAPI(UserName, OrgName, Pin, VerifyURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("Success"));
			Status = "Scenario_Passed";
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp")
	public void VerifyPinVelocity(String OrgName, String OAuth_Token, String IssueURL, String VerifyURL, int PinVelocityThreshold){//220462 Verify Pin
		String Result = "<-- VerifyPinThreshold: When an invalid pin is entered the pin failure message should be returned passed the velocity threshold of " + PinVelocityThreshold + ".  (Org:" + OrgName + ", URLs: " + IssueURL + ", " + VerifyURL + ", Threshold: " + PinVelocityThreshold + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName();
		
		try {
			Buffer = IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinExpirationDate"));
			//Test verify pin on valid request
			for (int i = 0; i < PinVelocityThreshold + 2; i++) {
				Buffer = VerifyPinAPI(UserName, OrgName, "1111", VerifyURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], containsString("PIN.FAILURE"));
			}
			Status = "Scenario_Passed";
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp")
	public void VerifyPinNoLongerValid(String OrgName, String OAuth_Token, String IssueURL, String VerifyURL){//220462 Verify Pin
		String Result = "<-- VerifyPinNoLongerValid: Verify that when user requests a second pin that the first is no longer valid.  (Org:" + OrgName + ", URLs: " + IssueURL + ", " + VerifyURL + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName();
		
		try {
			Buffer = IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinOTP"));//just to make sure valid response
			String Pin = ParsePIN(Buffer[1]);
			Integer.parseInt(Pin);
			Buffer = IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinOTP"));//just to make sure valid response
			String PinTwo = ParsePIN(Buffer[1]);
			Integer.parseInt(PinTwo);
			//Test that the first pin is no longer valid
			Buffer = VerifyPinAPI(UserName, OrgName, Pin, VerifyURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("PIN.FAILURE"));
			//Test verify pin on valid request
			Buffer = VerifyPinAPI(UserName, OrgName, PinTwo, VerifyURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("Success"));
			Status = "Scenario_Passed";
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@SuppressWarnings("deprecation")  //add due to the date comparison 
	@Test(dataProvider = "dp", priority=1, enabled=TestExpiration)
	public void IssuePinExpiration(String OrgName, String OAuth_Token, String IssueURL, String VerifyURL){
		String Result = "<-- IssuePinExpiration: Verify that after a pin is expired it can no longer be used to complete registration.  (Org:" + OrgName + ", URLs: " + IssueURL + ", " + VerifyURL + ")";
		String Status = "Scenario_Failed", Buffer[] = null;
		String UserName = UserName();
		
		try {
			Buffer = IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinExpirationDate")); 
			
			Date CurrrentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Date ExpirationTime = GetExpiration(Buffer[1]);
		
			if (ExpirationTime.getDate() == CurrrentTime.getDate() && ExpirationTime.getMonth() == CurrrentTime.getMonth()) {
				Thread currentThread = Thread.currentThread();
				long ThreadID = currentThread.getId();
				SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");
				while (ExpirationTime.compareTo(CurrrentTime) == 1){
					try {Thread.sleep(60000);} catch (InterruptedException e) {e.printStackTrace();}
					CurrrentTime = new Date();
					System.out.println("(" + ThreadID + " C:" + TF.format(CurrrentTime) + "->E:" + TF.format(ExpirationTime) + ") Waiting for Expiration " + OrgName);   //added to keep watch from gui and see progress. Will not update into file
				};
				Buffer = VerifyPinAPI(UserName, OrgName, ParsePIN(Buffer[1]), VerifyURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], containsString("PIN.FAILURE"));
				Status = "Scenario_Passed";
			}else {
				String LongExpirationMessage = "Not Validing the Expiraiton at this time, need to verify seperatly once the expiration has passed. Here is the CST time it will expire. " + ExpirationTime;
				PrintOut(LongExpirationMessage, true);
				Result += "\n" + LongExpirationMessage;
			}
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@SuppressWarnings("deprecation")  //add due to the date comparison 
	@Test(dataProvider = "dp", priority=1, enabled=TestExpiration)
	public void AdditionalEnrollmentExpiration(String OrgName, String SecondOrg, String OAuth_Token, String IssueURL, String VerifyURL){
		String Result = "<-- AdditionalEnrollmentExpiration: Verify that the user recieves the updated expiration time when changing enrollment method..  (Org:" + OrgName + ", URLs: " + IssueURL + ", " + VerifyURL + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName(), Pin = null;
		
		try {
			Buffer = IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinExpirationDate"));
			
			Date CurrrentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Date ExpirationTime = GetExpiration(Buffer[1]);
			Date SecondExpirationTime = null;
			if (ExpirationTime.getDate() == CurrrentTime.getDate() && ExpirationTime.getMonth() == CurrrentTime.getMonth()) {
				Buffer =  IssuePinAPI(UserName, SecondOrg, IssueURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], containsString("pinExpirationDate"));
				Pin = ParsePIN(Buffer[1]);
				SecondExpirationTime = GetExpiration(Buffer[1]);
			}

			if (SecondExpirationTime == null) {
				String LongExpirationMessage = "Not Validing the Expiraiton at this time, need to verify seperatly once the expiration has passed. Here is the CST time it will expire. " + ExpirationTime;
				PrintOut(LongExpirationMessage, true);
				Result += "\n" + LongExpirationMessage;
			}else if (SecondExpirationTime.compareTo(ExpirationTime) == 1) { //make sure second expiration is after initial would expire
				Thread currentThread = Thread.currentThread();
				long ThreadID = currentThread.getId();
				SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");
				while (ExpirationTime.compareTo(CurrrentTime) == 1){
					try {Thread.sleep(60000);} catch (InterruptedException e) {e.printStackTrace();}
					CurrrentTime = new Date();
					System.out.println("(" + ThreadID + " C:" + TF.format(CurrrentTime) + "->E:" + TF.format(ExpirationTime) + ") Waiting for Expiration " + OrgName + " after switching enrollment.");   //added to keep watch from gui and see progress. Will not update into file
				};
				//Test verify pin on valid request from the different org
				Buffer = VerifyPinAPI(UserName, SecondOrg, Pin, VerifyURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], containsString("Success"));
				Status = "Scenario_Passed";
			}else {
				//Test verify pin on valid request from the different org, this will not wait for the old one to have expired.
				Buffer = VerifyPinAPI(UserName, SecondOrg, Pin, VerifyURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], containsString("Success"));
				Status = "Scenario_Passed";
			}
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@AfterClass
	public void afterClass() {
		ResultsList.sort(String::compareToIgnoreCase);//sort the test case trace alphabetical
		
		int passedcnt = 0, failedcnt = 0;
		PrintOut("\n\n@@ Passed Test Cases:", false);
		for(int i = 0; i < ResultsList.size(); i++) {
			if (ResultsList.get(i).contains("Scenario_Passed")) {
				passedcnt++;
				PrintOut(passedcnt + ")    " + ResultsList.get(i), false);
				ResultsList.remove(i);
				i--;
			}
		}
		
		if (ResultsList.size() > 0) {
			PrintOut("\n\n@@ Failed Test Cases:", false);
			for(String s : ResultsList) {
				failedcnt++;
				PrintOut(passedcnt + failedcnt + ")    " + s, false);
			}
			PrintOut("", false);
		}
		
		try {//print out the total execution time.
			Date ClassEnd = new Date();
			long diffInMillies = ClassEnd.getTime() - ClassStart.getTime();
			TimeUnit timeUnit = TimeUnit.SECONDS;
			PrintOut("Execution Time in seconds: " + timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS) + "  (" + diffInMillies + " miliseconds)", false);
		}catch (Exception e) {}
		
		PrintOut("Passed: " + passedcnt + " - " + "Failed: " + failedcnt + "\n", false);
		
		String ConsoleText = "";
		for (String s : PrintLog){
			ConsoleText += s + "\n";
		}
		
		WriteToFile(LevelsToTest, ConsoleText, ".\\EclipseScreenshots\\MFAC\\" + CurrentDateTime() + " L" + LevelsToTest + " MFAC");
		MoveOldLogs();
	}

	@AfterMethod
	public void afterMethod(ITestResult r) {
		try{
		    if(r.getStatus() == ITestResult.SUCCESS){
		    	PrintOut(r.getName() + " passed " + Arrays.toString(r.getParameters()), false);
		    }else if(r.getStatus() == ITestResult.FAILURE) {
		    	PrintOut(r.getName() + " failed " + Arrays.toString(r.getParameters()), false);
		    }else if(r.getStatus() == ITestResult.SKIP ){
		    	PrintOut(r.getName() + " skipped " + Arrays.toString(r.getParameters()), false);
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	//@Test(dataProvider = "dp")
	public void DetermineLockoutTime(String OrgName, String OAuth_Token, String IssueURL){
		String Buffer[] = null, UserName = UserName();
		int ExpiraitonMinutes = -1, PinThreshold = -1;
		do {
			Buffer = IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			PinThreshold++;
		}while (Buffer[1].contains("pinExpirationDate"));
		ResultsList.add(OrgName + " " + IssueURL + "  Can request " + PinThreshold + " pins before being lockout out.");
		PrintOut(OrgName + " " + IssueURL + "  Can request " + PinThreshold + " pins before being lockout out.", true);
			
		do {
			Buffer = IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			ExpiraitonMinutes++;
			PrintOut("Sleeping for a minute", true);
			try {Thread.sleep(6000);} catch (Exception e) {}
		}while (!Buffer[1].contains("pinExpirationDate"));
		ResultsList.add(OrgName + " " + IssueURL + "Can request additional pins after " + ExpiraitonMinutes + " minutes. ");
		PrintOut(OrgName + " " + IssueURL + "Can request additional pins after " + ExpiraitonMinutes + " minutes. ", true);
	}
	
	///////////////////////////////////METHODS//////////////////////////////////
	
	public String[] AddressVelocityAPI(String UserName, String OrgName, String URL, String OAuth_Token){
		String Request = null;
		
		try{
			HttpPost httppost = new HttpPost(URL);
			
			Request = new JSONObject()
		        .put("userName", UserName)
		        .put("orgName", OrgName).toString();
			PrintOut("AddressVelocity Request: " + Request, true);
			
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Authorization", "Bearer " + OAuth_Token);
			StringEntity params = new StringEntity(Request.toString());
			httppost.setEntity(params);
			String Response = HTTPCall(httppost);
			PrintOut("AddressVelocity Response: " + Response, true);
			
			return new String[] {Request,Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
		 	//Sample request:   {"orgName":"FDM-POSTCARD-PIN","userName":"32t48NRQ0A-hOvDu6EgstCYeRiyKMDpKZzN"}
			//Sample Successful Response: {"transactionId":"6166229d-c0cf-416a-92e2-0884927b9fa6","output":{"advice":"ALLOW"}}
			//Sample velocity threshold Response: {"transactionId":"9e15ab6a-885b-45fb-8cbd-c641cfeac075","errors":[{"code":"DENY","message":"Unfortunately, too many failed attempts for registration have occurred. Please try again later."}]}
	}
	
	public String[] IssuePinAPI(String UserName, String OrgName, String URL, String OAuth_Token){
		String Request = null;
		try{
			HttpPost httppost = new HttpPost(URL);

			Request = new JSONObject()
		        .put("userName", UserName)
		        .put("orgName", OrgName).toString();
			PrintOut("IssuePin Request: " + Request, true);
			
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Authorization", "Bearer " + OAuth_Token);
			StringEntity params = new StringEntity(Request.toString());
			httppost.setEntity(params);
			String Response = HTTPCall(httppost);
			PrintOut("IssuePin Response: " + Response, true);
			return new String[] {Request, Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
	}
	
	public String[] VerifyPinAPI(String UserName, String OrgName, String Pin, String URL, String OAuth_Token){
		String Request = null;
		try{
			HttpPost httppost = new HttpPost(URL);
			
			Request = new JSONObject()
		        .put("userName", UserName)
		        .put("orgName", OrgName)
		        .put("otpInput", Pin).toString();
			PrintOut("VerifyPin Request: " + Request, true);
			
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Authorization", "Bearer " + OAuth_Token);
			StringEntity params = new StringEntity(Request.toString());
			httppost.setEntity(params);
			String Response = HTTPCall(httppost);
			PrintOut("VerifyPin Response: " + Response, true);
			return new String[] {Request, Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
	}
		
	public static String HTTPCall(HttpPost request) throws Exception {
		lock.lock();
		try {
			HttpResponse httpresponse = httpclient.execute(request);
			String Response = EntityUtils.toString(httpresponse.getEntity());
			return Response;
		}catch (Exception e) {
			return e.getMessage() + e.getCause();
		}finally {
			lock.unlock();
		} 
	}
	
	public static String ParsePIN(String s) {
		if(s.contains("pinOTP") && s.contains("pinExpirationDate")) {
			return s.substring(s.indexOf("pinOTP\":") + 9, s.indexOf("\",\"pinExpirationDate"));
		}
		return null;
	}
	
	public static String AddToResult(String ReqResp[]) {
		if (ReqResp[0].contains("Scenario_Passed") || ReqResp[0].contains("Scenario_Failed")) {
			return System.lineSeparator() + ReqResp[0] + System.lineSeparator();
		}else {
			return System.lineSeparator() + "Request: " + ReqResp[0] + System.lineSeparator() + "Response: " + ReqResp[1];
		}
		
	}
	
	public static Date GetExpiration(String s) {
		StringTokenizer st = new StringTokenizer(s,"\"");  
		while (st.hasMoreElements()) {
			if (st.nextToken().contentEquals("pinExpirationDate")) { //pinExpirationDate":"04/03/2018 02:50 PM GMT"
				st.nextToken();//Move past the ":" token
				break;
			}
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date ExpirationTime = null; 
		String ExpirationString = null;
		try {
			ExpirationString = st.nextToken();
			ExpirationTime = dateFormat.parse(ExpirationString);
		}catch (Exception e) {
			Assert.fail("Expiration time is not in valid format." + ExpirationString);
		}//		4/03/2018 02:50 PM GMT
		return ExpirationTime;
	}
	
	///////Helper Functions///////////////Condensed into single class	
	public static String getAuthToken(String URL, String clientIden, String clientSec) {
		//String authTokenURI = "https://api" + LevelURL + ":8443/auth/oauth/v2/token";
		//if (Level.contentEquals("4") || Level.contentEquals("6") || Level.contentEquals("7")) {
		//	authTokenURI = "https://api" + LevelURL + "/auth/oauth/v2/token";
		//}
		//	changed authTokenURI to URL
		
		PrintOut("OAuth: " + URL + "  Iden: " + clientIden + "  Secret: " + clientSec, true);
		
		//ex https://apidev.idev.fedex.com:8443/auth/oauth/v2/token 
		String grantType = "client_credentials";
		String scope = "oob";
		String response = "";
		try {
			String queryParam = "grant_type=" + grantType + "&client_id=" + clientIden + "&client_secret=" + clientSec + "&scope=" + scope;
			String method = "POST";
			URL serviceUrl = new URL(URL);

			HttpsURLConnection con = (HttpsURLConnection) serviceUrl.openConnection();
			con.setRequestMethod(method);

			String urlParameters = queryParam;
			StringBuffer response_buff = new StringBuffer();
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response_buff.append(inputLine);
			}
			in.close();
			response = response_buff.toString();
			
			if (response.contains("access_token") && response.contains("token_type")) {
					//{  "access_token":"ddea4340-d1e1-46bf-94b0-1271c49aa1a0",  "token_type":"Bearer",  "expires_in":3600,  "scope":"oob"}
					String start = "access_token\":\"";
					String end = "\",  \"token_type\"";
					String token = response.substring(response.indexOf(start) + start.length(), response.indexOf(end));
					return token;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}//end getAuthToken
	
	public static String UserName() {
		return getRandomString(10) + "-" + getRandomString(24);
	}

	public static ArrayList<String[]> getExcelData(String fileName, String sheetName) {
		//Note, may face issues if the file is an .xlsx, save it as a xls and works

		ArrayList<String[]> data = new ArrayList<>();
		try {
			FileInputStream fs = new FileInputStream(fileName);
			Workbook wb = Workbook.getWorkbook(fs);
			Sheet sh = wb.getSheet(sheetName);
			
			int totalNoOfCols = sh.getColumns();
			int totalNoOfRows = sh.getRows();
			
			for (int i= 0 ; i < totalNoOfRows; i++) { //change to start at 1 if want to ignore the first row.
				String buffer[] = new String[totalNoOfCols];
				for (int j=0; j < totalNoOfCols; j++) {
					buffer[j] = sh.getCell(j, i).getContents();
				}
				data.add(buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	public static void PrintOut(String Text, boolean TimeStamp){
		if (TimeStamp) {
			String CurrentTime = CurrentDateTime();
			Thread currentThread = Thread.currentThread();
			long ThreadID = currentThread.getId();//not sure if should keep this
			Text = ThreadID + " " + CurrentTime + ": " + Text;
			
		}
		System.out.println(Text); 
		PrintLog.add(Text);
		Text = Text.replaceAll("\n", System.lineSeparator());
	}

	public static String CurrentDateTime() {
		Date curDate = new Date();
    	SimpleDateFormat Dateformatter = new SimpleDateFormat("MMddyy");
    	SimpleDateFormat Timeformatter = new SimpleDateFormat("HHmmss");
    	return Dateformatter.format(curDate) + "T" + Timeformatter.format(curDate);
	}

	public static String getRandomString(int Length) {
        //String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		String SALTCHARS = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < Length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

	public static void WriteToFile(String Levels, String Text, String Path){	
		//this is only applicable if configured for tests that print the summery table.
		if (Text.contains("Passed: ") && Text.contains(" - Failed: ")) {
			String Results = Text.substring(Text.indexOf("Passed: "), Text.length());
			if (Results.length() < 28 && Results.contains(" - Failed: ")) {//check in case pulled to many characters
				Results = Results.replace("Passed: ", " - P");
				Results = Results.replace(" - Failed: ", "F");
				Results = Results.replaceAll("\r", "");
				Results = Results.replaceAll("\n", "");
				Path = Path.replace(".txt", "");
				Path += Results;
			}
		}
		if (!Path.contains(".txt")) {
			Path+= ".txt";
		}
		File newTextFile = new File(Path);
		FileWriter fw;
		try {
			String Folder = Path.substring(0, Path.lastIndexOf("\\"));
			if (!(new File(Folder)).exists()) {
				new File(Folder).mkdir();
			}
			newTextFile.createNewFile();
			fw = new FileWriter(newTextFile);
			fw.write(Text);
	        fw.close();
	        PrintOut("File Created - " + newTextFile, false);
		} catch (Exception e) {  
			PrintOut("Failure writing to file.", true);
			e.printStackTrace();
		}
	}
	
	public static void MoveOldLogs(){
    	String main_Source = "." + File.separator + "EclipseScreenshots";
    	File main_dir = new File(main_Source);
    	if(main_dir.isDirectory()) {
    	    File[] content_main = main_dir.listFiles();
    	    for(int j = 0; j < content_main.length; j++) {
    	    	try {
    	        	String PathSource = content_main[j].getPath();
    	        	String PathDestination = content_main[j].getPath() + File.separator + "Old";
    	        	File source_dir = new File(PathSource);
    	        	File destination_dir = new File(PathDestination);
    	        	int year = Calendar.getInstance().get(Calendar.YEAR);
    	        	int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
    	        	int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    	        	if(source_dir.isDirectory()) {
    	        	    File[] content_subfolder = source_dir.listFiles();
    	        	    for(int i = 0; i < content_subfolder.length; i++) {
    	        	    	if (content_subfolder[i].isDirectory()){
    	        	    		break; //do not need to seach within subfolders.
    	        	    	}
    	        	    	
    	        	    	try {
    	        	        	if (!destination_dir.exists()) {// if the directory does not exist, create it
    	        	        	    PrintOut("Creating directory: " + destination_dir.getName(), true);
    	        	        	    try{
    	        	        	    	destination_dir.mkdir();
    	        	        	        PrintOut(PathDestination + " DIR created", true);  
    	        	        	    } catch(SecurityException se){}        
    	        	        	}
    	        	        	BasicFileAttributes attr = Files.readAttributes(Paths.get(content_subfolder[i].getPath()), BasicFileAttributes.class);
    	        	        	String creationtime = " " + attr.creationTime();
    	        	        	if (!creationtime.contains(Integer.toString(year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day))){//if the file was created before today
    	        	        		String localPathDestination = PathDestination + File.separator + creationtime.substring(1, 8);
    	        	        		File old_month_dir = new File(localPathDestination);
    	        	        		if (!old_month_dir.exists()) {// if the directory does not exist for the old month then create it
    	            	        	    PrintOut("Creating directory: " + old_month_dir.getName(), true);
    	            	        	    try{
    	            	        	    	old_month_dir.mkdir();
    	            	        	        PrintOut(localPathDestination + " DIR created", true);  
    	            	        	    } catch(SecurityException se){}
    	            	        	}
    	        	        		//Files.move(from, to, CopyOption... options).
    	        	        		Files.move(Paths.get(content_subfolder[i].getPath()), Paths.get(content_subfolder[i].getPath().replace(PathSource, localPathDestination)), StandardCopyOption.REPLACE_EXISTING);
    	        	        	}
    	    				}catch (Exception e) {}
    	        	    }
    	        	}
				}catch (Exception e) {}
    	    }//end for finding each individual app
    	}
    }//end MoveOldLogs
	
}
package TestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.hamcrest.CoreMatchers;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import API_Calls.*;
import Data_Structures.*;
import Helper.Support_Functions;

public class MFAC{
	static String LevelsToTest = "12346"; //Can but updated to test multiple levels at once if needed. Setting to "23" will test both level 2 and level 3.
	final boolean TestExpiration = true;//flag to determine if the expiration scenarios should be tested. When set to false those tests will not be executed.
	
	static MFAC_Data DataClass[] = new MFAC_Data[8];//Stores the data for each individual level, please see the before class function below for more details.
	static ArrayList<String> ResultsList = new ArrayList<String>();//Stores the test cases trace. Will be printed at end for easy debug
	static ArrayList<String[]> ExpirationData = new ArrayList<String[]>();
	static Date ClassStart;
	
	@BeforeClass
	public void beforeClass() {		//implemented as a before class so the OAUTH tokens are only generated once.
		ArrayList<String[]> Excel_Data = Support_Functions.getExcelData(".\\Data\\MFAC_Properties.xls",  "MFAC");//load the relevant information from excel file.
		ClassStart = new Date();
		for (int i=0; i<LevelsToTest.length(); i++) {
			int ExcelRow = Integer.parseInt(LevelsToTest.charAt(i) + "");//the rows will correspond to the correct level. With the row 0 being the column titles.
			//below is each column that is expected in the excel and will be loaded.    08/24/18
			//OAuthToken (Will be populated within the class)	Level	OAuthToken_URL	Client_ID	Client_Secret	IssuePin_APIGURL	VerifyPin_APIGURL	Velocity_APIGURL	IssuePin_DirectURL	VerifyPin_DirectURL	Velocity_DirectURL	Pin_Velocity_PostCard	Pin_Velocity_Phone	Address_Velocity
			String EnvironmentInformation[] = Excel_Data.get(ExcelRow);
			
			for (int j = 0; j < EnvironmentInformation.length; j++) {//added as a precaution to remove spaces from the excel sheet
				EnvironmentInformation[j] = EnvironmentInformation[j].trim();
			}
			
			EnvironmentInformation[0] = General_API_Calls.getAuthToken(EnvironmentInformation[2], EnvironmentInformation[3], EnvironmentInformation[4]);//add token to front of new array after it is generated
			Support_Functions.PrintOut(Arrays.toString(EnvironmentInformation), true);//print out all of the urls and date for the level, this is just a reference point to executer
		    
		    DataClass[ExcelRow] = new MFAC_Data();
		    DataClass[ExcelRow].OAuth_Token = EnvironmentInformation[0];
		    DataClass[ExcelRow].Level = EnvironmentInformation[1];
		    DataClass[ExcelRow].AIssueURL = EnvironmentInformation[5];
		    DataClass[ExcelRow].AVerifyURL = EnvironmentInformation[6];
		    DataClass[ExcelRow].AVelocityURL = EnvironmentInformation[7];
		    DataClass[ExcelRow].DIssueURL = EnvironmentInformation[8];
		    DataClass[ExcelRow].DVerifyURL = EnvironmentInformation[9];
		    DataClass[ExcelRow].DVelocityURL = EnvironmentInformation[10];
		    DataClass[ExcelRow].PinVelocityThresholdPostcard = Integer.valueOf(EnvironmentInformation[11]);
		    DataClass[ExcelRow].PinVelocityThresholdPhone = Integer.valueOf(EnvironmentInformation[12]);
		    DataClass[ExcelRow].AddressVelocityThreshold = Integer.valueOf(EnvironmentInformation[13]);
		}
		Support_Functions.PrintOut("Thread -- Time (MMDDYY'T'HHMMSS): -- Current progress", false);
	}
	
	@DataProvider (parallel = true) //make sure to add <suite name="..." data-provider-thread-count="12"> to the .xml for speed.
	public static Iterator<Object[]> dp(Method m) {
	    List<Object[]> data = new ArrayList<>();
		
		for (int i = 1; i < 8; i++) {
			if (DataClass[i] != null) {
				MFAC_Data c = DataClass[i];
				switch (m.getName()) { //Based on the method that is being called the array list will be populated. This will make the TestNG Pass/Fail results more relevant.
				case "AddressVelocity":
					if (!c.Level.contentEquals("1")){
						data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.AVelocityURL, c.AddressVelocityThreshold});
						data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.AVelocityURL, c.AddressVelocityThreshold});
					}
	    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
	    				data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.DVelocityURL, c.AddressVelocityThreshold});
	    				data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.DVelocityURL, c.AddressVelocityThreshold});
	    			}
	    			break;
	    		case "IssuePin":
	    			if (!c.Level.contentEquals("1")){
	    				data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.AIssueURL});
	    				data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.AIssueURL});
	    			}
	    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
	    				data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.DIssueURL});
	    				data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.DIssueURL});
	    			}
	    			break;
	    		case "DetermineLockoutTime"://only need to test API call as this is a helper test to determine current lockouts set.
	    			data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.AIssueURL});
	    			data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.AIssueURL});
	    			break;
	    		case "IssuePinVelocity":
	    			if (!c.Level.contentEquals("1")){
	    				data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.AIssueURL, c.PinVelocityThresholdPostcard});
	    				data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.AIssueURL, c.PinVelocityThresholdPhone});
	    			}
	    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
	    				data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.DIssueURL, c.PinVelocityThresholdPostcard});
	    				data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.DIssueURL, c.PinVelocityThresholdPhone});
	    			}
	    			break;
	    		case "VerifyPinValid":
	    		case "VerifyPinNoLongerValid":
	    		case "IssuePinExpiration":
	    			if (!c.Level.contentEquals("1")){
	    				data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.AIssueURL, c.AVerifyURL});
	    				data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.AIssueURL, c.AVerifyURL});	
	    			}
	    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
	    				data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.DIssueURL, c.DVerifyURL});
	    				data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.DIssueURL, c.DVerifyURL});
	    			}
	    			break;
	    		case "VerifyPinVelocity":
	    			if (!c.Level.contentEquals("1")){
	    				data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.AIssueURL, c.AVerifyURL, c.PinVelocityThresholdPostcard});
	    				data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.AIssueURL, c.AVerifyURL, c.PinVelocityThresholdPhone});
	    			}
	    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
	    				data.add(new Object[] {c.OrgPostcard, c.OAuth_Token, c.DIssueURL, c.DVerifyURL, c.PinVelocityThresholdPostcard});
	    				data.add(new Object[] {c.OrgPhone, c.OAuth_Token, c.DIssueURL, c.DVerifyURL, c.PinVelocityThresholdPhone});
	    			}
	    			break;
	    		case "AdditionalEnrollmentExpiration":
	    			if (!c.Level.contentEquals("1")){
	    				data.add(new Object[] {c.OrgPostcard, c.OrgPhone, c.OAuth_Token, c.AIssueURL, c.AVerifyURL});
	    				data.add(new Object[] {c.OrgPhone, c.OrgPostcard, c.OAuth_Token, c.AIssueURL, c.AVerifyURL});
	    			}
	    			if (!c.Level.contentEquals("6") && !c.Level.contentEquals("7")){
	    				data.add(new Object[] {c.OrgPostcard, c.OrgPhone, c.OAuth_Token, c.DIssueURL, c.DVerifyURL});
	    				data.add(new Object[] {c.OrgPhone, c.OrgPostcard, c.OAuth_Token, c.DIssueURL, c.DVerifyURL});
	    			}
	    			break;
	    		case "IssuePinExpirationValidate":
	    			ExpirationData.sort((o1, o2) -> o1[1].compareTo(o2[1]));
	    			for (int j = 0 ; j < ExpirationData.size(); j++) {
	    				if (ExpirationData.get(j)[0].contains("IssuePinExpiration")) {
	    					data.add(ExpirationData.get(j));
	    					ExpirationData.remove(j);
	    				}
	    			}
	    			break;	
	    		case "AdditionalEnrollmentExpirationValidate":
	    			ExpirationData.sort((o1, o2) -> o1[1].compareTo(o2[1]));
	    			for (int j = 0 ; j < ExpirationData.size(); j++) {
	    				if (ExpirationData.get(j)[0].contains("AdditionalEnrollmentExpiration")) {
	    					data.add(ExpirationData.get(j));
	    					ExpirationData.remove(j);
	    				}
	    			}
	    			break;	
				}//end switch MethodName
			}
		}
		return data.iterator();
	}
	
	@Test(dataProvider = "dp", priority = 2)
	public void AddressVelocity(String OrgName, String OAuth_Token, String VelocityURL, int AddressVelocityThreshold) {//220496 Address Velocity
		String Result = String.format("<-- AddressVelocity: Verify that the address velocity of %s is reached and the correct error code is received. This is to replicate to0 many requests for pin at a given address.    (Org: %s, URL: %s, Threshold: %s)", AddressVelocityThreshold, OrgName, VelocityURL, AddressVelocityThreshold);
		String Status = "Scenario_Failed", UserName = UserName(), Buffer[];
		try {
			for (int i = 0; i < AddressVelocityThreshold; i++){
				Buffer = MFAC_API_Endpoints.AddressVelocityAPI(UserName, OrgName, VelocityURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], containsString("ALLOW"));
			}
			
			Buffer = MFAC_API_Endpoints.AddressVelocityAPI(UserName, OrgName, VelocityURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("DENY"), containsString("Unfortunately, too many failed attempts for registration have occurred. Please try again later.")));

			Status = "Scenario_Passed";
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp", priority = 2)
	public void IssuePin(String OrgName, String OAuth_Token, String IssueURL){//220459 IssuePin
		String Result = "<-- IssuePin: Verify that the user is able to request a pin.  (Org:" + OrgName + ", URL: "+ IssueURL + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName();
		
		try {
			Buffer = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
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
	
	@Test(dataProvider = "dp", priority = 2)
	public void IssuePinVelocity(String OrgName, String OAuth_Token, String IssueURL, int PinVelocityThreshold){//220459 IssuePin
		String Result = "<-- IssuePinVelocity: Verify that the user can request up to " + PinVelocityThreshold + " pin numbers before unable to request more.  (Org:" + OrgName + ", URL: " + IssueURL + ", Threshold: " + PinVelocityThreshold + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName();
		
		try {
			for (int i = 0; i < PinVelocityThreshold; i++){
				Buffer = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], CoreMatchers.allOf(containsString("pinOTP"), containsString("pinExpirationDate")));
			}
			Buffer = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			//033018 - updated value from DENY to 5700, updated to match with what USRC uses.
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("5700"), containsString("Unfortunately, you have exceeded your attempts for verification. Please try again later.")));
			Status = "Scenario_Passed";
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@Test(dataProvider = "dp", priority = 2)
	public void VerifyPinValid(String OrgName, String OAuth_Token, String IssueURL, String VerifyURL){//220462 Verify Pin
		String Result = "<-- VerifyPinValid: Verify that user is able to request a pin and then verify that can recieve success when using the generated pin.  (Org:" + OrgName + ", URLs: " + IssueURL + ", " + VerifyURL + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName();
		
		try {
			Buffer =  MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinExpirationDate"));
			String Pin = ParsePIN(Buffer[1]);
			
			//Test verify pin on valid request
			Buffer = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, Pin, VerifyURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("Success"));
			Status = "Scenario_Passed";
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp", priority = 2)
	public void VerifyPinVelocity(String OrgName, String OAuth_Token, String IssueURL, String VerifyURL, int PinVelocityThreshold){//220462 Verify Pin
		String Result = "<-- VerifyPinThreshold: When an invalid pin is entered the pin failure message should be returned passed the velocity threshold of " + PinVelocityThreshold + ".  (Org:" + OrgName + ", URLs: " + IssueURL + ", " + VerifyURL + ", Threshold: " + PinVelocityThreshold + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName();
		
		try {
			Buffer = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinExpirationDate"));
			//Test verify pin on valid request
			for (int i = 0; i < PinVelocityThreshold + 2; i++) {
				Buffer = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, "1111", VerifyURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], containsString("PIN.FAILURE"));
			}
			Status = "Scenario_Passed";
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp", priority = 2)
	public void VerifyPinNoLongerValid(String OrgName, String OAuth_Token, String IssueURL, String VerifyURL){//220462 Verify Pin
		String Result = "<-- VerifyPinNoLongerValid: Verify that when user requests a second pin that the first is no longer valid.  (Org:" + OrgName + ", URLs: " + IssueURL + ", " + VerifyURL + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName();
		
		try {
			Buffer = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinOTP"));//just to make sure valid response
			String Pin = ParsePIN(Buffer[1]);
			Integer.parseInt(Pin);
			Buffer = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinOTP"));//just to make sure valid response
			String PinTwo = ParsePIN(Buffer[1]);
			Integer.parseInt(PinTwo);
			//Test that the first pin is no longer valid
			Buffer = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, Pin, VerifyURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("PIN.FAILURE"));
			//Test verify pin on valid request
			Buffer = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, PinTwo, VerifyURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("Success"));
			Status = "Scenario_Passed";
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@SuppressWarnings("deprecation")  //add due to the date comparison 
	@Test(dataProvider = "dp", enabled = TestExpiration, priority = 1)
	public void IssuePinExpiration(String OrgName, String OAuth_Token, String IssueURL, String VerifyURL){
		String Result = "<-- IssuePinExpiration: Verify that after a pin is expired it can no longer be used to complete registration.  (Org:" + OrgName + ", URLs: " + IssueURL + ", " + VerifyURL + ")";
		String Status = "Scenario_Failed", Buffer[] = null;
		String UserName = UserName();
		
		try {
			Buffer = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinExpirationDate")); 
			
			Date CurrrentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Date ExpirationTime = GetExpiration(Buffer[1]);
		
			if (ExpirationTime.getDate() == CurrrentTime.getDate() && ExpirationTime.getMonth() == CurrrentTime.getMonth()) {
				String Expiration[] = new String[] {Result, dateFormat.format(ExpirationTime).toString(), UserName, OrgName, ParsePIN(Buffer[1]), VerifyURL, OAuth_Token, "PIN.FAILURE"};
				ExpirationData.add(Expiration);
				Result += AddToResult(new String[] {"Will be validated after expiration in later test. --IssuePinExpirationValidate--", UserName});
				Status = "Scenario_Passed";
				//throw new SkipException("Will be validated after expiration in later test. --IssuePinExpirationValidate--");
			}else {
				String LongExpirationMessage = "Not Validing the Expiraiton at this time, need to verify seperatly once the expiration has passed. Here is the CST time it will expire. " + ExpirationTime;
				Support_Functions.PrintOut(LongExpirationMessage, true);
				Result += "\n" + LongExpirationMessage;
			}
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@SuppressWarnings("deprecation")  //add due to the date comparison 
	@Test(dataProvider = "dp", enabled = TestExpiration, priority = 1)
	public void AdditionalEnrollmentExpiration(String OrgName, String SecondOrg, String OAuth_Token, String IssueURL, String VerifyURL){
		String Result = "<-- AdditionalEnrollmentExpiration: Verify that the user recieves the updated expiration time when changing enrollment method..  (Org:" + OrgName + ", URLs: " + IssueURL + ", " + VerifyURL + ")";
		String Status = "Scenario_Failed", Buffer[] = null, UserName = UserName(), Pin = null;
		
		try {
			Buffer = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString("pinExpirationDate"));
			
			Date CurrrentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Date ExpirationTime = GetExpiration(Buffer[1]);
			Date SecondExpirationTime = null;
			if (ExpirationTime.getDate() == CurrrentTime.getDate() && ExpirationTime.getMonth() == CurrrentTime.getMonth()) {
				Buffer =  MFAC_API_Endpoints.IssuePinAPI(UserName, SecondOrg, IssueURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], containsString("pinExpirationDate"));
				Pin = ParsePIN(Buffer[1]);
				SecondExpirationTime = GetExpiration(Buffer[1]);
			}

			if (SecondExpirationTime == null) {
				String LongExpirationMessage = "Not Validing the Expiraiton at this time, need to verify seperatly once the expiration has passed. Here is the CST time it will expire. " + ExpirationTime;
				Support_Functions.PrintOut(LongExpirationMessage, true);
				Result += "\n" + LongExpirationMessage;
			}else if (SecondExpirationTime.compareTo(ExpirationTime) == 1) { //make sure second expiration is after initial would expire
				String Expire[] = new String[] {Result, dateFormat.format(ExpirationTime).toString(), UserName, SecondOrg, Pin, VerifyURL, OAuth_Token, "Success"};
				ExpirationData.add(Expire);
				Result += AddToResult(new String[] {"Will be validated after expiration in later test --AdditionalEnrollmentExpirationValidate--", UserName});
				Status = "Scenario_Passed";
				//throw new SkipException("Will be validated after expiration in later test --AdditionalEnrollmentExpirationValidate--");
			}else {
				//Test verify pin on valid request from the different org, this will not wait for the old one to have expired.
				Buffer = MFAC_API_Endpoints.VerifyPinAPI(UserName, SecondOrg, Pin, VerifyURL, OAuth_Token);
				Result += AddToResult(Buffer);
				assertThat(Buffer[1], containsString("Success"));
				Status = "Scenario_Passed";
			}
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@Test(dataProvider = "dp",enabled = TestExpiration, priority = 3)
	public void IssuePinExpirationValidate(String Result, String ExpirationResponse, String UserName, String OrgName, String Pin, String VerifyURL, String OAuth_Token, String Expected){
		String Status = "Scenario_Failed", Buffer[] = null;
		
		try {
			Date CurrrentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Date ExpirationTime = null;
			try {
				ExpirationTime = dateFormat.parse(ExpirationResponse);
			} catch (Exception e1) {
				e1.printStackTrace();
			};
			
			Thread currentThread = Thread.currentThread();
			long ThreadID = currentThread.getId();
			SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");
			while (ExpirationTime.compareTo(CurrrentTime) == 1){
				try {Thread.sleep(60000);} catch (InterruptedException e) {e.printStackTrace();}
				CurrrentTime = new Date();
				System.out.println("(" + ThreadID + " C:" + TF.format(CurrrentTime) + "->E:" + TF.format(ExpirationTime) + ") Waiting for Expiration " + OrgName );   //added to keep watch from gui and see progress. Will not update into file
			};

			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Buffer = new String[]{"Attempting to validate after expiration time has passed", "Current Time: " + CurrrentTime};
			Result += AddToResult(Buffer);
			//Test verify pin on valid request from the different org
			Buffer = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, Pin, VerifyURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString(Expected));//expected will either be success of pin failure based on scenario.
			Status = "Scenario_Passed";
			
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@Test(dataProvider = "dp",enabled = TestExpiration, priority = 3)
	public void AdditionalEnrollmentExpirationValidate(String Result, String ExpirationResponse, String UserName, String OrgName, String Pin, String VerifyURL, String OAuth_Token, String Expected){
		String Status = "Scenario_Failed", Buffer[] = null;
		
		try {
			Date CurrrentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Date ExpirationTime = null;
			try {
				ExpirationTime = dateFormat.parse(ExpirationResponse);
			} catch (Exception e1) {
				e1.printStackTrace();
			};
			
			Thread currentThread = Thread.currentThread();
			long ThreadID = currentThread.getId();
			SimpleDateFormat TF = new SimpleDateFormat("HH:mm:ss");
			while (ExpirationTime.compareTo(CurrrentTime) == 1){
				try {Thread.sleep(60000);} catch (InterruptedException e) {e.printStackTrace();}
				CurrrentTime = new Date();
				System.out.println("(" + ThreadID + " C:" + TF.format(CurrrentTime) + "->E:" + TF.format(ExpirationTime) + ") Waiting for Expiration " + OrgName );   //added to keep watch from gui and see progress. Will not update into file
			};
			
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			dateFormat.format(CurrrentTime);
			Buffer = new String[]{"Attempting to validate after expiraiton time has passed", "Current Time: " + CurrrentTime};
			Result += AddToResult(Buffer);
			//Test verify pin on valid request from the different org
			Buffer = MFAC_API_Endpoints.VerifyPinAPI(UserName, OrgName, Pin, VerifyURL, OAuth_Token);
			Result += AddToResult(Buffer);
			assertThat(Buffer[1], containsString(Expected));//expected will either be success of pin failure based on scenario.
			Status = "Scenario_Passed";
			
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@AfterClass
	public void afterClass() {
		ResultsList.sort(String::compareToIgnoreCase);//sort the test case trace alphabetical
		
		int passedcnt = 0, failedcnt = 0;
		Support_Functions.PrintOut("\n\n@@ Passed Test Cases:", false);
		for(int i = 0; i < ResultsList.size(); i++) {
			if (ResultsList.get(i).contains("Scenario_Passed")) {
				passedcnt++;
				Support_Functions.PrintOut(passedcnt + ")    " + ResultsList.get(i), false);
				ResultsList.remove(i);
				i--;
			}
		}
		
		if (ResultsList.size() > 0) {
			Support_Functions.PrintOut("\n\n@@ Failed Test Cases:", false);
			for(String s : ResultsList) {
				failedcnt++;
				Support_Functions.PrintOut(passedcnt + failedcnt + ")    " + s, false);
			}
			Support_Functions.PrintOut("", false);
		}
		
		try {//print out the total execution time.
			Date ClassEnd = new Date();
			long diffInMillies = ClassEnd.getTime() - ClassStart.getTime();
			TimeUnit timeUnit = TimeUnit.SECONDS;
			Support_Functions.PrintOut("Execution Time in seconds: " + timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS) + "  (" + diffInMillies + " miliseconds)", false);
		}catch (Exception e) {}
		
		Support_Functions.PrintOut("Passed: " + passedcnt + " - " + "Failed: " + failedcnt + "\n", false);
		
		String ConsoleText = "";
		for (String s : Support_Functions.PrintLog){
			ConsoleText += s + System.lineSeparator();
		}
		ConsoleText.replaceAll("\n", System.lineSeparator());
		
		Support_Functions.WriteToFile(LevelsToTest, ConsoleText, ".\\EclipseScreenshots\\MFAC\\" + Support_Functions.CurrentDateTime() + " L" + LevelsToTest + " MFAC");
		Support_Functions.MoveOldLogs();
	}

	@AfterMethod
	public void afterMethod(ITestResult r) {
		try{
		    if(r.getStatus() == ITestResult.SUCCESS){
		    	Support_Functions.PrintOut(r.getName() + " passed " + Arrays.toString(r.getParameters()), false);
		    }else if(r.getStatus() == ITestResult.FAILURE) {
		    	Support_Functions.PrintOut(r.getName() + " failed " + Arrays.toString(r.getParameters()), false);
		    }else if(r.getStatus() == ITestResult.SKIP ){
		    	Support_Functions.PrintOut(r.getName() + " skipped " + Arrays.toString(r.getParameters()), false);
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "dp", enabled = false)
	public void DetermineLockoutTime(String OrgName, String OAuth_Token, String IssueURL){
		String Buffer[] = null, UserName = UserName();
		int ExpiraitonMinutes = -1, PinThreshold = -1;
		do {
			Buffer = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			PinThreshold++;
		}while (Buffer[1].contains("pinExpirationDate"));
		ResultsList.add(OrgName + " " + IssueURL + "  Can request " + PinThreshold + " pins before being lockout out.");
		Support_Functions.PrintOut(OrgName + " " + IssueURL + "  Can request " + PinThreshold + " pins before being lockout out.", true);
			
		do {
			Buffer = MFAC_API_Endpoints.IssuePinAPI(UserName, OrgName, IssueURL, OAuth_Token);
			ExpiraitonMinutes++;
			Support_Functions.PrintOut("Sleeping for a minute", true);
			try {Thread.sleep(6000);} catch (Exception e) {}
		}while (!Buffer[1].contains("pinExpirationDate"));
		ResultsList.add(OrgName + " " + IssueURL + "Can request additional pins after " + ExpiraitonMinutes + " minutes. ");
		Support_Functions.PrintOut(OrgName + " " + IssueURL + "Can request additional pins after " + ExpiraitonMinutes + " minutes. ", true);
	}
	
	///////////////////////////////////METHODS//////////////////////////////////
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
	
	///////Helper Functions///////////////	

	public static String UserName() {
		return Support_Functions.getRandomString(10) + "-" + Support_Functions.getRandomString(24);
	}
}
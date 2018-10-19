package TestNG;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Parameters;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import java.util.Iterator;
import java.util.List;

import SupportClasses.DriverFactory;
import SupportClasses.ThreadLogger;
import TestingFunctions.Helper_Functions;
import TestingFunctions.WIDM_Functions;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class WIDM extends Helper_Functions{
	static String LevelsToTest = "2";
	static boolean SmokeTest = true; // will limit the test cases to high level
	static String CountryList[][];
	
	@BeforeClass
	public void beforeClass() {
		DriverFactory.LevelsToTest = LevelsToTest;
		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			LoadUserIds(Integer.parseInt(Level));
		}
		
		//if (SmokeTest) 
		CountryList = new String[][]{{"US", "United States"}};
	
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			int intLevel = Integer.parseInt(Level);
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "WIDM_ResetPasswordSecret":
		    		for (int j=0; j < CountryList.length; j++) {
						data.add( new Object[] {Level, CountryList[j][0], DataClass[intLevel][1].SSO_LOGIN_DESC, DataClass[intLevel][1].USER_PASSWORD_DESC, DataClass[intLevel][1].SECRET_ANSWER_DESC}); 
					}
		    		
		    		break;
				case "WIDM_ForgotUserID":
					data.add( new Object[] {Level, MyEmail});   //level, email
					break;
				case "WIDM_Registration":
				case "WIDM_Registration_ErrorMessages":
					if (Level == "7") {
						data.add( new Object[] {Level, "US"});
					}else {
						for (int j=0; j < CountryList.length; j++) {
							data.add( new Object[] {Level, CountryList[j][0]});
						}
					}
					
					break;
				case "WIDM_RegistrationEFWS":
					data.add( new Object[] {Level, CountryList[0][0]});
					break;
				case "ResetPasswordWIDM_Email":
					data.add( new Object[] {Level, DataClass[intLevel][1].SSO_LOGIN_DESC, DataClass[intLevel][1].USER_PASSWORD_DESC});
					break;
			}
		}	
		return data.iterator();
	}

	@Parameters({ "Levels", "SmokeTest" })
	@Test(priority = 1)
	public void SetEnvironment(String parmaOne, String paramTwo){
		LevelsToTest = parmaOne;
		if (paramTwo.contentEquals("true")) {
			SmokeTest = true;
		}else if (paramTwo.contentEquals("false")){
			SmokeTest = false;
		}
		PrintOut(parmaOne + "          " + paramTwo, false);
	}
	
	@Test(dataProvider = "dp")
	public void WIDM_Registration(String Level, String CountryCode){
		try {
			String Address[] = LoadAddress(CountryCode, Level);
			String UserName[] = LoadDummyName("WIDM", Level);
			String UserId = LoadUserID("L" + Level + "WIDM" + CountryCode);
			String Result = WIDM_Functions.WIDM_Registration(Level, Address, UserName, UserId);
			PrintOut(Result, false);
		}catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WIDM_RegistrationEFWS(String Level, String CountryCode){
		try {
			String Address[] = LoadAddress(CountryCode, Level);
			String UserName[] = LoadDummyName("WIDM", Level);
			String UserId = LoadUserID("L" + Level + "EFWS");
			String Result = WIDM_Functions.WIDM_RegistrationEFWS(Level, Address, UserName, UserId);
			PrintOut(Result, false);
		}catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WIDM_ResetPasswordSecret(String Level, String CountryCode, String UserId, String Password, String SecretAnswer){
		try {
			String Result = WIDM_Functions.ResetPasswordWIDM_Secret(Level, CountryCode, UserId, Password + "5", SecretAnswer);
			PrintOut(Result, false);
		}catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WIDM_ForgotUserID(String Level, String Email){
		try {
			String Result = WIDM_Functions.Forgot_User_WIDM(Level, Email);
			PrintOut(Result, false);
		}catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void ResetPasswordWIDM_Email(String Level, String UserId, String Password){
		try {
			String Result = WIDM_Functions.Reset_Password_WIDM_Email(Level, UserId, Password);
			PrintOut(Result, false);
		}catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test(dataProvider = "dp")
	public void WIDM_Registration_ErrorMessages(String Level, String CountryCode){
		try {
			String Address[] = LoadAddress(CountryCode, Level);
			String Name[] = LoadDummyName(CountryCode, Level);
			String UserID = LoadUserID("L" + Level + "T");
			String Result = WIDM_Functions.WIDM_Registration_ErrorMessages(Level, Address, Name, UserID) ;
			PrintOut(Result, false);
		}catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@AfterClass
	public void afterClass() {
		Cleanup(LevelsToTest, ThreadLogger.ResultsList);
	}
	
}
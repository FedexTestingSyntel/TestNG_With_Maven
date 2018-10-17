package TestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import TestingFunctions.*;
import SupportClasses.*;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class WFCL{
	static ArrayList<String[]> AddressDetails = new ArrayList<String[]>();
	static String LevelsToTest = "3";
	final static boolean SmokeTest = true; // will limit the test cases to high level
	static String CountryList[][];

	@BeforeClass
	public void beforeClass() {
		DriverFactory.LevelsToTest = LevelsToTest;
		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			Helper_Functions.LoadUserIds(Integer.parseInt(Level));
		}
		
		AddressDetails = Helper_Functions.getExcelData(".\\Data\\AddressDetails.xls",  "Countries");//load the relevant information from excel file.

		//if (SmokeTest) 
		CountryList = new String[][]{{"US", "United States"}};
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i)), Account = null;
			int intLevel = Integer.parseInt(Level);
			//int intLevel = Integer.parseInt(Level);
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "WFCL_CreditCardRegistrationEnroll":
		    		for (int j = 0; j < CountryList.length; j++) {
		    			String EnrollmentID[] = Helper_Functions.LoadEnrollmentIDs(CountryList[j][0]);
		    			data.add( new Object[] {Level, EnrollmentID[0], CountryList[j][0]});
					}
		    	break;
		    	case "WFCL_TNT_Vat_Validation":
		    		String TNTValidation[] = {"GB", "NL", "CL", "FR", "BE", "LU"};
		    		for (int j = 0; j < TNTValidation.length; j++) {
		    			String EnrollmentID[] = Helper_Functions.LoadEnrollmentIDs(TNTValidation[j]);
		    			ArrayList<String[]> TaxInfo = Helper_Functions.LoadTaxInfo(TNTValidation[j]);
		    			for (String Tax[]: TaxInfo) {
		    				data.add(new Object[] {Level, EnrollmentID[0], TNTValidation[j], Tax});
		    			}
					}
		    	break;
		    	case "WFCL_UserRegistration":
		    		data.add( new Object[] {Level, "US"});
		    	break;
		    	case "WFCL_AccountRegistration_Admin":
		    		if (Level == "7") {
		    			break;
		    		}
		    		Account = Helper_Functions.getExcelFreshAccount(Level,  "us", true);
		    		data.add( new Object[] {Level, "US", Account});
		    	break;
		    	case "WFCL_AccountRegistration_INET":
		    	case "WFCL_AccountRegistration_WDPA":
		    		for (int j = 0; j < CountryList.length; j++) {
		    			Account = Helper_Functions.getExcelFreshAccount(Level, CountryList[j][0], true);
			    		data.add( new Object[] {Level, CountryList[j][0], Account});
					}
		    	break;
		    	case "WFCL_Forgot_User_Email":
		    		for (int j = 0; j < CountryList.length; j++) {
			    		data.add( new Object[] {Level, CountryList[j][0], Helper_Functions.MyEmail});
					}
		    	break;
		    	case "WFCL_PasswordResetSecret":
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < Helper_Functions.DataClass[intLevel].length; k++) {
		    				if (Helper_Functions.DataClass[intLevel][k].COUNTRY_CD.contentEquals(CountryList[j][0])) {
		    					data.add( new Object[] {Level, CountryList[j][0], Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC, Helper_Functions.DataClass[intLevel][k].USER_PASSWORD_DESC + "A", Helper_Functions.DataClass[intLevel][k].SECRET_ANSWER_DESC});
		    					break;
		    				}
		    			}
					}
		    	break;
		    	case "WFCL_Reset_Password_Email":
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < Helper_Functions.DataClass[intLevel].length; k++) {
		    				if (Helper_Functions.DataClass[intLevel][k].COUNTRY_CD.contentEquals(CountryList[j][0])) {
		    					data.add( new Object[] {Level, CountryList[j][0], Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC, Helper_Functions.DataClass[intLevel][k].USER_PASSWORD_DESC});
		    					break;
		    				}
		    			}
					}
		    	break;
		    	case "WFCL_WADM_Invitaiton":
		    		for (int k = 0; k < Helper_Functions.DataClass[intLevel].length; k++) {
	    				if (Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC.contains("CC")) {
	    					data.add( new Object[] {Level, Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC, Helper_Functions.DataClass[intLevel][k].USER_PASSWORD_DESC, Helper_Functions.MyEmail});
	    					break;
	    				}
	    			}
		    	break;
			}
		}	
		return data.iterator();
	}

	@Test(dataProvider = "dp")
	public void WFCL_CreditCardRegistrationEnroll(String Level, String EnrollmentID, String CountryCode) {
		try {
			String CreditCard[] = Helper_Functions.LoadCreditCard("V", Level);
			String ShippingAddress[] = Helper_Functions.LoadAddress(CountryCode, Level), BillingAddress[] = ShippingAddress;
			String UserId = Helper_Functions.LoadUserID("L" + Level + EnrollmentID + Thread.currentThread().getId() + "CC");
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode + "CC", Level);
			String Result[] = WFCL_Functions.CreditCardRegistrationEnroll(Level, EnrollmentID, CreditCard, ShippingAddress, BillingAddress, ContactName, UserId, false, null);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", enabled = false)
	public void WFCL_TNT_Vat_Validation(String Level, String EnrollmentID, String CountryCode, String VatNumber[]) {
		try {
			String CreditCard[] = Helper_Functions.LoadCreditCard("V", Level);
			String ShippingAddress[] = Helper_Functions.LoadAddress(CountryCode, Level), BillingAddress[] = ShippingAddress;
			String UserId = Helper_Functions.LoadUserID("L" + Level + EnrollmentID + "CC");
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode + "CC", Level);
			boolean BuisnessAccount = true;
				//may need to add a check here later for personal or business
			String Result[] = WFCL_Functions.CreditCardRegistrationEnroll(Level, EnrollmentID, CreditCard, ShippingAddress, BillingAddress, ContactName, UserId, BuisnessAccount, VatNumber);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(dataProvider = "dp")
	public void WFCL_UserRegistration(String Level, String CountryCode) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String UserID = Helper_Functions.LoadUserID("L" + Level  + Address[6] + "Create");
			String ContactName[] = Helper_Functions.LoadDummyName("Create", Level);
			String Result = Arrays.toString(WFCL_Functions.WFCL_UserRegistration(Level, UserID, ContactName, Address));
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 3)//since this method will consume an acocunt number run after others have completed
	public void WFCL_AccountRegistration_Admin(String Level, String CountryCode, String FreshAccount) {
		try {
			String UserName[] = Helper_Functions.LoadDummyName("INET", Level);
			String UserID = Helper_Functions.LoadUserID("L" + Level + FreshAccount + CountryCode);
			String AddressDetails[] = Helper_Functions.AccountDetails(Level, FreshAccount);
			String Result = WFCL_Functions.WFCL_AccountRegistration_INET(Level, UserName, UserID, FreshAccount, AddressDetails, true);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", priority = 1)
	public void WFCL_AccountRegistration_INET(String Level, String CountryCode, String Account){
		try {
			String AddressDetails[] = Helper_Functions.AccountDetails(Level, Account);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String UserID = Helper_Functions.LoadUserID("L" + Level + "Inet" + CountryCode);
			String Result = WFCL_Functions.WFCL_AccountRegistration_INET(Level, ContactName, UserID, Account, AddressDetails, false);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WFCL_Forgot_User_Email(String Level, String CountryCode, String Email) {
		try {
			String Result = WFCL_Functions.Forgot_User_Email(Level, CountryCode, Email);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WFCL_AccountRegistration_WDPA(String Level, String CountryCode, String Account){
		try {
			String AddressDetails[] = Helper_Functions.AccountDetails(Level, Account);
			String ContactName[] = Helper_Functions.LoadDummyName("WDPA", Level);
			String UserID = Helper_Functions.LoadUserID("L" + Level + Thread.currentThread().getId() + "WDPA");
			String Result[] = WFCL_Functions.WDPA_Registration(Level, ContactName, UserID, Account, AddressDetails);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WFCL_PasswordResetSecret(String Level, String Country, String UserId, String newPassword, String SecretAnswer){
		try {
			String Result = String.valueOf(WFCL_Functions.WFCL_Secret_Answer(Level, Country, UserId, newPassword, SecretAnswer));
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WFCL_Reset_Password_Email(String Level, String CountryCode, String UserId, String Password) {
		try {
			String Result = WFCL_Functions.ResetPasswordWFCL_Email(Level, CountryCode, UserId, Password);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp", enabled = !SmokeTest)
	public void WFCL_WADM_Invitaiton(String Level, String AdminUser, String Password, String Email){     //check on this, make sure that testing correct
		try {
			String ContactName[] = Helper_Functions.LoadDummyName("WADMInvite", Level);
			String Result[] = WFCL_Functions.WFCL_WADM_Invitaiton(Level, AdminUser, Password, ContactName, Email);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	};

	@AfterClass
	public void afterClass() {
		Helper_Functions.Cleanup(LevelsToTest, ThreadLogger.ResultsList);
	}
	  
	  /*

	
	//Non smoke test scenarios

	//@Test
	public void WFCL_AccountRegistration_Rewards(){
		if (Environment == 7) { //Set to not work in production due to limited test data
			Assert.fail();
		}
		
		String Results[] = null;
		try {
			String LocAccountNumber = NonAdminAccounts[0];//us account
		String AccountAddress[] = Helper_Functions.AccountDetails(LocAccountNumber);
			Results = WFCL_RewardsRegistration(Helper_Functions.LoadDummyName(AccountAddress[6]), Helper_Functions.LoadUserID(strLevel + "Rewards"), LocAccountNumber, AccountAddress);
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Results)));
	}
	
	//@Test
	public void WFCL_AccountRegistration_GFBO(){
		if (Environment == 7) { //Set to not work in production due to limited test data
			Assert.fail();
		}
		String Results[] = null;
		try {
			
			AccountNumber = "696394962";
			if (AccountNumber == null) {
				AccountNumber = GetFreshAccount("US");
			}
			String AccountAddress[] = Helper_Functions.AccountDetails(AccountNumber);
			Results = WFCL_AccountRegistration_GFBO(Helper_Functions.LoadDummyName(AccountAddress[6]), Helper_Functions.LoadUserID(strLevel + "GFBO"), AccountNumber, AccountAddress);
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Results)));
	};	
	
	//@Test
	public void WFCL_GFBO_Login(){
		try {
			WFCL_GFBO_Login(strGFBO, strPassword, "US");
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, strGFBO));
	};	
	
	//@Test
	public void WFCL_AccountRegistration_ISGT(){     //NEED to test
		String LocAccountNumber = NonAdminAccounts[0];//us account
		String Results[] = null;
		try {
			String Address[] = null;
			
			if (Environment == 7) {
				Address = Helper_Functions.LoadAddress("US", "TN", "10 FedEx Parkway");
			}else {
				Address = Helper_Functions.AccountDetails(LocAccountNumber);
			}
			
			Results = WFCL_AccountRegistration_ISGT(Helper_Functions.LoadDummyName(Address[6]), Helper_Functions.LoadUserID(strLevel + "ISGT"), LocAccountNumber, Address);
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Results)));
	}

	//@Test
	public void WFCL_WADM_Invitaiton(){     //check on this, make sure that testing correct
		String Results[] = null;
		try {
			Results = WFCL_WADM_Invitaiton(strDummyWADMAdmin, strPassword, Helper_Functions.LoadDummyName("FBO"), strEmail);
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Results)));
	};
	
	//@Test
	public void WFCL_GFBO_Invitaiton(){     //check on this, not sure how to get this working, GFBO does not reference elements and ids correctly
		String Results[] = null;
		try {
			Results = WFCL_GFBO_Invitaiton(strGFBO, strPassword, Helper_Functions.LoadDummyName("FBO"), strEmail, "2");
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Results)));
	};	
	
	//@Test
	public void WFCL_CreditCardRegistration_US_ErrorMessage() {   //PPM#45905- Change Verbiage of CC Failure in OADR
		for (int i = 0; i < EnrollmentIDs.length; i++){
			if (EnrollmentIDs[i][1].contentEquals("US")){
				String EnrollmentId = EnrollmentIDs[i][0];
				try {
					String ErrorMessage = "FedEx Online Account Registration is not able to process your request at this time. Please call 1.800.463.3339 and ask for \"new account setup\" to connect with a new account customer service representative."; 
					WFCL_CreditCardRegistration_Error(Helper_Functions.LoadCreditCard("I"), EnrollmentId, Helper_Functions.LoadAddress("US"), Helper_Functions.LoadDummyName(), Helper_Functions.LoadUserID(strLevel), ErrorMessage);
				} catch (Exception e) {
					Assert.fail();
				}
				PassOrFail = true;
				ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, "Correct"));
				break;
			}
		}
	}
	
	//@Test
	public void WFCL_UserRegistration_Captcha() {
		
		boolean Results = false;
		try {
			Results = WFCL_Captcha(strTodaysDate, Helper_Functions.LoadDummyName(), Helper_Functions.LoadAddress("US"));
		} catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Results + ""));
	}
	
	//@Test
	public void WFCL_UserRegistration_Captcha_Legacy() {
		boolean Results = false;
		try {
			Results = WFCL_Captcha_Legacy(strTodaysDate, Helper_Functions.LoadDummyName(), Helper_Functions.LoadAddress("US"));
		} catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Results + ""));
	}

	 */
	
    
}

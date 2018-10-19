package WaterfallApplications;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import SupportClasses.DriverFactory;
import TestingFunctions.Helper_Functions;
import TestingFunctions.WPRL_Functions;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class WPRL {

	static ArrayList<String[]> AddressDetails = new ArrayList<String[]>();
	static String LevelsToTest = "2";
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
			String Level = String.valueOf(LevelsToTest.charAt(i));
			int intLevel = Integer.parseInt(Level);
			//int intLevel = Integer.parseInt(Level);
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "WPRL_Contact_Admin":
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < Helper_Functions.DataClass[intLevel].length; k++) {
		    				if (Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC.contains("FDM")) {
		    					data.add( new Object[] {Level, Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC, Helper_Functions.DataClass[intLevel][k].USER_PASSWORD_DESC, CountryList[j][0], Helper_Functions.MyEmail});
		    					break;
		    				}
		    			}
					}
		    	break;
			}
		}	
		return data.iterator();
	}

	@Test(dataProvider = "dp")
	public void WPRL_Contact_Admin(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String ContactDetails[][] = Helper_Functions.LoadPhone_Mobile_Fax_Email(CountryCode);
			String Result[] =  WPRL_Functions.WPRL_Contact(Level, UserID, Password, Address, ContactName, ContactDetails, Email);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_Contact_NonAdmin(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String ContactDetails[][] = Helper_Functions.LoadPhone_Mobile_Fax_Email(CountryCode);
			String Result[] =  WPRL_Functions.WPRL_Contact(Level, UserID, Password, Address, ContactName, ContactDetails, Email);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_AccountManagement_Passkey(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String CreditCard[] = Helper_Functions.LoadCreditCard("V", Level);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String Result[] =  WPRL_Functions.WPRL_AccountManagement(Level, UserID, UserID, Address, CreditCard, ContactName);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_AccountManagement_NonPasskey(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String CreditCard[] = Helper_Functions.LoadCreditCard("V", Level);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String Result[] =  WPRL_Functions.WPRL_AccountManagement(Level, UserID, UserID, Address, CreditCard, ContactName);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_AccountManagement_BillingInvoice(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String CreditCard[] = Helper_Functions.LoadCreditCard("V", Level);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String Result[] =  WPRL_Functions.WPRL_AccountManagement(Level, UserID, Password, Address, CreditCard, ContactName);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(dataProvider = "dp")
	public void WPRL_FDM(String Level, String UserID, String Password, String CountryCode, String Email) {
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String CreditCard[] = Helper_Functions.LoadCreditCard("V", Level);
			String ContactName[] = Helper_Functions.LoadDummyName(CountryCode, Level);
			String Result[] =  WPRL_Functions.WPRL_FDM(Level, "US", UserID, Password, Address, CreditCard, ContactName);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}


/*
	
	//@Test
	public void WPRL_FDM_Enrollemnt(){
		String Result[] = null;
		try {
			String FDM_Address[] = LoadAddress("US");
			String UserId = strLevel + FDM_Address[6] + "FDM" + strTodaysDate;
			//String NewUser = CreateNewUser(FDM_Address, Environment + "", strPassword);
			String[] NewUser = WFCL_JUnit.WFCL_UserRegistration(UserId, LoadDummyName(), FDM_Address);
			Result = WPRL_FDM_Enrollemnt(NewUser[0], NewUser[1], FDM_Address, "postcard");
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Result)));
	}
	
	@Test
	public void WPRL_FDM_Enrollemnt_Add(){
		String Result[] = null;
		try {
			Result = WPRL_FDM("US", strWPRLsingleFDM, strPassword, LoadAddress("US"), LoadCreditCard("V"), LoadDummyName());
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Result)));
	}
*/
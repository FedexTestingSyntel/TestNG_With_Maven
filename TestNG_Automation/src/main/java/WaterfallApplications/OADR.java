package WaterfallApplications;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import TestingFunctions.*;
import SupportClasses.*;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class OADR{
	static String LevelsToTest = "2";
	final static boolean SmokeTest = true; // will limit the test cases to high level
	static ArrayList<String[]> AddressDetails = new ArrayList<String[]>();
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

			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "OADR_Apply_Discount_To_Account":
		    		String EnrollmentID[] = Helper_Functions.LoadEnrollmentIDs("US");
		    		for (int j = 0; j < Helper_Functions.DataClass[intLevel].length; j++) {
	    				if (Helper_Functions.DataClass[intLevel][j].COUNTRY_CD.contentEquals("US")) {
	    					data.add( new Object[] {Level, EnrollmentID[0], EnrollmentID[1], Helper_Functions.DataClass[intLevel][j].SSO_LOGIN_DESC, Helper_Functions.DataClass[intLevel][j].USER_PASSWORD_DESC});
	    					break;
	    				}
	    			}
		    	break;
			}
		}	
		return data.iterator();
	}
	
	
	@Test(dataProvider = "dp")
	public static void OADR_Apply_Discount_To_Account(String Level, String EnrollmentID, String CountryCode, String UserId, String Password){
		try {
			String Result = OADR_ApplyDiscount(Level, EnrollmentID, CountryCode, UserId, Password);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end OADR_ApplyDiscount

	public static String OADR_ApplyDiscount(String Level, String EnrollmentID, String CountryCode, String UserId, String Password) throws Exception{
		try {
			String Time = Helper_Functions.CurrentDateTime();
			String SCPath = Time + " L" + Level + CountryCode + " OADR ApplyDiscount ";
			
			WebDriver_Functions.ChangeURL("Enrollment_" + EnrollmentID, CountryCode, true, Level);
			WebDriver_Functions.WaitPresent(By.name("username"));
			WebDriver_Functions.Type(By.name("username"), UserId);
			WebDriver_Functions.Type(By.name("password"), Password);
			WebDriver_Functions.Click(By.name("login"));

			Helper_Functions.PrintOut("Logged in with " + UserId + "/" + Password, true);
	 		
	 		//select the apply discount radio button and continue.
			WebDriver_Functions.Click(By.xpath("//input[(@name='whichBillingAddress') and (@value = 'useContactAddress')]"));
			WebDriver_Functions.takeSnapShot(SCPath + "Apply Discount.png");
			WebDriver_Functions.Click(By.name("continue"));
	 		
	 		//after processing screen
	 		try {
	 			WebDriver_Functions.WaitForTextPresentIn(By.tagName("body"), "We are processing your request.");
	 			WebDriver_Functions.WaitForTextNotPresentIn(By.tagName("body"), "We are processing your request.");
			}catch (Exception e) {}
	 		
	 		if (WebDriver_Functions.isPresent(By.name("discountConflictResolution"))){
	 			WebDriver_Functions.Click(By.xpath("//input[(@name='discountConflictResolution') and (@value = 'applyNewDiscount')]"));
	 			WebDriver_Functions.ElementMatches(By.xpath("//*[@id='content']/div/div[2]/p[2]/font"), "You have existing discounts for FedEx Express®, FedEx Ground®, and/or FedEx Freight® services on your account. Those discounts will be replaced with new discounts should you choose to enroll in this program. For more information about the new discounts, call a FedEx Advantage® representative at 1.800.434.9918.", 116519);
	 			WebDriver_Functions.takeSnapShot(SCPath + "Discount Conflict.png");
	 			
	 			if (WebDriver_Functions.isPresent(By.name("lastFourDigits"))){
	 				InvoiceOrCCValidaiton();
	 			}else {
	 				WebDriver_Functions.Click(By.name("submit"));
	 			}
	 			
	 			WebDriver_Functions.WaitForTextNotPresentIn(By.className("mainheader1"), "We are processing your request. Please wait...");
	 		}
	 		
	 		WebDriver_Functions.WaitForText(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div[1]/div[2]/table/tbody/tr[1]/td[2]"), UserId);
	 		WebDriver_Functions.takeSnapShot(SCPath + "Confirmation.png");
	 		
		}catch (Exception e) {
			throw e;
		}
		return UserId + " " + EnrollmentID;
	}//end OADR_ApplyDiscount

	private static void InvoiceOrCCValidaiton() throws Exception{
		InvoiceOrCCValidaiton("4460", "750000000", "750000001");
	}

	private static void InvoiceOrCCValidaiton(String CCNumber, String InvoiceA, String InvoiceB) throws Exception{
		if (WebDriver_Functions.isPresent(By.name("invoiceNumberA"))){
			WebDriver_Functions.Type(By.name("invoiceNumberA"), InvoiceA);
			WebDriver_Functions.Type(By.name("invoiceNumberB"), InvoiceB);
			WebDriver_Functions.Click(By.className("buttonpurple"));
			WebDriver_Functions.WaitNotPresent(By.name("invoiceNumberB"));
		}else if (WebDriver_Functions.isPresent(By.name("lastFourDigits"))) {
			WebDriver_Functions.Type(By.name("lastFourDigits"), CCNumber);//this is just a guess as the number most commonly used. 
			WebDriver_Functions.Click(By.name("submit"));
			WebDriver_Functions.WaitNotPresent(By.name("lastFourDigits"));
		}
	}
}

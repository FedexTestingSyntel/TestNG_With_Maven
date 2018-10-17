package TestingFunctions;

import java.util.Arrays;
import org.openqa.selenium.By;

import SupportClasses.DriverFactory;

public class WFCL_Functions extends Helper_Functions{
	public static String strPassword = "Test1234";
	
	public static String[] CreditCardRegistrationEnroll(String Level, String EnrollmentID, String CreditCardDetils[], String AddressDetails[], String BillingAddressDetails[], String Name[], String UserId, boolean BusinessAccount, String TaxInfo[]) throws Exception{
		try {
			String CountryCode = AddressDetails[6];
			ChangeURL("INET", CountryCode, true, Level);//go to the INET page just to load the cookies
			ChangeURL("Enrollment_" + EnrollmentID, CountryCode, false, Level);//go to the INET page just to load the cookies
			String SCPath = CurrentDateTime() + " L" + Level + " WFCL " + EnrollmentID + " CC ";
			takeSnapShot(SCPath + "Discount Page.png");
			
			if (isPresent(By.name("Apply Now"))) {
				Click(By.name("Apply Now"));//apply link from marketing page
			}
			
			if (isPresent(By.linkText("Finish registering for a FedEx account"))) {
				Click(By.linkText("Finish registering for a FedEx account"));    //  //*[@id="signupnow"]
			}else if (isPresent(By.name("signupnow"))) {
				Click(By.name("signupnow"));    //  //*[@id="signupnow"]
			}

			//Step 1: Enter the contact information.
			WFCL_ContactInfo_Page(Name, AddressDetails, UserId, SCPath + "ContactInformation.png"); //enters all of the details

			//if ((AddressDetails[6].contentEquals("US") || AddressDetails[6].contentEquals("CA"))){Click(By.xpath("//input[(@name='accountType') and (@value = 'openAccount')]"));//select the open account radio button}

			//Step 2: Enter the credit card details
			WaitPresent(By.id("CCType"));
			boolean Multicard = true;
			if(TaxInfo != null && !TaxInfo[3].contains("Valid")) {
				Multicard = false;
			}
			CreditCardDetils = WFCL_CC_Page(CreditCardDetils, AddressDetails[6], "WFCL", SCPath + "Information.png", BusinessAccount, Multicard, TaxInfo);
			
			//Step 3: Confirmation Page
			//Domestic and international will be different  
			WaitOr_TextToBe(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div[1]/div[2]/table/tbody/tr[1]/td[2]"), UserId, By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div/div[2]/table/tbody/tr[1]/td[2]"),  UserId);
			String AccountNumber = DriverFactory.getInstance().getDriver().findElement(By.xpath("//*[@id='acctNbr']")).getText();
			takeSnapShot(SCPath + "Confirmation.png");

			boolean InetFlag = false, AdminFlag = false;

			//Check the INET page
			try{
				ChangeURL("INET", CountryCode, false, Level);
				WaitForTextPresentIn(By.id("module.from._collapsed"), Name[0] + " " + Name[2]);
				takeSnapShot(SCPath + "INET working.png");
				InetFlag = true;
			}catch (Exception e2){
				PrintOut("User not registered for INET", true);
			}

			//Check the Administration page
			try{
				ChangeURL("WADM", CountryCode, false, Level);
				WaitForText(By.cssSelector("#main > h1"), "Admin Home: " + Name[0] + " " + Name[2]);//note, will be company name if business account.
				takeSnapShot(SCPath + "Admin working.png");
				AdminFlag = true;
			}catch (Exception e2){
				PrintOut("User not registered for WADM", true);
			}

			String UUID = GetCookieValue("fcl_uuid");
			PrintOut("Finished CreditCardRegistrationEnroll  " + UserId + "/" + strPassword + "--" + AccountNumber + "--" + UUID, true);

			String LastFourOfCC = CreditCardDetils[1].substring(CreditCardDetils[1].length() - 4, CreditCardDetils[1].length());
			String ReturnValue[] = new String[] {UserId, AccountNumber, UUID, LastFourOfCC, "INET:" + InetFlag, "Admin:" + AdminFlag};
			WriteUserToExcel(Level, UserId, strPassword);
			return ReturnValue;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}//end CreditCardRegistrationEnroll

	//will return a string array with 0 as the user id and 1 as the password, 2 is the uuid
	public static String[] WFCL_UserRegistration(String Level, String UserId, String[] Name, String AddressDetails[]) throws Exception{
		try {
			String CountryCode = AddressDetails[6];
			ChangeURL("Pref", CountryCode, true, Level);//navigate to email preferences page to load cookies
			Click(By.id("registernow"));
			
			String Time = CurrentDateTime();
			String SCPath = Time + " L" + Level + CountryCode + " WFCL ";
			WFCL_ContactInfo_Page(Name, AddressDetails, UserId,  SCPath + "ContactInformation.png"); //enters all of the details

			//Confirmation page
			WaitForText(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div/h2"), "Login Information");
			WaitForText(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div/div[2]/table/tbody/tr/td[2]"), UserId);
			takeSnapShot(SCPath + "RegistrationConfirmation.png");
			String UUID = GetCookieValue("fcl_uuid");
			PrintOut("Finished WFCL_UserRegistration  " + UserId + "/" + strPassword + " -- " + UUID, true);
			String ReturnValue[] = new String[]{UserId, UUID};
			WriteUserToExcel(Level, UserId, strPassword);//Write User to file for later reference
			return ReturnValue;
		}catch (Exception e) {
			if (e.getMessage().contains("[(@name='accountType') and (@value = 'noAccount')]")){
				PrintOut("Radio button not present for " + AddressDetails[6] + " to do User id creation.", true);
			}
			throw e;
		}
	}//end WFCL_UserRegistration
	
	public static String Forgot_User_Email(String Level, String CountryCode, String Email) throws Exception{
		try{
			ChangeURL("INET", CountryCode, true, Level);//go to the INET page just to load the cookies
			Click(By.name("forgotUidPwd"));
			//wait for text box for user id to appear
			Type(By.name("email"),Email);
			String SCPath = CurrentDateTime() + " L" + Level + " WFCL ";
			takeSnapShot(SCPath + "Forgot User Id.png");
			Click(By.xpath("//*[@id='module.forgotuseridandpassword._expanded']/table/tbody/tr/td[3]/form/table/tbody/tr[6]/td/input[2]"));
			takeSnapShot(SCPath + "Forgot User Confirmation.png");
			PrintOut("Completed Forgot User Confirmation using " + Email + ". An email has been triggered and that test must be completed manually by to see the user list.", true);
			return Email;
		}catch(Exception e){
			PrintOut("General failure in Forgot_User_Email" + "  " + e.getMessage(), true);
			throw e;
		}
	}//end Forgot_User_Email

	//AddressDetails[] =  {Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, countryCode - 6};
	//String Name[] = {FirstName, Middle Name, Last Name}
	public static boolean WFCL_ContactInfo_Page(String Name[], String AddressDetails[], String UserId, String ScreenshotName) throws Exception{
		CheckBodyText("Country/Territory");
		WaitPresent(By.cssSelector("#reminderQuestion option[value=SP2Q1]"));//wait for secret questions to load.

		Type(By.id("firstName"), Name[0]);
		Type(By.name("initials"), Name[1]);
		Type(By.id("lastName"), Name[2]);
		Type(By.id("email"), MyEmail);
		Type(By.id("retypeEmail"), MyEmail);
		Type(By.id("address1"), AddressDetails[0]);
		Type(By.name("address2"), AddressDetails[1]);

		if (isPresent(By.name("city"))) {
			Type(By.name("city"), AddressDetails[2]);
		}else if (isPresent(By.name("city1"))){
			Type(By.name("city1"), AddressDetails[2]);
		}

		if (AddressDetails[4] != null && AddressDetails[4] != ""){
			try{
				Select(By.id("state"), AddressDetails[4],  "v");
			}catch (Exception e){
				try {
					Type(By.name("state"), AddressDetails[4]);//for the legacy registration page such as WDPA the code must be entered as text
				}catch (Exception e2){}
			};
		}

		Type(By.id("zip"), AddressDetails[5]);
		String strPhone = ValidPhoneNumber(AddressDetails[6]);
		Type(By.id("phone"), strPhone);
		Type(By.id("uid"), UserId);
		Type(By.id("password"), strPassword);
		Type(By.id("retypePassword"), strPassword);
		Select(By.id("reminderQuestion"), "What is your mother's first name?", "t");
		Type(By.id("reminderAnswer"), "mom");

		if (isPresent(By.id("acceptterms")) && DriverFactory.getInstance().getDriver().findElement(By.id("acceptterms")).isSelected() == false) {
			Click(By.id("acceptterms"));
		}

		PrintOut("Name: " + Arrays.toString(Name) + "    UserID:" + UserId, true);

		takeSnapShot(ScreenshotName);
		if (isPresent(By.id("iacceptbutton"))) {
			Click(By.id("iacceptbutton"));
		}else if (isPresent(By.id("createUserID"))) {
			Click(By.id("createUserID"));
		}

		return true;
	}//end WFCL_ContactInfo_Page

	public static String[] WFCL_CC_Page(String[] CreditCardDetils, String CountryCode, String App, String ScreenshotPath, boolean BusinessRegistration, boolean MultiCard, String TaxInfo[]) throws Exception{
		PrintOut("WFCL_CC_Page recieved: " + CreditCardDetils[0] + ", " + CountryCode + " From: " + Thread.currentThread().getStackTrace()[2].getMethodName(), true);
		GetCookieValue("fcl_uuid");

		/// 		if ((CountryCode.contentEquals("US") || CountryCode.contentEquals("CA"))){
		//			if (CreditCardDetils[0].contentEquals("MasterCard")){
		//CreditCardDetils[0] = "Mastercard";
		//			}
		//}
		WaitForTextPresentIn(By.cssSelector("#CCType"), CreditCardDetils[0]);//wait for the credit card types to load

		Type(By.id("creditCardNumber"), CreditCardDetils[1]);
		Type(By.id("creditCardIDNumber"), CreditCardDetils[2]);
		Select(By.id("monthExpiry"), CreditCardDetils[3], "t");
		Select(By.id("yearExpiry"), CreditCardDetils[4], "t");

		if (isPresent(By.name("editshipinfo"))) {
			Click(By.name("editshipinfo"));//the shipping address section
			ElementMatches(By.xpath("//*[@id='shipping-address-fields']/label[12]/span[1]"), "Country/Territory", 116577);
		}

		if (isPresent(By.name("editccinfo"))) {
			Click(By.name("editccinfo"));//edit the billing address
			if (isPresent(By.xpath("//*[@id='billing-address']/label[17]/span[1]"))) {
				ElementMatches(By.xpath("//*[@id='billing-address']/label[17]/span[1]"), "Country/Territory", 116577);
			}else if (isPresent(By.xpath("//*[@id='billing-address']/label[18]/span[1]"))) {//Non US field
				ElementMatches(By.xpath("//*[@id='billing-address']/label[18]/span[1]"), "Country/Territory", 116577);
			}
		}

		if(isPresent(By.name("questionCd9"))){ //This is just filler currently. Selecting just to cover the shipping needs section, present on US page
			Select(By.name("questionCd9"), "1", "i");
			Select(By.name("questionCd10"), "1", "i");
			Select(By.name("questionCd11"), "1", "i");
		}

		if (BusinessRegistration) {
			Click(By.xpath("//*[@id='accountTypeBus']"));
			Type(By.name("company"), "Company Name Here");
		}

		if (isPresent(By.name("indTaxID")) || isPresent(By.name("indStateTaxID"))) {
			if (isPresent(By.name("indTaxID"))) {
				Type(By.name("indTaxID"), TaxInfo[1]); 
			}
			if (isPresent(By.name("indStateTaxID"))) {
				Type(By.name("indStateTaxID"), TaxInfo[2]);
			}else if (isPresent(By.name("vatNo"))) {
				Type(By.name("vatNo"), TaxInfo[2]);
			}
		}

		Select(By.id("CCType"), CreditCardDetils[0], "t");

		takeSnapShot(ScreenshotPath);
		Click(By.id("Complete"));

		try {
			WaitForTextPresentIn(By.tagName("body"), "We are processing your request.");
			WaitForTextNotPresentIn(By.tagName("body"), "We are processing your request.");
		}catch (Exception e) {}

		//If the user is still on the CC entry page will try and enter a different credit card type.
		if (isPresent(By.id("monthExpiry")) && MultiCard) {
			PrintOut("Error on Credit Card entry screen. Attempting to register with differnet credit card", true);
			String NewCreditCard[] = LoadCreditCard(CreditCardDetils[1]);
			return WFCL_CC_Page(NewCreditCard, CountryCode, App, ScreenshotPath, BusinessRegistration, MultiCard, TaxInfo);
		}
		//longwait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div[1]/div[2]/table/tbody/tr[1]/td[2]")));//check that confirmation page loads
		return CreditCardDetils; //return the credit card used
	}//end WFCL_CC_Page

	public static String WFCL_AccountRegistration_INET(String Level, String Name[], String UserId, String AccountNumber, String AddressDetails[], boolean AdminReg) throws Exception{
		boolean InetFlag = false, AdminFlag = false;
		PrintOut("Attempting to register with " + AccountNumber, true);
		String CountryCode = AddressDetails[6].toUpperCase();

		String SCPath = CurrentDateTime() + " " + Level + CountryCode + " WFCL ";
		if (CountryCode.contentEquals("US") || CountryCode.contentEquals("CA")){
			ChangeURL("FCLLink", CountryCode, true, Level);
			Click(By.xpath("//input[(@name='accountType') and (@value = 'linkAccount')]"));//select the link account radio button
			WaitPresent(By.cssSelector("#reminderQuestion option[value=SP2Q1]"));//wait for secret questions to load.
			WFCL_ContactInfo_Page(Name, AddressDetails, UserId, SCPath); //enters all of the details
			takeSnapShot(SCPath + "ContactInformation.png");
			Click(By.id("createUserID"));
		}else{
			ChangeURL("FCLLinkInter", CountryCode, true, Level);
			WaitPresent(By.cssSelector("#reminderQuestion"));
			WFCL_ContactInfo_Page(Name, AddressDetails, UserId, SCPath); //enters all of the details
			takeSnapShot(SCPath + "ContactInformation.png");
			Click(By.id("iacceptbutton"));
		}

		//Step 2 Account information
		String AccountNickname = AccountNumber + "_" + CountryCode;
		WFCL_AccountEntryScreen(Level, AccountNumber, AccountNickname, SCPath);

		if (CountryCode.contains("US")){
			WaitForText(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div[1]/div[2]/table/tbody/tr[1]/td[2]"), UserId);
			WaitForText(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div[1]/div[2]/table/tbody/tr[2]/td[2]"), AccountNumber);
		}
		takeSnapShot(SCPath + "RegistrationConfirmation.png");
		//ChangeURL("https://" + strLevelURL + "/shipping/shipEntryAction.do?origincountry=" + CountryCode.toLowerCase());
		ChangeURL("INET", CountryCode, false, Level);
		//Register the account number for INET
		if (CountryCode.contains("US") || CountryCode.contains("CA")){
			WaitPresent(By.name("accountNumberOpco"));
			Select(By.name("accountNumberOpco"), AccountNumber,  "t");
			takeSnapShot(SCPath + "INET Account Selection.png");
			Click(By.className("buttonpurple"));
			//need to add clicking continue button

			//Confirmation from INET registration
			try{//EACI form
				WaitForText(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[1]/td[2]/table/tbody/tr[2]/td/b"), UserId);
				WaitForText(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[2]/td[2]/table/tbody/tr[2]/td/b"), AccountNumber);
				WaitForText(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[3]/td[2]/table/tbody/tr[2]/td/b"), AccountNickname);
				WaitForText(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[3]/td[4]/table/tbody/tr[2]/td/table/tbody/tr/td[2]/b"), AddressDetails[0] + "\n" + AddressDetails[2] + ", " + AddressDetails[4] + " " + AddressDetails[5] + "\n" + AddressDetails[6].toLowerCase());
				InetFlag = true;
			}catch (Exception e){
				PrintOut("Not able to Verify data on INET registration page.", true);
			}
			
			takeSnapShot(SCPath + "INET Confirmation.png");
			Click(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[1]/td[4]/table/tbody/tr/td/table/tbody/tr[1]/td[2]/a/img"));
			WaitPresent(By.xpath("//*[@id='appTitle']"));
		}

		if (AdminReg){//register the user to be pass-key 
			ChangeURL("AdminReg", CountryCode, false, Level);
			try{
				if (isPresent(By.name("accountNumber"))){ 
					Select(By.name("accountNumber"), AccountNumber + "|" + AccountNickname, "v");
					Click(By.name("submit"));	
				}

				if (isPresent(By.name("invoiceNumberA")) || isPresent(By.name("creditCardNumber"))) {
					InvoiceOrCCValidaiton();
				}
			}catch (Exception e2){
				PrintOut("Failure with admin registriaton.", true);
			};
			WaitPresent(By.name("companyName"));
			String CompanyName = "Company" + CurrentDateTime();
			Type(By.name("companyName"), CompanyName);
			takeSnapShot(SCPath + "WADM CompanyName.png");
			Click(By.className("buttonpurple"));

			//confirmation page
			try{
				WaitPresent(By.cssSelector("#confirmation > div > div.fx-col.col-3 > div > h3"));
				PrintOut("Registered for Admin. Current URL:" + DriverFactory.getInstance().getDriver().getCurrentUrl(), true);
				takeSnapShot(SCPath + "WADM Registration Confirmaiton.png");
				Click(By.cssSelector("#confirmation > div > div.fx-col.col-3 > div > p:nth-child(6) > a"));//click shipping admin link
				WaitForText(By.cssSelector("#main > h1"), "Admin Home: " + CompanyName);
				AdminFlag = true;
				//remove the account number from local storage as it is now locked to the company that was just created.
				RemoveAccountFromExcel(Level, AccountNumber);
				
			}catch (Exception e) {
				PrintOut("Not able to register for admin " + DriverFactory.getInstance().getDriver().getCurrentUrl(), true);
			}
		}
		String UUID = GetCookieValue("fcl_uuid");
		PrintOut("Finished WFCL_AccountRegistration  " + UserId + "/" + strPassword + "--" + AccountNumber + "--" + UUID, true);
		String ReturnValue[] = new String[] {UserId, AccountNumber, UUID, "INET:" + InetFlag, "Admin:" + AdminFlag};
		WriteUserToExcel(Level, UserId, strPassword);
		return Arrays.toString(ReturnValue);
	}//end WFCL_AccountRegistration

	public static boolean WFCL_AccountEntryScreen(String Level, String AccountNumber, String AccountNickname, String Path) throws Exception{
		if (isPresent(By.id("accountNumber"))){
			Type(By.id("accountNumber"), AccountNumber);
			Type(By.id("nickName"), AccountNickname);
			takeSnapShot(Path + "AccountInformation.png");
			Click(By.id("createUserID"));
			WaitNotPresent(By.id("nickName"));
		}else if (isPresent(By.name("newAccountNumber"))){
			Type(By.name("newAccountNumber"), AccountNumber);
			Type(By.name("newNickName"), AccountNickname);
			takeSnapShot(Path + "AccountInformation.png");
			Click(By.name("submit"));
			WaitNotPresent(By.name("newNickName"));
		}

		if (isPresent(By.name("invoiceNumberA")) || isPresent(By.name("creditCardNumber"))) {
			InvoiceOrCCValidaiton();
		}

		if (isPresent(By.id("accountNumber"))|| isPresent(By.name("newAccountNumber"))) {
			Click(By.name("submit"));
			PrintOut("Warning, still on account entry screen. The address entered may be incorrect.", true);
			//return WFCL_AccountEntryScreen(AccountNumber, AccountNickname, Path);//this is an issue, need to fix later as link is an issue for AU 
		}else if (WebDriver_Functions.CheckBodyText("Request Access from the Account Administrator")) {
			//remove the account number from local storage as it is now locked to the company that was just created.
			RemoveAccountFromExcel(Level, AccountNumber);
			Helper_Functions.PrintOut("Request Access from the Account Administrator", true);
			throw new Exception("Request Access from the Account Administrator");
		}
		

		WaitNotPresent(By.id("createUserID"));
		return true;
	}//end WFCL_AccountEntryScreen

	private static void InvoiceOrCCValidaiton() throws Exception{
		InvoiceOrCCValidaiton("4460", "750000000", "750000001");
	}

	private static void InvoiceOrCCValidaiton(String CCNumber, String InvoiceA, String InvoiceB) throws Exception{
		if (isPresent(By.name("invoiceNumberA"))){
			Type(By.name("invoiceNumberA"), InvoiceA);
			Type(By.name("invoiceNumberB"), InvoiceB);
			Click(By.className("buttonpurple"));
			WaitNotPresent(By.name("invoiceNumberB"));
		}else if (isPresent(By.name("creditCardNumber"))) {
			Type(By.name("creditCardNumber"), CCNumber);//this is just a guess as the number most commonly used. 
			Click(By.className("buttonpurple"));
			WaitNotPresent(By.name("creditCardNumber"));
		}
	}

	public static String[] WDPA_Registration(String Level, String Name[], String UserId, String AccountNumber, String AddressDetails[]) throws Exception{
 		try {
 			String CountryCode = AddressDetails[6].toUpperCase();
 			ChangeURL("WDPA", CountryCode, true, Level);
 			Click(By.name("signupnow"));
 			String Time = CurrentDateTime();
 			String SCPath = Time + " " + Level + CountryCode + " WFCL ";
 			WFCL_ContactInfo_Page(Name, AddressDetails, UserId, SCPath); //enters all of the details
 			
	 		//Step 2 Account information
	 		String AccountNickname = AccountNumber + "_" + CountryCode;
	 		WFCL_AccountEntryScreen(Level, AccountNumber, AccountNickname, SCPath);
	 		
	 		WaitForText(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/p[2]/table[2]/tbody/tr[3]/td/table[2]/tbody/tr/td[1]/table/tbody/tr[2]/td/b"), UserId);
	 		//*[@id="content"]/div/table/tbody/tr[1]/td[2]/p[2]/table[2]/tbody/tr[3]/td/table[2]/tbody/tr/td[1]/table/tbody/tr[2]/td/b
	 		takeSnapShot(SCPath + "RegistrationConfirmation.png");
		    
		    Click(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/p[2]/table[2]/tbody/tr[3]/td/table[2]/tbody/tr/td[2]/table/tbody/tr/td/table/tbody/tr[1]/td[2]/a/img"));
		    WaitPresent(By.id("module.account._headerTitle"));
		    
		    String UUID = GetCookieValue("fcl_uuid");
		    PrintOut("Finished WFCL_AccountRegistration_WDPA  " + UserId + "/" + strPassword + "--" + AccountNumber + "--" + UUID, true);
		    String ReturnValue[] = new String[] {UserId, AccountNumber, UUID};
		    WriteUserToExcel(Level, UserId, strPassword);
		    return ReturnValue;
 		}catch (Exception e) {
 			e.printStackTrace();
 			throw e;
 		}
 	}//end WFCL_AccountRegistration_WDPA
	
    public static boolean WFCL_Secret_Answer(String Level, String CountryCode, String strUserName, String NewPassword, String SecretAnswer) throws Exception{
 		ChangeURL("INET", CountryCode, true, Level);
 		
    	try{
    		//click the forgot password link
    		Click(By.name("forgotUidPwd"));
    		Type(By.name("userID"), strUserName);
    		String SCPath = CurrentDateTime() + " " + Level + " WFCL ";
    		takeSnapShot(SCPath + "Password Reset.png");
            //click the option 1 button and try to answer with secret question
    		Click(By.xpath("//*[@id='module.forgotuseridandpassword._expanded']/table/tbody/tr/td[1]/form/table/tbody/tr[6]/td/input[2]"));
    		//click the continue button
    		Click(By.xpath("//*[@id='module.resetpasswordoptions._expanded']/table/tbody/tr/td[1]/form/table/tbody/tr[5]/td/input"));

            Type(By.name("answer"), SecretAnswer);
            takeSnapShot(SCPath + "Reset Password Secret.png");
            Click(By.name("action1"));
            
			Type(By.name("password"),NewPassword);
			Type(By.name("retypePassword"),NewPassword);
			takeSnapShot(SCPath + "New Password.png");
			Click(By.name("confirm"));
			WaitForText(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td/table/tbody/tr/td/table/tbody/tr[1]/td/h1"), "Thank you.");
			boolean loginAttempt = Login(Level, strUserName, NewPassword);
			PrintOut(strUserName + " has had the password changed to " + NewPassword, true);
			if (NewPassword.contentEquals(strPassword) && loginAttempt && Thread.currentThread().getStackTrace()[2].getMethodName().contentEquals("WFCL_Secret_Answer")){
		    	return true;
			}else if (loginAttempt){
				return WFCL_Secret_Answer(Level, CountryCode, strUserName, strPassword, SecretAnswer);//change the password back
			}else{
				throw new Exception("Error.");
			}
		}catch (Exception e){
			PrintOut("Secret quesiton " + SecretAnswer + " was not accepted.", true);
		}
    	return false;
	}//end ResetPasswordWFCLSecret
	
    public static String ResetPasswordWFCL_Email(String Level, String CountryCode, String strUserName, String Password) throws Exception{
    	try{
    		Login(Level, strUserName, Password);

    		String Email = "--Could not retrieve email--";
    		String SCPath = CurrentDateTime()+ " L" + Level + " WFCL ";
    		try {
    			ChangeURL("WPRL", CountryCode, false, Level);
    			WaitPresent(By.id("ci_fullname_val"));
    			String UserDetails = DriverFactory.getInstance().getDriver().findElement(By.id("ci_fullname_val")).getText();
    			Email = UserDetails.substring(UserDetails.lastIndexOf('\n') + 1, UserDetails.length());
    			takeSnapShot(SCPath + "Password Reset Email UserDetails.png");
    		}catch (Exception e) {
    			PrintOut("Error " + e.getMessage() + "   " + Email, true);
    		}
    		
    		//trigger the password reset email
    		ChangeURL("INET", CountryCode, true, Level);
    		Click(By.name("forgotUidPwd"));
    		Type(By.name("userID"),strUserName);
    		
    		Click(By.xpath("//*[@id='module.forgotuseridandpassword._expanded']/table/tbody/tr/td[1]/form/table/tbody/tr[6]/td/input[2]"));
    		//click the option 1 button and try to answer with secret question
    		Click(By.name("action2"));
    		takeSnapShot(SCPath + "Password Reset Email.png");
    		PrintOut("Completed ResetPasswordWFCL using " + strUserName + ". An email has been triggered and that test must be completed manually by " + Email, true);
    					
    		return Email;
    	}catch(Exception e){
    		PrintOut("General failure in ResetPasswordWFCL_Email", true);
    		throw e;
    	}
	}//end ResetPasswordWFCL_Email
    
    public static String[] TaxIDinformation(String CountryCode, boolean BusinessAccount){
    	String TaxID = "", StateTaxID = "";
    	switch (CountryCode) {
			case "GB":		//worked for account 643527529 from 8/30/18
				TaxID = "";
				StateTaxID = "GB2332322322312";
				break;
			case "BR":		//check that address matches
				TaxID = "999.999.999-99";//worked for personal account 615002461 back in 2017
				StateTaxID = "0962675512";
				break;
    	}
    	return new String[] {TaxID, StateTaxID};
    }
    
    public static String[] WFCL_WADM_Invitaiton(String Level, String UserID, String Password, String Name[], String Email) throws Exception{	
		try {
			Login(Level, UserID, Password);
			ChangeURL("WADM", "US", false, Level);
			String Time = CurrentDateTime();
			String SCPath = Time + " " + Level + " WFCL ";
			takeSnapShot(SCPath + "UserTable before invite.png");
			
			Click(By.id("createNewUsers"));
			WaitNotPresent(By.id("loading-div"));//wait for the loading overlay to not be present
			Type(By.name("userfirstName"), Name[0]);
			Type(By.name("middleName"), Name[1]);
			Type(By.name("userlastName"), Name[2]);
			String UniqueID = Time.substring(Time.length() - 13, 10);
			Type(By.name("userAlias"), UniqueID);//unique filler value as the time stamp of attempt
			Type(By.name("email"), Email);
			Click(By.id("addAccountButton"));
			WaitNotPresent(By.id("loading-div"));//wait for the loading overlay to not be present
			Click(By.xpath("//*[@id='tableBody']/tr/td[1]/input"));//add the first account number to user.
			Click(By.id("addAccounts"));
			Select(By.id("userAdminTypeSelect"), "company", "v");//make the user company admin user role
			Click(By.id("inviteUsers"));
			takeSnapShot(SCPath + "Invitation.png");
			Click(By.id("userSave"));
			
			WaitNotPresent(By.id("loading-div"));//wait for the loading overlay to not be present
			Type(By.xpath("//*[@id='manageTablesContainer']/div[1]/fieldset[1]/input"), UniqueID);
			Select(By.id("manageTableDropDown"), "Unique ID", "t"); // select to search by id
			Click(By.id("goSearch"));
			//Check if the invited user is listed on user tab
			WaitForText(By.xpath("//*[@id=\"tableBody\"]/tr/td[1]"), Name[0]); //check first name
			WaitForText(By.xpath("//*[@id=\"tableBody\"]/tr/td[2]"), Name[2]); //check last name
			takeSnapShot(SCPath + "Invitation Sent.png");
			
			return new String[] {Email, UniqueID};
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
    }
    
}//End Class

/*	
	
	/////////////////////////////////////////////////////////////////////////////////
	
	public static String[] WFCL_AccountRegistration_GFBO(String Name[], String UserId, String AccountNumber, String AddressDetails[]) throws Exception{
		//The below applicable countries list was updated on 1/30/18
		String GFBOCountry_List[] = {"SV", "LU", "MO", "GU", "AE", "AN", "AR", "AT", "AU", "AW", "BB", "BE", "BH", "BM", "BR", "BS", "CH", "CL", "CN", "CR", "CZ", "DE", "DK", "DO", "EE", "ES", "FI", "FR", "GB", "GD", "GP", "GR", "GT", "HK", "HU", "IE", "IN", "IT", "JM", "JP", "KN", "KR", "KV", "KW", "KY", "LC", "LT", "LV", "MQ", "MX", "MY", "NL", "NO", "NZ", "PA", "PL", "PT", "RU", "SE", "SG", "TC", "TH", "TR", "TT", "TW", "US", "UY", "VC", "VE", "VG", "VI", "CA", "CW", "BQ", "SX", "MF", "BW", "LS", "MW", "MZ", "NA", "SI", "ZA", "SZ", "ZM", "VN", "BG", "HR", "RO", "SK", "QA", "PH", "AD", "AF", "AL", "AM", "AO", "AZ", "BA", "BD", "BF", "BI", "BJ", "BT", "BY", "CD", "CF", "CG", "CI", "CM", "CV", "CY", "DJ", "DZ", "EG", "ER", "ET", "GA", "GE", "GH", "GI", "GL", "GM", "GN", "GQ", "GW", "IL", "IQ", "IR", "IS", "JO", "KE", "KG", "KZ", "LB", "LI", "LK", "LR", "LY", "MA", "MC", "MD", "ME", "MG", "MK", "ML", "MR", "MT", "MU", "MV", "NE", "NG", "NP", "OM", "PK", "PS", "RE", "RS", "RW", "SA", "SC", "SD", "SL", "SN", "SO", "SY", "TD", "TG", "TM", "TN", "TZ", "UA", "UG", "UZ"};
 		if (!Arrays.asList(GFBOCountry_List).contains(AddressDetails[6])){
 			PrintOut("Please check that " + AddressDetails[6] + " is a valid country to register for GFBO. It is not valid in test levels as of 1/30/18");
 		}
 		TestData.LoadTime();
 		
 		try {
 			String WFCLPath = strTodaysDate + " " + strLevel + " WFCL GFBO ";
 			ChangeURL("https://" + strLevelURL + "/fcl/web/jsp/contactInfo.jsp?appName=fclgfbo&locale=" + AddressDetails[6].toLowerCase() + "_en&step3URL=https%3A%2F%2F" + strLevelURL + "%2Ffedexbillingonline%2Fregistration.jsp%3FuserRegistration%3DY%26locale%3Den_" + AddressDetails[6].toUpperCase() + "&afterwardsURL=https%3A%2F%2F" + strLevelURL + "%2Ffedexbillingonline%3F%26locale%3Den_" + AddressDetails[6].toUpperCase() + "&programIndicator");
 			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#reminderQuestion option[value=SP2Q1]")));//wait for secret questions to load.

	 		WFCL_ContactInfo_Page(Name, AddressDetails, UserId); //enters all of the details
	 		takeSnapShot(driver , WFCLPath + "ContactInformation.png");
		 	Click(By.id("iacceptbutton"));
		    
	 		//Step 2 Account information
	 		String AccountNickname = AccountNumber + "_" + AddressDetails[6];
	 		WFCL_AccountEntryScreen(AccountNumber, AccountNickname, WFCLPath);
	 		
		    //Confirmation page for FBO
	 		//wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selectbillingmedium1:j_idt24")));//the header for FBO
	 		if (CheckBodyText("FedEx Billing Online Registration")) {
	 			throw new Exception("Error on Confirmaiton page");
	 		}
	 		takeSnapShot(driver ,WFCLPath + "Confirmation.png");
	 		Click(By.id("selectbillingmedium1:continueId"));  
	 		
	 		//Registration Confirmation Page
	 		wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[1]/td[2]/table/tbody/tr[2]/td/b"), UserId));
	 		wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[2]/td[2]/table/tbody/tr[2]/td/b"), AccountNumber));
	 		wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[3]/td[2]/table/tbody/tr[2]/td/b"), AccountNickname));
	 		takeSnapShot(driver , WFCLPath + "RegistrationConfirmation.png");

			//Make sure the link on the confirmation page navigates to the correct page.
			wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[1]/td[4]/table/tbody/tr/td/table/tbody/tr[2]/td"), "        FedEx Billing Online"));			
			Click(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[1]/td[4]/table/tbody/tr/td/table/tbody/tr[1]/td[2]/a/img"));
			
			//The GFBO confirmation page based on if account number is set for invoices.
			if (!driver.findElements(By.id("splash2or3optionsForm:splash3OptionsSubmit")).isEmpty()){
				takeSnapShot(driver ,WFCLPath + "Congratulations.png");
				Click(By.id("splash2or3optionsForm:splash3OptionsSubmit"));
			}
			
			//Will now land on the GFBO page
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mainContentId:accSmyCmdLink")));//wait for the GFBO page to load
			takeSnapShot(driver ,WFCLPath + "Page.png");
			
			String UUID = GetCookieValue("fcl_uuid");
		    PrintOut("Finished WFCL_AccountRegistration_GFBO  " + UserId + "/" + strPassword + "--" + AccountNumber + "--" + UUID);
		    String ReturnValue[] = new String[] {UserId, AccountNumber, UUID};
		    WriteUserToFileAppend(ReturnValue);//Write User to file for later reference
		    return ReturnValue;
 		}catch (Exception e) {
 			GeneralFailure(e);
 			throw e;
 		}
 	}//end WFCL_AccountRegistration_GFBO
	
	public static void WFCL_GFBO_Login(String UserId, String Password, String Country) throws Exception{
		TestData.LoadTime();
 		
 		try {
 			ChangeURL("https://" + strLevelURL + "/fcl/?appName=fclgfbo&locale=" + Country.toLowerCase() + "_en&step3URL=https%3A%2F%2F" + strLevelURL + "%2Ffedexbillingonline%2Fregistration.jsp%3FuserRegistration%3DY%26locale%3Den_" + Country.toUpperCase() + "&returnurl=https%3A%2F%2F" + strLevelURL + "%2Ffedexbillingonline%3F%26locale%3Den_" + Country.toUpperCase() + "&programIndicator", true);
 			String WFCLPath = strTodaysDate + " " + strLevel + " WFCL GFBO ";
 			//Login to FBO
 			Type(By.name("username"), UserId);
 			Type(By.name("password"), Password);
 			Click(By.name("login"));
 			
 			//After page loads
 			if (CheckBodyText("FedEx Billing Online") && CheckBodyText("Account Summary") && CheckBodyText("Account Aging Summary")) { //checking body text as their page does not user consistent id's for the different elements.
 				captureScreenShot("WFCL", WFCLPath + "Login.png");
 			}else {
 				throw new Exception("Unable to validate GFBO landing page.");
 			}
 		}catch (Exception e){
 			GeneralFailure(e);
 		}
	}
	
 	
 	
	public static String[] WFCL_AccountRegistration_ISGT(String Name[], String UserId, String AccountNumber, String AddressDetails[]) throws Exception{
		String CountryCode = AddressDetails[6].toUpperCase();
 		TestData.LoadTime();
 		
 		try {
 			String WFCLPath = strTodaysDate + " " + strLevel + CountryCode + " WFCL ISGT ";

 			String UserRegister[] = WFCL_AccountRegistration(Name, UserId, AccountNumber, AddressDetails, WFCLPath);   //uerid, accountnumber, uuid is returned
 	
 			ChangeURL("https://" + strLevelURL + "/" + CountryCode.toLowerCase() + "/fcl/pckgenvlp/insight/");
 			Click(By.xpath("/html/body/div[1]/div[2]/div[2]/div/div[4]/a[1]"));//click the Log In link
 			
 			//Login with the user.       //need to recheck if this is expected.
 			Type(By.name("username"), UserId);
 			Type(By.name("password"), strPassword);
 			Click(By.name("login"));
 			
 			//The InSight confirmation page
 			ElementMatchesThrow(By.xpath("/html/body/table[2]/tbody/tr[2]/td/table[2]/tbody/tr/td[1]/table/tbody/tr[2]/td/table/tbody/tr[1]/td[1]/table/tbody/tr[2]/td/div/b"), UserId, 0);
 			captureScreenShot("WFCL", WFCLPath + " Confirmation.png");
 			String ReturnValue[] = {UserId, AccountNumber, UserRegister[2]};
		    WriteUserToFileAppend(ReturnValue);//Write User to file for later reference
		    return ReturnValue;
 		}catch (Exception e) {
 			GeneralFailure(e);
 			throw e;
 		}
 	}//end WFCL_AccountRegistration
 	

 	
	public static void WFCL_ReturnManager(String Name[], String UserId, String AccountNumber, String AddressDetails[]) throws Exception{       //////////Not finished
		TestData.LoadTime();
 		
 		try {
 			String WFCLPath = strTodaysDate + " " + strLevel + " WFCL GFBO ";
 			ChangeURL("https://" + strLevelURL + "/fcl/web/jsp/contactInfo.jsp?appName=fclgfbo&locale=" + AddressDetails[6].toLowerCase() + "_en&step3URL=https%3A%2F%2F" + strLevelURL + "%2Ffedexbillingonline%2Fregistration.jsp%3FuserRegistration%3DY%26locale%3Den_" + AddressDetails[6].toUpperCase() + "&afterwardsURL=https%3A%2F%2F" + strLevelURL + "%2Ffedexbillingonline%3F%26locale%3Den_" + AddressDetails[6].toUpperCase() + "&programIndicator");
 			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#reminderQuestion option[value=SP2Q1]")));//wait for secret questions to load.

	 		WFCL_ContactInfo_Page(Name, AddressDetails, UserId); //enters all of the details
	 		captureScreenShot("WFCL", WFCLPath + "ContactInformation.png");
	 		if (isPresent(By.xpath("//input[(@name='accountType') and (@value = 'linkAccount')]"))){
	 			Click(By.xpath("//input[(@name='accountType') and (@value = 'linkAccount')]"));//select the link account radio button
	 			Click(By.id("createUserID"));
	 		}else{
	 			Click(By.id("iacceptbutton"));
	 		}
		    
	 		//Step 2 Account information
	 		String AccountNickname = AccountNumber + "_" + AddressDetails[6];
	 		WFCL_AccountEntryScreen(AccountNumber, AccountNickname, WFCLPath);
	 		
		    //Confirmation page for FBO
	 		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("selectbillingmedium1:j_idt24")));//the header for FBO
	 		captureScreenShot("WFCL", WFCLPath + "Confirmation.png");
	 		Click(By.id("selectbillingmedium1:continueId"));
	 		
	 		//Registration Confirmation Page
	 		wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[1]/td[2]/table/tbody/tr[2]/td/b"), UserId));
	 		wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[2]/td[2]/table/tbody/tr[2]/td/b"), AccountNumber));
	 		wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[3]/td[2]/table/tbody/tr[2]/td/b"), AccountNickname));
			captureScreenShot("WFCL", WFCLPath + "RegistrationConfirmation.png");

			//Make sure the link on the confirmation page navigates to the correct page.
			wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[1]/td[4]/table/tbody/tr/td/table/tbody/tr[2]/td"), "        FedEx Billing Online"));	
			Click(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table/tbody/tr[1]/td[4]/table/tbody/tr/td/table/tbody/tr[1]/td[2]/a/img"));

			//The GFBO confirmation page based on if account number is set for invoices.
			if (!driver.findElements(By.id("splash2or3optionsForm:splash3OptionsSubmit")).isEmpty()){
				captureScreenShot("WFCL", WFCLPath + "Congratulations.png");
				Click(By.id("splash2or3optionsForm:splash3OptionsSubmit"));
			}
			
			//Will now land on the GFBO page
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mainContentId:accSmyCmdLink")));//wait for the GFBO page to load
			captureScreenShot("WFCL", WFCLPath + "Page.png");
	 		
		    PrintOut("Finished WFCL_AccountRegistration_GFBO  " + UserId + "/" + strPassword + "--" + AccountNumber);
 		}catch (Exception e) {
 			GeneralFailure(e);
 		}
 	}//end WFCL_ReturnManager
	
	
	public static void WFCL_CreditCardRegistration_Error(String CreditCardDetils[], String enrollmentid, String AddressDetails[], String Name[], String UserId, String ErrorMessage) throws Exception{
 		TestData.LoadTime();

 		try {
 			String WFCLPath = strTodaysDate + " " + strLevel + " WFCL ";
 			ChangeURL("https://" + strLevelURL + "/cgi-bin/ship_it/interNetShip?origincountry=us&locallang=us&urlparams=us");//go to the INET page just to load the cookies
 			ChangeURL("https://" + strLevelURL + "/fcl/ALL?enrollmentid=" + enrollmentid + "&OpenAccount=yes&language=en&country=" + AddressDetails[6].toLowerCase());
 			
 			//Step 1: Enter the contact information.
 			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#reminderQuestion option[value=SP2Q1]")));//wait for secret questions to load.
	 		WFCL_ContactInfo_Page(Name, AddressDetails, UserId); //enters all of the details
 			if ((AddressDetails[6].contentEquals("US") || AddressDetails[6].contentEquals("CA"))){
 				Click(By.xpath("//input[(@name='accountType') and (@value = 'openAccount')]"));//select the open account radio button
 			}
	 		
	 		Click(By.id("createUserID"));

	 		//Step 2: Enter the credit card details
	 		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("CCType")));
	 		WFCL_CC_Page(CreditCardDetils, AddressDetails[6], "WFCL", WFCLPath + "CC Information.png", false, false);
			ElementMatchesThrow(By.cssSelector("#invalidCreditcard > div"), ErrorMessage, 324229); 
			captureScreenShot("WFCL", WFCLPath + "OADR Error Message.png");
        }catch (Exception e) {
        	GeneralFailure(e);
        	throw e;
 		}
		
 	}//end WFCL_CreditCardRegistration_Error
	
	public static String[] WFCL_OpenAccountExistingUser(String UserId, String Password, String[] CreditCardDetils, String AddressDetails[]) throws Exception{
		try {		
	 		TestData.LoadTime();
	 		String WFCLPath = strTodaysDate + " " + strLevel + " WFCL OpenToExisting ";
			
	 		for (int i = 0; i < EnrollmentIDs.length; i++){
	 			if (EnrollmentIDs[i][1].contentEquals(AddressDetails[6].toUpperCase())){
	 				ChangeURL("https://" + strLevelURL + "/fcl/ALL?enrollmentid=" + EnrollmentIDs[i][0]);
	 				break;
	 			}else if (i == EnrollmentIDs.length - 1){
	 				PrintOut("Discount not found for " + AddressDetails[6].toUpperCase() + ".");
					throw new Exception("Discount not found");
	 			}
	 		}
			
	 		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
	 		Type(By.name("username"), UserId);
	 		Type(By.name("password"), Password);
	 		Click(By.name("login"));
	 		PrintOut("Logged in with " + UserId + "/" + Password);
	 		
	 		Click(By.xpath("//input[(@name='whichBillingAddress') and (@value = 'useNewAddress')]"));
	 		captureScreenShot("WFCL", WFCLPath + "Open.png");
	 		Click(By.name("continue"));
	 		
	 		//Step 2: Enter the credit card details
	 		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("CCType")));
	 		String UserDetails = driver.findElement(By.cssSelector("#billing-address-readonly > strong")).getText();
	 		CreditCardDetils = WFCL_CC_Page(CreditCardDetils, AddressDetails[6], "WFCL", WFCLPath + "CC Information.png", false, true);
	 		
			//Step 3: Confirmation Page
			try{
				//wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div[1]/h2")));//not sure why had .click here
				wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div[1]/div[2]/table/tbody/tr[1]/td[2]"), UserId));
				captureScreenShot("WFCL", WFCLPath + "CC Confirmation.png");
			}catch (Exception e){
				PrintOut("CC Confirmation page did not load.  " + UserDetails);
			}
	 		
			//Step 4: Check that account number is saved to user.
			String AccountNumber = driver.findElement(By.id("acctNbr")).getText();
			
			PrintOut("WFCL_OpenAccountExistingUser Did not fully complete, need to add a check to make sure account number created is linked to user. " + AccountNumber);
			PrintOut(UserDetails);
			return new String[] {UserId, AccountNumber};
		}catch (Exception e) {
			GeneralFailure(e);
			throw e;
		}
	}//end WFCL_OpenAccountExistingUser
	
	public static String[] WFCL_RewardsRegistration(String Name[], String UserId, String AccountNumber, String AddressDetails[]) throws Exception{
		boolean RewardsFlag = false;
 		String CountryCode = AddressDetails[6].toUpperCase();
 		TestData.LoadTime();
 		
 		try {
 			String WFCLPath = strTodaysDate + " " + strLevel + " " + CountryCode + " WFCL ";
 			ChangeURL(("https://" + strLevelURL + "/fcl/ALL?enrollmentid=cc16323314&fedId=Epsilon&accountOption=link"), true);
 		 	Click(By.name("signupnow"));
	 		wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#reminderQuestion option[value=SP2Q1]")));//wait for secret questions to load.
		 	WFCL_ContactInfo_Page(Name, AddressDetails, UserId); //enters all of the details
		 	captureScreenShot("WFCL", WFCLPath + "ContactInformation.png");
		 	Click(By.id("createUserID"));
 			
	 		//Step 2 Account information
	 		String AccountNickname = AccountNumber + "_" + CountryCode;
	 		WFCL_AccountEntryScreen(AccountNumber, AccountNickname, WFCLPath);
	 		
	 		wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div[1]/div[2]/table/tbody/tr[1]/td[2]"), UserId));
	 		wait.until(ExpectedConditions.textToBe(By.xpath("//*[@id='rightColumn']/table/tbody/tr/td[1]/div/div[1]/div[2]/table/tbody/tr[2]/td[2]"), AccountNumber));
		    captureScreenShot("WFCL", WFCLPath + "RegistrationConfirmation.png");
		    String UUID = GetCookieValue("fcl_uuid");
		    //Continue to My FedEx Rewards Link on the confirmation page
		    if (Environment >= 6) {//rewards only has a connection on L6/LP
		    	 Click(By.xpath("//*[@id='shipnow']/font"));
		    	 //need to add step to confirm rewards page
		    	 captureScreenShot("WFCL", WFCLPath + "Rewards Page.png");
		    	 RewardsFlag = true;
		    }else {
		    	PrintOut("Warning, Rewards only has connection on L6/Lp unable to validte rewards page.");
		    }
		    
		    PrintOut("Finished WFCL_AccountRegistration  " + UserId + "/" + strPassword + "--" + AccountNumber + "--" + UUID);
		    String ReturnValue[] = new String[] {UserId, AccountNumber, UUID, "Rewards:" + RewardsFlag};
		    WriteUserToFileAppend(ReturnValue);//Write User to file for later reference
		    return ReturnValue;
 		}catch (Exception e) {
 			GeneralFailure(e);
 			throw e;
 		}
 	}//end WFCL_RewardsRegistration
 

	//will return a string array with 0 as the user id and 1 as the password, 2 is the uuid
	public static boolean WFCL_Captcha(String UserId, String[] Name, String AddressDetails[]) throws Exception{
 		TestData.LoadTime();
 		
 		try {
 			String WFCLPath = strTodaysDate + " " + strLevel + " WFCL ";
 		//Testing the newer gui
 			
 			ChangeURL("https://" + strLevelURL + "/fcl/web/jsp/contactInfo1.jsp?appName=oadr&locale=" + AddressDetails[6].toLowerCase() + "_en&step3URL=https%3A%2F%2F.fedex.com%3A443%2Ffcl%2Fweb%2Fjsp%2FfclValidateAndCreate.jsp&afterwardsURL=https%3A%2F%2F" + strLevelURL + "%3A443%2Ffcl%2Fweb%2Fjsp%2Foadr.jsp&programIndicator=p314t9n1ey&accountOption=link&captcha=true");
 			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#reminderQuestion option[value=SP2Q1]")));//wait for secret questions to load.
 			
 			//select the no account radio button
	 		WFCL_ContactInfo_Page(Name, AddressDetails, UserId); //enters all of the details
	 		Click(By.xpath("//input[(@name='accountType') and (@value = 'noAccount')]"));
	 		Click(By.id("createUserID"));

	 		WaitForText(By.id("directions-verbose-label"), "Type the moving characters");
	 		WaitForText(By.id("directions-label"), "Moving characters:");
	 		Click(By.id("my-nucaptcha-refresh"));
	 		captureScreenShot("WFCL", WFCLPath + "Image Captcha.png");
	 		
	 		Click(By.id("my-nucaptcha-audio"));
	 		WaitForText(By.id("directions-verbose-label"), "Type the characters you hear in the audio");
	 		WaitForText(By.id("directions-label"), "Characters you hear:");
	 		captureScreenShot("WFCL", WFCLPath + "Audio Captcha.png");
	 		
	 		//Enter an invalid answer
	 		Type(By.id("nucaptcha-answer"), "Wrong");
	 		Click(By.id("createUserID"));
	 		
	 		return true;
 		}catch (Exception e) {
 			GeneralFailure(e);
 			throw e;
 		}
 	}//end WFCL_Captcha
	
	//will return a string array with 0 as the user id and 1 as the password, 2 is the uuid
	public static boolean WFCL_Captcha_Legacy(String UserId, String[] Name, String AddressDetails[]) throws Exception{
 		TestData.LoadTime();
 		
 		try {
 			String WFCLPath = strTodaysDate + " " + strLevel + " WFCL Legacy ";
	 	//Now test the WFCL legacy, in this case testing the WDPA page
	 		String CountryCode = AddressDetails[6].toUpperCase();
	 		ChangeURL("https://" + strLevelURL + "/PickupApp/login?locale=en_" + CountryCode + "&programIndicator=4", true);
	 		Click(By.name("signupnow"));
	 		String currentURL =  driver.getCurrentUrl();
	 		ChangeURL(currentURL + "&captcha=true", false);//update the url to captcha
	 		WFCL_ContactInfo_Page(Name, AddressDetails, UserId); //enters all of the details
	 		Click(By.id("iacceptbutton"));
	 			
	 		WaitForText(By.id("directions-verbose-label"), "Type the moving characters");
	 		WaitForText(By.id("directions-label"), "Moving characters:");
	 		Click(By.id("my-nucaptcha-refresh"));
	 		MoveTo(By.id("iacceptbutton"));//added to make sure screenshots cover the area
	 		captureScreenShot("WFCL", WFCLPath + "Image Captcha.png");
	 		
	 		Click(By.id("my-nucaptcha-audio"));
	 		WaitForText(By.id("directions-verbose-label"), "Type the characters you hear in the audio");
	 		WaitForText(By.id("directions-label"), "Characters you hear:");
	 		MoveTo(By.id("iacceptbutton"));//added to make sure screenshots cover the area
	 		captureScreenShot("WFCL", WFCLPath + "Audio Captcha.png");

	 		return true;
 		}catch (Exception e) {
 			GeneralFailure(e);
 			throw e;
 		}
 	}//end WFCL_Captcha

    public static String[] WFCL_GFBO_Invitaiton(String UserID, String Password, String Name[], String Email, String UserRole) throws Exception{
		TestData.LoadTime();
	 		
		try {
			String WFCLPath = strTodaysDate + " " + strLevel + " GFBO Invite ";
			
			Login(UserID, Password);
			ChangeURL("https://" + strLevelURL + "/fedexbillingonline?locale=en_US");
			captureScreenShot("WFCL", WFCLPath + "UserTable before invite.png");
			
			Click(By.cssSelector("a#mainContentId\\3a myOptnCmdLink")); 
			Click(By.cssSelector("a#mainContentId\\3a manageUsersId")); 
			Click(By.xpath("//*[@value='Invite new user']"));//invite new user button, the id and other directions don't work and change on each attempt.
			Type(By.xpath("//*[@id='mainContentId:j_idt227']"), Name[0]);//first name
			Type(By.xpath("//*[@id='mainContentId:j_idt232']"), Name[2]);//last name
			Type(By.xpath("//*[@id='mainContentId:j_idt237']"), Email);//email
			
			Select(By.xpath("//*[@id='mainContentId:j_idt242']"), UserRole, "v"); //2=standard  3=view only
			
			captureScreenShot("WFCL", WFCLPath + "Invitation");
			Click(By.cssSelector("input#mainContentId\\3a inviteNewuserButton"));

			PrintOut("Finished " + Thread.currentThread().getStackTrace()[2].getMethodName());
			return new String[] {UserID, Arrays.toString(Name), Email};
		}catch (Exception e) {
			GeneralFailure(e);
			throw e;
		}
    }
    
}

*/
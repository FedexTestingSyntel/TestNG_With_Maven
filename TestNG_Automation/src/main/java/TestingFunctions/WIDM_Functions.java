package TestingFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import org.openqa.selenium.By;
import SupportClasses.DriverFactory;

public class WIDM_Functions extends Helper_Functions{
	
	public static String strPassword = "Test1234";
	
	public static String WIDM_Registration(String Level, String AddressDetails[], String Name[], String UserId) throws Exception{
		String Time = CurrentDateTime();
		String CountryCode = AddressDetails[6];
		String SCPath = Time + " L" + Level + " WIDM ";

		try {
			ChangeURL("WIDM", CountryCode, true, Level);
			Click(By.linkText("Sign Up Now!"));

			//Enter all of the form data
			WIDM_Registration_Input(AddressDetails, Name, UserId);
			takeSnapShot(SCPath + "RegistrationConfirmation.png");
			Click(By.id("createUserID"));

			//confirmation page
			WaitForText(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td[2]/table[2]/tbody/tr[3]/td/table[2]/tbody/tr/td[1]/table/tbody/tr[2]/td/b"), UserId);
			String UUID = GetCookieValue("fcl_uuid");
			PrintOut("WIDM_Registration Completed: " + UserId + "/" + strPassword + "--" + UUID, true);
			takeSnapShot(SCPath + "Registration WIDM Confirmation.png");
			String ReturnValue[] = new String[] {UserId, strPassword, UUID};
			WriteUserToExcel(Level, UserId, strPassword);
			return Arrays.toString(ReturnValue);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}//end WIDM_Registration

	public static String WIDM_RegistrationEFWS(String Level, String AddressDetails[], String Name[], String UserId) throws Exception{
		String Time = CurrentDateTime();
		String CountryCode = AddressDetails[6];
		String SCPath = Time + " L" + Level + " WIDM ";

		try {
			ChangeURL("WIDM", CountryCode, true, Level);
			Click(By.linkText("Sign Up Now!"));
			
			//Enter all of the form data
			WIDM_Registration_Input(AddressDetails, Name, UserId);

			//enter the invalid email address
			String EfwsEmail = "robmus50@yahoo.com";
			Type(By.id("email"), EfwsEmail);
			Type(By.id("retypeEmail"), EfwsEmail);
			takeSnapShot(SCPath + "Registration EFWS.png");
			Click(By.id("createUserID"));

			//Check that the correct error message appears.
			WaitForText(By.cssSelector("#module\\2e registration\\2e _expanded > table > tbody > tr:nth-child(2) > td > b"), "Your registration request is not approved based on the information submitted.");
			PrintOut("WIDM_Registration_EFWS Completed with: " + EfwsEmail, true);
			takeSnapShot(SCPath + "Registration EFWS Message.png");
			return UserId + " " + EfwsEmail;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}//end WIDM_Registration_EFWS
	
	public static void WIDM_Registration_Input(String AddressDetails[], String Name[], String UserId) throws Exception{
		Type(By.id("firstName"), Name[0]);
		Type(By.id("lastName"), Name[2]);
		Type(By.id("email"), MyEmail);
		Type(By.id("retypeEmail"), MyEmail);
		Type(By.id("address1"), AddressDetails[0]);
		Type(By.id("address2"), AddressDetails[1]);
		if (isPresent(By.id("city"))) {
			Type(By.id("city"), AddressDetails[2]);
		}

		if (AddressDetails[4] != ""){
			Type(By.id("state"),AddressDetails[4]);
		}
		
		if (AddressDetails[5] != ""){
			Type(By.id("zip"),AddressDetails[5]);
		}

		if (isPresent(By.xpath("//*[@id='module.registration._expanded']/table/tbody/tr/td[1]/table/tbody/tr[24]/td[2]"))) {
			ElementMatches(By.xpath("//*[@id='module.registration._expanded']/table/tbody/tr/td[1]/table/tbody/tr[24]/td[2]"), "Country/Territory", 116678);
		}else {
			ElementMatches(By.xpath("//*[@id='module.registration._expanded']/table/tbody/tr/td[1]/table/tbody/tr[23]/td[2]"), "Country/Territory", 116678); //for non us
		}
		
		if (AddressDetails[6].toLowerCase().contains("ca")) {
			//for Canada the list populates ca_english or ca_french
			Select(By.id("country1"), "ca_english", "v");
		}else{
			Select(By.id("country1"), AddressDetails[6].toLowerCase(), "v");
		}

		Type(By.id("phone"), ValidPhoneNumber(AddressDetails[6]));
		Type(By.id("uid"), UserId); 
		PrintOut("Userid is: " + UserId, true);
		Type(By.id("password"), strPassword);
		Type(By.id("retypePassword"), strPassword);
		Select(By.id("reminderQuestion"), "What is your mother's first name?", "t");
		Type(By.id("reminderAnswer"), "mom");
		Click(By.id("acceptterms"));
		if (!DriverFactory.getInstance().getDriver().findElement(By.id("acceptterms")).isSelected()){//added this as there is an issue with error messages and script clicking checkbox, works normally manually.
			Click(By.id("acceptterms"));
		}
	}//end WIDM_Registration_Input

	public static String WIDM_Registration_ErrorMessages(String Level, String AddressDetails[], String Name[], String UserId) throws Exception{
		String Time = CurrentDateTime();
		String CountryCode = AddressDetails[6];
		String SCPath = Time + " L" + Level + " WIDM ";

		try {
			ArrayList<String> ResultsList = new ArrayList<String>();
			ChangeURL("WIDM", CountryCode, true, Level);
			Click(By.linkText("Sign Up Now!"));

			Click(By.id("createUserID"));
			ResultsList.add(ElementMatches(By.id("firstempty"), "First name is required.", 1014978) + " 1014978");

			Type(By.id("firstName"), "!!!First");
			Click(By.id("createUserID"));
			takeSnapShot(SCPath + "Error Messages.png");
			ResultsList.add(ElementMatches(By.id("firstspecialchar"), "Invalid character in First name.", 1014978) + " 1014978");
			ResultsList.add(ElementMatches(By.id("lastempty"), "Last name is required.", 1014980) + " 1014980");
			ResultsList.add(ElementMatches(By.id("emailempty"), "Email address is required.", 1) + " 1");
			ResultsList.add(ElementMatches(By.id("reemailempty"), "Retyped email address is required.", 2) + " 2");
			ResultsList.add(ElementMatches(By.id("add1empty"), "Address 1 is required.", 3) + " 3");
			//ResultsList.add(ElementMatches(By.id(""), "", ) + " ");
			//ResultsList.add(ElementMatches(By.id(""), "", ) + " ");

			//ResultsList.add(ElementMatches(By.id(""), "", ) + " ");

			Type(By.id("lastName"), "L");
			Click(By.id("createUserID"));
			ResultsList.add(ElementMatches(By.id("invalidlength"), "Last name must be at least 2 characters.", 1014980) + " 1014980");
		
			Type(By.id("lastName"), "!!Last");
			Click(By.id("createUserID"));
			ResultsList.add(ElementMatches(By.id("lnspecialchar"), "Invalid character in Last name.", 1014980) + " 1014980");

			Type(By.id("lastName"), "!@#$%^&*()");
			Click(By.id("createUserID"));
			ResultsList.add(ElementMatches(By.id("lnspecialchar"), "Invalid character in Last name.", 1019256) + " 1019256");

			Type(By.id("firstName"), Name[0]);
			Type(By.id("uid"), UserId);

			//Still need to add the remaining scenarios
			String Failures = "";
			for (String FailedChecks: ResultsList) {
				if (FailedChecks.contains("false")) {
					Failures += FailedChecks + " ";
				}
			}
			return Failures;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}//end WIDM_Registration_Input

	public static String ResetPasswordWIDM_Secret(String Level, String CountryCode, String strUserName, String NewPassword, String SecretAnswer) throws Exception{
		if(strUserName == null){
			PrintOut("Cannot login with user id as null. Recieved from " + Thread.currentThread().getStackTrace()[2].getMethodName(), true);
			throw new Exception("Userid required.");
		}
		String Time = CurrentDateTime();
		String SCPath = Time + " L" + Level + " WIDM ";
		
		ChangeURL("WIDM", CountryCode, true, Level);

		Click(By.name("forgotUidPwd"));//click the forgot password link
		Type(By.name("userID"), strUserName);
		takeSnapShot(SCPath + "Password Reset " + NewPassword + ".png");
		Click(By.xpath("//*[@id='module.forgotuseridandpassword._expanded\']/table/tbody/tr/td[1]/form/table/tbody/tr[6]/td/input[2]"));//click option 1 for reset through user id
		Click(By.xpath("//*[@id='module.resetpasswordoptions._expanded']/table/tbody/tr/td[1]/form/table/tbody/tr[5]/td/input"));//click the option 1 button and try to answer with secret question
		Type(By.name("answer"), SecretAnswer);
		takeSnapShot(SCPath + "Reset Password Secret " + NewPassword + ".png");
		Click(By.name("action1"));
		
		//wait for the new password text box appears. If doesn't with throw and error and try the next password
		WebDriver_Functions.WaitPresent(By.name("password"));
		Type(By.name("password"),NewPassword);
		Type(By.name("retypePassword"),NewPassword);
		takeSnapShot(SCPath + "New Password " + NewPassword + ".png");
		Click(By.name("confirm"));
		WebDriver_Functions.WaitForText(By.xpath("//*[@id='content']/div/table/tbody/tr[1]/td/table/tbody/tr/td/table/tbody/tr[1]/td"), "Thank you. Your password has been reset");
		boolean loginAttempt = Login(Level, strUserName, NewPassword);
		PrintOut(strUserName + " has had the password changed to " + NewPassword, true);
		if (NewPassword.contentEquals(strPassword)){
			return NewPassword;
		}else if (loginAttempt){
			return strUserName + " " + NewPassword + " " +  ResetPasswordWIDM_Secret(Level, CountryCode, strUserName, strPassword, SecretAnswer);//change the password back
		}else{
			PrintOut("Not able to verify changed password", true);
			throw new Exception("Not able to complete with generic secret questions.");
		}
	}//end ResetPasswordWIDM

	public static String Reset_Password_WIDM_Email(String Level, String strUserName, String Password) throws Exception{
		String Time = CurrentDateTime();
		String SCPath = Time + " L" + Level + " WIDM ";

		try{
			Login(Level, strUserName, Password);

			//trigger the password reset email
			ChangeURL("WPRL", "US", false, Level);
			WaitPresent(By.id("ci_fullname_val"));
			String UserDetails = DriverFactory.getInstance().getDriver().findElement(By.id("ci_fullname_val")).getText();
			String Email = UserDetails.substring(UserDetails.lastIndexOf('\n') + 1, UserDetails.length());
			ChangeURL("WIDM", "US", true, Level);
			Click(By.name("forgotUidPwd"));
			Type(By.name("userID"),strUserName);

			Click(By.xpath("//*[@id='module.forgotuseridandpassword._expanded']/table/tbody/tr/td[1]/form/table/tbody/tr[6]/td/input[2]"));
			//click the option 1 button and try to answer with secret question
			Click(By.name("action2"));
			WaitPresent(By.id("linkaction"));
			takeSnapShot(SCPath + "Password Reset Email.png");
			PrintOut("Completed ResetPasswordWIDM using " + strUserName + ". An email has been triggered and that test must be completed manually by " + Email, true);

			return strUserName + Email;
		}catch(Exception e){
			e.printStackTrace();
	throw e;
		}
	}//end ResetPasswordWIDM

	public static String Forgot_User_WIDM(String Level, String Email) throws Exception{
		String Time = CurrentDateTime();
		String SCPath = Time + " L" + Level + " WIDM ";

		try{	
			ChangeURL("WIDM", "US", true, Level);
			Click(By.name("forgotUidPwd"));
			//wait for text box for user id to appear
			Type(By.name("email"),Email);
			takeSnapShot(SCPath + "Forgot User Id.png");
			Click(By.xpath("//*[@id='module.forgotuseridandpassword._expanded']/table/tbody/tr/td[3]/form/table/tbody/tr[6]/td/input[2]"));
			takeSnapShot(SCPath + "Forgot User Confirmation.png");
			PrintOut("Completed Forgot User Confirmation using " + Email + ". An email has been triggered and that test must be completed manually by to see the user list.", true);

			return "Email sent to + " + Email;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}//end ResetPasswordWIDM
}

package TestingFunctions;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;

import org.openqa.selenium.By;
import org.testng.Assert;

public class WPRL_Functions {
	
	//need to redo this whole class

	public static String[] WPRL_Contact(String Level, String User, String Password, String AddressDetails[], String Name[], String Phone[][], String Email) throws Exception{
	 	String CountryCode = AddressDetails[6];
		try {
			String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WPRL ";
			
			Helper_Functions.Login(Level, User, Password);
			Helper_Functions.ChangeURL("WPRL", CountryCode, false, Level);
			//edit the contact information
	 		WPRL_Contact_Input(AddressDetails, Name, Phone, Email, "ci");
	 		Helper_Functions.takeSnapShot(SCPath + "Login contactEdit.png");
		}catch (Exception e) {
			throw e;
		}
		return new String[] {User, Password};
	}//end WPRL_Contact
	
	public static String[] WPRL_LoginInformation(String Level, String CountryCode, String User, String Password) throws Exception{	
		try {
			String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WPRL ";
			
			Helper_Functions.Login(Level, User, Password);
			Helper_Functions.ChangeURL("WPRL", CountryCode, false, Level);
	
			WPRL_Contact_LoginInformation(Password);
			Helper_Functions.takeSnapShot(SCPath + "Login contactEdit.png");
		}catch (Exception e) {
			throw e;
		}
		return new String[] {User, Password};
	}//end WPRL_Contact
	
	public static ArrayList<String> WPRL_Contact_Get(String Level, String[] User){
		ArrayList<String> Contact = new ArrayList<String>();
		for (int i = 0; i < User.length; i++) {
			try {
				if (!Helper_Functions.Login(Level, User[i], "Test1234")){throw new Exception();}
				Helper_Functions.ChangeURL("WPRL", "US", false, Level);
				WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
				Helper_Functions.Wait(1);
				
				WebDriver_Functions.WaitPresent(By.id("ci_fullname_val"));
				String ContactDetails = WebDriver_Functions.GetText(By.id("ci_fullname_val"));
				Contact.add(User[i] + "_____" + ContactDetails);
				Helper_Functions.PrintOut(User[i] + "_____" + ContactDetails, true);
			}catch (Exception e) {}
		}
		for(int i = 0; i < Contact.size(); i++) {
            Helper_Functions.PrintOut(Contact.get(i), true);
        }
		return Contact;
	}//end WPRL_Contact
	
	public static boolean WPRL_AccountNickname(boolean PasskeyFlag, String Nickname, String App, String SCPath) throws Exception{
		try{
			WebDriver_Functions.WaitForText(By.id("nns_moduleHeader"), "Nickname and Security Options");
			//check if the user has the edit link visible. Administrators will not be able to edit.
			if (!PasskeyFlag){
				WebDriver_Functions.Click(By.id("edit"));
				WebDriver_Functions.Click(By.id("nns_acctnicknm_input"));
				WebDriver_Functions.Type(By.id("nns_acctnicknm_input"), Nickname);
				WebDriver_Functions.Click(By.id("nns_savebtn"));
				WebDriver_Functions.WaitForText(By.id("nns_update_msg"), "Your updates have been saved.");
				Helper_Functions.takeSnapShot(SCPath + "NicknameEdit.png");
				return true;
			}else{
				Helper_Functions.PrintOut("Cannot update nickname for passkey users.", true);
				return true;
			}
		}catch(Exception e){
			Helper_Functions.PrintOut("Not able to edit nickname", true);
			
			throw e;
		}
	}
	
	public static boolean WPRL_Account_ShippingAddress(String Name[], String AddressDetails[], String App, String SCPath) throws Exception{
		try{
			if (WebDriver_Functions.isPresent(By.cssSelector("#shippingaddress > #show-hide > div.fx-toggler > #moduleHeader"))){
				assertEquals("Shipping Address", WebDriver_Functions.GetText(By.cssSelector("#shippingaddress > #show-hide > div.fx-toggler > #moduleHeader")));
				WebDriver_Functions.Click(By.cssSelector("#shippingaddress > #show-hide > div.fx-toggler > #edit"));
				WebDriver_Functions.WaitPresent(By.cssSelector("#sAddrCountrySelect option[value=US]"));

				//if the current postal code is same as new address update to refresh the city field section.
				WebDriver_Functions.Type(By.id("sAddrZipTxtBox"), "12345");
				
				WebDriver_Functions.ElementMatches(By.id("sAddrCountryLbl"), "Country/Territory", 116602);
				WebDriver_Functions.Type(By.id("sAddrFirstNameTxtBox"),Name[0]);
				WebDriver_Functions.Type(By.id("sAddrMiddleInitialTxtBox"),Name[1]);
				WebDriver_Functions.Type(By.id("sAddrLastNameTxtBox"),Name[2]);
				WebDriver_Functions.Type(By.id("sAddrZipTxtBox"),AddressDetails[5]);
				WebDriver_Functions.Type(By.id("sAddrLine1TxtBox"),AddressDetails[0]);
				WebDriver_Functions.Type(By.id("sAddrLine2TxtBox"),AddressDetails[1]);
		
				WebDriver_Functions.WaitPresent(By.cssSelector("#sAddrCitySelect option[value=" + AddressDetails[2].toUpperCase() + "]"));
				WebDriver_Functions.Select(By.id("sAddrCitySelect"), AddressDetails[2].toUpperCase(), "t");
						
				if (!AddressDetails[3].isEmpty()){
					WebDriver_Functions.WaitPresent(By.cssSelector("#sAddrStateSelect option[value=" + AddressDetails[4].toUpperCase() + "]"));
					WebDriver_Functions.Select(By.id("sAddrStateSelect"), AddressDetails[4], "v");
				}
				
				WebDriver_Functions.Type(By.id("sAddrPrimaryPhoneTxtBox"), "9011111111");////////Update dynamic later
				WebDriver_Functions.Type(By.id("sAddrEmailTxtBox"), Helper_Functions.MyEmail);
				WebDriver_Functions.Click(By.id("sAddrSaveBtn"));
				WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
				WebDriver_Functions.WaitForText(By.id("sAddrUpdatedTxt"), "Your updates have been saved.");
				Helper_Functions.takeSnapShot(SCPath + "Shipping Address.png");
				return true;
			}else {
				Helper_Functions.PrintOut("Shipping address section not present.", true);
				return true;
			}
		}catch(Exception e){
			Helper_Functions.PrintOut("Not able to verify Shipping Address Section", true);
			
			throw e;
		}
	}
	
	public static boolean WPRL_Account_CreditCard(String Name[], String AddressDetails[], String CardDetails[], String App, String SCPath) throws Exception{
		try{
			if (WebDriver_Functions.isPresent(By.cssSelector("#creditcardinformation > #show-hide > div.fx-toggler > #moduleHeader"))){
				assertEquals("Credit Card and Billing Information", WebDriver_Functions.GetText(By.cssSelector("#creditcardinformation > #show-hide > div.fx-toggler > #moduleHeader")));
				WebDriver_Functions.Click(By.cssSelector("#creditcardinformation > #show-hide > div.fx-toggler > #edit"));
				
				WebDriver_Functions.WaitForText(By.cssSelector("#credit-card-details-page-heading > p"), "Enter the credit card information for the card you want to associate with this FedEx account. A credit card is required for billing purposes.");
				WebDriver_Functions.WaitPresent(By.cssSelector("#card-state-fld option[value=" + AddressDetails[4].toUpperCase() + "]"));
				WebDriver_Functions.ElementMatches(By.id("card-country-lbl"), "Country/Territory", 116603);
				WebDriver_Functions.Type(By.id("card-first-name-fld"), Name[0]);
				WebDriver_Functions.Type(By.id("card-middle-name-fld"), Name[1]);
				WebDriver_Functions.Type(By.id("card-last-name-fld"), Name[2]);
				WebDriver_Functions.Type(By.id("card-company-fld"), "Company Here");
				WebDriver_Functions.WaitPresent(By.cssSelector("#card-type-fld option[value=" + CardDetails[0].toUpperCase() + "]"));
				WebDriver_Functions.Select(By.id("card-type-fld"), CardDetails[0].toUpperCase(), "t");
				WebDriver_Functions.Type(By.id("card-number-fld"), CardDetails[1]);
					
				
				WebDriver_Functions.WaitPresent(By.cssSelector("#card-expiration-month-fld option[value=Dec]"));
				WebDriver_Functions.Select(By.id("card-expiration-month-fld"), CardDetails[3], "t");		
						
				//Note that the below is hard coded for last two digits of the year
				WebDriver_Functions.Select(By.id("card-expiration-year-fld"), "20" + CardDetails[4], "t");

				WebDriver_Functions.Type(By.id("card-security-code-fld"), CardDetails[2]);
				WebDriver_Functions.Type(By.id("card-address1-fld"), AddressDetails[0]);
				WebDriver_Functions.Type(By.id("card-address1-fld"), AddressDetails[1]);
				WebDriver_Functions.Type(By.id("card-address2-fld"), AddressDetails[5]);
				WebDriver_Functions.Type(By.id("card-city-fld"), AddressDetails[2]);
				WebDriver_Functions.Click(By.cssSelector("#card-state-fld > option"));
				
				WebDriver_Functions.Select(By.id("card-state-fld"), AddressDetails[3], "t");
				
				WebDriver_Functions.Type(By.id("card-phone-fld"), "9011111111");/////////update later, dynamic
				WebDriver_Functions.Type(By.id("card-email-fld"), Helper_Functions.MyEmail);
				WebDriver_Functions.Type(By.id("card-email-retype-fld"), Helper_Functions.MyEmail);
				
				WebDriver_Functions.Click(By.id("card-continue-save-btn"));
				WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
				WebDriver_Functions.WaitForText(By.id("cci_update_msg"), "Your updates have been saved.");
				Helper_Functions.takeSnapShot(SCPath + "CC Update.png");
				return true;
			}else {
				Helper_Functions.PrintOut("Warning: WPRL_Account_CreditCard not executed as the billing section not loaded.", true);
				return true;
			}
		}catch(Exception e){
			Helper_Functions.PrintOut("Not able to update Credit Card and Billing Information", true);
			
			throw e;
		}
	}
	
	public static boolean WPRL_Account_Billing_Address(String Name[], String AddressDetails[], String CardDetails[], String App, String SCPath) throws Exception{
		try{
			if (WebDriver_Functions.isPresent(By.cssSelector("#billingaddress > div:nth-child(1) > div:nth-child(1) > span:nth-child(1)"))){
				WebDriver_Functions.Click(By.cssSelector("#billingaddress > div:nth-child(1) > div:nth-child(1) > span:nth-child(1)"));

				WebDriver_Functions.WaitPresent(By.cssSelector("#bAddrCountrySelect option[value=" + AddressDetails[6].toUpperCase() + "]"));
				WebDriver_Functions.WaitPresent(By.cssSelector("#bAddrStateSelect option[value=" + AddressDetails[4].toUpperCase() + "]"));
				WebDriver_Functions.ElementMatches(By.id("bAddrCountryLbl"), "Country/Territory", 116603);
				
				WebDriver_Functions.Type(By.id("bAddrContactNameTxtBox"), Name[0] + " " + Name[2]);
				WebDriver_Functions.Click(By.id("bAddrSaveBtn"));
				WebDriver_Functions.WaitForTextNot(By.id("bAddrUpdatedTxt"), ("Your updates have been saved."));
				Helper_Functions.takeSnapShot(SCPath + "Invoice Billing Address Update.png");
				return true;
			}else {
				Helper_Functions.PrintOut("Warning: WPRL_Account_Billing_Address not executed as the billing section not loaded.", true);
				return true;
			}
		}catch(Exception e){
			Helper_Functions.PrintOut("Not able to update Invoice Billing Address", true);
			
			throw e;
		}
	}

	public static String[] WPRL_AccountManagement(String Level, String User, String Password, String AddressDetails[], String CardDetails[], String Name[]) throws Exception{
		//https://wwwdev.idev.fedex.com/apps/myprofile/accountmanagement/?locale=en_US&cntry_code=us
		boolean Nickname = false, FedExOnlineSolutions = false, ShippingAddress = false, CreditCard = false, Invoice = false;
		String CountryCode = AddressDetails[6];
		try {
			String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WPRL ";

			Helper_Functions.Login(Level, User, Password);
			boolean UserPasskey = WebDriver_Functions.CheckifPasskey(Level);
			// launch the browser and direct it to the Base URL
			
			Helper_Functions.ChangeURL("WPRL_ACC", CountryCode, false, Level);
			
			WebDriver_Functions.WaitForText(By.id("accountheader"), "Account Management");
			//edit a listed account
			if(WebDriver_Functions.isPresent(By.xpath("//*[@id='alertBar']"))){
				if (WebDriver_Functions.GetText(By.xpath("//*[@id='alertBar']")).contentEquals("You currently do not have any accounts associated with your fedex.com profile. Click the link below if you would like to add an account")){
					Helper_Functions.PrintOut("User does not have account numbers", true);
					return new String[] {"No Account Number", User};
				}
			}

			WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
			WebDriver_Functions.WaitPresent(By.id("accountsubheader"));
			WebDriver_Functions.Click(By.xpath("//a[contains(text(),'View/Edit')]"));
			//Edit Account Nickname
			Nickname = WPRL_AccountNickname(UserPasskey, Helper_Functions.CurrentDateTime(), "WPRL", SCPath);

			//FedEx Online Solutions
			try{
				if (WebDriver_Functions.isPresent(By.xpath("(//p[@id='moduleHeader'])[3]"))){
					WebDriver_Functions.ElementMatches(By.xpath("(//p[@id='moduleHeader'])[3]"), "FedEx Online Solutions", 0);
					WebDriver_Functions.Click(By.cssSelector("#onlinesolutions > #show-hide > div.fx-toggler > #edit"));
					Helper_Functions.takeSnapShot(SCPath + "OnlineSolutionsModel.png");
					WebDriver_Functions.Click(By.id("os_cancelbtn"));
					FedExOnlineSolutions = true;
				}else {
					Helper_Functions.PrintOut("FedEx Online Solutions Modal not present", true);
				}
			}catch(Exception e){
				Helper_Functions.PrintOut("Not able to verify Online soluitions section", true);
			}

			//Shipping Address section
			ShippingAddress = WPRL_Account_ShippingAddress(Name, AddressDetails, "WPRL", SCPath);
 					
			//Credit Card and Billing Information
			CreditCard = WPRL_Account_CreditCard(Name, AddressDetails, CardDetails, "WPRL", SCPath);
				
			//Invoice Billing Address
			Invoice = WPRL_Account_Billing_Address(Name, AddressDetails, CardDetails, "WPRL", SCPath);
		}catch (Exception e) {
			throw e;
		}
		return new String[] {"Nickname: " + Nickname, "FedExOnlineSolutions: " + FedExOnlineSolutions, "ShippingAddress: " + ShippingAddress, "CreditCard:" + CreditCard, "Invoice:" + Invoice};
	}//end WPRL_Contact

	
	public static String[] WPRL_FDM(String Level, String CountryCode, String User, String Password, String AddressDetails[], String CardDetails[], String Name[]) throws Exception{
		//https://wwwdev.idev.fedex.com/apps/myprofile/deliverymanager/?locale=en_us&cntry_code=us
		
		try {
			String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WPRL ";
			Helper_Functions.Login(Level, User, Password);
			Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
			WebDriver_Functions.WaitPresent(By.id("notifAddNotificationsLink"));
			WebDriver_Functions.WaitForText(By.id("moduleHeader"), "Recipient Contact Information");
			WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present

			//if there is already a credit card linked remove it
			try{
				if (WebDriver_Functions.isPresent(By.id("cci_remove"))){
					WebDriver_Functions.Click(By.id("cci_remove"));
					WebDriver_Functions.Click(By.id("dialog-yes"));
					WebDriver_Functions.WaitForText(By.id("cii_header_info"), "You can store your credit card information for future use when requesting special delivery options for shipments coming to your home address");
					Helper_Functions.PrintOut("Removed existng CC", true);
				}
			}catch(Exception e){
				Helper_Functions.PrintOut("Failure when removing existing credit card", true);
			}
	 			
// Recipient Contact Information
			try{
				WebDriver_Functions.WaitForTextNot(By.id("rc_country_val"), (""));
				WebDriver_Functions.Click(By.xpath("//*[@id='edit']"));
				WPRL_Contact_Input(AddressDetails, Name, Helper_Functions.LoadPhone_Mobile_Fax_Email("US"), Helper_Functions.MyEmail, "rc");
				WebDriver_Functions.Click(By.id("rc_savebtn"));
			
				//confirm that updates have been made
				WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
				if(WebDriver_Functions.isPresent(By.id("general-errors-contact"))){
					Helper_Functions.PrintOut("--" + WebDriver_Functions.GetText(By.id("general-errors-contact")) + "--", true);
					Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
					WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
					throw new Exception();
				}
				WebDriver_Functions.WaitForText(By.id("rc_update_msg"), "Your updates have been saved.");
				Helper_Functions.takeSnapShot(SCPath + "FDM contactEdit.png");
			}catch(Exception e){
				Helper_Functions.PrintOut("Not able to update contact information ", true);
				Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
				e.printStackTrace();
				WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
			}
			
// Test the Credit Card Section
			try{
				WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
				WebDriver_Functions.Click(By.cssSelector("#creditcardinformation > #show-hide > div.fx-toggler > #edit"));
				WPRL_CreditCard_Input(AddressDetails, CardDetails, Name, Helper_Functions.myPhone, Helper_Functions.MyEmail);
				WebDriver_Functions.Click(By.id("card-continue-save-btn"));
				WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
				WebDriver_Functions.WaitPresent(By.cssSelector("#general-errors-creditcard > p"));
				Assert.assertNotSame("Error Message\nFedEx cannot process your credit card request with the information entered. Please verify all credit card data and resubmit your request.",WebDriver_Functions.GetText(By.cssSelector("#general-errors-creditcard > p")));
				WebDriver_Functions.WaitForText(By.id("cci_update_msg"), "Your updates have been saved.");
				WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
				Helper_Functions.takeSnapShot(SCPath + "FDMCC added.png");
			    
				//remove the credit card that was just added
				try{
					WebDriver_Functions.Click(By.id("cci_remove"));
					WebDriver_Functions.Click(By.id("dialog-yes"));
					WebDriver_Functions.WaitForText(By.id("cii_header_info"), "You can store your credit card information for future use when requesting special delivery options for shipments coming to your home address");
					Helper_Functions.takeSnapShot(SCPath + "FDMCC removed.png");
				}catch(Exception e){
					Helper_Functions.PrintOut("Not able to remove newly added CC ", true);
				}
			}catch(Exception e){
				Helper_Functions.PrintOut("Not able to update CC information ", true);
				Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
				e.printStackTrace();
				WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
			}

//check if can update the Alternate names
			try{
				//remove existing alternate names if present.
				try{
					WebDriver_Functions.WaitPresent(By.id("an_header_info_show"));

					while (WebDriver_Functions.GetText(By.id("an_header_info_show")).contentEquals("You have entered the following nicknames or alternative spellings of your name.")){
						WebDriver_Functions.Click(By.cssSelector("#alternatenames > #show-hide > div.fx-toggler > #edit"));
						WebDriver_Functions.Click(By.xpath("//*[@id='alternateNamesDivEdit0']/div/a"));
						WebDriver_Functions.Click(By.id("an_savebtn"));
						WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
						WebDriver_Functions.WaitForText(By.id("an_update_msg"), "Your updates have been saved.");
					}
				}catch(Exception e){
					Helper_Functions.PrintOut("Not able to update alternative names", true);
				}
				
				WebDriver_Functions.WaitForText(By.cssSelector("#alternatenames > #show-hide > div.fx-toggler > #moduleHeader"), "Alternate Names");
				WebDriver_Functions.Click(By.cssSelector("#alternatenames > #show-hide > div.fx-toggler > #edit"));
				WebDriver_Functions.Click(By.id("add_an_link"));
				WebDriver_Functions.Type(By.id("alternateNamesTextBoxEdit1"), Helper_Functions.CurrentDateTime());

				WebDriver_Functions.Click(By.id("an_savebtn"));
				WebDriver_Functions.WaitForText(By.id("an_update_msg"), "Your updates have been saved.");
				WebDriver_Functions.WaitForText(By.id("an_header_info_show"), "You have entered the following nicknames or alternative spellings of your name.");	
			
				//delete the name that was just added.
				WebDriver_Functions.Click(By.cssSelector("#alternatenames > #show-hide > div.fx-toggler > #edit"));
				WebDriver_Functions.Click(By.xpath("//*[@id='alternateNamesDivEdit0']/div/a"));
				WebDriver_Functions.Click(By.id("an_savebtn"));
				WebDriver_Functions.WaitForText(By.id("an_update_msg"), "Your updates have been saved.");
			}catch(Exception e){
				Helper_Functions.PrintOut("Not able to update alternative names", true);
				Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
				e.printStackTrace();
				WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
			}

// Test the Notifications Sections
			try{
				WebDriver_Functions.Click(By.xpath("(//span[@id='edit'])[4]"));
				try{//remove all existing notification options.
					String element[] = {"notifAddressedToMeChkBox", "notifDayBeforeDeliveryChkBox", "notifDayOfDeliveryChkBox", "notifDeliveryIssueChkBox", "notifReadyForPickupChkBox", "notifDeliveryMadeChkBox", "notifScheduleUpdatedChkBox"};
					for (int i = 0; i < element.length * 2; i++ ){
						int j = i % element.length;
						if (WebDriver_Functions.isSelected(By.id(element[j]))){
							WebDriver_Functions.Click(By.id(element[j]));
						}
					}
				}catch (Exception e2){}
	 				
				WebDriver_Functions.Click(By.id("notifAddressedToMeChkBox"));
				WebDriver_Functions.WaitPresent(By.cssSelector("#notifAddressedToMeNotifySelect option[value=EMAIL]"));//WebDriver_Functions.WaitForTextPresentInElement(By.id("notifAddressedToMeNotifySelect"), "Email"));
				WebDriver_Functions.Select(By.id("notifAddressedToMeNotifySelect"), "Email", "t");
				WebDriver_Functions.WaitPresent(By.id("notifEmailTxt"));
				WebDriver_Functions.Type(By.id("notifEmailTxt"), Helper_Functions.MyEmail);

				Helper_Functions.takeSnapShot(SCPath + "FDMNotif.png");
				WebDriver_Functions.Click(By.id("notifSaveBtn"));
				WebDriver_Functions.WaitForText(By.id("notifUpdatedTxt"), "Your updates have been saved.");
				WebDriver_Functions.WaitForText(By.cssSelector("#notifSummaryNotificationTypesGrp > ul > li"), "An email to " + Helper_Functions.MyEmail + " when FedEx has a package addressed to me.");
				Helper_Functions.takeSnapShot(SCPath + "FDMNotifSaved.png");
					    
				// Remove the Notification made above
	 			WebDriver_Functions.WaitPresent(By.linkText("View/Edit"));
	 			WebDriver_Functions.Click(By.cssSelector("#headerToggle > #edit"));
	 			WebDriver_Functions.Click(By.id("notifAddressedToMeChkBox"));
	 			WebDriver_Functions.Click(By.id("notifSaveBtn"));
	 			WebDriver_Functions.WaitForText(By.id("notifUpdatedTxt"), "Your updates have been saved.");
				Helper_Functions.takeSnapShot(SCPath + "FDMNotifRemoved.png");
	 		}catch(Exception e){
	 			Helper_Functions.PrintOut("Not able to update the notification section " + e.getCause(), true);
	 			Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
	 			e.printStackTrace();
	 	 		WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
	 		}
					
//check if can update the recipient name
	 		try{
	 			if (Level.contentEquals("7") || Level.contentEquals("6")){
	 				Helper_Functions.PrintOut("Can not automate updating recipeint name in " + Level, true);
	 				throw new Exception();
	 			}
	 			WebDriver_Functions.Click(By.cssSelector("#recipientNameTitle > #edit"));

	 			WebDriver_Functions.Type(By.id("rn_firstname_input"), Name[0]);
	 			WebDriver_Functions.Type(By.id("rn_lastname_input"), Name[2]);
	 			WebDriver_Functions.Click(By.id("rn_savebtn"));
	 			WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
	 			WebDriver_Functions.WaitForText(By.id("examauthsubheader"), "Name change requires authentication of your existing delivery addresses.");
	 			WebDriver_Functions.Click(By.id("ea_contbtn"));
	 			WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
	 			WebDriver_Functions.Click(By.id("ea_examcontbtn"));

	 			WebDriver_Functions.WaitForText(By.id("ExamUIModuleHeading"), "Validate your registration");
	 			WebDriver_Functions.Click(By.xpath("(//input[@name='answer0'])[4]"));
	 			WebDriver_Functions.Click(By.xpath("(//input[@name='answer1'])[4]"));
	 			WebDriver_Functions.Click(By.xpath("(//input[@name='answer2'])[4]"));
	 			WebDriver_Functions.Click(By.xpath("(//input[@name='answer3'])[4]"));
	 			WebDriver_Functions.Click(By.id("ExamUIModuleContinueValue"));
				WebDriver_Functions.WaitNotPresent(By.id("werlDialogDiv"));//wait for the loading overlay to not be present
				WebDriver_Functions.WaitForText(By.id("instructionaltext"),"The authentication for the address was successful.");
				WebDriver_Functions.Click(By.id("ea_confirmcontbtn"));
				WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
				WebDriver_Functions.WaitForText(By.cssSelector("#recipientNameTitle > #moduleHeader"), "Delivery Address(es) for : "+ Name[0] + " " + Name[2]);
	 		}catch(Exception e){
	 			Helper_Functions.PrintOut("Not able to update the recipient name", true);
	 			Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
	 			e.printStackTrace();
	 	 		WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
	 		}
			
//check editing the delivery address options
	 		try{
	 			WebDriver_Functions.Click(By.linkText("View/Edit"));
	 			for(int FDMAddressLine = 1;;FDMAddressLine++){
	 				//update Delivery Instructions
	 				WebDriver_Functions.WaitForText(By.cssSelector("#deliveryinstructions > #show-hide > div.fx-toggler > #moduleHeader"), "Delivery Instructions");
	 				if (WebDriver_Functions.isPresent(By.id("di_general-errors-instructions"))){
	 					Helper_Functions.PrintOut("--" +WebDriver_Functions.GetText(By.id("di_general-errors-instructions")) + "--", true);
	 				}
	 				WebDriver_Functions.Click(By.cssSelector("#deliveryinstructions > #show-hide > div.fx-toggler > #edit"));

	 				WebDriver_Functions.WaitPresent(By.cssSelector("#di_leavepackageat_input option[value=GARAGE]"));
	 				if (WebDriver_Functions.GetText(By.id("di_leavepackageat_input")).contains("Garage")){
	 					WebDriver_Functions.Select(By.id("di_leavepackageat_input"), "Side Door", "t");
	 				}else{
	 					WebDriver_Functions.Select(By.id("di_leavepackageat_input"), "Garage", "t");
	 				}
	 				WebDriver_Functions.Type(By.id("di_helpusfindyouraddress_input"), "Red Door");
	 				WebDriver_Functions.Click(By.id("di_savebtn"));
	 				WebDriver_Functions.WaitForText(By.id("di_update_msg"), "Your updates have been saved.");
					
	 				//update Vacation Hold
	 				WebDriver_Functions.WaitForText(By.cssSelector("#vacationhold > #show-hide > div.fx-toggler > #moduleHeader"), "Vacation Hold");
	 				if (WebDriver_Functions.isPresent(By.id("vh_general-errors-vacationhold"))){
	 					Helper_Functions.PrintOut("--" + WebDriver_Functions.GetText(By.id("vh_general-errors-vacationhold")) + "--", true);
	 				}
	 				WebDriver_Functions.Click(By.cssSelector("#vacationhold > #show-hide > div.fx-toggler > #edit"));
	 				Calendar cal = Calendar.getInstance();
	 				int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH) + 1;//need to make better, will not work for last day of the month
	 				String dayOfMonthStr = String.valueOf(dayOfMonth);
	 				WebDriver_Functions.Click(By.id("vh_begindate_input"));
	 				WebDriver_Functions.Click(By.linkText(dayOfMonthStr));//click the date on the calendar widget
	 				//the end date will be loaded by default
	 				WebDriver_Functions.Click(By.id("vh_savebtn"));
	 				WebDriver_Functions.WaitForText(By.id("vh_update_msg"), "Your updates have been saved.");

	 				try{	//if the user has multiple addresses then try to update them as well.
	 					if (FDMAddressLine == 1){
	 						WebDriver_Functions.Click(By.xpath("//tr[2]/td[4]/a"));//nested to save on wait time
	 					}else if (FDMAddressLine == 2){
	 						WebDriver_Functions.Click(By.xpath("//tr[3]/td[4]/a"));
	 					}else{
	 						break;
	 					}
	 				}catch (Exception e){
	 					break;
	 				}//check if multiple FDM addresses
	 			}//end for FDMAddressLine
	 		}catch(Exception e){
	 			Helper_Functions.PrintOut("Not able to update the delivery options", true);
	 			Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level); 
	 			e.printStackTrace();
	 	 		WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
	 		}
	 	}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return new String[] {"Need to add details"};
	}//end WPRL_FDM
	 	
	
	public static boolean WPRL_FDM_RemoveNotifications(String Level, String CountryCode, String User, String Password){
		try {
			Helper_Functions.Login(Level, User, Password);
			Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
			
			WebDriver_Functions.WaitPresent(By.id("headerbar"));
			WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
			try{//remove all existing notification options.
				if (WebDriver_Functions.CheckBodyText("Sign up for FedEx® Delivery Manager.")) {
					return false;
				}
				if (!WebDriver_Functions.CheckBodyText("Click [here] to receive notifications for your inbound residential shipments.")){
					WebDriver_Functions.Click(By.xpath("(//span[@id='edit'])[4]"));
					String element[] = {"notifAddressedToMeChkBox", "notifDayBeforeDeliveryChkBox", "notifDayOfDeliveryChkBox", "notifDeliveryIssueChkBox", "notifReadyForPickupChkBox", "notifDeliveryMadeChkBox", "notifScheduleUpdatedChkBox"};
					for (int i = 0; i < element.length * 2; i++ ){
						int j = i % element.length;
						if (WebDriver_Functions.isSelected(By.id(element[j]))){
							WebDriver_Functions.Click(By.id(element[j]));
							//longwait.until(ExpectedConditions.elementToBeClickable(By.id(element[j])));
						}
					}
					WebDriver_Functions.Click(By.id("notifSaveBtn"));
				}
				WebDriver_Functions.Click(By.xpath("//*[@id='cancelEnrollment']/a"));
				WebDriver_Functions.Click(By.id("dialog-yes"));
			}catch (Exception e2){}
		}catch(Exception e){
			 Helper_Functions.PrintOut("Not able to update the notification section " + e.getCause(), true);
			 try {
				Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
				WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
			} catch (Exception e1) {
			}
			 e.printStackTrace();
			
			 return false;
		}

		return true;		
	}
	//will except the Enrollment attempts as either "questions" or "postcard"
	public static String[] WPRL_FDM_Enrollemnt(String Level, String User, String Password, String AddressDetails[], String Enrollment) throws Exception{
	 		//https://wwwdev.idev.fedex.com/apps/myprofile/deliverymanager/?locale=en_us&cntry_code=us


	 		try {
	 			String CountryCode = AddressDetails[6];
	 			String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WPRL ";
	 			//need to change login to only take country, and app
	 			Helper_Functions.Login(Level, User, Password);
	 			// launch the browser and direct it to the Base URL
	 			Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
	 			WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
	 			WebDriver_Functions.Click(By.id("signUpNow"));
	 			if (Enrollment.contentEquals("postcard") && AddressDetails.length >= 8){
	 				WERL_Enrollment(Level, Enrollment, SCPath, AddressDetails[7]);
	 			}else{
	 				if (Enrollment.contentEquals("postcard")){
	 					Helper_Functions.PrintOut("Share id is not loaded for this address", true);
	 				}
	 				WERL_Enrollment(Level, "questions", SCPath, null);
	 			}
	 			

	 			//confirmation will appear
	 			try{
		 			WebDriver_Functions.WaitNotPresent(By.id("werlDialogDiv"));//wait for the WERL loading overlay to not be present
		 			WebDriver_Functions.WaitPresent(By.id("regConfModuleEmailValue"));
		 			WebDriver_Functions.Type(By.id("regConfModuleEmailValue"), Helper_Functions.MyEmail);

		 			Helper_Functions.takeSnapShot(SCPath + "FDM Confirmation.png");
		 			WebDriver_Functions.Click(By.id("regConfModuleModalSaveValue"));
	 			}catch (Exception e) {}

	 			//navigate back to WPRL FDM PAGE
	 			Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
	 			WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
	 			WebDriver_Functions.WaitPresent(By.id("recipientheader"));//check if the header appears	
	 			WebDriver_Functions.WaitNotPresent(By.id("signUpNow"));
	 		}catch (Exception e) {
	 			if (e.getMessage().contains("Expected condition failed: waiting for element to be clickable: By.id: signUpNow")){
	 				Helper_Functions.PrintOut("This user is already enrolled for FDM.", true);
	 			}else{
	 				
	 			}
	 		}//end main try catch
		return new String[] {User, Password, AddressDetails[0]};
	 	}//end WPRL_FDM_Enrollemnt

	public static void WPRL_FDM_Enrollemnt_Add(String Level, String User, String Password, String AddressDetails[], String Enrollment) throws Exception{
 		//https://wwwdev.idev.fedex.com/apps/myprofile/deliverymanager/?locale=en_us&cntry_code=us

 		try {
 			String CountryCode = AddressDetails[6];
 			String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WPRL ";
 			//need to change login to only take country, and app
 			Helper_Functions.Login(Level, User, Password);
 			// launch the browser and direct it to the Base URL
 			Helper_Functions.ChangeURL("WPRL_FDM", CountryCode, false, Level);
 			WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
 			WebDriver_Functions.Click(By.id("addAddress"));
 			WebDriver_Functions.WaitPresent(By.id("da_streetone_input"));
 			WebDriver_Functions.Type(By.id("da_zip_input"), AddressDetails[5]);
 			WebDriver_Functions.Type(By.id("da_streetone_input"), AddressDetails[0]);
 			WebDriver_Functions.Type(By.id("da_streettwo_input"), AddressDetails[1]);
 			WebDriver_Functions.Select(By.cssSelector("#da_city_select"), AddressDetails[2].toUpperCase(), "v");
 			WebDriver_Functions.Select(By.cssSelector("#da_state_input"), AddressDetails[4].toUpperCase(), "v");
 			Helper_Functions.takeSnapShot(SCPath + "FDM Add Address.png");
 			WebDriver_Functions.Click(By.id("da_savebtn"));
 			
 			WERL_Enrollment(Level, Enrollment, SCPath, AddressDetails[7]);
 			WebDriver_Functions.WaitNotPresent(By.id("werlDialogDiv"));//wait for the WERL loading overlay to not be present
 			WebDriver_Functions.WaitPresent(By.id("regConfModuleDescription"));
 			if (AddressDetails[0].contentEquals("")){
 				WebDriver_Functions.WaitForText(By.xpath("//*[@id='recInfo']/span[2]"), AddressDetails[0]);
 			}else{//Check this part
 				WebDriver_Functions.WaitForText(By.xpath("//*[@id='recInfo']/span[2]"), AddressDetails[0] + " " + AddressDetails[1]);
 			}
 			
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 	}//end WPRL_FDM_Enrollemnt_Add

	public static void WPRL_FDM_CancelEnrollemnt(String Level, String User, String Password) throws Exception{
 		try {
 			String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WPRL ";
 			//need to change login to only take country, and app
 			Helper_Functions.Login(Level, User, Password);
 			
 			Helper_Functions.ChangeURL("WPRL_FDM", "US", false, Level);
 			WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
 			WebDriver_Functions.Click(By.cssSelector("#cancelEnrollment > a"));
 			Helper_Functions.takeSnapShot(SCPath + "FDM Cancelation.png");
 			WebDriver_Functions.Click(By.id("dialog-yes"));
 			WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
 			Helper_Functions.ChangeURL("WPRL_FDM", "US", false, Level);
 			WebDriver_Functions.WaitPresent(By.id("signUpNow"));
 			Helper_Functions.takeSnapShot(SCPath + "FDM Cancelation Confim.png");
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 	}//end WPRL_FDM_CancelEnrollemnt

	public static boolean WERL_Enrollment(String Level, String Enrollment, String SCPath, String ShareID) throws Exception{
		if (Enrollment == null) {
			Enrollment = "postcard";
		}
		
		try{//this may fail if the user does not have access to all of the different registration options.
			WebDriver_Functions.WaitNotPresent(By.id("werlDialogDiv"));//wait for the WERL loading overlay to not be present
			switch (Enrollment) {
			case "questions": 
				WebDriver_Functions.Click(By.id("authOptionsModuleQuestionsRadioValue"));
				WebDriver_Functions.WaitNotPresent(By.id("werlDialogDiv"));//wait for the WERL loading overlay to not be present
				WebDriver_Functions.Click(By.id("optionsExamModuleStartValue"));
				break;
			case "postcard":
				WebDriver_Functions.Click(By.id("authOptionsModuleMailCodeRadioValue"));
				WebDriver_Functions.WaitNotPresent(By.id("werlDialogDiv"));//wait for the WERL loading overlay to not be present
				WebDriver_Functions.Click(By.id("optionsSendCodeValue"));
				break;
			}
			WebDriver_Functions.WaitNotPresent(By.id("werlDialogDiv"));//wait for the WERL loading overlay to not be present
		}catch(Exception e){
			if (WebDriver_Functions.isPresent(By.id("examStartModuleErrorMessage"))){
				//The address you provided is commercial or business address. These services are for residential addresses only. Please provide a residential address and try again.
				Helper_Functions.PrintOut(WebDriver_Functions.GetText(By.id("examStartModuleErrorMessage")), true);
				return false;
			}else if (WebDriver_Functions.isPresent(By.id("optionsSendCodeValue"))){
				//Only the postcard option is present
				if (Enrollment != "postcard") {
					Helper_Functions.PrintOut("Unable to select desired enrollment option. Attempting with Postcard.", true);
				}
				WebDriver_Functions.Click(By.id("optionsSendCodeValue"));
			}else if (!Enrollment.contentEquals("questions")){
				Helper_Functions.PrintOut("Unable to select desired enrollment option. Attempting with Exam Questions.", true);
				Enrollment = "questions";
			}
		}
			
		switch (Enrollment) {
		case "questions": 
			for (int i = 0; i < 4; i++){//select the 4 questions
				WebDriver_Functions.Click(By.xpath("(//input[@name='answer" + i + "'])[4]"));
			}
			Helper_Functions.takeSnapShot(SCPath + "FDM Questions.png");
			WebDriver_Functions.Click(By.id("optionsExamModuleContinueValue"));		
			break;
		case "postcard":
			String uuid = WebDriver_Functions.GetCookieValue("fcl_uuid");
			Integer PinNumber = TestNG.MFAC.IssuePinExternal(Level, uuid + "-" + ShareID, "POSTAL");
			//confirmation on request submitted
			WebDriver_Functions.WaitPresent(By.id("postcardConfModuleDescription"));
			Helper_Functions.takeSnapShot(SCPath + "FDM Registration.png");	
			//enter the pin number
			Helper_Functions.ChangeURL("WPRL_FDM", "US", false, Level);
			WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));//wait for the loading overlay to not be present
			
			if (WebDriver_Functions.isPresent(By.id("activationLink"))) {
				//user is doing initial enrollment
				WebDriver_Functions.Click(By.id("activationLink"));
			}else if (WebDriver_Functions.isPresent(By.id("pendingLink"))) {
				//user is adding an additional address
				WebDriver_Functions.Click(By.id("pendingLink"));
			}

			WebDriver_Functions.WaitPresent(By.id("postcardPinEntryModulePinValue"));
			WebDriver_Functions.Type(By.id("postcardPinEntryModulePinValue"), PinNumber.toString());
			WebDriver_Functions.Click(By.id("postcardPinEntryModuleActivateValue"));
			break;
		}
		return true;
	}
	
	public static void WPRL_Contact_LoginInformation(String Password) throws Exception{
		//edit the login information
		try{
			WebDriver_Functions.Click(By.id("edit"));
			WebDriver_Functions.WaitForText(By.id("useridRules"), "Must be at least 6 characters");
			WebDriver_Functions.WaitForText(By.id("passRules"), "Password must use at least 8 characters and contain one upper case letter, one lower case letter and one numeric character.");
			WebDriver_Functions.Type(By.id("lg_pw_input"), Password);
			WebDriver_Functions.Type(By.id("lg_newpw_input"), Password + "5");
			WebDriver_Functions.Type(By.id("lg_retypenewpw_input"), Password + "5");
			WebDriver_Functions.WaitPresent(By.cssSelector("#lg_secquestion_input option[value=SP2Q1]"));//wait for secret questions to load.
			WebDriver_Functions.Select(By.id("lg_secquestion_input"), "What is your mother's first name?", "t");
			WebDriver_Functions.Type(By.id("lg_secanswer_input"), "mom");
			WebDriver_Functions.Click(By.id("lg_savebtn"));
			WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
			
			WebDriver_Functions.WaitForText(By.id("updateSuccessful"), "Your updates have been saved.");
			//edit the password back
			try{
				WebDriver_Functions.Click(By.id("edit"));
				WebDriver_Functions.WaitForText(By.id("useridRules"), "Must be at least 6 characters");
				WebDriver_Functions.WaitForText(By.id("passRules"), "Password must use at least 8 characters and contain one upper case letter, one lower case letter and one numeric character.");
				WebDriver_Functions.Type(By.id("lg_pw_input"), Password + "5");
				WebDriver_Functions.Type(By.id("lg_newpw_input"), Password);
				WebDriver_Functions.Type(By.id("lg_retypenewpw_input"), Password);
				WebDriver_Functions.WaitPresent(By.cssSelector("#lg_secquestion_input option[value=SP2Q1]"));//wait for secret questions to load.
				
				WebDriver_Functions.Select(By.id("lg_secquestion_input"), "What is your mother's first name?", "t");
				WebDriver_Functions.Type(By.id("lg_secanswer_input"), "mom");
				WebDriver_Functions.Click(By.id("lg_savebtn"));
				WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
				WebDriver_Functions.WaitForText(By.id("updateSuccessful"), "Your updates have been saved.");
			}catch(Exception e){
				Helper_Functions.PrintOut("Not able to edit the password back", true);
				throw e;
			}//end editing password back
		}catch(Exception e){
			Helper_Functions.PrintOut("Not able to update the login infromation", true);
			throw e;
		}//end edit the login information
	}//end for WPRL_Contact_LoginInformation
	
	//String Name[] = {FirstName - 0, Middle Name - 1, Last Name - 2}
	//AddressDetails[] =  {Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, countryCode - 6}
	//String Phone[] = {Phone - 0, Mobile - 1, Fax - 2}
	//Subsection is the code for the element id needing to be updated.
	public static void WPRL_Contact_Input(String AddressDetails[], String Name[], String Phone[][], String Email, String Subsection) throws Exception{
		try{
			WebDriver_Functions.Click(By.cssSelector("#contactinfo > #show-hide > div.fx-toggler > #edit"));
			WebDriver_Functions.WaitPresent(By.cssSelector("#" + Subsection + "_country_input option[value=" + AddressDetails[6].toUpperCase() + "]"));
			
			if(WebDriver_Functions.isPresent(By.id("countryText"))) {
				WebDriver_Functions.ElementMatches(By.id("countryText"), "Country/Territory", 116603);
			}else if (WebDriver_Functions.isPresent(By.id(Subsection + "_countryLabel"))) {
				WebDriver_Functions.ElementMatches(By.id(Subsection + "_countryLabel"), "Country/Territory", 116603);
			}
			
			//The below two waits were not working, check later if better way to stall.
			//WebDriver_Functions.WaitPresent(By.cssSelector("#" + Subsection + "_country_input option[value=US]")));
			//WebDriver_Functions.WaitPresent(By.cssSelector("#" + Subsection + "_state_input option[value=Please select]")));
			
			if (WebDriver_Functions.GetValue(By.id(Subsection + "_zip_input")).contentEquals(AddressDetails[5])){ //refresh the city field if updating same address zip code
				WebDriver_Functions.Type(By.id(Subsection + "_zip_input") , "12345");
				WebDriver_Functions.Type(By.id(Subsection + "_firstname_input") , "12345");
				WebDriver_Functions.Type(By.id(Subsection + "_zip_input") , AddressDetails[5]);
				WebDriver_Functions.Type(By.id(Subsection + "_firstname_input"), "12345");
			}

			WebDriver_Functions.Select(By.id(Subsection + "_country_input"), AddressDetails[6].toUpperCase(), "v");
			WebDriver_Functions.Type(By.id(Subsection + "_firstname_input"), Name[0]);
			WebDriver_Functions.Type(By.id(Subsection + "_lastname_input"), Name[2]);
			WebDriver_Functions.Type(By.id(Subsection + "_zip_input"), AddressDetails[5]);
			WebDriver_Functions.Type(By.id(Subsection + "_streetone_input"), AddressDetails[0]);
			WebDriver_Functions.Type(By.id(Subsection + "_streettwo_input"), AddressDetails[1]);

			if (WebDriver_Functions.isPresent(By.id("#" + Subsection + "_city_input_select"))){
				WebDriver_Functions.Click(By.cssSelector("#" + Subsection + "_city_input_select option[value=" + AddressDetails[2].toUpperCase() + "]"));
				WebDriver_Functions.Select(By.cssSelector("#" + Subsection + "_city_input_select"), AddressDetails[2].toUpperCase(), "v");
			}else if (WebDriver_Functions.isPresent(By.id("#" + Subsection + "_city_select"))){
				WebDriver_Functions.Click(By.cssSelector("#" + Subsection + "_city_select option[value=" + AddressDetails[2].toUpperCase() + "]"));
				WebDriver_Functions.Select(By.cssSelector("#" + Subsection + "_city_select"), AddressDetails[2].toUpperCase(), "v");
			}

			WebDriver_Functions.WaitPresent(By.cssSelector("#" + Subsection + "_state_input option[value=" + AddressDetails[4].toUpperCase() + "]"));
			WebDriver_Functions.Select(By.cssSelector("#" + Subsection + "_state_input"), AddressDetails[4].toUpperCase(), "v");

			WebDriver_Functions.Type(By.id(Subsection + "_phone_input"), Phone[0][1]);
			
			if (Subsection.contentEquals("ci")){
				WebDriver_Functions.Type(By.id(Subsection + "_middle_input"), Name[1]);
				WebDriver_Functions.Select(By.id(Subsection + "_phone_prefix_select"), Phone[0][0], "t");
				WebDriver_Functions.Select(By.id(Subsection + "_mobile_prefix_select"), Phone[1][0], "t");
				WebDriver_Functions.Type(By.id(Subsection + "_mobile_input"), Phone[1][1]);
				WebDriver_Functions.Select(By.id(Subsection + "_fax_prefix_select"), Phone[2][0], "t");
				WebDriver_Functions.Type(By.id(Subsection + "_fax_input"), Phone[2][1]);
			}else if (Subsection.contentEquals("rc")){
				WebDriver_Functions.Type(By.id(Subsection + "_middleinitial_input"), Name[1]);
			}
			WebDriver_Functions.Type(By.id(Subsection + "_email_input"), Email);
			WebDriver_Functions.Click(By.id("ci_savebtn"));
			WebDriver_Functions.WaitNotVisable(By.id("LoadingDiv"));//wait for the loading overlay to not be present
			WebDriver_Functions.WaitForText(By.id("ci_update"), "Your updates have been saved.");
		}catch(Exception e){
			Helper_Functions.PrintOut("Not able to update the " + Subsection + " information. Sent from " + Thread.currentThread().getStackTrace()[2].getMethodName(), true);
			e.printStackTrace();
			throw e;
		}
	}//end WPRL_Contact_ContactInformatoin

	public static void WPRL_CreditCard_Input(String AddressDetails[], String CardDetails[], String Name[], String Phone, String Email){
		try{
			WebDriver_Functions.WaitPresent(By.cssSelector("#card-state-fld option[value=" + AddressDetails[4].toUpperCase() + "]"));
			WebDriver_Functions.Type(By.id("card-first-name-fld"), Name[0]);
			WebDriver_Functions.Type(By.id("card-middle-name-fld"), Name[1]);
			WebDriver_Functions.Type(By.id("card-last-name-fld"), Name[2]);
			WebDriver_Functions.Type(By.id("card-company-fld"), "Company Here");
			
			WebDriver_Functions.WaitPresent(By.cssSelector("#card-type-fld option[value=" + CardDetails[0].toUpperCase() + "]"));
			WebDriver_Functions.Select(By.id("card-type-fld"), CardDetails[0].toUpperCase(), "t");
			WebDriver_Functions.Type(By.id("card-number-fld"), CardDetails[1]);

			try{//Credit Card Expiration Month
				WebDriver_Functions.WaitPresent(By.cssSelector("#card-expiration-month-fld option[value=Dec]"));
				WebDriver_Functions.Select(By.id("card-expiration-month-fld"), CardDetails[3], "t");
			}catch (Exception e){
				Helper_Functions.PrintOut("Not able to select exp month", false);
			}
					
					
			try{//Note that the below is hard coded for last two digits of the year
				WebDriver_Functions.Select(By.id("card-expiration-year-fld"), "20" + CardDetails[4], "t");
			}catch (Exception e){Helper_Functions.PrintOut("Not able to select exp year", false);}
					
			WebDriver_Functions.Type(By.id("card-security-code-fld"), CardDetails[2]);
			WebDriver_Functions.ElementMatches(By.id("card-country-lbl"), "Country/Territory", 116605);
			WebDriver_Functions.Type(By.id("card-address1-fld"), AddressDetails[0]);
			WebDriver_Functions.Type(By.id("card-address2-fld"), AddressDetails[1]);
			WebDriver_Functions.Type(By.id("card-zip-fld"), AddressDetails[5]);
			WebDriver_Functions.Type(By.id("card-city-fld"), AddressDetails[2]);
			WebDriver_Functions.Click(By.cssSelector("#card-state-fld > option"));
			
			try{
				WebDriver_Functions.Select(By.id("card-state-fld"), AddressDetails[3], "t");
			}catch (Exception e){
				Helper_Functions.PrintOut("-" + AddressDetails[3] + "- State not present", false); 
			}
			
			WebDriver_Functions.Type(By.id("card-phone-fld"), Phone);
			WebDriver_Functions.Type(By.id("card-email-fld"), Email);
			WebDriver_Functions.Type(By.id("card-email-retype-fld"), Email);
		}catch (Exception e){}
	}//End WPRL_CreditCard_Input
	
	public static String CheckFDMEnrollment(String Level, String[] UserID) {
		String FDMUsers = "";
		for(int i = 0; i < UserID.length; i++) {
			try {
				if (Helper_Functions.Login(Level,  UserID[i], "Test1234")){
					Helper_Functions.ChangeURL("WPRL_FDM", "US", false, Level);
					WebDriver_Functions.WaitNotPresent(By.id("Loadingtxt"));
					WebDriver_Functions.WaitPresent(By.xpath("//*[@id='nav-local']/li[4]/a"));    
					if (!WebDriver_Functions.CheckBodyText("Sign up for FedEx® Delivery Manager.")) {
						Helper_Functions.PrintOut("User already enrolled", true);
					}
				}
			} catch (Exception e) {}
		}
		return FDMUsers;
	}

}

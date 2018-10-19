package TestingFunctions;

import static org.testng.Assert.assertEquals;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import SupportClasses.DriverFactory;

public class WDPA_Functions extends Helper_Functions{

	public static String strPhone = "9011111111";//remove this later
	
	//Address[] = Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, CountryCode - 6, ShareID - 7
	//PackageDetails for express/ground = {String Packages, String Weight, String WeightUnit (L or K), String Date, String ReadyTime, String CloseTime, String Special}
	//PackageDetails for expressLTL =     {String Skids,    String Weight, String WeightUnit,          String ReadyTime, String CloseTime, String Name, String Service, String ConfirmationNo, String Special, String Length, String Width, String Height)
	public static String[] WDPAPickupDetailed(String Level, String CountryCode, String User, String Password, String Service, String Company, String Name, String Phone, String Address[], String PackageDetails[], String ConfirmationRedirect) throws Exception{
		//https://wwwdev.idev.fedex.com/PickupApp/login?locale=en_US
		String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WDPA " + Service + " ";
		
		try {
			Login(Level, User, Password);
			
			ChangeURL("WDPA", CountryCode, false, Level);

			if (PackageDetails == null) {
				PackageDetails = new String[] {"1", "22", "L", null, null, null, null};
			}
			
			//enter the contact details
			WDPAExpressContactInformation(Company, Name, Address, Phone);

			//enter the package details
			if (Service.contentEquals("ground") || Service.contentEquals("express")) {
				WDPAPackageInformation(Service, PackageDetails);
			}else if (Service.contentEquals("expFreight")) {
				WDPAPackageInformationExpressLTL(PackageDetails);
			}
			takeSnapShot(SCPath + " Schedule.png");
			WebDriver_Functions.Click(By.id("button.completePickup"));

			if (WebDriver_Functions.CheckBodyText("A FedEx Ground pickup has already been scheduled by this account for this date, time and location.")){
				WDPAGroundPickupTime(SCPath);
				Helper_Functions.PrintOut("Need to use different Ground pikcup time.", true);
			}else if (WebDriver_Functions.CheckBodyText("Please correct the error(s) in red.") || WebDriver_Functions.CheckBodyText("The system has experienced an unexpected problem and is unable to complete your request.")){
				String ErrorMessage = "Error on Pickup Page.";//generic message.
				if (WebDriver_Functions.isPresent(By.xpath("//*[@id='primary.error.display']/div/label"))){
					ErrorMessage = WebDriver_Functions.GetText(By.xpath("//*[@id='primary.error.display']/div/label"));
				}
				Helper_Functions.PrintOut(ErrorMessage, true);
				throw(new Exception(ErrorMessage));	
			}

			//Check the pickup on confirmation page
			//need to add a check to see if details are correct
			WebDriver_Functions.ElementMatches(By.xpath("//*[@id='confirmationLeftPanel']/div[8]/div[2]/div/div/label"), Phone, 0);
			WebDriver_Functions.ElementMatches(By.xpath("//*[@id='confirmationRightPanel']/div[3]/div[2]/div/div/label"), PackageDetails[0], 0);//packages
			String Weight = null;
			if (PackageDetails[2] == "L") {
				Weight = PackageDetails[1] + " lbs";
			}else if (PackageDetails[2] == "K") {
				Weight = PackageDetails[1] + " kg";
			}
			
			WebDriver_Functions.ElementMatches(By.xpath("//*[@id='confirmationRightPanel']/div[4]/div[2]/div/div/label"), Weight, 0);//weight
			WebDriver_Functions.WaitPresent(By.cssSelector("div.confirmationRtColumnRight > div.confirmationFullWidthColumn > div.confirmationContentRight > label"));
			String ConfirmationNumber = WebDriver_Functions.GetText(By.cssSelector("div.confirmationRtColumnRight > div.confirmationFullWidthColumn > div.confirmationContentRight > label"));
			ConfirmationNumber = ConfirmationNumber.substring(ConfirmationNumber.indexOf(".") + 2);
			Helper_Functions.PrintOut("Schedule " + Service + " , Pickup: " + ConfirmationNumber, true);
			
			takeSnapShot(SCPath + " Confirmation.png");
			
			if (Service.contentEquals("ground") || Service.contentEquals("express")) {
				WDPAConfirmationLinks(ConfirmationRedirect, PackageDetails[0], PackageDetails[1], SCPath);
			}else if (Service.contentEquals("expFreight")) {
				//need to finish
			}
			
			WDPAMyPickupsPage(Level, Service, ConfirmationNumber, Address, PackageDetails, SCPath);

			return new String[] {ConfirmationNumber, ConfirmationRedirect, User};//need to add correct return valuers
     }catch (Exception e){
    	throw e;
     }
	}//end WDPAPickup
	
	// AddressDetails = {Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, CountryCode - 6, ShareID - 7};
	public static void WDPAExpressContactInformation(String Company, String Name, String AddressDetails[], String Phone) throws Exception {
		try{
			String Address1 = AddressDetails[0], Address2 = AddressDetails[1], City = AddressDetails[2], StateCode = AddressDetails[4], Zip = AddressDetails[5], Country = AddressDetails[6];
	    	if (Country != null && WebDriver_Functions.isPresent(By.id("address.accountAddressLinks"))) {
				WebDriver_Functions.Click(By.id("address.accountAddressLinks"));
			}
			//WebDriver_Functions.WaitPresent(By.id("button.completePickup")));
			if (WebDriver_Functions.isPresent(By.id("address.alternate.address1"))) {
				WebDriver_Functions.Type(By.id("address.alternate.contactName"), Name);
				WebDriver_Functions.Type(By.id("address.phoneNumber"), Phone);
			}else {
				WebDriver_Functions.Type(By.id("address.account.ContactName"), Name);
				WebDriver_Functions.Type(By.id("address.phoneNumber"), Phone);
			}
			
			if (Country != null && WebDriver_Functions.isPresent(By.id("address.alternate.address1"))) {
				WebDriver_Functions.Select(By.id("address.alternate.country"), Country, "v");
				WebDriver_Functions.Type(By.id("address.alternate.company"), Company);
				WebDriver_Functions.Type(By.id("address.alternate.address1"), Address1);
				WebDriver_Functions.Type(By.id("address.alternate.address2"), Address2);
				WebDriver_Functions.Type(By.id("address.alternate.zipPostal"), Zip);
				WebDriver_Functions.Type(By.id("address.alternate.city"), City);
				if (StateCode != null) {
					WebDriver_Functions.Select(By.id("address.alternate.stateProvince"), StateCode, "v");
				}
			}
		}catch (Exception e){
			Helper_Functions.PrintOut("Failure in Express entering contact informaiton.", true);
			e.printStackTrace();
			throw e;
		}
	}

	//PackageDetails = {String Packages, String Weight, String WeightUnit, String Date, String ReadyTime, String CloseTime, String Special}
	public static void WDPAPackageInformation(String Service, String[] PackageDetails) throws Exception{
		//String Packages, String Weight, String WeightUnit, String Date, String ReadyTime, String CloseTime, String Special
		String Packages = PackageDetails[0], Weight = PackageDetails[1], WeightUnit = PackageDetails[2], ReadyTime = PackageDetails[4], CloseTime = PackageDetails[5], Special = PackageDetails[6];
		//String Date = PackageDetails[3];
		
		String strFieldType;
		strFieldType = "package." + Service;
		if (!DriverFactory.getInstance().getDriver().findElement(By.id(strFieldType + ".field")).isSelected()){
			WebDriver_Functions.Click(By.id(strFieldType + ".field"));
		}
		
		WebDriver_Functions.Type(By.id(strFieldType + ".totalPackages"), Packages);
		WebDriver_Functions.Type(By.id(strFieldType + ".totalWeight"), Weight);
		WebDriver_Functions.Select(By.id(strFieldType + ".totalWeight.uom"), WeightUnit, "v");

		//select the last select able day from the calendar.   need to get this working for when testing prod
//uncomment
//		WebDriver_Functions.Click(By.id(CalenderDate(strFieldType + ".pickupDate", Date))); //warning, if trying to debug this may fail as the drop down calendar will close.
		WebDriver_Functions.WaitForTextPresentIn(By.id(strFieldType + ".closeTime"), "pm");
		//wait.until(ExpectedConditions.textMatches(By.id(strFieldType + ".closeTime"), Pattern.compile("pm")));//wait for the shipment time label to load

		if (ReadyTime != null) {
			WebDriver_Functions.Select(By.id(strFieldType + ".readyTime"), ReadyTime, "v");
		}
			
		if (CloseTime!= null) {
			WebDriver_Functions.Select(By.id(strFieldType + ".closeTime"), CloseTime, "v");
		}

		try{//this is only applicable for APAC countries. Need to select what the shipment contains and the country it is going to.
			if (WebDriver_Functions.isPresent(By.id(strFieldType + ".content1"))){
				WebDriver_Functions.Select(By.id(strFieldType + ".content1"), "1", "i");
				WebDriver_Functions.Select(By.id(strFieldType + ".destination1"), "1", "i");
			}
		}catch (Exception e){}
		
		if (Special != null) {
			WebDriver_Functions.Type(By.id(strFieldType + ".specialInst"), Special);
		}
	}
	
	public static void WDPAPackageInformationExpressLTL(String[] PackageDetails) throws Exception{

		String Skids = PackageDetails[0];
		String Weight = PackageDetails[1];
		String WeightUnit = PackageDetails[2];
		String ReadyTime = PackageDetails[3];
		String CloseTime = PackageDetails[4];
		String Name = PackageDetails[5];
		String Service = PackageDetails[6];
		String ConfirmationNo = PackageDetails[7];
		String Special = PackageDetails[8];
		String Length = PackageDetails[9];
		String Width = PackageDetails[10];
		String Height = PackageDetails[11];
		
		if (!DriverFactory.getInstance().getDriver().findElement(By.id("package.expFreight.field")).isSelected()){
			WebDriver_Functions.Click(By.id("package.expFreight.field"));
		}
		
		WebDriver_Functions.Type(By.id("package.expFreight.totalSkids"), Skids);
		WebDriver_Functions.Type(By.id("package.expFreight.totalWeight"), Weight);
		WebDriver_Functions.Select(By.id("package.expFreight.totalWeight.uom"), WeightUnit, "v");
		
		//wait for the shipment time label to load
		WebDriver_Functions.WaitForTextNot(By.id("package.expFreight.closeTime"), "");
		//wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.id("package.expFreight.closeTime"), (""))));
			
		if (ReadyTime != null) {
			WebDriver_Functions.Select(By.id("package.expFreight.readyTime"), ReadyTime, "v");
		}else {
			WebDriver_Functions.Select(By.id("package.expFreight.readyTime"), "1530", "v");
		}
			
		if (CloseTime!= null) {
			WebDriver_Functions.Select(By.id("package.expFreight.closeTime"), CloseTime, "v");
		}

		WebDriver_Functions.Type(By.id("package.expFreight.personWithSkids"), Name);
		WebDriver_Functions.Type(By.id("package.expFreight.specialInst"), Special);
		WebDriver_Functions.Select(By.id("package.expFreight.serviceType"), Service, "t");
		if (ConfirmationNo != null && WebDriver_Functions.isPresent(By.id("package.expFreight.bookingNumber"))) { //note, not applicable for 1 and 2 day freight 
			WebDriver_Functions.Type(By.id("package.expFreight.bookingNumber"), ConfirmationNo);
		}
		
		WebDriver_Functions.Select(By.id("package.expFreight.dimProfile"), "Enter Dimensions Manually", "t");
		WebDriver_Functions.Type(By.id("package.expFreight.length"), Length);
		WebDriver_Functions.Type(By.id("package.expFreight.width"), Width);
		WebDriver_Functions.Type(By.id("package.expFreight.height"), Height);
		
		WebDriver_Functions.Select(By.id("package.expFreight.truckType"), "Lift gate", "t");
	}
	
	public static void WDPAGroundPickupTime(String SCPath) {
		//if ground may need to change time if package already scheduled.
		try{//will through exceptions and skip looping if find a good time
			//A FedEx Ground pickup has already been scheduled by this account for this date, time and location.  Please refer to pickup confirmation number  for more information about this pickup.
				for(int r = 0; r < 99 ;r++){//start from last available time
    				WebElement selectElementRi = DriverFactory.getInstance().getDriver().findElement(By.id("package.ground.readyTime"));
    				Select selectRi = new Select(selectElementRi);
					selectRi.selectByIndex(r);
					for(int c = 0; c < 99; c++){
	    		    	WebElement selectElementCi = DriverFactory.getInstance().getDriver().findElement(By.id("package.ground.closeTime"));
	    		    	Select selectCi = new Select(selectElementCi);
						selectCi.selectByIndex(c);
						WebDriver_Functions.takeSnapShot(SCPath + " Schedule.png");
						WebDriver_Functions.Click(By.id("button.completePickup"));
		    			if (!DriverFactory.getInstance().getDriver().findElement(By.tagName("body")).getText().contains("A FedEx Ground pickup has already been scheduled by this account for this date, time and location.")){
		    				throw new Exception();//breaks out of the loop
		    			}
					}//for c end
				}// for r end
				Helper_Functions.PrintOut("Not able to schedule ground, Cancel some pickups", true);
				return;
		}catch(Exception e){}
	}
	
	public static void WDPAConfirmationLinks(String AppTested, String Packages, String Weight, String SCPath) throws Exception{
		if (AppTested.contentEquals("INET") && WebDriver_Functions.isPresent(By.xpath("//input[(@value='Ship')]"))){//test INET
			WebDriver_Functions.Click(By.xpath("//input[(@value='Ship')]"));
			WebDriver_Functions.Click(By.id("module.from._header"));
			//test the from location
			
			WebDriver_Functions.ElementMatchesSelect(By.id("psdData.numberOfPackages"), Packages, 0);

			if (Packages == "1"){
				String WeightFormated = Weight + ".00";
				WebDriver_Functions.ElementMatches(By.id("psd.mps.row.weight.0"), WeightFormated, 0);
				WebDriver_Functions.takeSnapShot(SCPath + " INET page.png");
			}else if (AppTested.contentEquals("WGRT") && WebDriver_Functions.isPresent(By.xpath("//input[(@value='Get rate quote')]"))){//test wgrt
				WebDriver_Functions.Click(By.xpath("//input[(@value='Get rate quote')]"));
				WebDriver_Functions.WaitForText(By.id("pageTitle"), "Get Rates & Transit Times");//changed from "Transit Times" on 4-18-18
				String weightElement = "totalPackageWeight";
				if (WebDriver_Functions.isPresent(By.id("NumOfPackages"))){//apac has different rules so this section may not be present  NumOfPackages
					weightElement = "perPackageWeight";
					WebDriver_Functions.ElementMatches(By.id("NumOfPackages"), Packages, 0);
				}
					
				if (Packages == "1"){
					WebDriver_Functions.ElementMatches(By.id(weightElement), Weight, 0);
				}
				WebDriver_Functions.takeSnapShot(SCPath + " WGRT page.png");
			}
			Helper_Functions.PrintOut(AppTested + " button working as expected", true);
		}
	}
	
	public static void WDPAMyPickupsPage(String Level, String Service, String ConfirmationNumber, String Address[], String PackageDetails[], String SCPath) throws Exception{
		//navigate to my pickups page
		WebDriver_Functions.ChangeURL("WDPA", Address[6], false, Level);
		
		//search for the pickup just created
		WebDriver_Functions.Type(By.id("history.filterField"), ConfirmationNumber);
		WebDriver_Functions.Select(By.id("history.filterColumn"), "Confirmation no.", "t");
		WebDriver_Functions.Click(By.id("history.search"));

		assertEquals(ConfirmationNumber, DriverFactory.getInstance().getDriver().findElement(By.id("table.pickupHistory._contents._row1._col4")).getText());
		
		//cancel the pickup
		WebDriver_Functions.Click(By.id("row.check1"));
		WebDriver_Functions.Click(By.id("history.cancelPickup"));
		WebDriver_Functions.Click(By.id("history.cancelConfirm"));
	    
		//check that the pickup is cancelled
		WebDriver_Functions.Type(By.id("history.filterField"), ConfirmationNumber);
		WebDriver_Functions.Select(By.id("history.filterColumn"), "Confirmation no.", "t");
		WebDriver_Functions.Click(By.id("history.search"));

		//make sure the pickup is present
		assertEquals(ConfirmationNumber, DriverFactory.getInstance().getDriver().findElement(By.id("table.pickupHistory._contents._row1._col4")).getText());
		//make sure the pickup is cancelled
		if (!(DriverFactory.getInstance().getDriver().findElement(By.id("table.pickupHistory._contents._row1._col6")).getText().contains("Cancelled"))){
			Helper_Functions.PrintOut("    Pickup:" + ConfirmationNumber + " has not been cancelled", true);
		}
		WebDriver_Functions.takeSnapShot(SCPath + " Cancellation.png");
	}
	

 	
	
 	/////////////////Delete this
	public static String WDPALTLPickup(String Level, String AddressDetails[], String User, String Password) throws Exception{
		return WDPALTLPickup(Level, AddressDetails, User ,Password, "10", "400");
	}
	
	public static String WDPALTLPickup(String Level, String AddressDetails[], String User, String Password, String HandelingUnits, String Weight) throws Exception{
		String CountryCode = AddressDetails[6];
		//https://wwwdev.idev.fedex.com/PickupApp/login?locale=en_US
		
		String strAccountSelected = "";
		String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WDPA LTL ";
	
		try {
			//check if logged in flow or not, blank user means non logged in flow.
			if (User != ""){
				Login(Level, User, Password);			
				// launch the browser and direct it to the Base URL  https://wwwdrt.idev.fedex.com/PickupApp/scheduleFreightPickup.do?method=doInit&locale=en_us
				ChangeURL("WDPA_LTL", CountryCode, false, Level);
				//wait for the WDPA page to load
				WebDriver_Functions.Click(By.id("account.freight.accountBox._LookupButton"));
				strAccountSelected = WebDriver_Functions.GetText(By.xpath("//option"));
				WebDriver_Functions.Click(By.xpath("//option"));
				WebDriver_Functions.Select(By.id("account.freight.accountBox._InputSelect"), "0", "i");
				
				if (DriverFactory.getInstance().getDriver().findElement(By.id("module.address._headerHide")).getAttribute("style").contains("none")){ //if select account and address is prepopulated.            if (!driver.findElement(By.id("address.alternate.contactName")).isDisplayed()) 
					WebDriver_Functions.Click(By.id("module.address._headerEdit"));
					WebDriver_Functions.WaitPresent(By.id("address.phoneNumber"));
			    	WebDriver_Functions.Type(By.id("address.alternate.contactName"), "TestingName");
			    	WebDriver_Functions.Type(By.id("address.phoneNumber"), strPhone);
				}else if (WebDriver_Functions.isPresent(By.id("address.accountAddressOne.field1"))) { //if the account number was selected and all the address details are needed.
					WebDriver_Functions.Type(By.id("address.alternate.company"), "CompanyName");
			    	WebDriver_Functions.Type(By.id("address.alternate.contactName"), "TestingName");
			    	WebDriver_Functions.Type(By.id("address.accountAddressOne.field1"), AddressDetails[0]);
			    	WebDriver_Functions.Type(By.id("address.accountAddressTwo.field1"), AddressDetails[1]);
			    	WebDriver_Functions.Type(By.id("address.alternate.city1"), AddressDetails[2]);
			    	WebDriver_Functions.WaitForTextNot(By.id("address.accountStateProvince.field1"), "");
			    	WebDriver_Functions.Select(By.id("address.accountStateProvince.field1"), AddressDetails[3], "t");
			    	WebDriver_Functions.Type(By.id("address.alternate.phoneNumber"), strPhone);
			    	WebDriver_Functions.Type(By.id("address.alternate.zipPostal"), AddressDetails[5]);
				}
			}else{
				ChangeURL("WDPA_LTL", CountryCode, false, Level);
				
				WebDriver_Functions.WaitPresent(By.id("address.alternate.company.ns"));
			    WebDriver_Functions.Type(By.id("address.alternate.company.ns"), "CompanyName");
			    WebDriver_Functions.Type(By.id("address.alternate.contactName.ns"), "TestingName");
			    WebDriver_Functions.Type(By.id("address.accountAddressOne.field1"), AddressDetails[0]);
			    WebDriver_Functions.Type(By.id("address.accountAddressTwo.field1"), AddressDetails[1]);
			    WebDriver_Functions.Type(By.id("address.alternate.city1"), AddressDetails[2]);
			    WebDriver_Functions.WaitForTextNot(By.id("address.accountStateProvince.field1"), "");
			    WebDriver_Functions.Select(By.id("address.accountStateProvince.field1"), AddressDetails[3], "t");
			    WebDriver_Functions.Type(By.id("address.alternate.phoneNumber"), strPhone);
			    WebDriver_Functions.Type(By.id("address.alternate.zipPostal"), AddressDetails[5]);
			}

			WebDriver_Functions.ElementMatches(By.xpath("//*[@id='address.accountCountry.display']/div[1]/div/div[2]/label"), "*\nCountry/Territory", 116632); //Added the "* \n" due to dev has setup
			WebDriver_Functions.ElementMatches(By.xpath("//*[@id='table.shipmentDetailTable._contents._header._col2']/span"), "*Country/Territory", 116633); 
			
			WebDriver_Functions.WaitForTextNot(By.id("freightPickupInfo.readyTime"), "");
		    
		    WebDriver_Functions.ElementMatches(By.cssSelector("div.pickupInfoRequiredContentLeft.pickupFloatLeft > div > label"), "Over Length (8 feet to < 12 feet)\nOver Length (8 feet to < 12 feet)", 179730); 
		    WebDriver_Functions.ElementMatches(By.xpath("//div[@id='module.pickupInfo.specialServices']/div[3]/div/div/div[2]/label"), "Extreme Length (12 feet or greater)\nExtreme Length (12 feet or greater)", 179730); 

		    //the location
		    WebDriver_Functions.Type(By.id("zipCode1"), AddressDetails[5]);
		    WebDriver_Functions.Select(By.id("serviceType1"), "Economy", "t");
		    WebDriver_Functions.Type(By.id("handlingUnits1"), HandelingUnits);
			WebDriver_Functions.Type(By.id("weight1"), Weight);
		    
		    
		    ///need to check here as the time is not loading as default.
		   // CalenderDate("pickupInfo.freight.pickupDate", getDayOfMonth() + 2 + "");    ///need to test
		    
		    
		    WebDriver_Functions.Select(By.id("freightPickupInfo.readyTime"), "3:00 pm", "t");
		    WebDriver_Functions.Select(By.id("freightPickupInfo.closeTime"), "11:00 pm", "t");
		    takeSnapShot(SCPath + " Pickup.png");
		    WebDriver_Functions.Click(By.id("button.freightpickup.schedulePickup"));
		    
		    WebDriver_Functions.WaitPresent(By.cssSelector("div.confirmationRtColumnRight > div.confirmationFullWidthColumn > div.confirmationContentRight > label"));
		    String strConfirmationNumber = WebDriver_Functions.GetText(By.cssSelector("div.confirmationRtColumnRight > div.confirmationFullWidthColumn > div.confirmationContentRight > label"));
		    strConfirmationNumber = strConfirmationNumber.substring(strConfirmationNumber.indexOf(".") + 2);
		    Helper_Functions.PrintOut("Schedule LTL Pickup:   " + strConfirmationNumber, true);
		    WebDriver_Functions.ElementMatches(By.xpath("//*[@id='confirmationLeftPanel']/div[1]/div[1]/div/div/label"), "Country/Territory", 116629); 
		    takeSnapShot(SCPath + " Confirmation.png");
		    
		    if (User != ""){
			    WebDriver_Functions.Click(By.id("menubar.nav.menu3_div"));
			    WebDriver_Functions.Click(By.id("account.freight.accountBox._LookupButton"));
			    WebDriver_Functions.Select(By.id("account.freight.accountBox._InputSelect"), strAccountSelected, "t");
			    
			    WebDriver_Functions.WaitPresent(By.id("history.search"));
			    WebDriver_Functions.Type(By.id("history.filterField"), strConfirmationNumber);
			    WebDriver_Functions.Select(By.id("history.filterColumn"), "Confirmation no.", "t");
			    WebDriver_Functions.Click(By.id("history.search"));
			    
			    WebDriver_Functions.WaitPresent(By.id("row.check1"));
			    assertEquals("Scheduled", WebDriver_Functions.GetText(By.id("statusLink0")));
			    assertEquals(strConfirmationNumber, WebDriver_Functions.GetText(By.id("confNumLink0")));
			    
			    WebDriver_Functions.Click(By.id("row.check1"));
			    WebDriver_Functions.Click(By.id("history.viewPrintPickupDetails"));
			    
			    WebDriver_Functions.WaitPresent(By.cssSelector("div.confirmationAlertMessage.pickupAlertMessageText"));
			    assertEquals(HandelingUnits, WebDriver_Functions.GetText(By.xpath("//div[@id='confirmationRightPanel']/div[5]/div[2]/div/div/label")));
			    assertEquals(Weight, WebDriver_Functions.GetText(By.xpath("//div[@id='confirmation.weight']/label")));
			    assertEquals("3:00pm - 11:00pm", WebDriver_Functions.GetText(By.xpath("//div[@id='confirmation.pickuptime']/label")));
			    assertEquals(AddressDetails[5], WebDriver_Functions.GetText(By.id("confirmation.lineItems.zipPostal")));
			    takeSnapShot(SCPath + "LTLDetails.png");
			    Helper_Functions.PrintOut("WDPALTLPickup Completed", true);
		    }
		    return strConfirmationNumber;
	     } catch (Exception e) {
			 try{ 
				 if (WebDriver_Functions.isPresent(By.xpath("//*[@id='primary.error.display']/div/label"))){
					 Helper_Functions.PrintOut(WebDriver_Functions.GetText(By.xpath("//*[@id='primary.error.display']/div/label")), true);
				 }
			 }catch (Exception e2){}
			 throw e;
		 }
	}//end WDPALTLPickup

	public static void WDPAShipment(String Level, String CountryCode, String User, String Password, String Service,  String OriginAddressDetails[], String DestAddressDetails[]) throws Exception{
		String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " INET ";
		try {
			// launch the browser and direct it to the Base URL
			Login(Level, User, Password);
			ChangeURL("INET", CountryCode, false, Level);
			WebDriver_Functions.Click(By.id("module.from._headerEdit"));
			for (int i = 0; i < 2; i++){
				String Loc = "to";
				String AddressDetails[] = DestAddressDetails; //{Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, CountryCode - 6};
				if (i == 1){
					Loc = "from";
					AddressDetails = OriginAddressDetails;
				}
				//wait for country to load
				WebDriver_Functions.WaitForTextPresentIn(By.id(Loc + "Data.countryCode"), "A");

	 			WebDriver_Functions.Select(By.id(Loc + "Data.countryCode"), AddressDetails[6].toUpperCase(), "v");
	 			
	 			WebDriver_Functions.WaitForTextPresentIn(By.id(Loc + "Data.countryCode"), "A");
				WebDriver_Functions.WaitPresent(By.id(Loc + "Data.contactName"));
				String ContactName = getRandomString(8);//enter filler name
				
				WebDriver_Functions.Type(By.id(Loc + "Data.companyName"), "Comp " + ContactName);
				WebDriver_Functions.Type(By.id(Loc + "Data.addressLine1"), AddressDetails[0]);
				WebDriver_Functions.Type(By.id(Loc + "Data.addressLine2"), AddressDetails[1]);
				
				if (!AddressDetails[5].isEmpty()){
					//had to change method of imput due to issues with error message when zip is cleared
					JavascriptExecutor myExecutor = ((JavascriptExecutor) DriverFactory.getInstance().getDriver());
			    	WebElement zipcode = DriverFactory.getInstance().getDriver().findElement(By.id(Loc + "Data.zipPostalCode"));
			    	myExecutor.executeScript("arguments[0].value='"+ AddressDetails[5] + "';", zipcode);
					//driver.findElement(By.id(Loc + "Data.zipPostalCode")).clear();driver.findElement(By.id(Loc + "Data.zipPostalCode")).sendKeys(AddressDetails[5]);
				}
				//enter city 
				WebDriver_Functions.Type(By.id(Loc + "Data.city"), AddressDetails[2]);
				WebDriver_Functions.Select(By.id(Loc + "Data.stateProvinceCode"), AddressDetails[4], "v");
				WebDriver_Functions.Type(By.id(Loc + "Data.phoneNumber"), strPhone);
			}
			WebDriver_Functions.Type(By.id("psd.mps.row.weight.0"), "1");
			
			//Service type
		    WebElement dropdown = DriverFactory.getInstance().getDriver().findElement(By.id("psdData.serviceType"));
		    //dropdown.click();
		    List<WebElement> options = dropdown.findElements(By.tagName("option"));
		    String optTxt = null;
		    for(WebElement option : options){
		    	optTxt = option.getText();
		        if(optTxt.contains(Service)){
		            break;
		        }
		    }
		    WebDriver_Functions.Select(By.id("psdData.serviceType"), optTxt, "t");
			if (Service.contentEquals("Priority")){
				WebDriver_Functions.WaitPresent(By.id("psd.mps.row.weight.0"));
				WebDriver_Functions.Type(By.id("psd.mps.row.weight.0"), "10");
				WebDriver_Functions.Select(By.id("psdData.packageType"), "FedEx Box", "v");
				if (optTxt.contains("International")){
					WebDriver_Functions.Click(By.id("commodityData.packageContents.products"));
					WebDriver_Functions.Type(By.id("commodityData.totalCustomsValue") ,"10");
				}
				//Pickup/Drop-off Modal
				WebDriver_Functions.Click(By.id("pdm.initialChoice.schedulePickup"));
				WebDriver_Functions.WaitPresent(By.id("pickupAddress.collapse"));
				WebDriver_Functions.WaitPresent(By.id("packageInfo.collapse"));
			}else if (Service.contentEquals("Ground")){
				WebDriver_Functions.Select(By.id("psdData.packageType"), "2", "i");//Barrel
				WebDriver_Functions.Select(By.id("commodityData.shipmentPurposeCode"), "3", "i");//Gift
				WebDriver_Functions.Type(By.id("commodityData.totalCustomsValue") ,"10");
				//Pickup/Drop-off Modal
				WebDriver_Functions.Click(By.id("pdm.initialChoice.schedulePickup"));
				WebDriver_Functions.WaitPresent(By.id("pickupAddress.collapse"));
				WebDriver_Functions.WaitPresent(By.id("packageInfo.collapse"));
			}else if (Service.contentEquals("Freight")){
				//Package type is set to "Your Packaging"
				WebDriver_Functions.WaitPresent(By.id("psd.mps.row.dimensions.0"));
				WebDriver_Functions.Type(By.id("psd.mps.row.weight.0") ,"200");
				
				//wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("psd.mps.row.dimensions.0")));
				WebDriver_Functions.Select(By.id("psd.mps.row.dimensions.0"), "manual", "v");
				//wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("psd.mps.row.dimensionLength.0")));
				//wait.until(ExpectedConditions.elementToBeClickable(By.id("psd.mps.row.dimensionLength.0")));
				
				WebDriver_Functions.Type(By.id("psd.mps.row.dimensionLength.0") ,"8");
				WebDriver_Functions.Type(By.id("psd.mps.row.dimensionWidth.0") ,"4");
				WebDriver_Functions.Type(By.id("psd.mps.row.dimensionHeight.0") ,"3");
				
				//Pickup/Drop-off Modal
				WebDriver_Functions.Click(By.id("pdm.initialChoice.schedulePickup"));
				///actions.moveToElement(driver.findElement(By.id("module.from._headerTitle"))).perform();
				///wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dropoff.viewMoreLocations.link")));
				///wait.until(ExpectedConditions.elementToBeClickable(By.id("pdm.initialChoice.schedulePickup"))).click();
				WebDriver_Functions.WaitPresent(By.id("pickupAddress.collapse"));
				WebDriver_Functions.WaitPresent(By.id("packageInfo.collapse"));
				//edit the pickup information
				WebDriver_Functions.Click(By.id("packageInfo.edit.plus"));
				WebDriver_Functions.WaitPresent(By.id("pdm.personWithSkidNumber"));
				WebDriver_Functions.Type(By.id("pdm.personWithSkidNumber"), "John");

				WebDriver_Functions.Select(By.id("pdm.dimProfile"), "manual", "v");
				//wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pdm.dimLength")));
				//wait.until(ExpectedConditions.elementToBeClickable(By.id("pdm.dimLength")));
				WebDriver_Functions.Type(By.id("pdm.dimLength") ,"8");
				WebDriver_Functions.Type(By.id("pdm.dimWidth") ,"4");
				WebDriver_Functions.Type(By.id("pdm.dimHeight") ,"3");
				//Lift gate
				WebDriver_Functions.Select(By.id("pdm.truckType"), "L", "v");
				//Trailer size = 28
				WebDriver_Functions.Select(By.id("pdm.truckSize"), "28", "v");
			}
			
			takeSnapShot(SCPath + "Shipment.png");
			WebDriver_Functions.Click(By.id("completeShip.ship.field"));
			
			//Enter product/commodity information
			if (optTxt.contains("International")){
				try{
					WebDriver_Functions.Select(By.id("commodityData.chosenProfile.profileID"), "add", "v");
					WebDriver_Functions.WaitPresent(By.id("commodityData.chosenProfile.description"));
					WebDriver_Functions.Type(By.id("commodityData.chosenProfile.description") ,"Generic Description");
					WebDriver_Functions.Select(By.id("commodityData.chosenProfile.unitOfMeasure"), "1", "i");
					WebDriver_Functions.Type(By.id("commodityData.chosenProfile.quantity") ,"10");
					WebDriver_Functions.Type(By.id("commodityData.chosenProfile.commodityWeight") ,"50");

					WebDriver_Functions.Select(By.id("commodityData.chosenProfile.manufacturingCountry"), "1", "i");
					WebDriver_Functions.Click(By.id("commodity.button.addCommodity"));
					WebDriver_Functions.WaitForText(By.id("commodity.summaryTable._contents._row1._col2"), "Generic Description");
					takeSnapShot(SCPath + "product_commodity information.png");
					WebDriver_Functions.Click(By.id("completeShip.ship.field"));
				}catch (Exception e){}
			}else{
				//Confirm shipping details
				WebDriver_Functions.WaitPresent(By.id("confirm.ship.field"));
				WebDriver_Functions.Click(By.id("completeShip.ship.field"));
			}
			
			//confirmation page
			WebDriver_Functions.WaitPresent(By.id("trackingNumber"));

			Helper_Functions.PrintOut("Need to finish", true);

			if (WebDriver_Functions.isPresent(By.id("label.alert.unsuccessfulPickupSchedule"))){
				Helper_Functions.PrintOut(WebDriver_Functions.GetText(By.id("label.alert.unsuccessfulPickupSchedule")), true);
			}
			
		}catch (Exception e){
			throw e;
		}
	}//end WDPAShipment
  
	//Format = this is the general format of the calender id. ex: package.express.pickupDate if the total is "package.express.pickupDate._week1day4"
	public static String CalenderDate(String IdFormat, String Date) throws Exception {
		boolean AddtionalMonth = false;
		String LastAvailable = null;
		WebDriver_Functions.Click(By.id(IdFormat + "._icon"));
		for (int week = 1; week < 7; week++) {
			for (int day = 1; day < 8; day++) {
				String AttemptDate = IdFormat + "._week" + week + "day" + day;
				if (WebDriver_Functions.isPresent(By.id(AttemptDate)) && DriverFactory.getInstance().getDriver().findElement(By.id(AttemptDate)).getAttribute("class").contentEquals("enabledDateStyle")) {
					LastAvailable = AttemptDate;
					if (WebDriver_Functions.GetText(By.id(LastAvailable)) == Date || Date == null) {//if a date is provided will return once found as a valid option, if date is null will return once finds first avaialbe.
						return LastAvailable;
					}
				}else if (week == 6){
					if (!AddtionalMonth && WebDriver_Functions.isPresent(By.id(IdFormat + "._nextMonth"))) {
						WebDriver_Functions.Click(By.id(IdFormat + "._nextMonth"));
						week = 1;
						day  = 0;
						AddtionalMonth = true;
					}else {
						break;//already tried for the next month, stop looking for available dates.
					}
				}
				//else {Helper_Functions.PrintOut(week + "  " + day + "   " + LastAvailable, true);}   //for debug if needed
			}
		}
		return LastAvailable;
	}
	
}

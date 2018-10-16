package TestingFunctions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import SupportClasses.DriverFactory;

public class WDPA_Functions {

	//DriverFactory.getInstance().getDriver()
	
	// AddressDetails = {Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, CountryCode - 6, ShareID - 7};
	public static void WDPAExpressContactInformation(String Company, String Name, String AddressDetails[], String Phone) throws Exception {
		try{
			String Address1 = AddressDetails[0], Address2 = AddressDetails[1], City = AddressDetails[2], StateCode = AddressDetails[4], Zip = AddressDetails[5], Country = AddressDetails[6];
	    	//if (Country != null && WebDriver_Functions.isPresent(By.id("address.accountAddressLinks"))) {
				//WebDriver_Functions.Click(By.id("address.accountAddressLinks"));
			//}
			//wait.until(ExpectedConditions.presenceOfElementLocated(By.id("button.completePickup")));
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
		String Packages = PackageDetails[0], Weight = PackageDetails[1], WeightUnit = PackageDetails[2], Date = PackageDetails[3], ReadyTime = PackageDetails[4], CloseTime = PackageDetails[5], Special = PackageDetails[6];
		
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
	
	public static void WDPAGroundPickupTime(String WDPAPath) {
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
						captureScreenShot("WDPA", WDPAPath + " Schedule.png");
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
	
	public static void WDPAConfirmationLinks(String AppTested, String Packages, String Weight, String WDPAPath) throws Exception{
		if (AppTested.contentEquals("INET") && WebDriver_Functions.isPresent(By.xpath("//input[(@value='Ship')]"))){//test INET
			WebDriver_Functions.Click(By.xpath("//input[(@value='Ship')]"));
			WebDriver_Functions.Click(By.id("module.from._header"));
			//test the from location
			
			WebDriver_Functions.ElementMatchesSelect(By.id("psdData.numberOfPackages"), Packages, 0);

			if (Packages == "1"){
				String WeightFormated = Weight + ".00";
				WebDriver_Functions.ElementMatches(By.id("psd.mps.row.weight.0"), WeightFormated, 0);
				captureScreenShot("WDPA", WDPAPath + " INET page.png");
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
				captureScreenShot("WDPA", WDPAPath + " WGRT page.png");
			}
			Helper_Functions.PrintOut(AppTested + " button working as expected", true);
		}
	}
	
	public static void WDPAMyPickupsPage(String Level, String Service, String ConfirmationNumber, String Address[], String PackageDetails[], String WDPAPath) throws Exception{
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
		captureScreenShot("WDPA", WDPAPath + " Cancellation.png");
	}
	
	//Address[] = Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, CountryCode - 6, ShareID - 7
	//PackageDetails for express/ground = {String Packages, String Weight, String WeightUnit (L or K), String Date, String ReadyTime, String CloseTime, String Special}
	//PackageDetails for expressLTL =     {String Skids,    String Weight, String WeightUnit,          String ReadyTime, String CloseTime, String Name, String Service, String ConfirmationNo, String Special, String Length, String Width, String Height)
	public static String[] WDPAPickupDetailed(String Level, String CountryCode, String User, String Password, String Service, String Company, String Name, String Phone, String Address[], String PackageDetails[], String ConfirmationRedirect) throws Exception{
		//https://wwwdev.idev.fedex.com/PickupApp/login?locale=en_US
		TestData.LoadTime();
		String WDPAPath = strTodaysDate + " " + strLevel + " " + CountryCode + " " + Service;
    
		try {
			Login(User, Password);
			ChangeURL("https://" + strLevelURL + "/PickupApp/login?locale=en_" + CountryCode);

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
	
			captureScreenShot("WDPA", WDPAPath + " Schedule.png");
			WebDriver_Functions.Click(By.id("button.completePickup"));

			if (WebDriver_Functions.CheckBodyText("A FedEx Ground pickup has already been scheduled by this account for this date, time and location.")){
				WDPAGroundPickupTime(WDPAPath);
				Helper_Functions.PrintOut("Need to use different Ground pikcup time.", true);
			}else if (WebDriver_Functions.CheckBodyText("Please correct the error(s) in red.") || WebDriver_Functions.CheckBodyText("The system has experienced an unexpected problem and is unable to complete your request.")){
				Helper_Functions.PrintOut("Error after scheduling pickup attempt.", true);
				throw(new Exception("Error on Pickup Page."));	
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
			String ConfirmationNumber = driver.findElement(By.cssSelector("div.confirmationRtColumnRight > div.confirmationFullWidthColumn > div.confirmationContentRight > label")).getText();
			ConfirmationNumber = ConfirmationNumber.substring(ConfirmationNumber.indexOf(".") + 2);
			Helper_Functions.PrintOut("Schedule " + Service + " , Pickup: " + ConfirmationNumber, true);
			captureScreenShot("WDPA", WDPAPath + " Confirmation.png");
			
			if (Service.contentEquals("ground") || Service.contentEquals("express")) {
				WDPAConfirmationLinks(ConfirmationRedirect, PackageDetails[0], PackageDetails[1], WDPAPath);
			}else if (Service.contentEquals("expFreight")) {
				//need to finish
			}
			
			WDPAMyPickupsPage(Level, Service, ConfirmationNumber, Address, PackageDetails, WDPAPath);

			return new String[] {ConfirmationNumber, ConfirmationRedirect, User};//need to add correct return valuers
     }catch (Exception e){
    	throw e;
     }
	}//end WDPAPickup
 	
	
 	
	public static String WDPALTLPickup(String AddressDetails[], String User, String Password) throws Exception{
		return WDPALTLPickup(AddressDetails, User ,Password, "10", "400");
	}
	
	public static String WDPALTLPickup(String AddressDetails[], String User, String Password, String HandelingUnits, String Weight) throws Exception{
		String CountryCode = AddressDetails[6];
		//https://wwwdev.idev.fedex.com/PickupApp/login?locale=en_US
		TestData.LoadTime();
		
		String strAccountSelected = "";
		String WDPAPath = strTodaysDate + " " + strLevel + " LTL";
	
		try {
			//check if logged in flow or not, blank user means non logged in flow.
			if (User != ""){
				Login(User, Password);			
				// launch the browser and direct it to the Base URL  https://wwwdrt.idev.fedex.com/PickupApp/scheduleFreightPickup.do?method=doInit&locale=en_us
				ChangeURL("https://" + strLevelURL + "/PickupApp/scheduleFreightPickup.do?method=doInit&locale=en_" + CountryCode);
				//wait for the WDPA page to load
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("account.freight.accountBox._LookupButton")));
				WebDriver_Functions.Click(By.id("account.freight.accountBox._LookupButton"));
				strAccountSelected = driver.findElement(By.xpath("//option")).getText();
				WebDriver_Functions.Click(By.xpath("//option"));
				WebDriver_Functions.Select(By.id("account.freight.accountBox._InputSelect"), "0", "i");
				
				if (driver.findElement(By.id("module.address._headerHide")).getAttribute("style").contains("none")){ //if select account and address is prepopulated.            if (!driver.findElement(By.id("address.alternate.contactName")).isDisplayed()) 
					WebDriver_Functions.Click(By.id("module.address._headerEdit"));
			    	wait.until(ExpectedConditions.presenceOfElementLocated(By.id("address.phoneNumber")));
			    	WebDriver_Functions.Type(By.id("address.alternate.contactName"), "TestingName");
			    	WebDriver_Functions.Type(By.id("address.phoneNumber"), strPhone);
				}else if (WebDriver_Functions.isPresent(By.id("address.accountAddressOne.field1"))) { //if the account number was selected and all the address details are needed.
					WebDriver_Functions.Type(By.id("address.alternate.company"), "CompanyName");
			    	WebDriver_Functions.Type(By.id("address.alternate.contactName"), "TestingName");
			    	WebDriver_Functions.Type(By.id("address.accountAddressOne.field1"), AddressDetails[0]);
			    	WebDriver_Functions.Type(By.id("address.accountAddressTwo.field1"), AddressDetails[1]);
			    	WebDriver_Functions.Type(By.id("address.alternate.city1"), AddressDetails[2]);
			    	wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.id("address.accountStateProvince.field1"), (""))));
			    	WebDriver_Functions.Select(By.id("address.accountStateProvince.field1"), AddressDetails[3], "t");
			    	WebDriver_Functions.Type(By.id("address.alternate.phoneNumber"), strPhone);
			    	WebDriver_Functions.Type(By.id("address.alternate.zipPostal"), AddressDetails[5]);
				}
			}else{
				ChangeURL("https://" + strLevelURL + "/PickupApp/scheduleFreightPickup.do?method=doInit&locale=en_" + CountryCode, true);
				
			    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("address.alternate.company.ns")));
			    WebDriver_Functions.Type(By.id("address.alternate.company.ns"), "CompanyName");
			    WebDriver_Functions.Type(By.id("address.alternate.contactName.ns"), "TestingName");
			    WebDriver_Functions.Type(By.id("address.accountAddressOne.field1"), AddressDetails[0]);
			    WebDriver_Functions.Type(By.id("address.accountAddressTwo.field1"), AddressDetails[1]);
			    WebDriver_Functions.Type(By.id("address.alternate.city1"), AddressDetails[2]);
			    wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.id("address.accountStateProvince.field1"), (""))));
			    WebDriver_Functions.Select(By.id("address.accountStateProvince.field1"), AddressDetails[3], "t");
			    WebDriver_Functions.Type(By.id("address.alternate.phoneNumber"), strPhone);
			    WebDriver_Functions.Type(By.id("address.alternate.zipPostal"), AddressDetails[5]);
			}

			WebDriver_Functions.ElementMatches(By.xpath("//*[@id='address.accountCountry.display']/div[1]/div/div[2]/label"), "*\nCountry/Territory", 116632); //Added the "* \n" due to dev has setup
			WebDriver_Functions.ElementMatches(By.xpath("//*[@id='table.shipmentDetailTable._contents._header._col2']/span"), "*Country/Territory", 116633); 
			
		    wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.id("freightPickupInfo.readyTime"), (""))));
		    
		    WebDriver_Functions.ElementMatches(By.cssSelector("div.pickupInfoRequiredContentLeft.pickupFloatLeft > div > label"), "Over Length (8 feet to < 12 feet)\nOver Length (8 feet to < 12 feet)", 179730); 
		    WebDriver_Functions.ElementMatches(By.xpath("//div[@id='module.pickupInfo.specialServices']/div[3]/div/div/div[2]/label"), "Extreme Length (12 feet or greater)\nExtreme Length (12 feet or greater)", 179730); 

		    //the location
		    WebDriver_Functions.Type(By.id("zipCode1"), AddressDetails[5]);
		    WebDriver_Functions.Select(By.id("serviceType1"), "Economy", "t");
		    WebDriver_Functions.Type(By.id("handlingUnits1"), HandelingUnits);
			WebDriver_Functions.Type(By.id("weight1"), Weight);
		    
		    
		    ///need to check here as the time is not loading as default.
		    CalenderDate("pickupInfo.freight.pickupDate", getDayOfMonth() + 2 + "");    ///need to test
		    
		    
		    WebDriver_Functions.Select(By.id("freightPickupInfo.readyTime"), "3:00 pm", "t");
		    WebDriver_Functions.Select(By.id("freightPickupInfo.closeTime"), "11:00 pm", "t");
		    captureScreenShot("WDPA", WDPAPath + " Pickup.png");
		    WebDriver_Functions.Click(By.id("button.freightpickup.schedulePickup"));
		    
		    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.confirmationRtColumnRight > div.confirmationFullWidthColumn > div.confirmationContentRight > label")));
		    String strConfirmationNumber = driver.findElement(By.cssSelector("div.confirmationRtColumnRight > div.confirmationFullWidthColumn > div.confirmationContentRight > label")).getText();
		    strConfirmationNumber = strConfirmationNumber.substring(strConfirmationNumber.indexOf(".") + 2);
		    Helper_Functions.PrintOut("Schedule LTL Pickup:   " + strConfirmationNumber, true);
		    WebDriver_Functions.ElementMatches(By.xpath("//*[@id='confirmationLeftPanel']/div[1]/div[1]/div/div/label"), "Country/Territory", 116629); 
		    captureScreenShot("WDPA", WDPAPath + " Confirmation.png");
		    
		    if (User != ""){
			    WebDriver_Functions.Click(By.id("menubar.nav.menu3_div"));
			    WebDriver_Functions.Click(By.id("account.freight.accountBox._LookupButton"));
			    WebDriver_Functions.Select(By.id("account.freight.accountBox._InputSelect"), strAccountSelected, "t");
			    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("history.search")));
			    WebDriver_Functions.Type(By.id("history.filterField"), strConfirmationNumber);
			    WebDriver_Functions.Select(By.id("history.filterColumn"), "Confirmation no.", "t");
			    WebDriver_Functions.Click(By.id("history.search"));
			    
			    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("row.check1")));
			    assertEquals("Scheduled", driver.findElement(By.id("statusLink0")).getText());
			    assertEquals(strConfirmationNumber, driver.findElement(By.id("confNumLink0")).getText());
			    
			    WebDriver_Functions.Click(By.id("row.check1"));
			    WebDriver_Functions.Click(By.id("history.viewPrintPickupDetails"));
			    
			    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.confirmationAlertMessage.pickupAlertMessageText")));
			    assertEquals(HandelingUnits, driver.findElement(By.xpath("//div[@id='confirmationRightPanel']/div[5]/div[2]/div/div/label")).getText());
			    assertEquals(Weight, driver.findElement(By.xpath("//div[@id='confirmation.weight']/label")).getText());
			    assertEquals("3:00pm - 11:00pm", driver.findElement(By.xpath("//div[@id='confirmation.pickuptime']/label")).getText());
			    assertEquals(AddressDetails[5], driver.findElement(By.id("confirmation.lineItems.zipPostal")).getText());
			    captureScreenShot("WDPA", WDPAPath + " LTLDetails.png");
			    Helper_Functions.PrintOut("WDPALTLPickup Completed", true);
		    }
		    return strConfirmationNumber;
	     } catch (Exception e) {
			 try{ 
				 if (driver.findElements(By.xpath("//*[@id='primary.error.display']/div/label")).size() > 0){
					 Helper_Functions.PrintOut(driver.findElement(By.xpath("//*[@id='primary.error.display']/div/label")).getText(), true);
				 }
			 }catch (Exception e2){}
			 GeneralFailure(e);
			 throw e;
		 }
	}//end WDPALTLPickup

	public static void WDPAShipment(String CountryCode, String User, String Password, String Service,  String OriginAddressDetails[], String DestAddressDetails[]) throws Exception{
		TestData.LoadTime();
		String WDPAPath = strTodaysDate + " " + strLevel + " INET ";

		try {
			// launch the browser and direct it to the Base URL
			Login(User, Password);
			ChangeURL("https://" + strLevelURL + "/shipping/shipAction.do?method=doInitialize&urlparams=" + CountryCode.toLowerCase());
			WebDriver_Functions.Click(By.id("module.from._headerEdit"));
			for (int i = 0; i < 2; i++){
				String Loc = "to";
				String AddressDetails[] = DestAddressDetails; //{Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, CountryCode - 6};
				if (i == 1){
					Loc = "from";
					AddressDetails = OriginAddressDetails;
				}
				//wait for country to load
	 			wait.until(ExpectedConditions.textMatches(By.id(Loc + "Data.countryCode"), Pattern.compile("[\\s\\S]*A[\\s\\S]*$")));
	 			WebDriver_Functions.Select(By.id(Loc + "Data.countryCode"), AddressDetails[6].toUpperCase(), "v");
				wait.until(ExpectedConditions.textMatches(By.id(Loc + "Data.countryCode"), Pattern.compile("[\\s\\S]*A[\\s\\S]*$")));
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id(Loc + "Data.contactName")));
				String ContactName = getRandomString(8);//enter filler name
				
				WebDriver_Functions.Type(By.id(Loc + "Data.companyName"), "Comp " + ContactName);
				WebDriver_Functions.Type(By.id(Loc + "Data.addressLine1"), AddressDetails[0]);
				WebDriver_Functions.Type(By.id(Loc + "Data.addressLine2"), AddressDetails[1]);
				if (!AddressDetails[5].isEmpty()){
					//had to change method of imput due to issues with error message when zip is cleared
					JavascriptExecutor myExecutor = ((JavascriptExecutor) driver);
			    	WebElement zipcode = driver.findElement(By.id(Loc + "Data.zipPostalCode"));
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
		    WebElement dropdown = driver.findElement(By.id("psdData.serviceType"));
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
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("psd.mps.row.weight.0")));
				driver.findElement(By.id("psd.mps.row.weight.0")).clear();
				driver.findElement(By.id("psd.mps.row.weight.0")).sendKeys("10");
				WebDriver_Functions.Select(By.id("psdData.packageType"), "FedEx Box", "v");
				if (optTxt.contains("International")){
					wait.until(ExpectedConditions.elementToBeClickable(By.id("commodityData.packageContents.products"))).click();
					driver.findElement(By.id("commodityData.totalCustomsValue")).clear();
					driver.findElement(By.id("commodityData.totalCustomsValue")).sendKeys("10");
				}
				//Pickup/Drop-off Modal
				actions.moveToElement(driver.findElement(By.id("module.from._headerTitle"))).perform();
				wait.until(ExpectedConditions.elementToBeClickable(By.id("pdm.initialChoice.schedulePickup"))).click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pickupAddress.collapse")));
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("packageInfo.collapse")));
			}else if (Service.contentEquals("Ground")){
				WebDriver_Functions.Select(By.id("psdData.packageType"), "2", "i");//Barrel
				WebDriver_Functions.Select(By.id("commodityData.shipmentPurposeCode"), "3", "i");//Gift
				driver.findElement(By.id("commodityData.totalCustomsValue")).clear();
				driver.findElement(By.id("commodityData.totalCustomsValue")).sendKeys("10");
				//Pickup/Drop-off Modal
				actions.moveToElement(driver.findElement(By.id("module.from._headerTitle"))).perform();
				wait.until(ExpectedConditions.elementToBeClickable(By.id("pdm.initialChoice.schedulePickup"))).click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pickupAddress.collapse")));
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("packageInfo.collapse")));
			}else if (Service.contentEquals("Freight")){
				//Package type is set to "Your Packaging"
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("psd.mps.row.dimensions.0")));
				driver.findElement(By.id("psd.mps.row.weight.0")).clear();
				driver.findElement(By.id("psd.mps.row.weight.0")).sendKeys("200");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("psd.mps.row.dimensions.0")));
				WebDriver_Functions.Select(By.id("psd.mps.row.dimensions.0"), "manual", "v");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("psd.mps.row.dimensionLength.0")));
				wait.until(ExpectedConditions.elementToBeClickable(By.id("psd.mps.row.dimensionLength.0")));
				driver.findElement(By.id("psd.mps.row.dimensionLength.0")).clear();
				driver.findElement(By.id("psd.mps.row.dimensionLength.0")).sendKeys("8");
				driver.findElement(By.id("psd.mps.row.dimensionWidth.0")).clear();
				driver.findElement(By.id("psd.mps.row.dimensionWidth.0")).sendKeys("4");
				driver.findElement(By.id("psd.mps.row.dimensionHeight.0")).clear();
				driver.findElement(By.id("psd.mps.row.dimensionHeight.0")).sendKeys("3");
				
				//Pickup/Drop-off Modal
				actions.moveToElement(driver.findElement(By.id("module.from._headerTitle"))).perform();
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dropoff.viewMoreLocations.link")));
				wait.until(ExpectedConditions.elementToBeClickable(By.id("pdm.initialChoice.schedulePickup"))).click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pickupAddress.collapse")));
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("packageInfo.collapse")));
				//edit the pickup information
				wait.until(ExpectedConditions.elementToBeClickable(By.id("packageInfo.edit.plus"))).click();
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pdm.personWithSkidNumber")));
				driver.findElement(By.id("pdm.personWithSkidNumber")).clear();
				driver.findElement(By.id("pdm.personWithSkidNumber")).sendKeys("John");
				WebDriver_Functions.Select(By.id("pdm.dimProfile"), "manual", "v");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pdm.dimLength")));
				wait.until(ExpectedConditions.elementToBeClickable(By.id("pdm.dimLength")));
				driver.findElement(By.id("pdm.dimLength")).clear();
				driver.findElement(By.id("pdm.dimLength")).sendKeys("8");
				driver.findElement(By.id("pdm.dimWidth")).clear();
				driver.findElement(By.id("pdm.dimWidth")).sendKeys("4");
				driver.findElement(By.id("pdm.dimHeight")).clear();
				driver.findElement(By.id("pdm.dimHeight")).sendKeys("3");
				//Lift gate
				WebDriver_Functions.Select(By.id("pdm.truckType"), "L", "v");
				//Trailer size = 28
				WebDriver_Functions.Select(By.id("pdm.truckSize"), "28", "v");
			}
			
			captureScreenShot("WDPA", WDPAPath + "Shipment.png");
			wait.until(ExpectedConditions.elementToBeClickable(By.id("completeShip.ship.field"))).click();
			
			//Enter product/commodity information
			if (optTxt.contains("International")){
				try{
					WebDriver_Functions.Select(By.id("commodityData.chosenProfile.profileID"), "add", "v");
					wait.until(ExpectedConditions.presenceOfElementLocated(By.id("commodityData.chosenProfile.description")));
					driver.findElement(By.id("commodityData.chosenProfile.description")).sendKeys("Generic Description");
					WebDriver_Functions.Select(By.id("commodityData.chosenProfile.unitOfMeasure"), "1", "i");
					driver.findElement(By.id("commodityData.chosenProfile.quantity")).sendKeys("10");
					driver.findElement(By.id("commodityData.chosenProfile.commodityWeight")).sendKeys("50");
					WebDriver_Functions.Select(By.id("commodityData.chosenProfile.manufacturingCountry"), "1", "i");
					wait.until(ExpectedConditions.elementToBeClickable(By.id("commodity.button.addCommodity"))).click();
					wait.until(ExpectedConditions.textToBe(By.id("commodity.summaryTable._contents._row1._col2"), "Generic Description"));
					captureScreenShot("WDPA", WDPAPath + "product_commodity information.png");
					wait.until(ExpectedConditions.elementToBeClickable(By.id("completeShip.ship.field"))).click();
				}catch (Exception e){}
			}else{
				//Confirm shipping details
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("confirm.ship.field")));
				wait.until(ExpectedConditions.elementToBeClickable(By.id("completeShip.ship.field"))).click();
			}
			
			//confirmation page
			wait.until(ExpectedConditions.presenceOfElementLocated(By.id("trackingNumber")));

			Helper_Functions.PrintOut("Need to finish", true);

			if (driver.findElements(By.id("label.alert.unsuccessfulPickupSchedule")).size() > 0){
				Helper_Functions.PrintOut(driver.findElement(By.id("label.alert.unsuccessfulPickupSchedule")).getText(), true);
			}
			
		}catch (Exception e){
			GeneralFailure(e);
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
				if (WebDriver_Functions.isPresent(By.id(AttemptDate)) && driver.findElement(By.id(AttemptDate)).getAttribute("class").contentEquals("enabledDateStyle")) {
					LastAvailable = AttemptDate;
					if (driver.findElement(By.id(LastAvailable)).getText() == Date || Date == null) {//if a date is provided will return once found as a valid option, if date is null will return once finds first avaialbe.
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

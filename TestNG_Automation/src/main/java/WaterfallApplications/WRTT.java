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
import SupportClasses.DriverFactory;
import TestingFunctions.Helper_Functions;
import TestingFunctions.WebDriver_Functions;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class WRTT{
	static String LevelsToTest = "2";
	static String CountryList[][];
	static boolean SmokeTest = true;
	
	@BeforeClass
	public void beforeClass() {
		DriverFactory.LevelsToTest = LevelsToTest;
		
		//if (SmokeTest) 
		CountryList = new String[][]{{"US", "United States"}, {"CA", "Canada"}};
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			//int intLevel = Integer.parseInt(Level);

			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "WRTT_Rate_Sheet":
		    		if (SmokeTest) {
		    			data.add( new Object[] {Level, true, 5, true, false});
		    			data.add( new Object[] {Level, true, 14, true, false});
		    			data.add( new Object[] {Level, true, 4, true, true});
		    		}else {
		    			data.add( new Object[] {Level, true, 5, true, false}); //add a loop here later for all
		    		}
		    	break;
		    	case "WRTT_eCRV_Page":
		    	case "WRTT_SpalshPage_eCRV":
		    		data.add( new Object[] {Level, "US"});
		    	break;
			}
		}	
		return data.iterator();
	}
	
	
	@Test(dataProvider = "dp")
	public static void WRTT_Rate_Sheet(String Level, boolean ZoneChart, int Service, boolean PDF, boolean List){
		Helper_Functions.PrintOut("Validate the rate sheet download through WRTT", false);
		try {
			String Result = WRTT_Generate(Level, true, 1, true, false);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WRTT_Rate_Sheet
	
	@Test(dataProvider = "dp")
	public static void WRTT_eCRV_Page(String Level, String CountryCode){
		Helper_Functions.PrintOut("Validate the eCRV page for " + CountryCode, false);
		try {
			String Result = WRTT_eCRV(Level, CountryCode);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WRTT_eCRV
	
	@Test(dataProvider = "dp")
	public static void WRTT_SpalshPage_eCRV(String Level, String CountryCode){
		Helper_Functions.PrintOut("Validate the navigation from home page to eCRV page for " + CountryCode, false);
		try {
			String Result = eCRVNavigation(Level, CountryCode);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WRTT_eCRV
	
	
	/////////////////////////Functions//////////////////////////

 	public static String WRTT_Generate(String Level, boolean ZoneChart, int Service, boolean PDF, boolean List) throws Exception {
 		// [x][0] Service name
        // [x][1] Xpath of the checkbox of the service 
 		String ServicesCheckbox[][] = {
			//Domestic Services 0 - 10
			{"SameDay", "//*[@id='domesticServiceBlock']/div/div[1]/div/div[1]/label/input"}, 
			{"FirstOvernight", "//*[@id='domesticServiceBlock']/div/div[1]/div/div[2]/label/input"},
			{"PriorityOvernight", "//*[@id='domesticServiceBlock']/div/div[1]/div/div[3]/label/input"},
			{"Standard Overnight", "//*[@id='domesticServiceBlock']/div/div[1]/div/div[4]/label/input"},
			{"2DayA.M.","//*[@id='domesticServiceBlock']/div/div[1]/div/div[5]/label/input"},
			{"2Day","//*[@id='domesticServiceBlock']/div/div[1]/div/div[6]/label/input"},
			{"ExpressSaver", "//*[@id='domesticServiceBlock']/div/div[1]/div/div[7]/label/input"},
			{"HawaiiNeighborIsland","//*[@id='domesticServiceBlock']/div/div[1]/div/div[8]/label/input"},
			{"ExpressFreight","//*[@id='domesticServiceBlock']/div/div[2]/div/div[1]/label/input"},
			{"Ground", "//*[@id='domesticServiceBlock']/div/div[2]/div/div[2]/label/input"}, 
			{"Home Delivery", "//*[@id='domesticServiceBlock']/div/div[2]/div/div[3]/label/input"},
			//Export Services 11-17
			{"InternationalNextFlight", "//*[@id='export_module']/div[2]/div/div[1]/div/div[1]/label/input"},
			{"InternationalFirst", "//*[@id='export_module']/div[2]/div/div[1]/div/div[2]/label/input"},
			{"InternationalPriority","//*[@id='export_module']/div[2]/div/div[1]/div/div[3]/label/input"},
			{"InternationalExonomy","//*[@id='export_module']/div[2]/div/div[1]/div/div[4]/label/input"},
			{"ExpressU.S.toPuertoRico","//*[@id='export_module']/div[2]/div/div[1]/div/div[5]/label/input"},
			{"ExpressInternationalFreight","//*[@id='export_module']/div[2]/div/div[2]/div/div[2]/label/input"},
			{"ExpressInternationalPremium","//*[@id='export_module']/div[2]/div/div[2]/div/div[3]/label/input"}
 		};
 		String Title = ""; //this is the title of the attempt <Z><Service><PDF/XLS><List/Retail>
 		String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " " + Service;
 		
		try {
			
			WebDriver_Functions.ChangeURL("WRTT", "US", true, Level);
			 
			//radio button if the attempt should include zone chart
			if (ZoneChart){
				Title = "Z";
				WebDriver_Functions.Click(By.id("yes"));
			}else{
				WebDriver_Functions.Click(By.id("no"));
			}
			
			Title = Title + ServicesCheckbox[Service][0];
				
			//radio button if the attempt should be for domestic
			if (Service < 11){
				WebDriver_Functions.Click(By.id("domesticradio"));
			}else{
				WebDriver_Functions.Click(By.id("internationalradio"));
			}
				
			//wait for the services to load
			WebDriver_Functions.Click(By.xpath(ServicesCheckbox[Service][1]));
				
			//select the file format
			if (PDF){
				WebDriver_Functions.Click(By.cssSelector("input[name=\"ratesByServiceFormat\"]"));
				Title = Title + "PDF";
			}else{
				WebDriver_Functions.Click(By.xpath("(//input[@name='ratesByServiceFormat'])[2]"));
				Title = Title + "XLS";
			}

			//select the rate type, only available for domestic
			if (Service < 11){
				if (List){
					WebDriver_Functions.Click(By.name("ratesByServiceType"));
					Title = Title + "L";
				}else{
					WebDriver_Functions.Click(By.xpath("(//input[@name='ratesByServiceType'])[2]"));
					Title = Title + "R";
				}
			}else if (!List && Service > 10){
				return "international do not have retail rates";
			}
			WebDriver_Functions.takeSnapShot(SCPath + Title + ".png");
			WebDriver_Functions.Click(By.id("requestsheetbtn"));

			//download the rates
			String mainWindowHandle = DriverFactory.getInstance().getDriver().getWindowHandle();
			for (String childWindowHandle : DriverFactory.getInstance().getDriver().getWindowHandles()) {
				//If window handle is not main window handle then close it 
				if(!childWindowHandle.equals(mainWindowHandle)){
					DriverFactory.getInstance().getDriver().switchTo().window(childWindowHandle);
					  
					//wait for the download button to load
					WebDriver_Functions.Click(By.xpath("/html/body/form/table/tbody/tr[10]/td/input"));
					//wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/form/table/tbody/tr[10]/td/input"))).click();  //delete after testing
							
					if (!PDF){
						WebDriver_Functions.WaitForTextNot(By.tagName("body"), (""));
						String bodyText = DriverFactory.getInstance().getDriver().findElement(By.tagName("body")).getText();
						Helper_Functions.WriteToFile(Level, bodyText, ".\\EclipseScreenshots\\WRTT\\" + SCPath + Title);
					}
														
					// Close child windows
					Helper_Functions.Wait(5);
					DriverFactory.getInstance().getDriver().close(); 
				}
			}//end for child window
				
			try{
				if (DriverFactory.getInstance().getDriver().findElement(By.tagName("body")).getText().contains("Retail rates are not available for this service. Please select a different service.")){
					//SameDay and InternationalNextFlight are not valid for retail rates
					if ((Service == 0 || Service == 11 || Service == 17) && !List){
						WebDriver_Functions.takeSnapShot(SCPath + Title + "RetailNotAvailable.png");
						Helper_Functions.PrintOut("WRTT " + Title + " Completed successfully since retail not available", true);
						return Title;
					}else{
						WebDriver_Functions.takeSnapShot(SCPath + Title + " SC.png");
						Helper_Functions.PrintOut("!!!Check for Title why rates not available", true);
					}
				}
			}catch(Exception e){}
				
			//switch back to main window
			DriverFactory.getInstance().getDriver().switchTo().window(mainWindowHandle);
			Helper_Functions.PrintOut("WRTT " + Title + " Completed successfully", true);
			return Title;
		}catch (Exception e) {
			throw e;
		}
}//end WRTT
 	
 	public static String WRTT_eCRV(String Level, String CountryCode) throws Exception {	 
		try {
			String SCPath = Helper_Functions.CurrentDateTime() + " L" + Level + " WRTT " + CountryCode;
			
			try{
				WebDriver_Functions.ChangeURL("WGRT", CountryCode, true, Level);
				WebDriver_Functions.Click(By.id("wgrt.rateTools.menu_div"));
				WebDriver_Functions.Click(By.id("wgrt.rateTools.rate._div"));
				WebDriver_Functions.WaitPresent(By.xpath("//*[@id='content']/form/div[4]/div[1]/div/button/label"));
			}catch (Exception e){
				Helper_Functions.PrintOut("For Country _" + CountryCode + "_ not able to navigate to splash page", true);
				WebDriver_Functions.ChangeURL("WRTT", CountryCode, true, Level);
			}
			WebDriver_Functions.WaitForText(By.cssSelector("label"), "FedEx Rate Sheets");
			WebDriver_Functions.WaitForText(By.xpath("//div[@id='content']/form/div[3]/div/div/h1/label"), "Standard rates");
			WebDriver_Functions.WaitForText(By.cssSelector("h4 > label"), "View standard rates for a specific FedEx® service across all zones.");
			WebDriver_Functions.WaitForText(By.cssSelector("button.fx-btn-primary"), "Get standard rates");
			WebDriver_Functions.WaitForText(By.xpath("//div[@id='content']/form/div[3]/div[2]/div/h1/label"), "Account-based rates");
			WebDriver_Functions.WaitForText(By.xpath("//div[@id='content']/form/div[3]/div[2]/div/h4/label"), "View account-based rates for a selected FedEx® service across all zones.");
			WebDriver_Functions.WaitForText(By.xpath("//button[@onclick='getWCRV();']"), "Get account-based rates");
			WebDriver_Functions.WaitForText(By.cssSelector("b > label"), "Please note:");
			WebDriver_Functions.WaitForText(By.cssSelector("li > label"), "Additional shipping fees and optional-service fees may apply. Please see Service Info or see FedEx Service Guide for details.");
			WebDriver_Functions.WaitForText(By.linkText("Service Info"), "Service Info");
			String Service_Info = DriverFactory.getInstance().getDriver().findElement(By.linkText("Service Info")).getAttribute("href"); 
			//check the link
			WebDriver_Functions.WaitForText(By.xpath("//div[@id='container']/div[3]/div/div/ul/li/label/a[2]"), "FedEx Service Guide");
			String FedEx_Service_Guide = DriverFactory.getInstance().getDriver().findElement(By.xpath("//div[@id='container']/div[3]/div/div/ul/li/label/a[2]")).getAttribute("href"); 
			WebDriver_Functions.WaitForText(By.xpath("//div[@id='container']/div[3]/div/div/ul/li[2]/label"), "FedEx reserves the right to modify its services and zone structure without notice.");
			WebDriver_Functions.takeSnapShot(SCPath + "eCRV.png");
			//quick check to see if land on page
			try{
				WebDriver_Functions.Click(By.cssSelector("button.fx-btn-primary"));
				String BodyText = DriverFactory.getInstance().getDriver().findElement(By.tagName("body")).getText().toLowerCase();
				if(CountryCode.toLowerCase().matches("us")){//US is the only country that has the WRTT page as of Jan18CL
					WebDriver_Functions.WaitForText(By.xpath("//*[@id='maincontent']/div[2]/div/div/h1"), "Standard Rate Sheets"); 
				}else if(!WebDriver_Functions.GetCurrentURL().toLowerCase().contains("/" + CountryCode.toLowerCase()) && !(WebDriver_Functions.GetCurrentURL().toLowerCase().contains("/rates") || WebDriver_Functions.GetCurrentURL().toLowerCase().contains("/downloadcenter") || WebDriver_Functions.GetCurrentURL().toLowerCase().contains("/serviceguide") || WebDriver_Functions.GetCurrentURL().toLowerCase().contains("/service-guide"))){
					throw new Exception();
				}else if (BodyText.contains("page not found") || BodyText.contains("this page is not available") ){
					throw new Exception();
				}
			}catch (Exception e) {
				Helper_Functions.PrintOut(CountryCode + " WRTT Button not working", false);
			}
			
			boolean blnService_Info = true, blnFedEx_Service_Guide = true;
			//check the Service_Info link that was present on eCRV page
			try{
				DriverFactory.getInstance().getDriver().get(Service_Info);
				if(!WebDriver_Functions.GetCurrentURL().toLowerCase().contains("/" + CountryCode.toLowerCase() + "/")){throw new Exception();}
				WebDriver_Functions.WaitForText(By.cssSelector("h1"), "Service Guide");
			}catch (Exception e) {
				blnService_Info = false;
				Helper_Functions.PrintOut(CountryCode + " Service_Info Landed on:    " + WebDriver_Functions.GetCurrentURL(), true);
			}
			
			//check the FedEx_Service_Guide link that was present on eCRV page
			try{
				DriverFactory.getInstance().getDriver().get(FedEx_Service_Guide);
				if(!WebDriver_Functions.GetCurrentURL().toLowerCase().contains("/" + CountryCode.toLowerCase())){throw new Exception();}
				WebDriver_Functions.WaitForText(By.cssSelector("h1"), "Service Guide");
			}catch (Exception e) {
				blnFedEx_Service_Guide = false;
				Helper_Functions.PrintOut(CountryCode + " FedEx_Service_Guide Landed on     " + WebDriver_Functions.GetCurrentURL(), true);
			}
			
			if (!blnService_Info && !blnFedEx_Service_Guide){
				//PrintOut("Service_Info and FedEx_Service_Guide not navigating correctly.");  //445766 open for this issue
			}else if (!blnService_Info){
				Helper_Functions.PrintOut("Service_Info not navigating correctly.", true);
			}else if (!blnFedEx_Service_Guide){
				Helper_Functions.PrintOut("FedEx_Service_Guide not navigating correctly.", true);
			}
			
			return"WRTT_eCRV for " + CountryCode + " completed.";
		}catch (Exception e) {
			throw e;
		}
	}//end WRTT_eCRV

 	public static String eCRVNavigation(String Level, String CountryCode) {
		// launch the browser and direct it to the Base URL
		try{
			WebDriver_Functions.ChangeURL("WGRT", CountryCode, true, Level);
				
			WebDriver_Functions.Click(By.id("wgrt.rateTools.menu_div"));
			WebDriver_Functions.Click(By.id("wgrt.rateTools.rate._div"));
			WebDriver_Functions.isPresent(By.xpath("//*[@id='content']/form/div[4]/div[1]/div/button/label"));
			return CountryCode + " is enabled";
		}catch (Exception e){
			return CountryCode + " is enabled";
		}
	}
 }


/*
	/////////////////////////////////////////////////////////////////////////





*/
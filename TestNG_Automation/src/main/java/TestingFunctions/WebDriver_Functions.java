package TestingFunctions;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import SupportClasses.*;

public class WebDriver_Functions{
	
	public static void ChangeURL(String AppDesignation, String CountryCode, boolean ClearCookies, String Level) throws Exception {
		String LevelURL = null, AppUrl = null, CCL = CountryCode.toLowerCase(),CCU = CountryCode.toUpperCase();
		
		LevelURL = LevelUrlReturn(Integer.valueOf(Level));

		switch (AppDesignation) {
    		case "INET":		
    			AppUrl = LevelURL + "/cgi-bin/ship_it/interNetShip?origincountry=" + CCL + "&locallang=en";;
    			break;
    		case "WADM":
    			AppUrl = LevelURL + "/apps/shipadmin/";
				break;
    		case "FCLCreate":  	
    			//AppUrl = LevelURL + "/fcl/web/jsp/signup.jsp";
    			AppUrl = LevelURL + "/fcl/web/jsp/contactInfo1.jsp?appName=oadr&locale=" + CCL + "_en&step3URL=https%3A%2F%2F.fedex.com%3A443%2Ffcl%2Fweb%2Fjsp%2FfclValidateAndCreate.jsp&afterwardsURL=" + LevelURL + "%3A443%2Ffcl%2Fweb%2Fjsp%2Foadr.jsp&programIndicator=p314t9n1ey&accountOption=link";
				break;
    		case "FCLLink":  	
    			AppUrl = LevelURL + "/fcl/web/jsp/contactInfo1.jsp?appName=oadr&locale=" + CCL + "_en&step3URL=https%3A%2F%2F.fedex.com%3A443%2Ffcl%2Fweb%2Fjsp%2FfclValidateAndCreate.jsp&afterwardsURL=" + LevelURL + "%3A443%2Ffcl%2Fweb%2Fjsp%2Foadr.jsp&programIndicator=p314t9n1ey&accountOption=link";
				break;
    		case "FCLLinkInter":  	
    			AppUrl = LevelURL + "/fcl/web/jsp/contactInfo.jsp?appName=fclfsm&locale=" + CCL + "_en&step3URL=" + LevelURL + "%2Fship%2FshipEntryAction.do%3Fmethod%3DdoRegistration%26link%3D1%26locale%3Den_" + CCU + "%26urlparams%3D" + CCL + "%26sType%3DF&afterwardsURL=" + LevelURL + "%2Fship%2FshipEntryAction.do%3Fmethod%3DdoEntry%26link%3D1%26locale%3Den_" + CCU + "%26urlparams%3D" + CCL + "%26sType%3DF&programIndicator=0";
    			if(Helper_Functions.Check_Country_Region(CountryCode).contains("APAC")){//APAC Country
					AppUrl = AppUrl.replaceAll("%2Fship%2", "%2Fshipping_apac%2");
 	 			}
    			break;	
    		case "AdminReg":  	
    			AppUrl = LevelURL + "/fcl/web/jsp/accountInfo1.jsp?appName=fclpasskey&registration=true&countryCode=" + CCL + "&languageCode=en&fclHost=" + LevelURL + "&step3URL=" + LevelURL + "%2Fapps%2Fshipadmin&afterwardsURL=" + LevelURL + "%2Fapps%2Fshipadmin&locale=en_" + CCU + "&programIndicator=1";
				break;
    		case "Pref":  		
    			AppUrl = LevelURL + "/preferences";
    			break;		
    		case "JSP":  		
    			AppUrl = "http://vjb00030.ute.fedex.com:7085/cfCDSTestApp/contact.jsp";//independent of level
				break;
    		case "ECAM":			
    			if (Level.contentEquals("2")) {
    				AppUrl = "https://ecamdev.idev.fedex.com/ecam/index.html";
    			}else if (Level.contentEquals("3")) {
    				AppUrl = "https://ecamdrt.idev.fedex.com/ecam/index.html";
    			}
    			break;
    		case "WPOR":  	
    			AppUrl = LevelURL + "/" + CCL + "/developer/web-services/process.html?tab=tab1";
				break;
    		case "WPORTR":  // WPOR flow - Technology Resources	
    			AppUrl = LevelURL + "/" + CCL + "/developer/ship-manager-server/process.html?tab=tab1";
				break;	
    		case "WPORWS":  	
    			AppUrl = LevelURL + "/" + CCL + "/developer/web-services/process.html?tab=tab3";
				break; 
    		case "WGTM":
    			AppUrl = LevelURL + "/GTM?cntry_code=" + CCL;
				break;
    		case "WIDM":// this is the Find International documents link from GTM page
    			AppUrl = LevelURL + "/FID?clienttype=dotcom&clickedPrint=false&action=entry&hazmatFilter=All&cntry_code=" + CCL + "&lang_code=en&option=fid";
				break;
    		case "HOME":  	
    			AppUrl = LevelURL + "/en-us/home.html";
				break;
    		case "WDPA":  	
    			AppUrl = LevelURL + "/PickupApp/login?locale=en_" + CCL;
				break;	
    		case "WDPA_LTL":  	
    			AppUrl = LevelURL + "/PickupApp/scheduleFreightPickup.do?method=doInit&locale=en_" + CCL;
				break;	
    		case "WPRL":  	
    			AppUrl = LevelURL + "/apps/myprofile/loginandcontact/?locale=en_" + CCU + "&cntry_code=" + CCL;
				break;
    		case "WCRV":  	
    			AppUrl = LevelURL + "/apps/ratevisibility/";
				break;
    		case "WRTT":  	
    			AppUrl = LevelURL + "/ratetools/RateToolsMain.do";
    			AppUrl = AppUrl.replace("https", "http");
				break;
    		case "WGRT":  	
    			AppUrl = LevelURL + "/ratefinder/home?cc=" + CCU + "&language=en&locId=express";
				break;
    			/*
    			case "":  	
    			AppUrl = LevelURL + ;
				break;
    			 */
    		default: //as a fall back append the correct level to the AppDesignation that is sent.
    			if (AppDesignation.contains("Enrollment_")){//https://wwwdrt.idev.fedex.com/fcl/ALL?enrollmentid=ml18024117&language=en&country=us 
    				AppDesignation = AppDesignation.replace("Enrollment_", "");
    				AppUrl = LevelURL + "/fcl/ALL?enrollmentid=" + AppDesignation + "&language=en&country=" + CCL;
    			}
		}//end switch AppDesignation
		
		if (ClearCookies) {
			DriverFactory.getInstance().getDriver().get(LevelURL + "/en-us/home.html");
			DriverFactory.getInstance().getDriver().manage().deleteAllCookies();
			Helper_Functions.PrintOut("Cookies Deleted", true);
		}
		Helper_Functions.PrintOut("Navigating to " + AppUrl, true);
		
		DriverFactory.getInstance().getDriver().get(AppUrl);
		
		if (AppDesignation == "WGTM" && !CheckBodyText("English")) {
			DriverFactory.getInstance().getDriver().get(AppUrl + "_english");
		}
	}
	
	public static boolean CheckBodyText(String TextToCheck) {
		String bodyText = DriverFactory.getInstance().getDriver().findElement(By.tagName("body")).getText();
		if (bodyText.contains(TextToCheck)) {
			return true;
		}
		return false;
	}
	
	public static void Type(By Ele, String text) throws Exception {
		WaitPresent(Ele);
		for (int i = 0 ; i < 10 ; i++) {
			WebElement Element = DriverFactory.getInstance().getDriver().findElement(Ele);
			Element.clear();
			if (i < 5) {
				Element.sendKeys(text);//try entering character by character
			}else {
				JavascriptExecutor myExecutor = ((JavascriptExecutor) DriverFactory.getInstance().getDriver());
				myExecutor.executeScript("arguments[0].value='" + text + "';", Element); //enter all at once, fastest method
			}
			try {Thread.sleep(100);} catch (InterruptedException e) {}//wait for a moment the check to make sure text was entered as expected
			if (Element.getText().contentEquals(text) || Element.getAttribute("value").contentEquals(text)) {
				Helper_Functions.PrintOut("    T--Text Entered " + text + " in element " + Ele.toString(), true);
				//Element.sendKeys(Keys.DOWN);//check on this copied from WCRV
				//Element.sendKeys(Keys.RETURN);
				return;
			}
		}
		throw new Exception("Not able to enter text");
    }
	
	public static void Click(By Ele) throws Exception{
		JavascriptExecutor js = ((JavascriptExecutor) DriverFactory.getInstance().getDriver());
		Actions a = new Actions(DriverFactory.getInstance().getDriver());
		try {
			a.moveToElement(DriverFactory.getInstance().getDriver().findElement(Ele)).perform();
			//DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.elementToBeClickable(Ele));
			DriverFactory.getInstance().getDriver().findElement(Ele).click();
		}catch (Exception e) {
			try {
				js.executeScript("window.scrollBy(0, 300)");//scroll down
				a.moveToElement(DriverFactory.getInstance().getDriver().findElement(Ele)).perform();
				//DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.elementToBeClickable(Ele));
				DriverFactory.getInstance().getDriver().findElement(Ele).click();
			}catch (Exception e1) {
				js.executeScript("window.scrollBy(0, -300)");//scroll up
				a.moveToElement(DriverFactory.getInstance().getDriver().findElement(Ele)).perform();
				//DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.elementToBeClickable(Ele));
				DriverFactory.getInstance().getDriver().findElement(Ele).click();
			}
		}			
		Helper_Functions.PrintOut("    E--Element Clicked " + Ele.toString(), true);
		// js.executeScript("window.scrollBy(0, -100)");//scroll down
		//js.executeScript("window.scrollBy(0,-2000)");// This  will scroll up the page by 500 pixel vertical	
		//js.executeScript("window.scrollTo(0, document.body.scrollHeight)");  //scroll to bottom of the page
	}
	
	//takes is the element, value, and selectByType
	public static void Select(By Ele, String Value, String SelectBy) throws Exception {
		SelectBy = SelectBy.toLowerCase(); //just in case wrong font is sent.
		DriverFactory.getInstance().getDriver().findElement(Ele).click();
		String Message = null;
		
		if (SelectBy.contentEquals("i")) {
			new Select(DriverFactory.getInstance().getDriver().findElement(Ele)).selectByIndex(Integer.parseInt(Value));
			Message = "by index " + Value;//Add to this later to show the item selected.
		}else if (SelectBy.contentEquals("v")) {
			new Select(DriverFactory.getInstance().getDriver().findElement(Ele)).selectByValue(Value);
			Message = "by value " + Value;
		}else if (SelectBy.contentEquals("t")) {
			new Select(DriverFactory.getInstance().getDriver().findElement(Ele)).selectByVisibleText(Value);
			Message = "by visible text " + Value;
		}else {
			Helper_Functions.PrintOut("Not able to select by " + SelectBy, true);
			throw new Exception("Invalid SelectBy sent to Select(By Ele, String Value, String SelectBy)");
		}
		Helper_Functions.PrintOut("    S--Selected Element " + Ele.toString() + "   " + Message, true);
	}
	
    public static void takeSnapShot(String FileName) throws Exception{
    	/// takeSnapShot(driver, ".\\EclipseScreenshots\\PPPL\\" + WFCLPath + "ContactInformation.png");  //C:\Users\5159473\eclipse-workspace\FedEx Automation\EclipseScreenshots\WFCL
    	String CallingClass = Thread.currentThread().getStackTrace()[2].getClassName();
    	CallingClass = CallingClass.substring(CallingClass.indexOf(".") + 1, CallingClass.length());//remove the package of the class
    	if (CallingClass.contains("_")) {
    		CallingClass = CallingClass.substring(0, CallingClass.indexOf("_"));
    	}
    	//The below assumes that the classes follow the format of the APPName_Method. ex. If the app is WFCL then the class name would be WFCL_JUnit or WFCL_TestNG
    	//uncomment to test the stack trace.  for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {System.out.println(ste);}
    	
    	Thread.sleep(1000); //added to give page extra time to load 
    	
        //Convert web driver object to TakeScreenshot
        TakesScreenshot scrShot =((TakesScreenshot) DriverFactory.getInstance().getDriver());
        //Call getScreenshotAs method to create image file
        File SrcFile=scrShot.getScreenshotAs(OutputType.FILE);
        //Move image file to new destination
        String FilePath = ".\\EclipseScreenshots\\" + CallingClass + "\\" + FileName;
        File DestFile=new File(FilePath);
        //Copy file at destination
        FileUtils.copyFile(SrcFile, DestFile);
        Helper_Functions.PrintOut(FileName + " Screenshot Taken", true);
    }
	
	public static boolean isPresent(By Ele){
		boolean result = true;
	    try {
	    	DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(DriverFactory.WaitTimeOut, TimeUnit.MICROSECONDS); //sets the timeout for short to make reduce delay
	        DriverFactory.getInstance().getDriver().findElement(Ele);
	    }catch (Exception e) {
	    	result = false;
	    	//Helper_Functions.PrintOut(Ele.toString() + " is not present");
	    }finally {
	    	DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(DriverFactory.WaitTimeOut, TimeUnit.SECONDS);
	    }
	
	    return result;
	}
	
	public static boolean WaitNotPresent(By Ele) throws Exception{
		boolean Result = false;
		for(int i = 0; i < DriverFactory.WaitTimeOut; i++) {
			Result = isPresent(Ele);
			if (!Result) {
				return Result;
			}
			Thread.sleep(1000);
		}
		throw new Exception ("Element is still present on page.  " + Ele.toString());
	}
    
    public static void WaitPresent(By Ele) throws Exception{
    	DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.presenceOfElementLocated(Ele));
	}
    
    public static void WaitPresent(By Ele1, By Ele2) throws Exception{
    	DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.or(
				ExpectedConditions.presenceOfElementLocated(Ele1), 
				ExpectedConditions.presenceOfElementLocated(Ele2)
				));
	}
    
	public static void WaitForText(By Ele, String Text) throws Exception{
		DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.textToBe(Ele, Text));
	}
	
	public static void WaitForTextNot(By Ele, String Text) throws Exception{
		DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.not(ExpectedConditions.textToBe(Ele, Text)));
	}
	
	public static void WaitOr_TextToBe(By Ele1, String Text1, By Ele2, String Text2) throws Exception{
		for(int i = 0; i < DriverFactory.WaitTimeOut + 1; i++) {
			String ValueT1 = DriverFactory.getInstance().getDriver().findElement(Ele1).getAttribute("value");
			String ValueT2 = DriverFactory.getInstance().getDriver().findElement(Ele2).getAttribute("value");
			String TextT1 = DriverFactory.getInstance().getDriver().findElement(Ele1).getText();
			String TextT2 = DriverFactory.getInstance().getDriver().findElement(Ele2).getText();
			if (( ValueT1 != null && ValueT1.contentEquals(Text1)) || (ValueT2 != null && ValueT2.contentEquals(Text2))) {
				break;
			}else if (( TextT1 != null && TextT1.contentEquals(Text1)) || (TextT2 != null && TextT2.contentEquals(Text2))) {
				break;
			}else if (i == DriverFactory.WaitTimeOut) {
				throw new Exception ("Text does not match.  " + Ele1.toString() + " " + Ele2.toString());
			}
			Thread.sleep(1000);
		}
	}
	
	public static void WaitForTextPresentIn(By Ele, String Text) throws Exception{
		for(int i = 0; i < DriverFactory.WaitTimeOut + 1; i++) {
			if (isPresent(Ele)) {
				String DriverValue = DriverFactory.getInstance().getDriver().findElement(Ele).getAttribute("value");
				String DriverText = DriverFactory.getInstance().getDriver().findElement(Ele).getText();
				if (DriverValue != null && DriverValue.contains(Text)) {
					break;
				}else if (DriverText!= null && DriverText.contains(Text)) {
					break;
				}else if (i == DriverFactory.WaitTimeOut) {
					throw new Exception ("Text does not match.  " + Ele.toString());
				}
			}
			Thread.sleep(1000);
		}
	}
	
	public static void WaitForTextNotPresentIn(By Ele, String Text) throws Exception{
		for(int i = 0; i < DriverFactory.WaitTimeOut + 1; i++) {
			if (!DriverFactory.getInstance().getDriver().findElement(Ele).getAttribute("value").contains(Text)) {
				break;
			}else if (!DriverFactory.getInstance().getDriver().findElement(Ele).getText().contains(Text)) {
				break;
			}else if (i == DriverFactory.WaitTimeOut) {
				throw new Exception ("Text does not match.  " + Ele.toString());
			}
			Thread.sleep(1000);
		}
	}

    public static void WaitClickable(By Ele) throws Exception{
    	DriverFactory.getInstance().getDriverWait().until(ExpectedConditions.elementToBeClickable(Ele));
	}
	
	public static String GetCookieValue(String Name){
		Set<Cookie> cookies = DriverFactory.getInstance().getDriver().manage().getCookies();
        Iterator<Cookie> itr = cookies.iterator();
        while (itr.hasNext()) {
            Cookie cookie = itr.next();
            if (cookie.getName().contentEquals(Name)){
            	Helper_Functions.PrintOut(Name + " value is " + cookie.getValue(), true);
            	return cookie.getValue();
            }
           //System.out.println("Name: " + cookie.getName() + "\n Path: " + cookie.getPath()+ "\n  Domain:  " + cookie.getDomain() + "\n   Value:  " + cookie.getValue()+ "\n    Expiry:  " + cookie.getExpiry());
        }
		return null;
	}
	
	public static String GetCurrentURL() {
		return DriverFactory.getInstance().getDriver().getCurrentUrl();
	}

	//takes in the element expected and requirement and checks if true
	//if error will print message
	public static boolean ElementMatches(By bypath, String expected, int requirement){
		String CurrentText = "<<not present>>";
		try {
			CurrentText = DriverFactory.getInstance().getDriver().findElement(bypath).getText();
			if (CurrentText == "<<not present>>" || CurrentText == "") {
				CurrentText = DriverFactory.getInstance().getDriver().findElement(bypath).getAttribute("value");
			}
			Helper_Functions.PrintOut("Verified Text: " + expected.replaceAll("\n", "\\ n") + "     " + bypath, true); //will replace all new line characters for sake of formatting.
			return true;
		}catch (Exception e){
			Helper_Functions.PrintOut("FAILURE: _" + expected + "_ is not present. ID " + requirement + ". Current _" + CurrentText + "_", true);
			Helper_Functions.PrintOut("Differnce starts at: " + StringUtils.difference(expected, CurrentText), true);
		} 
		return false;
	}
	
	public static boolean ElementMatchesSelect(By bypath, String expected, int requirement){
		String CurrentText = "<<not present>>";
		try {
			Select select = new Select(DriverFactory.getInstance().getDriver().findElement(bypath));
			WebElement option = select.getFirstSelectedOption();
			CurrentText = option.getText();
			
			if (CurrentText != expected) {
				throw new Exception ("does not match");
			}
			Helper_Functions.PrintOut("Verified Text: " + DriverFactory.getInstance().getDriver().findElement(bypath).getText() + "     " + bypath, true);
			return true;
		}catch (Exception e){
			Helper_Functions.PrintOut("FAILURE: _" + expected + "_ is not present. ID " + requirement + ". Current _" + CurrentText + "_", true);
			Helper_Functions.PrintOut("Differnce starts at: " + StringUtils.difference(expected, CurrentText), true);
		} 
		return false;
	}
	
	public static String GetValue(By Ele) {
		return DriverFactory.getInstance().getDriver().findElement(Ele).getAttribute("value");
	}
	
	public static String GetText(By Ele) {
		return DriverFactory.getInstance().getDriver().findElement(Ele).getText();
	}

    public static boolean Login(String Level, String UserName, String Password) throws Exception {
    	if(UserName == null || UserName == ""){
    		Helper_Functions.PrintOut("Cannot login with user id as null. Recieved from " + Thread.currentThread().getStackTrace()[2].getMethodName(), true);
    		throw new Exception("User not working.");
    	}
    	ChangeURL("HOME", "US", true, Level);
    	
    	try{
			//try to login from the WDPA page
    		ChangeURL("WDPA", "US", false, Level);
			Type(By.name("username"), UserName);
			Type(By.name("password"), Password);
			Click(By.name("login"));
			if (GetCookieValue("fcl_uuid") == null) {
    			throw new Exception("Did not login through WDPA");
    		}
			Helper_Functions.PrintOut("Login:" + UserName + "/" + Password, true);
	    	return true;
		}catch(Exception e3){
			Helper_Functions.PrintOut("Not able to login through WDPA", true);
		}
    	
    	try{
    		//this will navigate to the home page
    		ChangeURL("HOME", "US", false, Level);
    		Click(By.cssSelector("span.fxg-user-options__sign-in-text"));
            //wait for text box for user id to appear
    		Type(By.id("NavLoginUserId"), UserName);
    		Type(By.id("NavLoginPassword"), Password);
    		Thread.sleep(1000);
    		Click(By.cssSelector("#HeaderLogin > button.fxg-button.fxg-button--orange"));
    		Thread.sleep(5000);
            if (GetCookieValue("fcl_uuid") == null) {
             	throw new Exception("Did not login through global header");
            }
            Helper_Functions.PrintOut("Login:" + UserName + "/" + Password, true);
        	return true;
    	}catch(Exception e){
    		Helper_Functions.PrintOut("Not able to login through US home page", true);
    	}//end try catch for logging into home page
    	
    	try{
			//try to login from the INET page.
    		ChangeURL("INET", "US", false, Level);
    		if (DriverFactory.getInstance().getDriver().getPageSource().contains("Error 404") || DriverFactory.getInstance().getDriver().getPageSource().contains("unable to process your request at this time.")){
    			throw new Exception();
    		}
    		
    		if (isPresent(By.name("username"))){
    			Type(By.name("username"), UserName);
    			Type(By.name("password"), Password);
    			Click(By.name("login"));
    			if (GetCookieValue("fcl_uuid") == null) {
    				throw new Exception("Did not login through INET");
    			}
    		}
    		Helper_Functions.PrintOut("Login:" + UserName + "/" + Password, true);
        	return true;
		}catch(Exception e2){
			Helper_Functions.PrintOut("Not able to login through INET", true);
			throw new Exception("User not working.");
		}//end try catch for logging into INET page
      }//end Login    
  	
  	public static String LevelUrlReturn(int Level) {
  		String LevelURL = null;
  		switch (Level) {
      		case 1:
      			LevelURL = "https://wwwbase.idev.fedex.com"; break;
      		case 2:
      			LevelURL = "https://wwwdev.idev.fedex.com";  break;
      		case 3:
      			LevelURL = "https://wwwdrt.idev.fedex.com"; break;
      		case 4:
      			LevelURL = "https://wwwstress.dmz.idev.fedex.com"; break;
      		case 5:
      			LevelURL = "https://wwwbit.idev.fedex.com"; break;
      		case 6:
      			LevelURL = "https://wwwtest.fedex.com"; break;
      		case 7:
      			LevelURL = "https://www.fedex.com"; break;
  		}
  		return LevelURL;
  	}
}

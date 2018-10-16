package TestingFunctions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import Data_Structures.User_Data;
import SupportClasses.DriverFactory;
import SupportClasses.ThreadLogger;
import jxl.Sheet;
import jxl.Workbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

public class Helper_Functions extends WebDriver_Functions{
	public static String MyEmail = "sean.kauffman.osv@fedex.com";
	//public static String MyEmail = "accept@fedex.com";
	public static String strPassword = "Test1234";
	public static String Passed = "Passed", Failed = "Fail", Skipped = "Skipped";
	
	//a list of the Userids
	public static User_Data DataClass[][]= new User_Data[8][];
	
	public static ArrayList<String[]> ContactList = new ArrayList<String[]>(), CreditCardList = new ArrayList<String[]>(), EnrollmentList = new ArrayList<String[]>(), TaxInfoList = new ArrayList<String[]>();

    public static void Cleanup(String Levels, ArrayList<String[]> ResultsList) {
    	
		ResultsList.set(0, new String[] {ResultsList.get(0)[0], CurrentDateTime(true), "", ""});//This will save the end time the starting time in the first element of the list
		String Results = "";
		
		try {
			PrintOutTable(ResultsList);
		}catch (Exception e) {
			PrintOut("\n\n", false);
			for (String[] arr : ResultsList) {
				PrintOut(Arrays.toString(arr), false);
				Results += "\n" + Arrays.toString(arr);
			}
			PrintOut("\n\n", false);
		}
		
		ArrayList<String> CurrentLogs = ThreadLogger.getInstance().ReturnLogs();
		String TablePrinted = "";
	    for (int i = 0; i < CurrentLogs.size(); i++){
	    	TablePrinted += CurrentLogs.get(i) + System.lineSeparator();
		}
		ThreadLogger.ThreadLog.add(TablePrinted + System.lineSeparator());
    	
		PrintOut("\n\n", false);
		
		for (int i = 0 ; i < ThreadLogger.ThreadLog.size(); i++) {
			String Line = ThreadLogger.ThreadLog.get(i);
			PrintOut(i + ") " + Line + "\n", false);
			Results += "\n" + Line;
		}
		Results += "\n\n";
		
		try {
			MoveOldScreenshots();//cleans up the screenshots.
			Runtime.getRuntime().exec("taskkill /F /IM ChromeDriver.exe");//close out the old processes if still present.
			PrintOut("ChromeDriver.exe Cleanup Executed", true);
		} catch (Exception e) {}

		String CurrentTime =  CurrentDateTime();
		String Caller = getCallerClassName();
		Caller = getCallerClassName().replace("TestNG.", "").replace("_TestNG", "");
		String FileName = Caller + File.separator + CurrentTime + " L" + Levels + " " + Caller;
		//File newTextFile = new File("." + File.separator + "EclipseScreenshots" + File.separator + FileName + ".txt");
		String FilePath = "./EclipseScreenshots/" + FileName;
		
		WriteToFile(Levels, Results, FilePath);
 
		MoveOldLogs();
		//need to close out the drivers
    }
    
	public static String getCallerClassName() { 
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Helper_Functions.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
                return ste.getClassName();
            }
        }
        return null;
     }
	
	public static void WriteToFile(String Levels, String Text, String Path){	
		//this is only applicable if configured for tests that print the summery table.
		if (Text.contains("Passed: ") && Text.contains(" - Failed: ")) {
			String Results = Text.substring(Text.indexOf("Passed: "), Text.length());
			if (Results.length() < 28 && Results.contains(" - Failed: ")) {//check in case pulled to many characters
				Results = Results.replace("Passed: ", " - P");
				Results = Results.replace(" - Failed: ", "F");
				Results = Results.replaceAll("\r", "");
				Results = Results.replaceAll("\n", "");
				Path = Path.replace(".txt", "");
				Path += Results;
			}
		}
		if (!Path.contains(".txt")) {
			Path+= ".txt";
		}
		File newTextFile = new File(Path);
		FileWriter fw;
		try {
			String Folder = Path.substring(0, Path.lastIndexOf("\\"));
			if (!(new File(Folder)).exists()) {
				new File(Folder).mkdir();
			}
			newTextFile.createNewFile();
			fw = new FileWriter(newTextFile);
			fw.write(Text);
	        fw.close();
		} catch (Exception e) {  
			PrintOut("Failure writing to file.", true);
			e.printStackTrace();
		}
	}
	
	public static void MoveOldLogs(){
    	String main_Source = "." + File.separator + "EclipseScreenshots";
    	File main_dir = new File(main_Source);
    	if(main_dir.isDirectory()) {
    	    File[] content_main = main_dir.listFiles();
    	    for(int j = 0; j < content_main.length; j++) {
    	    	try {
    	        	String PathSource = content_main[j].getPath();
    	        	String PathDestination = content_main[j].getPath() + File.separator + "Old";
    	        	File source_dir = new File(PathSource);
    	        	File destination_dir = new File(PathDestination);
    	        	int year = Calendar.getInstance().get(Calendar.YEAR);
    	        	int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
    	        	int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    	        	if(source_dir.isDirectory()) {
    	        	    File[] content_subfolder = source_dir.listFiles();
    	        	    for(int i = 0; i < content_subfolder.length; i++) {
    	        	    	if (content_subfolder[i].isDirectory()){
    	        	    		break; //do not need to seach within subfolders.
    	        	    	}
    	        	    	
    	        	    	try {
    	        	        	if (!destination_dir.exists()) {// if the directory does not exist, create it
    	        	        	    PrintOut("Creating directory: " + destination_dir.getName(), true);
    	        	        	    try{
    	        	        	    	destination_dir.mkdir();
    	        	        	        PrintOut(PathDestination + " DIR created", true);  
    	        	        	    } catch(SecurityException se){}        
    	        	        	}
    	        	        	BasicFileAttributes attr = Files.readAttributes(Paths.get(content_subfolder[i].getPath()), BasicFileAttributes.class);
    	        	        	String creationtime = " " + attr.creationTime();
    	        	        	if (!creationtime.contains(Integer.toString(year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day))){//if the file was created before today
    	        	        		String localPathDestination = PathDestination + File.separator + creationtime.substring(1, 8);
    	        	        		File old_month_dir = new File(localPathDestination);
    	        	        		if (!old_month_dir.exists()) {// if the directory does not exist for the old month then create it
    	            	        	    PrintOut("Creating directory: " + old_month_dir.getName(), true);
    	            	        	    try{
    	            	        	    	old_month_dir.mkdir();
    	            	        	        PrintOut(localPathDestination + " DIR created", true);  
    	            	        	    } catch(SecurityException se){}
    	            	        	}
    	        	        		//Files.move(from, to, CopyOption... options).
    	        	        		Files.move(Paths.get(content_subfolder[i].getPath()), Paths.get(content_subfolder[i].getPath().replace(PathSource, localPathDestination)), StandardCopyOption.REPLACE_EXISTING);
    	        	        	}
    	    				}catch (Exception e) {}
    	        	    }
    	        	}
				}catch (Exception e) {}
    	    }//end for finding each individual app
    	}
    }//end MoveOldLogs
    
	public static void Wait(long Seconds) {
		try {
			Thread.sleep(Seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
 	//String[] {Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, countryCode - 6};
	public static String[] AccountDetails(String Level, String AccountNumber){
		String Streetline1 = "", Streetline2 = "", City ="", State ="", StateCode = "", postalCode = "", countryCode = "";
		String TempLevel = "L" + Level;
		if (!"L1L2L3L4".contains(TempLevel)){
			PrintOut("Invalid Level to find account number detials, checking account vs L3", true);
			TempLevel = "L3";
		}
 		try {
 			PrintOut("Account number " + AccountNumber + " recieved from " + Thread.currentThread().getStackTrace()[2].getMethodName(), true);
 			ChangeURL("JSP", "", false, "");
 			Type(By.name("contactAccountNumber"), AccountNumber);
 			Type(By.name("contactAccountOpCo"), "FX");

 		    //selects the correct radio button for the level, only works for 1,2,3,4
 			Click(By.xpath("//input[(@name='contactLevel') and (@value = '" + TempLevel + "')]"));
			Click(By.name("contactAccountSubmit"));
			String SourceText = DriverFactory.getInstance().getDriver().getPageSource();
			
			int intStartingPoint;
			//PrintOut(SourceText.replaceAll("\n", ""));
			String StartingPoint = "&lt;streetLine&gt;";
			if (SourceText.indexOf(StartingPoint) < 0){
				StartingPoint = "&lt;customer:streetLine&gt;";
			}
			for (int i=0;i<3;i++){ //instead of three if you set to 2 the shipping address will be returned.
				SourceText = SourceText.substring(SourceText.indexOf(StartingPoint) + StartingPoint.length(), SourceText.length());
				//PrintOut(SourceText.replaceAll("\n", ""));
			}
			String start = "name=\"";
			String end  = "\" value=\"";
			Streetline1 = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			
			StartingPoint = "additionalLine1&gt;";//save if the account number has an address line1 value
			if(SourceText.indexOf(StartingPoint) > 0){
				intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
				SourceText = SourceText.substring(intStartingPoint, SourceText.length() - intStartingPoint);
				Streetline2 = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			}
			
			StartingPoint = "geoPoliticalSubdivision2&gt";
			intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
			SourceText = SourceText.substring(intStartingPoint, SourceText.length() - intStartingPoint);
			if (intStartingPoint > StartingPoint.length()){
				City = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			}
			
			StartingPoint = "geoPoliticalSubdivision3&gt";			
			intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
			SourceText = SourceText.substring(intStartingPoint, SourceText.length() - intStartingPoint);
			if (intStartingPoint > StartingPoint.length()){
				StateCode = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			}
			
			StartingPoint = "postalCode&gt";
			intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
			SourceText = SourceText.substring(intStartingPoint, SourceText.length() - intStartingPoint);
			if (intStartingPoint > StartingPoint.length()){
				postalCode = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			}
			
			
			StartingPoint = "countryCode&gt";
			intStartingPoint = SourceText.indexOf(StartingPoint) + StartingPoint.length();
			SourceText = SourceText.substring(intStartingPoint, SourceText.length() - intStartingPoint);
			countryCode = SourceText.substring(SourceText.indexOf(start) + start.length(), SourceText.indexOf(end));
			
			if (postalCode.length() > 5 && countryCode.contentEquals("US")){
				postalCode = postalCode.substring(0, 5);
			}
			
			//remove any special characters
			String AccountDetails[] = {Streetline1, Streetline2, City, State, StateCode, postalCode, countryCode};
			for (int i = 0; i < AccountDetails.length; i++){
				String nfdNormalizedString = Normalizer.normalize(AccountDetails[i], Normalizer.Form.NFD); 
				Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
				AccountDetails[i] = pattern.matcher(nfdNormalizedString).replaceAll("");
			}
			if (countryCode.length() > 5 || countryCode.length() == 0){return null;}
			PrintOut("AccountDetails: " + Arrays.toString(AccountDetails), true);
			return AccountDetails;
 		}catch (Exception e){
 			PrintOut("Not able to fully retrieve address: " + Streetline1 + " " + Streetline2+ " " + City+ " " + State+ " " + StateCode+ " " +postalCode+ " " + countryCode, true);
			return LoadAddress("US", Level);
 		}
 	}//end AccountDetails
    
	public static String CreateAccountNumbers(String Level, String AccountDetails[], String AddressDetails[]) throws Exception{ 
		PrintOut("Attempting to create account number for " + Arrays.toString(AddressDetails), true);
		try {
			// AccountDetails Example = 
			//ShippingCountryCode, BillingCountryCode, OperatingCompanies (E = Express, G = Ground, F = Freight so "EDF" is all three), NumberOfAccounts
			// AddressDetails Example =  {"10 FEDEX PKWY 2nd FL", "", "COLLIERVILLE", "Tennessee", "TN", "38017", "US"});
			//Address line 1, address line 2, City, StateName, StateCode, ZipCode, CountryCode

			ChangeURL("ECAM", "", false, Level);
			if (isPresent(By.id("username"))) {
				Type(By.id("username"), "");//update with your user id
				Type(By.id("password"), "");//update with your user id
				Click(By.id("submit"));
			}

			//ECAM Page
			for(;;) {
				try {
					WaitPresent(By.id("new_act_info"));
		 			WaitClickable(By.id("new_act_info"));
					Click(By.id("new_act_info"));
					break;
				}catch (Exception e) {}
			}

			WaitPresent(By.id("acct_info_ship_countrylist"));
			String ShippingCountrCode = AccountDetails[0].toUpperCase(),  BillingCountryCode = AccountDetails[1].toUpperCase();
			Select(By.id("acct_info_ship_countrylist"), ShippingCountrCode, "v");//shipping country
			Select(By.id("acct_info_countrylist"), BillingCountryCode, "v");//billing country
			Select(By.id("acctinfo_customertype") , "BUSINESS","v");//Customer Type
			
			//This next section will populate based on the country and the customer type
			if (AccountDetails[2].contains("E")) {
				
				Click(By.id("check_exp"));
				Select(By.id("acct_type_exp") , "BUSINESS","v");//Express Type
			}else if (AccountDetails[2].contains("G")) {
				Click(By.id("check_gnd"));
				Select(By.id("acct_type_exp") , "BUSINESS","v");//Express Type
			}else if (AccountDetails[2].contains("F")) {
				Click(By.id("check_fht"));
				Select(By.id("acct_type_fht") , "SHIPPER","v");//Freight Type
				Select(By.id("acct_sub_type_fht") , "DOCK","v");//Freight Sub Type
			}
			
			String NumAccounts = AccountDetails[3];
			Type(By.id("acctinfo_no_acct"), NumAccounts); //number of account numbers that should be created
			Select(By.id("acct_info_source_grp"), "ALLIANCES","v");//the Source group of the account numbers
			Click(By.id("next_contact"));
			
			//Account Contact Information
			Type(By.id("first_name"), "John");
			Type(By.id("last_name"), "Doe");
			try {
				Select(By.id("contact_language") , "EN","v");//set the language as English
			}catch (Exception e){}
			
			Type(By.id("contact_phn_one"), "9011111111"); //Will need to update later for additional countries
			Click(By.name("ship_radio"));
			Click(By.id("next_address"));
			
			//Account Address
			Type(By.id("acctinfo_postal_input_info"), AddressDetails[5]);
			Type(By.id("add_info_company"), CurrentDateTime() + AccountDetails[2]);
			Type(By.id("address_phn_number"), "9011111111");
			Type(By.id("acctinfo_addr_one"), AddressDetails[0]);
			Type(By.id("acctinfo_addr_two"), AddressDetails[1]);
			try {//try and select the city, may be only the single city or multiple based on zip code.
				DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(DriverFactory.WaitTimeOut / 3, TimeUnit.SECONDS); //sets the timeout for short to make reduce delay
				new Select(DriverFactory.getInstance().getDriver().findElement(By.id("acctinfo_city_input_info_list"))).selectByValue(AddressDetails[2].toUpperCase());
			}catch (Exception e) {
				Type(By.id("acctinfo_city_input_info_box"), AddressDetails[2]);
			}finally {
				DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(DriverFactory.WaitTimeOut, TimeUnit.SECONDS);
			}
			
			Click(By.id("next_payment"));
			
			try {//if there address matches differs address may need to skip the below.
				if (isPresent(By.id("nomatch"))) {
					Click(By.id("nomatch"));
				}
				if (isPresent(By.id("next_reg"))) {//Regulatory Informaiton page
					Click(By.id("next_reg"));
				}
			}catch (Exception e){}
			
			try {
				//Regulatory Informaiton page
				Click(By.id("next_reg"));
			}catch (Exception e){}
			
			//Payment Information
			String CreditCard[] = LoadCreditCard("V");
			String Payment = "Invoice";
			try {
				Select(By.id("acct_pay_info_list") , "Credit_Card","v");
				
				//Example  "Visa", "4005554444444460", "460", "12", "20"
				Type(By.id("acct_payment_info_number_details"), CreditCard[1]);
				Type(By.id("name_on_card"), "John Doe");
				Type(By.id("acct_pay_expiry"), CreditCard[3] + "/20" + CreditCard[4]);
				Type(By.id("acct_pay_cvv_code"), CreditCard[2]);
				Select(By.id("acct_payment_info_card_type") , CreditCard[0].toUpperCase(),"v");   //Auto populated
				Click(By.id("next_comments"));
				if (isPresent(By.id("next_comments"))) {
					throw new Exception("Could not link to CC");
				}
				Payment = CreditCard[1];
			}catch (Exception e){
				for(int i = 0; i < 30; i++) {
					try {
						Select(By.id("acct_pay_info_list") , "Invoice","v");	
						Click(By.id("next_comments"));
						break;
					}catch (Exception e1){}
				}
			}
			
			//Comment/confirmation page
			Click(By.id("comments_form_save"));	
			//*[@id="dialog-confirm"]/center/text()
			WaitPresent(By.id("dialog-confirm"));
			WaitForTextPresentIn(By.id("dialog-confirm"), "Account has been created");
	        String AccountNumbers = DriverFactory.getInstance().getDriver().findElement(By.id("dialog-confirm")).getText();
	        PrintOut("AccountNumberstest:   " + AccountNumbers + "     -- " + Payment, false);
			AccountNumbers = AccountNumbers.replace("Account has been created successfully and Account Numbers are ", "");
			return AccountNumbers;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    
	public static String CurrentDateTime() {
		Date curDate = new Date();
    	SimpleDateFormat Dateformatter = new SimpleDateFormat("MMddyy");
    	SimpleDateFormat Timeformatter = new SimpleDateFormat("HHmmss");
    	return Dateformatter.format(curDate) + "T" + Timeformatter.format(curDate);
	}
	
	public static String CurrentDateTime(boolean t) {
		Date curDate = new Date();
    	SimpleDateFormat DateTime= new SimpleDateFormat("MM-dd-yy HH:mm:ss:SS");
    	return DateTime.format(curDate);
	}
	
	public static String ValidPhoneNumber(String CountryCode){
		switch (CountryCode) {
    		case "SG":  
    			return "9011111";  //need to check to make sure valid
    		default:
    			return "9011111111";
		}//end switch CountryCode
	}
	
	public static void PrintOut(String Text, boolean TimeStamp){
		long ThreadID = Thread.currentThread().getId();
		
		if (TimeStamp) {
			Text = CurrentDateTime() + ": " + Text;
			System.out.println(ThreadID + " " + Text); 
		}else {
			System.out.println(Text);
		}
		
		Text = Text.replaceAll("\n", System.lineSeparator());
		ThreadLogger.getInstance().UpdateLogs(Text);//Store the all values that are printed for a given thread.
	}
	
    public static void MoveOldScreenshots(){
    	String main_Source = "." + File.separator + "EclipseScreenshots";
    	File main_dir = new File(main_Source);
    	if(main_dir.isDirectory()) {
    	    File[] content_main = main_dir.listFiles();
    	    for(int j = 0; j < content_main.length; j++) {
    	    	try {
    	        	String PathSource = content_main[j].getPath();
    	        	String PathDestination = content_main[j].getPath() + File.separator + "Old";
    	        	File source_dir = new File(PathSource);
    	        	File destination_dir = new File(PathDestination);
    	        	int year = Calendar.getInstance().get(Calendar.YEAR);
    	        	int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
    	        	int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    	        	if(source_dir.isDirectory()) {
    	        	    File[] content_subfolder = source_dir.listFiles();
    	        	    for(int i = 0; i < content_subfolder.length; i++) {
    	        	    	if (content_subfolder[i].isDirectory()){
    	        	    		break; //do not need to seach within subfolders.
    	        	    	}
    	        	    	
    	        	    	try {
    	        	        	if (!destination_dir.exists()) {// if the directory does not exist, create it
    	        	        	    PrintOut("Creating directory: " + destination_dir.getName(), true);
    	        	        	    try{
    	        	        	    	destination_dir.mkdir();
    	        	        	        PrintOut(PathDestination + " DIR created", true);  
    	        	        	    } catch(SecurityException se){}        
    	        	        	}
    	        	        	BasicFileAttributes attr = Files.readAttributes(Paths.get(content_subfolder[i].getPath()), BasicFileAttributes.class);
    	        	        	String creationtime = " " + attr.creationTime();
    	        	        	if (!creationtime.contains(Integer.toString(year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day))){//if the file was created before today
    	        	        		String localPathDestination = PathDestination + File.separator + creationtime.substring(1, 8);
    	        	        		File old_month_dir = new File(localPathDestination);
    	        	        		if (!old_month_dir.exists()) {// if the directory does not exist for the old month then create it
    	            	        	    PrintOut("Creating directory: " + old_month_dir.getName(), true);
    	            	        	    try{
    	            	        	    	old_month_dir.mkdir();
    	            	        	        PrintOut(localPathDestination + " DIR created", true);  
    	            	        	    } catch(SecurityException se){}
    	            	        	}
    	        	        		//Files.move(from, to, CopyOption... options).
    	        	        		Files.move(Paths.get(content_subfolder[i].getPath()), Paths.get(content_subfolder[i].getPath().replace(PathSource, localPathDestination)), StandardCopyOption.REPLACE_EXISTING);
    	        	        	}
    	    				}catch (Exception e) {}
    	        	    }
    	        	}
				}catch (Exception e) {}
    	    }//end for finding each individual app
    	}
    }//end MoveOldScreenshots
    
	public static void WriteUserToExcel(String Level, String UserID, String Password) {
		if (DataClass[Integer.valueOf(Level)] == null) {
			LoadUserIds(Integer.valueOf(Level));
		}
		int Row = DataClass[Integer.valueOf(Level)].length;
		writeExcelData(".\\Data\\TestingData.xls", "L" + Level, UserID, Row, 1);
		writeExcelData(".\\Data\\TestingData.xls", "L" + Level, Password, Row, 2);
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
			PrintOut("Verified Text: " + expected.replaceAll("\n", "\\ n") + "     " + bypath, true); //will replace all new line characters for sake of formatting.
			return true;
		}catch (Exception e){
			PrintOut("FAILURE: _" + expected + "_ is not present. ID " + requirement + ". Current _" + CurrentText + "_", true);
			PrintOut("Differnce starts at: " + StringUtils.difference(expected, CurrentText), true);
		} 
		return false;
	}

	public static String[] LoadCreditCard(String CardDetails){
		return LoadCreditCard(CardDetails, "");
	}
	//{Card Type - 0, Card Number - 1, CVV - 2, Expiration Month - 3, Expiration year - 4}
	public static String[] LoadCreditCard(String CardDetails, String Level){
		//Note, this is not designed to run in prod and test levels at the same time.
		CardDetails = CardDetails.toUpperCase();
		PrintOut("LoadCreditCard Received: " + CardDetails, true);
		String CreditCard[] = null;

		if (CardDetails.contains("I")) {//if requesting invalid credit card
			CreditCard = new String[] {"Visa", "400555444444416", "111", "12", "20"};
			PrintOut("LoadCreditCard is Returning Invalid Card: " + Arrays.toString(CreditCard), true);
			return CreditCard;
		}

		if (CreditCardList.isEmpty() && Level.contentEquals("7")) {
			//update with prod cc
			//CreditCardList.add(new String[] );
		}else if (CreditCardList.isEmpty()) {
			CreditCardList.add(new String[] {"MasterCard", "5204730000001003", "003", "12", "20"});
			CreditCardList.add(new String[] {"American Express", "378598529621002", "2234", "12", "20"});
			CreditCardList.add(new String[] {"American Express", "371307196801005", "5222", "12", "20"});
			CreditCardList.add(new String[] {"Visa", "4005554444444460", "460", "12", "20"});
			CreditCardList.add(new String[] {"Visa", "4111111111111111", "111", "12", "20"});
			CreditCardList.add(new String[] {"Visa", "4929450317991005", "005", "12", "20"});
			CreditCardList.add(new String[] {"Visa", "4000000000000002", "002", "12", "20"});
			CreditCardList.add(new String[] {"Visa", "4596777727893450", "450", "12", "20"});
			CreditCardList.add(new String[] {"Visa", "4005554444440013", "013", "12", "20"});
			CreditCardList.add(new String[] {"Visa", "4895400000000002", "002", "12", "20"});
			CreditCardList.add(new String[] {"Visa", "4005554444444163", "163", "12", "18"});
		}

		for (int i = 0; i < CreditCardList.size(); i++) {
			CreditCard = CreditCardList.get(i);
			if (CardDetails.length() == 1 && CreditCard[0].contains(CardDetails)) {
				PrintOut("LoadCreditCard is Returning: " + Arrays.toString(CreditCard), true);
				return CreditCard;
			}else if (CardDetails.length() <= 16 && CardDetails.length() >= 15 && CreditCard[1].contains(CardDetails)){ //if a credit card number was sent
				try{
					CreditCard = CreditCardList.get(i + 1);//return the next card id possible
				}catch(Exception e) {
					CreditCard = CreditCardList.get(i - 1);//else return the previous card
				}
				PrintOut("LoadCreditCard is Returning: " + Arrays.toString(CreditCard), true);
				return CreditCard;
			}
		}
		
		PrintOut("Unable to find desired card. LoadCreditCard is Returning: " + Arrays.toString(CreditCardList.get(0)), true);
		return CreditCardList.get(0);
	}//end LoadCreditCard
	
	public static String[] LoadAddress(String CountryCode, String Level){
		return LoadAddress(CountryCode, "", "", Level);
	}
	//{Streetline1 - 0, Streetline2 - 1, City - 2, State - 3, StateCode - 4, postalCode - 5, CountryCode - 6, ShareID - 7};
	public static String[] LoadAddress(String CountryCode, String CodeState, String Address1, String Level){
		CountryCode = CountryCode.toUpperCase();
		CodeState = CodeState.toUpperCase();
		PrintOut("LoadAddress Received: _" + CountryCode + "_ _" + CodeState + "_", true);
		String ReturnAddress[] = null;
		if (Level.contentEquals("7") && CountryCode.contentEquals("US")){
			ReturnAddress = new String[] {"10 FEDEX PKWY 2nd FL", "", "COLLIERVILLE", "Tennessee", "TN", "38017", "US"};
			PrintOut("Since testing production sending back 10 FEDEX PKWY 2nd FL address.   " + Arrays.toString(ReturnAddress), true);
			return ReturnAddress;
		}
		if (ContactList.isEmpty()) {
			ContactList = getExcelData(".\\Data\\AddressDetails.xls", "Countries"); 
		}
		
		for(int i = 0; i< ContactList.size(); i++) {
			if (ContactList.get(i)[6].contentEquals(CountryCode)) {
				if (CodeState == "") {
					ReturnAddress = ContactList.get(i);
					break;
				}else if (CodeState != "" && ContactList.get(i)[4].contentEquals(CodeState)) {
					if (Address1 == "") {
						ReturnAddress = ContactList.get(i);
						break;
					}else if (Address1 != "" && ContactList.get(i)[0].contentEquals(Address1)) {
						ReturnAddress = ContactList.get(i);
						break;
					}
				}
			}
		}
		
		if (ReturnAddress != null) {
			PrintOut("LoadAddress is Returning: " + Arrays.toString(ReturnAddress), true);
			return ReturnAddress;
		}
		
		PrintOut("Unable to return requested address.", true);
		throw new Error("Address not loaded");
	}//end LoadAddress
	
	public static String[] LoadEnrollmentIDs(String CountryCode){
		PrintOut("LoadEnrollmentIDs Received: _" + CountryCode, true);
		if (EnrollmentList.isEmpty()) {
			EnrollmentList = getExcelData(".\\Data\\EnrollmentIds.xls", "EnrollmentIds"); 
		}
		
		String EnrollmentID[] = null;
		for(String Enrollment[] : EnrollmentList) {
			if (Enrollment[1].contentEquals(CountryCode)) {
				EnrollmentID = Enrollment;
				break;
			}
		}
		PrintOut("LoadEnrollmentIDs returning: _" + Arrays.toString(EnrollmentID), true);
		return EnrollmentID;//enrollment for the given country not found
	}
	
	public static String Check_Country_Region(String CountryCode) {
		for (String CountrCode[] : ContactList) {
			if (CountrCode[6].contentEquals(CountryCode.toUpperCase())) {
				return CountrCode[7];
			}
		}
		return "Country Not Found";
	}
	
	//{First Name - 0, Middle Name - 1, Last Name - 2}
	public static String[] LoadDummyName(String Base, String Level){
		final String[] numNames = {"", "one","two","three","four","five","six","seven"};
	    String DummyName[] = {"F" + numNames[Integer.valueOf(Level)] + Base + getRandomString(7), "M", "L" + getRandomString(7)};
		PrintOut("Generate Name is Returning: " + Arrays.toString(DummyName), true);
		return DummyName;
	}
	
	
	public static ArrayList<String[]> LoadTaxInfo(String CountryCode){
		if (TaxInfoList.isEmpty()) {
			TaxInfoList = getExcelData(".\\Data\\VatNumbers.xls", "TaxIds"); 
		}
		
		ArrayList<String[]> countrySpecificTaxInfo = new ArrayList<String[]>();
		for(String TaxInfo[] : TaxInfoList) {
			if (TaxInfo[0] != null && TaxInfo[0].contentEquals(CountryCode)) {
				countrySpecificTaxInfo.add(TaxInfo);
			}
		}
		return countrySpecificTaxInfo;
	}
	
	public static void LoadUserIds(int intLevel) {
		List<String[]> FullDataFromExcel = new ArrayList<String[]>();
		FullDataFromExcel = getExcelData(".\\Data\\TestingData.xls", "L" + intLevel);
		DataClass[intLevel] = new User_Data[FullDataFromExcel.size()];
		int filled = 0;
		for (int j = 0; j < FullDataFromExcel.size(); j++) {
			try {
				DataClass[intLevel][filled] = new User_Data(); 
				DataClass[intLevel][filled].UUID_NBR = FullDataFromExcel.get(j)[0];
				DataClass[intLevel][filled].SSO_LOGIN_DESC = FullDataFromExcel.get(j)[1];
				DataClass[intLevel][filled].USER_PASSWORD_DESC = FullDataFromExcel.get(j)[2];
				DataClass[intLevel][filled].SECRET_QUESTION_DESC = FullDataFromExcel.get(j)[3];
				DataClass[intLevel][filled].SECRET_ANSWER_DESC = FullDataFromExcel.get(j)[4];
				DataClass[intLevel][filled].FIRST_NM = FullDataFromExcel.get(j)[5];
				DataClass[intLevel][filled].LAST_NM = FullDataFromExcel.get(j)[6];
				DataClass[intLevel][filled].STREET_DESC = FullDataFromExcel.get(j)[7];
				DataClass[intLevel][filled].CITY_NM = FullDataFromExcel.get(j)[8];
				DataClass[intLevel][filled].STATE_CD = FullDataFromExcel.get(j)[9];
				DataClass[intLevel][filled].POSTAL_CD = FullDataFromExcel.get(j)[10];
				DataClass[intLevel][filled].COUNTRY_CD = FullDataFromExcel.get(j)[11];
				DataClass[intLevel][filled].EMAIL_ADDR_DESC = FullDataFromExcel.get(j)[12];
				DataClass[intLevel][filled].PHONE_NBR = FullDataFromExcel.get(j)[13];
				DataClass[intLevel][filled].STREET2_DESC = FullDataFromExcel.get(j)[14];
				DataClass[intLevel][filled].INITIALS_NM = FullDataFromExcel.get(j)[15];
				DataClass[intLevel][filled].FAX_NBR = FullDataFromExcel.get(j)[16];
				DataClass[intLevel][filled].ACCOUNT_NBR = FullDataFromExcel.get(j)[17];
				DataClass[intLevel][filled].ACCOUNT_RELATION_DESC = FullDataFromExcel.get(j)[18];
				DataClass[intLevel][filled].MODIFY_DT = FullDataFromExcel.get(j)[19];
				DataClass[intLevel][filled].COMPANY_NM = FullDataFromExcel.get(j)[20];
				DataClass[intLevel][filled].EMAIL_ALLOWED_FLG = FullDataFromExcel.get(j)[21];
				DataClass[intLevel][filled].LAST_LOGIN_DT = FullDataFromExcel.get(j)[22];
				DataClass[intLevel][filled].LANGUAGE_CD = FullDataFromExcel.get(j)[23];
				DataClass[intLevel][filled].DEACTIVATE_CD = FullDataFromExcel.get(j)[24];
				DataClass[intLevel][filled].MARKETING_ANSWER_DESC = FullDataFromExcel.get(j)[25];
				DataClass[intLevel][filled].REGISTRATION_DT = FullDataFromExcel.get(j)[26];
				DataClass[intLevel][filled].T_C_DATE = FullDataFromExcel.get(j)[27];
				DataClass[intLevel][filled].DELETE_DT = FullDataFromExcel.get(j)[28];
				DataClass[intLevel][filled].INVALID_LOGIN_DT = FullDataFromExcel.get(j)[29];
				filled++;
			}catch (Exception e) {
				PrintOut("Warning, unable to load user data for line " + j + " ", false);
			}
		}
	}
	
	public static String getRandomString(int Length) {
        //String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		String SALTCHARS = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < Length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
	
	public static String LoadUserID(String Base){
		return Base + CurrentDateTime();
	}
	
	//returns the transaction id from a string
	public static String ParseTransactionId(String s) {
		if(s.contains("transactionId")) {
			String TransactionIdStart = "transactionId\":\"";
			int TransactionIdEnd;
			for (TransactionIdEnd = s.indexOf(TransactionIdStart) + TransactionIdStart.length(); TransactionIdEnd < s.length(); TransactionIdEnd++) {
				if (s.substring(TransactionIdEnd, TransactionIdEnd + 1).contentEquals("\"")) {
					break;
				}
			}
			return s.substring(s.indexOf(TransactionIdStart) + TransactionIdStart.length(), TransactionIdEnd);
		}
		return null;
	}
	
	//will take in the level and country code
	//check the listed excel fild for the given country and level and return account number if possible.
	public static String getExcelFreshAccount(String Level, String CountryCode, boolean SingleAccount){
		CountryCode = CountryCode.toUpperCase();
		ArrayList<String[]> data = getExcelData(".\\Data\\AddressDetails.xls", "Accounts");
		//Here ar ethe assumed headers of the excel file.
		//[Address_Line_1, Address_Line_2, City, State, State_Code, Zip, Country_Code, Region, Country, L1_Account, L2_Account, L3_Account, L4_Account, L5_Account, L6_Account, L7_Account]
		if (!data.get(0)[6].contentEquals("Country_Code") || !data.get(0)[8 + Integer.parseInt(Level)].contentEquals("L" + Level + "_Account")){
			PrintOut("WARNING, excel file does not match with expected columns", false);
		}
		String Account = null;
		for (String CountryArray[] : data){
			//if the correct line for the country and there are account numbers loaded.
			if (CountryArray[6].contentEquals(CountryCode) && !CountryArray[8 + Integer.parseInt(Level)].isEmpty()) {//position 9 in the L1 accounts
				Account = CountryArray[8 + Integer.parseInt(Level)];
				if (Account.contains(",") && SingleAccount) {
					Account = Account.substring(0, Account.indexOf(","));//multiple account numbers could be stored separated with a ","
					Account = Account.replaceAll(" ", "");
				}
				break;
			}
		}
		return Account;
	}
	
	public static ArrayList<String[]> getExcelData(String fileName, String sheetName) {
		//Note, may face issues if the file is an .xlsx, save it as a xls and works

		ArrayList<String[]> data = new ArrayList<>();
		try {
			
			
			/*
			FileInputStream fsIP= new FileInputStream(new File(fileName));                 
			HSSFWorkbook wb = new HSSFWorkbook(fsIP);
			HSSFSheet worksheet = wb.getSheetAt(0);
			for(int i = 1; i< wb.getNumberOfSheets() + 1;i++) {
				//PrintOut("CurrentSheet: " + worksheet.getSheetName(), false);  //for debugging if getting errors with sheet not found
				if (worksheet.getSheetName().contentEquals(sheetName)) {
					break;
				}
				worksheet = wb.getSheetAt(i);
			}
			DataFormatter dataFormatter = new DataFormatter();
			
			worksheet.forEach(row -> {
				ArrayList<String> buffer = new ArrayList<>();
	            row.forEach(cell -> {
	                String cellValue = dataFormatter.formatCellValue(cell);
	                buffer.add(cellValue);
	            });
	            String[] stringArray = buffer.toArray(new String[0]);
	            data.add(stringArray);
	        });
			//Close the InputStream  
			fsIP.close(); 
			//Open FileOutputStream to write updates
			FileOutputStream output_file =new FileOutputStream(new File(fileName));  
			//write changes
			wb.write(output_file);
			//close the stream
			output_file.close();
			wb.close();
			*/
			
			FileInputStream fs = new FileInputStream(fileName);
			Workbook wb = Workbook.getWorkbook(fs);
			Sheet sh = wb.getSheet(sheetName);
			
			int totalNoOfCols = sh.getColumns();
			int totalNoOfRows = sh.getRows();
			
			for (int i= 0 ; i < totalNoOfRows; i++) { //change to start at 1 if want to ignore the first row.
				String buffer[] = new String[totalNoOfCols];
				for (int j=0; j < totalNoOfCols; j++) {
					String CellContents = sh.getCell(j, i).getContents();
					if (CellContents == null) {
						CellContents = "";
					}
					buffer[j] = CellContents;
				}
				data.add(buffer);
			}
			wb.close();
			fs.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static boolean RemoveAccountFromExcel(String Level, String CountryCode, String AccountsOverwrite) {
		CountryCode = CountryCode.toLowerCase();
		ArrayList<String[]> data = getExcelData(".\\Data\\AddressDetails.xls", "Accounts");
		//Here ar ethe assumed headers of the excel file.
		//[Address_Line_1, Address_Line_2, City, State, State_Code, Zip, Country_Code, Region, Country, L1_Account, L2_Account, L3_Account, L4_Account, L5_Account, L6_Account, L7_Account]
		for (int i = 0; i < data.size(); i++){
			String CountryArray[] = data.get(i);
			//if the correct line for the country and there are account numbers loaded.
			if (CountryArray[8 + Integer.parseInt(Level)].contains(AccountsOverwrite)) {//position 9 in the L1 accounts
				return writeExcelData(".\\Data\\AddressDetails.xls", "Accounts", AccountsOverwrite, i, 8 + Integer.parseInt(Level));
			}
		}
		
		return false;
	}
	
	public static boolean writeExcelData(String fileName, String sheetName, String CellData, int RowtoWrite, int ColumntoWrite){
		try {
			//Read the spreadsheet that needs to be updated
			FileInputStream fsIP= new FileInputStream(new File(fileName));  
			//Access the workbook                  
			HSSFWorkbook wb = new HSSFWorkbook(fsIP);
			//Access the worksheet, so that we can update / modify it. 
			HSSFSheet worksheet = wb.getSheetAt(0);
			for(int i = 1; i< wb.getNumberOfSheets() + 1;i++) {
				//PrintOut("CurrentSheet: " + worksheet.getSheetName(), false);  //for debugging if getting errors with sheet not found
				if (worksheet.getSheetName().contentEquals(sheetName)) {
					break;
				}
				worksheet = wb.getSheetAt(i);
			}
			
			// declare a Cell object
			Cell cell = null; 
			// Access the second cell in second row to update the value
			if (worksheet.getRow(RowtoWrite) == null) {//if row not present create it
				worksheet.createRow(RowtoWrite);
			}
			if (worksheet.getRow(RowtoWrite).getCell(ColumntoWrite) == null) {//if cell not present create it
				worksheet.getRow(RowtoWrite).createCell(ColumntoWrite);
			}
			cell = worksheet.getRow(RowtoWrite).getCell(ColumntoWrite);
			
			// Get current cell value value and overwrite the value
			cell.setCellValue(CellData);
			//Close the InputStream  
			fsIP.close(); 
			//Open FileOutputStream to write updates
			FileOutputStream output_file =new FileOutputStream(new File(fileName));  
			//write changes
			wb.write(output_file);
			//close the stream
			output_file.close();
			wb.close();
		}catch (Exception e) {
			PrintOut("WARNING, Unable to write to Excel.", true);
			return false;
		}
		return true;
	}

	public static void PrintOutTable(ArrayList<String[]> List) {
		try {
			if (List != null) {
				int cells =  List.get(0).length;
				String[][] R = new String [List.size()][cells];
				for(int i = 0; i < List.size(); i++) {
					String []L =  List.get(i);
					for (int j = 0; j < cells; j++) {
						R[i][j] = L[j];
					}
				}
				PrintOutTable(R);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void PrintOutTable(String[][] Results) {
		try {
			//Save the time it took to complete
			DateFormat formatter = new SimpleDateFormat("MM-dd-yy HH:mm:ss:SS"); 
			Date startdate = (Date)formatter.parse(Results[0][0]);
			Date enddate = (Date)formatter.parse(Results[0][1]);
			long diff = enddate.getTime() - startdate.getTime();
			long diffMinutes = diff / (60 * 1000) % 60;
			if (diffMinutes > 0) {
				Results[0][Results[0].length - 1] = Long.toString(diffMinutes) + " minutes"; 
			}else {
				long diffSeconds = diff / 1000 % 60;
				Results[0][Results[0].length - 1] = Long.toString(diffSeconds) + " seconds"; 
			}
			
			int Lenghts[] = new int[Results[0].length];
			for (int i = 0; i < Results.length; i++) { //find the max lengths for formatting sake.
				if (Results[i][0] == "") {
					break;
				}
				for (int j = 0 ; j < Results[0].length; j++) {
					if (Results[i][j] != null && Results[i][j].length() > Lenghts[j]) {
						Lenghts[j] = Results[i][j].length();
					}
				}
			}

			//for formatting of the table that is being printed  "____" on the top
			
			int SumOfLengths = 0;     //need to determine why cannot use int sum = IntStream.of(a).sum(); when using java8, compiler error
			for (int i : Lenghts)
				SumOfLengths += i;
			char[] c = new char[SumOfLengths + 5]; 
		    Arrays.fill(c, '_');
			PrintOut("\n" + new String(c), false);
			
			int passedcnt = 0, failedcnt = 0;//failed starts as -1 as the first line will be the execution time and status will be marked as failed.
			for (int i = 0; i < Results.length; i++) { //find the max lengths for formatting sake.
				if (Results[i][0] == "") {
					break;
				}
				String StrFormat = "|";
				for(int j = 0; j < Results[i].length; j++) {
					StrFormat += String.format("%" + Lenghts[j] + "s|", Results[i][j]);
				}
				PrintOut(StrFormat, false);
				if (Results[i][Lenghts.length - 1].contains(Passed)) {
					passedcnt+=1;
				}else if (Results[i][Lenghts.length - 1].contains(Failed)) {
					failedcnt+=1;
				}
			}
			
			//for formatting of the table that is being printed  "____" on the bottom
			PrintOut("|" + new String(c).substring(0, c.length-2) + "|", false);
			PrintOut("Passed: " + passedcnt + " - " + "Failed: " + failedcnt + "\n", false);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}//End Class
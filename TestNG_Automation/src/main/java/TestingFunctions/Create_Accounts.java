package TestingFunctions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import SupportClasses.DriverFactory;
import TestingFunctions.Helper_Functions;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class Create_Accounts extends Helper_Functions{
	private static String ECAMuserid;
	private static String ECAMpassword;
	
	@DataProvider //(parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		ArrayList<String[]> PersonalData = new ArrayList<String[]>();
		PersonalData = getExcelData(".\\Data\\Load_Your_UserIds.xls",  "Data");//create your own file with the specific data
		for(String s[]: PersonalData) {
			if (s[0].contentEquals("ECAM")) {
				ECAMuserid = s[1];
				ECAMpassword = s[2];
			}
		}
			
		List<Object[]> data = new ArrayList<Object[]>();
		ArrayList<String[]> AddressDetails = new ArrayList<String[]>();
		AddressDetails = getExcelData(".\\Data\\AddressDetails.xls",  "Accounts");//load the relevant information from excel file.
		String LevelsToTest = "2";
		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			for (int j = 1; j < AddressDetails.size(); j++) {
				String CountryList[] = AddressDetails.get(j);
				int intLevel = Integer.parseInt(Level);
				
				//pattern to see if the accounts section has at least three account numbers.
				final Pattern pattern = Pattern.compile(".*,.*,.*");
				final Matcher matcher = pattern.matcher(CountryList[8 + intLevel]);
				
				//check if account number is loaded for this country and the given level
				if (CountryList[8 + intLevel] == "") {
					data.add( new Object[] {Level, CountryList, j});
				}else if (!matcher.matches() && CountryList[0].contains("3614 DELVERNE RD")) {
					data.add( new Object[] {Level, CountryList, j});
				}

				
				/*  //incase need to make mass updates to the column
				for (int k = 9; k < 15; k++) {
					if (CountryList[k] != null && CountryList[k].contains(",")) {
						String AccountsUpdated = CountryList[k].replaceAll(",", ", ");
						AccountsUpdated = AccountsUpdated.replaceAll(",  ", ", ");
						PrintOut("Updated: _" + AccountsUpdated, false);
						Helper_Functions.writeExcelData(".\\Data\\AddressDetails.xls", "Accounts", AccountsUpdated, j, k);
					}
				}
				*/
			}
		}
		return data.iterator();
	}

	@Test(dataProvider = "dp")
	public void Account_Creation(String Level, String CountryDetails[], int Row) {
		try {
			String Accounts = null;
			String CountryCode = CountryDetails[6];
			String AccountNumber = Helper_Functions.getExcelFreshAccount(Level, CountryCode, false);
			if (AccountNumber == null || !AccountNumber.contains(",")) {
				String OperatingCompanies = "E";
				if (CountryDetails[7].contentEquals("us")) {
					OperatingCompanies += "F";
				}
				
				String AccountDetails[] = new String[] {CountryCode, CountryCode, OperatingCompanies, "10"};
				String AddressDetails[] = new String[] {CountryDetails[0], CountryDetails[1], CountryDetails[2], CountryDetails[3], CountryDetails[4], CountryDetails[5], CountryCode};

				try {
					Accounts = CreateAccountNumbers(Level, AccountDetails, AddressDetails);
					Helper_Functions.PrintOut(Accounts, false);
					writeExcelData(".\\Data\\AddressDetails.xls", "Accounts", Accounts, Row, 8 + Integer.valueOf(Level));
				} catch (Exception e) {
					writeExcelData(".\\Data\\AddressDetails.xls", "Accounts", "Error", Row, 8 + Integer.valueOf(Level));
					Helper_Functions.PrintOut("Wrote Error to file for", false);
				}
			}
			PrintOut(Accounts, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	
	public static String CreateAccountNumbers(String Level, String AccountDetails[], String AddressDetails[]) throws Exception{ 
		PrintOut("Attempting to create account number for " + Arrays.toString(AddressDetails), true);
		try {
			// AccountDetails Example = 
			//ShippingCountryCode, BillingCountryCode, OperatingCompanies (E = Express, G = Ground, F = Freight so "EDF" is all three), NumberOfAccounts
			// AddressDetails Example =  {"10 FEDEX PKWY 2nd FL", "", "COLLIERVILLE", "Tennessee", "TN", "38017", "US"});
			//Address line 1, address line 2, City, StateName, StateCode, ZipCode, CountryCode

			ChangeURL("ECAM", "", false, Level);
			if (isPresent(By.id("username"))) {
				Type(By.id("username"), ECAMuserid);
				Type(By.id("password"), ECAMpassword);
				Click(By.id("submit"));
			}

			//ECAM Page
			for(int i = 0; i < 30; i++) {
				try {
					WaitPresent(By.id("new_act_info"));
		 			WaitClickable(By.id("new_act_info"));
					Click(By.id("new_act_info"));
					break;
				}catch (Exception e) {
					Helper_Functions.Wait(1);
				}
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
				DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS); //sets the timeout for short to make reduce delay
				new Select(DriverFactory.getInstance().getDriver().findElement(By.id("acctinfo_city_input_info_list"))).selectByValue(AddressDetails[2].toUpperCase());
			}catch (Exception e) {
				Type(By.id("acctinfo_city_input_info_box"), AddressDetails[2]);
			}finally {
				DriverFactory.getInstance().getDriver().manage().timeouts().implicitlyWait(DriverFactory.WaitTimeOut, TimeUnit.SECONDS);
			}
			
			
			
			if (isPresent(By.id("nomatch"))) {
				Click(By.id("nomatch"));
			}
			if (isPresent(By.id("next_reg"))) {//Regulatory Informaiton page
				if (WebDriver_Functions.isPresent(By.xpath("//*[@id='mand']"))){//Tax id 1
					//By.id("reg_tax_id_one")
				}
				if (WebDriver_Functions.isPresent(By.xpath("//*[@id='mand1']"))){//Tax id 2
					//By.id("reg_tax_id_two")
				}
				Click(By.id("next_reg"));
			}
			
			if (isPresent(By.id("next_payment"))) {
				Click(By.id("next_payment"));
			}
			
			//Payment Information
			String CreditCard[] = LoadCreditCard("V");
			String Payment = "Invoice";
			try {
				WaitPresent(By.id("acct_pay_info_list"));
				Select(By.id("acct_pay_info_list"), "Credit_Card","v");
				
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
	
	
}
	/*
	
	public static void main(String[] args) {
		ArrayList<String[]> Addresses = new ArrayList<String[]>();
		Addresses = Helper_Functions.getExcelData(".\\Data\\AddressDetails.xls", "Accounts");//load the relevant information from excel file.
		String Level = "2";
		String AddressDetailsFormat[] = Addresses.get(0);
		Helper_Functions.PrintOut(Arrays.toString(AddressDetailsFormat), false);
		// AccountDetails Example = 
		//ShippingCountryCode, BillingCountryCode, OperatingCompanies (E = Express, G = Ground, F = Freight so "EDF" is all three), NumberOfAccounts
		// AddressDetails Example =  {"10 FEDEX PKWY 2nd FL", "", "COLLIERVILLE", "Tennessee", "TN", "38017", "US"});
		//Address line 1, address line 2, City, StateName, StateCode, ZipCode, CountryCode
		
		for(int i = 1; i < Addresses.size(); i++) {
			String CountryDetails[] = Addresses.get(i);
			for (int Format = 0; Format < 8; Format++) {
				boolean update = false;
				String initial = CountryDetails[Format];
				if (CountryDetails[Format].contains("\n") || CountryDetails[Format].contains("  ")) {
					CountryDetails[Format] = CountryDetails[Format].replaceAll("\n", "");
					CountryDetails[Format] = CountryDetails[Format].replaceAll("  ", " ");
				}
				if (Format == 6 && CountryDetails[Format] != CountryDetails[Format].toUpperCase()) {
					CountryDetails[Format] = CountryDetails[Format].toUpperCase();
				}
				if (CountryDetails[Format].length() > 30) {
					CountryDetails[Format] = CountryDetails[Format].substring(0, 29);
				}
				if (!CountryDetails[Format].matches("[^A-Za-z0-9 ]")) {
					CountryDetails[Format] = unAccent(CountryDetails[Format]);
				}
				if (update) {
					PrintOut("Updated: _" + initial + "_ -> _" + CountryDetails[Format], false);
					Helper_Functions.writeExcelData(".\\Data\\AddressDetails.xls", "Accounts", CountryDetails[Format], i, Format);
				}
			}
			
			
			String CountryCode = CountryDetails[6];
			//if (CountryCode.contentEquals("US")) {
				String AccountNumber = Helper_Functions.getExcelFreshAccount(Level, CountryCode, false);
				if (AccountNumber == null || !AccountNumber.contains(",")) {
					String OperatingCompanies = "E";
					if (CountryDetails[7].contentEquals("us")) {
						OperatingCompanies += "F";
					}
				
					String AccountDetails[] = new String[] {CountryCode, CountryCode, OperatingCompanies, "10"};
					String AddressDetails[] = new String[] {CountryDetails[0], CountryDetails[1], CountryDetails[2], CountryDetails[3], CountryDetails[4], CountryDetails[5], CountryCode};
				
					String Accounts = null;
					try {
						Accounts = Helper_Functions.CreateAccountNumbers(Level, AccountDetails, AddressDetails);
						Helper_Functions.PrintOut(Accounts, false);
						writeExcelData(".\\Data\\AddressDetails.xls", "Accounts", Accounts, i, 8 + Integer.valueOf(Level));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			//}
			
		}

		DriverFactory.getInstance().removeDriver();
	}
	
	public static String unAccent(String s) {
	    //
	    // JDK1.5
	    //   use sun.text.Normalizer.normalize(s, Normalizer.DECOMP, 0);
	    //
	    String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(temp).replaceAll("");
	  }

}

/*
need to add the below to the address excel sheet.


			ContactList.add(new String[] {"3614 DELVERNE RD", "", "BALTIMORE", "Maryland", "MD", "21218", "US", "7i4ksltmhnqlxhzbs9j6356ix"});
			ContactList.add(new String[] {"891 Federal Ridge road", "Apt 202", "COLLIERVILLE", "Tennessee", "TN", "38017", "US", "44k0o0ipf25thfcyl8svm65zz"});
			ContactList.add(new String[] {"910 S MADISON ROW CT", "", "COLLIERVILLE", "Tennessee", "TN",  "38017", "US", "16nsluqsf6tdh736wjaito6"});
			ContactList.add(new String[] {"10 FEDEX PKWY 2nd FL", "", "COLLIERVILLE", "Tennessee", "TN", "38017", "US"});
			ContactList.add(new String[] {"10 FedEx Parkway", "", "COLLIERVILLE", "Tennessee", "TN", "38017", "US"});
			ContactList.add(new String[] {"3400, N Charles St", "Apt #103", "Baltimore", "Maryland", "MD", "21218", "US"});
			ContactList.add(new String[] {"10 FedEx Parkway", "", "COLLIERVILLE", "Tennessee", "TN", "38017", "US"});
			ContactList.add(new String[] {"7939 Silver Lake Lane", "Apt 303", "Memphis", "Tennessee", "TN", "38119", "US"});
			ContactList.add(new String[] {"4900 Alton Court", "", "Irondale", "Alabama", "AL", "35210", "US"});
			ContactList.add(new String[] {"240 Turner Rd", "", "Forrest City", "Arkansas", "AR", "72335", "US"});
			ContactList.add(new String[] {"9015 E VIA LINDA STE 999", "", "SCOTTSDALE", "Arizona", "AZ", "85258", "US"});
			ContactList.add(new String[] {"3901 INGLEWOOD AVE", "", "REDONDO BEACH", "California", "CA", "90278", "US", "3dfkio9dacatwofvg2ydhwahs"});
			ContactList.add(new String[] {"350 spectrum loop", "", "colorado springs", "Colorado", "CO", "80921", "US"});
			ContactList.add(new String[] {"4 MEADOW ST", "", "NORWALK", "Connecticut", "CT", "06854", "US"});
			ContactList.add(new String[] {"1900 SUMMIT TOWER BLVD STE 300", "", "ORLANDO", "Florida", "FL", "32810", "US"});
			ContactList.add(new String[] {"1070 BERTRAM RD", "", "AUGUSTA", "Georgia", "GA", "30909", "US"});
			ContactList.add(new String[] {"1154 FORT STREET MALL", "", "HONOLULU", "Hawaii", "HI", "96813", "US"});
			ContactList.add(new String[] {"8527 UNIVERSITY BLVD STE 99", "", "DES MOINES", "Iowa", "IA", "50325", "US"});
			ContactList.add(new String[] {"1430 E 17TH ST", "", "IDAHO FALLS", "Idaho", "ID", "83404", "US"});
			ContactList.add(new String[] {"1315 W 22ND ST", "", "OAK BROOK", "Illinois", "IL", "60523", "US"});
			ContactList.add(new String[] {"6648 S PERIMETER RD", "", "INDIANAPOLIS", " Indiana", "IN", "46241", "US"});
			ContactList.add(new String[] {"1530 S HOOVER", "", "WICHITA", "Kansas", "KS", "67209", "US"});
			ContactList.add(new String[] {"6330 STRAWBERRY LN", "", "LOUISVILLE", "Kentucky", "KY", "40214", "US"});
			ContactList.add(new String[] {"2122 GREENWOOD RD", "", "SHREVEPORT", "Louisiana", "LA", "71103", "US"});
			ContactList.add(new String[] {"25 Sycamore Ave", "", "Medford", "Massachusetts", "MA", "02155", "US"});
			ContactList.add(new String[] {"95 HUTCHINS DR", "", "PORTLAND", "Maine", "ME", "04102", "US"});
			ContactList.add(new String[] {"2386 TRAVERSEFIELD", "", "TRAVERSE CITY", "Michigan", "MI", "49686", "US"});
			ContactList.add(new String[] {"261 CHESTER ST", "", "SAINT PAUL", "Minnesota", "MN", "55107", "US"});
			ContactList.add(new String[] {"9133 Superior Dr", "", "OLIVE BRANCH", "Mississippi", "MS", "38654", "US"});
			ContactList.add(new String[] {"1203 Beartooth Drive", "", "Laurel", "Montana", "MT", "59044", "US"});
			ContactList.add(new String[] {"3801 BEAM RD STE F", "", "CHARLOTTE", "North Carolina", "NC", "28217", "US"});
			ContactList.add(new String[] {"7130 Q ST", "", "OMAHA", "Nebraska", "NE", "68117", "US"});
			ContactList.add(new String[] {"190 JONY DR", "", "CARLSTADT", "New Jersey", "NJ", "07072", "US"});
			ContactList.add(new String[] {"98 WESTGATE ST", "", "LAS CRUCES", "New Mexico", "NM", "88005", "US"});
			ContactList.add(new String[] {"1025 WESTCHESTER AVE STE", "", "WEST HARRISON", "New York", "NY", "10604", "US"});
			ContactList.add(new String[] {"1330 ELM ST", "", "CINCINNATI", "Ohio", "OH", "45202", "US"});
			ContactList.add(new String[] {"7181 S Mingo Rd", "", "Tulsa", "Oklahoma", "OK", "74133", "US"});
			ContactList.add(new String[] {"1800 NW 169TH PL STE B200", "", "BEAVERTON", "Oregon", "OR", "97006", "US"});
			ContactList.add(new String[] {"6350 HEDGEWOOD DR", "", "ALLENTOWN", "Pennsylvania", "PA", "18106", "US"});
			ContactList.add(new String[] {"255 METRO CENTER BLVD", "", "WARWICK", "Rhode Island", "RI", "02886", "US"});
			ContactList.add(new String[] {"345 W STEAMBOAT DR", "", "NORTH SIOUX CITY", "South Dakota", "SD", "57049", "US"});
			ContactList.add(new String[] {"4200 Regent Blvd", "", "Irving", "Texas", "TX", "75063", "US"});
			ContactList.add(new String[] {"200 S MARQUETTE RD", "", "PRAIRIE DU CHIEN", "Wisconsin", "WI", "53821", "US"});
			ContactList.add(new String[] {"1206 GREENBRIER ST", "", "CHARLESTON", "West Virginia", "WV", "25311", "US"});
			ContactList.add(new String[] {"1249 Tongass Avenue", "", "KETCHIKAN","Alaska", "AK",  "99901", "US"});
			ContactList.add(new String[] {"1555 E University Dr #1", "", "Mesa","Arizona", "AZ",  "85203", "US"});
			ContactList.add(new String[] {"32 Meadow Crest Dr", "", "Sherwood", "Arkansas", "AR", "72120", "US"});
			ContactList.add(new String[] {"329 Madison Street", "", "Denver", "Colorado", "CO", "80206", "US"});
			ContactList.add(new String[] {"58 Cabot St", "", "Hartford", "Connecticut", "CT", "06112", "US"});
			ContactList.add(new String[] {"310 Haines St", "", "Newark", "Delaware", "DE", "19717", "US"});
			ContactList.add(new String[] {"1405 Rhode Island Ave NW", "", "Washington", "District of Columbia", "DC", "20005", "US"});
			ContactList.add(new String[] {"2950 N 28th Terrace", "", "Hollywood", "Florida", "FL", "33020", "US"});
			ContactList.add(new String[] {"901 Hitt St", "", "Columbia", "Missouri", "MO", "65212", "US"});
			ContactList.add(new String[] {"75-681 Lalii Pl", "", "Kailua Kona", "Hawaii", "HI", "96740", "US"});
			ContactList.add(new String[] {"410 W Washington St", "", "Caseyville", "Illinois", "IL", "62232", "US"});
			ContactList.add(new String[] {"1131 Shelby St", "", "Indianapolis", "Indiana", "IN", "46203", "US"});
			ContactList.add(new String[] {"11110 W Greenspoint St", "", "Wichita", "Kansas", "KS", "67205", "US"});
			ContactList.add(new String[] {"5613 Fern Valley Rd", "", "Louisville", "Kentucky", "KY", "40228", "US"});
			ContactList.add(new String[] {"2206 Urbandale St", "", "Shreveport", "Louisiana", "LA", "71118", "US"});
			ContactList.add(new String[] {"89 Turnpike Rd", "", "Ipswich", "Massachusetts", "MA", "01938", "US"});
			ContactList.add(new String[] {"500 S State St # 2005", "", "Ann Arbor", "Michigan", "MI", "48109", "US"});
			ContactList.add(new String[] {"210 Delaware St SE", "", "Minneapolis", "Minnesota", "MN", "55455", "US"});
			
			ContactList.add(new String[] {"310 ROUTE 70", "", "ABU DHABI", "", "", "", "AE"});
			ContactList.add(new String[] {"Ciudad Evita", "", "DUA Buenos Aires", "", "", "B1778", "AR"});
			ContactList.add(new String[] {"Regina Bianchi Peruzzo", "", "Rio Grande do Sul", "Rio Grande do Sul", "RS", "99965", "BR"});
			ContactList.add(new String[] {"2 MARINE PARADE", "", "BELIZE CITY", "", "", "480", "BZ"});
			ContactList.add(new String[] {"1100 BOUL RENE-LEVESQUE E", "", "QUEBEC", "QUEBEC", "PQ", "G1R 5V2", "CA"});
			ContactList.add(new String[] {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "CL"});
			ContactList.add(new String[] {"BROADWAY LAYOUT", "", "BEIJING ODA", "", "", "102200", "CN"});
			ContactList.add(new String[] {"791 MILL STREET", "", "WITTEN", "", "", "58448", "DE"});
			ContactList.add(new String[] {"120 MARINE DRIVE", "", "LAUTOKA", "", "", "", "FJ"});
			ContactList.add(new String[] {"4 Boulevard Berthier", "", "Paris", "", "", "75017", "FR"});
			ContactList.add(new String[] {"333 SHERWOOD DRIVE", "", "NEWPORT", "", "", "TF10 7BX", "GB"});
			ContactList.add(new String[] {"10 AVENUE PARK", "", "BARRIGADA", "", "", "96913", "GU"});
			ContactList.add(new String[] {"BROADWAY LAYOUT", "", "BEIJING ODA", "", "", "102200", "HK"});
			ContactList.add(new String[] {"150 Kennedy Road", "", "HONG KONG", "", "", "", "HK"});
			ContactList.add(new String[] {"Aghapura", "", "", "Telangana", "TS", "500001", "IN"});
			ContactList.add(new String[] {"Via di Acqua Bullicante", "", "Roma", "", "", "00176", "IT"});
			ContactList.add(new String[] {"1-5-2 Higashi-Shimbashi", "", "Minato-ku", "", "", "1057123", "JP"});
			ContactList.add(new String[] {"Calle San Juan de Dios 68", "", "MEXICO", "Distrito Federal", "DF", "14370", "MX"});
			ContactList.add(new String[] {"10, Lorong P Ramlee, Kuala Lumpur", "", "KUALA LUMPUR", "", "", "50250", "MY"});
			ContactList.add(new String[] {"15 Stevens Cl", "", "Singapore", "", "", "25795", "SG"});

 */

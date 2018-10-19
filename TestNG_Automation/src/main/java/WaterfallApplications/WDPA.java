package WaterfallApplications;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import SupportClasses.DriverFactory;
import TestingFunctions.Helper_Functions;
import TestingFunctions.WDPA_Functions;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class WDPA extends WDPA_Functions{
	
	static ArrayList<String[]> AddressDetails = new ArrayList<String[]>();
	static String LevelsToTest = "2";
	static String CountryList[][];

	@BeforeClass
	public void beforeClass() {
		DriverFactory.LevelsToTest = LevelsToTest;
		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			Helper_Functions.LoadUserIds(Integer.parseInt(Level));
		}
		CountryList = new String[][]{{"US", "United States"}};
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			int intLevel = Integer.parseInt(Level);

			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "WDPA_Pickup_Ground":
		    	case "WDPA_Pickup_Express":
		    	case "WDPAPickup_ExpressFright"://need to fix this later, not for all countries.
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < Helper_Functions.DataClass[intLevel].length; k++) {
		    				if (Helper_Functions.DataClass[intLevel][k].COUNTRY_CD.contentEquals(CountryList[j][0])) {
		    					data.add( new Object[] {Level, CountryList[j][0], Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC, Helper_Functions.DataClass[intLevel][k].USER_PASSWORD_DESC});
		    					break;
		    				}
		    			}
					}
		    	break;
		    	case "WDPAPickup_LTLFreight":    //update this later to restrict based on country
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < Helper_Functions.DataClass[intLevel].length; k++) {
		    				if (Helper_Functions.DataClass[intLevel][k].COUNTRY_CD.contentEquals(CountryList[j][0]) && Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC.contains("Freight")) {
		    					data.add( new Object[] {Level, CountryList[j][0], Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC, Helper_Functions.DataClass[intLevel][k].USER_PASSWORD_DESC});
		    					break;
		    				}
		    			}
					}
		    	break;
		    	case "WDPAPickup_LTLFreight_Anonymous":    //update this later to restrict based on country
		    		for (int j = 0; j < CountryList.length; j++) {
		    			data.add( new Object[] {Level, CountryList[j][0]});
					}
		    	break;
			}
		}	
		return data.iterator();
	}
	
	
	@Test(dataProvider = "dp")
	public static void WDPA_Pickup_Ground(String Level, String CountryCode, String UserID, String Password){
		Helper_Functions.PrintOut("Schedule a ground pickup.", false);
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String Result = Arrays.toString(WDPAPickupDetailed(Level, CountryCode, UserID, Password, "ground", "CompanyNameHere", "John Doe", "9011111111", Address, null, "INET"));
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WDPA_Pickup_Ground
	
	@Test(dataProvider = "dp")
	public static void WDPA_Pickup_Express(String Level, String CountryCode, String UserID, String Password){
		Helper_Functions.PrintOut("Schedule an express pickup.", false);
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String Result = Arrays.toString(WDPAPickupDetailed(Level, CountryCode, UserID, Password, "express", "CompanyNameHere", "John Doe", "9011111111", Address, null, "INET"));
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WDPA_Pickup_Express
	
	@Test(dataProvider = "dp")
	public static void WDPAPickup_ExpressFright(String Level, String CountryCode, String UserID, String Password){
		Helper_Functions.PrintOut("Schedule an express freight pickup.", false);
		try {
			String PackageDetails[] = {"1", "444", "L", "1500", "1800", "ExpLTL Attempt", "FedEx 1Day Freight", "ConfFiller", "side of barn", "5", "6", "7"};
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String Result = Arrays.toString(WDPAPickupDetailed(Level, "US", UserID, Password, "expFreight", "ExpressLTL Testing", "ExpressLTL Attempt", "9011111111", Address, PackageDetails, "INET"));
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WDPAPickup_ExpressFright
	
	
	@Test(dataProvider = "dp")
	public static void WDPAPickup_LTLFreight(String Level, String CountryCode, String UserID, String Password){
		Helper_Functions.PrintOut("Schedule a LTL pickup while logged in.", false);
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String Result = WDPALTLPickup(Level, Address, UserID, Password);
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WDPAPickup_ExpressFright
	
	
	@Test(dataProvider = "dp")
	public static void WDPAPickup_LTLFreight_Anonymous(String Level, String CountryCode){
		Helper_Functions.PrintOut("Schedule a LTL pickup while not logged into FedEx.com", false);
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String Result = WDPALTLPickup(Level, Address, "", "");
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WDPAPickup_ExpressFright
	
}


/*

	
	@Test
	public void WDPAPickup_LTLFreight_Anonymous() {
		String Confirmation = null;
		try {
			Confirmation = WDPALTLPickup(LoadAddress("US"), "", "");
		} catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(ResultsList.size() - 1, UpdateArrayList(ResultsList.get(ResultsList.size() - 1), 1, "Anonymous " + Confirmation));
	}

	
	
	
	//@Test
	public void WDPAPickup_GroundLoop() {
		for (int i = 1;;i++) {
			String Result[] = null;
			try {
				PrintOut("Attempt: " + i + "  @@@@@@@#######", false);
				//WDPAPickupDetailed("US", "L3ePRSTest116308843", strPassword, "ground", "CompanyNameHere", "John Doe", "9011111111", LoadAddress("US", "CA", "3901 INGLEWOOD AVE"), null, "INET");
				WDPAPickupDetailed("US", "L3ePRSTest116308843", strPassword, "ground", "CompanyNameHere", strTodaysDate, "9011111111", LoadAddress("US", "CA", "4252 Camino Del Rio N."), null, "INET");
				PassOrFail = true;
				ResultsList.set(ResultsList.size() - 1, UpdateArrayList(ResultsList.get(ResultsList.size() - 1), 1, Arrays.toString(Result)));
				Thread.sleep(60000);
	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//Relevant funcitons for the above
	////////////////////////////////////////////////////////////////////////////////////////
	

	
}


*/
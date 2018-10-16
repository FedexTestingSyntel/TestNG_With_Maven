package WaterfallApplications;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import SupportClasses.DriverFactory;
import TestingFunctions.Helper_Functions;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class WDPA {
	
	static ArrayList<String[]> AddressDetails = new ArrayList<String[]>();
	static String LevelsToTest = "6";
	static String CountryList[][];

	@BeforeClass
	public void beforeClass() {
		DriverFactory.LevelsToTest = LevelsToTest;
		
		CountryList = new String[][]{{"US", "United States"}};
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			//int intLevel = Integer.parseInt(Level);

			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "WRTT_Rate_Sheet":
		    		data.add( new Object[] {Level, true, 5, true, false});
		    		data.add( new Object[] {Level, true, 14, true, false});
		    		data.add( new Object[] {Level, true, 4, true, true});
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
	public static void WDPA_Pickup_Ground(String Level, String CountryCode, String UserID, String Password){
		Helper_Functions.PrintOut("Schedule a ground pickup.", false);
		try {
			String Address[] = Helper_Functions.LoadAddress(CountryCode, Level);
			String Result = WDPAPickupDetailed(Level, CountryCode, UserID, Password, "ground", "CompanyNameHere", "John Doe", "9011111111", Address, null, "INET");
			Helper_Functions.PrintOut(Result, false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}//end WRTT_Rate_Sheet
	
	
}


/*
	@Test
	public void WDPAPickup_Ground() {
		String Result[] = null;
		try {
			Result = WDPAPickupDetailed("US", strWDPAnonAdmin, strPassword, "ground", "CompanyNameHere", "John Doe", "9011111111", LoadAddress("US"), null, "INET");
		} catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(ResultsList.size() - 1, UpdateArrayList(ResultsList.get(ResultsList.size() - 1), 1, Arrays.toString(Result)));
	}
	

	@Test
	public void WDPAPickup_Express() {
		String Result[] = null;
		try {
			//PackageDetails = {String Packages, String Weight, String WeightUnit (L or K), String Date, String ReadyTime, String CloseTime, String Special}
			String PackageDetails[] = {"1", "22", "L", null, null, null, null};
			Result = WDPAPickupDetailed("US", strWDPAnonAdmin, strPassword, "express", "Express Testing", "Express Attempt", strPhone, LoadAddress("US"), PackageDetails, "INET");
		} catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(ResultsList.size() - 1, UpdateArrayList(ResultsList.get(ResultsList.size() - 1), 1, Arrays.toString(Result)));
	}
	
	//@Test
	public void WDPAPickup_ExpressFright() {
		String Result[] = null;
		try {
			String PackageDetails[] = {"1", "444", "L", "1500", "1800", "ExpLTL Attempt", "FedEx 1Day Freight", "ConfFiller", "side of barn", "5", "6", "7"};
			Result = WDPAPickupDetailed("US", strWDPAnonAdmin, strPassword, "expFreight", "ExpressLTL Testing", "ExpressLTL Attempt", strPhone, LoadAddress("US"), PackageDetails, "INET");
			PrintOut(Arrays.toString(Result));
		} catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(ResultsList.size() - 1, UpdateArrayList(ResultsList.get(ResultsList.size() - 1), 1, Arrays.toString(Result)));
	}

	@Test
	public void WDPAPickup_LTLFreight() {
		String Confirmation = null;
		try {
			Confirmation = WDPALTLPickup(LoadAddress("US"), strWDPAFreight, strPassword);
		} catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(ResultsList.size() - 1, UpdateArrayList(ResultsList.get(ResultsList.size() - 1), 1, strWDPAFreight + " " + Confirmation));
	}
	
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
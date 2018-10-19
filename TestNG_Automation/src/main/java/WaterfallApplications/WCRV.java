package WaterfallApplications;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import TestingFunctions.*;

public class WCRV extends WCRV_Functions{
	static ArrayList<String[]> ResultsList = new ArrayList<String[]>();
	static ArrayList<String[]> AddressDetails = new ArrayList<String[]>();
	static String LevelsToTest = "3";
	final static boolean SmokeTest = true; // will limit the test cases to high level
	static String CountryList[][];

	@BeforeClass
	public void beforeClass() {
		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			Helper_Functions.LoadUserIds(Integer.parseInt(Level));
		}
		
		AddressDetails = Helper_Functions.getExcelData(".\\Data\\AddressDetails.xls",  "Countries");//load the relevant information from excel file.

		//if (SmokeTest) 
				CountryList = new String[][]{{"US", "United States"}};
	}
	
	@DataProvider (parallel = true)
	public static Iterator<Object[]> dp(Method m) {
		List<Object[]> data = new ArrayList<Object[]>();

		for (int i=0; i < LevelsToTest.length(); i++) {
			String Level = String.valueOf(LevelsToTest.charAt(i));
			int intLevel = Integer.parseInt(Level);
			//int intLevel = Integer.parseInt(Level);
			switch (m.getName()) { //Based on the method that is being called the array list will be populated.
		    	case "WCRV_Generate_RateSheet":
		    		for (int j = 0; j < CountryList.length; j++) {
		    			for (int k = 0; k < Helper_Functions.DataClass[intLevel].length; k++) {
		    				if (Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC.contains("WCRV") && Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC.contains(CountryList[j][0]) ) {
		    					data.add( new Object[] {Level, CountryList[j][0], Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC, Helper_Functions.DataClass[intLevel][k].USER_PASSWORD_DESC, "intra"});
		    					data.add( new Object[] {Level, CountryList[j][0], Helper_Functions.DataClass[intLevel][k].SSO_LOGIN_DESC, Helper_Functions.DataClass[intLevel][k].USER_PASSWORD_DESC, "notintra"});
		    					break;
		    				}
		    			}
		    		}
		    	break;
			}
		}	
		return data.iterator();
	}
	
	@BeforeMethod
	public void beforeMethod(Method method, Object[] params){
		Helper_Functions.PrintOut(method.getName(), false);    //will print out the name of the function about to be run.  
		String paramsList = "";
		for (Object arr : params) {           //////Need to fix this later, if the object is an array will not print out the correct string
			if (paramsList == "") {
				paramsList = (String) arr;
			}else {
				paramsList += ", " + arr;
			}
        }
		 
		Helper_Functions.PrintOut(paramsList, false); 		//will print out the "<All of the parameters>" 
	}

	@Test(dataProvider = "dp")
	public void WCRV_Generate_RateSheet(String Level, String CountryCode, String UserId, String Password, String Service) {
		try {
			String Result[] = WCRV_Generate(Level, CountryCode, UserId, Password, Service);
			Helper_Functions.PrintOut(Arrays.toString(Result), false);
		}catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	/*
	@Test
	public void WCRV_Generate_Admin_International(){
		String Result[] = null;
		try {
			Result = WCRV_Generate("US", strWCRVAdmin, strPassword, "notintra");
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Result)));
	}
	
	@Test
	public void WCRV_Generate_NonAdmin_Domestic(){
		String Result[] = null;
		try {
			Result = WCRV_Generate("US", strWCRVnonAdmin, strPassword, "intra");
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Result)));
	}
	
	@Test
	public void WCRV_Generate_NonAdmin_International(){
		String Result[] = null;
		try {
			Result = WCRV_Generate("US", strWCRVnonAdmin, strPassword, "notintra");
		}catch (Exception e) {
			Assert.fail();
		}
		PassOrFail = true;
		ResultsList.set(TestNumber, UpdateArrayList(ResultsList.get(TestNumber), 1, Arrays.toString(Result)));
	}
	*/
}

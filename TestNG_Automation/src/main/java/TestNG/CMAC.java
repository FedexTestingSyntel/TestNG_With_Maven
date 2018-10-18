package TestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import org.hamcrest.CoreMatchers;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;

import Helper.Support_Functions;
import TestingFunctions.Helper_Functions;
import API_Calls.*;
import Data_Structures.*;

@Listeners(SupportClasses.TestNG_TestListener.class)

public class CMAC{
	static String LevelsToTest = "2"; //Can but updated to test multiple levels at once if needed. Setting to "23" will test both level 2 and level 3.
	static CMAC_Data DataClass[] = new CMAC_Data[8];//Stores the data for each individual level, please see the before class function below for more details.
	static ArrayList<String[]> ResourceList = new ArrayList<String[]>();//this is a list of when multiple resources are added. Will be initialized in before class.
	static ArrayList<String> applicationUUIDToDelete = new ArrayList<String>();
	
	@BeforeClass
	public void beforeClass() {		//implemented as a before class so the OAUTH tokens are only generated once.
		ArrayList<String[]> Excel_Data = Support_Functions.getExcelData(".\\Data\\CMAC_Properties.xls",  "CMAC");//load the relevant information from excel file.
		String Headers[] = Excel_Data.get(0);
		for (int i = 0; i < LevelsToTest.length(); i++) {
			int Level = Integer.parseInt(LevelsToTest.charAt(i) + "");//the rows will correspond to the correct level. With the row 0 being the column titles.
			//below is each column that is expected in the excel and will be loaded.    08/24/18
			String EnvironmentInformation[] = Excel_Data.get(Level);
			DataClass[Level] = new CMAC_Data();
			for (int j = 0; j < EnvironmentInformation.length; j++) {//added as a precaution to remove spaces from the excel sheet
				EnvironmentInformation[j] = EnvironmentInformation[j].trim();
			}
			//EnvironmentInformation[0] = getAuthToken(EnvironmentInformation[2], EnvironmentInformation[3], EnvironmentInformation[4]);//add token to front of new array after it is generated
			Support_Functions.PrintOut("Headers: " + Arrays.toString(Headers), true);
			Support_Functions.PrintOut(Arrays.toString(EnvironmentInformation), true);//print out all of the urls and date for the level, this is just a reference point to executer
			
		    DataClass[Level].Create_Project_URL = EnvironmentInformation[5];
		    DataClass[Level].Retrieve_Project_Details_URL = EnvironmentInformation[6];
		    DataClass[Level].Update_Project_URL = EnvironmentInformation[7];
		    DataClass[Level].Delete_Project_URL = EnvironmentInformation[8];
		    DataClass[Level].Retrieve_Project_Summary_URL = EnvironmentInformation[9];
		    DataClass[Level].Create_Credentials_Project_URL = EnvironmentInformation[10];
		    DataClass[Level].Delete_Credential_URL = EnvironmentInformation[11];
		    DataClass[Level].DeleteResource_URL = EnvironmentInformation[12];
		    DataClass[Level].Create_Resource_Project_URL = EnvironmentInformation[13];
		}
		Support_Functions.PrintOut("\n\nThread -- Time (MMDDYY'T'HHMMSS): -- Current progress", false);
		
		for (int i = 0; i < 9; i++) {
			String R[] = new String[i];
			for(int j = 0; j < i; j++) {
				R[j] = "Track Service " + j;
			}
			ResourceList.add(R);
		}
		
	}
	
	@DataProvider (parallel = true)
	public Iterator<Object[]> dp(Method m) {
	    List<Object[]> data = new ArrayList<>();

		for (int i = 1; i < 8; i++) {
			if (DataClass[i] != null) {
				CMAC_Data c = DataClass[i];
				
				String Pid, ProjectName;
				switch (m.getName()) { //Based on the method that is being called the array list will be populated.
				case "CreateProject":
					for(int j = 1; j < ResourceList.size(); j++) {
						ProjectName = "Proj_DEL" + j + " " + Support_Functions.CurrentDateTime();
						data.add( new Object[] {c.Create_Project_URL, c.P_Cookie[j % c.P_Cookie.length], ProjectName, ResourceList.get(j)});
					}
					break;
				case "RetrieveProjectDetails":
					for (int j = 0; j < c.P_Cookie.length; j++) {
						ArrayList<String> PidList = PidOfAllProject(c.Retrieve_Project_Summary_URL, c.P_Cookie[j]);
						for (int k = 0; k < PidList.size(); k++) {
							data.add( new Object[] {c.Retrieve_Project_Summary_URL, c.P_Cookie[j], PidList.get(k)});
						}
					}
					break;
				case "RetrieveProjectSummary":
					for (int j = 0; j < c.P_Cookie.length; j++) {
						data.add( new Object[] {c.Retrieve_Project_Summary_URL, c.P_Cookie[j]});
					}
					break;
				case "DeleteProject":
					for(int j=0;j<PidToDelete.size();j++) {
						data.add( new Object[] {c.Delete_Project_URL, PidToDelete.get(j)});
					}
					break;
				case "UpdateProject":
					Pid = PidOfAllProject(c.Retrieve_Project_Summary_URL, c.P_Cookie[0]).get(0);
					ProjectName = "Proj Update " + Support_Functions.CurrentDateTime();
					data.add( new Object[] {c.Update_Project_URL, Pid, ProjectName});//update first project name
					break;
				}//end switch MethodName
			}
		}
		return data.iterator();
	}
		
	@Test(dataProvider = "dp", priority = 1, description = "380527")
	public void CreateProject(String URL, String OAuth_Token, String applicationUUID, String projectName, String latype, String laversion, String latimeStamp) {
		String Response = "";
		
		Response = CMAC_API_Endpoints.CreateProject_API(URL, OAuth_Token, applicationUUID, projectName, latype, laversion, latimeStamp);
		assertThat(Response, CoreMatchers.containsString("transactionId"));
			
		//need to add a check here to see if there are any errors.
		applicationUUIDToDelete.add(applicationUUID);//update this to store the applicationUUID of the created project. Will be deleted later as part of the delete tests.
	}
	
	@Test(dataProvider = "dp", priority = 2, description = "380579 - Details")    
	public void RetrieveProjectDetails(String URL, String OAuth_Token, String organizationUUID, String applicationUUID) {
		String Response = "";
		
		Response = CMAC_API_Endpoints.RetrieveProjectDetails_API(URL, OAuth_Token, organizationUUID, applicationUUID);
			
		String[] Response_Variables = {"transactionId", "output", "applicationUUID", "projectName", "latype", "laversion", "latimeStamp"};
		for(int i = 0; i < Response_Variables.length; i++) {
			assertThat(Response, CoreMatchers.containsString(Response_Variables[i]));
		}
	}
	
	@Test(dataProvider = "dp", priority = 2, description = "380579 - Summery")
	public void RetrieveProjectSummary(String URL, String OAuth_Token, String organizationUUID) {
		String Response;
		
		Response = CMAC_API_Endpoints.RetrieveProjectSummary_API(URL, OAuth_Token, organizationUUID);
			
		String[] Response_Variables = {"transactionId", "output", "applicationUUID", "projectName", "latype", "laversion", "latimeStamp"};
		for(int i = 0; i < Response_Variables.length; i++) {
			assertThat(Response, CoreMatchers.containsString(Response_Variables[i]));
		}

	}

	@Test(dataProvider = "dp", dependsOnMethods = "CreateProject", priority = 2)
	public void DeleteProject(String URL, String Cookie, String Pid) {
		String Result = String.format("<-- DeleteProject(ID:324343): Delete a project based on the pid appended to the URL.    URL:%s, Pid:%s", URL, Pid);
		String Status = Failed, Response;
		
		try {
			Response = CMAC_API_Endpoints.DeleteProject_API(URL, Pid);
			Result += AddToResult(Response);
			
			assertThat(Response, CoreMatchers.allOf(containsString("transactionId"), endsWith("\"}")));
			
			//now check that the project has been removed
			Response = CMAC_API_Endpoints.RetrieveProjectDetails_API(URL, Cookie, Pid);
			Result += AddToResult(Response);
			
			assertThat(Response, CoreMatchers.allOf(containsString("code"), containsString("SYSTEM.UNEXPECTED.ERROR"), containsString("message"), containsString("GENERIC.ERROR")));//not sure if this is correct message
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@Test(dataProvider = "dp", priority = 1)
	public void UpdateProject(String URL, String Cookie, String Pid, String Projectname) {
		String Result = String.format("<-- UpdateProjectName(ID:320258): Udpate the name of a specific Project.    URL:%s, Pid:%s, ProjectName:%s", URL, Pid, Projectname);
		String Response;
		
		try {
			Response = CMAC_API_Endpoints.UpdateProject_API(URL, Pid, Projectname);
			
			assertThat(Response, containsString("transactionId")); ///not sure on the expected, getting error currently
			
			//now check that the project has been updated
			Response = CMAC_API_Endpoints.RetrieveProjectDetails_API(URL, Cookie, Pid);
			
			assertThat(Response, CoreMatchers.allOf(containsString("transactionId"), containsString("output"), containsString("pid"), containsString("projectname"), containsString("resourcesoutput"), containsString("credentialOutput")));
			assertThat(Response, containsString("\"projectname\":\"" + Projectname));
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}


	///////////////////////////////////METHODS//////////////////////////////////
	public ArrayList<String> PidOfAllProject(String Retrieve_Project_Summary_URL, String Cookie) {
		ArrayList<String> Pids = new ArrayList<String>();
		try {
			String b[] = CMAC_API_Endpoints.RetrieveProjectSummary_API(Retrieve_Project_Summary_URL, Cookie);
			String resp = b[1];
			while (resp.contains("pid")) {
				int start = resp.indexOf("{\"pid\":") + 7;
				int end = resp.indexOf(",\"projectname\":");
				Pids.add(resp.substring(start, end));
				resp = resp.substring(end + 15, resp.length() - 1);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return Pids;
	}
	
	///////Helper Functions///////////////Condensed into single class
	public static String[] ParseStringToArray(String s, String Token) {
		int commas = s.replaceAll("[^,]","").length();
		String Temp[] = new String[commas + 1];
		StringTokenizer st1 = new StringTokenizer(s, Token);
	        for (int i = 0; st1.hasMoreTokens(); i++) {
	        	Temp[i] = st1.nextToken().replaceAll(" ", "");
	        }
	    return Temp;
	}
	
}
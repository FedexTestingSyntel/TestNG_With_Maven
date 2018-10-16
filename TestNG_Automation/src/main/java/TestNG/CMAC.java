package TestNG;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
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
import API_Calls.*;
import Data_Structures.*;

public class CMAC{
	static String LevelsToTest = "2"; //Can but updated to test multiple levels at once if needed. Setting to "23" will test both level 2 and level 3.
	static String Passed = "Scenario_Passed", Failed = "Scenario_Failed";
	static ArrayList<String> ResultsList = new ArrayList<String>();//Stores the test cases trace. Will be printed at end for easy debug
	static CMAC_Data DataClass[] = new CMAC_Data[8];//Stores the data for each individual level, please see the before class function below for more details.
	static ArrayList<String[]> ResourceList = new ArrayList<String[]>();//this is a list of when multiple resources are added. Will be initialized in before class.
	static ArrayList<String> PidToDelete = new ArrayList<String>();
	
	static Date ClassStart;
	
	@BeforeClass
	public void beforeClass() {		//implemented as a before class so the OAUTH tokens are only generated once.
		ArrayList<String[]> Excel_Data = Support_Functions.getExcelData(".\\Data\\CMAC_Properties.xls",  "CMAC");//load the relevant information from excel file.
		ClassStart = new Date();
		String Headers[] = Excel_Data.get(0);
		for (int i = 0; i < LevelsToTest.length(); i++) {
			int Level = Integer.parseInt(LevelsToTest.charAt(i) + "");//the rows will correspond to the correct level. With the row 0 being the column titles.
			//below is each column that is expected in the excel and will be loaded.    08/24/18
			String EnvironmentInformation[] = Excel_Data.get(Level);
			DataClass[Level] = new CMAC_Data();
			for (int j = 0; j < EnvironmentInformation.length; j++) {//added as a precaution to remove spaces from the excel sheet
				EnvironmentInformation[j] = EnvironmentInformation[j].trim();
				if (Headers[j].contains("_UUID")) {
					String UserID[] = ParseStringToArray(EnvironmentInformation[j-3], ",");
					String User_Password[] = ParseStringToArray(EnvironmentInformation[j-2], ",");
					String Cookies[] = new String[UserID.length];
					String UUIDs[] = new String[UserID.length];
					for (int cky = 0; cky < UserID.length; cky++) {
						String fdx_login[] = USRC_API_Endpoints.Login(UserID[cky], User_Password[cky], Level);
						Cookies[cky] = fdx_login[0];//update the cookie value with one that was just retrieved from USRC.
						UUIDs[cky] = fdx_login[1];//update the uuid value with one that was just retrieved from USRC.
						Support_Functions.PrintOut(String.format("  Cookie: %s, UUID: %s", fdx_login[0], fdx_login[1]), true);
					}

					if (Headers[j].contentEquals("Passkey_UUID")) {
						DataClass[Level].P_UserID = UserID;
						DataClass[Level].P_User_Password = User_Password;
						DataClass[Level].P_Cookie = Cookies;
						DataClass[Level].P_UUID = UUIDs;
					}else if (Headers[j].contentEquals("Non_Passkey_UUID")) {
						DataClass[Level].NonP_UserID = UserID;
						DataClass[Level].NonP_User_Password = User_Password;
						DataClass[Level].NonP_Cookie = Cookies;
						DataClass[Level].NonP_UUID = UUIDs;
					}
				}
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
				case "CreateProjectInvalidLength":
					ProjectName = "Proj_Invalid Lentgh " + Support_Functions.getRandomString(99); //update this later with the max lenght
					data.add(new Object[] {c.Create_Project_URL, c.P_Cookie[0], ProjectName, ResourceList.get(0)});
					break;
				case "RetrieveProjectDetails":    ///Need to add cookie
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
				case "DeleteResources":
					for(int j=0;j<PidToDelete.size();j++) {
						data.add( new Object[] {c.DeleteResource_URL, PidToDelete.get(j)});
					}
					break;
				case "DeleteCredentials":
					for(int j=0;j<PidToDelete.size();j++) {
						data.add( new Object[] {c.Delete_Credential_URL, PidToDelete.get(j)});
					}
					break;
				case "UpdateProjectName":
					Pid = PidOfAllProject(c.Retrieve_Project_Summary_URL, c.P_Cookie[0]).get(0);
					
					ProjectName = "Proj Update " + Support_Functions.CurrentDateTime();
					data.add( new Object[] {c.Update_Project_URL, Pid, ProjectName});//update first project name
					break;
				case "CreateCredentialsForProject":
					for (int j = 0; j < c.P_Cookie.length; j++) {
						data.add(new Object[] {c.Retrieve_Project_Summary_URL, c.P_Cookie[j]});
					}
					break;
				case "AddResourcesToProject":
					Pid = PidOfAllProject(c.Retrieve_Project_Summary_URL, c.P_Cookie[0]).get(0);
					//String ProjDetails[] = RetrieveProjectDetails_API(Retrieve_Project_Details_URL, Pid);
					for(int j = 1; j < ResourceList.size(); j++) {
						data.add( new Object[] {c.Create_Resource_Project_URL, Pid, ResourceList.get(j)});
					}
					break;
				}//end switch MethodName
			}
		}
		return data.iterator();
	}
	
	@DataProvider (parallel = true)//the below are only used for managing test data
	public Iterator<Object[]> TestData_dp(Method m) {
	    List<Object[]> data = new ArrayList<>();

		for (int i = 1; i < 8; i++) {
			if (DataClass[i] != null) {
				CMAC_Data c = DataClass[i];
				
				switch (m.getName()) { //Based on the method that is being called the array list will be populated.
				case "DeleteAllProject"://delete all of the projects
					data.add( new Object[] {c.Delete_Project_URL, c.Retrieve_Project_Summary_URL});
					break;	
				case "CreateProjectForTest":
					for(int j = 1; j < c.P_Cookie.length; j++) {
						String ProjectName = "Proj " + c.P_UserID[j];
						data.add( new Object[] {c.Create_Project_URL, c.P_Cookie[j], ProjectName, ResourceList.get(j)});
					}
					break;
				}//end switch MethodName
			}
		}
		return data.iterator();
	}
		
	@Test(dataProvider = "dp", priority = 1)
	public void CreateProject(String URL, String Cookie, String ProjectName, String Resources[]) {
		String Result = String.format("<-- CreateProject(ID: 274893): Create a new project based on the UUID.    URL:%s, Cookie:%s, ProjectName:%s, Resources%s", URL, Cookie, ProjectName, Arrays.toString(Resources));
		String Status = Failed, Buffer[] = new String[] {"", ""};
		
		try {
			Buffer = CMAC_API_Endpoints.CreateProject_API(URL, Cookie, ProjectName, Resources);
			Result += AddToResult(Buffer);
			
			String[] Response_Variables = {"transactionId", "pid", "projectname", "cid", "accountNumber", "creationtime", "resources", "expirationdate", "key"};
			for(int i = 0; i < Response_Variables.length; i++) {
				assertThat(Buffer[1], CoreMatchers.containsString(Response_Variables[i]));
			}
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
			try {
				Buffer[0] = Buffer[1].substring(Buffer[1].indexOf("pid\":") + 5, Buffer[1].indexOf(",\"projectname"));
				PidToDelete.add(Buffer[0]);//update this to store the pid of the created project. Will be deleted later as part of the delete tests.
			}catch (Exception e) {}
		}
	}
	
	@Test(dataProvider = "dp", priority = 1)
	public void CreateProjectInvalidLength(String URL, String Cookie, String ProjectName, String Resources[]) {
		String Result = String.format("<-- CreateProjectInvalidName(ID: 274893): Create a new project based on the UUID.    URL:%s, Cookie:%s, ProjectName:%s, Resources%s", URL, Cookie, ProjectName, Arrays.toString(Resources));
		String Status = Failed, Buffer[] = new String[] {"", ""};
		
		try {
			Buffer = CMAC_API_Endpoints.CreateProject_API(URL, Cookie, ProjectName, Resources);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("Bad Request"), containsString("400")));//not sure if this is correct message
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
			try {
				Buffer[0] = Buffer[1].substring(Buffer[1].indexOf("pid\":") + 5, Buffer[1].indexOf(",\"projectname"));
				PidToDelete.add(Buffer[0]);//update this to store the pid of the created project
			}catch (Exception e) {}
		}
	}
	
	@Test(dataProvider = "dp", priority = 1)    
	public void RetrieveProjectDetails(String URL, String Cookie, String Pid) {
		String Result = "<-- RetrieveProjectDetails(ID: 324340): Get the project details of the specific project.    ";
		Result += String.format("URL:%s, Pid:%s", URL, Pid);
		String Status = Failed, Buffer[];
		
		try {
			Buffer = CMAC_API_Endpoints.RetrieveProjectDetails_API(URL, Cookie, Pid);
			Result += AddToResult(Buffer);
			
			String[] Response_Variables = {"transactionId", "output", "pid", "projectname", "credentialOutput", "resourcesoutput"};
			for(int i = 0; i < Response_Variables.length; i++) {
				assertThat(Buffer[1], CoreMatchers.containsString(Response_Variables[i]));
			}
			
			//the below will only be present if the project contains credentials.
			Response_Variables = new String[] {"projectid", "accountNumber", "creationtime", "expirationdate", "key"};
			if (Buffer[1].contains(Response_Variables[2])) {
				for(int i = 0; i < Response_Variables.length; i++) {
					assertThat(Buffer[1], CoreMatchers.containsString(Response_Variables[i]));
				}
			}else {
				Result += AddToResult(new String[]{"Project does not contain credentials", ""});
			}
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp", priority = 1)
	public void RetrieveProjectSummary(String URL, String Cookie) {   //the code needs to be updated. Currently will not limit the projects on what user should be able to see.
		String Result = "<-- RetrieveProjectSummary(ID:324332): Get the details of all projects.    ";
		Result += String.format("URL:%s Cookie:%s", URL, Cookie);
		String Status = Failed, Buffer[];
		
		try {
			Buffer = CMAC_API_Endpoints.RetrieveProjectSummary_API(URL, Cookie);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("transactionId"), containsString("output"), containsString("projects"), containsString("pid"), containsString("projectname")));
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@Test(dataProvider = "dp", dependsOnMethods = "CreateProject", priority = 2)
	public void DeleteProject(String URL, String Cookie, String Pid) {
		String Result = String.format("<-- DeleteProject(ID:324343): Delete a project based on the pid appended to the URL.    URL:%s, Pid:%s", URL, Pid);
		String Status = Failed, Buffer[];
		
		try {
			Buffer = CMAC_API_Endpoints.DeleteProject_API(URL, Pid);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("transactionId"), endsWith("\"}")));
			
			//now check that the project has been removed
			Buffer = CMAC_API_Endpoints.RetrieveProjectDetails_API(URL, Cookie, Pid);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("code"), containsString("SYSTEM.UNEXPECTED.ERROR"), containsString("message"), containsString("GENERIC.ERROR")));//not sure if this is correct message
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp", priority = 1)
	public void DeleteResources(String URL, String Cookie, String Pid, String Resource) {
		String Result = "<-- DeleteResources(ID:?): Delete a resource from a project.    " + String.format("URL:%s, Pid:%s, Resource:%s", URL, Pid, Resource);
		String Status = Failed, Buffer[];
		try {
			Buffer = CMAC_API_Endpoints.DeleteResource_API(URL, Pid, Resource);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("transactionId"), endsWith("\"}")));
			
			//now check that the project has been removed, need to add how this works
			Buffer = CMAC_API_Endpoints.RetrieveProjectDetails_API(URL, Cookie, Pid);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("code"), containsString("SYSTEM.UNEXPECTED.ERROR"), containsString("message"), containsString("GENERIC.ERROR")));//not sure if this is correct message
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp", priority = 1)
	public void DeleteCredentials(String URL, String Cookie, String Pid, String Cid) {
		String Result = String.format("<-- DeleteCredentials(ID:324336): Delete the credentials of a project.    URL:%s, Pid:%s, Cid:%s", URL, Pid, Cid);
		String Status = Failed, Buffer[];
		
		try {
			Buffer = CMAC_API_Endpoints.DeleteCredentials_API(URL, Cid);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("transactionId"), endsWith("\"}")));
			
			//now check that the project has been removed
			Buffer = CMAC_API_Endpoints.RetrieveProjectDetails_API(URL, Cookie, Pid);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("code"), containsString("SYSTEM.UNEXPECTED.ERROR"), containsString("message"), containsString("GENERIC.ERROR")));//not sure if this is correct message
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}
	
	@Test(dataProvider = "dp", priority = 1)
	public void UpdateProjectName(String URL, String Cookie, String Pid, String Projectname) {
		String Result = String.format("<-- UpdateProjectName(ID:320258): Udpate the name of a specific Project.    URL:%s, Pid:%s, ProjectName:%s", URL, Pid, Projectname);
		String Status = Failed, Buffer[];
		
		try {
			Buffer = CMAC_API_Endpoints.UpdateProject_API(URL, Pid, Projectname);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], containsString("transactionId")); ///not sure on the expected, getting error currently
			
			//now check that the project has been updated
			Buffer = CMAC_API_Endpoints.RetrieveProjectDetails_API(URL, Cookie, Pid);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("transactionId"), containsString("output"), containsString("pid"), containsString("projectname"), containsString("resourcesoutput"), containsString("credentialOutput")));
			assertThat(Buffer[1], containsString("\"projectname\":\"" + Projectname));
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
		}
	}

	@Test(dataProvider = "dp", priority = 1)
	public void CreateCredentialsForProject(String URL, String Cookie) {
		String Result = "<-- CreateCredentialsForProject(ID:325036): Create the individual credential that is linked to the project.    ";
		Result += String.format("URL:%s, Cookie:%s", URL, Cookie);

		String Status = Failed, Buffer[] = new String[] {"", ""};
		
		try {
			String Pid = PidOfAllProject(URL, Cookie).get(0);
			Result += AddToResult(new String[] {"Pin from " + Cookie, Pid});
			Buffer = CMAC_API_Endpoints.CreateCredentials_API(URL, Cookie, Pid);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("transactionId"), containsString("output"), containsString("projectid"), containsString("creationtime"), containsString("expirationdate")));
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("key"), containsString("accountNumber")));

			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
			try {
				Buffer[0] = Buffer[1].substring(Buffer[1].indexOf("pid\":") + 5, Buffer[1].indexOf(",\"projectname"));
				PidToDelete.add(Buffer[0]);//update this to store the pid of the newly created project
			}catch (Exception e) {}
		}
	}
	
	@Test(dataProvider = "dp", priority = 1)
	public void AddResourcesToProject(String URL, String Pid, String Resources[]) {
		String Result = String.format("<-- AddResourcesToProject(ID:??). Add Resources to a Project.    URL:%s, PID:%s, Resources:%s", URL, Pid, Arrays.toString(Resources));
		String Status = Failed, Buffer[] = new String[] {"", ""};
		
		try {
			Buffer = CMAC_API_Endpoints.AddResources_API(URL, Pid, Resources);
			Result += AddToResult(Buffer);
	
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("output"), containsString("resourceDescription"), containsString(Pid)));
			for (int i = 0; i < Resources.length; i++) {
				assertThat(Buffer[1], containsString(Resources[i]));//check all resources are in the response.
			}
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
			try {
				Buffer[0] = Buffer[1].substring(Buffer[1].indexOf("pid\":") + 5, Buffer[1].indexOf(",\"projectname"));
				PidToDelete.add(Buffer[0]);//update this to store the pid of the created project
			}catch (Exception e) {}
		}
	}
	
	@AfterClass
	public void afterClass() {
		ResultsList.sort(String::compareToIgnoreCase);//sort the test case trace alphabetical
		
		int passedcnt = 0, failedcnt = 0;
		Support_Functions.PrintOut("\n\n@@ Passed Test Cases:", false);
		for(int i = 0; i < ResultsList.size(); i++) {
			if (ResultsList.get(i).contains(Passed)) {
				passedcnt++;
				Support_Functions.PrintOut(passedcnt + ")    " + ResultsList.get(i), false);
				ResultsList.remove(i);
				i--;
			}
		}
		
		if (ResultsList.size() > 0) {
			Support_Functions.PrintOut("\n\n@@ Failed Test Cases:", false);
			for(int i = 0; i < ResultsList.size(); i++) {
				failedcnt++;
				Support_Functions.PrintOut(passedcnt + failedcnt + ")    " + ResultsList.get(i), false);
			}
			Support_Functions.PrintOut("", false);
		}
		
		try {//print out the total execution time.
			Date ClassEnd = new Date();
			long diffInMillies = ClassEnd.getTime() - ClassStart.getTime();
			TimeUnit timeUnit = TimeUnit.SECONDS;
			Support_Functions.PrintOut("Execution Time in seconds: " + timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS) + "  (" + diffInMillies + " miliseconds)", false);
		}catch (Exception e) {}
		
		Support_Functions.PrintOut("Passed: " + passedcnt + " - " + "Failed: " + failedcnt + "\n", false);
		
		String ConsoleText = "";
		for (String s : Support_Functions.PrintLog){
			ConsoleText += s + System.lineSeparator();
		}
		
		Support_Functions.WriteToFile(LevelsToTest, ConsoleText, ".\\EclipseScreenshots\\CMAC\\"+ Support_Functions.CurrentDateTime() + " L" + LevelsToTest + " CMAC");
		Support_Functions.MoveOldLogs();
	}

	@AfterMethod
	public void afterMethod(ITestResult r) {
		try{
		    if(r.getStatus() == ITestResult.SUCCESS){
		    	Support_Functions.PrintOut(r.getName() + " passed " + Arrays.toString(r.getParameters()), false);
		    }else if(r.getStatus() == ITestResult.FAILURE) {
		    	Support_Functions.PrintOut(r.getName() + " failed " + Arrays.toString(r.getParameters()), false);
		    }else if(r.getStatus() == ITestResult.SKIP ){
		    	Support_Functions.PrintOut(r.getName() + " skipped " + Arrays.toString(r.getParameters()), false);
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Test(dataProvider = "TestData_dp", priority = 1)
	public void CreateProjectForTest(String URL, String Cookie, String ProjectName, String Resources[]) {
		String Result = String.format("<-- CreateProject to setup test data    URL:%s, Cookie:%s, ProjectName:%s, Resources%s", URL, Cookie, ProjectName, Arrays.toString(Resources));
		String Status = Failed, Buffer[] = new String[] {"", ""};
		
		try {
			Buffer = CMAC_API_Endpoints.CreateProject_API(URL, Cookie, ProjectName, Resources);
			Result += AddToResult(Buffer);
			
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("transactionId"), containsString("pid"), containsString("projectname"), containsString("cid"), containsString("accountNumber"), containsString("creationtime")));
			assertThat(Buffer[1], CoreMatchers.allOf(containsString("expirationdate"), containsString("resources"), containsString("key")));
			
			Status = Passed;
		}finally {
			Result += AddToResult(new String[]{Status, ""});
			ResultsList.add(Result);
			try {
				Buffer[0] = Buffer[1].substring(Buffer[1].indexOf("pid\":") + 5, Buffer[1].indexOf(",\"projectname"));
				PidToDelete.add(Buffer[0]);//update this to store the pid of the created project. Will be deleted later as part of the delete tests.
			}catch (Exception e) {}
		}
	}
	
	@Test(dataProvider = "TestData_dp", enabled = false)
	public void DeleteAllProject(String URL, String Cookie, String Retrieve_Project_Summary_URL) {
		String Result = "<-- Delete all projects, set. URL: " + URL;
		String Buffer[];
		ArrayList<String> Pids = PidOfAllProject(Retrieve_Project_Summary_URL, Cookie);
		
		try {
			for (String s : Pids){
				Buffer = CMAC_API_Endpoints.RetrieveProjectDetails_API(URL, Cookie, s);
				Result += AddToResult(Buffer);
				if (Buffer[1].contains("\"projectname\":\"Proj_DEL")) {//Only delete the projects that i added.
					Buffer = CMAC_API_Endpoints.DeleteProject_API(URL, s);
					Result += AddToResult(Buffer);
					assertThat(Buffer[1], containsString("transactionId"));
					assertThat(Buffer[1], endsWith("\"}"));
					//now check that the project has been removed
					Buffer = CMAC_API_Endpoints.RetrieveProjectDetails_API(URL, Cookie, s);
					Result += AddToResult(Buffer);
					assertThat(Buffer[1], CoreMatchers.allOf(containsString("code"), containsString("SYSTEM.UNEXPECTED.ERROR"), containsString("message"), containsString("GENERIC.ERROR")));//not sure if this is correct message
				}
			}
		}finally {
			Buffer = CMAC_API_Endpoints.RetrieveProjectSummary_API(Retrieve_Project_Summary_URL, Cookie);
			Result += AddToResult(new String[]{"Done", ""});
			ResultsList.add(Result);
		}
	}
	///////////////////////////////////METHODS//////////////////////////////////
	public static String AddToResult(String ReqResp[]) {
		if (ReqResp[0].contains(Passed) || ReqResp[0].contains(Failed)) {
			return System.lineSeparator() + ReqResp[0] + System.lineSeparator();
		}else {
			return System.lineSeparator() + "Request: " + ReqResp[0] + System.lineSeparator() + "Response: " + ReqResp[1];
		}
	}
	
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
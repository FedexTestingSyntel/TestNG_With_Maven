package API_Calls;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import TestingFunctions.Helper_Functions;

public class CMAC_API_Endpoints{
	
	public static String CreateProject_API(String URL, String OAuth_Token, String appUUID, String projectName, String latype, String laversion, String latimeStamp){
		String Request = "";
		
		try{
			HttpPost httppost = new HttpPost(URL);
			
			httppost.addHeader("Content-Type", "application/json");
			//httppost.addHeader("Authorization", "Bearer " + OAuth_Token);
			
			JSONObject MainBody = new JSONObject()
					.put("applicationUUID", appUUID)
					.put("latimeStamp", projectName) 
					.put("latype", latype) 
					.put("laversion", laversion) 
					.put("projectName", latimeStamp) ;
						
			Request = MainBody.toString();
			StringEntity params = new StringEntity(Request);
			httppost.setEntity(params);
			
			String Response = General_API_Calls.HTTPCall(httppost, Request);
			return Response;
		}catch (Exception e){
			e.printStackTrace();
			return e.toString();
		}
		//Sample request:
		
		//Sample response:

	}
	
	public static String DeleteProject_API(String URL, String OAuth_Token, String application_uuid){
		String Request = "";
		try{
			URL = URL.replace("{application_uuid}", application_uuid);
			HttpDelete  httpdel = new HttpDelete (URL);
			
			httpdel.addHeader("Content-Type", "application/json");
			//httppost.addHeader("Authorization", "Bearer " + OAuth_Token);
			
			String Response = General_API_Calls.HTTPCall(httpdel, Request);

			return Response;
		}catch (Exception e){
			e.printStackTrace();
			return e.toString();
		}
		//Sample request:
		
		//Sample response:

	}
	
	public static String RetrieveProjectDetails_API(String URL, String OAuth_Token, String organizationUUID, String applicationUUID){
		String Request = "";
		
		try{
			//The url should look like the below format.
			//{domain}/cmac/v3/projects/{organizationUUID}/{appUUID}
			URL = URL.replace("{organizationUUID}", organizationUUID);
			URL = URL.replace("{applicationUUID}", applicationUUID);
			HttpGet httpget = new HttpGet(URL);
			
			httpget.addHeader("Content-Type", "application/json");
			//httppost.addHeader("Authorization", "Bearer " + OAuth_Token);

			String Response = General_API_Calls.HTTPCall(httpget, Request);
			return Response;
		}catch (Exception e){
			e.printStackTrace();
			return e.toString();
		}
		//Sample request:
		
		//Sample response:

	}
		
	public static String RetrieveProjectSummary_API(String URL, String OAuth_Token, String organization_uuid){
		String Request = "";
		
		try{
			URL = URL.replace("{organization_uuid}", organization_uuid);
			HttpGet httpget = new HttpGet(URL);
			httpget.addHeader("Content-Type", "application/json");
			//httppost.addHeader("Authorization", "Bearer " + OAuth_Token);

			String Response = General_API_Calls.HTTPCall(httpget, Request);
			Helper_Functions.PrintOut("RetrieveProjectSummary Response : " + Response, true);
			return Response;
		}catch (Exception e){
			e.printStackTrace();
			return e.toString();
		}
		//Sample request:
		
		//Sample response:

	}
	
	public static String UpdateProject_API(String URL, String OAuth_Token, String applicationUUID, String latimeStamp, String latype, String laversion, String projectName){
		String Request = "";
		
		try{
			HttpPut httpput = new HttpPut (URL);
			
			httpput.addHeader("Content-Type", "application/json");
			//httppost.addHeader("Authorization", "Bearer " + OAuth_Token);
			
			JSONObject MainBody = new JSONObject()
					.put("applicationUUID", applicationUUID)
					.put("latimeStamp", latimeStamp)
					.put("latype", latype)
					.put("laversion", laversion)
					.put("projectName", projectName);
			
			Request = MainBody.toString();
			StringEntity params = new StringEntity(Request);
			httpput.setEntity(params);
			
			String Response = General_API_Calls.HTTPCall(httpput, Request);

			return Response;
		}catch (Exception e){
			e.printStackTrace();
			return e.toString();
		}
		//Sample request:
		
		//Sample response:

	}
}

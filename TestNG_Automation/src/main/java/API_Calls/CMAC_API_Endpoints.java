package API_Calls;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import Helper.Support_Functions;

public class CMAC_API_Endpoints {
	
	public static String[] CreateProject_API(String URL, String Cookie, String ProjectName, String resourceDescriptions[]){
		String Request = null;
		
		try{
			HttpPost httppost = new HttpPost(URL);
			JSONObject MainBody = new JSONObject()
					.put("projectname", ProjectName);
			for (int i = -1; i < resourceDescriptions.length; i++) {							
				JSONObject Resources = new JSONObject();
				if (i == -1) {
					Resources.put("resourceDescription", "$$");//need to fix this for when only single resource
				}else {
					Resources.put("resourceDescription", resourceDescriptions[i]);
				}
				MainBody.accumulate("resourcesInput", Resources);
			}
						
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Cookie", Cookie); // ex "fdx_login=ssoxdev1.8ffc.38829b44"
			
			Request = MainBody.toString().replace("{\"resourceDescription\":\"$$\"},", "");
			StringEntity params = new StringEntity(Request);
			httppost.setEntity(params);
			
			String Response = General_API_Calls.HTTPCall(httppost, Request);
			return new String[] {Request,Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
		 	//Sample request:   {"projectname": "fedexproject-1","resourcesInput":[{"resourceDescription": "Track Service"},{"resourceDescription": "Rate Service"}]}
		    //Sample response:  {"transactionId":"7121c0ec-1b8c-4a7a-a17d-90c6b7cbcfe6","output":{"pid":1006,"projectname":"Proj 082918T094916","cid":428,"accountnumber":113475006,"creationtime":"2018.08.29.14.49.17","expirationdate":"2018.09.28.23.37.56","resources":["Rate Service","Rate Service"],"key":"4LYKY7S7AA"}}

	}
	
	public static String[] RetrieveProjectDetails_API(String URL, String Cookie, String Pid){
		String Request = "";
		URL += Pid;
		
		try{
			HttpGet httpget = new HttpGet(URL);
			
			httpget.addHeader("Content-Type", "application/json");
			httpget.addHeader("Cookie", Cookie);

			String Response = General_API_Calls.HTTPCall(httpget, Request);
			return new String[] {Request,Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
		
		//example if no credentials on the project			(added on 09/28/18)
		//{"transactionId":"cc5a4deb-7acc-4965-b4fc-5f4e44521e68","output":{"pid":10127,"projectname":"Proj L2AdminBR605083560","credentialOutput":[],"resourcesoutput":["Track Service 0","Track Service 1","Track Service 2","Track Service 3","Track Service 4","Track Service 5","Track Service 6","Track Service 7"]}}

		//example if credentials are under the project		(added on 09/28/18)
		//{"transactionId":"981121d8-5d1c-4f39-bbbd-c0584a074be5","output":{"pid":10003,"projectname":"Proj_DEL8 092618T102447","credentialOutput":[{"projectid":10003,"creationtime":"2018.09.24.11.39.59","expirationdate":"2018.10.24.11.39.59","key":"KT81JESPBO","accountNumber":"784795640"}],"resourcesoutput":["Track Service 0","Track Service 1","Track Service 2","Track Service 3","Track Service 4","Track Service 5","Track Service 6","Track Service 7"]}}
	}
		
	public static String[] RetrieveProjectSummary_API(String URL, String Cookie){
		String Request = "";
		
		try{
			HttpGet httpget = new HttpGet(URL);
			httpget.addHeader("Content-Type", "application/json");
			httpget.addHeader("Cookie", Cookie);

			String Response = General_API_Calls.HTTPCall(httpget, Request);
			Support_Functions.PrintOut("RetrieveProjectSummary Response : " + Response, true);
			return new String[] {Request,Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
		//Sample response: {"transactionId":"aa628ada-d1b4-4c01-958d-51620c6c8333","output":{"projects":[{"pid":1001,"projectname":"IRCPROJECT"},{"pid":1002,"projectname":"Projerndjscbgt"},{"pid":1003,"projectname":"Projnssxurmdif"},{"pid":1004,"projectname":"Proj 082918T094823"},{"pid":1005,"projectname":"Proj 082918T094901"},{"pid":1006,"projectname":"Proj 082918T094916"},{"pid":1007,"projectname":"fedexproject-1"},{"pid":1008,"projectname":"Proj 082918T114400"},{"pid":1009,"projectname":"Proj 082918T144049"},{"pid":10010,"projectname":"Proj 082918T145853"},{"pid":100111,"projectname":"Proj 082918T150829"},{"pid":1001112,"projectname":"Proj 082918T154141"},{"pid":10011113,"projectname":"Proj 082918T154505"}]}}
	}
	
	public static String[] DeleteResource_API(String URL, String Pid, String Resource){
		String Request = "";
		URL += Pid + "/resources/" + Resource;
		//{{CMACURL}}/cmac/v3/projects/1001/resources/Rate Service
		
		try{
			HttpDelete  httpdel = new HttpDelete (URL);
			httpdel.addHeader("Content-Type", "application/json");

			String Response = General_API_Calls.HTTPCall(httpdel, Request);

			return new String[] {Request,Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
	}
	
	public static String[] DeleteCredentials_API(String URL, String Cid){
		String Request = "";
		URL += Cid;
		
		try{
			HttpDelete  httpdel = new HttpDelete (URL);
			
			httpdel.addHeader("Content-Type", "application/json");

			String Response = General_API_Calls.HTTPCall(httpdel, Request);

			return new String[] {Request,Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
	}
	
	public static String[] DeleteProject_API(String URL, String Pid){
		String Request = "";
		URL += Pid;
		
		try{
			HttpDelete  httpdel = new HttpDelete (URL);
			
			httpdel.addHeader("Content-Type", "application/json");

			String Response = General_API_Calls.HTTPCall(httpdel, Request);

			return new String[] {Request,Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
	}
	
	public static String[] UpdateProject_API(String URL, String Pid, String ProjectName){
		String Request = "";
		URL += Pid;
		
		try{
			HttpPut httpput = new HttpPut (URL);
			
			httpput.addHeader("Content-Type", "application/json");
			JSONObject MainBody = new JSONObject()
					.put("projectname", ProjectName);
			
			Request = MainBody.toString();
			StringEntity params = new StringEntity(Request);
			httpput.setEntity(params);
			
			String Response = General_API_Calls.HTTPCall(httpput, Request);

			return new String[] {Request,Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
		//Sampel response: {"transactionId":"aa628ada-d1b4-4c01-958d-51620c6c8333","output":{"projects":[{"pid":1001,"projectname":"IRCPROJECT"},{"pid":1002,"projectname":"Projerndjscbgt"},{"pid":1003,"projectname":"Projnssxurmdif"},{"pid":1004,"projectname":"Proj 082918T094823"},{"pid":1005,"projectname":"Proj 082918T094901"},{"pid":1006,"projectname":"Proj 082918T094916"},{"pid":1007,"projectname":"fedexproject-1"},{"pid":1008,"projectname":"Proj 082918T114400"},{"pid":1009,"projectname":"Proj 082918T144049"},{"pid":10010,"projectname":"Proj 082918T145853"},{"pid":100111,"projectname":"Proj 082918T150829"},{"pid":1001112,"projectname":"Proj 082918T154141"},{"pid":10011113,"projectname":"Proj 082918T154505"}]}}
	}
	
	public static String[] CreateCredentials_API(String URL, String Cookie, String Pid){
		String Request = "";
		URL += Pid + "/credentials";
		
		try{
			HttpPost httppost = new HttpPost(URL);						
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Cookie", Cookie);
			
			StringEntity params = new StringEntity(Request);
			httppost.setEntity(params);
			
			String Response = General_API_Calls.HTTPCall(httppost, Request);
			
			return new String[] {Request,Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
		 	//Sample request:   {"projectname": "fedexproject-1","resourcesInput":[{"resourceDescription": "Track Service"},{"resourceDescription": "Rate Service"}]}
		    //Sample response:  {"transactionId":"7121c0ec-1b8c-4a7a-a17d-90c6b7cbcfe6","output":{"pid":1006,"projectname":"Proj 082918T094916","cid":428,"accountnumber":113475006,"creationtime":"2018.08.29.14.49.17","expirationdate":"2018.09.28.23.37.56","resources":["Rate Service","Rate Service"],"key":"4LYKY7S7AA"}}

	}
	
	public static String[] AddResources_API(String URL, String Pid, String Resources[]){
		String Request = "";
		URL += Pid + "/resources";
		try{
			HttpPost httppost = new HttpPost(URL);			
			JSONObject MainBody = new JSONObject();
			for (int i = -1; i < Resources.length; i++) {							
				JSONObject JSONResources = new JSONObject();
				if (i == -1) {
					JSONResources.put("resourceDescription", "$$");//need to fix this for when only single resource
				}else {
					JSONResources.put("resourceDescription", Resources[i]);
				}
				MainBody.accumulate("resourcesInput", JSONResources);
			}
						
			httppost.addHeader("Content-Type", "application/json");
			Request = MainBody.toString().replace("{\"resourceDescription\":\"$$\"},", "");
			StringEntity params = new StringEntity(Request);
			httppost.setEntity(params);

			String Response = General_API_Calls.HTTPCall(httppost, Request);

			return new String[] {Request,Response};
		}catch (Exception e){
			e.printStackTrace();
			return new String[] {Request, e.toString()};
		}
		 	//Sample request:   {"projectname": "fedexproject-1","resourcesInput":[{"resourceDescription": "Track Service"},{"resourceDescription": "Rate Service"}]}
		    //Sample response:  {"transactionId":"7121c0ec-1b8c-4a7a-a17d-90c6b7cbcfe6","output":{"pid":1006,"projectname":"Proj 082918T094916","cid":428,"accountnumber":113475006,"creationtime":"2018.08.29.14.49.17","expirationdate":"2018.09.28.23.37.56","resources":["Rate Service","Rate Service"],"key":"4LYKY7S7AA"}}

	}
	
}

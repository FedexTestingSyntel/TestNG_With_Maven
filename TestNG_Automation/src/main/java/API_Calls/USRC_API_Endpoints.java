package API_Calls;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import Helper.*;

public class USRC_API_Endpoints {
	 //Takes in the userid, password, and the level and returns the cookies generated.
  	//{fdx_=[cookie], uuid};  0 is the cookie, 1 is the uuid     ex [fdx_login=ssodrt-cos2.97fb.364ddc40, gw0g0h657p]
  	public static String[] Login(String UserID, String Password, int Level){
  		try{
  			HttpClient httpclient = HttpClients.createDefault();
  				
  			String url = Support_Functions.LevelUrlReturn(Level) + "/userCal/user";
  			HttpPost httppost = new HttpPost(url);

  			JSONObject processingParameters = new JSONObject()
  				.put("anonymousTransaction", true)
  				.put("clientId", "WCDO")
  				.put("clientVersion", "3")
  				.put("returnDetailedErrors", false)
  				.put("returnLocalizedDateTime", false);
  				
  			JSONObject LogInRequest = new JSONObject()
  				.put("processingParameters", processingParameters)
  				.put("userName", UserID)
  				.put("password", Password);
  				
  			JSONObject main = new JSONObject()
  				.put("LogInRequest", LogInRequest);

  			String json = main.toString();
  				
  			httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");
  			httppost.addHeader("Accept", "application/json");
  			httppost.addHeader("Content-Type", "application/json");

  			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
  			urlParameters.add(new BasicNameValuePair("action", "LogIn"));
  			urlParameters.add(new BasicNameValuePair("format", "json"));
  			urlParameters.add(new BasicNameValuePair("version", "1"));
  			urlParameters.add(new BasicNameValuePair("locale", "en_US"));
  			urlParameters.add(new BasicNameValuePair("data", json));

  			httppost.setEntity(new UrlEncodedFormEntity(urlParameters));
  			Support_Functions.PrintOut("Get cookie from USRC for " + UserID + "/" + Password, true);
  			HttpResponse response = httpclient.execute(httppost);
  			Header[] headers = response.getAllHeaders();
  			//takes apart the headers of the response and returns the fdx_login cookie if present
  			String fdx_login = null, fcl_uuid = null;
  			for (Header header : headers) {
  				//String Test = header.getName() + "    " + header.getValue();PrintOut(Test, false);// used in debugging or if want to see other cookie values
  				if (header.getName().contentEquals("Set-Cookie") && header.getValue().contains("fdx_login")) {
  					fdx_login = header.toString().replace("Set-Cookie: ", "").replace("; domain=.fedex.com; path=/; secure", "");
  				}else if (header.getName().contentEquals("Set-Cookie") && header.getValue().contains("fcl_uuid")) {
  					fcl_uuid = header.toString().replace("Set-Cookie: fcl_uuid=", "").replace("; domain=.fedex.com; path=/", "");
  				}
  			}
  			return new String[] {fdx_login, fcl_uuid};
  		}catch (Exception e){
  			e.printStackTrace();
  			return null;
  		}
  	}
}

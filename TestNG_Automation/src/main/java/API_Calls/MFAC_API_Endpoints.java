package API_Calls;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

public class MFAC_API_Endpoints {
	
	public static String AddressVelocityAPI(String UserName, String OrgName, String URL, String OAuth_Token){
		String Request = "";
		
		try{
			HttpPost httppost = new HttpPost(URL);
			
			Request = new JSONObject()
		        .put("userName", UserName)
		        .put("orgName", OrgName).toString();
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Authorization", "Bearer " + OAuth_Token);

			StringEntity params = new StringEntity(Request.toString());
			httppost.setEntity(params);
			String Response = General_API_Calls.HTTPCall(httppost, Request);
		
			return Response;
		}catch (Exception e){
			e.printStackTrace();
			return e.toString();
		}
			/*
			Sample url:    https://apidev.idev.fedex.com:8443/security/v1/addresses/velocitycheck____http://mfacdev-cos-vip.test.cloud.fedex.com:9090/mfac/v3/addressVelocityCheck
		 	Sample request:   {"orgName":"FDM-POSTCARD-PIN","userName":"32t48NRQ0A-hOvDu6EgstCYeRiyKMDpKZzN"}
			Sample Successful Response: {"transactionId":"6166229d-c0cf-416a-92e2-0884927b9fa6","output":{"advice":"ALLOW"}}
			Sample velocity threshold Response: {"transactionId":"9e15ab6a-885b-45fb-8cbd-c641cfeac075","errors":[{"code":"DENY","message":"Unfortunately, too many failed attempts for registration have occurred. Please try again later."}]}
			*/
	}
	
	public static String IssuePinAPI(String UserName, String OrgName, String URL, String OAuth_Token){
		String Request = "";
		try{
			HttpPost httppost = new HttpPost(URL);

			Request = new JSONObject()
		        .put("userName", UserName)
		        .put("orgName", OrgName).toString();
			
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Authorization", "Bearer " + OAuth_Token);
			StringEntity params = new StringEntity(Request.toString());
			httppost.setEntity(params);
			String Response = General_API_Calls.HTTPCall(httppost, Request);
			return Response;
		}catch (Exception e){
			e.printStackTrace();
			return e.toString();
		}
		/*
		 sample URL: https://apidev.idev.fedex.com:8443/security/v1/pin____http://mfacdev-cos-vip.test.cloud.fedex.com:9090/mfac/v3/issuePIN
		 sample Request: {"orgName":"FDM-PHONE-PIN","userName":"rvjspdeayd-ousyqpuplxoutltxrhizxvad"}
		 sample Response: {"transactionId":"4d32b0b0-d3f9-44ff-8ebf-12f3f3a83712","output":{"pinOTP":"768752","pinExpirationDate":"09/16/2018 04:49 AM GMT"}}
		 */
	}
	
	public static String VerifyPinAPI(String UserName, String OrgName, String Pin, String URL, String OAuth_Token){
		String Request = "";
		try{
			HttpPost httppost = new HttpPost(URL);
			
			Request = new JSONObject()
		        .put("userName", UserName)
		        .put("orgName", OrgName)
		        .put("otpInput", Pin).toString();
			
			httppost.addHeader("Content-Type", "application/json");
			httppost.addHeader("Authorization", "Bearer " + OAuth_Token);
			StringEntity params = new StringEntity(Request.toString());
			httppost.setEntity(params);
			String Response = General_API_Calls.HTTPCall(httppost, Request);
			return Response;
		}catch (Exception e){
			e.printStackTrace();
			return e.toString();
		}
		/*
		 sample URL: https://apidev.idev.fedex.com:8443/security/v1/pin/verify____http://mfacdev-cos-vip.test.cloud.fedex.com:9090/mfac/v3/verifyPIN
		 sample Request: {"orgName":"FDM-PHONE-PIN","otpInput":"345713","userName":"gkpusdeqcu-pfwongykpssmcntpqgmalvqo"}
         sample Response: {"transactionId":"b9a85dc6-d374-4e64-b5f1-e8b24091e4fc","output":{"pinOTP":"219118","pinExpirationDate":"09/16/2018 05:09 AM GMT"}}
		 sample Response for invalid pin: {"transactionId":"0dda1468-eaca-414c-b6fd-e3208a488fe1","errors":[{"code":"PIN.FAILURE","message":"Unfortunately, you have entered an invalid PIN. Please try again."}]}
		 */
	}
		

}

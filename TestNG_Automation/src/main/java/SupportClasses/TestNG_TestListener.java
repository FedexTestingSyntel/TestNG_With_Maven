package SupportClasses;

import java.io.File;
import java.util.ArrayList;    //The below needed for tracking the status of the tests.
import java.util.Properties;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import SupportClasses.ThreadLogger;
import TestingFunctions.Helper_Functions;
//https://www.guru99.com/pdf-emails-and-screenshot-of-test-reports-in-selenium.html

public class TestNG_TestListener implements ITestListener{
	
	@Override
    public void onStart(ITestContext arg0) {
    }
	
	@Override
    public void onTestStart(ITestResult arg0) {
    }

    @Override
    public void onTestSuccess(ITestResult arg0) {
    	TestResults(arg0);
    }

    @Override
    public void onTestFailure(ITestResult arg0) {
    	TestResults(arg0);
    }

    @Override
    public void onTestSkipped(ITestResult arg0) {
    	TestResults(arg0);
    }

    @Override
    public void onFinish(ITestContext arg0) {
    	//Need to work on this to send out the email report.
    	//java.net.URL classUrl = this.getClass().getResource("com.sun.mail.util.TraceInputStream");
    	//System.out.println(classUrl.getFile());
    	//sendPDFReportByGMail("FedexTestingSyntel@gmail.com", "Test12345", "sean.kauffman.osv@fedex.com", "PDF Report", "");
    	
    	Helper_Functions.PrintOut("\n\n", false);
		
		for (int i = 0 ; i < ThreadLogger.ThreadLog.size(); i++) {
			Helper_Functions.PrintOut(i + ") " + ThreadLogger.ThreadLog.get(i), false);
		}
		
		try {
			Helper_Functions.MoveOldScreenshots();//cleans up the screenshots.
			Runtime.getRuntime().exec("taskkill /F /IM ChromeDriver.exe");//close out the old processes if still present.
			Helper_Functions.PrintOut("ChromeDriver.exe Cleanup Executed", true);
		} catch (Exception e) {}

		Helper_Functions.MoveOldLogs();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
        // TODO Auto-generated method stub
    }
    
    private void TestResults(ITestResult arg0) {
    	DriverFactory.getInstance().removeDriver();
    	
    	arg0.setAttribute("ExecutionLog", ThreadLogger.getInstance().ReturnLogString());
    	
    	ArrayList<String> CurrentLogs = ThreadLogger.getInstance().ReturnLogs();
    	String TestCompleteData = "";		
    	for (int i = 0; i < CurrentLogs.size(); i++){
		    TestCompleteData += CurrentLogs.get(i) + System.lineSeparator();
		}
    	ThreadLogger.ThreadLog.add(TestCompleteData + System.lineSeparator());
    }
    
    private static void sendPDFReportByGMail(String from, String pass, String to, String subject, String body) {
    	Properties props = System.getProperties();
    	String host = "smtp.gmail.com";
    	props.put("mail.smtp.starttls.enable", "true");
    	props.put("mail.smtp.host", host);
    	props.put("mail.smtp.user", from);
    	props.put("mail.smtp.password", pass);
    	props.put("mail.smtp.port", "587");
    	props.put("mail.smtp.auth", "true");
    	Session session = Session.getDefaultInstance(props);

    	MimeMessage message = new MimeMessage(session);
 
    	try {
    	    //Set from address
    		message.setFrom(new InternetAddress(from));
    		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

    		//Set subject
    		message.setSubject(subject);
    		message.setText(body);
    		BodyPart objMessageBodyPart = new MimeBodyPart();
    		objMessageBodyPart.setText("Please Find The Attached Report File!");
    		Multipart multipart = new MimeMultipart();
    		multipart.addBodyPart(objMessageBodyPart);
    		objMessageBodyPart = new MimeBodyPart();

    		//Set path to the pdf report file
    		String filename = System.getProperty("user.dir")+"\\test-output\\custom-emailable-report.html";

    		//Create data source to attach the file in mail
    		DataSource source = new FileDataSource(filename);
    		objMessageBodyPart.setDataHandler(new DataHandler(source));
    		objMessageBodyPart.setFileName(filename);
    		multipart.addBodyPart(objMessageBodyPart);
    		message.setContent(multipart);
    		Transport transport = session.getTransport("smtp");
    		transport.connect(host, from, pass);
    		transport.sendMessage(message, message.getAllRecipients());
    		transport.close();
    	}catch (Exception ae) {
    		ae.printStackTrace();
    	}
    }
    
}
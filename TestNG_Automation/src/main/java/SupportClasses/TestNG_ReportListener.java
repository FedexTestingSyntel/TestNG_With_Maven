package SupportClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;
import TestingFunctions.Helper_Functions;
/*
 * For when sending emails
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
 */

public class TestNG_ReportListener implements IReporter {
	
	int totalTestCount = 0;
	int totalTestPassed = 0;
	int totalTestFailed = 0;
	int totalTestSkipped = 0;
	
	//This is the customize emailabel report template file path.
	private static final String emailableReportTemplateFile = System.getProperty("user.dir") + "/src/main/java/XMLExecution/customize-emailable-report-template.html";
	
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		
		try{
			//Name of the test suit from the XML
			String Application = xmlSuites.get(0).getName().substring(0, 4);
			
			// Get content data in TestNG report template file.
			String customReportTemplateStr = this.readEmailabelReportTemplate();
			
			// Create custom report title.
			String customReportTitle = this.getCustomReportTitle(Application + " TestNG Report");
			
			// Create test suite summary data.
			String customSuiteSummary = this.getTestSuiteSummary(suites);
			
			// Create test methods summary data.
			String customTestMethodSummary = this.getTestMehodSummary(suites);
			
			// Replace report title place holder with custom title.
			customReportTemplateStr = customReportTemplateStr.replaceAll("\\$TestNG_Custom_Report_Title\\$", Matcher.quoteReplacement(customReportTitle));
			
			// Replace test suite place holder with custom test suite summary.
			customReportTemplateStr = customReportTemplateStr.replaceAll("\\$Test_Case_Summary\\$", Matcher.quoteReplacement(customSuiteSummary));
			
			// Replace test methods place holder with custom test method summary.
			customReportTemplateStr = customReportTemplateStr.replaceAll("\\$Test_Case_Detail\\$", Matcher.quoteReplacement(customTestMethodSummary));
			
			// Write replaced test report content to custom-emailable-report.html.
			String ReportName = Helper_Functions.CurrentDateTime() + " L" + DriverFactory.LevelsToTest + " " + Application + " Report";
			String ReportTitle = "L" + DriverFactory.LevelsToTest + " " + Application;
			outputDirectory = System.getProperty("user.dir") + "\\EclipseScreenshots\\" + Application + "\\" + ReportName;
			outputDirectory += String.format(" T%sP%sF%s", totalTestCount, totalTestPassed, totalTestFailed);
			File targetFile = new File(outputDirectory + ".html");
			System.out.println("Report Saved: " + outputDirectory + ".html");
			
			customReportTemplateStr = "<!DOCTYPE html><html><head><title>" + ReportTitle + "</title><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" /></head>" + customReportTemplateStr + "</html>";
			
			//Create folder directory for writing the report.
			String Folder = outputDirectory;
			FileWriter fw = null;
			try {
				Folder = outputDirectory.substring(0, outputDirectory.lastIndexOf("\\"));
				if (!(new File(Folder)).exists()) {
					new File(Folder).mkdir();
				}
				fw = new FileWriter(targetFile);
				fw.write(customReportTemplateStr);
			} catch (Exception e) {  
				System.out.println("Warning, Unable to create directory for: " + Folder);
			}finally {
				fw.flush();
				fw.close();
			}
			
			//CreatePDFReport(outputDirectory, customReportTemplateStr);
			//Need to work on this, something is most likely wrong with HTML format.    //java.lang.IllegalArgumentException: The number of columns in PdfPTable constructor must be greater than zero.
		
	    	//Need to work on this to send out the email report.
	    	//java.net.URL classUrl = this.getClass().getResource("com.sun.mail.util.TraceInputStream");
	    	//System.out.println(classUrl.getFile());
			/*
			String SenderEamil, SenderPassword, RecipientEmail;
			ArrayList<String[]> PersonalData = new ArrayList<String[]>();
			PersonalData = Helper_Functions.getExcelData(".\\Data\\Load_Your_UserIds.xls",  "Data");//create your own file with the specific data
			for(String s[]: PersonalData) {
				if (s[0].contentEquals("GMAIL")) {
					SenderEamil = s[1];
					SenderPassword = s[2];
				}else if(s[0].contentEquals("MYEMAIL")){
					RecipientEmail = s[1];
				}
			}
	    	sendPDFReportByGMail(SenderAddress, SenderPassword, RecipientEmail, ReportTitle, customReportTemplateStr, outputDirectory);
	    	*/
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/* Read template content. */
	private String readEmailabelReportTemplate(){
		StringBuffer retBuf = new StringBuffer();
		File file = null;
		FileReader fr = null;
		BufferedReader br = null;
		try {
			file = new File(TestNG_ReportListener.emailableReportTemplateFile);
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			String line = br.readLine();
			while(line!=null){
				retBuf.append(line);
				line = br.readLine();
			}
			
		}catch (Exception ex) {
			ex.printStackTrace();
		}finally{
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return retBuf.toString();
	}
	
	/* Build custom report title. */
	private String getCustomReportTitle(String title){
		StringBuffer retBuf = new StringBuffer();
		retBuf.append(title + " " + this.getDateInStringFormat(new Date()));
		return retBuf.toString();
	}
	
	/* Build test suite summary data. */
	private String getTestSuiteSummary(List<ISuite> suites){
		StringBuffer retBuf = new StringBuffer();
		
		try{
			for(ISuite tempSuite: suites){
				retBuf.append("<tr><td colspan=11><center><b>" + tempSuite.getName() + "</b></center></td></tr>");
				
				Map<String, ISuiteResult> testResults = tempSuite.getResults();
				
				for (ISuiteResult result : testResults.values()) {
					
					retBuf.append("<tr>");
					ITestContext testObj = result.getTestContext();
					totalTestPassed = testObj.getPassedTests().getAllMethods().size();
					totalTestSkipped = testObj.getSkippedTests().getAllMethods().size();
					totalTestFailed = testObj.getFailedTests().getAllMethods().size();
					totalTestCount = totalTestPassed + totalTestSkipped + totalTestFailed;
					
					/* Test name. */
					retBuf.append("<td>" + testObj.getName() + "</td>");
					/* Total method count. */
					retBuf.append("<td>" + totalTestCount + "</td>");
					/* Passed method count. */
					retBuf.append("<td bgcolor=green>" + totalTestPassed+ "</td>");
					/* Skipped method count. */
					retBuf.append("<td bgcolor=yellow>" + totalTestSkipped + "</td>");
					/* Failed method count. */
					retBuf.append("<td bgcolor=red>" + totalTestFailed + "</td>");
					/* Get browser type. */
					String browserType = tempSuite.getParameter("browserType");
					if(browserType==null || browserType.trim().length()==0){
						browserType = "Chrome";
					}
					/* Append browser type. */
					retBuf.append("<td>" + browserType + "</td>");
					/* Start Date*/
					Date startDate = testObj.getStartDate();
					retBuf.append("<td>" + this.getDateInStringFormat(startDate) + "</td>");
					/* End Date*/
					Date endDate = testObj.getEndDate();
					retBuf.append("<td>" + this.getDateInStringFormat(endDate) + "</td>");
					/* Execute Time */
					long deltaTime = endDate.getTime() - startDate.getTime();
					String deltaTimeStr = this.convertDeltaTimeToString(deltaTime);
					retBuf.append("<td>" + deltaTimeStr + "</td>");
					/* Include groups. */
					retBuf.append("<td>" + this.stringArrayToString(testObj.getIncludedGroups()) + "</td>");
					/* Exclude groups. */
					retBuf.append("<td>" + this.stringArrayToString(testObj.getExcludedGroups()) + "</td>");
					retBuf.append("</tr>");
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retBuf.toString();
	}

	/* Get date string format value. */
	private String getDateInStringFormat(Date date){
		StringBuffer retBuf = new StringBuffer();
		if(date==null){
			date = new Date();
		}
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		retBuf.append(df.format(date));
		return retBuf.toString();
	}
	
	/* Convert long type deltaTime to format hh:mm:ss:mi. */
	private String convertDeltaTimeToString(long deltaTime){
		StringBuffer retBuf = new StringBuffer();
		
		long milli = deltaTime;
		long seconds = deltaTime / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		retBuf.append(hours + ":" + minutes + ":" + seconds + ":" + milli);
		
		return retBuf.toString();
	}
	
	/* Get test method summary info. */
	private String getTestMehodSummary(List<ISuite> suites){
		StringBuffer retBuf = new StringBuffer();
		
		try{
			for(ISuite tempSuite: suites){
				retBuf.append("<tr><td colspan=7><center><b>" + tempSuite.getName() + "</b></center></td></tr>");
				
				Map<String, ISuiteResult> testResults = tempSuite.getResults();
				
				for (ISuiteResult result : testResults.values()) {
					
					ITestContext testObj = result.getTestContext();

					String testName = testObj.getName();
					
					/* Get failed test method related data. */
					IResultMap testFailedResult = testObj.getFailedTests();
					String failedTestMethodInfo = this.getTestMethodReport(testName, testFailedResult, false, false);
					retBuf.append(failedTestMethodInfo);
					
					/* Get skipped test method related data. */
					IResultMap testSkippedResult = testObj.getSkippedTests();
					String skippedTestMethodInfo = this.getTestMethodReport(testName, testSkippedResult, false, true);
					retBuf.append(skippedTestMethodInfo);
					
					/* Get passed test method related data. */
					IResultMap testPassedResult = testObj.getPassedTests();
					String passedTestMethodInfo = this.getTestMethodReport(testName, testPassedResult, true, false);
					retBuf.append(passedTestMethodInfo);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retBuf.toString();
	}
	
	/* Get failed, passed or skipped test methods report. */
	private String getTestMethodReport(String testName, IResultMap testResultMap, boolean passedReault, boolean skippedResult){
		ArrayList<String[]> ResultList = new ArrayList<String[]>();
		StringBuffer retStrBuf = new StringBuffer();
		StringBuffer sortingStrBuf = new StringBuffer();
		
		String resultTitle = testName;
		
		String color = "green";
		
		if(skippedResult){
			resultTitle += " - Skipped ";
			color = "yellow";
		}else if(passedReault){
			resultTitle += " - Passed ";
			color = "green";
		}else{
			resultTitle += " - Failed ";
			color = "red";
		}
		
		retStrBuf.append("<tr bgcolor=" + color + "><td colspan=7><center><b>" + resultTitle + "</b></center></td></tr>");
			
		Set<ITestResult> testResultSet = testResultMap.getAllResults();
			
		for(ITestResult testResult : testResultSet){
			String Application = "", testMethodName = "", startDateStr = "", executeTimeStr = "", paramStr = "", reporterMessage = "", exceptionMessage = "";
			
			//Get Application name, should be the same as the tesitng class name
			Application = testResult.getTestClass().getName();
			Application = Application.substring(Application.lastIndexOf(".") + 1, Application.length());
				
			//Get testMethodName
			testMethodName = testResult.getMethod().getMethodName();
				
			//Get startDateStr
			long startTimeMillis = testResult.getStartMillis();
			startDateStr = this.getDateInStringFormat(new Date(startTimeMillis));

			//Get Execute time.
			long deltaMillis = testResult.getEndMillis() - testResult.getStartMillis();
			executeTimeStr = this.convertDeltaTimeToString(deltaMillis);

			//Get parameter list.
			Object paramObjArr[] = testResult.getParameters();
			for(Object paramObj : paramObjArr){
				try {
					paramStr += paramObj.toString() + "<br />";
				}catch (Exception e) {
					paramStr += paramObj + "<br />";
				}
			}
				
			//This is a custom variable that is set in the TestListener for trace of execution.
			Object val = testResult.getAttribute("ExecutionLog");
			String ExecutionLog = val.toString().replaceAll("\n", "<br />");
			reporterMessage = ExecutionLog;
			
			//Get exception message.
			Throwable exception = testResult.getThrowable();
			if(exception!=null){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				exception.printStackTrace(pw);
				
				exceptionMessage = sw.toString();
			}
			
			sortingStrBuf.append("<tr bgcolor=" + color + ">");
			
			/* Add test class name. */
			sortingStrBuf.append("<td>" + Application + "</td>");
			/* Add test method name. */
			sortingStrBuf.append("<td>" + testMethodName + "</td>");
			/* Add start time. */
			sortingStrBuf.append("<td>" + startDateStr + "</td>");
			/* Add execution time. */
			sortingStrBuf.append("<td>" + executeTimeStr + "</td>");
			/* Add parameter. */
			sortingStrBuf.append("<td>" + paramStr + "</td>");
			/* Add reporter message. */
			sortingStrBuf.append("<td>" + reporterMessage + "</td>");
			/* Add exception message. */
			sortingStrBuf.append("<td>" + exceptionMessage + "</td>");;
			
			sortingStrBuf.append("</tr>");
			ResultList.add(new String[] {Application, sortingStrBuf.toString()});
		}
		
		Collections.sort(ResultList,new Comparator<String[]>() {
			public int compare(String[] strings, String[] otherStrings) {
				return strings[0].compareTo(otherStrings[0]);
			}
		});
		
		for (String[] sa : ResultList) {
			retStrBuf.append(sa[1]);
		}
		
		return retStrBuf.toString();
	}
	
	/* Convert a string array elements to a string. */
	private String stringArrayToString(String strArr[]) {
		StringBuffer retStrBuf = new StringBuffer();
		if(strArr!=null){
			for(String str : strArr){
				retStrBuf.append(str + " ");
			}
		}
		return retStrBuf.toString();
	}
	
	public void CreatePDFReport(String outputDirectory, String ReportName) {
		try {
			String path = outputDirectory + ".pdf";
			PdfWriter pdfWriter = null;

			// create a new document
			Document document = new Document();
			pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(path));
			document.open();
			
            // get Instance of the PDFWriter
            pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(path));
            //pdfWriter.setPdfVersion(PdfWriter.PDF_VERSION_1_4);
            pdfWriter.setLinearPageMode();
            pdfWriter.setFullCompression();


            // document header attributes
            document.addAuthor("GTM");
            document.addCreationDate();
            document.addProducer();
            document.addCreator("Sean Kauffman");
            document.addTitle(ReportName);
            document.setPageSize(PageSize.A4);

            // open document
            document.open();

            HTMLWorker htmlWorker = new HTMLWorker(document);

            String str = "";
            StringBuilder contentBuilder = new StringBuilder();
            BufferedReader in = null;

            //System.out.println("Html Content :");
            try {
                in = new BufferedReader(new FileReader(outputDirectory + ".html"));

                while ((str = in.readLine()) != null) {

                    contentBuilder.append(str);
                    //System.out.println(str);
                }
            } catch (Exception e) {
                System.out.print("HTML file close problem:" + e.getMessage());
            } finally {
                in.close();
                System.gc();
            }
            String content = contentBuilder.toString();

            htmlWorker.parse(new StringReader(content));
            document.close();
            pdfWriter.close();
            System.out.println("Report Saved as PDF: " + outputDirectory + ".pdf");
        }catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/*
	private static void sendPDFReportByGMail(String from, String pass, String to, String subject, String body, String FileName) {
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
    		Helper_Functions.PrintOut("Emial has been sent to " + pass, true);
    	}catch (Exception ae) {
    		ae.printStackTrace();
    	}
    }
    */

}
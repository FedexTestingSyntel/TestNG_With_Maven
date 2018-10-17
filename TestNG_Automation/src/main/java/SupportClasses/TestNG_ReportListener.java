package SupportClasses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
			outputDirectory = System.getProperty("user.dir") + "\\EclipseScreenshots\\" + Application + "\\" + ReportName;
			outputDirectory += String.format(" T%sP%sF%s", totalTestCount, totalTestPassed, totalTestFailed);
			File targetFile = new File(outputDirectory + ".html");
			System.out.println("Report Saved: " + outputDirectory + ".html");
			
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
			
			//customReportTemplateStr = "<!DOCTYPE html><html><head><title>Title</title><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" /></head>" + customReportTemplateStr + "</html>";
			//CreatePDFReport(outputDirectory, customReportTemplateStr);
			//Need to work on this, something is most likely wrong with HTML format.    //java.lang.IllegalArgumentException: The number of columns in PdfPTable constructor must be greater than zero.
		
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
					retBuf.append("<td>");
					retBuf.append(testObj.getName());
					retBuf.append("</td>");
					
					/* Total method count. */
					retBuf.append("<td>");
					retBuf.append(totalTestCount);
					retBuf.append("</td>");
					
					/* Passed method count. */
					retBuf.append("<td bgcolor=green>");
					retBuf.append(totalTestPassed);
					retBuf.append("</td>");
					
					/* Skipped method count. */
					retBuf.append("<td bgcolor=yellow>");
					retBuf.append(totalTestSkipped);
					retBuf.append("</td>");
					
					/* Failed method count. */
					retBuf.append("<td bgcolor=red>");
					retBuf.append(totalTestFailed);
					retBuf.append("</td>");
					
					/* Get browser type. */
					String browserType = tempSuite.getParameter("browserType");
					if(browserType==null || browserType.trim().length()==0){
						browserType = "Chrome";
					}
					
					/* Append browser type. */
					retBuf.append("<td>");
					retBuf.append(browserType);
					retBuf.append("</td>");
					
					/* Start Date*/
					Date startDate = testObj.getStartDate();
					retBuf.append("<td>");
					retBuf.append(this.getDateInStringFormat(startDate));
					retBuf.append("</td>");
					
					/* End Date*/
					Date endDate = testObj.getEndDate();
					retBuf.append("<td>");
					retBuf.append(this.getDateInStringFormat(endDate));
					retBuf.append("</td>");
					
					/* Execute Time */
					long deltaTime = endDate.getTime() - startDate.getTime();
					String deltaTimeStr = this.convertDeltaTimeToString(deltaTime);
					retBuf.append("<td>");
					retBuf.append(deltaTimeStr);
					retBuf.append("</td>");
					
					/* Include groups. */
					retBuf.append("<td>");
					retBuf.append(this.stringArrayToString(testObj.getIncludedGroups()));
					retBuf.append("</td>");
					
					/* Exclude groups. */
					retBuf.append("<td>");
					retBuf.append(this.stringArrayToString(testObj.getExcludedGroups()));
					retBuf.append("</td>");
					
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
			sortingStrBuf.append("<td>");
			sortingStrBuf.append(Application);
			sortingStrBuf.append("</td>");
			
			/* Add test method name. */
			sortingStrBuf.append("<td>");
			sortingStrBuf.append(testMethodName);
			sortingStrBuf.append("</td>");
			
			/* Add start time. */
			sortingStrBuf.append("<td>");
			sortingStrBuf.append(startDateStr);
			sortingStrBuf.append("</td>");
			
			/* Add execution time. */
			sortingStrBuf.append("<td>");
			sortingStrBuf.append(executeTimeStr);
			sortingStrBuf.append("</td>");
			
			/* Add parameter. */
			sortingStrBuf.append("<td>");
			sortingStrBuf.append(paramStr);
			sortingStrBuf.append("</td>");
			
			/* Add reporter message. */
			sortingStrBuf.append("<td>");
			sortingStrBuf.append(reporterMessage);
			sortingStrBuf.append("</td>");
			
			/* Add exception message. */
			sortingStrBuf.append("<td>");
			sortingStrBuf.append(exceptionMessage);
			sortingStrBuf.append("</td>");
			
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

}



/*
public class TestNG_ReportListener implements IReporter{
	   
    //For report generation
    @Override
    public void generateReport(List<XmlSuite> arg0, List<ISuite> arg1, String outputDirectory) {
    	// Second parameter of this method ISuite will contain all the suite executed.
        for (ISuite iSuite : arg1) {
        	//Get a map of result of a single suite at a time
            Map<String,ISuiteResult> results = iSuite.getResults();
            //Get the key of the result map
            Set<String> keys = results.keySet();
            //Go to each map value one by one
            for (String key : keys) {
            	//The Context object of current result
            	ITestContext context = results.get(key).getTestContext();
            	//Print Suite detail in Console
            	System.out.println("Suite Name->"+context.getName());
            	System.out.println("Report output Ditectory->"+context.getOutputDirectory());
            	System.out.println("Suite Name->"+ context.getSuite().getName());
            	System.out.println("Start Date Time for execution->"+context.getStartDate());
            	System.out.println("End Date Time for execution->"+context.getEndDate());
             
            	//Get Map for only failed test cases
            	IResultMap resultMap = context.getFailedTests();
            	//Get method detail of failed test cases
            	Collection<ITestNGMethod> failedMethods = resultMap.getAllMethods();
            	//Loop one by one in all failed methods
            	System.out.println("--------FAILED TEST CASE---------");
            	for (ITestNGMethod iTestNGMethod : failedMethods) {
            		//Print failed test cases detail
            		System.out.println("TESTCASE NAME->"+iTestNGMethod.getMethodName()
                        +"\nDescription->"+iTestNGMethod.getDescription()
                        +"\nPriority->"+iTestNGMethod.getPriority()
                        +"\n:Date->"+new Date(iTestNGMethod.getDate()));
            	}
            }
        }
    }
}
*/

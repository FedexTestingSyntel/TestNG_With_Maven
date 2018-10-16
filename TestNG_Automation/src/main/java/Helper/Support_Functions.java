package Helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.*;


public class Support_Functions {

	public static ArrayList<String> PrintLog = new ArrayList<String>();
	
	public static void WriteToFile(String Levels, String Text, String Path){
		//this is only applicable if configured for tests that print the summery table.
		if (Text.contains("Passed: ") && Text.contains(" - Failed: ")) {
			String Results = Text.substring(Text.indexOf("Passed: "), Text.length());
			if (Results.length() < 28 && Results.contains(" - Failed: ")) {//check in case pulled to many characters
				Results = Results.replace("Passed: ", " - P");
				Results = Results.replace(" - Failed: ", "F");
				Results = Results.replaceAll("\r", "");
				Results = Results.replaceAll("\n", "");
				Path = Path.replace(".txt", "");
				Path += Results;
			}
		}
		if (!Path.contains(".txt")) {
			Path+= ".txt";
		}
		File newTextFile = new File(Path);
		FileWriter fw;
		try {
			String Folder = Path.substring(0, Path.lastIndexOf("\\"));
			if (!(new File(Folder)).exists()) {
				new File(Folder).mkdir();
			}
			newTextFile.createNewFile();
			fw = new FileWriter(newTextFile);
			fw.write(Text);
	        fw.close();
	        PrintOut("File Created: " + Path, true);
		} catch (Exception e) {  
			PrintOut("Failure writing to file.", true);
			e.printStackTrace();
		}
	}
	
	public static void MoveOldLogs(){
    	String main_Source = "." + File.separator + "EclipseScreenshots";
    	File main_dir = new File(main_Source);
    	if(main_dir.isDirectory()) {
    	    File[] content_main = main_dir.listFiles();
    	    for(int j = 0; j < content_main.length; j++) {
    	    	try {
    	        	String PathSource = content_main[j].getPath();
    	        	String PathDestination = content_main[j].getPath() + File.separator + "Old";
    	        	File source_dir = new File(PathSource);
    	        	File destination_dir = new File(PathDestination);
    	        	int year = Calendar.getInstance().get(Calendar.YEAR);// this will be used to check if the file in question should be moved. Please note that the comparison is local time vs GTM time so may be a differnce as to what day should be archived.
    	        	int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
    	        	int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    	        	if(source_dir.isDirectory()) {
    	        	    File[] content_subfolder = source_dir.listFiles();
    	        	    for(int i = 0; i < content_subfolder.length; i++) {
    	        	    	if (content_subfolder[i].isDirectory()){
    	        	    		break; //do not need to seach within subfolders.
    	        	    	}
    	        	    	
    	        	    	try {
    	        	        	if (!destination_dir.exists()) {// if the directory does not exist, create it
    	        	        	    PrintOut("Creating directory: " + destination_dir.getName(), true);
    	        	        	    try{
    	        	        	    	destination_dir.mkdir();
    	        	        	        PrintOut(PathDestination + " DIR created", true);  
    	        	        	    } catch(SecurityException se){}        
    	        	        	}
    	        	        	BasicFileAttributes attr = Files.readAttributes(Paths.get(content_subfolder[i].getPath()), BasicFileAttributes.class);
    	        	        	String creationtime = " " + attr.creationTime();
    	        	        	if (!creationtime.contains(Integer.toString(year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day))){//if the file was created before today
    	        	        		String localPathDestination = PathDestination + File.separator + creationtime.substring(1, 8);
    	        	        		File old_month_dir = new File(localPathDestination);
    	        	        		if (!old_month_dir.exists()) {// if the directory does not exist for the old month then create it
    	            	        	    PrintOut("Creating directory: " + old_month_dir.getName(), true);
    	            	        	    try{
    	            	        	    	old_month_dir.mkdir();
    	            	        	        PrintOut(localPathDestination + " DIR created", true);  
    	            	        	    } catch(SecurityException se){}
    	            	        	}
    	        	        		//Files.move(from, to, CopyOption... options).
    	        	        		Files.move(Paths.get(content_subfolder[i].getPath()), Paths.get(content_subfolder[i].getPath().replace(PathSource, localPathDestination)), StandardCopyOption.REPLACE_EXISTING);
    	        	        	}
    	    				}catch (Exception e) {}
    	        	    }
    	        	}
				}catch (Exception e) {}
    	    }//end for finding each individual app
    	}
    }//end MoveOldLogs
    
	public static String CurrentDateTime() {
		Date curDate = new Date();
    	SimpleDateFormat Dateformatter = new SimpleDateFormat("MMddyy");
    	SimpleDateFormat Timeformatter = new SimpleDateFormat("HHmmss");
    	return Dateformatter.format(curDate) + "T" + Timeformatter.format(curDate);
	}
	
	public static String PrintOut(String Text, boolean TimeStamp){
		if (TimeStamp) {
			String CurrentTime = Support_Functions.CurrentDateTime();
			Thread currentThread = Thread.currentThread();
			long ThreadID = currentThread.getId();//not sure if should keep this
			Text = ThreadID + " " + CurrentTime + ": " + Text;
			
		}
		System.out.println(Text); 
		
		Text = Text.replaceAll("\n", System.lineSeparator());
		PrintLog.add(Text);
		return "\n" + Text + System.lineSeparator();
	}
	
	public static String getRandomString(int Length) {
        //String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		String SALTCHARS = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < Length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
	
	public static ArrayList<String[]> getExcelData(String fileName, String sheetName) {
		//Note, may face issues if the file is an .xlsx, save it as a xls and works

		ArrayList<String[]> data = new ArrayList<>();
		try {
			FileInputStream fs = new FileInputStream(fileName);
			Workbook wb = Workbook.getWorkbook(fs);
			Sheet sh = wb.getSheet(sheetName);
			
			int totalNoOfCols = sh.getColumns();
			int totalNoOfRows = sh.getRows();
			
			for (int i= 0 ; i < totalNoOfRows; i++) { //change to start at 1 if want to ignore the first row.
				String buffer[] = new String[totalNoOfCols];
				for (int j=0; j < totalNoOfCols; j++) {
					buffer[j] = sh.getCell(j, i).getContents();
				}
				data.add(buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static void writeExcelData(String fileName, String sheetName, String AddressDetails[], int ColumntoWrite) {
		try {
			FileInputStream fs = new FileInputStream(fileName);
			Workbook wb = Workbook.getWorkbook(fs);
			Sheet sh = wb.getSheet(sheetName);
			
			int totalNoOfCols = sh.getColumns();
			int totalNoOfRows = sh.getRows();
			
			String ColumNames[] = new String[totalNoOfCols];;
			for (int j=0; j < totalNoOfCols; j++) {
				ColumNames[j] = sh.getCell(j, 0).getContents();
			}
			
			for (int i= 1 ; i < totalNoOfRows; i++) { 
				String buffer[] = new String[totalNoOfCols];
				for (int j=0; j < totalNoOfCols; j++) {
					buffer[j] = sh.getCell(j, i).getContents();
				}
				
				// AddressDetails Example =  {"10 FEDEX PKWY 2nd FL", "", "COLLIERVILLE", "Tennessee", "TN", "38017", "US"});
				//Address line 1, address line 2, City, StateName, StateCode, ZipCode, CountryCode

				if (buffer[0] == AddressDetails[1] && buffer[1] == AddressDetails[2] && buffer[2] == AddressDetails[3] && buffer[3] == AddressDetails[4] && buffer[4] == AddressDetails[5] && buffer[5] == AddressDetails[6]) {
					WritableWorkbook copy = Workbook.createWorkbook(new File("temp.xls"), wb);
					WritableSheet sheet2 = copy.getSheet(sheetName); 
					WritableCell cell = sheet2.getWritableCell(ColumntoWrite, i); 
					
					if (cell.getType() == CellType.LABEL) { 
						Label l = (Label) cell; 
						l.setString("modified cell"); 
					}
					copy.write(); 
					copy.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
 
  	
  	public static String LevelUrlReturn(int Level) {
  		String LevelURL = null;
  		switch (Level) {
      		case 1:
      			LevelURL = "https://wwwbase.idev.fedex.com"; break;
      		case 2:
      			LevelURL = "https://wwwdev.idev.fedex.com";  break;
      		case 3:
      			LevelURL = "https://wwwdrt.idev.fedex.com"; break;
      		case 4:
      			LevelURL = "https://wwwstress.dmz.idev.fedex.com"; break;
      		case 5:
      			LevelURL = "https://wwwbit.idev.fedex.com"; break;
      		case 6:
      			LevelURL = "https://wwwtest.fedex.com"; break;
      		case 7:
      			LevelURL = "https://www.fedex.com"; break;
  		}
  		return LevelURL;
  	}
    

}//End Class

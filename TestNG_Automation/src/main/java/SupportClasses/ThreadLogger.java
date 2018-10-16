package SupportClasses;

import java.util.ArrayList;

public class ThreadLogger{
	
	private static ThreadLogger instance = new ThreadLogger();
	public static ArrayList<String[]> ResultsList = new ArrayList<String[]>();//the results of a single test case
	public static ArrayList<String> ThreadLog = new ArrayList<String>();//The overall status and all text printed out
	
	private ThreadLogger(){
		//Do-nothing..Do not allow to initialize this class from outside
	}
	
	public static ThreadLogger getInstance(){
		return instance;
	}

	ThreadLocal<ArrayList<String>> myStringArrayLocal = new ThreadLocal<ArrayList<String>>() {
	   @Override 
	   protected ArrayList<String> initialValue() {
		   return new ArrayList<String>();
	   }
   };
   
   public void UpdateLogs(String s){ //add items to the thread instance 
	   myStringArrayLocal.get().add(s);
   }

   public ArrayList<String> ReturnLogs(){ //Returns the current logs
	  return myStringArrayLocal.get();
   }
   
   public String ReturnLogString() {
	   String Log = "";
	   for (String s: myStringArrayLocal.get()) {
		   Log += s + "\n";
	   }
	   return Log;
   }
}
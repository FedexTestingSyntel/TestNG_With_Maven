package SupportClasses;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DriverFactory{
	
	private static DriverFactory instance = new DriverFactory();
	public static int BrowserCurrent = 0, BrowserLimit = 3;
	public static String LevelsToTest;
	public static int WaitTimeOut = 30;
	private static boolean CloseBrowser = true;//when set to false will not close the browser when a test case fails. This will be used when manually debugging an issue.
	
	private DriverFactory(){
		//Do-nothing..Do not allow to initialize this class from outside
	}
	
	public static DriverFactory getInstance(){
		return instance;
	}

   ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>(){ // thread local driver object for webdriver
	   @Override
	   synchronized protected WebDriver initialValue(){
		   while (BrowserCurrent >= BrowserLimit){
			   try {
				   Thread.sleep(5000);
			   } catch (InterruptedException e) {}
		   }
		   //lock.lock();
		   WebDriver Locdriver = null;
		  // String BrowerType = "Chrome";
		   System.setProperty("webdriver.chrome.driver",".\\chromedriver.exe");//make sure driver in the project folder
		   
		   ChromeOptions options = new ChromeOptions();
		   options.addArguments("disable-infobars"); //this is used to remove the "Chrome is being controlled by automated test software" banner
		   options.addArguments("start-maximized");   //options.addArguments("--start-fullscreen");
			
		   HashMap<String, Object> chromeOptionsMap = new HashMap<String, Object>();
		   chromeOptionsMap.put("plugins.plugins_disabled", new String[] {"Chrome PDF Viewer"});
		   chromeOptionsMap.put("plugins.always_open_pdf_externally", true);
		   options.setExperimentalOption("prefs", chromeOptionsMap);
		   String downloadFilepath = System.getProperty("user.dir") + "\\EclipseScreenshots\\Download"; //need to come back later and make this dynamic 
		   chromeOptionsMap.put("download.default_directory", downloadFilepath);
		   DesiredCapabilities cap = DesiredCapabilities.chrome();
		   cap.setCapability(ChromeOptions.CAPABILITY, chromeOptionsMap);
		   cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		   cap.setCapability(ChromeOptions.CAPABILITY, options);

		   Locdriver = new ChromeDriver(options);
		   Locdriver.manage().timeouts().implicitlyWait(WaitTimeOut, TimeUnit.SECONDS);
		   		
		   //Helper_Functions.PrintOut("New driver created. " + BrowerType, true);
		   BrowserCurrent++;
		   //lock.unlock();
		   return Locdriver;
	   }
   };
   	   
   public WebDriver getDriver(){ // call this method to get the driver object and launch the browser
	   return driver.get();
   }

   public void removeDriver(){ // Quits the driver and closes the browser
	   if (BrowserCurrent > 0 && CloseBrowser) {
		    driver.get().quit();
		    driver.remove();
		    BrowserCurrent--;
	   }else if (BrowserCurrent > 0 && CloseBrowser == false){
		   System.out.println("Browser not closed, manaully debugging");
	   }

   }
   
   public synchronized void updateDriverLimit(){
	   BrowserLimit += 1;
   }
    
    //WebDriverWait wait = new WebDriverWait(DriverFactory.getInstance().getDriver(), 3);
    
    ThreadLocal<WebDriverWait> wait = new ThreadLocal<WebDriverWait>(){ // thread local driver object for webdriver
 	   @Override
 	   synchronized protected WebDriverWait initialValue(){
 		   return new WebDriverWait(DriverFactory.getInstance().getDriver(), WaitTimeOut);
 	   }
    };
    
    public WebDriverWait getDriverWait(){ // call this method to get the driver object and launch the browser
 	   return wait.get();
    }
    
    ThreadLocal<WebDriverWait> quickwait = new ThreadLocal<WebDriverWait>(){ // thread local driver object for webdriver
  	   @Override
  	   synchronized protected WebDriverWait initialValue(){
  		   return new WebDriverWait(DriverFactory.getInstance().getDriver(), 1);
  	   }
     };
     
     public WebDriverWait getDriverQuickWait(){ // call this method to get the driver object and launch the browser
  	   return quickwait.get();
     }
}
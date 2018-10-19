package SupportClasses;

import java.io.FileInputStream;
import java.util.Properties;

public class PropertyManager {


	private static PropertyManager instance;
	private static final Object lock = new Object();
	private static String propertyFilePath = System.getProperty("user.dir") + "//src//test//java//configs//Configurations.properties";
	private static String url;

	//Create a Singleton instance. We need only one instance of Property Manager.
	public static PropertyManager getInstance () {
		if (instance == null) {
			synchronized (lock) {
				instance = new PropertyManager();
				instance.loadData();
           }
       }
       return instance;
   }

	//Get all configuration data and assign to related fields.
	private void loadData() {
		//Declare a properties object
		Properties prop = new Properties();

		//Read configuration.properties file
		try {
			prop.load(new FileInputStream(propertyFilePath));
			//prop.load(this.getClass().getClassLoader().getResourceAsStream("configuration.properties"));
		} catch (Exception e) {
			System.out.println("Configuration properties file cannot be found");
		}

       //Get properties from configuration.properties
       url = prop.getProperty("url");

	}

	public String getURL () {
		return url;
	}
}


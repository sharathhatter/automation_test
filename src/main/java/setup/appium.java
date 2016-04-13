package setup;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.remote.DesiredCapabilities;

public class appium {
	public static AppiumDriver<MobileElement> driver;
	
	public static AppiumDriver<MobileElement> platform() throws MalformedURLException{			
		AndroidSetup();
		return driver;			
	}
	 public static void AndroidSetup() throws MalformedURLException{
	    	//Set up desired capabilities and pass the Android app-activity and app-package to Appium
			DesiredCapabilities capabilities = new DesiredCapabilities();
			//capabilities.setCapability("BROWSER_NAME", "Android");
			capabilities.setCapability("VERSION", "4.4.1"); 
			capabilities.setCapability("deviceName","Emulator");
			capabilities.setCapability("platformName","Android");   
		    capabilities.setCapability("appPackage", "com.bigbasket.mobileapp");
		    capabilities.setCapability("noReset", true);
		// This package name of your app (you can get it from apk 4info app)
			capabilities.setCapability("appActivity","com.bigbasket.mobileapp.activity.SplashActivity"); // This is Launcher activity of your app (you can get it from apk info app)
		//Create RemoteWebDriver instance and connect to the Appium server
		 //It will launch the Calculator App in Android Device using the configurations specified in Desired Capabilities
		    driver = new AndroidDriver<MobileElement>(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);			    
	    }
}

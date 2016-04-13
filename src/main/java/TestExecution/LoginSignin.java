package TestExecution;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;

import java.net.MalformedURLException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import setup.appium;
import IntializePageobjectClass.LoginIntialize;


public class LoginSignin {		
	public static AppiumDriver<MobileElement> driver;
	public static String filename = "user_details.property";
	@BeforeSuite
	public PropertyReader readproperty(){
		PropertyReader pr = new PropertyReader(filename); 
		pr.read_property();
		return pr;
		
	}
	@BeforeClass
	 public void setupdriver() throws MalformedURLException{		
		driver= appium.platform();
	}		
	@Test
	public void signup() throws MalformedURLException{
		System.out.println("test");
		LoginIntialize loginit = new LoginIntialize(driver);
		if(loginit.signupbuttondisplayed() == true){
			loginit.signUp();}		
		}
	@AfterClass
	 public void tearDownAppium(){
        driver.quit();
    }
	/*public void testsignup() throws MalformedURLException{		
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
    driver = new AppiumDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);			
}*/

}

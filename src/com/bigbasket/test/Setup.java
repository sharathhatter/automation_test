package com.bigbasket.test;
import io.appium.java_client.AppiumDriver;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
//import org.openqa.selenium.remote.CapabilityType;

public class Setup {
public static AppiumDriver driver;
public static  WebDriverWait wait;

public  void setUp() throws MalformedURLException{
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
	    driver = new AppiumDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
}
//btnLogin
public static void main(String args[]){	
	try {			
		Setup s = new Setup();
		 s.setUp();
		home_page hp = new home_page(driver);
//		hp.TopLevelCategory();
		driver = hp.TopLevelCategoryLayout(driver);
//		Login log = new Login();
//		log.login_fields(driver);		
//		Add_to_Basket atb = new Add_to_Basket();
//		atb.srch_icon_clk(driver, "Anand sweets");
//		driver = ClickBasketIcon.clk_basket(driver);
//		Checkout abc = new Checkout();
//		abc.co_address(driver);
//		Payment payment = new Payment();
//		payment.co_payu(driver);		
//		APIStart apis = new APIStart();
//		apis.testing("http://dev1.bigbasket.com/mapi/v2.0.0/cities/");
//		URLReader ured = new URLReader();		
//		APIStart api = new APIStart();
//		api.excutePost("https:\\testaws.bigbasket.com", urlParameters)
//		driver = s.setUp(driver);		
//		driver= home_page.category_count(driver);
//		driver = Login.login_user(driver);
//		driver = Add_to_Basket.srch_icon_clk(driver, null);		
//		driver = Register_user.register(driver);
//		driver = Register_user.enter_creds(driver);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
/*
@AfterClass
public void teardown(){
	//close the app
	driver.quit();
}*/
}

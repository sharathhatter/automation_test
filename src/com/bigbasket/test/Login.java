package com.bigbasket.test;

import io.appium.java_client.AppiumDriver;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Login extends Setup {	
	 AppiumDriver driver; 
	 WebElement signupLayout ; 
	 WebElement login_btn ;
	 WebElement signUpBtn; 
	 String username = "ankur@bigbasket.com";
	 String password = "password";
	 String username_1 = "bbxxxx-ankur@bigbasket.com";
	 public WebDriverWait wait;	
	 List <WebElement> signupcreds = null;
	 String fName = "Ankur";
	 String lName = "Joshi";
	 String email = "AnkurJoshi8@bigbasket.com";
	 String passwordSignUp = "Password";	 
	 
	 public void Setup (AppiumDriver driver1){
		 this.driver = driver1;
	 }	 
	 //This Function will check if suer wanna do sign up or login
	 
	 public AppiumDriver userChoiceOfAction(){		 		 
		 WebElement button_layout=driver.findElement(By.id("com.bigbasket.mobileapp:id/layoutLoginButtons"));	
		 login_btn=button_layout.findElement(By.id("com.bigbasket.mobileapp:id/btn_login"));
		 signUpBtn=button_layout.findElement(By.id("com.bigbasket.mobileapp:id/btnRegister"));
		 return driver;
	 }	 
	 public  AppiumDriver loginEnterCreds(AppiumDriver driver1) throws Exception {	
		 try { 
		 driver = driver1 ; 		
		 driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		 WebElement button_layout=driver.findElement(By.id("com.bigbasket.mobileapp:id/layoutLoginButtons"));
		 WebElement login_btn=button_layout.findElement(By.id("com.bigbasket.mobileapp:id/btn_login"));
		 System.out.println("Abt to click login");
		 login_btn.click();
		 //master email id : com.bigbasket.mobileapp:id/email_input
	   	WebElement email=driver.findElement(By.id("com.bigbasket.mobileapp:id/email_input"));
	   		email.clear();
	   		email.sendKeys(username);	   		
	   WebElement pwd=driver.findElement(By.id("com.bigbasket.mobileapp:id/edit_text_passwd"));
	   		pwd.clear();
	   		pwd.sendKeys("password");	   		
	   WebElement login_button = driver.findElement(By.id("com.bigbasket.mobileapp:id/btn_login"));
	   		login_button.click();
	   		driver = skipTutorial(driver);
	   		return driver;
//			driver.quit();
		 }
		 catch (Exception e){
			 System.out.println("Error on Login");			 
			 driver.quit();			 
		 }
	   return driver;
	}	 
	 public AppiumDriver skipTutorial(AppiumDriver driver1) throws Exception{
		 try { 
			 	driver = driver1;
				driver.findElement(By.id("com.bigbasket.mobileapp:id/lblSkip")).isDisplayed();
				System.out.println("Login passed");
				WebElement skip_tutorial = driver.findElement(By.id("com.bigbasket.mobileapp:id/lblSkip"));
				skip_tutorial.click();   
			}
			catch (Exception e){				
				loginEnterCreds(driver);
				driver.findElement(By.name("OK"));
				System.out.println("login Failed");  
			}
		return driver;		 
	 }	 
	 public void signUp(){
//		 com.bigbasket.mobileapp:id/btnRegister
		 WebElement signupLayout = driver.findElement(By.id("com.bigbasket.mobileapp:id/login_form"));
		 signupcreds = signupLayout.findElements(By.className("TextInputLayout"));		 
		 signupcreds.get(0).sendKeys(fName);
		 signupcreds.get(1).sendKeys(lName);
		 signupcreds.get(2).sendKeys(email);
		 signupcreds.get(3).sendKeys(passwordSignUp);
		 signupcreds.get(4).click();
		 selectLocation();
		 driver = selectLocation();
		 WebElement signUpBtn = signupLayout.findElement(By.id("com.bigbasket.mobileapp:id/editTextCurrentLocation"));
		 signUpBtn.click(); 
	 }
	 public AppiumDriver selectLocation(){
		 WebElement locationNotEnabledLayout = driver.findElement(By.className("android.widget.FrameLayout"));
		 WebElement btnLayout = locationNotEnabledLayout.findElement(By.id("com.bigbasket.mobileapp:id/buttonPanel"));
		 List <WebElement> btnEnableLocation  = btnLayout.findElements(By.className("android.widget.Button"));
		 btnEnableLocation.get(1).click();		 
//		 Linear layout signup  : android.widget.LinearLayout;
//	 	 First name : com.bigbasket.mobileapp:id/editTextFirstName;
//	 	 Lastname : com.bigbasket.mobileapp:id/editTextLastName;
//	 	 email : com.bigbasket.mobileapp:id/email_input;
//	 	 password : com.bigbasket.mobileapp:id/edit_text_passwd; 
//	 	 select location : com.bigbasket.mobileapp:id/editTextCurrentLocation;
//		 USE CURRENT LOCATION : com.bigbasket.mobileapp:id/btnToCurrentLocation;
//		 SELECT LOCATION MANUALLY : com.bigbasket.mobileapp:id/btnChooseLocation;
//		 class : android.widget.FrameLayout;
//		 Enable Location  :com.bigbasket.mobileapp:id/alertTitle ; 
//		 Cancel : android:id/button2;
//		 Enable : android:id/button1; 
//		 com.android.packageinstaller:id/permission_allow_button
		return driver;
		 
//		 social loging layout g+ select account = android.widget.FrameLayout;
	 }
	 
	 public void signUpGoogle(){
		 WebElement gLogin = signupLayout.findElement(By.id("com.bigbasket.mobileapp:id/plus_sign_in_button"));
		 gLogin.click(); 
		 }
	 
	 public void signUpFacebook(){
		 WebElement fbLogin = signupLayout.findElement(By.id("com.bigbasket.mobileapp:id/btn_fb_login"));
		 fbLogin.click(); 
		 } 
} 


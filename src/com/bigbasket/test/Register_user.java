package com.bigbasket.test;

import io.appium.java_client.AppiumDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Register_user {

static String username= "Ankur";
static String lastname="Joshi";
static String pwd= "1234567";
static String e_mail = "xyz@bigbasket.com";
static AppiumDriver driver;
	
	public static AppiumDriver register(AppiumDriver driver1){
	driver = driver1;
	WebElement Register=driver.findElement(By.id("btnRegister"));
	Register.click();	
	return driver;
}
	
	public static AppiumDriver enter_creds(AppiumDriver driver1){		
		driver = driver1 ; 		
		WebElement first_name = driver.findElement(By.id("editTextFirstName"));
			first_name.sendKeys(username);
		WebElement last_name = driver.findElement(By.id("editTextFirstName"));
			last_name.sendKeys(lastname);
		WebElement email = driver.findElement(By.id("emailInput"));
			email.sendKeys(e_mail);		
		WebElement password = driver.findElement(By.id("editTextPasswd"));
			password.sendKeys(pwd);
			/*WebElement city_spinner = driver.findElement(By.id("editTextChooseCity"));
			city_spinner.click();
			*/			
			WebElement city_spinner = driver.findElement(By.id("txtChooseLocation"));
			city_spinner.click();			
			return driver1;
			
//		editTextFirstName;
//		editTextFirstName;
//		emailInput;
//		editTextPasswd;
//		editTextChooseCity;
	}
}

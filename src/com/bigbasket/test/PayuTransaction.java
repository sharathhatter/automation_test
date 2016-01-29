package com.bigbasket.test;

import java.util.concurrent.TimeUnit;

import io.appium.java_client.AppiumDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class PayuTransaction {
	AppiumDriver driver ; 
	public static String card_no = "5123456789012346";
	public static String nameOnCard = "Test";
	public static String month = " 05";
	public static String year = "2017";
	public static String cvv = "123";	
	
	public  void payuMainMenu(/*AppiumDriver driver2*/){
		try { 
//			driver = driver2;
			System.out.println("in Payu main menu");
//			WebElement linearLayout = driver.findElementById("android.widget.LinearLayout");
			WebElement cc = Setup.driver.findElement(By.id("com.bigbasket.mobileapp:id/button_credit_debit_card"));
			cc.click();			
			//driver = payuEntercreds(driver);4
			payuEntercreds(Setup.driver);
		}
		catch(Exception e ){
			System.out.println("payu click failed");			
		}		
//		return driver ;
	}	
	public  AppiumDriver payuEntercreds(AppiumDriver driver2){
		driver = driver2;
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		WebElement cardno = driver.findElementById("com.bigbasket.mobileapp:id/edit_text_card_number");
		cardno.sendKeys(card_no);
		WebElement nameOnTheCard = driver.findElementById("com.bigbasket.mobileapp:id/edit_text_name_on_card");
		nameOnTheCard.sendKeys(nameOnCard);
		WebElement mm = driver.findElementById("com.bigbasket.mobileapp:id/edit_text_expiry_month");
		mm.sendKeys(month);
		WebElement yy = driver.findElementById("com.bigbasket.mobileapp:id/edit_text_expiry_year");
		yy.sendKeys(year);
		WebElement cvvNo = driver.findElementById("com.bigbasket.mobileapp:id/edit_text_card_cvv");
		cvvNo.sendKeys(cvv);
		payNow(driver);
		return driver;		
	}
	public  AppiumDriver payNow(AppiumDriver driver2){
		driver = driver2;
		WebElement paynow = driver.findElementById("com.bigbasket.mobileapp:id/button_card_make_payment");
		paynow.click();		
		ThankyouPage thankupage = new ThankyouPage(); 
		thankupage.thankyouPage();
		return driver;		
	}
	
}

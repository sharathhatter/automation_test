package com.bigbasket.test;

import io.appium.java_client.AppiumDriver;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Payment {
	static AppiumDriver driver; 
	WebElement paymentLayout;
	List <WebElement> paymentOptions;
	
	public AppiumDriver findPaymentLayout (AppiumDriver driver2){
	driver= driver2; 
	driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	WebElement ppaymentLayout=driver.findElement(By.id("com.bigbasket.mobileapp:id/layoutChoosePayment"));
	paymentLayout=ppaymentLayout.findElement(By.id("com.bigbasket.mobileapp:id/layoutPaymentOptions"));
	paymentOptions = paymentLayout.findElements(By.id("com.bigbasket.mobileapp:id/mPaymentParentRelativelayout"));	
//	WebElement payment_layout=driver.findElement(By.id("com.bigbasket.mobileapp:id/layoutPaymentOptions"));
//	WebElement payu_click=driver.findElement(By.id("com.bigbasket.mobileapp:id/layoutPaymentOptions"));
	return driver;
	}
	
	public  AppiumDriver co_cod(AppiumDriver driver2) throws Exception {		
		driver= driver2;
		WebElement co_cod=paymentLayout.findElement(By.name("Cash On Delivery"));
		co_cod.click();
		return driver;
	}
	public  AppiumDriver co_payu(AppiumDriver driver2) throws Exception {		
		driver= driver2;
		findPaymentLayout(driver);
		paymentOptions.get(3).click();
//		WebElement w_co_payu=payment_layout.findElement(By.id("Credit/Debit Card, Net Banking"));
//		WebElement w_co_payu=paymentLayout.findElement(By.id("com.bigbasket.mobileapp:id/mPaymentParentRelativelayout"));
//		com.bigbasket.mobileapp:id/mPaymentParentRelativelayout
//		w_co_payu.click();	
		driver = Footer.footerClk(driver);
		PayuTransaction ptransaction = new PayuTransaction(); 
		ptransaction.payuMainMenu();
		return driver;
	}
	public  AppiumDriver co_crd_od(AppiumDriver driver2) throws Exception {		
		driver= driver2;
		WebElement w_co_crd_od=paymentLayout.findElement(By.name("Card On Delivery"));
		w_co_crd_od.click();
		return driver;
	}
	public  AppiumDriver co_payzapp(AppiumDriver driver2) throws Exception {		
		driver= driver2;
		WebElement w_co_payzapp=paymentLayout.findElement(By.name("PayZapp"));
		w_co_payzapp.click();
		return driver;
	}
	public  AppiumDriver co_mobikwik(AppiumDriver driver2) throws Exception {		
		driver= driver2;
		WebElement w_co_mobikwik=paymentLayout.findElement(By.name("Mobikwik Wallet"));
		w_co_mobikwik.click();
		return driver;
	}
	
	public  AppiumDriver shipments(AppiumDriver driver2) throws Exception {
		driver = driver2;
		WebElement w_co_mobikwik=paymentLayout.findElement(By.name("com.bigbasket.mobileapp:id/btnSelectedSlot"));
		w_co_mobikwik.click();
		return driver;
	}
}

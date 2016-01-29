package com.bigbasket.test;

import io.appium.java_client.AppiumDriver;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Checkout {
	public AppiumDriver driver;
	
	public static String partial_addr_str = "Incomplete";

	public  AppiumDriver co_clk(AppiumDriver driver2) throws Exception {
		AppiumDriver driver = driver2;
		driver = Footer.footerClk(driver);
		co_address(driver);
		return driver;
	}
	public AppiumDriver co_clk_again(AppiumDriver driver2) throws Exception {
		AppiumDriver driver = driver2;
		driver = Footer.footerClk(driver);
//		WebElement checkout_btn_layout = driver.findElement(By
//				.id("com.bigbasket.mobileapp:id/layoutCheckoutFooter"));
//		WebElement checkout_btn = checkout_btn_layout.findElement(By
//				.id("com.bigbasket.mobileapp:id/txtAction"));
//		checkout_btn.click();
//		WebElement continue_btn = checkout_btn_layout.findElement(By
//				.id("com.bigbasket.mobileapp:id/txtAction"));
//		continue_btn.click();		
		return driver;
	}
	public AppiumDriver co_address(AppiumDriver driver3)throws Exception {
		AppiumDriver driver = driver3;
		System.out.println("in Daddr");
		WebElement layout_delivery_addr = null;
		WebElement check_incomplete = null;
		try {
			System.out.println("in try");
			layout_delivery_addr = driver.findElement(By
				.id("com.bigbasket.mobileapp:id/layoutAddressDetails"));		
			check_incomplete = layout_delivery_addr.findElement(By
					.id("com.bigbasket.mobileapp:id/txtPartialAddress"));
			System.out.println(check_incomplete.getText().toString());
		}
		catch (Exception e){
			System.out.println("catch");	
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	//		on checkout if partial address exist then we'll navigate user to change address section / or complete partial address section
			Footer.footerClk(driver);
			Footer.footerClk(driver);
				/*if (check_incomplete.getText().toString().equals(partial_addr_str)) {
				System.out.println("in if loop");
				System.out.println("found Layout");
				WebElement change_delivery_addr = driver.findElement(By.id("com.bigbasket.mobileapp:id/txtChangeAddress"));
				System.out.println("found change button");
				change_delivery_addr.click();		
				select_address(driver);*/
			} 
				/*else {
					Footer.footerClk(driver);
				}	*/
		return driver;
		}	
	public AppiumDriver select_address(AppiumDriver driver2)
			throws Exception {
		AppiumDriver driver = driver2;
		System.out.println("in select address function");
		WebElement delivery_addr_layout = driver.findElement(By.className("android.widget.RelativeLayout"));			
		List <WebElement> user_delivery_address_layout = delivery_addr_layout.findElements(By.id("com.bigbasket.mobileapp:id/address_layout"));
		System.out.println(user_delivery_address_layout.size());
		if (user_delivery_address_layout.size() < 0) {
			System.out.println("Please add and address - no address found");
		} else {
			user_delivery_address_layout.get(1).click();			
			co_clk_again(driver);
		}
		return driver;
	}

	/*public static AppiumDriver co_shipments(AppiumDriver driver2)
			throws Exception {
		AppiumDriver driver = driver2;
		// WebElement
		// clk_cont2=driver.findElement(By.id("com.bigbasket.mobileapp:id/txtAction"));
		WebDriverWait wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.elementToBeClickable(By
				.id("com.bigbasket.mobileapp:id/layoutCheckoutFooter")));
		WebElement clk_cont2 = driver.findElement(By
				.id("com.bigbasket.mobileapp:id/layoutCheckoutFooter"));
		WebElement clk_cont3 = driver.findElement(By.name("CONTINUE"));
		clk_cont2.click();
		clk_cont3.click();
		payment.co_cod(driver);
		return driver;
	}*/
}

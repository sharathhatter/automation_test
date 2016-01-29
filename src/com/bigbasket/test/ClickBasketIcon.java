package com.bigbasket.test;

import io.appium.java_client.AppiumDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ClickBasketIcon {
	static AppiumDriver driver;
	public static AppiumDriver clk_basket(AppiumDriver driver2) throws Exception {
		driver = driver2;	
		WebElement srch_txt=driver.findElement(By.id("com.bigbasket.mobileapp:id/img"));
		srch_txt.click();
		Footer.footerClk(driver);
		return driver;		
	}
}

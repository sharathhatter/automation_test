package com.bigbasket.test;

import io.appium.java_client.AppiumDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Footer {
	static AppiumDriver driver;
	public static AppiumDriver footerClk(AppiumDriver driver2){
		driver=driver2;
	WebElement checkout_btn_layout = driver.findElement(By
			.id("com.bigbasket.mobileapp:id/layoutCheckoutFooter"));
	WebElement checkout_btn = checkout_btn_layout.findElement(By
			.id("com.bigbasket.mobileapp:id/txtAction"));
	checkout_btn.click();
	return driver;
	}
}

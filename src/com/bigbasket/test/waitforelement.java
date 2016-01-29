package com.bigbasket.test;

import io.appium.java_client.AppiumDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class waitforelement {
	static AppiumDriver driver;
		public static AppiumDriver wait(WebElement x){
			WebDriverWait wait = new WebDriverWait(driver, 15);
		    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#submitButton")));
			return driver;
		}
}

package com.bigbasket.pageobject;

import io.appium.java_client.AppiumDriver;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class MyAccount {
	AppiumDriver driver;	
	List <WebElement> homePageToolBarLayout = null;
	public void homeMenuIconClicked(){
		homePageToolBarLayout = driver.findElements(By.id("com.bigbasket.mobileapp:id/toolbarMain"));				
		}
	public void drawerBtn(){
		
		homePageToolBarLayout.get(0).click();
	}
	public void AddressSelection(){
		homePageToolBarLayout.get(1).click();
	}

}
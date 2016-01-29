package com.bigbasket.test;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Address {
	static WebDriver driver;
	public static WebDriver select_address(WebDriver addr_driver){
		driver = addr_driver;		
		WebElement username = driver.findElement(By.xpath("//RelativeLayout[@index='1']/radioBtnSelectedAddress[@index='1']"));
		username.click();		
		List<WebElement> srch_home=driver.findElements(By.id("radioBtnSelectedAddress"));
   		System.out.println(srch_home);
   		srch_home.get(1).click();
		return addr_driver;
		
	}
}

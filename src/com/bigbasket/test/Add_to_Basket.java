package com.bigbasket.test;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidKeyCode;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Add_to_Basket {
	public AppiumDriver driver ; 
	
	public AppiumDriver srch_icon_clk(AppiumDriver driver2, String item_name) throws Exception {
//		WebDriver wdriver= driver2;
		driver= driver2; 
//		WebDriverWait wait = new WebDriverWait(wdriver, 15);
//	    wait.until(ExpectedConditions.elementToBeClickable(By.id("com.bigbasket.mobileapp:id/action_search")));
		WebElement srch_home=driver.findElement(By.id("com.bigbasket.mobileapp:id/action_search"));
   		srch_home.click();
   		WebElement srch_txt=driver.findElement(By.id("com.bigbasket.mobileapp:id/txtAddress"));		   		
   		srch_txt.sendKeys(item_name);
   		((AppiumDriver) driver).sendKeyEvent(AndroidKeyCode.KEYCODE_ENTER); 
//   		driver= (AppiumDriver) wdriver ; 
   		add_jit_item(driver);
//   		add_express_item(driver);
		return driver;
	}	
	public  AppiumDriver add_jit_item(AppiumDriver driver2) throws Exception {
		driver= driver2;
		Thread.sleep(5000L);
		   		List <WebElement> add_button_lst=driver.findElements(By.id("com.bigbasket.mobileapp:id/imgAddToBasket"));
		   		try {
		   			add_button_lst.get(0).click();			   			
		   			ClickBasketIcon.clk_basket(driver);
				} catch (Exception e) {
					// TODO: handle exception
				}		   			
   		return driver;
	}
	public AppiumDriver add_normal_item(AppiumDriver driver2) throws Exception {
		driver = driver2;		
		srch_icon_clk(driver,"Onion");
		return driver;		
	}
	public AppiumDriver add_express_item(AppiumDriver driver2) throws Exception {
		driver = driver2;	
		srch_icon_clk(driver,"Onion");
		return driver;		
	}
	
	public AppiumDriver clear_basket(AppiumDriver driver2) throws Exception {
		return driver;
	}
}

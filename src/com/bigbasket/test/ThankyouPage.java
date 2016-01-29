package com.bigbasket.test;

import org.openqa.selenium.WebElement;
import org.testng.Assert;

public class ThankyouPage {
	public String thankyou = "Thank You! Your order has been placed.";
	public void thankyouPage(){
		
		try {
			WebElement thankyoutext = Setup.driver.findElementById("com.bigbasket.mobileapp:id/txtThankYou");
			if(thankyoutext.isDisplayed()){
				Assert.assertEquals(thankyoutext.getText().toString(), thankyou);
				System.out.println("Order Placed ");			
				WebElement orderNoArea = Setup.driver.findElementById("com.bigbasket.mobileapp:id/txtThankYou");
				System.out.println(orderNoArea.getText()); 
				WebElement continueBtn = Setup.driver.findElementById("com.bigbasket.mobileapp:id/btn_login");
				continueBtn.click();
			//  orderid field id : com.bigbasket.mobileapp:id/txtOrderNum
			//  Continue button- com.bigbasket.mobileapp:id/btn_login -  CONTINUE SHOPPING!
			}	
		}
		catch (Exception e){
			System.out.println("failed payment");
		}
	}
}

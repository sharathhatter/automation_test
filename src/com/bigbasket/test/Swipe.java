package com.bigbasket.test;

import io.appium.java_client.AppiumDriver;

import org.openqa.selenium.WebElement;

public class Swipe {
 static AppiumDriver driver ; 
	public static AppiumDriver swipeElementExample(WebElement el, AppiumDriver driver1) {
		driver=driver1;
		  String orientation = driver.getOrientation().value();

		  // get the X coordinate of the upper left corner of the element, then add the element's width to get the rightmost X value of the element
		  int leftX = el.getLocation().getX();
//		  leftX  = leftX + 10; 
		  int rightX = leftX + el.getSize().getWidth();

		  // get the Y coordinate of the upper left corner of the element, then subtract the height to get the lowest Y value of the element
		  int upperY = el.getLocation().getY();
		  int lowerY = upperY - el.getSize().getHeight();
		  int middleY = (upperY - lowerY) / 2;

		  if (orientation.equals("portrait")) {
		    // Swipe from just inside the left-middle to just inside the right-middle of the element over 500ms
		      driver.swipe(leftX + 5, middleY, rightX - 5, middleY, 500);
		  }
		  else if (orientation.equals("landscape")) {
		    // Swipe from just inside the right-middle to just inside the left-middle of the element over 500ms
		    driver.swipe(rightX - 5, middleY, leftX + 5, middleY, 500);
		  }
		  return driver;
		}

}

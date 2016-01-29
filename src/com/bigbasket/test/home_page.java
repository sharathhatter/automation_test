package com.bigbasket.test;

import io.appium.java_client.AppiumDriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class home_page {		
	     AppiumDriver driver;
		 int count ;
		 int displayedcategory;
		 int diffCategory;
		 WebDriverWait wait; 
		 List<WebElement> category_select = null;
		 List <WebElement> homePageToolBarLayout = null;
		 int noOfCategoryChecked = 0;
		 int startx,starty,endx,endy; 
		 int totalTopLevelCategory; 
		 int categoryCountAfterSwipe; 
		 int leftCategory; 
		 int untouchedCategorycheck ; 
		 int totalCategory = 9;
		 int i ; 
		 HashMap <Integer,WebElement> hashtry = new HashMap<>();
	
	public home_page(AppiumDriver driver) {
			 this.driver =driver;
		}
	public void call_all_functions() throws Exception{		
	}	
	public  AppiumDriver TopLevelCategory(AppiumDriver driver2) throws Exception {	
		driver=driver2;
//		WebElement co_button = driver.findElement(By.id("com.bigbasket.mobileapp:id/horizontalRecyclerView"));
//		category_select = co_button.findElements(By.id("com.bigbasket.mobileapp:id/sectionLayoutContainer"));	
		 startx = category_select.get(category_select.size()-1).getLocation().getX();
		 starty = category_select.get(category_select.size()-1).getLocation().getY();
		 endx = (category_select.get(category_select.size()-1).getLocation().getX())/3;
		 endy = (category_select.get(category_select.size()-1).getLocation().getY());		
		System.out.println(startx + " ::::::: " + starty + " ::::::: " + endx +  " ::::::: " +	endy);		
		System.out.println(category_select.size());	
		return driver;
	}		
	// This Function will click on each category/Element found in TLC layout.
	public void clickEachTLC() throws InterruptedException{	
		try {
		if(noOfCategoryChecked <= totalCategory){
		for (int i = 5 ; i < category_select.size();i++){							  	
				WebDriverWait wait = new WebDriverWait(driver, 30);			
				noOfCategoryChecked = (i+1); 								
//				hashtry.put((i+1), category_select.get(i));
				category_select.get(i).click();			
//				System.out.println(category_select.get(i));		
				wait.until(ExpectedConditions.elementToBeClickable(By
				        .id("com.bigbasket.mobileapp:id/txtProductListTitle")));
//				driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
				WebElement get_title = driver.findElement(By.id("com.bigbasket.mobileapp:id/txtProductListTitle"));
				System.out.println(get_title.getText() +"  "+"Checked");	
				System.out.println("No of category checked" + " " +noOfCategoryChecked + " "+i);
				driver.navigate().back();		
				
				if(noOfCategoryChecked==category_select.size()){
					System.out.println("in Swipe");
					driver = TopLevelCategory(driver);
					scrollLeft(startx, starty, endx, endy, 1000, driver);
					driver = TopLevelCategoryLayout(driver);
					clickEachTLC();
			}
		}
	} 
		}
		catch (Exception e ){
			driver.quit();
		}
	}	
	public void enterCategoryInHash (){
		for(Entry<Integer, WebElement> m:hashtry.entrySet()){  
			hashtry.put( i ,category_select.get(i) );
	   System.out.println(m.getKey()+" "+m.getValue());  
	}	
	}
	public int nextLeftCategory(int categoryAfterSwipe1){
		leftCategory = totalTopLevelCategory - noOfCategoryChecked;
		untouchedCategorycheck = categoryAfterSwipe1 - leftCategory ; 
		return untouchedCategorycheck; 
	}
	public  AppiumDriver TopLevelCategoryLayout(AppiumDriver driver) throws Exception {	
		WebElement homePageCategoryLayout = driver.findElement(By.id("com.bigbasket.mobileapp:id/horizontalRecyclerView"));
		category_select = homePageCategoryLayout.findElements(By.id("com.bigbasket.mobileapp:id/sectionLayoutContainer"));
		driver=TopLevelCategory(driver);
		clickEachTLC();
		return driver;
	}
	public void scrollLeft(int startx, int starty, int endx, int endy, int duration , AppiumDriver driver2) throws InterruptedException{
		driver = driver2;
		driver.swipe(startx, starty, endx, endy, 1000);
//		System.out.println("test");
//		scrollLeft( endx, endy,startx, starty, 2000);	
	}
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


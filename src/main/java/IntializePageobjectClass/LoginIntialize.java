package IntializePageobjectClass;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.support.PageFactory;
import org.testng.Reporter;
import com.bigbasket.pageobject.LoginObjects;
import TestExecution.PropertyReader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import setup.Setup;

public class LoginIntialize extends Setup {	
	LoginObjects loginobject = new LoginObjects();		
	public static AppiumDriver<MobileElement> driver;
	
		public LoginIntialize(AppiumDriver<MobileElement> driver) throws MalformedURLException{
		 super(driver);
		 PageFactory.initElements(new AppiumFieldDecorator(driver, 5, TimeUnit.SECONDS), loginobject);
		}	 		
		public boolean signupbuttondisplayed(){		 
		 return loginobject.SIGNUP_BUTTON.isDisplayed();
	 }	 
		public boolean loginbuttondisplayed(){
		 return loginobject.LOGIN_LOGINBTN.isDisplayed();
	 }
	 
		public AppiumDriver<MobileElement> signUp() throws MalformedURLException{
		 	signupclick();
		 	loginobject.U_FIRST_NAME.sendKeys(PropertyReader.fname);
			loginobject.U_LASTNAME.sendKeys(PropertyReader.lname);
			loginobject.PASSWORD.sendKeys(PropertyReader.password);
			loginobject.EMAIL_INPUT.sendKeys(PropertyReader.email);
			loginobject.LOCATION_MAIN_MENU.click();		 
		 if (PropertyReader.manual_signup == 1 && PropertyReader.choosecurrentlocation == 1){
			 current_location();
		 }		 
		 if(PropertyReader.manual_signup == 1 && PropertyReader.choosecurrentlocation == 0) { 
			 loginobject.SELECT_LOCATION_MANUALLY_BUTTON.click();
			 loginobject.PERMISSION_ALLOW.click();
			 loginobject.SEARCH_BUTTON.click();
			 enter_Loc_text_manually();
			 loginobject.MANUAL_SEARCH_RESULT.get(0).click();
			 loginobject.SIGNUP_BUTTON.click(); 
		 }		 
		 if (PropertyReader.googlesignup==1){
			 Reporter.log("Google Sign Up");}	 		 
		return (driver);
	 }			 
		/* Choose the Text manually to select the location*/
		public void enter_Loc_text_manually(){
		 loginobject.ENTER_SEARCH_LOC_MANUALLY.sendKeys(PropertyReader.manualaddress);
	 }	 
		public void current_location(){		 		 
			 loginobject.PERMISSION_ALLOW.click();
			 loginobject.CHOOSE_CURRENT_LOCATION_BUTTON.click();
	 }	 
		public void signUpGoogle(){
		 loginobject.GOOGLE_SOCIAL_SIGNUP_BUTTON.click();
		 }	 
		public AppiumDriver<MobileElement> signupclick(){
		 loginobject.SIGNUP_BUTTON.click();	
		 return driver;		 
	 }
}

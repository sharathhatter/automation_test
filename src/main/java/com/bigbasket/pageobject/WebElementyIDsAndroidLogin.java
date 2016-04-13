package com.bigbasket.pageobject;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.pagefactory.AndroidFindBy;

public class WebElementyIDsAndroidLogin {
	AppiumDriver driver;
	
	public WebElementyIDsAndroidLogin(AppiumDriver driver1) {
		this.driver = driver1; 
	}
	private static final String packageName = "com.bigbasket.mobileapp:id/";
	//Login WebElements Id's 
	@AndroidFindBy(id=packageName+"layoutLoginButtons")
	public static MobileElement LOGIN_BUTTON_LAYOUT; 
	@AndroidFindBy(className=packageName+"layoutLoginButtons")
	public MobileElement LOGIN_BUTTON_CLASS;
	@AndroidFindBy(id=packageName+"btn_login")
	public MobileElement LOGIN_LOGINBTN;
	@AndroidFindBy(id=packageName+"email_input")
	public MobileElement LOGIN_EMAIL;
	@AndroidFindBy(id=packageName+"edit_text_passwd")
	public MobileElement LOGIN_PASSWORD;
	@AndroidFindBy(id=packageName+"clblSkip")
	public MobileElement LOGIN_SKIP_TUTORIAL;
	@AndroidFindBy(id=packageName+"login_form")
	public MobileElement SIGNUP_LAYOUT;
	@AndroidFindBy(className="TextInputLayout")
	public MobileElement SIGNUP_BUTTON_CLASS;
	
	
	
	/*
	public static final By LOGIN_BUTTON_LAYOUT = By.id(packageName+"layoutLoginButtons");
	public static final By LOGIN_BUTTON_CLASS =By.className("android.widget.Button");
	public static final By LOGIN_LOGINBTN = By.id(packageName+"btn_login");
	public static final By LOGIN_EMAIL =By.id(packageName+"email_input");
	public static final By LOGIN_PASSWORD = By.id(packageName+"edit_text_passwd");
	public static final By LOGIN_SKIP_TUTORIAL=By.id(packageName+"clblSkip");
	//Sign Up WebElements id's
	public static final By SIGNUP_LAYOUT = By.id(packageName+"login_form");
	public static final By SIGNUP_BUTTON_CLASS = By.className("TextInputLayout");
	public static final By SIGNUP_FNAME=null;
	public static final By SIGNUP_LNAME=null;
	public static final By SIGNUP_EMAIL=null;
	public static final By SIGNUP_PASSWORD=null;	*/
}

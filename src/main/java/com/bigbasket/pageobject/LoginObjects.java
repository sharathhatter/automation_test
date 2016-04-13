package com.bigbasket.pageobject;

import io.appium.java_client.MobileElement;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AndroidFindBys;

import java.util.List;

import org.openqa.selenium.WebElement;

public class LoginObjects {
	
	private static final String packageName = "com.bigbasket.mobileapp:id/";
	//Login WebElements Id's 
	@AndroidFindBy(id=packageName+"layoutLoginButtons")
	public MobileElement LOGIN_BUTTON_LAYOUT; 
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
	
	//Signup WebElements Id's 
	@AndroidFindBy(id=packageName+"btnRegister")
	public WebElement SIGNUP_BUTTON; 
	@AndroidFindBy(id=packageName+"editTextFirstName")
	public MobileElement U_FIRST_NAME;
	@AndroidFindBy(id=packageName+"editTextLastName")
	public MobileElement U_LASTNAME;
	@AndroidFindBy(id=packageName+"email_input")
	public MobileElement EMAIL_INPUT;
	@AndroidFindBy(id=packageName+"edit_text_passwd")
	public MobileElement PASSWORD;
	@AndroidFindBy(id=packageName+"txtChooseLocation")
	public MobileElement LOCATION_MAIN_MENU;
	@AndroidFindBy(id=packageName+"plus_sign_in_button")
	public MobileElement GOOGLE_SOCIAL_SIGNUP_BUTTON;
	@AndroidFindBy(id=packageName+"btn_fb_login")
	public MobileElement FACEBOOK_SOCIAL_SIGNUP_BUTTON;
	@AndroidFindBy(id="com.android.packageinstaller:id/permission_allow_button")
	public MobileElement PERMISSION_ALLOW ; 
	@AndroidFindBy(id="com.android.packageinstaller:id/permission_deny_button")
	public MobileElement PERMISSION_DENY ;
	@AndroidFindBy(id="com.bigbasket.mobileapp:id/btnToCurrentLocation")
	public MobileElement CHOOSE_CURRENT_LOCATION_BUTTON;
	@AndroidFindBy(id="com.bigbasket.mobileapp:id/btnChooseLocation")
	public MobileElement SELECT_LOCATION_MANUALLY_BUTTON;
	@AndroidFindBy(id="com.google.android.gms:id/places_ui_menu_main_search")
	public MobileElement SEARCH_BUTTON;	
	@AndroidFindBy(id="com.google.android.gms:id/places_ui_menu_main_search")
	public MobileElement ENTER_SEARCH_LOC_MANUALLY;	
	@AndroidFindBys({
	@AndroidFindBy(id = "com.google.android.gms:id/list")}) 
	public List<MobileElement> MANUAL_SEARCH_RESULT; 
	
	//gmail account picker id 
	@AndroidFindBy(id="com.google.android.gms:id/account_picker")
	public List<MobileElement> ACCOUNT_PICKER;
	@AndroidFindBy(id="com.google.android.gms:id/account_name")
	public MobileElement GOOGLE_LOGGEDIN_USER;
}

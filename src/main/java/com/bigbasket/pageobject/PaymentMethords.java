package com.bigbasket.pageobject;

import io.appium.java_client.pagefactory.AndroidFindBy;

import org.openqa.selenium.WebElement;

public class PaymentMethordsobjects {
	@AndroidFindBy(id = "layoutChoosePayment")
	WebElement PAYMENT_LAYOUT_;
	private static final String package_Name = "com.bigbasket.mobileapp:id/";
	
	//Payment Layout id's
	@AndroidFindBy(id=package_Name+"layoutChoosePayment")
	public WebElement PAYMENT_LAYOUT_1; 
	@AndroidFindBy(id=package_Name+"layoutPaymentOptions")
	public WebElement PAYMENT_LAYOUT_2;
	@AndroidFindBy(id=package_Name+"mPaymentParentRelativelayout")
	public WebElement PAYMENT_LAYOUT_3;
	
	//This is the layout for , this pops up from bottom while placing the order in payment page.
	@AndroidFindBy(id=package_Name+"parentPanel")
	public WebElement outer_Layout_Bottom_Warning;
	@AndroidFindBy(id=package_Name+"dont_show_check_box")
	public WebElement Dont_Show_CheckBox_Again;
	@AndroidFindBy(id="android:id/button1")
	public WebElement continue_Btn;
	
	/*All Payment methords ids*/
	@AndroidFindBy(name="Cash On Delivery")
	public WebElement COD;
	@AndroidFindBy(name="Card On Delivery")
	public WebElement CARD;
	@AndroidFindBy(id="PayZapp")
	public WebElement PAYZAPP;
	@AndroidFindBy(id=package_Name+"dont_show_check_box")
	public WebElement SHIPMENTS;
	@AndroidFindBy(name="Mobikwik Wallet")
	public WebElement MOBIKWIK;
	@AndroidFindBy(id="android:id/button1")
	public WebElement CONTINUE_BTN;
	
	/*
	public static final By PAYMENT_LAYOUT_1 = By.id(package_Name+"layoutChoosePayment");
	public static final By PAYMENT_LAYOUT_2 = By.id(package_Name+"layoutPaymentOptions");
	public static final By PAYMENT_LAYOUT_3 = By.id(package_Name+"mPaymentParentRelativelayout");*/
	
	/*
	public static final By outer_Layout_Bottom_Warning = By.id(package_Name+"parentPanel");
	public static final By Dont_Show_CheckBox_Again = By.id(package_Name+"dont_show_check_box");
	public static final By continue_Btn =By.id("android:id/button1");	*/
	
	/*
	public static final By COD = By.name("Cash On Delivery");
	public static final By CARD = By.name("Card On Delivery");
	public static final By PAYZAPP = By.name("PayZapp");
	public static final By MOBIKWIK = By.name("Mobikwik Wallet");
	public static final By SHIPMENTS = By.id(package_Name+"dont_show_check_box");
	public static final By CONTINUE_BTN =By.id("android:id/button1");*/
}


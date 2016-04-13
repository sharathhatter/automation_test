package IntializePageobjectClass;

import java.io.InputStream;

import org.openqa.selenium.support.PageFactory;
import com.bigbasket.pageobject.PaymentMethordsobjects;
import TestExecution.PropertyReader;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import setup.Setup;

public class PaymentMethordIntialize extends Setup{	
	PaymentMethordsobjects paymentobject = new PaymentMethordsobjects();
	public static AppiumDriver<MobileElement> driver;
	public static int paymentmode;
	public static String filename = "payment.property";
	static InputStream input = null ; 
	
		public PaymentMethordIntialize(AppiumDriver<MobileElement> driver){
			super(driver);
			 PageFactory.initElements(new AppiumFieldDecorator(driver, 10, null),paymentobject); 
		}		
		public void readproperty(){
			PropertyReader pr = new PropertyReader(filename);
			input = pr.read_property();
		}
	}


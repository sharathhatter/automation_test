package IntializePageobjectClass;


import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;
import com.bigbasket.pageobject.MyAccount;

public class MyAccountIntialize {
	MyAccount myAcc = new MyAccount();
	
	public MyAccountIntialize(AppiumDriver driver){
		PageFactory.initElements(new AppiumFieldDecorator(driver, 20, null),this); 					
	}
}

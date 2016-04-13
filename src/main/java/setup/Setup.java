package setup;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;


public  class Setup {	
	public static AppiumDriver<MobileElement> driver;	
	
	public Setup(AppiumDriver<MobileElement> sentdriver) {
			driver = sentdriver; 
			System.out.println("help");}			    		  		
		   /* @BeforeSuite
		    public void setUpAppium() throws MalformedURLException{
		        final String URL_STRING = "http://127.0.0.1:4723/wd/hub";
		        URL url = new URL(URL_STRING);
		        //Use a empty DesiredCapabilities object
		        driver = new AndroidDriver<MobileElement>(url, new DesiredCapabilities());
		        //Use a higher value if your mobile elements take time to show up
		        driver.manage().timeouts().implicitlyWait(35, TimeUnit.SECONDS);
		        Reporter.log("In setupappium function"); 
		    }*/		   
//		    @BeforeClass
//		    public void navigateTo() throws InterruptedException {
//		    	navigationPage = new Navigation(driver);
//		        navigationPage.gotoCategory(getName());
//		    }

		    /**
		     * Restart the app after every test class to go back to the main
		     * screen and to reset the behavior
		     */
		  
		    public void restartApp() {
		        driver.resetApp();
		    }
		}
  


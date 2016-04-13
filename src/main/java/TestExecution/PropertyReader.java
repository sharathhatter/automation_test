package TestExecution;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
	public static String filename ;
	public static String email ;
	public static String password ;
	public static String fname;
	public static String lname;
	public static int choosecurrentlocation;
	public static int searchmanually;
	public static String manualaddress;
	public static int googlesignup;
	public static int facebooksignup;
	public static int manual_signup;
	public static int platform;
	public static int mashmallow;
	public static String propertyfilename; 	
	public static int payment ; 
	
	public PropertyReader(String propertfilename){
		String baseURL = "src/test/resources/";
		propertyfilename = baseURL.concat(propertfilename);
	}	
	
	public InputStream read_property(){	
	Properties prop = new Properties();
	InputStream input = null;			
	try {		
	   input = new FileInputStream(propertyfilename);
		prop.load(input);		
//		payment = Integer.parseInt(prop.getProperty("payment_mode"));
		//load a properties file from class path, inside static method		
            //get the property value and print it out
		  /* email = prop.getProperty("EMAIL_SIGHUP");
	       password = prop.getProperty("PASSWORD");
	       fname=  prop.getProperty("F_NAME");
	       lname = prop.getProperty("L_NAME");
	       choosecurrentlocation = Integer.parseInt(prop.getProperty("CHOOSE_CURRENT_LOCATION"));
	       searchmanually = Integer.parseInt(prop.getProperty("SEARCH_MANUALLY"));
	       manualaddress = prop.getProperty("MANUAL_LOC");
	       googlesignup = Integer.parseInt(prop.getProperty("GOOGLE_SIGNUP"));
	       facebooksignup = Integer.parseInt(prop.getProperty("FACEBOOK_SIGNUP"));	 
	       manual_signup = Integer.parseInt(prop.getProperty("MANUAL_SIGNUP"));
	       platform = Integer.parseInt(prop.getProperty("PLATFORM")); 	
	       mashmallow = Integer.parseInt(prop.getProperty("MARSHMALLOW"));*/
	} catch (IOException ex) {
		ex.printStackTrace();
		
    } finally{
    	if(input!=null){
    		try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	}
    }
	return input;
}
//	public static void main(String args[]){
//		PropertyReader pr = new PropertyReader("payment.property");
//		pr.read_property();
//		System.out.println(payment + " " );
//		
//	}
}


package com.bigbasket.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Read_Data {
		String result = "";
	InputStream inputStream;
	 static String user;
	 static String password;
		public void getPropValues() throws IOException {
	 
			try {
				Properties prop = new Properties();
				String propFileName = "user_details.properties";
	 
				inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
	 
				if (inputStream != null) {
					prop.load(inputStream);
				} else {
					throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
				}
	 
//				Date time = new Date(System.currentTimeMillis());
	 
				// get the property value and print it out
				user = prop.getProperty("user");
				System.out.println(user);
				password = prop.getProperty("password");
	 
//				result = "Company List = " + password + ", " + company2 + ", " + company3;
//				System.out.println(result + "\nProgram Ran on " + time + " by user=" + user);
			} catch (Exception e) {
				System.out.println("Exception: " + e);
			} finally {
				inputStream.close();
			}
//			return result;
		}
	}


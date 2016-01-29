package com.bigbasket.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class URLReader {
	
    public static void main(String[] args) throws Exception {

       // URL oracle = new URL("http://www.google.com/");
        URL oracle = new URL("http://testaws.bigbasket.com/mapi/v2.0.2/register-device");
        BufferedReader in = new BufferedReader(
        new InputStreamReader(oracle.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);
        int i=0;
        Map m = new HashMap();
        m.put(i, inputLine);
        i++;
        in.close();
    }
}
//public class APIStart {
//public StringBuffer testing(String url)  throws Exception
//{
//	System.out.println("in Http call");
//        URL obj; 
//
//        obj = new URL(url);
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//        con.setRequestMethod("GET");
//        int responseCode = con.getResponseCode();
//        System.out.println(responseCode);
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();         
//        
//        return response;
//}
//
//}
//


//public class APIStart {
////String targetURL, String urlParameters
//	public static String excutePost() {
//		HttpURLConnection connection = null;
//		try {
//			// Create connection
//			URL url = new URL("http://dev1.bigbasket.com/mapi/v2.0.0/cities/");
//			connection = (HttpURLConnection) url.openConnection();
//			connection.setRequestMethod("GET");
////			connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
////			connection.setRequestProperty("Content-Length",Integer.toString(urlParameters.getBytes().length));
//			connection.setRequestProperty("Content-Language", "en-US");
//			connection.setUseCaches(false);
//			connection.setDoOutput(true);
//
//			// Send request
////			DataOutputStream wr = new DataOutputStream(
////					connection.getOutputStream());
////			wr.writeBytes(urlParameters);
////			wr.close();
//
//			// Get Response
//			InputStream is = connection.getInputStream();
//			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//			StringBuilder response = new StringBuilder(); // or StringBuffer if
//															// not Java 5+
//			String line;
//			while ((line = rd.readLine()) != null) {
//				response.append(line);
//				response.append('\r');
//			}
//			rd.close();
//			return response.toString();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		} finally {
//			if (connection != null) {
//				connection.disconnect();
//			}
//
//		}
//
//	}
//}

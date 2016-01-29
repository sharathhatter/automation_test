package com.bigbasket.test;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ReadJsonFile {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		JSONParser parser = new JSONParser();

		try {

			Object obj = parser.parse(new FileReader(
					"file1.txt"));

			JSONObject jsonObject = (JSONObject) obj;

			String name = (String) jsonObject.get("Name");
			String author = (String) jsonObject.get("Author");
			JSONArray companyList = (JSONArray) jsonObject.get("Company List");

			System.out.println("Name: " + name);
			System.out.println("Author: " + author);
			System.out.println("\nCompany List:");

			List<String> list = new ArrayList<String>();
			for (int i = 0; i < companyList.size(); i++) {
				list.add(companyList.get(i).toString());
			}

			Iterator<String> iterator = list.iterator();
			while (iterator.hasNext()) {
				System.out.println(iterator.next());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
/**
 * @param args
 */


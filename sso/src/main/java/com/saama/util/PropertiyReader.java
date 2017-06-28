package com.saama.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiyReader {

	private String fileName;

	public PropertiyReader(String fileName) {
		this.fileName = fileName;
	}

	public String readPropertyFile(String key) {
		String returnValue = "";
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = this.getClass().getResourceAsStream(this.fileName);
			prop.load(input);
			returnValue = prop.getProperty(key);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return returnValue;
	}

}
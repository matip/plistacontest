package de.dailab.plistacontest.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MahoutWriter implements Runnable {

	String fileName;
	String impression;
	int rating;
	

	public MahoutWriter(final String _fileName, String _impression, int _rating) {
		this.fileName = _fileName;
		this.impression = _impression;
		this.rating = _rating;
		
	}

	public void writeImpressions() {

		final JSONObject obj = (JSONObject) JSONValue.parse(this.impression);

		boolean write = false;
		FileWriter fw = null;
		try {
				
			write = Boolean.parseBoolean(((JSONObject) obj.get("item")).get(
					"recommendable").toString());
		} catch (Exception e) {
			//
		}

		if (write) {
			try {
				fw = new FileWriter(this.fileName, true);
				fw.append( ((JSONObject) obj.get("client")).get(
						"id").toString() + "," + ((JSONObject) obj.get("item")).get(
						"id").toString() + "," +this.rating + "," + Calendar.getInstance().getTimeInMillis());
				fw.append(System.getProperty("line.separator"));
			} catch (Exception e) {
				System.err.println("Konnte Datei nicht erstellen");
			} finally {
				if(fw != null) {
					try {
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
	}

	public void run() {
		writeImpressions();
		
	}
}

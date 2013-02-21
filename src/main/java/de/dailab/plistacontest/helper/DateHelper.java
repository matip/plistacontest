package de.dailab.plistacontest.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateHelper {

	
	public static Date getDateWithHours(int hours) {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, hours); 
		return cal.getTime();
		
		
	}
	
	/**
	 * Date of today
	 * @return date string with the format yyyyMMdd indicating today
	 */
	public static String getDate(){
		final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		final Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	
	/**
	 * The date of yesterday
	 * @return date string with the format yyyyMMdd indicating yesterday
	 */
	public static String getYesterday() {
		final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return dateFormat.format(cal.getTime());
	}
	
	/**
	 * The day before yesterday
	 * @return date string with the format yyyyMMdd indicating the day before yesterday
	 */
	public static String getTDBY() {
		final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -2);
		return dateFormat.format(cal.getTime());
	}
	
	/**
	 * Current date minus a specified number of days
	 * @return date string with the format yyyyMMdd indicating the day before yesterday
	 */
	public static String getDateBefore(final int _daysbefores) {
		final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, _daysbefores);
		return dateFormat.format(cal.getTime());
	}
	
}



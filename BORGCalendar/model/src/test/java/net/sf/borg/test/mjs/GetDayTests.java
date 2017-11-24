package net.sf.borg.test.mjs;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.TreeSet;

import net.sf.borg.model.db.DBHelper;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.entity.CheckList;
import net.sf.borg.model.entity.Appointment;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Day;

public class GetDayTests {

	@BeforeClass
	public static void setUp() throws Exception {
		// open the borg dbs - in memory
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:hsqldb:mem:whatever");

	}

	@AfterClass
	public static void tearDown() throws Exception {
		DBHelper.getController().close();
		
	}
	// --------------------------------------------------------------------
	// Tests from Specification Analysis
	// --------------------------------------------------------------------
	
	
	@Test
	public void Test1DaylightSavingTime() throws Exception {
		Day d = Day.getDay(2017, 2, 12);
		String expectedItemColor = "black";
		String expectedItemLabel = Resource.getResourceString("Daylight_Savings_Time");

		String observedItemColor = ((TreeSet<CalendarEntity>) d.getItems()).first().getColor();
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Item Color", expectedItemColor, observedItemColor);
		assertEquals("Item Title", expectedItemLabel, observedItemLabel);
	}

	@Test
	public void Test2StandardTime() throws Exception {
		Day d = Day.getDay(2018, 10, 4);
		String expectedItemColor = "black";
		String expectedItemLabel = Resource.getResourceString("Standard_Time");

		String observedItemColor = ((TreeSet<CalendarEntity>) d.getItems()).first().getColor();
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Item Color", expectedItemColor, observedItemColor);
		assertEquals("Item Title", expectedItemLabel, observedItemLabel);
	}

	@Test
	public void Test3Appointment() throws Exception {
		String appointmentColor = "green";
		String appointmentTitle = "Test Appointment";

		Appointment a = new Appointment();
		Calendar ApptDate = new GregorianCalendar(2017, 5, 17);
		a.setColor(appointmentColor);
		a.setText(appointmentTitle);
		a.setDate(ApptDate.getTime());
		AppointmentModel.getReference().saveAppt(a);
		int expectedHolidayFlag = 0;
		int expectedVacationFlag = 0;
		
		Day d = Day.getDay(2017, 5, 17);

		String observedItemColor = ((TreeSet<CalendarEntity>) d.getItems()).first().getColor();
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Item Color", appointmentColor, observedItemColor);
		assertEquals("Item Title", appointmentTitle, observedItemLabel);
		assertEquals("Holiday Flag", expectedHolidayFlag, d.getHoliday());
		assertEquals("Vacation Flag", expectedVacationFlag, d.getVacation());
		
		AppointmentModel.getReference().delAppt(a);
	}
	
	@Test
	public void Test4USHoliday() throws Exception {
		String prefValue = Prefs.getPref(PrefName.SHOWUSHOLIDAYS);
		
		Prefs.putPref(PrefName.SHOWUSHOLIDAYS, "true");
		Day d = Day.getDay(2017, 9, 31);
		String expectedItemColor = "purple";
		String expectedItemLabel = Resource.getResourceString("Halloween");
		int expectedHolidayFlag = 0;
		
		String observedItemColor = ((TreeSet<CalendarEntity>) d.getItems()).first().getColor();
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Item Color", expectedItemColor, observedItemColor);
		assertEquals("Item Title", expectedItemLabel, observedItemLabel);
		assertEquals("Holiday Flag",expectedHolidayFlag,d.getHoliday());
		
		Prefs.putPref(PrefName.SHOWUSHOLIDAYS, prefValue);
	}
	
	@Test
	public void Test5CanadianHoliday() throws Exception {
		String prefValue = Prefs.getPref(PrefName.SHOWCANHOLIDAYS);
		
		Prefs.putPref(PrefName.SHOWCANHOLIDAYS, "true");
		Day d = Day.getDay(2017,6,1);
		String expectedItemColor = "purple";
		String expectedItemLabel = Resource.getResourceString("Canada_Day");
		int expectedHolidayFlag = 0;

		String observedItemColor = ((TreeSet<CalendarEntity>) d.getItems()).first().getColor();
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Item Color", expectedItemColor, observedItemColor);
		assertEquals("Item Title", expectedItemLabel, observedItemLabel);
		assertEquals("Holiday Flag",expectedHolidayFlag,d.getHoliday());
		
		Prefs.putPref(PrefName.SHOWCANHOLIDAYS, prefValue);
	}
	
	@Test
	//This covers the first day of a month and year as well as a common holiday
	public void Test6CommonHoliday() throws Exception {
		String prefValue = Prefs.getPref(PrefName.SHOWCANHOLIDAYS);
		
		Prefs.putPref(PrefName.SHOWCANHOLIDAYS, "true");
		Day d = Day.getDay(2018,0,1);
		String expectedItemColor = "purple";
		String expectedItemLabel = Resource.getResourceString("New_Year's_Day");
		int expectedHolidayFlag = 1;

		String observedItemColor = ((TreeSet<CalendarEntity>) d.getItems()).first().getColor();
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Item Color", expectedItemColor, observedItemColor);
		assertEquals("Item Title", expectedItemLabel, observedItemLabel);
		assertEquals("Holiday Flag",expectedHolidayFlag,d.getHoliday());
		
		Prefs.putPref(PrefName.SHOWCANHOLIDAYS, prefValue);
	}
	
	@Test
	public void Test7EndOfMonthAndYear() throws Exception {
		Day d = Day.getDay(2017,11,31);
		int expectedHolidayFlag = 0;
		int expectedVacationFlag = 0;

		assertEquals("Holiday Flag",expectedHolidayFlag,d.getHoliday());
		assertEquals("Vacation Flag", expectedVacationFlag, d.getVacation());
		
		assertEquals("No items should exist on the day",0,d.getItems().size());		
	}
	
	// --------------------------------------------------------------------
	// Tests from Boundary Value Analysis
	// --------------------------------------------------------------------
	
	@Test
	public void Test9MonthLowerBoundryGreater() throws Exception {
		CheckDayForAppointment(2009,1,2,2009,1,2);
	}
	@Test
	public void Test10MonthLowerBoundaryAt() throws Exception {
		CheckDayForAppointment(2009,0,2,2009,0,2);
	}
	//@Test
	public void Test11MonthLowerBoundryLess() throws Exception {
		CheckDayForAppointment(2008,11,2,2009,-1,2);
	}
	
	@Test
	public void Test12MonthUpperBoundryGreater() throws Exception {
		CheckDayForAppointment(2010,0,2,2009,12,2);
	}
	
	@Test
	public void Test13MonthUpperBoundaryAt() throws Exception {
		CheckDayForAppointment(2009,11,2,2009,11,2);
	}
	
	//@Test
	public void Test14MonthUpperBoundryLess() throws Exception {
		CheckDayForAppointment(2009,10,2,2009,10,2);
	}
	
	//DayLowerBoundaryGreater is covered by Test9MonthLowerBoundaryGreater
	
	@Test
	public void Test15DayLowerBoundaryAt() throws Exception {
		CheckDayForAppointment(2009,1,1,2009,1,1);
	}
	
	//@Test
	public void Test16DayLowerBoundryLess() throws Exception {
		CheckDayForAppointment(2009,0,31,2009,1,0);
	}
	
	@Test
	public void Test17DayUpperBoundryGreater() throws Exception {
		CheckDayForAppointment(2009,3,1,2009,2,32);
	}
	
	@Test
	public void Test18DayUpperBoundaryAt() throws Exception {
		CheckDayForAppointment(2009,2,31,2009,2,31);
	}
	
	//@Test
	public void Test19YearUpperBoundryLess() throws Exception {
		CheckDayForAppointment(2009,2,30,2009,2,30);
	}
	
	/// 1. Create an appointment using apptYear, apptMonth, and apptDay
	/// 2. Call getDay for checkYear, checkMonth, and checkDay
	/// 3. Assert that the returned Day has the created appointment
	/// 4. Delete the appointment
	private void CheckDayForAppointment(int apptYear, int apptMonth, int apptDay, int checkYear, int checkMonth, int checkDay) throws Exception {
		String appointmentTitle = "Test Appointment";

		Appointment a = new Appointment();
		Calendar ApptDate = new GregorianCalendar(apptYear, apptMonth, apptDay);
		a.setText(appointmentTitle);
		a.setDate(ApptDate.getTime());
		AppointmentModel.getReference().saveAppt(a);
		
		Day d = Day.getDay(checkYear, checkMonth, checkDay);

		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Item Title", appointmentTitle, observedItemLabel);
		
		AppointmentModel.getReference().delAppt(a);
	}
	
	
	

	//@Test
	public void test() throws Exception {
		
		  Prefs.putPref(PrefName.SHOWUSHOLIDAYS, "true"); Day d = Day.getDay(2017, 11,
		  32); //assertEquals(1,d.getHoliday());
		  assertEquals(Prefs.getPref(PrefName.SHOWUSHOLIDAYS),"true");
		  
		  Calendar cal = new GregorianCalendar(0, 0, 2);
		  Calendar cal2 = new GregorianCalendar(1,0,2);
		  assertEquals("years", cal.get(Calendar.YEAR), cal2.get(Calendar.YEAR));
		  assertEquals(cal.get(Calendar.YEAR),1);
		  assertEquals(cal.get(Calendar.MONTH),0);
		  assertEquals(cal.get(Calendar.DAY_OF_MONTH),2);
		  assertEquals(cal.getTime().toString(), cal2.getTime().toString());
		 	}

}

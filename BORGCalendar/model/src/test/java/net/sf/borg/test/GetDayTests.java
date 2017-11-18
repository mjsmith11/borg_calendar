package net.sf.borg.test;

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

	// --------------------------------------------------------------------
	// Tests from Specification Analysis
	// --------------------------------------------------------------------
	@Test
	public void DaylightSavingTime() throws Exception {
		Day d = Day.getDay(2017, 2, 12);
		String expectedItemColor = "black";
		String expectedItemLabel = Resource.getResourceString("Daylight_Savings_Time");

		String observedItemColor = ((TreeSet<CalendarEntity>) d.getItems()).first().getColor();
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Item Color", expectedItemColor, observedItemColor);
		assertEquals("Item Title", expectedItemLabel, observedItemLabel);
	}

	@Test
	public void StandardTime() throws Exception {
		Day d = Day.getDay(2018, 10, 4);
		String expectedItemColor = "black";
		String expectedItemLabel = Resource.getResourceString("Standard_Time");

		String observedItemColor = ((TreeSet<CalendarEntity>) d.getItems()).first().getColor();
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Item Color", expectedItemColor, observedItemColor);
		assertEquals("Item Title", expectedItemLabel, observedItemLabel);
	}

	@Test
	public void Appointment() throws Exception {
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
	public void USHoliday() throws Exception {
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
	public void CanadianHoliday() throws Exception {
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
	public void CommonHoliday() throws Exception {
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
	public void EndOfMonthAndYear() throws Exception {
		Day d = Day.getDay(2017,11,31);
		int expectedHolidayFlag = 0;
		int expectedVacationFlag = 0;

		assertEquals("Holiday Flag",expectedHolidayFlag,d.getHoliday());
		assertEquals("Vacation Flag", expectedVacationFlag, d.getVacation());
		
		assertEquals("No items should exist on the day",0,d.getItems().size());		
	}
	
	@Test
	public void ClosedDatabaseConnection() throws Exception {
		DBHelper.getController().getConnection().close();
		Day d = Day.getDay(2017, 11, 9);
		
	}
	
	

	@Test
	public void test() throws Exception {
		/*
		 * Prefs.putPref(PrefName.SHOWUSHOLIDAYS, "true"); Day d = Day.getDay(2017, 11,
		 * 32); //assertEquals(1,d.getHoliday());
		 * assertEquals(Prefs.getPref(PrefName.SHOWUSHOLIDAYS),"true");
		 * 
		 * Calendar cal = new GregorianCalendar(0, 1, -1);
		 * assertEquals(cal.get(Calendar.YEAR),2014);
		 * assertEquals(cal.get(Calendar.MONTH),0);
		 * assertEquals(cal.get(Calendar.DAY_OF_MONTH),31);
		 */

		GregorianCalendar gc = new GregorianCalendar(2017, 2, 12, 11, 00);
		boolean dstNow = TimeZone.getDefault().inDaylightTime(gc.getTime());
		gc.add(Calendar.DATE, -1);
		boolean dstYesterday = TimeZone.getDefault().inDaylightTime(gc.getTime());

		assertTrue(dstNow);
		assertFalse(dstYesterday);

	}

}

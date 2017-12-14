package net.sf.borg.test.mjs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Day;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.Appointment;
import net.sf.borg.model.entity.CalendarEntity;

public class addToDayTests {
	
	private static Appointment publicAppt;
	private static Appointment privateAppt;
	private static Appointment vacationAppt;
	private static Appointment holidayAppt;
	private static Appointment vacationHolidayAppt;


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		// open the borg dbs - in memory		
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:hsqldb:mem:appointments");
		
		Calendar ApptDate = new GregorianCalendar(2017, 5, 17);
		publicAppt = AppointmentModel.getReference().newAppt();
		publicAppt.setText("My Public Appt");
		publicAppt.setDate(ApptDate.getTime());
		publicAppt.setPrivate(false);
		AppointmentModel.getReference().saveAppt(publicAppt);
		
		privateAppt = AppointmentModel.getReference().newAppt();
		privateAppt.setText("My Private Appt");
		privateAppt.setPrivate(true);
		privateAppt.setDate(ApptDate.getTime());
		AppointmentModel.getReference().saveAppt(privateAppt);
		
		vacationAppt = AppointmentModel.getReference().newAppt();
		vacationAppt.setText("My Vacation Appt");
		vacationAppt.setVacation(1);
		vacationAppt.setDate(ApptDate.getTime());
		AppointmentModel.getReference().saveAppt(vacationAppt);
		
		holidayAppt = AppointmentModel.getReference().newAppt();
		holidayAppt.setText("My Holiday Appt");
		holidayAppt.setHoliday(1);
		holidayAppt.setDate(ApptDate.getTime());
		AppointmentModel.getReference().saveAppt(holidayAppt);
		
		vacationHolidayAppt = AppointmentModel.getReference().newAppt();
		vacationHolidayAppt.setText("My Holiday & Vacation Appt");
		vacationHolidayAppt.setHoliday(1);
		vacationHolidayAppt.setVacation(1);
		vacationHolidayAppt.setDate(ApptDate.getTime());
		AppointmentModel.getReference().saveAppt(vacationHolidayAppt);
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		DBHelper.getController().close();
	}

	
	// --------------------------------------------------------------------
	// Tests from Specification Analysis
	// --------------------------------------------------------------------
	@Test
	public void Test2Null() throws Exception{
		Day d = Day.getNewDay();
		Day.TestAddToDay(d, null);
		
		assertEquals("Items in collection",0, d.getItems().size());
		
	}
	
	@Test
	public void Test3Empty() throws Exception {
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",0, d.getItems().size());
	}

	@Test
	public void Test4PublicAppt() throws Exception {
		String prefValue = Prefs.getPref(PrefName.SHOWPUBLIC);
		
		Prefs.putPref(PrefName.SHOWPUBLIC, "true");
		
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(publicAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",1, d.getItems().size());
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Appointment Text", publicAppt.getText(), observedItemLabel);
		
		Prefs.putPref(PrefName.SHOWPUBLIC, prefValue);
	}
	
	@Test
	public void Test5PrivateAppt() throws Exception {
		String prefValue = Prefs.getPref(PrefName.SHOWPRIVATE);
		
		Prefs.putPref(PrefName.SHOWPRIVATE, "true");
		
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(privateAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",1, d.getItems().size());
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Appointment Text", privateAppt.getText(), observedItemLabel);
		
		Prefs.putPref(PrefName.SHOWPRIVATE, prefValue);
	}
	
	@Test
	public void Test6VacationAppt() throws Exception {
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(vacationAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",1, d.getItems().size());
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		int observedVacation = d.getVacation();
		int observedHoliday = d.getHoliday();
		assertEquals("Appointment Text", vacationAppt.getText(), observedItemLabel);
		assertEquals("Vacation Flag", 1, observedVacation);
		assertEquals("Holiday Flag", 0, observedHoliday);
		
	}
	
	@Test
	public void Test7HolidayAppt() throws Exception {
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(holidayAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",1, d.getItems().size());
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		int observedHoliday = d.getHoliday();
		int observedVacation = d.getVacation();
		assertEquals("Appointment Text", holidayAppt.getText(), observedItemLabel);
		assertEquals("Holiday Flag", 1, observedHoliday);
		assertEquals("Vacation Flag", 0, observedVacation);
		
	}
	
	// --------------------------------------------------------------------
	// Tests from Boundary Value Analysis
	// No additional tests.  Tests from previous approaches are sufficient
	// --------------------------------------------------------------------
	
	// --------------------------------------------------------------------
	// Tests from Equivalence Class Partitioning
	// --------------------------------------------------------------------
	@Test
	public void Test8PublicPrivateAppts() throws Exception {
		String publicValue = Prefs.getPref(PrefName.SHOWPUBLIC);
		Prefs.putPref(PrefName.SHOWPUBLIC, "true");
		
		String privateValue = Prefs.getPref(PrefName.SHOWPRIVATE);
		Prefs.putPref(PrefName.SHOWPRIVATE, "true");
		
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(publicAppt.getKey());
		l.add(privateAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",2, d.getItems().size());
		String observedItemLabel1 = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		String observedItemLabel2 = ((TreeSet<CalendarEntity>) d.getItems()).last().getText();
		assertEquals("Appointment Text 1", privateAppt.getText(), observedItemLabel1);
		assertEquals("Appointment Text 2", publicAppt.getText(), observedItemLabel2);
		
		Prefs.putPref(PrefName.SHOWPUBLIC, publicValue);
		Prefs.putPref(PrefName.SHOWPRIVATE, privateValue);		
	}
	
	@Test
	public void Test9NoHolidayNoVacation() throws Exception {
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(publicAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",1, d.getItems().size());
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		int observedHoliday = d.getHoliday();
		int observedVacation = d.getVacation();
		assertEquals("Appointment Text", publicAppt.getText(), observedItemLabel);
		assertEquals("Holiday Flag", 0, observedHoliday);
		assertEquals("Vacation Flag", 0, observedVacation);
		
	}
	
	@Test
	public void Test10HolidayVacation() throws Exception {
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(vacationHolidayAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",1, d.getItems().size());
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		int observedHoliday = d.getHoliday();
		int observedVacation = d.getVacation();
		assertEquals("Appointment Text", vacationHolidayAppt.getText(), observedItemLabel);
		assertEquals("Holiday Flag", 1, observedHoliday);
		assertEquals("Vacation Flag", 1, observedVacation);
		
	}
	
	// --------------------------------------------------------------------
	// Tests from Predicate Analysis
	// --------------------------------------------------------------------
	@Test
	public void Test11() throws Exception {
		String publicValue = Prefs.getPref(PrefName.SHOWPUBLIC);
		Prefs.putPref(PrefName.SHOWPUBLIC, "true");
		
		String privateValue = Prefs.getPref(PrefName.SHOWPRIVATE);
		Prefs.putPref(PrefName.SHOWPRIVATE, "true");
		
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(privateAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",1, d.getItems().size());
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Appointment Text", privateAppt.getText(), observedItemLabel);
		
		Prefs.putPref(PrefName.SHOWPUBLIC, publicValue);
		Prefs.putPref(PrefName.SHOWPRIVATE, privateValue);	
	}
	
	@Test
	public void Test12() throws Exception {
		String publicValue = Prefs.getPref(PrefName.SHOWPUBLIC);
		Prefs.putPref(PrefName.SHOWPUBLIC, "true");
		
		String privateValue = Prefs.getPref(PrefName.SHOWPRIVATE);
		Prefs.putPref(PrefName.SHOWPRIVATE, "true");
		
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(publicAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",1, d.getItems().size());
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		assertEquals("Appointment Text", publicAppt.getText(), observedItemLabel);
		
		Prefs.putPref(PrefName.SHOWPUBLIC, publicValue);
		Prefs.putPref(PrefName.SHOWPRIVATE, privateValue);	
	}
	
	@Test
	public void Test13() throws Exception {
		String publicValue = Prefs.getPref(PrefName.SHOWPUBLIC);
		Prefs.putPref(PrefName.SHOWPUBLIC, "false");
		
		String privateValue = Prefs.getPref(PrefName.SHOWPRIVATE);
		Prefs.putPref(PrefName.SHOWPRIVATE, "true");
		
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(publicAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection", 0, d.getItems().size());
		
		Prefs.putPref(PrefName.SHOWPUBLIC, publicValue);
		Prefs.putPref(PrefName.SHOWPRIVATE, privateValue);	
	}
	
	@Test
	public void Test14() throws Exception {
		String publicValue = Prefs.getPref(PrefName.SHOWPUBLIC);
		Prefs.putPref(PrefName.SHOWPUBLIC, "true");
		
		String privateValue = Prefs.getPref(PrefName.SHOWPRIVATE);
		Prefs.putPref(PrefName.SHOWPRIVATE, "false");
		
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(privateAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection", 0, d.getItems().size());
		
		Prefs.putPref(PrefName.SHOWPUBLIC, publicValue);
		Prefs.putPref(PrefName.SHOWPRIVATE, privateValue);	
	}
	
	@Test
	public void Test15() throws Exception {
		String publicValue = Prefs.getPref(PrefName.SHOWPUBLIC);
		Prefs.putPref(PrefName.SHOWPUBLIC, "false");
		
		String privateValue = Prefs.getPref(PrefName.SHOWPRIVATE);
		Prefs.putPref(PrefName.SHOWPRIVATE, "false");
		
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(privateAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection", 0, d.getItems().size());
		
		Prefs.putPref(PrefName.SHOWPUBLIC, publicValue);
		Prefs.putPref(PrefName.SHOWPRIVATE, privateValue);	
	}
	
	@Test
	public void Test16() throws Exception {
		String publicValue = Prefs.getPref(PrefName.SHOWPUBLIC);
		Prefs.putPref(PrefName.SHOWPUBLIC, "false");
		
		String privateValue = Prefs.getPref(PrefName.SHOWPRIVATE);
		Prefs.putPref(PrefName.SHOWPRIVATE, "false");
		
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(publicAppt.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection", 0, d.getItems().size());
		
		Prefs.putPref(PrefName.SHOWPUBLIC, publicValue);
		Prefs.putPref(PrefName.SHOWPRIVATE, privateValue);	
	}
}

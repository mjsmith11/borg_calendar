package net.sf.borg.test.mjs;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import net.sf.borg.model.db.DBHelper;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.entity.CheckList;
import net.sf.borg.model.entity.Task;
import net.sf.borg.model.entity.Appointment;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Day;
import net.sf.borg.model.TaskModel;

public class GetDayTests {

	@BeforeClass
	public static void setUp() throws Exception {
		// open the borg dbs - in memory
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:hsqldb:mem:days");

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
	@Test
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
	
	@Test
	public void Test14MonthUpperBoundryLess() throws Exception {
		CheckDayForAppointment(2009,10,2,2009,10,2);
	}
	
	//DayLowerBoundaryGreater is covered by Test9MonthLowerBoundaryGreater
	
	@Test
	public void Test15DayLowerBoundaryAt() throws Exception {
		CheckDayForAppointment(2009,1,1,2009,1,1);
	}
	
	@Test
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
	
	@Test
	public void Test19YearUpperBoundryLess() throws Exception {
		CheckDayForAppointment(2009,2,30,2009,2,30);
	}
	
	/// Helper method for boundary value tests
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
	
	// --------------------------------------------------------------------
	// Tests from Equivalence Partitioning
	// **At this time, previous test cases cover all equivalence classes
	// and there are no additional test cases to add here
	// --------------------------------------------------------------------
	
	// --------------------------------------------------------------------
	// Tests from Predicate Analysis
	// --------------------------------------------------------------------
	
	@Test
	public void Test20to23Halloween() throws Exception{
		CheckDayForHoliday("true","",9,31,2017,Resource.getResourceString("Halloween")); 				//20
		CheckDayForHoliday("false","",9,31,2017,""); 													//21
		CheckDayForHoliday("true","",10,31,2017,""); 													//22
		CheckDayForHoliday("true","",9,30,2017,""); 													//23
	}
	
	@Test
	public void Test24to27IndependenceDay() throws Exception{
		CheckDayForHoliday("true","",6,4,2017,Resource.getResourceString("Independence_Day"));			//24
		CheckDayForHoliday("false","",6,4,2017,"");														//25
		CheckDayForHoliday("true","",7,4,2017,"");														//26
		CheckDayForHoliday("true","",6,5,2017,"");														//27
	}
	
	@Test
	public void Test28to31GroundhogDay() throws Exception{
		CheckDayForHoliday("true","",1,2,2017,Resource.getResourceString("Ground_Hog_Day"));			//28
		CheckDayForHoliday("false","",1,2,2017,"");														//29
		CheckDayForHoliday("true","",2,2,2017,"");														//30
		CheckDayForHoliday("true","",1,3,2017,"");														//31
	}
	
	@Test
	public void Test32to35ValentinesDay() throws Exception{
		CheckDayForHoliday("true","",1,14,2017,Resource.getResourceString("Valentine's_Day"));			//32
		CheckDayForHoliday("false","",1,14,2017,"");													//33
		CheckDayForHoliday("true","",2,14,2017,"");														//34
		CheckDayForHoliday("true","",1,15,2017,"");														//35
	}
		
	@Test
	public void Test36to39StPatricksDay() throws Exception{
		CheckDayForHoliday("true","",2,17,2017,Resource.getResourceString("St._Patrick's_Day"));		//36
		CheckDayForHoliday("false","",2,17,2017,"");													//37
		CheckDayForHoliday("true","",3,17,2017,"");														//38
		CheckDayForHoliday("true","",2,18,2017,"");														//39
	}
	
	@Test
	public void Test40to43VeteransDay() throws Exception{
		//false is needed for Canadian Holidays to exclude Remembrance Day
		CheckDayForHoliday("true","false",10,11,2017,Resource.getResourceString("Veteran's_Day"));		//40
		CheckDayForHoliday("false","false",10,11,2017,"");												//41
		CheckDayForHoliday("true","",11,11,2017,"");													//42
		CheckDayForHoliday("true","",10,12,2017,"");													//43
	}
	
	@Test
	public void Test44to47LaborDay() throws Exception{
		//must include false for Canadian holidays to exclude Canadian Labour Day and Civic Holiday.
		CheckDayForHoliday("true","false",8,4,2017,Resource.getResourceString("Labor_Day"));			//44
		CheckDayForHoliday("false","false",8,4,2017,"");												//45
		CheckDayForHoliday("true","false",7,7,2017,"");													//46
		CheckDayForHoliday("true","",8,5,2017,"");														//47
	}
	
	@Test
	public void Test48to51MLKDay() throws Exception{
		CheckDayForHoliday("true","",0,15,2018,Resource.getResourceString("Martin_Luther_King_Day"));	//48
		CheckDayForHoliday("false","",0,15,2018,"");													//49
		CheckDayForHoliday("true","",2,19,2018,"");														//50
		CheckDayForHoliday("true","",0,16,2018,"");														//51
	}
	
	@Test
	public void Test52to55PresidentsDay() throws Exception{
		CheckDayForHoliday("true","",1,20,2017,Resource.getResourceString("Presidents_Day"));			//52
		CheckDayForHoliday("false","",1,20,2017,"");													//53
		CheckDayForHoliday("true","",10,20,2017,"");													//54
		CheckDayForHoliday("true","",1,21,2017,"");														//55
	}
	
	@Test
	public void Test56to59MemorialDay() throws Exception{
		CheckDayForHoliday("true","",4,29,2017,Resource.getResourceString("Memorial_Day"));				//56
		CheckDayForHoliday("false","",4,29,2017,"");													//57
		CheckDayForHoliday("true","",5,26,2017,"");														//58
		CheckDayForHoliday("true","",4,30,2017,"");														//59
	}
	
	@Test
	public void Test60to63ColumbusDay() throws Exception{
		//false is required for canadian holidays because 9,9 is canadian thanksgiving.
		CheckDayForHoliday("true","false",9,9,2017,Resource.getResourceString("Columbus_Day"));			//60
		CheckDayForHoliday("false","false",9,9,2017,"");												//61
		CheckDayForHoliday("true","",10,13,2017,"");													//62
		CheckDayForHoliday("true","",9,10,2017,"");														//63
	}
	
	@Test
	public void Test64to67MothersDay() throws Exception{
		CheckDayForHoliday("true","",4,14,2017,Resource.getResourceString("Mother's_Day"));				//64
		CheckDayForHoliday("false","",4,14,2017,"");													//65
		CheckDayForHoliday("true","",5,11,2017,"");														//66
		CheckDayForHoliday("true","",4,15,2017,"");														//67
	}
	
	@Test
	public void Test68to71FathersDay() throws Exception{
		CheckDayForHoliday("true","",5,18,2017,Resource.getResourceString("Father's_Day"));				//68
		CheckDayForHoliday("false","",5,18,2017,"");													//69
		CheckDayForHoliday("true","",6,16,2017,"");														//70
		CheckDayForHoliday("true","",5,19,2017,"");														//71
	}
	
	@Test
	public void Test72to75Thanksgiving() throws Exception{
		CheckDayForHoliday("true","",10,23,2017,Resource.getResourceString("Thanksgiving"));			//72
		CheckDayForHoliday("false","",10,23,2017,"");													//73
		CheckDayForHoliday("true","",11,28,2017,"");													//74
		CheckDayForHoliday("true","",10,24,2017,"");													//75
	}
	
	@Test
	public void Test76to79CanadaDay() throws Exception{
		CheckDayForHoliday("","true",6,1,2017,Resource.getResourceString("Canada_Day"));				//76
		CheckDayForHoliday("","false",6,1,2017,"");														//77
		CheckDayForHoliday("","true",7,1,2017,"");														//78
		CheckDayForHoliday("","true",6,2,2017,"");														//79
	}
	
	@Test
	public void Test80to83BoxingDay() throws Exception{
		CheckDayForHoliday("","true",11,26,2017,Resource.getResourceString("Boxing_Day"));				//80
		CheckDayForHoliday("","false",11,26,2017,"");													//81
		CheckDayForHoliday("","true",10,26,2017,"");													//82
		CheckDayForHoliday("","true",11,27,2017,"");													//83
	}
	
	@Test
	public void Test84to87CivicHoliday() throws Exception{
		CheckDayForHoliday("","true",7,7,2017,Resource.getResourceString("Civic_Holiday"));				//84
		CheckDayForHoliday("","false",7,7,2017,"");														//85
		CheckDayForHoliday("","true",10,6,2017,"");														//86
		CheckDayForHoliday("","true",7,8,2017,"");														//87
	}
	
	@Test
	public void Test88to91RemembranceDay() throws Exception{
		CheckDayForHoliday("false","true",10,11,2017,Resource.getResourceString("Remembrance_Day"));	//88
		CheckDayForHoliday("false","false",10,11,2017,"");												//89
		CheckDayForHoliday("","true",11,11,2017,"");													//90
		CheckDayForHoliday("","true",10,12,2017,"");													//91
	}
	
	@Test
	public void Test92to95LabourDayCan() throws Exception{
		CheckDayForHoliday("false","true",8,4,2017,Resource.getResourceString("Labour_Day_(Can)"));		//92
		CheckDayForHoliday("false","false",8,4,2017,"");												//93
		CheckDayForHoliday("","true",9,2,2017,"");														//94
		CheckDayForHoliday("","true",8,5,2017,"");														//95
	}
	
	@Test
	public void Test96to99CommonwealthDay() throws Exception{
		CheckDayForHoliday("","true",2,13,2017,Resource.getResourceString("Commonwealth_Day"));			//96
		CheckDayForHoliday("","false",2,13,2017,"");													//97
		CheckDayForHoliday("","true",10,13,2017,"");													//98
		CheckDayForHoliday("","true",2,14,2017,"");														//99
	}
	
	@Test
	public void Test100to103ThanksgivingCan() throws Exception{
		CheckDayForHoliday("false","true",9,9,2017,Resource.getResourceString("Thanksgiving_(Can)"));	//100
		CheckDayForHoliday("false","false",9,9,2017,"");												//101
		CheckDayForHoliday("","true",10,13,2017,"");													//102
		CheckDayForHoliday("","true",9,10,2017,"");														//103
	}
	
	@Test
	public void Test104to108NewYears() throws Exception {
		CheckDayForHoliday("true","false",0,1,2017,Resource.getResourceString("New_Year's_Day"));		//104
		CheckDayForHoliday("false","true",0,1,2017,Resource.getResourceString("New_Year's_Day"));		//105
		CheckDayForHoliday("false","false",0,1,2017,"");												//106
		CheckDayForHoliday("true","false",1,1,2017,"");													//107
		CheckDayForHoliday("true","false",0,2,2017,"");													//108
	}
	
	@Test
	public void Test109to113Christmas() throws Exception {
		CheckDayForHoliday("true","false",11,25,2017,Resource.getResourceString("Christmas"));			//109
		CheckDayForHoliday("false","true",11,25,2017,Resource.getResourceString("Christmas"));			//110
		CheckDayForHoliday("false","false",11,25,2017,"");												//111
		CheckDayForHoliday("true","false",10,25,2017,"");												//112
		CheckDayForHoliday("true","false",11,24,2017,"");												//113
	}
	
	@Test
	public void Test114to119VictoriaDay() throws Exception {
		CheckDayForHoliday("","true",4,22,2017,Resource.getResourceString("Victoria_Day"));				//114
		CheckDayForHoliday("","false",4,22,2017,"");													//115
		CheckDayForHoliday("","true",4,23,2017,"");														//116
		CheckDayForHoliday("","true",4,15,2017,"");														//117
		CheckDayForHoliday("false","true",4,29,2017,"");												//118
		CheckDayForHoliday("","true",5,19,2017,"");														//119
	}
	
	
	///Helper Method for Predicate Analysis Tests
	///1. Set SHOWUSHOLIDAYS to showUS if it isn't ""
	///2. Set SHOWCANHOLIDAYS to showCAN if it isn't ""
	///3. getDay for month, day, and year
	///4. If expectedHoliday is "", assert that there are no items on the returned day
	///5. If expectedHoliday is not "", assert that the specified holiday is returned on the day
	///6. Restore SHOWUSHOLIDAYS and SHOWCANHOLIDAYS if they were changed
	private void CheckDayForHoliday(String showUS, String showCAN, int month, int day, int year, String expectedHoliday) throws Exception {
		String backupShowUS="";
		String backupShowCAN="";
		if(!showUS.equals("")) {
			backupShowUS = Prefs.getPref(PrefName.SHOWUSHOLIDAYS);
			Prefs.putPref(PrefName.SHOWUSHOLIDAYS, showUS);
		}
		if(!showCAN.equals("")) {
			backupShowCAN = Prefs.getPref(PrefName.SHOWCANHOLIDAYS);
			Prefs.putPref(PrefName.SHOWCANHOLIDAYS, showCAN);
		}
		
		Day d = Day.getDay(year, month, day);
		if(expectedHoliday.equals(""))
		{
			assertEquals("No items should exist on the day",0,d.getItems().size());
		}
		else
		{
			String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
			assertEquals("Expected holiday not found",expectedHoliday,observedItemLabel);
		}
		
		if(!showUS.equals("")) {;
			Prefs.putPref(PrefName.SHOWUSHOLIDAYS, backupShowUS);
		}
		if(!showCAN.equals("")) {
			Prefs.putPref(PrefName.SHOWCANHOLIDAYS, backupShowCAN);
		}
	}
	
	// --------------------------------------------------------------------
	// Tests Finite State Model Analysis
	// --------------------------------------------------------------------
	@Test
	public void Test120() throws Exception{
		Day d = getFSMDay("v",10,1,2017);
		//State q1
		assertEquals("Holiday Flag",0,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test121() throws Exception{
		Day d = getFSMDay("h",10,2,2017);
		//State q2
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",0,d.getVacation());
	}
	
	@Test
	public void Test122() throws Exception{
		Day d = getFSMDay("vv",10,3,2017);
		//State q1
		assertEquals("Holiday Flag",0,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test123() throws Exception{
		Day d = getFSMDay("vh",10,4,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test124() throws Exception{
		Day d = getFSMDay("hv",10,5,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test125() throws Exception{
		Day d = getFSMDay("hh",10,6,2017);
		//State q2
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",0,d.getVacation());
	}
	
	@Test
	public void Test126() throws Exception{
		Day d = getFSMDay("vvv",10,7,2017);
		//State q1
		assertEquals("Holiday Flag",0,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test127() throws Exception{
		Day d = getFSMDay("vvh",10,8,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test128() throws Exception{
		Day d = getFSMDay("vhv",10,9,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test129() throws Exception{
		Day d = getFSMDay("vhh",10,10,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test130() throws Exception{
		Day d = getFSMDay("hvv",10,11,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test131() throws Exception{
		Day d = getFSMDay("hvh",10,12,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test132() throws Exception{
		Day d = getFSMDay("hhv",10,13,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test133() throws Exception{
		Day d = getFSMDay("hhh",10,14,2017);
		//State q2
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",0,d.getVacation());
	}
	
	@Test
	public void Test134() throws Exception{
		Day d = getFSMDay("hvvv",10,15,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test135() throws Exception{
		Day d = getFSMDay("hvvh",10,16,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test136() throws Exception{
		Day d = getFSMDay("hvhv",10,17,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	@Test
	public void Test137() throws Exception{
		Day d = getFSMDay("hvhh",10,11,2017);
		//State q3
		assertEquals("Holiday Flag",1,d.getHoliday());
		assertEquals("Vacation Flag",1,d.getVacation());
	}
	
	///Helper for Finite State Model Testing
	///Loops through the provided input left to right
	///creating a vacation appointment for each v and
	///a holiday appointment for each h on the day provided
	///in month, day, and year.  Then it returns the Day for
	///that month, day, and year.  Last, it deletes all of the appointments 
	// it created.
	private Day getFSMDay(String input, int month, int day, int year) throws Exception {
		List<Appointment> appts = new ArrayList<Appointment>();
		for(int i = 0; i<input.length(); i++)
		{
			Appointment a = new Appointment();
			Calendar date = new GregorianCalendar(year, month, day);
			a.setText("Appointment "+i);
			a.setDate(date.getTime());
			if(input.charAt(i)=='v')
				a.setVacation(1);
			else if(input.charAt(i)=='h')
				a.setHoliday(1);
			AppointmentModel.getReference().saveAppt(a);
			appts.add(a);
		}
		Day ret = Day.getDay(year, month, day);
		for (Appointment app : appts)
		{
			AppointmentModel.getReference().delAppt(app);
		}
		return ret;
	}
	
}

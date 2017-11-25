package net.sf.borg.test.mjs;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.model.Day;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.CalendarEntity;
import net.sf.borg.model.entity.Task;

public class GetDayTestsV2 {

	@BeforeClass
	public static void setUp() throws Exception {
		// open the borg dbs - in memory
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:hsqldb:mem:whatever2");

	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		DBHelper.getController().close();
		
	}

	@Test
	public void Test138VictoriaDay() throws Exception{
		CheckDayForHoliday("","true",4,25,2014,"");	
	}
	
	@Test
	public void Test139Tasks() throws Exception
	{
		String taskDesc = "Task1";
		Task t = new Task();
		Calendar TDate=new GregorianCalendar(2017,5,13);
		t.setDueDate(TDate.getTime());
		t.setStartDate(TDate.getTime());
		t.setDescription(taskDesc);
		t.setState("Not Started");
		t.setType("New");
		TaskModel.getReference().savetask(t);
		
		Day d = Day.getDay(2017,5,13);
		Task t1 = (Task)(((TreeSet<CalendarEntity>) d.getItems()).first());
		assertEquals(taskDesc, t1.getDescription());
	}
	
	///Helper Method
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
}

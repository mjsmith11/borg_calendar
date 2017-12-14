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

public class addToDayTestsV2 {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// open the borg dbs - in memory		
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:hsqldb:mem:appointments2");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		DBHelper.getController().close();
	}

	@Test
	public void Test35Colors() throws Exception {
		Calendar ApptDate = new GregorianCalendar(2017, 5, 17);
		Appointment a= AppointmentModel.getReference().newAppt();
		a.setText("My Public Appt");
		a.setDate(ApptDate.getTime());
		a.setPrivate(false);
		a.setColor("green");
		AppointmentModel.getReference().saveAppt(a);
		
		String publicValue = Prefs.getPref(PrefName.SHOWPUBLIC);
		Prefs.putPref(PrefName.SHOWPUBLIC, "true");
		
		Day d = Day.getNewDay();
		List<Integer> l = new ArrayList<Integer>();
		l.add(a.getKey());
		Day.TestAddToDay(d, l);
		
		assertEquals("Items in collection",1, d.getItems().size());
		String observedItemLabel = ((TreeSet<CalendarEntity>) d.getItems()).first().getText();
		String observedColor = ((TreeSet<CalendarEntity>) d.getItems()).first().getColor();
		
		assertEquals("Appointment Text", a.getText(), observedItemLabel);
		assertEquals("Appointment Color", a.getColor(), observedColor);
		
		Prefs.putPref(PrefName.SHOWPUBLIC, publicValue);
	}

}

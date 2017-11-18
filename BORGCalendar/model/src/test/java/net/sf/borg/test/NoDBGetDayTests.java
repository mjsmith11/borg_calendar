package net.sf.borg.test;

import static org.junit.Assert.*;


import org.junit.Test;

import net.sf.borg.model.Day;

public class NoDBGetDayTests {

	@Test(expected=java.lang.ExceptionInInitializerError.class)
	public void ClosedDatabaseConnection() throws Exception {
		Day d = Day.getDay(2017, 11, 9);
		
	}
}

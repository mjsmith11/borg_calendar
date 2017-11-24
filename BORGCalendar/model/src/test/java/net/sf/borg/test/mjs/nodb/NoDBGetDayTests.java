package net.sf.borg.test.mjs.nodb;

import static org.junit.Assert.*;

import java.lang.ExceptionInInitializerError;

import org.junit.Test;

import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.Day;

public class NoDBGetDayTests {
	// --------------------------------------------------------------------
	// Tests from Specification Analysis
	// --------------------------------------------------------------------
	
	//This actually causes a null pointer exception, but something is causing JUnit to see ExceptionInInitializerError
	@Test(expected=ExceptionInInitializerError.class)
	public void Test8NoDatabaseConnection() throws Exception {
		AppointmentModel.getReference().refresh();
		Day d = Day.getDay(2017, 11, 9);
		
	}
}

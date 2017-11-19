package net.sf.borg.test;

import static org.junit.Assert.*;

import java.lang.ExceptionInInitializerError;

import org.junit.Test;

import net.sf.borg.model.Day;

public class NoDBGetDayTests {

	//This actually causes a null pointer exception, but something is causing JUnit to see ExceptionInInitializerError
	@Test(expected=ExceptionInInitializerError.class)
	public void NoDatabaseConnection() throws Exception {
		Day d = Day.getDay(2017, 11, 9);
		
	}
}

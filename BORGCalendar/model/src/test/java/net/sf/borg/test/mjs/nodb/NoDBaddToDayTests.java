package net.sf.borg.test.mjs.nodb;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import net.sf.borg.model.Day;

public class NoDBaddToDayTests {

	@Test(expected=ExceptionInInitializerError.class)
	public void Test1NoDBConnection() throws Exception {
		Collection<Integer> list = new ArrayList<Integer>();
		list.add(1);
		Day.TestAddToDay(Day.getNewDay(), list);
	}

}

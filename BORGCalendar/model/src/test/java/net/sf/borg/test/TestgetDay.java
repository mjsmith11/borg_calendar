package net.sf.borg.test;

import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.CheckList;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.borg.model.Day;

public class TestgetDay {

	@BeforeClass
	public static void setUp() throws Exception {
		// open the borg dbs - in memory
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:hsqldb:mem:whatever");
		
	}

	@Test
	public void test() throws Exception{
		Day d = Day.getDay(2014, 3, 24);
	}

}

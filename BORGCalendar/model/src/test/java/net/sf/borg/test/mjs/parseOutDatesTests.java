package net.sf.borg.test.mjs;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.borg.model.entity.Memo;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;

public class parseOutDatesTests {
	
	final private int YEAR_OFFSET = 1900;
	final private int MONTH_OFFSET = 1;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// open the borg dbs - in memory
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:hsqldb:mem:whatever");
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		DBHelper.getController().close();
		
	}
	// --------------------------------------------------------------------
	// Tests from Specification Analysis
	// --------------------------------------------------------------------
	
	@Test
	public void Test1NullMemoText() throws Exception {
		Memo m = new Memo();
		String name = "my memo 1";
		m.setMemoName(name);
		CreateMemo(m);
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date",result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		assertNull("Memo Text", result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test2BlankMemoText() throws Exception {
		Memo m = new Memo();
		String name = "my memo 2";
		m.setMemoName(name);
		m.setMemoText("");
		CreateMemo(m);
		
		String expectedText = "";
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date",result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test3CreateDate() throws Exception {
		Memo m = new Memo();
		String name = "my memo 3";
		m.setMemoName(name);
		m.setMemoText("TS;12/10/2017 09:23 PM;12/20/2017 09:23 AM;");
		CreateMemo(m);
		
		String expectedText = "";
		Date expectedCreated = new Date(2017 - YEAR_OFFSET, 12 - MONTH_OFFSET, 10, 21, 23);
		Date expectedUpdated = new Date(2017 - YEAR_OFFSET, 12 - MONTH_OFFSET, 20, 9, 23);
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertEquals("Created Date",expectedCreated,result.getCreated());
		assertEquals("Updated Date", expectedUpdated, result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test4NoTimestamps() throws Exception {
		Memo m = new Memo();
		String name = "my memo 4";
		m.setMemoName(name);
		m.setMemoText("My memo text");
		CreateMemo(m);
		
		String expectedText = "My memo text";
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date",result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
	}
	
	
	private void CreateMemo(Memo m) throws Exception {
		MemoModel.getReference().getDB().addMemo(m);
	}

}

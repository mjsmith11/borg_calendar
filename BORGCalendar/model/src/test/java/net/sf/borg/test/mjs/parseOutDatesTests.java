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
	final private String VALID_DATE_S = "05/06/2005 6:30 PM;";
	final private Date VALID_DATE_D = new Date(2005 - YEAR_OFFSET, 5 - MONTH_OFFSET, 6, 18, 30);
	final private String INVALID_DATE = "Not a date";

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
	
	
	// --------------------------------------------------------------------
	// Tests from Boundary Value Analysis
	// Covered by Tests 1 and 2
	// --------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------
	// Tests from Equivalence Class Partitioning
	// --------------------------------------------------------------------
	@Test
	public void Test5ValidTimestampsPlusText() throws Exception {
		Memo m = new Memo();
		String name = "my memo 5";
		m.setMemoName(name);
		m.setMemoText("TS;10/10/2017 09:28 PM;10/21/2017 09:25 AM;Hello");
		CreateMemo(m);
		
		String expectedText = "Hello";
		Date expectedCreated = new Date(2017 - YEAR_OFFSET, 10 - MONTH_OFFSET, 10, 21, 28);
		Date expectedUpdated = new Date(2017 - YEAR_OFFSET, 10 - MONTH_OFFSET, 21, 9, 25);
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertEquals("Created Date",expectedCreated,result.getCreated());
		assertEquals("Updated Date", expectedUpdated, result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test6MissingOneTimestamp() throws Exception {
		Memo m = new Memo();
		String name = "my memo 6";
		m.setMemoName(name);
		m.setMemoText("TS;10/10/2017 09:28 PM;");
		CreateMemo(m);
		
		String expectedText = "TS;10/10/2017 09:28 PM;";
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test7MissingTwoTimestamps() throws Exception {
		Memo m = new Memo();
		String name = "my memo 7";
		m.setMemoName(name);
		m.setMemoText("TS;");
		CreateMemo(m);
		
		String expectedText = "TS;";
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test8InvalidTimestamps() throws Exception {
		Memo m = new Memo();
		String name = "my memo 8";
		m.setMemoName(name);
		m.setMemoText("TS;10/10 09:28 PM;10/21/2017 25:25 AM;");
		CreateMemo(m);
		
		String expectedText = "TS;10/10 09:28 PM;10/21/2017 25:25 AM;";
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	// --------------------------------------------------------------------
	// Tests from Predicate Analysis
	// --------------------------------------------------------------------
	
	@Test
	public void Test9NoTS() throws Exception {
		Memo m = new Memo();
		String name = "my memo 9";
		m.setMemoName(name);
		m.setMemoText("S;10/10/2017 09:28 PM;10/21/2017 09:25 AM;");
		CreateMemo(m);
		
		String expectedText = "S;10/10/2017 09:28 PM;10/21/2017 09:25 AM;";
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test10NotEnoughSemicolons() throws Exception {
		Memo m = new Memo();
		String name = "my memo 10";
		m.setMemoName(name);
		m.setMemoText("TS;10/10/2017 09:28 PM;10/21/2017 09:25 AM");
		CreateMemo(m);
		
		String expectedText = "TS;10/10/2017 09:28 PM;10/21/2017 09:25 AM";
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test11InvalidCreated() throws Exception {
		Memo m = new Memo();
		String name = "my memo 10";
		m.setMemoName(name);
		m.setMemoText("TS;10//2017 09:28 PM;10/21/2017 09:25 AM;");
		CreateMemo(m);
		
		String expectedText = "TS;10//2017 09:28 PM;10/21/2017 09:25 AM;";
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	
	@Test
	public void Test12InvalidUpdated() throws Exception {
		Memo m = new Memo();
		String name = "my memo 10";
		m.setMemoName(name);
		m.setMemoText("TS;10/05/2017 09:28 PM;10/21/2017;");
		CreateMemo(m);
		
		String expectedText = "TS;10/05/2017 09:28 PM;10/21/2017;";
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		assertEquals("Memo Text",expectedText, result.getMemoText());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	// --------------------------------------------------------------------
	// Tests from Finite State Model Analysis
	// --------------------------------------------------------------------
	
	@Test
	public void Test13() throws Exception {
		Memo m = new Memo();
		String name = "my memo 13";
		m.setMemoName(name);
		m.setMemoText(getFSMString("n"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test14() throws Exception {
		Memo m = new Memo();
		String name = "my memo 14";
		m.setMemoName(name);
		m.setMemoText(getFSMString("nd"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test15() throws Exception {
		Memo m = new Memo();
		String name = "my memo 15";
		m.setMemoName(name);
		m.setMemoText(getFSMString("nn"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test16() throws Exception {
		Memo m = new Memo();
		String name = "my memo 16";
		m.setMemoName(name);
		m.setMemoText(getFSMString("ndn"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test17() throws Exception {
		Memo m = new Memo();
		String name = "my memo 17";
		m.setMemoName(name);
		m.setMemoText(getFSMString("ndd"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test18() throws Exception {
		Memo m = new Memo();
		String name = "my memo 18";
		m.setMemoName(name);
		m.setMemoText(getFSMString("nnd"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test19() throws Exception {
		Memo m = new Memo();
		String name = "my memo 19";
		m.setMemoName(name);
		m.setMemoText(getFSMString("nnn"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test20() throws Exception {
		Memo m = new Memo();
		String name = "my memo 20";
		m.setMemoName(name);
		m.setMemoText(getFSMString("d"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test21() throws Exception {
		Memo m = new Memo();
		String name = "my memo 21";
		m.setMemoName(name);
		m.setMemoText(getFSMString("dd"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertEquals("Created Date", VALID_DATE_D, result.getCreated());
		assertEquals("Updated Date", VALID_DATE_D, result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test22() throws Exception {
		Memo m = new Memo();
		String name = "my memo 22";
		m.setMemoName(name);
		m.setMemoText(getFSMString("dn"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test23() throws Exception {
		Memo m = new Memo();
		String name = "my memo 23";
		m.setMemoName(name);
		m.setMemoText(getFSMString("ddn"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertEquals("Created Date", VALID_DATE_D, result.getCreated());
		assertEquals("Updated Date", VALID_DATE_D, result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test24() throws Exception {
		Memo m = new Memo();
		String name = "my memo 24";
		m.setMemoName(name);
		m.setMemoText(getFSMString("ddd"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertEquals("Created Date", VALID_DATE_D, result.getCreated());
		assertEquals("Updated Date", VALID_DATE_D, result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test25() throws Exception {
		Memo m = new Memo();
		String name = "my memo 25";
		m.setMemoName(name);
		m.setMemoText(getFSMString("dnd"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	@Test
	public void Test26() throws Exception {
		Memo m = new Memo();
		String name = "my memo 26";
		m.setMemoName(name);
		m.setMemoText(getFSMString("dn"));
		CreateMemo(m);
	
		
		Memo result = MemoModel.getReference().getMemo(name);
		assertNull("Created Date", result.getCreated());
		assertNull("Updated Date", result.getUpdated());
		
		MemoModel.getReference().delete(name, false);
				
	}
	
	
	
	
	// --------------------------------------------------------------------
	// Helpers
	// --------------------------------------------------------------------
	
	private void CreateMemo(Memo m) throws Exception {
		MemoModel.getReference().getDB().addMemo(m);
	}
	
	private String getFSMString(String inputSequence) {
		String res = "TS;";
		for(int i = 0; i<inputSequence.length(); i++) {
			if(inputSequence.charAt(i) == 'n') {
				res = res + INVALID_DATE;
			}
			if (inputSequence.charAt(i) == 'd') {
				res = res + VALID_DATE_S;				
			}
		}
		
		return res;
	}

}

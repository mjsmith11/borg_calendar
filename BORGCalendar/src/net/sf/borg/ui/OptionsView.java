/*
 This file is part of BORG.

 BORG is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 BORG is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with BORG; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Copyright 2003 by Mike Berger
 */

package net.sf.borg.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.ui.popup.ReminderTimePanel;
import net.sf.borg.ui.util.GridBagConstraintsFactory;
import net.sf.borg.ui.util.JButtonKnowsBgColor;
import net.sf.borg.ui.util.NwFontChooserS;
import net.sf.borg.ui.util.StripedTable;

// propgui displays the edit preferences window
public class OptionsView extends View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4743111117071445783L;

	// to break a dependency with the contol package
	public interface RestartListener {
		public void restart();
	}

	static private RestartListener rl_ = null; // someone to call to

	// request a

	private static OptionsView singleton = null;

	// restart

	static {
		int rgb = Prefs.getIntPref(PrefName.UCS_STRIPE);
		StripedTable.setStripeColor(new Color(rgb));
	}

	// prompt the user to enter a database directory
	public static String chooseDir(boolean update) {

		String dbdir = null;
		while (true) {
			JFileChooser chooser = new JFileChooser();

			chooser.setCurrentDirectory(new File("."));
			chooser
					.setDialogTitle("Please choose directory for database files");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int returnVal = chooser.showOpenDialog(null);
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return (null);
			}

			dbdir = chooser.getSelectedFile().getAbsolutePath();
			File dir = new File(dbdir);
			String err = null;
			if (!dir.exists()) {
				err = "Database Directory [" + dbdir + "] does not exist";
			} else if (!dir.isDirectory()) {
				err = "Database Directory [" + dbdir + "] is not a directory";
			}

			if (err == null) {
				break;
			}

			Errmsg.notice(err);
		}

		return (dbdir);
	}

	public static void dbSelectOnly() {
		new OptionsView(true).setVisible(true);

	}

	public static OptionsView getReference() {
		if (singleton == null || !singleton.isShowing()) {
			singleton = new OptionsView(false);
		}
		return (singleton);
	}

	static public void setRestartListener(RestartListener rl) {
		rl_ = rl;
	}

	static private void setBooleanPref(JCheckBox box, PrefName pn) {
		if (box.isSelected()) {
			Prefs.putPref(pn, "true");
		} else {
			Prefs.putPref(pn, "false");
		}
	}

	static private void setCheckBox(JCheckBox box, PrefName pn) {
		String val = Prefs.getPref(pn);
		if (val.equals("true")) {
			box.setSelected(true);
		} else {
			box.setSelected(false);
		}
	}

	private javax.swing.JButton applyButton;

	private JPanel applyDismissPanel = null;

	private javax.swing.JButton apptFontButton;

	private JButtonKnowsBgColor btn_ucs_birthdays;

	private JButtonKnowsBgColor btn_ucs_black;

	private JButtonKnowsBgColor btn_ucs_blue;

	private JButtonKnowsBgColor btn_ucs_default;

	private JButtonKnowsBgColor btn_ucs_green;

	private JButtonKnowsBgColor btn_ucs_halfday;

	private JButtonKnowsBgColor btn_ucs_holiday;

	private JButtonKnowsBgColor btn_ucs_holidays;

	private JButtonKnowsBgColor btn_ucs_red;

	private JButton btn_ucs_restore;

	private JButtonKnowsBgColor btn_ucs_stripe;

	private JButtonKnowsBgColor btn_ucs_tasks;

	private JButtonKnowsBgColor btn_ucs_today;

	private JButtonKnowsBgColor btn_ucs_vacation;

	private JButtonKnowsBgColor btn_ucs_weekday;

	private JButtonKnowsBgColor btn_ucs_weekend;

	private JButtonKnowsBgColor btn_ucs_white;

	private javax.swing.JCheckBox canadabox;

	private javax.swing.JCheckBox cb_ucs_marktodo;

	private javax.swing.JCheckBox cb_ucs_ontodo;

	private javax.swing.JSpinner checkfreq;

	private javax.swing.JButton chgdb;

	private javax.swing.JCheckBox colorprint;

	private javax.swing.JCheckBox colorsortbox;

	private javax.swing.JButton dayFontButton = new JButton();

	private javax.swing.JTextField dbDirText;

	private javax.swing.JTextField dbHostText;

	private javax.swing.JTextField dbNameText;

	private javax.swing.JTextField dbPortText;

	private javax.swing.ButtonGroup dbTypeGroup;

	private JPanel dbTypePanel = null;

	private javax.swing.JTextField dbUserText;

	private javax.swing.JButton defFontButton;

	private javax.swing.JButton dismissButton;

	private JCheckBox doyBox = null;

	private javax.swing.JCheckBox emailbox;

	private javax.swing.JTextField emailtext;

	private JSpinner emailtimebox = null;

	private javax.swing.JCheckBox holiday1;

	private JRadioButton hsqldbButton;

	private JPanel hsqldbPanel;

	private javax.swing.JCheckBox iso8601Box = new JCheckBox();

	private JRadioButton jdbcButton = null;

	private JPanel jdbcPanel = null;

	private JTextField jdbcText = null;

	private javax.swing.JLabel jLabel1;

	private javax.swing.JLabel jLabel15;

	private javax.swing.JLabel jLabel16;

	private javax.swing.JLabel jLabel17;

	private javax.swing.JLabel jLabel18;

	private javax.swing.JLabel jLabel19;

	private javax.swing.JLabel jLabel2;

	private javax.swing.JLabel jLabel20;

	private javax.swing.JLabel jLabel4;

	private javax.swing.JLabel jLabel5;

	private javax.swing.JLabel jLabel6;

	private javax.swing.JLabel jLabel7;

	private javax.swing.JLabel jLabel8;

	private JPanel jPanelUCS = null;

	private javax.swing.JPasswordField jPasswordField1;

	private javax.swing.JSeparator jSeparator1;

	private javax.swing.JTabbedPane jTabbedPane1;

	private javax.swing.JComboBox lnfBox;

	private javax.swing.JComboBox localebox;

	private javax.swing.JCheckBox miltime;

	private javax.swing.JCheckBox mondaycb;

	private javax.swing.JButton monthFontButton = new JButton();

	private JRadioButton MySQLButton = null;

	private javax.swing.JPanel mysqlPanel;

	private javax.swing.JCheckBox popenablebox;

	private javax.swing.JCheckBox privbox;

	private javax.swing.JCheckBox pubbox;

	private JLabel remtimelabel = null;

	private ReminderTimePanel remTimePanel = new ReminderTimePanel();

	private javax.swing.JTextField smtptext;
	
	private JTextField smtpport = new JTextField();

	private javax.swing.JCheckBox soundbox;

	private javax.swing.JCheckBox splashbox;

	private javax.swing.JCheckBox stackbox;

	private javax.swing.JTextField tf_ucs_marker;

	private JPanel topPanel = null;

	private JCheckBox truncbox = null;

	private JCheckBox useBeep = null;

	private javax.swing.JButton weekFontButton = new JButton();

	private javax.swing.JComboBox wkendhr;

	private javax.swing.JComboBox wkstarthr;

	JTextField hsqldbdir = new JTextField();

	JTextField backupDir = new JTextField();

	JPasswordField smpw = new JPasswordField();

	JTextField socketPort = new JTextField();

	JTextField usertext = new JTextField();

	JCheckBox useSysTray = new JCheckBox();

	JCheckBox dock = new JCheckBox();

	JCheckBox hide_strike_box = new JCheckBox();

	// dbonly will only allow db changes
	private OptionsView(boolean dbonly) {
		super();

		initComponents();
		dbTypeGroup = new javax.swing.ButtonGroup();
		dbTypeGroup.add(hsqldbButton);
		dbTypeGroup.add(MySQLButton);
		dbTypeGroup.add(jdbcButton);

		if (!dbonly) {
			addModel(AppointmentModel.getReference());
		} else {
			setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		}

		// set the various screen items based on the existing user
		// preferences

		int emmins = Prefs.getIntPref(PrefName.EMAILTIME);
		Calendar cal = new GregorianCalendar(1980, 1, 1, 0, 0, 0);
		cal.add(Calendar.MINUTE, emmins);
		emailtimebox.setValue(cal.getTime());

		//
		// database
		//
		String dbtype = Prefs.getPref(PrefName.DBTYPE);
		if (dbtype.equals("mysql")) {
			MySQLButton.setSelected(true);
		} else if (dbtype.equals("hsqldb")) {
			hsqldbButton.setSelected(true);
		} else {
			jdbcButton.setSelected(true);
		}
		dbTypeChange(dbtype);

		dbNameText.setText(Prefs.getPref(PrefName.DBNAME));
		dbPortText.setText(Prefs.getPref(PrefName.DBPORT));
		dbHostText.setText(Prefs.getPref(PrefName.DBHOST));
		dbUserText.setText(Prefs.getPref(PrefName.DBUSER));
		jPasswordField1.setText(Prefs.getPref(PrefName.DBPASS));
		jdbcText.setText(Prefs.getPref(PrefName.JDBCURL));
		hsqldbdir.setText(Prefs.getPref(PrefName.HSQLDBDIR));
		backupDir.setText(Prefs.getPref(PrefName.BACKUPDIR));

		if (dbonly) {
			// disable lots of non-db-related stuff
			jTabbedPane1.setEnabledAt(0, false);
			jTabbedPane1.setEnabledAt(1, false);
			jTabbedPane1.setEnabledAt(3, false);
			jTabbedPane1.setEnabledAt(4, false);
			jTabbedPane1.setEnabledAt(5, false);
			jTabbedPane1.setEnabledAt(6, false);
			jTabbedPane1.setEnabledAt(7, false);

			jTabbedPane1.setSelectedIndex(2);
			dismissButton.setEnabled(false);
			applyButton.setEnabled(false);

			return;

		}

		// set various simple boolean checkboxes
		setCheckBox(colorprint, PrefName.COLORPRINT);
		setCheckBox(pubbox, PrefName.SHOWPUBLIC);
		setCheckBox(privbox, PrefName.SHOWPRIVATE);
		setCheckBox(emailbox, PrefName.EMAILENABLED);
		setCheckBox(holiday1, PrefName.SHOWUSHOLIDAYS);
		setCheckBox(canadabox, PrefName.SHOWCANHOLIDAYS);
		setCheckBox(doyBox, PrefName.DAYOFYEAR);
		setCheckBox(colorsortbox, PrefName.COLORSORT);
		setCheckBox(miltime, PrefName.MILTIME);
		setCheckBox(splashbox, PrefName.SPLASH);
		setCheckBox(stackbox, PrefName.STACKTRACE);
		setCheckBox(popenablebox, PrefName.REMINDERS);
		setCheckBox(soundbox, PrefName.BEEPINGREMINDERS);
		setCheckBox(useBeep, PrefName.USESYSTEMBEEP);
		setCheckBox(truncbox, PrefName.TRUNCAPPT);
		setCheckBox(iso8601Box, PrefName.ISOWKNUMBER);
		// setCheckBox(extraDayBox, PrefName.SHOWEXTRADAYS);
		setCheckBox(useSysTray, PrefName.USESYSTRAY);
		setCheckBox(taskAbbrevBox, PrefName.TASK_SHOW_ABBREV);
		setCheckBox(calShowTaskBox, PrefName.CAL_SHOW_TASKS);
		setCheckBox(calShowSubtaskBox, PrefName.CAL_SHOW_SUBTASKS);
		setCheckBox(ganttShowSubtaskBox, PrefName.GANTT_SHOW_SUBTASKS);
		setCheckBox(dock, PrefName.DOCKPANELS);
		setCheckBox(hide_strike_box, PrefName.HIDESTRIKETHROUGH);

		int socket = Prefs.getIntPref(PrefName.SOCKETPORT);
		socketPort.setText(Integer.toString(socket));

		// email server and address
		smtptext.setText(Prefs.getPref(PrefName.EMAILSERVER));
		smtpport.setText(Prefs.getPref(PrefName.EMAILPORT));
		emailtext.setText(Prefs.getPref(PrefName.EMAILADDR));
		usertext.setText(Prefs.getPref(PrefName.EMAILUSER));
		smpw.setText(Prefs.getPref(PrefName.EMAILPASS));

		int fdow = Prefs.getIntPref(PrefName.FIRSTDOW);
		if (fdow == Calendar.MONDAY) {
			mondaycb.setSelected(true);
		} else {
			mondaycb.setSelected(false);
		}

		// add installed look and feels to lnfBox
		lnfBox.removeAllItems();
		TreeSet<String> lnfs = new TreeSet<String>();
		String curlnf = Prefs.getPref(PrefName.LNF);
		LookAndFeelInfo lnfinfo[] = UIManager.getInstalledLookAndFeels();
		for (int i = 0; i < lnfinfo.length; i++) {
			String name = lnfinfo[i].getClassName();
			lnfs.add(name);
		}
		try {
			Class.forName("com.jgoodies.looks.plastic.PlasticLookAndFeel");
			lnfs.add("com.jgoodies.looks.plastic.PlasticLookAndFeel");
		} catch (Exception e) {
		}
		try {
			Class.forName("com.jgoodies.looks.windows.WindowsLookAndFeel");
			lnfs.add("com.jgoodies.looks.windows.WindowsLookAndFeel");
		} catch (Exception e) {
		}
		try {
			Class.forName("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
			lnfs.add("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
		} catch (Exception e) {
		}
		try {
			Class.forName("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
			lnfs.add("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
		} catch (Exception e) {
		}
		try {
			Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
			lnfs.add("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
		} catch (Exception e) {
		}
		try {
			Class
					.forName("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
			lnfs
					.add("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
		} catch (Exception e) {
		}

		lnfs.add(curlnf);

		Iterator<String> it = lnfs.iterator();
		while (it.hasNext()) {
			lnfBox.addItem(it.next());
		}

		lnfBox.setSelectedItem(curlnf);
		lnfBox.setEditable(false);

		String shr = Prefs.getPref(PrefName.WKSTARTHOUR);
		String ehr = Prefs.getPref(PrefName.WKENDHOUR);
		wkstarthr.setSelectedItem(shr);
		wkendhr.setSelectedItem(ehr);

		// add locales
		localebox.removeAllItems();

		Locale locs[] = Locale.getAvailableLocales();
		for (int i = 0; i < locs.length; i++) {
			// String name = locs[i].
			localebox.addItem(locs[i].getDisplayName());
		}

		String currentlocale = Locale.getDefault().getDisplayName();
		localebox.setSelectedItem(currentlocale);

		int mins = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
		checkfreq.setValue(new Integer(mins));

		setCheckBox(cb_ucs_ontodo, PrefName.UCS_ONTODO);
		setCheckBox(cb_ucs_marktodo, PrefName.UCS_MARKTODO);

		tf_ucs_marker.setText(Prefs.getPref(PrefName.UCS_MARKER));
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_RED));
		btn_ucs_red.setColorProperty(new Color(mins));
		btn_ucs_red.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_BLUE));
		btn_ucs_blue.setColorProperty(new Color(mins));
		btn_ucs_blue.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_GREEN));
		btn_ucs_green.setColorProperty(new Color(mins));
		btn_ucs_green.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_BLACK));
		btn_ucs_black.setColorProperty(new Color(mins));
		btn_ucs_black.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_WHITE));
		btn_ucs_white.setColorProperty(new Color(mins));
		btn_ucs_white.setColorByProperty();

		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_NAVY));
		btn_ucs_tasks.setColorProperty(new Color(mins));
		btn_ucs_tasks.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_PURPLE));
		btn_ucs_holidays.setColorProperty(new Color(mins));
		btn_ucs_holidays.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_BRICK));
		btn_ucs_birthdays.setColorProperty(new Color(mins));
		btn_ucs_birthdays.setColorByProperty();

		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_DEFAULT));
		btn_ucs_default.setColorProperty(new Color(mins));
		btn_ucs_default.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_TODAY));
		btn_ucs_today.setColorProperty(new Color(mins));
		btn_ucs_today.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_HOLIDAY));
		btn_ucs_holiday.setColorProperty(new Color(mins));
		btn_ucs_holiday.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_HALFDAY));
		btn_ucs_halfday.setColorProperty(new Color(mins));
		btn_ucs_halfday.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_VACATION));
		btn_ucs_vacation.setColorProperty(new Color(mins));
		btn_ucs_vacation.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_WEEKEND));
		btn_ucs_weekend.setColorProperty(new Color(mins));
		btn_ucs_weekend.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_WEEKDAY));
		btn_ucs_weekday.setColorProperty(new Color(mins));
		btn_ucs_weekday.setColorByProperty();
		mins = Integer.parseInt(Prefs.getPref(PrefName.UCS_STRIPE));
		btn_ucs_stripe.setColorProperty(new Color(mins));
		btn_ucs_stripe.setColorByProperty();

		manageMySize(PrefName.OPTVIEWSIZE);
	}

	public void destroy() {
		this.dispose();
	}

	public void refresh() {
	}

	private void apply(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_apply
		applyChanges();
	}// GEN-LAST:event_apply

	private void applyChanges() {

		setBooleanPref(colorprint, PrefName.COLORPRINT);
		setBooleanPref(pubbox, PrefName.SHOWPUBLIC);
		setBooleanPref(privbox, PrefName.SHOWPRIVATE);
		setBooleanPref(emailbox, PrefName.EMAILENABLED);
		setBooleanPref(holiday1, PrefName.SHOWUSHOLIDAYS);
		setBooleanPref(canadabox, PrefName.SHOWCANHOLIDAYS);
		setBooleanPref(doyBox, PrefName.DAYOFYEAR);
		setBooleanPref(colorsortbox, PrefName.COLORSORT);
		setBooleanPref(miltime, PrefName.MILTIME);
		setBooleanPref(splashbox, PrefName.SPLASH);
		setBooleanPref(stackbox, PrefName.STACKTRACE);
		setBooleanPref(popenablebox, PrefName.REMINDERS);
		setBooleanPref(soundbox, PrefName.BEEPINGREMINDERS);
		setBooleanPref(useBeep, PrefName.USESYSTEMBEEP);
		setBooleanPref(truncbox, PrefName.TRUNCAPPT);
		setBooleanPref(iso8601Box, PrefName.ISOWKNUMBER);
		// setBooleanPref(extraDayBox, PrefName.SHOWEXTRADAYS);
		setBooleanPref(useSysTray, PrefName.USESYSTRAY);
		setBooleanPref(taskAbbrevBox, PrefName.TASK_SHOW_ABBREV);
		setBooleanPref(calShowTaskBox, PrefName.CAL_SHOW_TASKS);
		setBooleanPref(calShowSubtaskBox, PrefName.CAL_SHOW_SUBTASKS);
		setBooleanPref(ganttShowSubtaskBox, PrefName.GANTT_SHOW_SUBTASKS);
		setBooleanPref(dock, PrefName.DOCKPANELS);
		setBooleanPref(hide_strike_box, PrefName.HIDESTRIKETHROUGH);

		Prefs.putPref(PrefName.BACKUPDIR, backupDir.getText());

		try {
			int socket = Integer.parseInt(socketPort.getText());
			Prefs.putPref(PrefName.SOCKETPORT, new Integer(socket));
		} catch (NumberFormatException e) {
			Errmsg.notice(Resource.getPlainResourceString("socket_warn"));
			socketPort.setText("-1");
			Prefs.putPref(PrefName.SOCKETPORT, new Integer(-1));
			return;
		}

		Integer i = (Integer) checkfreq.getValue();
		int cur = Prefs.getIntPref(PrefName.REMINDERCHECKMINS);
		if (i.intValue() != cur) {
			Prefs.putPref(PrefName.REMINDERCHECKMINS, i);
		}

		if (mondaycb.isSelected()) {
			Prefs.putPref(PrefName.FIRSTDOW, new Integer(Calendar.MONDAY));
		} else {
			Prefs.putPref(PrefName.FIRSTDOW, new Integer(Calendar.SUNDAY));
		}

		Prefs.putPref(PrefName.WKENDHOUR, wkendhr.getSelectedItem());
		Prefs.putPref(PrefName.WKSTARTHOUR, wkstarthr.getSelectedItem());

		setBooleanPref(cb_ucs_ontodo, PrefName.UCS_ONTODO);
		setBooleanPref(cb_ucs_marktodo, PrefName.UCS_MARKTODO);

		Prefs.putPref(PrefName.UCS_MARKER, tf_ucs_marker.getText());

		Integer ucsi = new Integer((btn_ucs_red.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_RED, ucsi.toString());
		ucsi = new Integer((btn_ucs_blue.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_BLUE, ucsi.toString());
		ucsi = new Integer((btn_ucs_green.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_GREEN, ucsi.toString());
		ucsi = new Integer((btn_ucs_black.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_BLACK, ucsi.toString());
		ucsi = new Integer((btn_ucs_white.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_WHITE, ucsi.toString());

		ucsi = new Integer((btn_ucs_tasks.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_NAVY, ucsi.toString());
		ucsi = new Integer((btn_ucs_holidays.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_PURPLE, ucsi.toString());
		ucsi = new Integer((btn_ucs_birthdays.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_BRICK, ucsi.toString());

		ucsi = new Integer((btn_ucs_default.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_DEFAULT, ucsi.toString());
		ucsi = new Integer((btn_ucs_holiday.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_HOLIDAY, ucsi.toString());
		ucsi = new Integer((btn_ucs_halfday.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_HALFDAY, ucsi.toString());
		ucsi = new Integer((btn_ucs_vacation.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_VACATION, ucsi.toString());
		ucsi = new Integer((btn_ucs_today.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_TODAY, ucsi.toString());
		ucsi = new Integer((btn_ucs_weekend.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_WEEKEND, ucsi.toString());
		ucsi = new Integer((btn_ucs_weekday.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_WEEKDAY, ucsi.toString());
		ucsi = new Integer((btn_ucs_stripe.getColorProperty()).getRGB());
		Prefs.putPref(PrefName.UCS_STRIPE, ucsi.toString());
		StripedTable.setStripeColor(new Color(ucsi.intValue()));

		if (emailbox.isSelected()) {
			Prefs.putPref(PrefName.EMAILSERVER, smtptext.getText());
			Prefs.putPref(PrefName.EMAILPORT, smtpport.getText());
			Prefs.putPref(PrefName.EMAILADDR, emailtext.getText());
			Prefs.putPref(PrefName.EMAILUSER, usertext.getText());
			Prefs.putPref(PrefName.EMAILPASS, new String(smpw.getPassword()));

		}

		Locale locs[] = Locale.getAvailableLocales();
		String choice = (String) localebox.getSelectedItem();
		for (int ii = 0; ii < locs.length; ii++) {
			if (choice.equals(locs[ii].getDisplayName())) {
				Prefs.putPref(PrefName.COUNTRY, locs[ii].getCountry());
				Prefs.putPref(PrefName.LANGUAGE, locs[ii].getLanguage());
			}
		}

		String newlnf = (String) lnfBox.getSelectedItem();
		String oldlnf = Prefs.getPref(PrefName.LNF);
		if (!newlnf.equals(oldlnf)) {
			Errmsg.notice(Resource.getPlainResourceString("lfrestart"));
			Prefs.putPref(PrefName.LNF, newlnf);

		}

		Date d = (Date) emailtimebox.getValue();
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		Prefs.putPref(PrefName.EMAILTIME, new Integer(hour * 60 + min));
		remTimePanel.setTimes();

		Prefs.notifyListeners();

	}

	private void chgdbActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_chgdbActionPerformed
	{// GEN-HEADEREND:event_chgdbActionPerformed
		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("Really_change_the_database?"), Resource
				.getResourceString("Confirm_DB_Change"),
				JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {

			String hh = hsqldbdir.getText();
			Prefs.putPref(PrefName.HSQLDBDIR, hh);

			if (MySQLButton.isSelected()) {
				Prefs.putPref(PrefName.DBTYPE, "mysql");
			} else if (hsqldbButton.isSelected()) {
				Prefs.putPref(PrefName.DBTYPE, "hsqldb");
			} else {
				Prefs.putPref(PrefName.DBTYPE, "jdbc");
			}
			Prefs.putPref(PrefName.DBNAME, dbNameText.getText());
			Prefs.putPref(PrefName.DBPORT, dbPortText.getText());
			Prefs.putPref(PrefName.DBHOST, dbHostText.getText());
			Prefs.putPref(PrefName.DBUSER, dbUserText.getText());
			Prefs.putPref(PrefName.DBPASS, new String(jPasswordField1
					.getPassword()));
			Prefs.putPref(PrefName.JDBCURL, jdbcText.getText());

			if (rl_ != null) {
				rl_.restart();
			}

			this.dispose();
		}
	}// GEN-LAST:event_chgdbActionPerformed

	private void dbTypeAction(java.awt.event.ActionEvent evt)// GEN-FIRST:event_dbTypeAction
	{// GEN-HEADEREND:event_dbTypeAction
		dbTypeChange(evt.getActionCommand());
	}// GEN-LAST:event_dbTypeAction

	private void dbTypeChange(String type) {
		if (type.equals("mysql")) {
			mysqlPanel.setVisible(true);
			hsqldbPanel.setVisible(false);
			jdbcPanel.setVisible(false);
		} else if (type.equals("hsqldb")) {
			mysqlPanel.setVisible(false);
			hsqldbPanel.setVisible(true);
			jdbcPanel.setVisible(false);
		} else {
			mysqlPanel.setVisible(false);
			hsqldbPanel.setVisible(false);
			jdbcPanel.setVisible(true);
		}
	}

	private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_exitForm
		this.dispose();
	}// GEN-LAST:event_exitForm

	private void fontActionPerformed(java.awt.event.ActionEvent evt,
			PrefName fontname) {// GEN-FIRST:event_incfontActionPerformed

		Font pf = Font.decode(Prefs.getPref(fontname));
		Font f = NwFontChooserS.showDialog(null, null, pf);
		if (f == null) {
			return;
		}
		String s = NwFontChooserS.fontString(f);

		Prefs.putPref(fontname, s);
		if (fontname == PrefName.DEFFONT) {
			NwFontChooserS.setDefaultFont(f);
			SwingUtilities.updateComponentTreeUI(this);
		}

		Prefs.notifyListeners();

	}

	private JPanel getAppearancePanel() {
		JPanel appearancePanel = new JPanel();
		appearancePanel.setLayout(new java.awt.GridBagLayout());

		appearancePanel.setName(Resource.getResourceString("appearance"));
		ResourceHelper.setText(privbox, "Show_Private_Appointments");
		appearancePanel.add(privbox, GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH));

		ResourceHelper.setText(pubbox, "Show_Public_Appointments");
		appearancePanel.add(pubbox, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));

		ResourceHelper.setText(jLabel4, "Look_and_Feel:");
		jLabel4.setLabelFor(lnfBox);
		appearancePanel.add(jLabel4, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		lnfBox.setEditable(true);
		lnfBox.setMaximumSize(new java.awt.Dimension(131, 24));
		lnfBox.setPreferredSize(new java.awt.Dimension(50, 24));
		lnfBox.setAutoscrolls(true);
		appearancePanel.add(lnfBox, GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH));

		ResourceHelper.setText(holiday1, "Show_U.S._Holidays");
		appearancePanel.add(holiday1, GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));

		ResourceHelper.setText(mondaycb, "Week_Starts_with_Monday");
		appearancePanel.add(mondaycb, GridBagConstraintsFactory.create(1, 4, GridBagConstraints.BOTH));

		ResourceHelper.setText(miltime, "Use_24_hour_time_format");
		appearancePanel.add(miltime, GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));

		ResourceHelper.setText(jLabel5, "Week_View_Start_Hour:_");
		jLabel5.setLabelFor(wkstarthr);
		wkstarthr
				.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
						"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
						"11" }));
		appearancePanel.add(jLabel5, GridBagConstraintsFactory.create(0, 6, GridBagConstraints.BOTH));

		wkendhr.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"12", "13", "14", "15", "16", "17", "18", "19", "20", "21",
				"22", "23", "24" }));
		appearancePanel.add(wkstarthr, GridBagConstraintsFactory.create(1, 6, GridBagConstraints.BOTH));

		ResourceHelper.setText(jLabel6, "Week_View_End_Hour:_");
		jLabel6.setLabelFor(wkendhr);
		appearancePanel.add(wkendhr, GridBagConstraintsFactory.create(1, 7, GridBagConstraints.BOTH));
		appearancePanel.add(jLabel6, GridBagConstraintsFactory.create(0, 7, GridBagConstraints.BOTH));

		ResourceHelper.setText(canadabox, "Show_Canadian_Holidays");
		appearancePanel.add(canadabox, GridBagConstraintsFactory.create(1, 3, GridBagConstraints.BOTH));

		ResourceHelper.setText(jLabel8, "locale");
		jLabel8.setLabelFor(localebox);
		appearancePanel.add(jLabel8, GridBagConstraintsFactory.create(0, 11, GridBagConstraints.BOTH));

		appearancePanel.add(localebox, GridBagConstraintsFactory.create(1, 11, GridBagConstraints.BOTH));

		hide_strike_box.setText(Resource.getPlainResourceString("hide_strike"));
		appearancePanel.add(hide_strike_box, GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH));

		ResourceHelper.setText(iso8601Box, "ISO_week_number");
		appearancePanel.add(iso8601Box, GridBagConstraintsFactory.create(1, 8, GridBagConstraints.BOTH,1.0,0.0));

		ResourceHelper.setText(dock, "dock_option");
		appearancePanel.add(dock, GridBagConstraintsFactory.create(0, 8, GridBagConstraints.BOTH));

		ResourceHelper.setText(colorsortbox, "colorsort");
		appearancePanel.add(colorsortbox, GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH));

		appearancePanel.add(getDoyBox(), GridBagConstraintsFactory.create(1, 5, GridBagConstraints.BOTH));

		appearancePanel.add(getTruncbox(), GridBagConstraintsFactory.create(1, 2, GridBagConstraints.BOTH));

		return appearancePanel;
	}

	private JPanel getApplyDismissPanel() {
		if (applyDismissPanel == null) {
			applyDismissPanel = new JPanel();

			applyButton.setIcon(new javax.swing.ImageIcon(getClass()
					.getResource("/resource/Save16.gif")));
			ResourceHelper.setText(applyButton, "apply");
			applyButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					apply(evt);
				}
			});
			applyDismissPanel.add(applyButton, null);

			dismissButton.setIcon(new javax.swing.ImageIcon(getClass()
					.getResource("/resource/Stop16.gif")));
			ResourceHelper.setText(dismissButton, "Dismiss");
			dismissButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(
								java.awt.event.ActionEvent evt) {
							jButton2ActionPerformed(evt);
						}
					});
			setDismissButton(dismissButton);
			applyDismissPanel.add(dismissButton, null);
		}
		return applyDismissPanel;
	}

	private JPanel getDBPanel() {

		JPanel dbPanel = new JPanel();

		dbPanel = new JPanel();
		dbPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbcm =GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 1.0, 1.0);
		gbcm.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		dbPanel.add(getMysqlPanel(), gbcm);
		dbPanel.add(getDbTypePanel(), GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		GridBagConstraints gridBagConstraints5 = GridBagConstraintsFactory.create(0, 6);
		gridBagConstraints5.weightx = 1.0;
		gridBagConstraints5.anchor = GridBagConstraints.CENTER;
		dbPanel.add(chgdb, gridBagConstraints5); // Generated
		
		chgdb.setForeground(new java.awt.Color(255, 0, 51));
		chgdb.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Refresh16.gif")));
		ResourceHelper.setText(chgdb, "Apply_DB_Change");
		chgdb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				chgdbActionPerformed(evt);
			}
		});

		JButton help = new JButton();
		GridBagConstraints gridBagConstraintsh = GridBagConstraintsFactory.create(1, 6);
		gridBagConstraintsh.weightx = 1.0;
		gridBagConstraintsh.anchor = GridBagConstraints.CENTER;
		dbPanel.add(help, gridBagConstraintsh); // Generated
		help.setForeground(new java.awt.Color(255, 0, 51));
		help.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/resource/Help16.gif")));
		ResourceHelper.setText(help, "Help");

		help.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					HelpProxy.launchHelp();
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		});

		GridBagConstraints gridBagConstraints6h =GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH, 1.0, 1.0);
		gridBagConstraints6h.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		dbPanel.add(getHSQLDBPanel(), gridBagConstraints6h); 

		GridBagConstraints gridBagConstraints7h = GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH, 1.0, 1.0);
		gridBagConstraints7h.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		dbPanel.add(getJdbcPanel(), gridBagConstraints7h); // Generated

		return dbPanel;
	}

	private JPanel getDbTypePanel() {
		if (dbTypePanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.LEFT); // Generated
			flowLayout.setHgap(40); // Generated
			dbTypePanel = new JPanel();
			dbTypePanel.setLayout(flowLayout); // Generated
			dbTypePanel.add(getHSQLDBFileButton(), null);
			dbTypePanel.add(getMySQLButton(), null); // Generated
			dbTypePanel.add(getJdbcButton(), null); // Generated

		}
		return dbTypePanel;
	}

	private JCheckBox getDoyBox() {
		if (doyBox == null) {
			doyBox = new JCheckBox();
			ResourceHelper.setText(doyBox, "showdoy");
		}
		return doyBox;
	}

	private JPanel getEmailPanel() {
		JPanel emailPanel = new JPanel();
		emailPanel.setLayout(new java.awt.GridBagLayout());

		ResourceHelper.setText(jLabel1, "SMTP_Server");
		emailPanel.add(jLabel1, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));
		jLabel1.setLabelFor(smtptext);
		
		smtptext.setColumns(30);
		emailPanel.add(smtptext, GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH, 1.0, 0.0));
		
		JLabel portLabel = new JLabel();
		ResourceHelper.setText(portLabel, "SMTP_Port");
		emailPanel.add(portLabel, GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH));
		jLabel1.setLabelFor(smtpport);
		
		smtpport.setColumns(30);
		emailPanel.add(smtpport, GridBagConstraintsFactory.create(1, 2, GridBagConstraints.BOTH, 1.0, 0.0));

		JLabel userlabel = new JLabel();
		ResourceHelper.setText(userlabel, "SMTP_user");
		emailPanel.add(userlabel, GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));
		userlabel.setLabelFor(usertext);
		
		emailPanel.add(usertext, GridBagConstraintsFactory.create(1, 3, GridBagConstraints.BOTH));

		JLabel passlabel = new JLabel();
		ResourceHelper.setText(passlabel, "SMTP_password");
		emailPanel.add(passlabel, GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));
		passlabel.setLabelFor(smpw);

		emailPanel.add(smpw, GridBagConstraintsFactory.create(1, 4, GridBagConstraints.BOTH));

		ResourceHelper.setText(jLabel2, "Your_Email_Address");
		emailPanel.add(jLabel2, GridBagConstraintsFactory.create(0, 5, GridBagConstraints.BOTH));
		jLabel2.setLabelFor(emailtext);

		
		emailtext.setColumns(30);
		emailPanel.add(emailtext, GridBagConstraintsFactory.create(1, 5, GridBagConstraints.BOTH, 1.0, 0.0));

		ResourceHelper.setText(emailbox, "Enable_Email");
		emailPanel.add(emailbox, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		ResourceHelper.setText(remtimelabel, "reminder_time");
		remtimelabel.setLabelFor(emailtimebox);
		emailPanel.add(remtimelabel, GridBagConstraintsFactory.create(0, 6, GridBagConstraints.BOTH));

		emailPanel.add(getEmailtimebox(), GridBagConstraintsFactory.create(1, 6, GridBagConstraints.BOTH, 1.0, 0.0));

		return emailPanel;
	}

	private JSpinner getEmailtimebox() {
		if (emailtimebox == null) {
			emailtimebox = new JSpinner(new SpinnerDateModel());
			JSpinner.DateEditor de = new JSpinner.DateEditor(emailtimebox,
					"HH:mm");
			emailtimebox.setEditor(de);
			// emailtimebox.setValue(new Date());

		}
		return emailtimebox;
	}

	/*
	 * private JCheckBox getExtraDayBox() { if (extraDayBox == null) {
	 * extraDayBox = new JCheckBox(); ResourceHelper.setText(extraDayBox,
	 * "show_extra"); } return extraDayBox; }
	 */

	private JPanel getFontPanel() {
		JPanel fontPanel = new JPanel();
		fontPanel.setLayout(new FlowLayout());

		ResourceHelper.setText(apptFontButton, "set_appt_font");
		apptFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
				javax.swing.border.BevelBorder.RAISED));
		// apptFontButton.setFont(Font.decode(Prefs.getPref(PrefName.APPTFONT)));
		apptFontButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(evt, PrefName.APPTFONT);
				// apptFontButton.setFont(Font.decode(Prefs.getPref(PrefName.APPTFONT)));
			}
		});
		fontPanel.add(apptFontButton);

		ResourceHelper.setText(defFontButton, "set_def_font");
		defFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
				javax.swing.border.BevelBorder.RAISED));
		// if( !Prefs.getPref(PrefName.DEFFONT).equals(""))
		// defFontButton.setFont(Font.decode(Prefs.getPref(PrefName.DEFFONT)));
		defFontButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(evt, PrefName.DEFFONT);
				// defFontButton.setFont(Font.decode(Prefs.getPref(PrefName.DEFFONT)));
			}
		});
		fontPanel.add(defFontButton);

		ResourceHelper.setText(dayFontButton, "dview_font");
		dayFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
				javax.swing.border.BevelBorder.RAISED));
		// dayFontButton.setFont(Font.decode(Prefs.getPref(PrefName.DAYVIEWFONT)));
		dayFontButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(evt, PrefName.DAYVIEWFONT);
				// dayFontButton.setFont(Font.decode(Prefs.getPref(PrefName.DAYVIEWFONT)));
			}
		});
		fontPanel.add(dayFontButton);

		ResourceHelper.setText(weekFontButton, "wview_font");
		weekFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
				javax.swing.border.BevelBorder.RAISED));
		// weekFontButton.setFont(Font.decode(Prefs.getPref(PrefName.WEEKVIEWFONT)));
		weekFontButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(evt, PrefName.WEEKVIEWFONT);
				// weekFontButton.setFont(Font.decode(Prefs.getPref(PrefName.WEEKVIEWFONT)));
			}
		});
		fontPanel.add(weekFontButton);

		ResourceHelper.setText(monthFontButton, "mview_font");
		monthFontButton.setBorder(new javax.swing.border.SoftBevelBorder(
				javax.swing.border.BevelBorder.RAISED));
		// monthFontButton.setFont(Font.decode(Prefs.getPref(PrefName.MONTHVIEWFONT)));
		monthFontButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fontActionPerformed(evt, PrefName.MONTHVIEWFONT);
				// monthFontButton.setFont(Font.decode(Prefs.getPref(PrefName.MONTHVIEWFONT)));
			}
		});
		fontPanel.add(monthFontButton);

		return fontPanel;
	}

	private JRadioButton getHSQLDBFileButton() {
		if (hsqldbButton == null) {
			hsqldbButton = new JRadioButton();
			hsqldbButton.setActionCommand("hsqldb");
			ResourceHelper.setText(hsqldbButton, "hsqldb");
			hsqldbButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dbTypeAction(e);
				}
			});
		}
		return hsqldbButton;
	}

	private JPanel getHSQLDBPanel() {
		hsqldbPanel = new JPanel();
		hsqldbPanel.setLayout(new java.awt.GridBagLayout());

		JLabel hs1 = new JLabel();
		hsqldbPanel.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("hsqldbinfo")));
		ResourceHelper.setText(hs1, "DataBase_Directory");
		hs1.setLabelFor(dbDirText);
		hsqldbPanel.add(hs1, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		hsqldbPanel.add(hsqldbdir, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 0.5, 0.0));

		JButton hsb1 = new JButton();
		ResourceHelper.setText(hsb1, "Browse");
		hsb1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				hsqldbActionPerformed(evt);
			}
		});

		hsqldbPanel.add(hsb1, GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH));

		return hsqldbPanel;
	}

	private JRadioButton getJdbcButton() {
		if (jdbcButton == null) {
			jdbcButton = new JRadioButton();
			jdbcButton.setActionCommand("jdbc");
			ResourceHelper.setText(jdbcButton, "jdbc");
			jdbcButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dbTypeAction(e);
				}
			});
		}
		return jdbcButton;
	}

	private JPanel getJdbcPanel() {
		if (jdbcPanel == null) {

			JLabel enturlLabel = new JLabel();
			ResourceHelper.setText(enturlLabel, "enturl");
			enturlLabel.setLabelFor(getJdbcText());
			enturlLabel
					.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT); // Generated
			enturlLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT); // Generated
			jdbcPanel = new JPanel();
			jdbcPanel.setLayout(new GridBagLayout()); // Generated
			jdbcPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
					null, Resource.getResourceString("jdbc"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
					null)); // Generated
			jdbcPanel.add(enturlLabel, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH)); // Generated
			jdbcPanel.add(getJdbcText(), GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH, 1.0, 0.0)); // Generated
		}
		return jdbcPanel;
	}

	private JTextField getJdbcText() {
		if (jdbcText == null) {
			jdbcText = new JTextField();
		}
		return jdbcText;
	}

	private JPanel getJPanelUCS() {
		if (jPanelUCS == null) {
			jPanelUCS = new JPanel();
			jPanelUCS.setLayout(new GridLayout(10, 2));

			cb_ucs_ontodo = new javax.swing.JCheckBox();
			ResourceHelper.setText(cb_ucs_ontodo, "ucolortext1");
			cb_ucs_marktodo = new javax.swing.JCheckBox();
			ResourceHelper.setText(cb_ucs_marktodo, "ucolortext2");
			tf_ucs_marker = new JTextField("! "); //$NON-NLS-1$
			btn_ucs_red = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext4"), Color.WHITE, false); //$NON-NLS-1$
			btn_ucs_blue = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext5"), Color.WHITE, false); //$NON-NLS-1$
			btn_ucs_green = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext6"), Color.WHITE, false); //$NON-NLS-1$
			btn_ucs_black = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext7"), Color.WHITE, false); //$NON-NLS-1$
			btn_ucs_white = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext8"), Color.WHITE, false); //$NON-NLS-1$
			btn_ucs_tasks = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext9"), Color.WHITE, false); //$NON-NLS-1$
			btn_ucs_holidays = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext10"), Color.WHITE, //$NON-NLS-1$
					false);
			btn_ucs_birthdays = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext11"), Color.WHITE, //$NON-NLS-1$
					false);
			btn_ucs_default = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext12"), Color.WHITE, true); //$NON-NLS-1$
			btn_ucs_holiday = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext13"), Color.WHITE, true); //$NON-NLS-1$
			btn_ucs_halfday = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext14"), Color.WHITE, true); //$NON-NLS-1$
			btn_ucs_vacation = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext15"), Color.WHITE, //$NON-NLS-1$
					true);
			btn_ucs_today = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext16"), Color.WHITE, true); //$NON-NLS-1$
			btn_ucs_weekend = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext17"), Color.WHITE, true); //$NON-NLS-1$
			btn_ucs_weekday = new JButtonKnowsBgColor(Resource
					.getResourceString("ucolortext18"), Color.WHITE, true); //$NON-NLS-1$
			btn_ucs_stripe = new JButtonKnowsBgColor(Resource
					.getResourceString("stripecolor"), Color.WHITE, true);

			btn_ucs_restore = new JButton(Resource
					.getResourceString("restore_defaults")); //$NON-NLS-1$

			btn_ucs_restore.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btn_ucs_red.setColorProperty(new Color(13369395));
					btn_ucs_red.setColorByProperty();
					btn_ucs_blue.setColorProperty(new Color(6684876));
					btn_ucs_blue.setColorByProperty();
					btn_ucs_green.setColorProperty(new Color(39168));
					btn_ucs_green.setColorByProperty();
					btn_ucs_black.setColorProperty(new Color(13107));
					btn_ucs_black.setColorByProperty();
					btn_ucs_white.setColorProperty(new Color(16250609));
					btn_ucs_white.setColorByProperty();
					btn_ucs_tasks.setColorProperty(new Color(13158));
					btn_ucs_tasks.setColorByProperty();
					btn_ucs_holidays.setColorProperty(new Color(10027212));
					btn_ucs_holidays.setColorByProperty();
					btn_ucs_birthdays.setColorProperty(new Color(10027008));
					btn_ucs_birthdays.setColorByProperty();
					// // Calendar view day background
					// colors

					btn_ucs_default.setColorProperty(new Color(16777164));
					btn_ucs_default.setColorByProperty();
					btn_ucs_today.setColorProperty(new Color(16751001));
					btn_ucs_today.setColorByProperty();
					btn_ucs_holiday.setColorProperty(new Color(16764108));
					btn_ucs_holiday.setColorByProperty();
					btn_ucs_vacation.setColorProperty(new Color(13434828));
					btn_ucs_vacation.setColorByProperty();
					btn_ucs_halfday.setColorProperty(new Color(13421823));
					btn_ucs_halfday.setColorByProperty();
					btn_ucs_weekend.setColorProperty(new Color(16764057));
					btn_ucs_weekend.setColorByProperty();
					btn_ucs_weekday.setColorProperty(new Color(16777164));
					btn_ucs_weekday.setColorByProperty();
					btn_ucs_stripe.setColorProperty(new Color(15792890));
					btn_ucs_stripe.setColorByProperty();
				}
			});

			jPanelUCS.add(btn_ucs_red);
			jPanelUCS.add(btn_ucs_default);
			jPanelUCS.add(btn_ucs_blue);
			jPanelUCS.add(btn_ucs_today);
			jPanelUCS.add(btn_ucs_green);
			jPanelUCS.add(btn_ucs_holiday);
			jPanelUCS.add(btn_ucs_black);
			jPanelUCS.add(btn_ucs_halfday);
			jPanelUCS.add(btn_ucs_white);
			jPanelUCS.add(btn_ucs_vacation);
			jPanelUCS.add(btn_ucs_tasks);
			jPanelUCS.add(btn_ucs_weekend);
			jPanelUCS.add(btn_ucs_holidays);
			jPanelUCS.add(btn_ucs_weekday);
			jPanelUCS.add(btn_ucs_birthdays);
			jPanelUCS.add(btn_ucs_stripe);
			jPanelUCS.add(btn_ucs_restore);
			jPanelUCS.add(cb_ucs_ontodo);

			JPanel njp = new JPanel();
			njp.setLayout(new BorderLayout());
			njp.add(cb_ucs_marktodo, BorderLayout.WEST);
			njp.add(tf_ucs_marker, BorderLayout.CENTER);
			getJPanelUCS().add(njp);
		}
		return jPanelUCS;
	}

	private JPanel getMiscPanel() {
		JPanel miscPanel = new JPanel();

		miscPanel.setLayout(new java.awt.GridBagLayout());

		ResourceHelper.setText(splashbox, "splash");
		miscPanel.add(splashbox, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		ResourceHelper.setText(stackbox, "stackonerr");
		miscPanel.add(stackbox, GridBagConstraintsFactory.create(0, 40, GridBagConstraints.BOTH));

		JLabel sportlabel = new JLabel();
		ResourceHelper.setText(sportlabel, "socket_port");
		miscPanel.add(sportlabel, GridBagConstraintsFactory.create(0, 9, GridBagConstraints.BOTH));

		miscPanel.add(socketPort, GridBagConstraintsFactory.create(1, 9, GridBagConstraints.BOTH));

		useSysTray.setText(Resource.getPlainResourceString("enable_systray"));
		miscPanel.add(useSysTray, GridBagConstraintsFactory.create(0, 10, GridBagConstraints.BOTH));

		JPanel backp = new JPanel();
		backp.setLayout(new GridBagLayout());

		backp.add(new JLabel(Resource.getPlainResourceString("backup_dir")
				+ ": "), GridBagConstraintsFactory.create(0, 0, GridBagConstraints.NONE));

		backp.add(backupDir, GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH, 1.0, 0.0));

		JButton bb = new JButton();
		ResourceHelper.setText(bb, "Browse");
		bb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				backupDirActionPerformed(evt);
			}
		});
		backp.add(bb, GridBagConstraintsFactory.create(2, 0, GridBagConstraints.NONE));

		GridBagConstraints gbc1 = GridBagConstraintsFactory.create(0, 11, GridBagConstraints.BOTH, 1.0, 0.0);
		gbc1.gridwidth = 2;
		miscPanel.add(backp, gbc1);

		ResourceHelper.setText(colorprint, "Print_In_Color?");
		miscPanel.add(colorprint, GridBagConstraintsFactory.create(0, 12, GridBagConstraints.BOTH));

		return miscPanel;
	}

	private JPanel taskOptionPanel = null; // @jve:decl-index=0:visual-constraint="12,2528"

	private JCheckBox taskAbbrevBox = new JCheckBox();

	private JCheckBox calShowTaskBox = new JCheckBox();

	private JCheckBox calShowSubtaskBox = new JCheckBox();

	private JCheckBox ganttShowSubtaskBox = new JCheckBox();

	private JPanel getTaskOptionPanel() {
		if (taskOptionPanel == null) {
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints20.gridy = 2;
			gridBagConstraints20.ipady = 0;
			gridBagConstraints20.fill = GridBagConstraints.NONE;
			gridBagConstraints20.anchor = GridBagConstraints.WEST;
			gridBagConstraints20.gridx = 0;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints21.gridy = 3;
			gridBagConstraints21.ipady = 0;
			gridBagConstraints21.fill = GridBagConstraints.NONE;
			gridBagConstraints21.anchor = GridBagConstraints.WEST;
			gridBagConstraints21.gridx = 0;
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints19.gridy = 1;
			gridBagConstraints19.ipady = 0;
			gridBagConstraints19.fill = GridBagConstraints.NONE;
			gridBagConstraints19.anchor = GridBagConstraints.WEST;
			gridBagConstraints19.gridx = 0;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 0;
			gridBagConstraints17.ipadx = 0;
			gridBagConstraints17.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints17.fill = GridBagConstraints.NONE;
			gridBagConstraints17.anchor = GridBagConstraints.WEST;
			gridBagConstraints17.gridy = 0;
			taskOptionPanel = new JPanel();

			taskOptionPanel.setLayout(new GridBagLayout());
			taskOptionPanel.setSize(new Dimension(168, 159));
			taskOptionPanel.add(taskAbbrevBox, gridBagConstraints17);
			taskOptionPanel.add(calShowTaskBox, gridBagConstraints19);
			taskOptionPanel.add(calShowSubtaskBox, gridBagConstraints20);
			taskOptionPanel.add(ganttShowSubtaskBox, gridBagConstraints21);
			taskAbbrevBox.setText(Resource
					.getPlainResourceString("task_abbrev"));
			calShowTaskBox.setText(Resource
					.getPlainResourceString("calShowTask"));
			calShowSubtaskBox.setText(Resource
					.getPlainResourceString("calShowSubtask"));
			ganttShowSubtaskBox.setText(Resource
					.getPlainResourceString("ganttShowSubtask"));
		}
		return taskOptionPanel;
	}

	/**
	 * This method initializes jRadioButton1
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getMySQLButton() {
		if (MySQLButton == null) {
			MySQLButton = new JRadioButton();
			MySQLButton.setActionCommand("mysql");
			MySQLButton.setText("MySQL");
			MySQLButton.setMnemonic(KeyEvent.VK_M);
			MySQLButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dbTypeAction(e);
				}
			});
		}
		return MySQLButton;
	}

	private JPanel getMysqlPanel() {
		mysqlPanel = new javax.swing.JPanel();
		mysqlPanel.setLayout(new java.awt.GridBagLayout());

		mysqlPanel.setBorder(new javax.swing.border.TitledBorder(Resource
				.getResourceString("MySQLInfo")));
		ResourceHelper.setText(jLabel7, "DatabaseName");
		jLabel7.setLabelFor(dbNameText);
		mysqlPanel.add(jLabel7, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		mysqlPanel.add(dbNameText, GridBagConstraintsFactory.create(1, 0, GridBagConstraints.BOTH));

		ResourceHelper.setText(jLabel17, "hostname");
		jLabel17.setLabelFor(dbHostText);
		mysqlPanel.add(jLabel17, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));

		mysqlPanel.add(dbHostText, GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH, 1.0, 0.0));

		ResourceHelper.setText(jLabel18, "port");
		jLabel18.setLabelFor(dbPortText);
		mysqlPanel.add(jLabel18, GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH));

		mysqlPanel.add(dbPortText, GridBagConstraintsFactory.create(1, 2, GridBagConstraints.BOTH, 1.0, 0.0));

		ResourceHelper.setText(jLabel19, "User");
		jLabel19.setLabelFor(dbUserText);
		mysqlPanel.add(jLabel19, GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));

		mysqlPanel.add(dbUserText, GridBagConstraintsFactory.create(1, 3, GridBagConstraints.BOTH, 1.0, 0.0));

		ResourceHelper.setText(jLabel20, "Password");
		jLabel20.setLabelFor(jPasswordField1);
		mysqlPanel.add(jLabel20, GridBagConstraintsFactory.create(0, 4, GridBagConstraints.BOTH));

		mysqlPanel.add(jPasswordField1, GridBagConstraintsFactory.create(1, 4, GridBagConstraints.BOTH));

		return mysqlPanel;
	}

	private JPanel getReminderPanel() {

		JPanel reminderPanel = new JPanel();
		reminderPanel.setLayout(new java.awt.GridBagLayout());

		ResourceHelper.setText(popenablebox, "enable_popups");
		reminderPanel.add(popenablebox, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH));

		ResourceHelper.setText(soundbox, "beeps");
		reminderPanel.add(soundbox, GridBagConstraintsFactory.create(0, 3, GridBagConstraints.BOTH));

		jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
		ResourceHelper.setText(jLabel15, "min_between_chks");
		jLabel15.setLabelFor(getEmailtimebox());
		reminderPanel.add(jLabel15, GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));

		checkfreq.setMinimumSize(new java.awt.Dimension(50, 20));
		reminderPanel.add(checkfreq, GridBagConstraintsFactory.create(1, 1, GridBagConstraints.BOTH,1.0,0.0));

		GridBagConstraints gridBagConstraints65 = GridBagConstraintsFactory.create(0, 2, GridBagConstraints.BOTH);
		gridBagConstraints65.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		reminderPanel.add(jSeparator1, gridBagConstraints65);

		ResourceHelper.setText(jLabel16, "restart_req");
		reminderPanel.add(jLabel16, GridBagConstraintsFactory.create(2, 1, GridBagConstraints.BOTH));

		reminderPanel.add(getUseBeep(), GridBagConstraintsFactory.create(0, 4));

		GridBagConstraints gridBagConstraints113 = GridBagConstraintsFactory.create(0, 5);
		gridBagConstraints113.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints113.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints113.insets = new java.awt.Insets(18, 18, 18, 18);
		reminderPanel.add(remTimePanel, gridBagConstraints113);

		return reminderPanel;
	}

	private JPanel getTopPanel() {
		if (topPanel == null) {
		
			topPanel = new JPanel();
			topPanel.setLayout(new GridBagLayout());
			
			topPanel.add(jTabbedPane1, GridBagConstraintsFactory.create(0, 0, GridBagConstraints.BOTH,1.0,1.0));
			topPanel.add(getApplyDismissPanel(), GridBagConstraintsFactory.create(0, 1, GridBagConstraints.BOTH));
		}
		return topPanel;
	}

	/**
	 * This method initializes truncbox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getTruncbox() {
		if (truncbox == null) {
			truncbox = new JCheckBox();
			ResourceHelper.setText(truncbox, "truncate_appts");
		}
		return truncbox;
	}

	/**
	 * This method initializes useBeep
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getUseBeep() {
		if (useBeep == null) {
			useBeep = new JCheckBox();
			ResourceHelper.setText(useBeep, "Use_system_beep");
		}
		return useBeep;
	}

	private void hsqldbActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton5ActionPerformed

		// browse for new database dir
		String dbdir = OptionsView.chooseDir(false);
		if (dbdir == null) {
			return;
		}

		// update text field - nothing else changes. DB change will take
		// effect
		// only on restart
		hsqldbdir.setText(dbdir);

	}

	private void backupDirActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton5ActionPerformed

		String dbdir = OptionsView.chooseDir(false);
		if (dbdir == null) {
			return;
		}

		backupDir.setText(dbdir);

	}

	private void initComponents() {

		calShowSubtaskBox.setName("calShowSubtaskBox");
		calShowSubtaskBox.setHorizontalAlignment(SwingConstants.LEFT);
		ganttShowSubtaskBox.setName("calShowSubtaskBox");
		ganttShowSubtaskBox.setHorizontalAlignment(SwingConstants.LEFT);
		calShowTaskBox.setName("calShowTaskBox");
		calShowTaskBox.setHorizontalAlignment(SwingConstants.LEFT);
		taskAbbrevBox.setName("taskAbbrevBox");
		taskAbbrevBox.setHorizontalAlignment(SwingConstants.LEFT);
		remtimelabel = new JLabel();
		jTabbedPane1 = new javax.swing.JTabbedPane();
		privbox = new javax.swing.JCheckBox();
		pubbox = new javax.swing.JCheckBox();
		apptFontButton = new javax.swing.JButton();
		jLabel4 = new javax.swing.JLabel();
		lnfBox = new javax.swing.JComboBox();
		holiday1 = new javax.swing.JCheckBox();
		mondaycb = new javax.swing.JCheckBox();
		miltime = new javax.swing.JCheckBox();
		jLabel5 = new javax.swing.JLabel();
		wkstarthr = new javax.swing.JComboBox();
		wkendhr = new javax.swing.JComboBox();
		jLabel6 = new javax.swing.JLabel();
		canadabox = new javax.swing.JCheckBox();
		jLabel8 = new javax.swing.JLabel();
		localebox = new javax.swing.JComboBox();
		defFontButton = new javax.swing.JButton();
		colorsortbox = new javax.swing.JCheckBox();
		jLabel7 = new javax.swing.JLabel();
		dbNameText = new javax.swing.JTextField();
		jLabel17 = new javax.swing.JLabel();
		dbHostText = new javax.swing.JTextField();
		jLabel18 = new javax.swing.JLabel();
		dbPortText = new javax.swing.JTextField();
		jLabel19 = new javax.swing.JLabel();
		dbUserText = new javax.swing.JTextField();
		jLabel20 = new javax.swing.JLabel();
		jPasswordField1 = new javax.swing.JPasswordField();

		dbDirText = new javax.swing.JTextField();

		chgdb = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		smtptext = new javax.swing.JTextField();
		emailtext = new javax.swing.JTextField();
		emailbox = new javax.swing.JCheckBox();
		colorprint = new javax.swing.JCheckBox();

		splashbox = new javax.swing.JCheckBox();
		stackbox = new javax.swing.JCheckBox();

		popenablebox = new javax.swing.JCheckBox();
		soundbox = new javax.swing.JCheckBox();
		jLabel15 = new javax.swing.JLabel();
		checkfreq = new javax.swing.JSpinner();
		jSeparator1 = new javax.swing.JSeparator();
		jLabel16 = new javax.swing.JLabel();

		dismissButton = new javax.swing.JButton();
		applyButton = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		ResourceHelper.setTitle(this, "Options");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		ResourceHelper.addTab(jTabbedPane1, "appearance", getAppearancePanel());
		ResourceHelper.addTab(jTabbedPane1, "fonts", getFontPanel());
		ResourceHelper
				.addTab(jTabbedPane1, "DatabaseInformation", getDBPanel());
		ResourceHelper.addTab(jTabbedPane1, "EmailParameters", getEmailPanel());
		ResourceHelper.addTab(jTabbedPane1, "popup_reminders",
				getReminderPanel());

		ResourceHelper.addTab(jTabbedPane1, "misc", getMiscPanel());
		ResourceHelper.addTab(jTabbedPane1, "UserColorScheme", getJPanelUCS());
		ResourceHelper
				.addTab(jTabbedPane1, "taskOptions", getTaskOptionPanel());

		this.setContentPane(getTopPanel());
		this.setSize(629, 493);

		pack();
	}// GEN-END:initComponents

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_jButton2ActionPerformed
	{// GEN-HEADEREND:event_jButton2ActionPerformed
		this.dispose();
	}// GEN-LAST:event_jButton2ActionPerformed

}

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
package net.sf.borg.model;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;

import net.sf.borg.common.DateUtil;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.Resource;
import net.sf.borg.common.Warning;
import net.sf.borg.common.XTree;
import net.sf.borg.model.CategoryModel.CategorySource;
import net.sf.borg.model.db.EntityDB;
import net.sf.borg.model.db.TaskDB;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.model.db.jdbc.TaskJdbcDB;
import net.sf.borg.model.entity.BorgOption;
import net.sf.borg.model.entity.Project;
import net.sf.borg.model.entity.Subtask;
import net.sf.borg.model.entity.Task;
import net.sf.borg.model.entity.Tasklog;
import net.sf.borg.model.undo.ProjectUndoItem;
import net.sf.borg.model.undo.SubtaskUndoItem;
import net.sf.borg.model.undo.TaskUndoItem;
import net.sf.borg.model.undo.UndoLog;
import net.sf.borg.model.xml.ProjectXMLAdapter;
import net.sf.borg.model.xml.SubtaskXMLAdapter;
import net.sf.borg.model.xml.TaskXMLAdapter;
import net.sf.borg.model.xml.TasklogXMLAdapter;

/**
 * TaksModel manages all of the task related entities - Task, Project, Subtask,
 * and Tasklog
 */
public class TaskModel extends Model implements Model.Listener, Transactional,
		CategorySource {

	// hard-code to TaskJdbcDB just to access options logic
	// need to fix this in the future
	/** The db */
	private TaskJdbcDB db_;

	/**
	 * Gets the dB.
	 * 
	 * @return the dB
	 */
	public EntityDB<Task> getDB() {
		return (db_);
	}

	/** map of tasks keyed by due date */
	private HashMap<Integer, Collection<Task>> btmap_;

	/** cache of all open tasks with a due date */
	private Vector<Task> openTaskMap;

	/** map of subtasks keyed by due date */
	private HashMap<Integer, Collection<Subtask>> stmap_;

	/** map of projects keyed by due date */
	private HashMap<Integer, Collection<Project>> pmap_;

	/** The task types */
	private TaskTypes taskTypes_ = new TaskTypes();

	/**
	 * Get all tasks due on a particular date
	 * 
	 * @param d
	 *            the date
	 * 
	 * @return the tasks
	 */
	public Collection<Task> get_tasks(Date d) {
		return (btmap_.get(new Integer(DateUtil.dayOfEpoch(d))));
	}

	/**
	 * Get all subtasks due on a particular date
	 * 
	 * @param d
	 *            the date
	 * 
	 * @return the subtasks
	 */
	public Collection<Subtask> get_subtasks(Date d) {
		return (stmap_.get(new Integer(DateUtil.dayOfEpoch(d))));
	}

	/**
	 * Get all projects due on a particular date
	 * 
	 * @param d
	 *            the date
	 * 
	 * @return the projects
	 */
	public Collection<Project> get_projects(Date d) {
		return (pmap_.get(new Integer(DateUtil.dayOfEpoch(d))));
	}

	/**
	 * Get all open tasks with a due date
	 * 
	 * @return the tasks
	 */
	public Vector<Task> get_tasks() {
		return (openTaskMap);
	}

	/** The singleton */
	static private TaskModel self_ = null;

	/**
	 * Gets the singleton.
	 * 
	 * @return the singleton
	 */
	static public TaskModel getReference() {
		if (self_ == null)
			try {
				self_ = new TaskModel();
				self_.load_map();
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return null;
			}
		return (self_);
	}

	/**
	 * Gets the task types.
	 * 
	 * @return the task types
	 */
	public TaskTypes getTaskTypes() {
		return (taskTypes_);
	}

	/**
	 * save the task types to the db
	 * 
	 * @param tt
	 *            the task types
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void saveTaskTypes(TaskTypes tt) throws Exception {
		if (tt != null) {
			tt.validate();
			taskTypes_ = tt.copy();
		}
		db_.setOption(new BorgOption("SMODEL", taskTypes_.toString()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.CategoryModel.CategorySource#getCategories()
	 */
	public Collection<String> getCategories() {

		TreeSet<String> categories = new TreeSet<String>();
		try {
			for (Task t : db_.readAll()) {
				String cat = t.getCategory();
				if (cat != null && !cat.equals(""))
					categories.add(cat);
			}
		} catch (Exception e1) {
			Errmsg.errmsg(e1);
		}

		try {
			for (Project t : getProjects()) {
				String cat = t.getCategory();
				if (cat != null && !cat.equals(""))
					categories.add(cat);
			}
		} catch (Exception e) {
			// ignore this one
		}
		return (categories);

	}

	/**
	 * Get all tasks.
	 * 
	 * @return the tasks
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Task> getTasks() throws Exception {
		return db_.readAll();
	}

	/**
	 * load caches of open tasks for performance. this is because the views will
	 * repeatedly need to retrieve tasks per day and it would be slow to
	 * repeatedly traverse the entire task DB looking for them only tasks that
	 * are viewed by day need to be cached here - those that are not CLOSED and
	 * have a due date - a small fraction of the total
	 */
	private void load_map() {

		// clear map
		btmap_.clear();
		openTaskMap.clear();
		stmap_.clear();
		pmap_.clear();

		try {

			// iterate through tasks using taskmodel
			for (Task mr : getTasks()) {
				// for each task, get state and skip CLOSED or PR tasks
				if (isClosed(mr))
					continue;

				if (!CategoryModel.getReference().isShown(mr.getCategory()))
					continue;

				// use task due date to build a day key
				Date due = mr.getDueDate();
				if (due == null)
					continue;

				int key = DateUtil.dayOfEpoch(due);

				// add the task string to the btmap_
				// add the task to the mrs_ Vector. This is used by the todo gui
				Collection<Task> o = btmap_.get(new Integer(key));
				if (o == null) {
					o = new LinkedList<Task>();
					btmap_.put(new Integer(key), o);
				}

				o.add(mr);
				openTaskMap.add(mr);
			}

			if (db_ instanceof TaskDB) {

				for (Project pj : getProjects()) {
					if (pj.getDueDate() == null)
						continue;

					if (pj.getStatus().equals(
							Resource.getPlainResourceString("CLOSED")))
						continue;

					if (!CategoryModel.getReference().isShown(pj.getCategory()))
						continue;

					// use task due date to build a day key
					Date due = pj.getDueDate();
					int key = DateUtil.dayOfEpoch(due);
					;

					// add the string to the btmap_
					Collection<Project> o = pmap_.get(new Integer(key));
					if (o == null) {
						o = new LinkedList<Project>();
						pmap_.put(new Integer(key), o);
					}

					o.add(pj);
				}

				for (Subtask st : getSubTasks()) {
					if (st.getCloseDate() != null || st.getDueDate() == null)
						continue;

					Task mr = getTask(st.getTask().intValue());
					String cat = mr.getCategory();
					if (cat == null || cat.equals(""))
						cat = CategoryModel.UNCATEGORIZED;

					if (!CategoryModel.getReference().isShown(cat))
						continue;

					// use task due date to build a day key
					Date due = st.getDueDate();
					int key = DateUtil.dayOfEpoch(due);
					;

					// add the string to the btmap_
					Collection<Subtask> o = stmap_.get(new Integer(key));
					if (o == null) {
						o = new LinkedList<Subtask>();
						stmap_.put(new Integer(key), o);
					}

					o.add(st);
				}
			}

		} catch (Exception e) {

			Errmsg.errmsg(e);
			return;
		}

	}

	/**
	 * Instantiates a new task model.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private TaskModel() throws Exception {

		btmap_ = new HashMap<Integer, Collection<Task>>();
		stmap_ = new HashMap<Integer, Collection<Subtask>>();
		pmap_ = new HashMap<Integer, Collection<Project>>();
		openTaskMap = new Vector<Task>();

		db_ = new TaskJdbcDB();

		String sm = db_.getOption("SMODEL");
		if (sm == null) {
			try {
				// load XML from a file in the JAR
				// System.out.println("Loading default task model");
				taskTypes_.loadDefault();
				sm = taskTypes_.toString();
				db_.setOption(new BorgOption("SMODEL", sm));
			} catch (NoSuchMethodError nsme) {
				// running in a Palm conduit under JRE 1.3
				// ignore
			} catch (Exception e) {
				Errmsg.errmsg(e);
				return;
			}
		} else {
			taskTypes_.fromString(sm);
		}

		CategoryModel.getReference().addSource(this);
		CategoryModel.getReference().addListener(this);

	}

	/**
	 * Delete a task
	 * 
	 * @param tasknum
	 *            the task id
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void delete(int tasknum) throws Exception {
		delete(tasknum, false);
	}

	/**
	 * Delete a task
	 * 
	 * @param tasknum
	 *            the task id
	 * @param undo
	 *            true if we are executing an undo
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void delete(int tasknum, boolean undo) throws Exception {

		try {
			LinkModel.getReference().deleteLinks(tasknum, Task.class);
			if (!undo) {
				Task task = getTask(tasknum);
				UndoLog.getReference().addItem(TaskUndoItem.recordDelete(task));
				// subtasks are removed by cascading delete, so set undo records
				// here
				Collection<Subtask> coll = getSubTasks(task.getKey());
				if (coll != null) {
					for (Subtask st : coll) {
						SubtaskUndoItem.recordDelete(st);
					}
				}
			}
			db_.delete(tasknum);
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

		load_map();

		refreshListeners();

	}

	/**
	 * Delete a project.
	 * 
	 * @param id
	 *            the project id
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void deleteProject(int id) throws Exception {

		int ret = JOptionPane.showConfirmDialog(null, Resource
				.getResourceString("cannot_undo"), null,
				JOptionPane.OK_CANCEL_OPTION);

		if (ret != JOptionPane.OK_OPTION)
			return;

		try {
			if (db_ instanceof TaskDB == false)
				throw new Warning(Resource
						.getPlainResourceString("SubtaskNotSupported"));
			TaskDB sdb = (TaskDB) db_;
			beginTransaction();
			LinkModel.getReference().deleteLinks(id, Project.class);

			sdb.deleteProject(id);
			commitTransaction();
		} catch (Exception e) {
			rollbackTransaction();
			Errmsg.errmsg(e);
		}

		load_map();

		refreshListeners();

	}

	/**
	 * Save a task.
	 * 
	 * @param task
	 *            the task
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void savetask(Task task) throws Exception {
		savetask(task, false);
	}

	/**
	 * Save a task.
	 * 
	 * @param task
	 *            the task
	 * @param undo
	 *            true if we are executing an undo
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void savetask(Task task, boolean undo) throws Exception {

		// validations
		if (task.getProject() != null) {
			Project p = TaskModel.getReference().getProject(
					task.getProject().intValue());
			if (p != null && task.getDueDate() != null
					&& p.getDueDate() != null
					&& DateUtil.isAfter(task.getDueDate(), p.getDueDate())) {
				throw new Warning(Resource
						.getPlainResourceString("taskdd_warning"));
			}
		}

		// add task to DB
		Integer num = task.getKey();
		Task indb = null;
		if (num != null)
			indb = getTask(num);

		// if the task number is -1, it is a new task so
		// get a new task number.
		if (num == null || num.intValue() == -1 || indb == null) {
			if (!undo || num == null) {
				int newkey = db_.nextkey();
				task.setKey(newkey);
			}
			db_.addObj(task);
			if (!undo) {
				Task t = getTask(task.getKey());
				UndoLog.getReference().addItem(TaskUndoItem.recordAdd(t));
			}

		} else {
			// task exists - so update existing task in DB

			// update close date
			if (task.getState() != null && isClosed(task))
				task.setCompletionDate(new Date());
			int key = task.getKey();
			task.setKey(key);
			if (!undo) {
				Task t = getTask(task.getKey());
				UndoLog.getReference().addItem(TaskUndoItem.recordUpdate(t));
			}
			db_.updateObj(task);

		}

		load_map();

		// inform views of data change
		refreshListeners();

	}

	/**
	 * create a new task
	 * 
	 * @return the task
	 */
	public Task newMR() {
		return (db_.newObj());
	}

	/**
	 * Gets a task by id.
	 * 
	 * @param num
	 *            the id
	 * 
	 * @return the task
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Task getTask(int num) throws Exception {
		return (db_.readObj(num));
	}

	/**
	 * close a task
	 * 
	 * @param num
	 *            the task id
	 * 
	 * @throws Exception
	 *             the exception
	 * @throws Warning
	 *             the warning
	 */
	public void close(int num) throws Exception, Warning {

		for (Subtask st : TaskModel.getReference().getSubTasks(num)) {
			if (st.getCloseDate() == null) {
				throw new Warning(Resource.getResourceString("open_subtasks"));
			}
		}

		Task task = getTask(num);
		task.setState(TaskModel.getReference().getTaskTypes().getFinalState(
				task.getType()));
		savetask(task);
	}

	/**
	 * Close a project.
	 * 
	 * @param num
	 *            the project id
	 * 
	 * @throws Exception
	 *             the exception
	 * @throws Warning
	 *             the warning
	 */
	public void closeProject(int num) throws Exception, Warning {

		Project p = getProject(num);
		p.setStatus(Resource.getPlainResourceString("CLOSED"));
		saveProject(p);
	}

	/**
	 * export the task data for all tasks to XML. Also exports the options
	 * 
	 * @param fw
	 *            the writer to send XML to
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void export(Writer fw) throws Exception {

		// FileWriter fw = new FileWriter(fname);
		fw.write("<TASKS>\n");
		TaskXMLAdapter ta = new TaskXMLAdapter();

		// export options
		for (BorgOption option : db_.getOptions()) {
			XTree xt = new XTree();
			xt.name("OPTION");
			xt.appendChild(option.getKey(), option.getValue());
			fw.write(xt.toString());
		}

		ProjectXMLAdapter pa = new ProjectXMLAdapter();

		// export projects
		if (TaskModel.getReference().hasSubTasks()) {

			for (Project p : getProjects()) {
				XTree xt = pa.toXml(p);
				fw.write(xt.toString());
			}

		}
		// export tasks
		for (Task task : getTasks()) {
			XTree xt = ta.toXml(task);
			fw.write(xt.toString());
		}

		SubtaskXMLAdapter sta = new SubtaskXMLAdapter();

		// export subtasks
		if (TaskModel.getReference().hasSubTasks()) {
			for (Subtask stask : getSubTasks()) {
				XTree xt = sta.toXml(stask);
				fw.write(xt.toString());
			}

			TasklogXMLAdapter tla = new TasklogXMLAdapter();

			// export tasklogs
			for (Tasklog tlog : getLogs()) {
				XTree xt = tla.toXml(tlog);
				fw.write(xt.toString());
			}

		}
		fw.write("</TASKS>");

	}

	/**
	 * Get all projects.
	 * 
	 * @return the projects
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Project> getProjects() throws Exception {
		if (db_ instanceof TaskDB == false)
			return new ArrayList<Project>();
		TaskDB sdb = (TaskDB) db_;
		return sdb.getProjects();
	}

	/**
	 * Get a project by id.
	 * 
	 * @param id
	 *            the id
	 * 
	 * @return the project
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Project getProject(int id) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));
		TaskDB sdb = (TaskDB) db_;
		return sdb.getProject(id);
	}

	/**
	 * Import all task related entities from xml
	 * 
	 * @param xt
	 *            the XML
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void importXml(XTree xt) throws Exception {

		TaskXMLAdapter aa = new TaskXMLAdapter();
		SubtaskXMLAdapter sa = new SubtaskXMLAdapter();
		TasklogXMLAdapter la = new TasklogXMLAdapter();
		ProjectXMLAdapter pa = new ProjectXMLAdapter();

		JdbcDB.execSQL("SET REFERENTIAL_INTEGRITY FALSE;");

		// for each appt - create an Appointment and store
		for (int i = 1;; i++) {
			XTree ch = xt.child(i);
			if (ch == null)
				break;

			if (ch.name().equals("OPTION")) {
				XTree opt = ch.child(1);
				if (opt == null)
					continue;

				if (opt.name().equals("SMODEL")) {
					taskTypes_.fromString(opt.value());
					db_.setOption(new BorgOption("SMODEL", taskTypes_
							.toString()));

				} else {
					db_.setOption(new BorgOption(opt.name(), opt.value()));
				}
			}

			else if (ch.name().equals("Task")) {
				Task task = aa.fromXml(ch);
				if (task.getPriority() == null)
					task.setPriority(new Integer(3));

				db_.addObj(task);

			}

			else if (ch.name().equals("Subtask")) {
				Subtask subtask = sa.fromXml(ch);
				try {
					subtask.setKey(-1);
					saveSubTask(subtask);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}

			else if (ch.name().equals("Tasklog")) {
				Tasklog tlog = la.fromXml(ch);
				try {
					tlog.setKey(-1);
					saveLog(tlog);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			} else if (ch.name().equals("Project")) {
				Project p = pa.fromXml(ch);
				try {
					TaskDB sdb = (TaskDB) db_;
					sdb.addProject(p);
				} catch (Exception e) {
					Errmsg.errmsg(e);
				}
			}
		}
		JdbcDB.execSQL("SET REFERENTIAL_INTEGRITY TRUE;");
		// refresh all views that are displaying appt data from this model
		load_map();
		refreshListeners();

	}

	/**
	 * sync with db
	 */
	public void sync() {
		db_.sync();
		load_map();
		refreshListeners();
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Model.Listener#refresh()
	 */
	public void refresh() {
		try {
			load_map();
			refreshListeners();
		} catch (Exception e) {
			Errmsg.errmsg(e);
		}

	}

	/**
	 * Get the sub tasks for a task
	 * 
	 * @param taskid
	 *            the task id
	 * 
	 * @return the sub tasks
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Subtask> getSubTasks(int taskid) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		return sdb.getSubTasks(taskid);

	}

	/**
	 * Get all sub tasks.
	 * 
	 * @return the sub tasks
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Subtask> getSubTasks() throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		return sdb.getSubTasks();

	}

	/**
	 * Get a sub task by id.
	 * 
	 * @param id
	 *            the subtask id
	 * 
	 * @return the sub task
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Subtask getSubTask(int id) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		return sdb.getSubTask(id);

	}

	/**
	 * Get all tasks for a project.
	 * 
	 * @param projectid
	 *            the projectid
	 * 
	 * @return the tasks
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Task> getTasks(int projectid) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		return sdb.getTasks(projectid);

	}

	/**
	 * Get sub projects for a project - direct children only
	 * 
	 * @param projectid
	 *            the project id
	 * 
	 * @return the sub projects
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Project> getSubProjects(int projectid) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		return sdb.getSubProjects(projectid);

	}

	/**
	 * Gets the entire project tree for a project
	 * 
	 * @param projectid
	 *            the root project id
	 * 
	 * @return the all sub projects
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Project> getAllSubProjects(int projectid)
			throws Exception {
		Collection<Project> c = new ArrayList<Project>();
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));
		addSubProjectsToCollection(c, projectid);
		return c;
	}

	
	private void addSubProjectsToCollection(Collection<Project> c, int projectid)
			throws Exception {

		// add my children
		Collection<Project> children = getSubProjects(projectid);
		if (children.isEmpty())
			return;
		c.addAll(children);

		// add my children's children
		Iterator<Project> it = children.iterator();
		while (it.hasNext()) {
			Project p = it.next();
			addSubProjectsToCollection(c, p.getKey());
		}

	}

	/**
	 * Delete a sub task.
	 * 
	 * @param id
	 *            the subtask id
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void deleteSubTask(int id) throws Exception {
		deleteSubTask(id, false);
	}

	/**
	 * Delete a sub task.
	 * 
	 * @param id
	 *            the subtask id
	 * @param undo
	 *            true if we are executing an undo
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void deleteSubTask(int id, boolean undo) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		if (!undo) {
			Subtask st = sdb.getSubTask(id);
			SubtaskUndoItem.recordDelete(st);
		}
		sdb.deleteSubTask(id);
		load_map();
		refreshListeners();
	}

	/**
	 * Save a sub task.
	 * 
	 * @param s
	 *            the subtask
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void saveSubTask(Subtask s) throws Exception {
		saveSubTask(s, false);
	}

	/**
	 * Save a sub task.
	 * 
	 * @param s
	 *            the subtask 
	 * @param undo
	 *           true if we are executing an undo
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void saveSubTask(Subtask s, boolean undo) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		TaskDB sdb = (TaskDB) db_;
		if (s.getKey() <= 0 || null == sdb.getSubTask(s.getKey())) {
			if (!undo || s.getKey() == -1)
				s.setKey(sdb.nextSubTaskKey());
			sdb.addSubTask(s);
			if (!undo) {
				Subtask st = sdb.getSubTask(s.getKey());
				SubtaskUndoItem.recordAdd(st);
			}
		} else {
			if (!undo) {
				Subtask st = sdb.getSubTask(s.getKey());
				SubtaskUndoItem.recordUpdate(st);
			}
			sdb.updateSubTask(s);
		}

		load_map();
		refreshListeners();
	}

	/**
	 * Add a task log entry.
	 * 
	 * @param taskid
	 *            the task id
	 * @param desc
	 *            the log message
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void addLog(int taskid, String desc) throws Exception {
		if (db_ instanceof TaskDB == false)
			return;
		// throw new Exception(Resource
		// .getPlainResourceString("SubtaskNotSupported"));
		TaskDB sdb = (TaskDB) db_;
		sdb.addLog(taskid, desc);
	}

	/**
	 * Save a task log.
	 * 
	 * @param tlog
	 *            the task log
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void saveLog(Tasklog tlog) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));
		TaskDB sdb = (TaskDB) db_;
		sdb.saveLog(tlog);
	}

	/**
	 * Get all task logs for a task.
	 * 
	 * @param taskid
	 *            the task id
	 * 
	 * @return the logs
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Tasklog> getLogs(int taskid) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));
		TaskDB sdb = (TaskDB) db_;
		return sdb.getLogs(taskid);
	}

	/**
	 * Get all task logs.
	 * 
	 * @return the logs
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Collection<Tasklog> getLogs() throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));
		TaskDB sdb = (TaskDB) db_;
		return sdb.getLogs();
	}

	/**
	 * return the number of days left before a given date.
	 * 
	 * @param dd
	 *            the date
	 * 
	 * @return the number of days left
	 */
	public static int daysLeft(Date dd) {

		if (dd == null)
			return 0;
		Calendar today = new GregorianCalendar();
		Calendar dcal = new GregorianCalendar();
		dcal.setTime(dd);

		// find days left
		int days = 0;
		if (dcal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
			days = dcal.get(Calendar.DAY_OF_YEAR)
					- today.get(Calendar.DAY_OF_YEAR);
		} else {
			days = new Long((dd.getTime() - today.getTime().getTime())
					/ (1000 * 60 * 60 * 24)).intValue();
		}

		// if due date is past, set days left to 0
		// negative days are silly
		if (days < 0)
			days = 0;
		return days;
	}

	/**
	 * determine the number fo days between two dates
	 * 
	 * @param start
	 *            the first date
	 * @param dd
	 *            the later date
	 * 
	 * @return the int
	 */
	public static int daysBetween(Date start, Date dd) {

		if (dd == null)
			return 0;
		Calendar startcal = new GregorianCalendar();
		Calendar dcal = new GregorianCalendar();
		dcal.setTime(dd);
		startcal.setTime(start);

		// find days left
		int days = 0;
		if (dcal.get(Calendar.YEAR) == startcal.get(Calendar.YEAR)) {
			days = dcal.get(Calendar.DAY_OF_YEAR)
					- startcal.get(Calendar.DAY_OF_YEAR);
		} else {
			days = new Long((dd.getTime() - startcal.getTime().getTime())
					/ (1000 * 60 * 60 * 24)).intValue();
		}

		// if due date is past, set days left to 0
		// negative days are silly
		if (days < 0)
			days = 0;
		return days;
	}

	/**
	 * Checks for sub task support
	 * 
	 * @return true, if the db supports subtasks (always true as of version 1.7)
	 */
	public boolean hasSubTasks() {
		return db_ instanceof TaskDB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Transactional#beginTransaction()
	 */
	public void beginTransaction() throws Exception {
		if (db_ instanceof Transactional) {
			Transactional t = (Transactional) db_;
			t.beginTransaction();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Transactional#commitTransaction()
	 */
	public void commitTransaction() throws Exception {
		if (db_ instanceof Transactional) {
			Transactional t = (Transactional) db_;
			t.commitTransaction();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.borg.model.Transactional#rollbackTransaction()
	 */
	public void rollbackTransaction() throws Exception {
		if (db_ instanceof Transactional) {
			Transactional t = (Transactional) db_;
			t.rollbackTransaction();
		}
	}

	/**
	 * Save a project.
	 * 
	 * @param p
	 *            the project
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void saveProject(Project p) throws Exception {
		saveProject(p, false);
	}

	/**
	 * Save a project.
	 * 
	 * @param p
	 *            the project
	 * @param undo
	 *            true if we are executing an undo
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void saveProject(Project p, boolean undo) throws Exception {
		if (db_ instanceof TaskDB == false)
			throw new Warning(Resource
					.getPlainResourceString("SubtaskNotSupported"));

		// validation that task due dates are before project due date
		if (p.getKey() != -1) {
			for (Task t : TaskModel.getReference().getTasks(p.getKey())) {
				if (p.getDueDate() != null && t.getDueDate() != null
						&& !TaskModel.isClosed(t)
						&& DateUtil.isAfter(t.getDueDate(), p.getDueDate())) {
					throw new Warning(Resource
							.getPlainResourceString("projdd_warning")
							+ ": " + t.getKey());
				}
			}

			for (Project child : TaskModel.getReference().getSubProjects(
					p.getKey())) {
				if (p.getDueDate() != null && child.getDueDate() != null
						&& !TaskModel.isClosed(child)
						&& DateUtil.isAfter(child.getDueDate(), p.getDueDate())) {
					throw new Warning(Resource
							.getPlainResourceString("projchild_warning")
							+ ": " + child.getKey());
				}
			}
		}

		// validate against parent
		if (p.getParent() != null) {
			Project par = TaskModel.getReference().getProject(
					p.getParent().intValue());
			if (par != null) {
				if (p.getDueDate() != null && par.getDueDate() != null
						&& DateUtil.isAfter(p.getDueDate(), par.getDueDate())) {
					throw new Warning(Resource
							.getPlainResourceString("projpar_warning"));
				}
			}
		}

		if (p.getStatus().equals(Resource.getPlainResourceString("CLOSED"))) {
			// make sure that all tasks are closed
			for (Task pt : TaskModel.getReference().getTasks(p.getKey())) {
				if (!isClosed(pt)) {
					throw new Warning(Resource
							.getPlainResourceString("close_proj_warn"));
				}
			}
		}

		TaskDB sdb = (TaskDB) db_;
		if (p.getKey() <= 0) {
			if (!undo)
				p.setKey(sdb.nextProjectKey());
			sdb.addProject(p);
			if (!undo) {
				Project t = getProject(p.getKey());
				UndoLog.getReference().addItem(ProjectUndoItem.recordAdd(t));
			}
		} else {
			if (!undo) {
				Project t = getProject(p.getKey());
				UndoLog.getReference().addItem(ProjectUndoItem.recordUpdate(t));
			}
			sdb.updateProject(p);
		}

		load_map();
		refreshListeners();
	}

	/**
	 * Checks if a task is closed.
	 * 
	 * @param t
	 *            the task
	 * 
	 * @return true, if the task is closed
	 */
	static public boolean isClosed(Task t) {
		String stat = t.getState();
		String type = t.getType();
		return stat.equals(TaskModel.getReference().getTaskTypes()
				.getFinalState(type));
	}

	/**
	 * Checks if a project is closed.
	 * 
	 * @param p
	 *            the project
	 * 
	 * @return true, if a project is closed
	 */
	static public boolean isClosed(Project p) {
		String stat = p.getStatus();
		return stat.equals(Resource.getPlainResourceString("CLOSED"));
	}
}

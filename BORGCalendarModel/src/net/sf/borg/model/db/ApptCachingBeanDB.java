/*
This file is part of BORG.
 
	BORG is free software; you can redistribute it and/or modify
	it under the terms of the GNU General public final synchronized License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.
 
	BORG is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General public final synchronized License for more details.
 
	You should have received a copy of the GNU General public final synchronized License
	along with BORG; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
Copyright 2003 by Mike Berger
 */

package net.sf.borg.model.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.borg.model.Appointment;
import net.sf.borg.model.AppointmentKeyFilter;

public class ApptCachingBeanDB extends CachingBeanDB implements AppointmentKeyFilter
{
	public ApptCachingBeanDB(BeanDB delegate)
	{
		super(delegate);
//		filter = (AppointmentKeyFilter) delegate;
	}
	
	public Collection getTodoKeys() throws Exception {
		refresh();
		return todoKeys;
	}


	public Collection getRepeatKeys() throws Exception {
		refresh();
		return repeatKeys;
	}
	
	// protected //
	protected boolean refresh() throws Exception
	{
		if (!super.refresh())
			return false;
		
		// Refresh our cached ToDo and Repeat keys.
		todoKeys.clear();
		repeatKeys.clear();
		Iterator itr = getObjectMap().entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			Appointment appt = (Appointment) entry.getValue();
			if (appt.getTodo())
				todoKeys.add(entry.getKey());
			if (appt.getRepeatFlag())
				repeatKeys.add(entry.getKey());
		}

		return true;
	}
	
	// private //
//	private AppointmentKeyFilter filter;
	private List todoKeys = new ArrayList();
	private List repeatKeys = new ArrayList();
}

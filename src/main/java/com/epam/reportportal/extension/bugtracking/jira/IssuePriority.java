/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-jira
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */ 
 
package com.epam.reportportal.extension.bugtracking.jira;

/**
 * IssueSeverity enumerator<br>
 * Describe default severities from JIRA (a while)
 * 
 * @author Andrei_Ramanchuk
 */
public enum IssuePriority {

	//@formatter:off
	BLOCKER(1),
	CRITICAL(2),
	MAJOR(3), 
	MINOR(4), 
	TRIVIAL(5);
	//@formatter:on

	private long priority;

	public long getValue() {
		return priority;
	}

	IssuePriority(long value) {
		this.priority = value;
	}

	public static IssuePriority findByName(String name) {
		for (IssuePriority type : IssuePriority.values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}

	public static boolean isPresent(String name) {
		return null != findByName(name);
	}
}
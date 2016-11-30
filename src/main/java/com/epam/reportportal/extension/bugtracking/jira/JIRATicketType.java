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
 * Define possible jira ticket types.
 * 
 * @author Aliaksei_Makayed
 * 
 */
public enum JIRATicketType {
	
	//@formatter:off
	STORY("story"),
	BUG("bug"), 
	IMPROVEMENT("improvement");
	//@formatter:on
	
	JIRATicketType(String value) {
		this.value = value;
	}

	private String value;

	public String getValue() {
		return value;
	}
	
	public static JIRATicketType findByName(String name) {
		for (JIRATicketType type : JIRATicketType.values()) {
			if (type.getValue().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}

	public static boolean isPresent(String name) {
		return null != findByName(name);
	}
}
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
 * Jira query builder. Provide functionality for building queries.<br>
 * 
 * <b>NOTE: This class isn't thread safe. It's not state less</b>
 * 
 * @author Aliaksei_Makayed
 * 
 */
public class JQLQueryBuilder {

	private StringBuilder query;

	private JQLQueryBuilder() {
		query = new StringBuilder();
	}

	public static JQLQueryBuilder getInstance() {
		return new JQLQueryBuilder();
	}

	public JQLQueryBuilder and(Condition condition, String value) {
		if (condition == null || value == null) {
			return this;
		}
		if (query.length() != 0) {
			query.append(" AND ");
		}
		query.append(condition.addCondition(value));
		return this;
	}

	public String build() {
		String result = query.toString();
		query = new StringBuilder();
		return result;
	}

	public enum Condition {
		EQUALS_PROJECT {
			@Override
			public String addCondition(String value) {
				if (value == null) {
					return "";
				}
				return new StringBuilder().append("project = ").append(value).toString();
			}
		},
		EQUALS_TYPE {
			@Override
			public String addCondition(String value) {
				if (value == null) {
					return "";
				}
				return new StringBuilder().append("type = ").append(value).toString();
			}
		},
		BEGIN_WITH_SUMMARY {
			@Override
			public String addCondition(String value) {
				if (value == null) {
					return "";
				}
				return new StringBuilder().append("summary~\"").append(value).append("\"").toString();
			}
		};

		abstract public String addCondition(String value);
	}

}
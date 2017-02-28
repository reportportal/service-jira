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

import com.atlassian.jira.rest.client.api.domain.BasicComponent;

import java.util.function.Function;

/**
 * Set of useful {@link java.util.function.Predicate}s and {@link Function}s for JIRA-related stuff
 *
 * @author Andrei_Ramanchuk
 */
final class JiraPredicates {

	private JiraPredicates() {

	}

	/**
	 * {@link BasicComponent} to project name converter
	 *
	 * @return converter
	 */
	static final Function<BasicComponent, String> COMPONENT_NAMES = BasicComponent::getName;

}
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

import org.junit.Assert;
import org.junit.Test;

public class JQLQueryBuilderTest {

	@Test
	public void testCreateInstance() {
		JQLQueryBuilder firstInstance = JQLQueryBuilder.getInstance();
		JQLQueryBuilder secondInstance = JQLQueryBuilder.getInstance();
		Assert.assertTrue(firstInstance != secondInstance);
	}

	@Test
	public void testNull() {
		JQLQueryBuilder builder = JQLQueryBuilder.getInstance();
		String query = builder.and(null, null).build();
		Assert.assertNotNull(query);
		Assert.assertEquals("", query);
	}

	@Test
	public void testNullExtended() {
		JQLQueryBuilder builder = JQLQueryBuilder.getInstance();
		String query = builder.and(JQLQueryBuilder.Condition.BEGIN_WITH_SUMMARY, null).and(JQLQueryBuilder.Condition.EQUALS_PROJECT, null).and(
				JQLQueryBuilder.Condition.EQUALS_TYPE, null)
				.build();
		Assert.assertNotNull(query);
		Assert.assertEquals("", query);
	}

	@Test
	public void testBuildFullQuery() {
		JQLQueryBuilder builder = JQLQueryBuilder.getInstance();
		String query = builder.and(JQLQueryBuilder.Condition.BEGIN_WITH_SUMMARY, "test_summary").and(JQLQueryBuilder.Condition.EQUALS_PROJECT, "TEST_PROJECT")
				.and(JQLQueryBuilder.Condition.EQUALS_TYPE, "story").build();
		Assert.assertNotNull(query);
		Assert.assertEquals("summary~\"test_summary\" AND project = TEST_PROJECT AND type = story", query);
	}

}
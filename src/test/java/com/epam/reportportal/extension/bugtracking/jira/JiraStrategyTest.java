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

import com.atlassian.jira.rest.client.api.*;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.util.concurrent.Promise;
import com.epam.ta.reportportal.database.entity.AuthType;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.item.issue.ExternalSystemType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class JiraStrategyTest {

	//	@Rule
	//	@Autowired
	//	public SpringFixtureRule dfRule;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private JiraStrategy jiraStrategy = jiraMock();

	@Test
	public void connectionTest() {
		final ExternalSystem details = new ExternalSystem();
		details.setProject("forConnectionTest");
		details.setExternalSystemAuth(AuthType.BASIC);
		details.setUsername("user");
		details.setPassword("password");
		details.setUrl("https://jira.epam.com");
		Assert.assertTrue(jiraStrategy.checkConnection(details));
		details.setProject("notExist");
		Assert.assertFalse(jiraStrategy.checkConnection(details));
		details.setProject("exception");
		Assert.assertFalse(jiraStrategy.checkConnection(details));
		details.setExternalSystemAuth(AuthType.OAUTH);
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(
				formattedSupplier(UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM.getDescription(), "AccessKey value cannot be NULL").get());
		jiraStrategy.checkConnection(details);

	}

	@Test
	public void getTicketTest() {
		final ExternalSystem system = new ExternalSystem();
		system.setUrl("https://jira.epam.com");
		system.setExternalSystemType(ExternalSystemType.JIRA);
		final Optional<Ticket> ticketId = jiraStrategy.getTicket("ticketId", system);
		Assert.assertTrue(ticketId.isPresent());
		Assert.assertEquals("ticketId", ticketId.get().getId());
		final Optional<Ticket> ticketNotExist = jiraStrategy.getTicket("ticketNotExist", system);
		Assert.assertFalse(ticketNotExist.isPresent());
		final Optional<Ticket> exceptionalTicket = jiraStrategy.getTicket("exceptionalTicket", system);
		Assert.assertFalse(exceptionalTicket.isPresent());
	}

	@Test
	public void getTicketFieldsTest() {
		final ExternalSystem details = new ExternalSystem();
		details.setUrl("https://jira.epam.com");
		details.setProject("project1");
		final List<PostFormField> ticketFields = jiraStrategy.getTicketFields("BUG", details);
		Assert.assertNotNull(ticketFields);
		Assert.assertFalse(ticketFields.isEmpty());
	}

	//
	//	@Bean(name = "jiraProvider")
	//	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	//	public JiraOAuthSecurityProvider jiraProvider() {
	//		return mock(JiraOAuthSecurityProvider.class);
	//	}
	//
	//	@Bean(name = "jiraOAuthService")
	//	@Scope(BeanDefinition.SCOPE_SINGLETON)
	//	public OAuthClientService jiraOAuthService() {
	//		return mock(OAuthClientService.class);
	//	}

	//	@Bean(name = "jiraStrategy")
	public JiraStrategy jiraMock() {
		return new JiraStrategy() {
			@Override
			public JiraRestClient getClient(String uri, String providedUsername, String providePassword) {
				JiraRestClient client = mock(JiraRestClient.class);
				ProjectRestClient projectRestClient = mock(ProjectRestClient.class);
				when(client.getProjectClient()).thenReturn(projectRestClient);
				// existing jira project
				Promise forConnection = mock(Promise.class);
				when(projectRestClient.getProject("forConnectionTest")).thenReturn(forConnection);
				when(forConnection.claim()).thenReturn(new Project(null, null, null, null, null, null, null, null, null, null, null, null));
				//
				Promise notExist = mock(Promise.class);
				when(projectRestClient.getProject("notExist")).thenReturn(notExist);
				when(notExist.claim()).thenReturn(null);
				// return exception during search jira project
				Promise exception = mock(Promise.class);
				when(projectRestClient.getProject("exception")).thenReturn(exception);
				when(exception.claim()).thenThrow(IOException.class);
				// getTicketTest
				SearchRestClient searchRestClient = mock(SearchRestClient.class);
				when(client.getSearchClient()).thenReturn(searchRestClient);
				Promise searchPromise = mock(Promise.class);
				when(searchRestClient.searchJql("issue = ticketId")).thenReturn(searchPromise);
				Promise ticketNotExist = mock(Promise.class);
				when(searchRestClient.searchJql("issue = ticketNotExist")).thenReturn(ticketNotExist);
				when(searchRestClient.searchJql("issue = exceptionalTicket")).thenThrow(IOException.class);

				Status status = new Status(null, null, "statusName", null, null);
				Issue issue = new Issue("summary", null, "ticketId", null, null, null, status, null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
				when(searchPromise.claim()).thenReturn(new SearchResult(0, 1, 1, singletonList(issue)));
				when(ticketNotExist.claim()).thenReturn(new SearchResult(0, 0, 0, new ArrayList<>()));
				IssueRestClient issueRestClient = mock(IssueRestClient.class);
				when(client.getIssueClient()).thenReturn(issueRestClient);
				Promise ticketPromise = mock(Promise.class);
				when(issueRestClient.getIssue("ticketId")).thenReturn(ticketPromise);
				when(ticketPromise.claim()).thenReturn(issue);
				Promise createdIssue = mock(Promise.class);
				when(issueRestClient.createIssue(any())).thenReturn(createdIssue);
				when(createdIssue.claim()).thenReturn(new BasicIssue(null, "ticketId", 1L));

				// getTicketFields
				Promise projectPromise = mock(Promise.class);
				when(projectRestClient.getProject("project1")).thenReturn(projectPromise);
				OptionalIterable<IssueType> issueTypes = new OptionalIterable<>(
						singletonList(new IssueType(null, 1L, "BUG", false, null, null)));
				Project project = new Project(null, null, "key", null, null, null, null, null,
						singletonList(new Version(null, 10000L, "VERSION1", "V1", false, false, null)),
						singletonList(new BasicComponent(null, 10001L, "Debug Component", "Debug")), issueTypes, singletonList(
						new ProjectRole(1L, null, "Developers", null,
								singletonList(new RoleActor(1L, "RoleActor", "", "RoleActor", null)))));
				when(projectPromise.claim()).thenReturn(project);
				Promise cimProject = mock(Promise.class);
				when(issueRestClient.getCreateIssueMetadata(any())).thenReturn(cimProject);
				UserRestClient userRestClient = mock(UserRestClient.class);
				when(client.getUserClient()).thenReturn(userRestClient);
				Promise jiraUser = mock(Promise.class);
				when(userRestClient.getUser("customAssignee1")).thenReturn(jiraUser);
				HashMap<String, URI> avatarUris = new HashMap<>();
				avatarUris.put("48x48", URI.create("https://jira.epam.com"));
				when(jiraUser.claim()).thenReturn(new User(null, "customAssignee1", "customAssignee1", null, null, avatarUris, null));
				HashMap<String, CimFieldInfo> fields = getFields();

				when(cimProject.claim()).thenReturn(singletonList(new CimProject(null, "Bug", null, null, null,
						singletonList(new CimIssueType(null, 1L, null, false, null, null, fields)))));
				return client;
			}
		};
	}

	private HashMap<String, CimFieldInfo> getFields() {
		HashMap<String, CimFieldInfo> fields = new HashMap<>();
		fields.put("summary",
				new CimFieldInfo("summary", true, "Summary", new FieldSchema("string", null, null, null, null), null, null, null));
		fields.put("components",
				new CimFieldInfo("components", false, "Component/s", new FieldSchema("array", null, null, null, null), null, null, null));
		fields.put("fixVersions",
				new CimFieldInfo("fixVersions", false, "Fix Version/s", new FieldSchema("array", null, null, null, null), null, null,
						null));
		fields.put("versions",
				new CimFieldInfo("versions", false, "Affects Version/s", new FieldSchema("array", null, null, null, null), null, null,
						null));
		fields.put("priority", new CimFieldInfo("priority", false, "Priority", new FieldSchema("priority", null, null, null, null), null,
				singletonList(new BasicPriority(null, 1L, "Blocker")), null));
		fields.put("issuetype",
				new CimFieldInfo("issuetype", true, "Issue Type", new FieldSchema("issuetype", null, null, null, null), null, null, null));
		fields.put("assignee",
				new CimFieldInfo("assignee", false, "Assignee", new FieldSchema("user", null, null, null, null), null, null, null));
		fields.put("customAssignee1",
				new CimFieldInfo("customAssignee1", false, "CustomAssignee1", new FieldSchema("user", null, null, null, 1L), null, null,
						null));
		fields.put("description",
				new CimFieldInfo("description", false, "Description", new FieldSchema("string", null, "description", null, null), null,
						null, null));
		fields.put("customArrayField1",
				new CimFieldInfo("customArrayField1", false, "CustomArrayField1", new FieldSchema("array", null, null, null, 1L), null,
						null, null));
		fields.put("customArrayField2",
				new CimFieldInfo("customArrayField2", false, "CustomArrayField2", new FieldSchema("array", null, null, null, 1L), null,
						singletonList(new CustomFieldOption(1L, null, null, null, null)), null));
		fields.put("labels", new CimFieldInfo("labels", false, "Labels", new FieldSchema("array", null, null, null, 1L), null, null, null));
		fields.put("customNumberField1",
				new CimFieldInfo("customNumberField1", false, "CustomNumberField1", new FieldSchema("number", null, null, null, 1L), null,
						null, null));
		fields.put("customComponentField1",
				new CimFieldInfo("customComponentField1", false, "customComponentField1", new FieldSchema("string", null, null, null, 1L),
						null, singletonList(new CustomFieldOption(1L, null, null, null, null)), null));
		fields.put("duedate",
				new CimFieldInfo("duedate", false, "Due Date", new FieldSchema("date", null, null, null, 1L), null, null, null));
		fields.put("timetracking",
				new CimFieldInfo("timetracking", false, "Time Tracking", new FieldSchema("timetracking", null, null, null, 1L), null, null,
						null));
		fields.put("attachment",
				new CimFieldInfo("attachment", false, "Attachment", new FieldSchema("array", null, null, null, 1L), null, null, null));
		fields.put("project",
				new CimFieldInfo("project", true, "Project", new FieldSchema("project", null, null, null, 1L), null, null, null));
		fields.put("Sprint",
				new CimFieldInfo("Sprint", false, "Sprint", new FieldSchema("sprint", null, null, null, 1L), null, null, null));
		return fields;
	}
}
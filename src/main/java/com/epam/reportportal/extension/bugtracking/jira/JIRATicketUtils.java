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

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import com.google.common.collect.Lists;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provide tools for working with JIRA tickets(conversion).
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
public class JIRATicketUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(JIRATicketUtils.class);
	public static final DateTimeFormatter JIRA_DATE_FORMATTER = ISODateTimeFormat.date();

	// Field format from UI calendar control
	public static final String JIRA_FORMAT = "yyyy-MM-dd";

	private JIRATicketUtils() {
	}

	public static Ticket toTicket(Issue input, ExternalSystem details) {
		Ticket ticket = new Ticket();
		ticket.setId(input.getKey());
		ticket.setSummary(input.getSummary());
		ticket.setStatus(input.getStatus().getName());
		ticket.setTicketUrl(details.getExternalSystemType().makeUrl(details.getUrl(), input.getKey()));
		return ticket;
	}

	public static IssueInput toIssueInput(JiraRestClient client, Project jiraProject, Optional<IssueType> issueType, PostTicketRQ ticketRQ,
			Iterable<String> itemIds, JIRATicketDescriptionService descriptionService) {
		String userDefinedDescription = "";
		IssueInputBuilder issueInputBuilder = new IssueInputBuilder(jiraProject, issueType.get());
		GetCreateIssueMetadataOptions options = new GetCreateIssueMetadataOptionsBuilder().withExpandedIssueTypesFields()
				.withProjectKeys(jiraProject.getKey()).build();
		Iterator<CimProject> projects = client.getIssueClient().getCreateIssueMetadata(options).claim().iterator();
		BusinessRule.expect(projects.hasNext(), Predicates.equalTo(true))
				.verify(ErrorType.UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM, String.format("Project %s not found", jiraProject.getKey()));
		CimProject project = projects.next();
		CimIssueType cimIssueType = EntityHelper.findEntityById(project.getIssueTypes(), issueType.get().getId());
		List<PostFormField> fields = ticketRQ.getFields();
		for (PostFormField one : fields) {
			CimFieldInfo cimFieldInfo = cimIssueType.getFields().get(one.getId());
			if (one.getIsRequired() && one.getValue().isEmpty()) {
				BusinessRule.fail().withError(ErrorType.UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM,
						Suppliers.formattedSupplier("Required parameter '{}' is empty", one.getFieldName()));
			}

			if (!checkField(one))
				continue;

			// Skip issuetype and project fields cause got them in
			// issueInputBuilder already
			if (one.getId().equalsIgnoreCase(IssueFieldId.ISSUE_TYPE_FIELD.id) || one.getId()
					.equalsIgnoreCase(IssueFieldId.PROJECT_FIELD.id))
				continue;

			if (one.getId().equalsIgnoreCase(IssueFieldId.DESCRIPTION_FIELD.id))
				userDefinedDescription = one.getValue().get(0);
			if (one.getId().equalsIgnoreCase(IssueFieldId.SUMMARY_FIELD.id)) {
				issueInputBuilder.setSummary(one.getValue().get(0));
				continue;
			}
			if (one.getId().equalsIgnoreCase(IssueFieldId.PRIORITY_FIELD.id)) {
				if (null != IssuePriority.findByName(one.getValue().get(0)))
					issueInputBuilder.setPriorityId(IssuePriority.findByName(one.getValue().get(0)).getValue());
				continue;
			}
			if (one.getId().equalsIgnoreCase(IssueFieldId.COMPONENTS_FIELD.id)) {
				issueInputBuilder.setComponentsNames(one.getValue());
				continue;
			}
			if (one.getId().equalsIgnoreCase(IssueFieldId.ASSIGNEE_FIELD.id)) {
				issueInputBuilder.setAssigneeName(one.getValue().get(0));
				continue;
			}
			if (one.getId().equalsIgnoreCase(IssueFieldId.REPORTER_FIELD.id)) {
				issueInputBuilder.setReporterName(one.getValue().get(0));
				continue;
			}
			if (one.getId().equalsIgnoreCase(IssueFieldId.AFFECTS_VERSIONS_FIELD.id)) {
				issueInputBuilder.setAffectedVersionsNames(one.getValue());
				continue;
			}
			if (one.getId().equalsIgnoreCase(IssueFieldId.FIX_VERSIONS_FIELD.id)) {
				issueInputBuilder.setFixVersionsNames(one.getValue());
				continue;
			}

			// Arrays and fields with 'allowedValues' handler
			if (null != cimFieldInfo.getAllowedValues()) {
				try {
					List<ComplexIssueInputFieldValue> arrayOfValues = Lists.newArrayList();
					for (Object object : cimFieldInfo.getAllowedValues()) {
						if (object instanceof CustomFieldOption) {
							CustomFieldOption cfo = (CustomFieldOption) object;
							arrayOfValues.add(ComplexIssueInputFieldValue.with("id", String.valueOf(cfo.getId())));
						}
					}
					if (one.getFieldType().equalsIgnoreCase(IssueFieldType.ARRAY.name))
						issueInputBuilder.setFieldValue(one.getId(), arrayOfValues);
					else
						issueInputBuilder.setFieldValue(one.getId(), arrayOfValues.get(0));
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
					issueInputBuilder.setFieldValue(one.getId(), "ReportPortal autofield");
				}
			} else {
				if (one.getFieldType().equalsIgnoreCase(IssueFieldType.ARRAY.name)) {
					if (one.getId().equalsIgnoreCase(IssueFieldId.LABELS_FIELD.id))
						issueInputBuilder.setFieldValue(one.getId(), processLabels(one.getValue().get(0)));
					else
						issueInputBuilder.setFieldValue(one.getId(), one.getValue());
				} else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.NUMBER.name))
					issueInputBuilder.setFieldValue(one.getId(), Long.valueOf(one.getValue().get(0)));
				else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.USER.name)) {
					if (!one.getValue().get(0).equals("")) {
						// TODO create user cache (like for projects) for JIRA
						// 'user' type fields
						User jiraUser = client.getUserClient().getUser(one.getValue().get(0)).claim();
						// FIXME change validator as common validate method for
						// fields
						BusinessRule.expect(jiraUser, Predicates.notNull()).verify(ErrorType.UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM,
								Suppliers.formattedSupplier("Value for '{}' field with 'user' type wasn't found in JIRA",
										one.getValue().get(0)));
						issueInputBuilder.setFieldValue(one.getId(), jiraUser);
					}
				} else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.DATE.name)) {
					try {
						SimpleDateFormat format = new SimpleDateFormat(JIRA_FORMAT);
						Date fieldValue = format.parse(one.getValue().get(0));
						issueInputBuilder.setFieldValue(one.getId(), JIRA_DATE_FORMATTER.print(fieldValue.getTime()));
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
					}
				} else
					issueInputBuilder.setFieldValue(one.getId(), one.getValue().get(0));
			}
		}
		issueInputBuilder.setDescription(userDefinedDescription.concat("\n").concat(descriptionService.getDescription(itemIds, ticketRQ)));
		return issueInputBuilder.build();
	}

	/**
	 * Processing labels for JIRA through spaces split
	 *
	 * @param values
	 * @return
	 */
	private static List<String> processLabels(String values) {
		return Stream.of(values.split(" ")).collect(Collectors.toList());
	}

	/**
	 * Just JIRA field types enumerator
	 *
	 * @author Andrei_Ramanchuk
	 */
	public enum IssueFieldType {
		//@formatter:off
		ARRAY("array"), 
		DATE("date"), 
		NUMBER("number"), 
		USER("user"), 
		STRING("string");
		//@formatter:on

		private final String name;

		public String getName() {
			return name;
		}

		IssueFieldType(String value) {
			this.name = value;
		}
	}

	private static boolean checkField(PostFormField field) {
		return ((null != field.getValue()) && (!field.getValue().isEmpty()) && (!"".equals(field.getValue().get(0))));
	}
}
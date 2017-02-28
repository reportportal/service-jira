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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;

/**
 * Provide functionality for building jira's ticket description
 * 
 * @author Aliaksei_Makayed
 * @author Dzmitry_Kavalets
 */
@Service
public class JIRATicketDescriptionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(JIRATicketDescriptionService.class);

	public static final String JIRA_MARKUP_LINE_BREAK = "\\\\ ";
	public static final String BACK_LINK_HEADER = "h3.*Back link to Report Portal:*";
	public static final String BACK_LINK_PATTERN = "[Link to defect|%s]%n";
	public static final String COMMENTS_HEADER = "h3.*Test Item comments:*";
	public static final String CODE = "{code}";
	private static final String IMAGE_CONTENT = "image";
	private static final String IMAGE_HEIGHT_TEMPLATE = "|height=366!";

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private TestItemRepository itemRepository;

	private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	/**
	 * Generate ticket description using logs of specified test item.
	 * 
	 * @param itemIds
	 * @param ticketRQ
	 * @return
	 */
	public String getDescription(Iterable<String> itemIds, PostTicketRQ ticketRQ) {
		if (null == itemIds) {
			return "";
		}
		TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
		MimeTypes mimeRepository = tikaConfig.getMimeRepository();
		StringBuilder descriptionBuilder = new StringBuilder();
		for (String itemId : itemIds) {
			List<Log> logs = logRepository.findByTestItemRef(itemId, ticketRQ.getNumberOfLogs(), ticketRQ.getIsIncludeScreenshots());
			if (null != ticketRQ.getBackLinks().get(itemId) && !ticketRQ.getBackLinks().get(itemId).isEmpty()) {
				descriptionBuilder.append(BACK_LINK_HEADER);
				descriptionBuilder.append("\n");
				descriptionBuilder.append(" - ");
				descriptionBuilder.append(String.format(BACK_LINK_PATTERN, ticketRQ.getBackLinks().get(itemId)));
				descriptionBuilder.append("\n");
			}
			// For single test-item only
			// TODO add multiple test-items backlinks
			if (ticketRQ.getIsIncludeComments() && (ticketRQ.getBackLinks().size() == 1)) {
				if (null != ticketRQ.getBackLinks().get(itemId) && !ticketRQ.getBackLinks().get(itemId).isEmpty()) {
					TestItem item = itemRepository.findOne(ticketRQ.getTestItemId());
					// If test-item contains any comments, then add it for JIRA
					// comments section
					if ((null != item.getIssue().getIssueDescription()) && (!item.getIssue().getIssueDescription().isEmpty())) {
						descriptionBuilder.append(COMMENTS_HEADER);
						descriptionBuilder.append("\n");
						descriptionBuilder.append(item.getIssue().getIssueDescription());
						descriptionBuilder.append("\n");
					}
				}
			}
			if (!logs.isEmpty() && (ticketRQ.getIsIncludeLogs() || ticketRQ.getIsIncludeScreenshots())) {
				descriptionBuilder.append("h3.*Test execution log:*\n");
				descriptionBuilder
						.append("{panel:title=Test execution log|borderStyle=solid|borderColor=#ccc|titleColor=#34302D|titleBGColor=#6DB33F}");
				for (Log log : logs) {
					if (ticketRQ.getIsIncludeLogs()) {
						descriptionBuilder.append(CODE).append(getFormattedMessage(log)).append(CODE);
					}
					if (log.getBinaryContent() != null && ticketRQ.getIsIncludeScreenshots()) {
						try {
							MimeType mimeType = mimeRepository.forName(log.getBinaryContent().getContentType());
							if (log.getBinaryContent().getContentType().contains(IMAGE_CONTENT)) {
								descriptionBuilder.append("!").append(log.getBinaryContent().getBinaryDataId())
										.append(mimeType.getExtension()).append(IMAGE_HEIGHT_TEMPLATE);
							} else {
								descriptionBuilder.append("[^").append(log.getBinaryContent().getBinaryDataId())
										.append(mimeType.getExtension()).append("]");
							}
							descriptionBuilder.append(JIRA_MARKUP_LINE_BREAK);
						} catch (MimeTypeException e) {
							descriptionBuilder.append(JIRA_MARKUP_LINE_BREAK);
							LOGGER.error("JIRATicketDescriptionService error: " + e.getMessage(), e);
						}

					}
				}
				descriptionBuilder.append("{panel}\n");
			}
		}
		return descriptionBuilder.toString();
	}

	private String getFormattedMessage(Log log) {
		StringBuilder messageBuilder = new StringBuilder();
		if (log.getLogTime() != null) {
			messageBuilder.append(" Time: ").append(dateFormat.format(log.getLogTime())).append(", ");
		}
		if (log.getLevel() != null) {
			messageBuilder.append("Level: ").append(log.getLevel()).append(", ");
		}
		messageBuilder.append("Log: ").append(log.getLogMsg()).append("\n");
		return messageBuilder.toString();
	}
}
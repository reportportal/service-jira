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

import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;

@Ignore
public class TicketDescriptionServiceTest {

	@Autowired
	private JIRATicketDescriptionService descriptionService;

	@Test
	public void testNull() {
		PostTicketRQ postTicketRQ = new PostTicketRQ();
		postTicketRQ.setNumberOfLogs(0);
		postTicketRQ.setIsIncludeScreenshots(false);
		HashMap<String, String> backLinks = new HashMap<>();
		backLinks.put("1234", "https://localhost:8443/reportportal-ws/");
		postTicketRQ.setBackLinks(backLinks);
		String result = descriptionService.getDescription(null, postTicketRQ);
		Assert.assertNotNull(result);
		Assert.assertEquals("", result);
		postTicketRQ.setIsIncludeLogs(false);
		String description = descriptionService.getDescription(Collections.singletonList("1234"), postTicketRQ);
		Assert.assertNotNull(description);
		//		Assert.assertEquals("h3.*Back link to Report Portal:*\n - [Link to defect|https://localhost:8443/reportportal-ws/]\r\n\n", description);
	}

	@Test
	public void testDescription() {
		PostTicketRQ postTicketRQ = new PostTicketRQ();
		postTicketRQ.setIsIncludeScreenshots(true);
		postTicketRQ.setNumberOfLogs(1);
		postTicketRQ.setIsIncludeLogs(true);
		postTicketRQ.setBackLinks(new HashMap<>());
		String description = descriptionService.getDescription(Collections.singletonList("44524cc1553de743b3e5aa2f"), postTicketRQ);
		Assert.assertNotNull(description);
		Assert.assertEquals(
				"h3.*Test execution log:*\n{panel:title=Test execution log|borderStyle=solid|borderColor=#ccc|titleColor=#34302D|titleBGColor=#6DB33F}{code} Time: 05/06/2013 18:26:00, Log: Demo Test Log Message_spdOP\n{code}{panel}\n",
				description);
	}

	@Test
	public void testDescriptionWithBackLink() {
		PostTicketRQ postTicketRQ = new PostTicketRQ();
		postTicketRQ.setIsIncludeScreenshots(true);
		postTicketRQ.setNumberOfLogs(1);
		postTicketRQ.setIsIncludeLogs(true);
		HashMap<String, String> backLinks = new HashMap<>();
		backLinks.put("44524cc1553de743b3e5aa2f", "https://localhost:8443/reportportal-ws/");
		postTicketRQ.setBackLinks(backLinks);
		String description = descriptionService.getDescription(Collections.singletonList("44524cc1553de743b3e5aa2f"), postTicketRQ);
		Assert.assertNotNull(description);
		//		 Assert.assertEquals("h3.*Back link to Report Portal:*\n - [Link to defect|https://localhost:8443/reportportal-ws/]\r\n\nh3.*Test execution log:*\n{panel:title=Test execution log|borderStyle=solid|borderColor=#ccc|titleColor=#34302D|titleBGColor=#6DB33F}{code} Time: 05/06/2013 18:26:00, Log: Demo Test Log Message_spdOP\n{code}{panel}\n",description);
		postTicketRQ.setIsIncludeLogs(false);
		postTicketRQ.setIsIncludeScreenshots(false);
		String descriptionWithoutLogs = descriptionService
				.getDescription(Collections.singletonList("44524cc1553de743b3e5aa2f"), postTicketRQ);
		Assert.assertNotNull(descriptionWithoutLogs);
		//		 Assert.assertEquals("h3.*Back link to Report Portal:*\n - [Link to defect|https://localhost:8443/reportportal-ws/]\r\n\n",descriptionWithoutLogs);
	}
}
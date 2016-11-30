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

import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.AuthType;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateExternalSystemRQ;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Created by andrei_varabyeu on 6/14/16.
 */
public class JiraOAuthController {

//	@Autowired
//	private OAuthClientService oauthJiraService;
//
//	@Autowired
//	private JiraOAuthSecurityProvider pairProvider;

	@Autowired
	private ProjectRepository projectRepository;

	/**
	 * Create {@link ExternalSystem} entry method via OAuth
	 *
	 * @param projectName   Project Name
	 * @param updateRQ      Request Data
	 * @param request       HTTP request
	 * @param response      HTTP response
	 * @param principalName Login
	 * @return Response Data
	 */
	@RequestMapping(value = "/oauthconnect", method = RequestMethod.POST, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public void externalSystemOAuthConnect(String projectName, UpdateExternalSystemRQ updateRQ, HttpServletRequest request,
			HttpServletResponse response, String principalName) {

		Project project = projectRepository.findByName(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		if (AuthType.OAUTH.name().equalsIgnoreCase(updateRQ.getExternalSystemAuth())) {
			try {
				String baseUrl = updateRQ.getUrl();
				String consumerKey = updateRQ.getAccessKey();
				// Start OAuth authentication process
				this.getRequestToken(projectName, consumerKey, baseUrl, request, response, principalName);
			} catch (Exception e) {
				throw new ReportPortalException("External system oauth connection exception", e);
			}
		} else {
			fail().withError(INCORRECT_REQUEST, "Authentication Type 'OAUTH' parameter missed or invalid!");
		}
	}

	private void getRequestToken(String projectName, String consumerId, String baseUrl, HttpServletRequest request,
			HttpServletResponse response, String username) throws Exception {
		String resource = "/api/v1/oauth/callback";
		String callback = getBaseUri(request.getRequestURL().toString()).concat(resource);
		// args: consumerKey, certPrivateKey, baseJiraUrl, callbackUrl
//		AtlassianOAuthClient client = new AtlassianOAuthClient(consumerId, pairProvider.extractPrivateKey(), baseUrl, callback);
//
//		JiraOAuthParams holder = client.getRequestToken();
//		holder.setConsumerKey(consumerId);
//		holder.setRpProject(projectName);
//		holder.setBaseUrl(baseUrl);
//		holder.setCallback(callback);
//		holder.setUsername(username);
//		// Referer for redirect back to report portal
//		holder.setReferer(request.getHeader(oauthJiraService.getBackHeader()));
//		oauthJiraService.storeOAuthDetails(username, holder);
//		client.redirect(request, response, client.getAuthorizeUrlForToken(holder.getRequestToken()));
	}

	private static String getBaseUri(String url) {
		return StringUtils.substringBefore(url, "/api/v");
	}
}
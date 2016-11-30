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
package com.epam.reportportal.extension.bugtracking.jira.oauth;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;

import java.time.LocalDate;

/**
 * Retryable Jira OAuth Token representation
 *
 * @author Andrei Varabyeu
 */
@Beta
public class JiraOAuthToken {

	//@Id
	private String id;

	private String externalSystemUrl;

	private String consumerKey;

	private String externalSystemType;

	private LocalDate issuedOn;

	private LocalDate expiresOn;

	private LocalDate expiresAuthorizationExpiresOn;

	private String user;

	private String accessToken;

	private String requestToken;

	private String requestTokenSecret;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExternalSystemUrl() {
		return externalSystemUrl;
	}

	public void setExternalSystemUrl(String externalSystemUrl) {
		this.externalSystemUrl = externalSystemUrl;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getExternalSystemType() {
		return externalSystemType;
	}

	public void setExternalSystemType(String externalSystemType) {
		this.externalSystemType = externalSystemType;
	}

	public LocalDate getIssuedOn() {
		return issuedOn;
	}

	public void setIssuedOn(LocalDate issuedOn) {
		this.issuedOn = issuedOn;
	}

	public LocalDate getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(LocalDate expiresOn) {
		this.expiresOn = expiresOn;
	}

	public LocalDate getExpiresAuthorizationExpiresOn() {
		return expiresAuthorizationExpiresOn;
	}

	public void setExpiresAuthorizationExpiresOn(LocalDate expiresAuthorizationExpiresOn) {
		this.expiresAuthorizationExpiresOn = expiresAuthorizationExpiresOn;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRequestToken() {
		return requestToken;
	}

	public void setRequestToken(String requestToken) {
		this.requestToken = requestToken;
	}

	public String getRequestTokenSecret() {
		return requestTokenSecret;
	}

	public void setRequestTokenSecret(String requestTokenSecret) {
		this.requestTokenSecret = requestTokenSecret;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", id).add("externalSystemUrl", externalSystemUrl).add("consumerKey", consumerKey)
				.add("externalSystemType", externalSystemType).add("issuedOn", issuedOn).add("expiresOn", expiresOn)
				.add("expiresAuthorizationExpiresOn", expiresAuthorizationExpiresOn).add("user", user).add("accessToken", accessToken)
				.add("requestToken", requestToken).add("requestTokenSecret", requestTokenSecret).toString();
	}
}
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

import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.epam.ta.reportportal.database.entity.OAuthSignature;
import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.oauth.*;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.http.HttpMessage;
import net.oauth.signature.RSA_SHA1;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;

import static net.oauth.OAuth.OAUTH_VERIFIER;

/**
 * @author Andrei Varabyeu
 */
@Beta
public class AtlassianOAuthClient {
	protected static final String SERVLET_BASE_URL = "/plugins/servlet";

	private final String consumerKey;
	private final String privateKey;
	private final String baseUrl;
	private final String callback;
	private OAuthAccessor accessor;

	public AtlassianOAuthClient(String consumerKey, String privateKey, String baseUrl, String callback) {
		this.consumerKey = consumerKey;
		this.privateKey = privateKey;
		this.baseUrl = baseUrl;
		this.callback = callback;
		this.accessor = buildAccessor();
	}

	public TokenSecretVerifierHolder getRequestToken() {
		try {
			OAuthClient oAuthClient = new OAuthClient(new HttpClient4());

			List<OAuth.Parameter> callBack;
			if (callback == null || "".equals(callback)) {
				callBack = Collections.emptyList();
			} else {
				callBack = ImmutableList.of(new OAuth.Parameter(OAuth.OAUTH_CALLBACK, callback));
			}

			OAuthMessage message = oAuthClient.getRequestTokenResponse(accessor, "POST", callBack);
			TokenSecretVerifierHolder tokenSecretVerifier = new TokenSecretVerifierHolder();
			tokenSecretVerifier.token = accessor.requestToken;
			tokenSecretVerifier.secret = accessor.tokenSecret;
			tokenSecretVerifier.verifier = message.getParameter(OAUTH_VERIFIER);
			return tokenSecretVerifier;
		} catch (Exception e) {
			throw new RuntimeException("Failed to obtain request token", e);
		}
	}

	public OAuthSignature obtainAccessToken(String requestToken, String tokenSecret) {
		try {
			OAuthClient client = new OAuthClient(new HttpClient4());
			accessor.requestToken = requestToken;
			accessor.tokenSecret = tokenSecret;

			OAuthMessage message = client.getAccessToken(accessor, "POST", Lists.newArrayList());

			OAuthSignature signature = new OAuthSignature();
			signature.setAccessToken(message.getToken());
			signature.setConsumerKey(this.consumerKey);
			signature.setExpiresOn(Date.from(Instant.ofEpochSecond(Long.parseLong(message.getParameter("oauth_expires_in")))));

			System.out.println(Date.from(Instant.ofEpochSecond(Long.parseLong(message.getParameter("oauth_authorization_expires_in")))));

			return signature;
		} catch (Exception e) {
			throw new RuntimeException("Failed to swap request token with access token", e);
		}
	}

	public AuthenticationHandler toJiraAuthHandler(String token) {
		return builder -> {
			accessor.accessToken = token;
			HttpMessage message = null;
			try {

				Request rq = builder.build();
				String method = Optional.ofNullable(rq.getMethod()).orElse(Request.Method.GET).name();
				message = HttpMessage
						.newRequest(accessor.newRequestMessage(method, rq.getUri().toString(), Collections.<Map.Entry<?, ?>>emptySet()),
								ParameterStyle.AUTHORIZATION_HEADER);
				message.headers.stream().forEach(h -> builder.setHeader(h.getKey(), h.getValue()));

			} catch (OAuthException | IOException | URISyntaxException e) {
				e.printStackTrace();
			}

		};
	}

	public final OAuthAccessor buildAccessor() {
		OAuthServiceProvider serviceProvider = new OAuthServiceProvider(getRequestTokenUrl(), getAuthorizeUrl(), getAccessTokenUrl());
		OAuthConsumer consumer = new OAuthConsumer(callback, consumerKey, null, serviceProvider);
		consumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey);
		consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);

		return new OAuthAccessor(consumer);

	}

	private String getAccessTokenUrl() {
		return baseUrl + SERVLET_BASE_URL + "/oauth/access-token";
	}

	private String getRequestTokenUrl() {
		return baseUrl + SERVLET_BASE_URL + "/oauth/request-token";
	}

	public String getAuthorizeUrlForToken(String token) {
		return getAuthorizeUrl() + "?oauth_token=" + token;
	}

	private String getAuthorizeUrl() {
		return baseUrl + SERVLET_BASE_URL + "/oauth/authorize";
	}
}
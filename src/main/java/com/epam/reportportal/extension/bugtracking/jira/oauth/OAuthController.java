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
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Andrei Varabyeu
 */
@Controller
@Beta
public class OAuthController {

	@RequestMapping(value = "/approve", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get registered external system instance")
	public void callback(HttpServletRequest rq) throws IOException {
		System.out.println(rq);
		rq.getParameterMap().forEach((k, v) -> System.out.println(k + ":" + v[0]));
	}
}
package com.appirio.tech.core.api.security.auth0;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class Auth0AuthenticationEntryPoint implements AuthenticationEntryPoint  {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
		PrintWriter writer = response.getWriter();
		
		if(authException instanceof Auth0AuthenticationException){
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			writer.println(getResponseContent(HttpServletResponse.SC_UNAUTHORIZED, authException));
		}else {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			writer.println(getResponseContent(HttpServletResponse.SC_FORBIDDEN, authException));
		}
	}
	
	private String getResponseContent(int responseStatus, AuthenticationException authException) {
		return "HTTP Status " + responseStatus + " - " + authException.getMessage();
	}
}
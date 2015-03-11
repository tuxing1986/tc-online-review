/**
 * 
 */
package com.appirio.tech.core.service.identity.ctrl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.appirio.tech.core.service.identity.view.LoginView;

/**
 * Temporary Login Resource for DW for secured Login Webflow of Auth0.
 * TODO: We should move this to pure RESTful if we can.
 * 
 * @author sudo
 *
 */
@Path("/login")
@Produces(MediaType.TEXT_HTML)
public class LoginWebflowCtrl {

	@GET
	public LoginView getLoginPage(
			@QueryParam("retUrl") String retUrl,
			@DefaultValue("false") @QueryParam("setParam") Boolean setParam,
			@DefaultValue("false") @QueryParam("debug") Boolean debug,	// a flag to debug the webflow
			@Context HttpServletRequest request) throws Exception {
		return new LoginView(retUrl, setParam, debug, request);
	}
}

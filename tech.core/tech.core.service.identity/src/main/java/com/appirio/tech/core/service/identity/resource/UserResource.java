package com.appirio.tech.core.service.identity.resource;

import static com.appirio.tech.core.service.identity.util.Constants.*;
import io.dropwizard.auth.Auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.api.v3.exception.APIRuntimeException;
import com.appirio.tech.core.api.v3.request.FieldSelector;
import com.appirio.tech.core.api.v3.request.PostPutRequest;
import com.appirio.tech.core.api.v3.request.QueryParameter;
import com.appirio.tech.core.api.v3.request.annotation.APIFieldParam;
import com.appirio.tech.core.api.v3.resource.DDLResource;
import com.appirio.tech.core.api.v3.resource.GetResource;
import com.appirio.tech.core.api.v3.response.ApiResponse;
import com.appirio.tech.core.api.v3.response.ApiResponseFactory;
import com.appirio.tech.core.auth.AuthUser;
import com.appirio.tech.core.service.identity.dao.UserDAO;
import com.appirio.tech.core.service.identity.representation.User;
import com.appirio.tech.core.service.identity.util.Utils;
import com.appirio.tech.core.service.identity.util.idgen.SequenceDAO;
import com.appirio.tech.core.service.identity.util.ldap.LDAPService;
import com.codahale.metrics.annotation.Timed;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource implements GetResource<User>, DDLResource {

	protected UserDAO userDao;
	
	protected SequenceDAO sequenceDao;
	
	protected LDAPService ldapService;
	
	public UserResource(
				UserDAO userDao,
				SequenceDAO sequenceDao) {
		this.userDao = userDao;
		this.sequenceDao = sequenceDao;
	}
	
	@Override
	@GET
	@Path("/{resourceId}")
	@Timed
	public ApiResponse getObject(
			@Auth AuthUser authUser,
			@PathParam("resourceId") TCID recordId,
			@APIFieldParam(repClass = User.class) FieldSelector selector,
			@Context HttpServletRequest request) throws Exception {
		
		User user = this.userDao.populateById(selector, recordId);
		return ApiResponseFactory.createFieldSelectorResponse(user, selector);
	}

	@Override
	@GET
	@Timed
	public ApiResponse getObjects(
			@Auth AuthUser authUser,
			@APIFieldParam(repClass = User.class) QueryParameter query,
			@Context HttpServletRequest request) throws Exception {
		//TODO:
		throw new APIRuntimeException(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}
	
	@Override
	@POST
	@Timed
	public ApiResponse createObject(
			@Auth(required=false) AuthUser authUser,
			@Valid PostPutRequest postRequest,
			@Context HttpServletRequest request) throws Exception {
		
		User user = (User)postRequest.getParamObject(User.class);
		String error = user.validate();
		if(error==null)
			error = validateHandle(user.getHandle());
		if(error==null)
			error = validateEmail(user.getEmail());
        if(error!=null) {
        	throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, error);
        }
        
		//TODO:
		user.setActive(true);
		userDao.register(user);
		
		return ApiResponseFactory.createResponse(user);
	}

	@Override
	@PUT
	@Path("/{resourceId}")
	@Timed
	public ApiResponse updateObject(
			@Auth AuthUser authUser,
			@PathParam("resourceId") String resourceId,
			@Valid PostPutRequest putRequest,
			@Context HttpServletRequest request) throws Exception {
		//TODO:
		throw new APIRuntimeException(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}

	@Override
	@DELETE
	@Path("/{resourceId}")
	@Timed
	public ApiResponse deleteObject(
			@Auth AuthUser authUser,
			@PathParam("resourceId") String resourceId,
			@Context HttpServletRequest request) throws Exception {
		//TODO:
		throw new APIRuntimeException(HttpServletResponse.SC_NOT_IMPLEMENTED);
	}

    protected String validateHandle(String handle) {
    	if (this.userDao==null)
    		throw new IllegalArgumentException("userDao is not specified.");
    	if (handle==null || handle.length()==0)
    		throw new IllegalArgumentException("handle must be specified.");
    	
        if (userDao.handleExists(handle))
           return String.format(MSG_TEMPLATE_DUPLICATED_HANDLE, handle);
        return null;
    }	

    protected String validateEmail(String email) {
    	if (this.userDao==null)
    		throw new IllegalArgumentException("userDao is not specified.");
    	if (email==null || email.length()==0)
    		throw new IllegalArgumentException("email must be specified.");
    	
        User user = userDao.findUserByEmail(email);
        if (user != null)
            return String.format(MSG_TEMPLATE_DUPLICATED_EMAIL, email);
        return null;
    }
}

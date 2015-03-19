package com.appirio.tech.core.service.identity.resource;

import io.dropwizard.auth.Auth;

import java.util.LinkedList;
import java.util.List;

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
public class UserResource implements GetResource<User>, DDLResource<User> {

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
			@Valid PostPutRequest<User> postRequest,
			@Context HttpServletRequest request) throws Exception {
		
		User user = postRequest.getParam();
		List<String> messages = new LinkedList<String>();
        validateHandle(user.getHandle(), messages, userDao);
        validateEmail(user.getEmail(), messages, userDao);
        validatePassword(user.getCredential()!=null ? user.getCredential().getPassword() : "", messages);
		/*
		// TODO: validate user
            validateFirstName();
            validateLastName();
            validateCountry();
            validateVerificationCode();
		 */
        if(messages.size()>0) {
        	throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, messages.get(0));
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
			@Valid PostPutRequest<User> putRequest,
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

	
    private void validateHandle(String handle, List<String> messages, UserDAO userDao) {
    	if(handle == null || handle.length()==0) {
            messages.add("Handle is requried");
            return;
    	}
        // Check if the handle is invalid.
        String result = Utils.validateHandle(handle);
        if (null != result) {
            messages.add(result);
        } else {
            if (userDao.handleExists(handle)) {
                messages.add("Handle '" + handle + "' has already been taken");
            }
        }
    }	

    private void validateEmail(String email, List<String> messages, UserDAO userDao) {
        // validate email.
        if (email==null || email.length()==0) {
            messages.add("Email is required");
            return;
        }
        String result = Utils.validateEmail(email);
        if (null != result) {
            messages.add(result);
            return;
        }
        User user = userDao.findUserByEmail(email);
        if (user != null) {
            messages.add("The email - '" + email + "' is already registered, please use another one.");
        }
    }
    
    private void validatePassword(String password, List<String> messages){
    	if (password==null || password.length()==0) {
            messages.add("Password is required");
            return;
        }
    	String result = Utils.validatePassword(password);
        if (null != result) {
            messages.add(result);
            return;
        }
        return;
    }
}

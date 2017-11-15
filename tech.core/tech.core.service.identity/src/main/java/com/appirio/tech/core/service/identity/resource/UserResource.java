package com.appirio.tech.core.service.identity.resource;

import static com.appirio.tech.core.service.identity.util.Constants.*;
import static javax.servlet.http.HttpServletResponse.*;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.PATCH;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.appirio.eventsbus.api.client.EventProducer;
import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.api.v3.exception.APIRuntimeException;
import com.appirio.tech.core.api.v3.request.FieldSelector;
import com.appirio.tech.core.api.v3.request.PostPutRequest;
import com.appirio.tech.core.api.v3.request.QueryParameter;
import com.appirio.tech.core.api.v3.request.annotation.APIFieldParam;
import com.appirio.tech.core.api.v3.request.annotation.APIQueryParam;
import com.appirio.tech.core.api.v3.resource.DDLResource;
import com.appirio.tech.core.api.v3.resource.GetResource;
import com.appirio.tech.core.api.v3.response.ApiResponse;
import com.appirio.tech.core.api.v3.response.ApiResponseFactory;
import com.appirio.tech.core.api.v3.util.jwt.JWTToken;
import com.appirio.tech.core.auth.AuthUser;
import com.appirio.tech.core.service.identity.dao.GroupDAO;
import com.appirio.tech.core.service.identity.dao.RoleDAO;
import com.appirio.tech.core.service.identity.dao.SSOUserDAO;
import com.appirio.tech.core.service.identity.dao.UserDAO;
import com.appirio.tech.core.service.identity.representation.Achievement;
import com.appirio.tech.core.service.identity.representation.Country;
import com.appirio.tech.core.service.identity.representation.Credential;
import com.appirio.tech.core.service.identity.representation.Email;
import com.appirio.tech.core.service.identity.representation.GroupMembership;
import com.appirio.tech.core.service.identity.representation.ProviderType;
import com.appirio.tech.core.service.identity.representation.Role;
import com.appirio.tech.core.service.identity.representation.User;
import com.appirio.tech.core.service.identity.representation.UserProfile;
import com.appirio.tech.core.service.identity.util.Constants;
import com.appirio.tech.core.service.identity.util.Utils;
import com.appirio.tech.core.service.identity.util.auth.Auth0Client;
import com.appirio.tech.core.service.identity.util.auth.OneTimeToken;
import com.appirio.tech.core.service.identity.util.cache.CacheService;
import com.appirio.tech.core.service.identity.util.event.MailRepresentation;
import com.appirio.tech.core.service.identity.util.event.NotificationPayload;
import com.appirio.tech.core.service.identity.util.ldap.MemberStatus;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * UserResource provides the user endpoints to manage the users.
 * 
 * <p>
 * Changes in the version 1.1 72h TC Identity Service API Enhancements v1.0
 * - add createOrUpdateSSOUserLogin method
 * - getObject will return an user with ssoLogin flag and providers info stored in the profiles fields
 * </p>
 * 
 * @author TCCoder
 * @version 1.1
 *
 */
@Path("users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource implements GetResource<User>, DDLResource<User> {
    // TODO: switch to slf4j directly (this delegates to it) - it's more efficient
    private static final Logger logger = Logger.getLogger(UserResource.class);
    
    private int resetTokenExpirySeconds = 30 * 60; //30min
    
    private int resendActivationCodeExpirySeconds = 30 * 60; //30min

    private int oneTimeTokenExpirySeconds = 10 * 60; //10min

    private String domain;
    
    protected UserDAO userDao;
    
    private final RoleDAO roleDao;
    
    // TODO: temporary fix
    private GroupDAO groupDao;
    public void setGroupDAO(GroupDAO groupDao) { this.groupDao = groupDao; }

    protected CacheService cacheService;
    
    private Auth0Client auth0;

    private EventProducer eventProducer;

    private ObjectMapper objectMapper = new ObjectMapper();
    
    private Long defaultUserRoleId;
    
    private String secret;
    
    
    public UserResource(
                UserDAO userDao,
                RoleDAO roleDao,
                CacheService cacheService,
                EventProducer eventProducer) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.cacheService = cacheService;
        this.eventProducer = eventProducer;
    }

    protected void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public void setAuth0Client(Auth0Client auth0) {
        this.auth0 = auth0;
    }
    
    /**
     * Create or update sso user login
     *
     * @param authUser the authUser to use
     * @param userId the userId to use
     * @param postRequest the postRequest to use
     * @throws Exception if any error occurs
     * @return the ApiResponse result
     */
    @POST
    @Timed
    @Path("/{userId}/createOrUpdateSSOUserLogin")
    public ApiResponse createOrUpdateSSOUserLogin(@Auth AuthUser authUser,
            @PathParam("userId") long userId,
            @Valid PostPutRequest<UserProfile> postRequest) throws Exception {
        if(!hasAdminRole(authUser)) {
            throw new APIRuntimeException(SC_FORBIDDEN, "Forbidden");
        }
        if (userId <= 0) {
            throw new APIRuntimeException(SC_BAD_REQUEST, "userId should be positive:" + userId);
        }
        
        UserProfile profile = postRequest.getParam();
        if (profile == null) {
            throw new APIRuntimeException(SC_BAD_REQUEST, "profile must be specified.");
        }
        if (profile.getProvider() == null) {
            throw new APIRuntimeException(SC_BAD_REQUEST, "profile must have provider.");
        }
        if (profile.getUserId() == null) {
            throw new APIRuntimeException(SC_BAD_REQUEST, "profile must have sso user id.");
        }
        try {
            SSOUserDAO ssoUserDao = this.userDao.createSSOUserDAO();
            Long providerId = ssoUserDao.getSSOProviderIdByName(profile.getProvider());
            if (providerId == null) {
                throw new APIRuntimeException(SC_BAD_REQUEST,
                        "The provider id not found for the provider:" + profile.getProvider());
            }
            int count = ssoUserDao.checkUserIdAndProviderId(userId, providerId);
            if (count > 0) {
                ssoUserDao.updateSSOUser(userId, profile);
            } else {
                ssoUserDao.createSSOUser(userId, profile);
            }
        } catch (APIRuntimeException are) {
            throw are;
        } catch (Exception exp) {
            throw new APIRuntimeException(SC_INTERNAL_SERVER_ERROR, exp);
        }
        
        return ApiResponseFactory.createResponse(profile);
    }

    @Override
    @GET
    @Timed
    public ApiResponse getObjects(
            @Auth AuthUser authUser,
            @APIQueryParam(repClass = User.class) QueryParameter query,
            @Context HttpServletRequest request) throws Exception {

        logger.info("getObjects");

        if(!hasAdminRole(authUser)) {
            throw new APIRuntimeException(SC_FORBIDDEN, "Forbidden");
        }

        try {
            List<User> users = userDao.findUsers(
                    query.getFilter(), query.getOrderByQuery().getItems(), query.getLimitQuery());
            return ApiResponseFactory.createFieldSelectorResponse(users, query.getSelector());
        } catch (IllegalArgumentException e) {
            throw new APIRuntimeException(SC_BAD_REQUEST, e.getMessage());
        }
    }
    
    /**
     * Get user object
     * @param authUser the authUser to use
     * @param recordId the recordId to use
     * @param selector the selector to use
     * @param request the request to use
     * @throws Exception if any error occurs
     * @return the ApiResponse result
     */
    @Override
    @GET
    @Path("/{resourceId}")
    @Timed
    public ApiResponse getObject(
            @Auth AuthUser authUser,
            @PathParam("resourceId") TCID recordId,
            @APIFieldParam(repClass = User.class) FieldSelector selector,
            @Context HttpServletRequest request) throws Exception {
        if (recordId == null) {
            throw new APIRuntimeException(SC_BAD_REQUEST, "resourceId is required.");
        }

        // checking ID
        checkResourceId(recordId);
        // checking permission
        checkAdminPermission(authUser, recordId);

        User user = this.userDao.populateById(selector, recordId);
        if (user == null) {
            throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_USER_NOT_FOUND);
        }

        return ApiResponseFactory.createFieldSelectorResponse(user, selector);
    }
    
    @Override
    public ApiResponse createObject(
            AuthUser authUser,
            @Valid PostPutRequest<User> postRequest,
            @Context HttpServletRequest request) throws Exception {
        throw new APIRuntimeException(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
    
    @POST
    @Timed
    public ApiResponse createObject(
            @Valid PostPutRequest<User> postRequest,
            @Context HttpServletRequest request) throws Exception {

        logger.info("createObject");

        checkParam(postRequest);

        User user = postRequest.getParam();

        // The user should have UserProfile when registering with it's social account.
        // And password is set to the default value.
        if(user.getProfile()!=null &&
                (user.getCredential()==null || user.getCredential().getPassword()==null || user.getCredential().getPassword().length()==0)) {
            if(user.getCredential()==null)
                user.setCredential(new Credential());
            user.getCredential().setPassword(Utils.getString("defaultPassword", "default-password"));
        }
        String error = user.validate();
        if (error == null)
            error = validateHandle(user.getHandle());
        if (error == null)
            error = validateEmail(user.getEmail());
        if (error == null && user.getCountry()!=null)
            error = validateCountry(user.getCountry());
        if (error == null && user.getProfile()!=null)
            error = validateProfile(user.getProfile());
        if (error == null && user.isReferralProgramCampaign())
            error = validateReferral(user.getUtmSource());
        if (error != null) {
            throw new APIRuntimeException(SC_BAD_REQUEST, error);
        }

        // default status: inactive
        if(!user.isActive()) {
            user.setActive(false);
        }

        if(user.getProfile()!=null) {
            // get access_token of IdP from Auth0.
            setAccessToken(user.getProfile());
        }
        
        // register user
        logger.debug(String.format("registering user: %s", user.getHandle()));
        userDao.register(user);
        
        // TODO: This is a temporary fix for the urgent issue. This should be fixed. 
        if(user.getProfile()!=null && "wipro-adfs".equalsIgnoreCase(user.getProfile().getProvider()) ) {
        	logger.info(String.format("Adding Wipro user to the Wipro-All group. (%s, %s, %s)", user.getId(), user.getHandle(), user.getProfile().getUserId() ));
            long groupWiproAll = 20000000L; // Authorization.group in MySQL
            GroupMembership membership = new GroupMembership();
            membership.setMemberId(Utils.toLongValue(user.getId()));
            membership.setMembershipType("user");
            membership.setGroupId(groupWiproAll);
            membership.setCreatedBy(user.getId());
            membership.setCreatedAt(DateTime.now());
            try {
                groupDao.addMembership(membership);
            } catch(Exception e) {
                logger.error(String.format("Failed to add user %s(%s) to Wipro group.", user.getId(), user.getHandle()), e); 
            }
        }
        
        // registration mail with activation code for inactive user
        if(!user.isActive()) {
            String redirectUrl = postRequest.getOptionString("afterActivationURL");
            logger.debug(String.format("sending registeration mail to: %s (%s)", user.getEmail(), user.getHandle()));
            // publish event.
            notifyActivation(user, redirectUrl);
        } else {
            assignDefaultUserRole(user); // Add Topcoder User role if the user was auto-activated
        }

        // publish event
        publishUserEvent("event.user.created", user);

        return ApiResponseFactory.createResponse(user);
    }
    
    @Override
    @PATCH
    @Path("/{resourceId}")
    @Timed
    public ApiResponse updateObject(
            @Auth AuthUser authUser,
            @PathParam("resourceId") String resourceId,
            @Valid PostPutRequest<User> patchRequest,
            @Context HttpServletRequest request) throws Exception {

        logger.info(String.format("updateObject userId: %s", resourceId));

        TCID id = new TCID(resourceId);
        // checking ID
        checkResourceId(id);
        // checking permission
        checkAdminPermission(authUser, id);
        // checking param
        checkParam(patchRequest);

        User user = patchRequest.getParam();
        user.setId(id);
        
        Long userId = Utils.toLongValue(id);
        logger.debug(String.format("findUserById(%s)", userId));
        User userInDB = userDao.findUserById(userId);
        if(userInDB==null)
            throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_USER_NOT_FOUND);

        // Can't update handle, email, isActive
        if(user.getHandle()!=null && !user.getHandle().equals(userInDB.getHandle()))
            throw new APIRuntimeException(SC_BAD_REQUEST, "Handle can't be updated");
        if(user.getEmail()!=null && !user.getEmail().equals(userInDB.getEmail()))
            throw new APIRuntimeException(SC_BAD_REQUEST, "Email address can't be updated");
        if(user.getStatus()!=null && !user.getStatus().equals(userInDB.getStatus()))
            throw new APIRuntimeException(SC_BAD_REQUEST, "Status can't be updated");
        
        // validate new values
        String error = null;
        if(user.getFirstName()!=null && !user.getFirstName().equals(userInDB.getFirstName()))
            error = user.validateFirstName();
        if(error==null && user.getLastName()!=null && !user.getLastName().equals(userInDB.getLastName()))
            error = user.validateLastName();
        
        Credential cred = user.getCredential();
        if(error==null && cred!=null && cred.getPassword()!=null)
            error = user.validatePassoword();
        
        if(error != null) {
            throw new APIRuntimeException(SC_BAD_REQUEST, error);
        }
        
        // validate password if it's specified.
        if(cred!=null && cred.getPassword()!=null) {
            // currentPassword is required and must match with registered password
            if(cred.getCurrentPassword()==null)
                throw new APIRuntimeException(SC_BAD_REQUEST, String.format(Constants.MSG_TEMPLATE_MANDATORY, "Current password"));
            if(!userInDB.getCredential().isCurrentPassword(cred.getCurrentPassword()))
                throw new APIRuntimeException(SC_BAD_REQUEST, Constants.MSG_TEMPLATE_INVALID_CURRENT_PASSWORD);
        }

        // update user
        logger.debug(String.format("updating user: %s", userInDB.getHandle()));
        if(user.getFirstName()!=null)
            userInDB.setFirstName(user.getFirstName());
        if(user.getLastName()!=null)
            userInDB.setLastName(user.getLastName());
        if(user.getRegSource()!=null)
            userInDB.setRegSource(user.getRegSource());
        if(user.getUtmSource()!=null)
            userInDB.setUtmSource(user.getUtmSource());
        if(user.getUtmMedium()!=null)
            userInDB.setUtmMedium(user.getUtmMedium());
        if(user.getUtmCampaign()!=null)
            userInDB.setUtmCampaign(user.getUtmCampaign());
        userDao.update(userInDB);

        // update password
        if(cred!=null && cred.getPassword()!=null) {
            logger.debug(String.format("updating password: %s", userInDB.getHandle()));
            userInDB.getCredential().setPassword(cred.getPassword());
            userDao.updatePassword(userInDB);
        }

        return ApiResponseFactory.createResponse(userInDB);
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
        throw new APIRuntimeException(SC_NOT_IMPLEMENTED);
    }
    
    @POST
    @Path("/{resourceId}/profiles")
    @Timed
    public ApiResponse createUserProfile(
            @Auth AuthUser authUser,
            @PathParam("resourceId") String resourceId,
            @Valid PostPutRequest<UserProfile> postRequest,
            @Context HttpServletRequest request) throws Exception {

        logger.info(String.format("createUserProfile(%s)", resourceId));

        TCID id = new TCID(resourceId);
        // checking ID
        checkResourceId(id);
        // checking permission
        checkAdminPermission(authUser, id);
        // checking param
        checkParam(postRequest);
        
        UserProfile profile = postRequest.getParam();

        Long userId = Utils.toLongValue(id);
        String error = validateProfile(userId, profile);
        if (error != null)
            throw new APIRuntimeException(SC_BAD_REQUEST, error);
        
        logger.info(String.format("findUserById(%s)", resourceId));
        User user = userDao.findUserById(userId);
        if(user==null)
            throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_USER_NOT_FOUND);
        
        // get access_token of IdP from Auth0.
        setAccessToken(profile);
        
        logger.info(String.format("addSocialProfile(%s, {%s, %s})", resourceId, profile.getUserId(), profile.getProviderType()));
        userDao.addSocialProfile(userId, profile);
        
        return ApiResponseFactory.createResponse(profile);
    }
    
    protected void setAccessToken(UserProfile profile) {
        if(profile==null)
            throw new IllegalArgumentException("profile must be specified.");
        
        String auth0UserId = profile.getContext()!=null ? profile.getContext().get("auth0UserId") : null;
        if(auth0UserId==null) {
            auth0UserId = profile.getProviderType() + "|" + profile.getUserId();
            logger.warn("Profile does not have 'auth0UserId' in its context. It has been built with 'providerType' and 'userId': "+auth0UserId);
        }
        logger.info(String.format("setAccessToken(%s)", auth0UserId));
        try {
            String accessToken = auth0.getIdProviderAccessToken(auth0UserId);
            if(accessToken!=null) {
                if(profile.getContext()==null) {
                    profile.setContext(new HashMap<String, String>());
                }
                profile.getContext().put("accessToken", accessToken);
            }
        } catch (Exception e) {
            logger.error("Failed to obtain the access token for "+auth0UserId, e);
        }        
    }
    
    @DELETE
    @Path("/{resourceId}/profiles/{provider}")
    @Timed
    public ApiResponse deleteUserProfile(
            @Auth AuthUser authUser,
            @PathParam("resourceId") String resourceId,
            @PathParam("provider") String provider,
            @Context HttpServletRequest request) throws Exception {

        logger.info(String.format("deleteUserProfile(%s, %s)", resourceId, provider));
        
        if(resourceId==null)
            throw new APIRuntimeException(SC_BAD_REQUEST, String.format(Constants.MSG_TEMPLATE_MANDATORY, "resourceId"));
        if(provider==null)
            throw new APIRuntimeException(SC_BAD_REQUEST, String.format(Constants.MSG_TEMPLATE_MANDATORY, "provider"));
        
        TCID id = new TCID(resourceId);
        // checking ID
        checkResourceId(id);
        // checking permission
        checkAdminPermission(authUser, id);
        
        ProviderType providerType = ProviderType.getByName(provider);
        if(providerType==null)
            throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, String.format(MSG_TEMPLATE_UNSUPPORTED_PROVIDER, provider));
        
        List<UserProfile> profiles = null;
        if(providerType.isSocial) {
            Long userId = Utils.toLongValue(id);
            profiles = userDao.getSocialProfiles(userId, providerType);
            if(profiles==null || profiles.size()==0)
                throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_SOCIAL_PROFILE_NOT_FOUND);
            userDao.deleteSocialProfiles(userId, providerType);
        }
        else if(providerType.isEnterprise) {
            // TODO
            throw new APIRuntimeException(HttpServletResponse.SC_NOT_IMPLEMENTED, String.format(MSG_TEMPLATE_UNSUPPORTED_PROVIDER, provider));
        }
        
        return ApiResponseFactory.createResponse(profiles);
    }
    
    /**
     * API to authenticate users with email and password.
     * This is supposed to be called from Auth0 custom connection.
     * @param email
     * @param password
     * @param request
     * @return
     * @throws Exception
     */
    @POST
    @Path("/login")
    @Consumes("application/x-www-form-urlencoded")
    @Timed
    public ApiResponse login(
            @FormParam("handleOrEmail") String handleOrEmail,
            @FormParam("password") String password,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("login(%s, [PASSWORD])", handleOrEmail));
        if(Utils.isEmpty(handleOrEmail))
            throw new APIRuntimeException(SC_BAD_REQUEST, String.format(MSG_TEMPLATE_MANDATORY, "Handle or Email"));
        if(Utils.isEmpty(password))
            throw new APIRuntimeException(SC_BAD_REQUEST, String.format(MSG_TEMPLATE_MANDATORY, "Password"));

        logger.debug(String.format("authenticating user by '%s'", handleOrEmail));
        User user = userDao.authenticate(handleOrEmail, password);
        if(user==null) {
            throw new APIRuntimeException(SC_UNAUTHORIZED, "Credentials are incorrect.");
        }
        
        return ApiResponseFactory.createResponse(user);
    }
    
    //TODO: should be PATCH?
    @PUT
    @Path("/activate")
    @Timed
    public ApiResponse activateUser(
            @QueryParam("code") String code,
            @Context HttpServletRequest request) throws Exception {

        logger.info(String.format("activateUser(%s)", code));

        int userId = Utils.getCoderId(code);
        if(userId==0)
            throw new APIRuntimeException(SC_BAD_REQUEST, MSG_TEMPLATE_INVALID_ACTIVATION_CODE);
        logger.debug(String.format("user id: %s from %s", userId, code));
        
        logger.debug(String.format("findUserById(%s)", userId));
        User user = userDao.findUserById(userId);
        if(user==null ||
            (user.getCredential()!=null && !code.equals(user.getCredential().getActivationCode()))) {
            throw new APIRuntimeException(SC_BAD_REQUEST, MSG_TEMPLATE_INVALID_ACTIVATION_CODE);
        }
        logger.debug(String.format("findUserById(%s): %s", userId, user.getHandle()));
        if(user.isActive())
            throw new APIRuntimeException(SC_BAD_REQUEST, MSG_TEMPLATE_USER_ALREADY_ACTIVATED);
        
        logger.debug(String.format("activating user: %s", user.getHandle()));
        userDao.activate(user);

        // publish event
        publishUserEvent("event.user.activated", user);
        
        // Fix for https://app.asana.com/0/152805928309317/156708836631075
        // The current Welcome mail should not be sent to customers (connect users). 
        String source = request.getParameter("source");
        if(!"connect".equalsIgnoreCase(source)) {
            notifyWelcome(user);
        }
        
        // assign a default user role
        assignDefaultUserRole(user);

        return ApiResponseFactory.createResponse(user);
    }

    @POST
    @Path("/{resourceId}/sendActivationCode")
    @Timed
    public ApiResponse sendActivationCode(
            @PathParam("resourceId") String resourceId,
            @Valid PostPutRequest<User> postRequest,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("sendActivationCode(%s)", resourceId));

        TCID id = new TCID(resourceId);
        // checking ID
        checkResourceId(id);

        // find user by resourceId
        User user = userDao.findUserById(Utils.toLongValue(id));

        // return 404 if user is not found
        if(user==null)
            throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_USER_NOT_FOUND);
        
        // return 400 if user has been activated
        if(user.isActive())
            throw new APIRuntimeException(SC_BAD_REQUEST, MSG_TEMPLATE_USER_ALREADY_ACTIVATED);
        
        // check cache with email
        String cacheKey = getCacheKeyForActivationCode(user.getId(), user.getEmail());
        String code = cacheService.get(cacheKey);
        logger.debug(String.format("cache[%s] -> %s", cacheKey, code));
        
        // return 400 if code!=null
        if(code!=null) {
            // TODO: MSG
            throw new APIRuntimeException(HttpURLConnection.HTTP_BAD_REQUEST, "You have already requested the activation code. Please find it in your email inbox. If it's not there, please contact support@topcoder.com.");
        }
        logger.debug(String.format("cache[%s] <- %s", cacheKey, user.getCredential().getActivationCode()));
        cacheService.put(cacheKey, user.getCredential().getActivationCode(), getResendActivationCodeExpirySeconds());
        
        // registration mail with activation code for inactive user
        String redirectUrl = postRequest.getOptionString("afterActivationURL");
        logger.debug(String.format("sending registeration mail to: %s (%s)", user.getEmail(), user.getHandle()));
        // publish event
        notifyActivation(user, redirectUrl);
        
        return ApiResponseFactory.createResponse("Activation mail has been sent successfully.");
    }
    
    @PATCH
    @Path("/{resourceId}/handle")
    @Timed
    public ApiResponse updateHandle(
            @Auth AuthUser authUser,
            @PathParam("resourceId") String resourceId,
            @Valid PostPutRequest<User> patchRequest,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("updateHandle(%s)", resourceId));

        TCID id = new TCID(resourceId);
        // checking ID
        checkResourceId(id);
        // checking permission
        checkAdminPermission(authUser, id);
        // checking param
        checkParam(patchRequest);

        User user = patchRequest.getParam();
        String error = user.validateHandle();
        if(error == null) {
            error = validateHandle(user.getHandle());
        }
        if(error != null) {
            throw new APIRuntimeException(SC_BAD_REQUEST, error);
        }

        Long userId = Utils.toLongValue(id);
        logger.debug(String.format("findUserById(%s)", userId));
        User userInDB = userDao.findUserById(userId);
        if(userInDB==null) {
            throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_USER_NOT_FOUND);
        }
        String oldHandle = userInDB.getHandle();
        if(oldHandle.equals(user.getHandle())) {
            return ApiResponseFactory.createResponse(userInDB);
        }
        userInDB.setHandle(user.getHandle());
        userInDB.setModifiedBy(authUser.getUserId());
        
        logger.debug(String.format("updateHandle(%s, %s)", resourceId, user.getHandle()));
        userDao.updateHandle(userInDB);
        
        publishUserEvent("event.user.updated", userInDB);
        
        return ApiResponseFactory.createResponse(userInDB);
    }

    @PATCH
    @Path("/{resourceId}/email")
    @Timed
    public ApiResponse updatePrimaryEmail(
            @Auth AuthUser authUser,
            @PathParam("resourceId") String resourceId,
            @Valid PostPutRequest<User> patchRequest,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("updatePrimaryEmail(%s)", resourceId));

        TCID id = new TCID(resourceId);
        // checking ID
        checkResourceId(id);
        // checking permission
        checkAdminPermission(authUser, id);
        // checking param
        checkParam(patchRequest);

        User user = patchRequest.getParam();        
        String error = user.validateEmail();
        if(error == null) {
            error = validateEmail(user.getEmail());
        }
        if(error != null) {
            throw new APIRuntimeException(SC_BAD_REQUEST, error);
        }

        Long userId = Utils.toLongValue(id);
        logger.debug(String.format("findUserById(%s)", userId));
        User userInDB = userDao.findUserById(userId);
        if(userInDB==null) {
            throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_USER_NOT_FOUND);
        }
        String oldEmail = userInDB.getEmail();
        if(oldEmail.equals(user.getEmail())) {
            return ApiResponseFactory.createResponse(userInDB);
        }
        userInDB.setEmail(user.getEmail());
        userInDB.setModifiedBy(authUser.getUserId());
        
        logger.debug(String.format("updatePrimaryEmail(%s, %s)", resourceId, user.getEmail()));
        Email email = userDao.updatePrimaryEmail(userInDB);
        if(email==null) {
            throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_PRIMARY_EMAIL_NOT_FOUND);
        }
        
        publishUserEvent("event.user.updated", userInDB);
        
        return ApiResponseFactory.createResponse(userInDB);
    }

    /**
     * This endpoint is used to update email of a specified user (only) in the registration flow.
     * A bearer token is needed in Authorization header, which is created by getOneTimeToken().   
     * @param resourceId User ID
     * @param email    New email address
     * @param request
     * @return
     * @throws Exception
     */
    @POST
    @Path("/{resourceId}/email/{email}")
    @Timed
    public ApiResponse updateEmailWithOneTimeToken(
            @PathParam("resourceId") String resourceId,
            @PathParam("email") String email,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("updateEmailWithOneTimeToken(%s)", resourceId));
        
        String token = Utils.extractBearer(request);
        if(token==null)
            throw new APIRuntimeException(SC_UNAUTHORIZED, "Valid credentials are required.");

        OneTimeToken onetimeToken;
        try {
            onetimeToken = createOneTimeToken(token);
        } catch (Exception e) {
            throw new APIRuntimeException(SC_UNAUTHORIZED, e.getMessage());
        }
        AuthUser authUser = onetimeToken.getAuthUser();
        
        String cache = cacheService.get(getCacheKeyForOneTimeToken(authUser.getUserId()));
        if(cache==null)
            throw new APIRuntimeException(SC_UNAUTHORIZED, "Token is expired.");
        
        PostPutRequest<User> postRequest = new PostPutRequest<User>();
        User user = new User();
        user.setId(authUser.getUserId());
        user.setEmail(email);
        postRequest.setParam(user);
        
        try {
            return updatePrimaryEmail(authUser, resourceId, postRequest, request);
        } finally {
            try { cacheService.delete(getCacheKeyForOneTimeToken(user.getId())); } catch(Exception e){}
        }
    }

    protected OneTimeToken createOneTimeToken(String token) {
        return new OneTimeToken(token, getDomain(), getSecret());
    }
    
    @PATCH
    @Path("/{resourceId}/status")
    @Timed
    public ApiResponse updateStatus(
            @Auth AuthUser authUser,
            @PathParam("resourceId") String resourceId,
            @Valid PostPutRequest<User> patchRequest,
            @QueryParam("comment") String comment,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("updateStatus(%s, %s)", resourceId, comment));

        TCID id = new TCID(resourceId);
        // checking ID
        checkResourceId(id);
        // checking permission
        checkAdminPermission(authUser, id);
        // checking param
        checkParam(patchRequest);
        
        User user = patchRequest.getParam();
        
        if(!isValidStatusValue(user.getStatus())) {
            throw new APIRuntimeException(SC_BAD_REQUEST, Constants.MSG_TEMPLATE_INVALID_STATUS);
        }
        Long userId = Utils.toLongValue(id);
        logger.debug(String.format("findUserById(%s)", userId));
        User userInDB = userDao.findUserById(userId);
        if(userInDB==null) {
            throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_USER_NOT_FOUND);
        }
        String oldStatus = userInDB.getStatus();
        userInDB.setStatus(user.getStatus());
        userInDB.setModifiedBy(authUser.getUserId());

        logger.debug(String.format("updateStatus(%s, %s, %s)", resourceId, user.getStatus(), comment));
        userDao.updateStatus(userInDB, comment);
        
        // Fire an event to notify the user is updated
        if(!user.getStatus().equals(oldStatus)) {
            publishUserEvent(getTopicForUpdatedStatus(user.getStatus()), userInDB);
        }
        // Fire an event to send the welcome mail
        if(MemberStatus.UNVERIFIED == MemberStatus.getByValue(oldStatus) &&
            MemberStatus.ACTIVE == MemberStatus.getByValue(user.getStatus())) {
            notifyWelcome(userInDB);
        }
        
        return ApiResponseFactory.createResponse(userInDB);
    }
    
    @SuppressWarnings("rawtypes")
    protected void checkParam(PostPutRequest request) {
        if(request==null || request.getParam()==null)
            throw new APIRuntimeException(SC_BAD_REQUEST, "The request does not contain param data.");
    }

    //TODO: should be PATCH?
    @PUT
    @Path("/resetPassword")
    @Timed
    public ApiResponse resetPassword(
            @Valid PostPutRequest<User> postRequest,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("resetPassword"));

        // checking param
        checkParam(postRequest);
        
        User user = postRequest.getParam();
        String error = user.validatePassoword();
        if (error != null) {
            throw new APIRuntimeException(SC_BAD_REQUEST, error);
        }
        String token = user.getCredential()!=null ? user.getCredential().getResetToken() : null;
        if(token==null || token.length()==0) {
            throw new APIRuntimeException(SC_BAD_REQUEST, String.format(MSG_TEMPLATE_MANDATORY, "Token"));
        }
        User dbUser = null;
        if(user.getHandle()!=null) {
            logger.debug(String.format("findUserByHandle(%s)", user.getHandle()));
            dbUser = this.userDao.findUserByHandle(user.getHandle());
        }
        if(dbUser==null && user.getEmail()!=null) {
            logger.debug(String.format("findUserByEmail(%s)", user.getEmail()));
            dbUser = this.userDao.findUserByEmail(user.getEmail());
        }
        if(dbUser==null) {
            throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_USER_NOT_FOUND);
        }
        String tokenCacheKey = getCacheKeyForResetToken(dbUser);
        String cachedToken = this.cacheService.get(tokenCacheKey);
        logger.debug(String.format("cache[%s]: %s", tokenCacheKey, cachedToken));
        if(cachedToken==null) {
            throw new APIRuntimeException(SC_BAD_REQUEST, MSG_TEMPLATE_EXPIRED_RESET_TOKEN);
        }
        if(!cachedToken.equals(user.getCredential().getResetToken())) {
            throw new APIRuntimeException(SC_BAD_REQUEST, MSG_TEMPLATE_INVALID_RESET_TOKEN);
        }
        if(dbUser.getCredential()==null)
            dbUser.setCredential(new Credential());
        dbUser.getCredential().setPassword(user.getCredential().getPassword());
        
        logger.debug(String.format("updating password for user: %s", dbUser.getHandle()));
        userDao.updatePassword(dbUser);
        
        logger.debug(String.format("cache[%s] -> deleted", tokenCacheKey));
        this.cacheService.delete(tokenCacheKey);
        
        return ApiResponseFactory.createResponse(dbUser);
    }
    
    @GET
    @Path("/resetToken")
    @Timed
    public ApiResponse getResetToken(
            @QueryParam("handle") String handle,
            @QueryParam("email") String email,
            @Context HttpServletRequest request) throws Exception {

        logger.info(String.format("getResetToken(%s, %s)", handle, email));

        if((email==null || email.length()==0) && (handle==null || handle.length()==0)) {
            throw new APIRuntimeException(HttpURLConnection.HTTP_BAD_REQUEST, String.format(MSG_TEMPLATE_MANDATORY, "Email or password"));
        }
        
        User user = null;
        if(handle!=null && handle.length()>0) {
            logger.debug(String.format("findUserByHandle(%s)", handle));
            user = this.userDao.findUserByHandle(handle);
        }
        if(user==null) {
            logger.debug(String.format("findUserByEmail(%s)", email));
            user = this.userDao.findUserByEmail(email);
        }
        if(user==null)
            throw new APIRuntimeException(HttpURLConnection.HTTP_NOT_FOUND, MSG_TEMPLATE_USER_NOT_FOUND);
        
        // SSO users can't reset their password.
        List<UserProfile> ssoProfiles = userDao.getSSOProfiles(Utils.toLongValue(user.getId()));
        if(ssoProfiles!=null && ssoProfiles.size()>0)
            throw new APIRuntimeException(HttpURLConnection.HTTP_FORBIDDEN, MSG_TEMPLATE_NOT_ALLOWED_TO_RESET_PASSWORD);
        

        logger.debug(String.format("user[%s].handle: %s", user.getId(), user.getHandle()));
        
        // check social user account
        long userId = Utils.toLongValue(user.getId());
        user.setProfiles(userDao.getSocialProfiles(userId));
        
        // check SSO user account
        if(user.getProfiles()==null) {
            user.setProfiles(userDao.getSSOProfiles(userId));
        }

        String cacheKey = getCacheKeyForResetToken(user);
        String cachedToken = cacheService.get(cacheKey);
        logger.debug(String.format("cache[%s] -> %s", cacheKey, cachedToken));
        if(cachedToken!=null) {
            throw new APIRuntimeException(HttpURLConnection.HTTP_BAD_REQUEST, MSG_TEMPLATE_RESET_TOKEN_ALREADY_ISSUED);
        }
        String resetToken = generateResetToken();
        user.getCredential().setResetToken(resetToken);
        
        logger.debug(String.format("cache[%s] <- %s", cacheKey, resetToken));
        cacheService.put(cacheKey, resetToken, getResetTokenExpirySeconds());
        
        logger.debug(String.format("sending password-reset mail to %s.", user.getEmail()));
        notifyPasswordReset(user, resetToken, getResetPasswordUrlPrefix(request));

        user.getCredential().clearResetToken();
        user.getCredential().clearActivationCode();
        return ApiResponseFactory.createResponse(user);
    }
    
    @GET
    @Path("/{resourceId}/achievements")
    @Timed
    public ApiResponse getAchievements(
            @Auth AuthUser authUser,
            @PathParam("resourceId") TCID resourceId,
            @APIQueryParam(repClass = Achievement.class) QueryParameter query,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("getAchievements(%s)", resourceId));
        
        // checking ID
        checkResourceId(resourceId);
        // checking permission
        checkAdminPermission(authUser, resourceId);
        
        Long userId = Utils.toLongValue(resourceId);
        logger.debug(String.format("findUserById(%s)", userId));
        User userInDB = userDao.findUserById(userId);
        if(userInDB==null) {
            throw new APIRuntimeException(SC_NOT_FOUND, MSG_TEMPLATE_USER_NOT_FOUND);
        }
        
        List<Achievement> achievements = userDao.findAchievements(userId);
        return ApiResponseFactory.createFieldSelectorResponse(achievements, query.getSelector());
    }

    @GET
    @Path("/validateHandle")
    @Timed
    public ApiResponse validateHandle(
            @QueryParam("handle") String handle,
            @Context HttpServletRequest request) throws Exception {
        logger.info(String.format("validateHandle(%s)", handle));
        
        if(handle==null || handle.length()==0)
            throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, String.format(MSG_TEMPLATE_MANDATORY, "handle"));
        
        User user = new User();
        user.setHandle(handle);
        
        String err = user.validateHandle();
        if(err == null)
            err = validateHandle(handle);
        
        return ApiResponseFactory.createResponse(
                createValidationResult((err == null), err));
    }
    
    @GET
    @Path("/validateEmail")
    @Timed
    public ApiResponse validateEmail(
            @QueryParam("email") String email,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("validateEmail(%s)", email));
        
        if(email==null || email.length()==0)
            throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, String.format(MSG_TEMPLATE_MANDATORY, "email"));

        User user = new User();
        user.setEmail(email);
        
        String err = user.validateEmail();
        if(err == null)
            err = validateEmail(email);
        
        return ApiResponseFactory.createResponse(
                createValidationResult((err == null), err));
    }
    
    @GET
    @Path("/validateSocial")
    @Timed
    public ApiResponse validateSocial(
            @QueryParam("socialUserId") String socialUserId,
            @QueryParam("socialProvider") String socialProvider,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("validateSocial(userId=%s, provider=%s)", socialUserId, socialProvider));
        
        if(socialUserId==null || socialUserId.length()==0)
            throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, String.format(MSG_TEMPLATE_MANDATORY, "socialUserId"));
        if(socialProvider==null || socialProvider.length()==0)
            throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, String.format(MSG_TEMPLATE_MANDATORY, "socialProvider"));

        UserProfile profile = new UserProfile();
        profile.setUserId(socialUserId);
        profile.setProviderType(socialProvider);
        
        if(profile.getProviderTypeEnum()==null || !profile.getProviderTypeEnum().isSocial)
            throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, String.format(MSG_TEMPLATE_UNSUPPORTED_PROVIDER, socialProvider));
        
        String err = validateSocialProfile(profile);
        
        return ApiResponseFactory.createResponse(
                createValidationResult((err == null), err));
    }
    
    @POST
    @Path("/oneTimeToken")
    @Timed
    public ApiResponse getOneTimeToken(
            @FormParam("userId") String userId,
            @FormParam("password") String password,
            @Context HttpServletRequest request) throws Exception {
        
        logger.info(String.format("getOneTimeToken(%s)", userId));
        
        if(Utils.isEmpty(userId))
            throw new APIRuntimeException(SC_BAD_REQUEST, String.format(MSG_TEMPLATE_MANDATORY, "userId"));
        if(Utils.isEmpty(password))
            throw new APIRuntimeException(SC_BAD_REQUEST, String.format(MSG_TEMPLATE_MANDATORY, "password"));

        TCID id = new TCID(userId);
        // checking ID
        checkResourceId(id);
        
        // authenticate user by userId and password. return 403
        logger.debug(String.format("authenticating user by '%s'", userId));
        User user = userDao.authenticate(Utils.toLongValue(id), password);
        if(user==null) {
            throw new APIRuntimeException(SC_UNAUTHORIZED, "Credentials are incorrect.");
        }

        String tokenKey = getCacheKeyForOneTimeToken(id);
        String token = cacheService.get(tokenKey);
        
        // return 400 when token has been issued
        if(token!=null)
            throw new APIRuntimeException(HttpURLConnection.HTTP_BAD_REQUEST, "One-Time Token has been issued."); //TODO: MSG
        
        // generate token
        token = generateOneTimeToken(user, getDomain(), getOneTimeTokenExpirySeconds());
        
        // store token with expiry time
        cacheService.put(tokenKey, token, getOneTimeTokenExpirySeconds());
        
        // return token
        return ApiResponseFactory.createResponse(token);
    }
    
    protected String generateOneTimeToken(User user, String domain, Integer expirySeconds) {
        JWTToken jwt = new JWTToken();
        jwt.setHandle(user.getHandle());
        jwt.setUserId(user.getId().toString());
        jwt.setIssuer(jwt.createIssuerFor(domain));
        if(expirySeconds!=null)
            jwt.setExpirySeconds(expirySeconds);
        
        return jwt.generateToken(getSecret());
    }
    
    protected void checkAdminPermission(AuthUser operator, TCID resourceId) {
        if(operator==null)
            throw new IllegalArgumentException("operator should be specified.");
        if(resourceId!=null && !resourceId.equals(operator.getUserId()) && !hasAdminRole(operator))
            throw new APIRuntimeException(SC_FORBIDDEN, "Forbidden");
    }

    protected void checkResourceId(TCID id) {
        if(!Utils.isValid(id))
            throw new APIRuntimeException(SC_BAD_REQUEST, Constants.MSG_TEMPLATE_INVALID_ID);
    }
    
    protected String getTopicForUpdatedStatus(String newStatus) {
        return MemberStatus.ACTIVE.getValue().equals(newStatus) ?
                "event.user.activated" : "event.user.deactivated";
    }

    protected boolean isValidStatusValue(String status) {
        return MemberStatus.getByValue(status)!=null;
    }
    
    protected String getResetPasswordUrlPrefix(HttpServletRequest request) {
        String resetPasswordUrlPrefix = request.getParameter("resetPasswordUrlPrefix");
        if(resetPasswordUrlPrefix!=null)
            return resetPasswordUrlPrefix;
        
        String source = request.getParameter("source");
        String domain = getDomain()!=null ? getDomain() : "topcoder.com";
        String template = "https://%s.%s/reset-password";
        return "connect".equalsIgnoreCase(source) ?
                String.format(template, "connect", domain) :
                String.format(template, "www", domain);
    }
    
    protected boolean hasAdminRole(AuthUser user) {
        if(user==null || user.getRoles()==null)
            return false;
        return user.getRoles().contains("administrator");
    }
    
    protected String generateResetToken() {
        return Utils.generateRandomString(ALPHABET_ALPHA_EN+ALPHABET_DIGITS_EN, 6);
    }
    
    protected String getCacheKeyForResetToken(User user) {
        return String.format("ap:identity:reset-tokens:%s", user.getId().toString());
    }

    protected String getCacheKeyForActivationCode(TCID userId, String email) {
        return String.format("ap:identity:activation-codes:%s-%s", userId, email);
    }
    
    protected String getCacheKeyForOneTimeToken(TCID userId) {
        return String.format("ap:identity:onetime-tokens:%s", userId);
    }

    protected String validateHandle(String handle) {
        if (this.userDao==null)
            throw new IllegalArgumentException("userDao is not specified.");
        if (handle==null || handle.length()==0)
            throw new IllegalArgumentException("handle must be specified.");

        if (userDao.isInvalidHandle(handle))
            return MSG_TEMPLATE_INVALID_HANDLE;
        if (userDao.handleExists(handle))
            return String.format(MSG_TEMPLATE_DUPLICATED_HANDLE, handle);
        return null;
    }    

    protected String validateEmail(String email) {
        if (email==null || email.length()==0)
            throw new IllegalArgumentException("email must be specified.");
        if (this.userDao==null)
            throw new IllegalStateException("userDao is not specified.");
        
        boolean exists = userDao.emailExists(email);
        logger.info(String.format("emailExists(%s): %s", email, exists));
        if (exists)
            return String.format(MSG_TEMPLATE_DUPLICATED_EMAIL, email);
        return null;
    }

    /**
     * Validates country#code and country#name.
     * If the country has value on these fields, the method checks they are existing in "country" table.
     * @param country
     * @return null if country is valid. otherwise error message.
     */
    protected String validateCountry(Country country) {
        if(country==null)
            throw new IllegalArgumentException("country must be specified.");
        
        Country cnt = userDao.findCountryBy(country);
        if(cnt==null)
            return MSG_TEMPLATE_INVALID_COUNTRY;
        
        // populate with data in database for the subsequent process
        country.setCode(cnt.getCode());
        country.setISOAlpha2Code(cnt.getISOAlpha2Code());
        country.setISOAlpha3Code(cnt.getISOAlpha3Code());
        country.setName(cnt.getName());
        return null;
    }
    
    // for registration 
    protected String validateProfile(UserProfile profile) {
        return validateProfile(null, profile);
    }
    
    // for adding profile
    protected String validateProfile(Long userId, UserProfile profile) {
        if (profile==null)
            throw new IllegalArgumentException("profile must be specified.");
        
        if(profile.isSocial()) {
            return validateSocialProfile(userId, profile);
        }
        if(profile.isEnterprise() && profile.getProviderTypeEnum() != ProviderType.LDAP) {
            return validateSSOProfile(profile);
        }
        return null;
    }
    
    protected String validateSocialProfile(UserProfile profile) {
        return validateSocialProfile(null, profile);
    }
    
    protected String validateSocialProfile(Long userId, UserProfile profile) {
        if (profile==null)
            throw new IllegalArgumentException("profile must be specified.");
        
        String socialId = profile.getUserId();
        if(socialId==null)
            return String.format(MSG_TEMPLATE_MANDATORY, "Social User Id");
        ProviderType provider =  profile.getProviderTypeEnum();
        if(provider==null || !provider.isSocial)
            return "Unsupported provider: "+profile.getProviderType(); // unsupported provider
        
        if(userId!=null) {
            List<UserProfile> profiles = userDao.getSocialProfiles(userId, provider);
            if(profiles!=null && profiles.size()>0)
                return MSG_TEMPLATE_USER_ALREADY_BOUND_WITH_PROVIDER; // user is already bound with the specified provider
        }
        
        if(userDao.socialUserExists(profile))
            return MSG_TEMPLATE_SOCIAL_PROFILE_IN_USE; // already exists

        return null;
    }
    
    protected String validateSSOProfile(UserProfile profile) {
        if(profile==null)
            throw new IllegalArgumentException("profile must be specified.");
        
        String userId = profile.getUserId();
        String email = profile.getEmail();
        if(userId==null && email==null)
            return String.format(MSG_TEMPLATE_MANDATORY, "At least one of SSO User ID or Email");
        
        ProviderType provider =  profile.getProviderTypeEnum();
        if(provider==null || !provider.isEnterprise)
            return "Unsupported provider: "+profile.getProviderType(); // unsupported provider
        
        if(userDao.ssoUserExists(profile))
            return MSG_TEMPLATE_SSO_PROFILE_IN_USE; // already exists

        return null;
    }
    
    protected String validateReferral(String source) {
        if(source==null || source.trim().length()==0)
            return MSG_TEMPLATE_MISSING_UTMSOURCE;
        
        if(!userDao.handleExists(source))
            return MSG_TEMPLATE_USER_NOT_FOUND;
        return null;
    }

    public int getResetTokenExpirySeconds() {
        return resetTokenExpirySeconds;
    }

    public void setResetTokenExpirySeconds(int resetTokenExpirySeconds) {
        this.resetTokenExpirySeconds = resetTokenExpirySeconds;
    }
    
    public int getResendActivationCodeExpirySeconds() {
        return resendActivationCodeExpirySeconds;
    }

    public void setResendActivationCodeExpirySeconds(int resendActivationCodeExpirySeconds) {
        this.resendActivationCodeExpirySeconds = resendActivationCodeExpirySeconds;
    }
    
    public int getOneTimeTokenExpirySeconds() {
        return oneTimeTokenExpirySeconds;
    }

    public void setOneTimeTokenExpirySeconds(int oneTimeTokenExpirySeconds) {
        this.oneTimeTokenExpirySeconds = oneTimeTokenExpirySeconds;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    protected void publishUserEvent(String topic, User user) {
        if(user==null)
            return;
        logger.info(String.format("Publishing a user event to '%s'. userId: %s, handle: %s", topic, user.getId(), user.getHandle()));
        publishEvent(topic, user);
    }
    
    
    protected void notifyActivation(User user, String redirectUrl) {
    	NotificationPayload payload = createActivationNotificationPayload(user, redirectUrl);
    	publishNotificationEvent(payload.getMailRepresentation());
    }
    
    protected NotificationPayload createActivationNotificationPayload(User user, String redirectUrl) {
    	//If for Connect registration, send activation email with activation code only.
    	boolean codeOnly = user.getUtmSource() != null && user.getUtmSource().contains("connect");
    	if(codeOnly) 
    		return new NotificationPayload.ActivationCodeOnlyPayload(user, redirectUrl);
    	
    	return new NotificationPayload.ActivationPayload(user, redirectUrl);
    }

    protected void notifyPasswordReset(User user, String resetToken, String resetPasswordUrlPrefix) {
        publishNotificationEvent(
                new NotificationPayload.PasswordResetPayload(user, resetToken, getResetTokenExpirySeconds(), resetPasswordUrlPrefix).getMailRepresentation());
    }
    
    protected void notifyWelcome(User user) {
        publishNotificationEvent(
                new NotificationPayload.WelcomePayload(user).getMailRepresentation());
    }

    protected void publishNotificationEvent(MailRepresentation mail) {
        if(mail==null)
            return;
        publishEvent("event.notification.send", mail);
    }
    
    protected void publishEvent(String topic, Object payload) {
        if(payload==null)
            return;
        if(topic==null)
            throw new IllegalArgumentException("topic must be specified.");
        if(this.eventProducer==null)
            throw new IllegalStateException("eventProducer must be configured.");
        if(this.objectMapper==null)
            throw new IllegalStateException("objectMapper must be configured.");
        String strPayload = null;
        try {
            logger.debug(String.format("Publishing an event to '%s'.", topic));
            strPayload = this.objectMapper.writeValueAsString(payload);
            this.eventProducer.publish(topic, strPayload);
        } catch (Exception e) {
            logger.error(String.format("Failed to publish an event. topic: %s, payload: %s", topic, strPayload), e);
        }
    }
    
    protected ValidationResult createValidationResult(boolean valid, String reason) {
        ValidationResult result = new ValidationResult();
        result.valid = valid;
        if(reason!=null && reason.indexOf("__")>0) {
            String[] tmp = reason.split("__", 2);
            result.reason = tmp[1];
            result.reasonCode = tmp[0];
        } else {
            result.reason = reason;
            result.reasonCode = Constants.code(reason);
        }
        return result;
    }
    
    /**
     * Result class for validation endpoints 
     */
    public static class ValidationResult {
        public boolean valid;
        public String reasonCode;
        public String reason;
    }
    
    private void assignDefaultUserRole(User user) {
        if (defaultUserRoleId == null) {
            Role role = roleDao.findRoleByName("Topcoder User");
            if (role == null) {
                logger.error("No role found for 'Topcoder User'");
                throw new IllegalStateException("Unable to assign default user role");
            }
            defaultUserRoleId = Long.parseLong(role.getId().toString());
        }
        
        try {
            long userId = Long.parseLong(user.getId().toString());
            int rows = roleDao.assignRole(defaultUserRoleId, userId, userId);
            
            if (rows == 0) {
                logger.error("No assignment row created when assigning default role to user " + userId);
            } else if (logger.isDebugEnabled()) {
                logger.debug(String.format("Created role assignment for user %d", userId));
            }
        } catch (Exception e) {
            logger.error("Unable to assign default user role to user " + user.getId(), e);
        }
    }
}

package com.appirio.tech.core.service.identity.resource;

import com.appirio.tech.core.service.identity.util.cache.CacheService;
import io.dropwizard.auth.Auth;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
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

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.api.v3.exception.APIRuntimeException;
import com.appirio.tech.core.api.v3.request.FieldSelector;
import com.appirio.tech.core.api.v3.request.PostPutRequest;
import com.appirio.tech.core.api.v3.request.QueryParameter;
import com.appirio.tech.core.api.v3.request.annotation.APIFieldParam;
import com.appirio.tech.core.api.v3.resource.GetResource;
import com.appirio.tech.core.api.v3.response.ApiResponse;
import com.appirio.tech.core.api.v3.response.ApiResponseFactory;
import com.appirio.tech.core.api.v3.util.jwt.InvalidTokenException;
import com.appirio.tech.core.api.v3.util.jwt.JWTToken;
import com.appirio.tech.core.api.v3.util.jwt.TokenExpiredException;
import com.appirio.tech.core.auth.AuthUser;
import com.appirio.tech.core.service.identity.dao.ClientDAO;
import com.appirio.tech.core.service.identity.dao.RoleDAO;
import com.appirio.tech.core.service.identity.dao.UserDAO;
import com.appirio.tech.core.service.identity.representation.Authorization;
import com.appirio.tech.core.service.identity.representation.Client;
import com.appirio.tech.core.service.identity.representation.Role;
import com.appirio.tech.core.service.identity.representation.User;
import com.appirio.tech.core.service.identity.representation.UserProfile;
import com.appirio.tech.core.service.identity.representation.ProviderType;
import com.appirio.tech.core.service.identity.util.Constants;
import com.appirio.tech.core.service.identity.util.HttpUtil;
import com.appirio.tech.core.service.identity.util.Utils;
import com.appirio.tech.core.service.identity.util.auth.Auth0Client;
import com.appirio.tech.core.service.identity.util.auth.Auth0Credential;
import com.appirio.tech.core.service.identity.util.auth.ServiceAccount;
import com.appirio.tech.core.service.identity.util.auth.ServiceAccountAuthenticator;
import com.appirio.tech.core.service.identity.util.store.AuthDataStore;
import com.appirio.tech.core.service.identity.util.zendesk.ZendeskAuthPlugin;
import com.auth0.jwt.JWTExpiredException;
import com.codahale.metrics.annotation.Timed;

@Path("authorizations")
@Produces(MediaType.APPLICATION_JSON)
public class AuthorizationResource implements GetResource<Authorization> {

    private static final Logger logger = Logger.getLogger(AuthorizationResource.class);

    public static final Integer MAX_COOKIE_EXPIRY_SECONDS = 90 * 24 * 3600; // 90d

    public static final String AUTH_REFRESH_LOG_KEY_PREFIX = "identity:";

    public static final String AUTH_REFRESH_LOG_KEY_DELIM = ",";

    public static final String AUTH_REFRESH_LOG_DATE_FORMAT = "yyyy-MM-dd_HH:mm:ss";
    
    private String authDomain;
    
    private AuthDataStore authDataStore;
    
    private Auth0Client auth0;
    
    private ServiceAccountAuthenticator serviceAccountAuthenticator;
    
    private UserDAO userDao;
    
    private RoleDAO roleDao;
    
    private ClientDAO clientDao;
    
    private ZendeskAuthPlugin zendeskAuthPlugin;
    
    private Integer jwtExpirySeconds;
    
    private Integer cookieExpirySeconds;
    
    private String secret;

    protected CacheService cacheService;

    public AuthorizationResource(String authDomain, AuthDataStore authDataStore, Auth0Client auth0, ServiceAccountAuthenticator serviceAccountAuthenticator, UserDAO userDao, RoleDAO roleDao) {
        this.authDomain = authDomain;
        this.authDataStore = authDataStore;
        this.auth0 = auth0;
        this.serviceAccountAuthenticator = serviceAccountAuthenticator;
        this.userDao = userDao;
        this.roleDao = roleDao;
    }

    public AuthorizationResource(String authDomain, AuthDataStore authDataStore, Auth0Client auth0, ServiceAccountAuthenticator serviceAccountAuthenticator, UserDAO userDao, RoleDAO roleDao, CacheService cacheService) {
        this(authDomain, authDataStore, auth0, serviceAccountAuthenticator, userDao, roleDao);
        this.cacheService = cacheService;
    }

    protected AuthDataStore getAuthDataStore() {
        return authDataStore;
    }

    protected Auth0Client getAuth0Client() {
        return this.auth0;
    }
    
    public ServiceAccountAuthenticator getServiceAccountAuthenticator() {
        return serviceAccountAuthenticator;
    }

    public String getAuthDomain() {
        return authDomain;
    }

    public void setAuthDomain(String authDomain) {
        this.authDomain = authDomain;
    }
    
    public Integer getJwtExpirySeconds() {
        return jwtExpirySeconds;
    }

    public void setJwtExpirySeconds(Integer jwtExpirySeconds) {
        this.jwtExpirySeconds = jwtExpirySeconds;
    }

    public Integer getCookieExpirySeconds() {
        return cookieExpirySeconds;
    }

    public void setCookieExpirySeconds(Integer cookieExpiarySeconds) {
        this.cookieExpirySeconds = cookieExpiarySeconds;
    }

    public void setClientDao(ClientDAO clientDao) {
        this.clientDao = clientDao;
    }

    public ZendeskAuthPlugin getZendeskAuthPlugin() {
        return zendeskAuthPlugin;
    }

    public void setZendeskAuthPlugin(ZendeskAuthPlugin zendeskAuthPlugin) {
        this.zendeskAuthPlugin = zendeskAuthPlugin;
    }

    @POST
    @Timed
    public ApiResponse createObject(
            @Valid PostPutRequest<Authorization> postRequest,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws Exception {
        
        Authorization auth = postRequest!=null ? postRequest.getParam() : null;
        if(auth==null) {
            // Creating Authorization with Auth0 access token
            String authCode = HttpUtil.getAuthorizationParam("Auth0Code", request);
            if(authCode==null || authCode.length()==0)
                throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            auth = createAuthorization(authCode, request);
        }
        else {
            // Creating Authorization with Auth0 JWT token
            if(auth.getExternalToken()==null || auth.getExternalToken().length()==0)
                throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
            if(auth.getId()==null)
                auth.setId(new TCID(auth.hashCode()));
            auth.setToken(createJWTToken(auth.getExternalToken()));
            auth.setTarget("1");
        }
        
        // Zendesk
        addZendeskInfo(auth);
        
        // Last Login Date
        updateLastLoginDate(auth);
        
        // Creating cookies (tcjwt, tcsso) for compatibility
        if(response!=null) {
            processTCCookies(auth, request, response);
        }
        
        // Saving authorization
        if(getAuthDataStore()==null)
            throw new IllegalStateException("authDataStore is not specified.");
        getAuthDataStore().put(auth);
        
        return ApiResponseFactory.createResponse(auth);
    }

    protected void addZendeskInfo(Authorization auth) {
        if(auth==null)
            return;
        if(zendeskAuthPlugin==null)
            throw new IllegalStateException("zendeskAuthPlugin has not been initialized.");
        zendeskAuthPlugin.process(auth);
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Timed
    public ApiResponse createObject(
            @FormParam("clientId") String clientId,
            @FormParam("secret") String clientSecret,
            @Context HttpServletRequest request) throws Exception {
        if(clientId==null || clientId.length()==0 || clientSecret==null || clientSecret.length()==0)
            throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");            
        
        // Authenticate with clientId and secret
        if(getServiceAccountAuthenticator()==null)
            throw new IllegalStateException("serviceAccountAuthenticator is not specified.");
        
        ServiceAccount account = getServiceAccountAuthenticator().authenticate(clientId, clientSecret);
        if(account==null) {
            throw new APIRuntimeException(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
        
        // Creating Authorization object
        Authorization auth = null;
        if(getAuthDataStore()==null)
            throw new IllegalStateException("authDataStore is not specified.");
        try {
            auth = createAuthorization(account.getContextUserId());

            // Last Login Date
            updateLastLoginDate(auth);

            // Saving authorization
            getAuthDataStore().put(auth);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new APIRuntimeException(e);
        }
        return ApiResponseFactory.createResponse(auth);
    }
    
    //@Override
    @PUT
    @Path("/{resourceId}")
    @Timed
    public ApiResponse updateObject(
            @Auth AuthUser authUser,
            @PathParam("resourceId") String resourceId,
            @Valid PostPutRequest<Authorization> putRequest,
            @Context HttpServletRequest request)
            throws Exception {
        throw new APIRuntimeException(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    //@Override
    @DELETE
    @Path("/{targetId}")
    @Timed
    public ApiResponse deleteObject(
            @Auth AuthUser authUser,
            @PathParam("targetId") String targetId,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws Exception {
        
        // Getting JWT token from header
        String token = HttpUtil.getAuthorizationParam("Bearer", request);
        if(token==null || token.length()==0)
            throw new APIRuntimeException(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        
        AuthDataStore store = getAuthDataStore();
        if(store==null)
            throw new IllegalStateException("authDataStore is not specified.");

        // response
        ApiResponse resp = new ApiResponse();
        resp.setResult(true, HttpServletResponse.SC_OK, null);

        // delete cookies
        deleteTCCookies(response);
        
        Authorization auth = store.get(token, targetId);
        if(auth==null) {
            // do nothing
            return resp;
        }
        
        // Clear authorization in store
        store.delete(token, targetId);
        
        // request to revoke refresh token.
        if(auth.getRefreshToken()!=null) {
            if(getAuth0Client()==null)
                throw new IllegalStateException("Auth0Client is not specified.");
            try {
                getAuth0Client().revokeRefreshToken(auth.getExternalToken(), auth.getRefreshToken());
            } catch(Exception e) {
                logger.warn("Failed to revoke refresh token.", e);
            }
        }
        return resp;
    }
    
    @DELETE
    @Timed
    public ApiResponse deleteObject(
            @Auth AuthUser authUser,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws Exception {
        return deleteObject(authUser, "1", request, response);
    }

    @Override
    //@GET
    public ApiResponse getObjects(
            @Auth AuthUser authUser,
            @APIFieldParam(repClass = Authorization.class) QueryParameter query,
            @Context HttpServletRequest request) throws Exception {
        
        throw new APIRuntimeException(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
    
    /**
     * Returns ASP token from given Authorization Bearer header.
     * Bearer can hold either of 2 token, (a) Appirio Service Platform JWT or (b) Auth0 JWT
     * 
     * (a) is the normal case, that JWT was created in this Identity Service, hence access/refresh token are stored in cache.
     * Verify that the JWT is valid, and return the refreshed new JWT.
     * 
     * (b) This is to support legacy login system.
     * Auth0 JWT comes in if user logged in from legacy site (since legacy login site only have Auth0 JWT in tcjwt cookie)
     * We'll exchange Auth0 token with ASP token and give it back.
     */
    //@Override
    @GET
    @Path("/{resourceId}")
    @Timed
    public ApiResponse getObject(
            @PathParam("resourceId") TCID targetId,
            @APIFieldParam(repClass = Authorization.class) FieldSelector selector,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws Exception {
        
        // Getting JWT token from header
        String token = HttpUtil.getAuthorizationParam("Bearer", request);
        if(token==null || token.length()==0)
            throw new APIRuntimeException(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");

        Authorization auth = null;
        //Assuming if the issuer is the same domain then token is ASP jwt, else it came from Auth0
        if(isIssuerSameDomain(token)) {
            // Verifying token
            if(!verifyJWTToken(token))
                throw new APIRuntimeException(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");

            // Getting authorization from cache
            //   If no token -> 401
            if(getAuthDataStore()==null)
                throw new IllegalStateException("authDataStore is not specified.");
            auth = getAuthDataStore().get(token, targetId.getId());
            if(auth==null)
                throw new APIRuntimeException(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");

            // Creating new token from auth0 token
            try {
                String newToken = createJWTToken(auth.getExternalToken());
                if(newToken==null)
                    throw new Exception("Failed to create JWT token.");
                auth.setToken(newToken);
                // testing to refresh token: throw new JWTExpiredException(0L);
            } catch (JWTExpiredException e) {
                // auth0 token expired. need to refresh it.
                auth.setToken(refresh(auth.getRefreshToken()));
            } catch (APIRuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new APIRuntimeException(e);
            }
            // COR-236 (https://appirio.atlassian.net/browse/COR-236)
            // User's session should be expired after 90 days from the last logged-in time.
            // Authorization object should not be refreshed in the cache.
            getAuthDataStore().put(auth);
        } else {
            //case (b): the user only has Auth0 JWT and not access/refresh token to Auth0
            Auth0Credential cred = new Auth0Credential();
            cred.setIdToken(token);
            cred.setTokenType("Bearer");
            auth = createAuthorization(cred);
        }
        
        // Creating cookies (tcjwt, tcsso) for compatibility
        if(response!=null) {
            processTCCookies(auth, request, response);
        }
        
        return ApiResponseFactory.createResponse(auth);
    }

    protected boolean verifyJWTToken(String token) {
        try {
            JWTToken jwt = new JWTToken();
            jwt.verifyAndApply(token, getSecret());
        } catch(TokenExpiredException e) {
            // ok
        } catch(InvalidTokenException e) {
            return false;
        }
        return true;
    }
    
    
    @GET
    @Path("/validateClient")
    @Timed
    public ApiResponse validateClient(
            @QueryParam("clientId") String clientId,
            @QueryParam("redirectUrl") String redirectUrl,
            @QueryParam("scope") String scope,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) throws Exception {
        
        if(clientId==null || clientId.length()==0)
            throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, "missing clientId");
        if(redirectUrl==null || redirectUrl.length()==0)
            throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, "missing redirectUrl");
        
        Client client = this.clientDao.findClient(clientId);
        if(client==null) {
            throw new APIRuntimeException(HttpServletResponse.SC_UNAUTHORIZED, "Unknown Client ID");
        }
        List<String> redirectUris = client.getRedirectUris();
        if(!redirectUris.contains(redirectUrl)) {
            throw new APIRuntimeException(HttpServletResponse.SC_UNAUTHORIZED, "Unregistered URI to redirect");
        }
        return ApiResponseFactory.createResponse("Valid client");
    }
    
    @Override
    public ApiResponse getObject(
            @Auth AuthUser authUser,
            @PathParam("resourceId") TCID targetId,
            @APIFieldParam(repClass = Authorization.class) FieldSelector selector,
            @Context HttpServletRequest request) throws Exception {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    protected Authorization createAuthorization(String authCode, HttpServletRequest request) {
        if(authCode==null)
            throw new IllegalArgumentException("authCode must be specified.");
        if(request==null)
            throw new IllegalArgumentException("request must be specified.");
        try {
            // Requesting access token to auth0
            if(getAuth0Client()==null)
                throw new IllegalStateException("Auth0Client is not specified.");
            Auth0Credential credential = getAuth0Client().getToken(authCode, createRedirectURL(request));
            return createAuthorization(credential);
        } catch (APIRuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new APIRuntimeException(e);
        }
    }
    
    protected Authorization createAuthorization(String systemUserId) throws Exception {
        if(systemUserId==null)
            throw new IllegalArgumentException("systemUserId must be specified.");
                
        Authorization auth = new Authorization();
        auth.setId(new TCID(auth.hashCode())); //TODO:
        
        JWTToken jwt = new JWTToken();
        jwt.setUserId(systemUserId);
        jwt.setIssuer(jwt.createIssuerFor(getAuthDomain()));
        if(this.jwtExpirySeconds!=null)
            jwt.setExpirySeconds(this.jwtExpirySeconds);
        // getting roles
        jwt.setRoles(getRoleNames(parseLong(systemUserId)));
        
        auth.setToken(jwt.generateToken(getSecret()));
        auth.setTarget("1");
        return auth;
    }

    protected Authorization createAuthorization(Auth0Credential credential) throws Exception {
        if(credential==null)
            throw new IllegalArgumentException("credential must be specified.");
        if(getAuth0Client()==null)
            throw new IllegalStateException("Auth0Client is not specified.");
        
        String auth0Token = credential.getIdToken();
        String newToken = createJWTToken(auth0Token);
        
        Authorization auth = new Authorization();
        auth.setId(new TCID(auth.hashCode())); //TODO:
        auth.setToken(newToken);
        auth.setRefreshToken(credential.getRefreshToken());
        auth.setExternalToken(auth0Token);
        auth.setTarget("1");
        return auth;
    }
    
    protected String createJWTToken(String auth0Token) throws Exception {
        if(auth0Token==null || auth0Token.length()==0)
            throw new IllegalArgumentException("auth0Token must be specified.");

        JWTToken jwt = new JWTToken();
        jwt.setUserId(getUserId(auth0Token));
        jwt.setIssuer(jwt.createIssuerFor(getAuthDomain()));
        if(this.jwtExpirySeconds!=null)
            jwt.setExpirySeconds(this.jwtExpirySeconds);

        Long userId = parseLong(jwt.getUserId());
        if(userId!=null) {
            // getting user attributes from database
            User user = userDao.findUserById(userId);
            if(user==null)
                throw new APIRuntimeException(HttpServletResponse.SC_NOT_FOUND, Constants.MSG_TEMPLATE_USER_NOT_FOUND);
            
            if(!user.isActive())
                throw new APIRuntimeException(HttpServletResponse.SC_FORBIDDEN, "Account Inactive");
            
            jwt.setHandle(user.getHandle());
            jwt.setEmail(user.getEmail());
            // getting roles
            jwt.setRoles(getRoleNames(userId));

            // store auth refresh log to redis
            this.storeAuthRefreshLogToCache(userId, user);
        }
        return jwt.generateToken(getSecret());
    }

    private void storeAuthRefreshLogToCache(Long userId, User user) {
        if (userId == null || user == null )
            throw new IllegalArgumentException("userId and user must be specified.");

        if (this.cacheService != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(AUTH_REFRESH_LOG_DATE_FORMAT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String refreshAt = dateFormat.format(new Date());
            String key = AUTH_REFRESH_LOG_KEY_PREFIX + userId + AUTH_REFRESH_LOG_KEY_DELIM + user.getHandle();

            try {
                this.cacheService.put(key, refreshAt);
            } catch(Exception e) {
                logger.warn("Failed to store auth refresh log.", e);
            }
        }
    }

    protected List<String> getRoleNames(Long userId) {
        if(roleDao==null)
            return null; // don't throw an error for the case Shiro is disabled.
        
        List<Role> roles = roleDao.getRolesBySubjectId(userId);
        if(roles==null)
            return new ArrayList<String>(0);
        return roles.stream().map(role -> role.getRoleName()).collect(Collectors.toList());
    }
    
    protected Long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch(Exception e) { logger.warn("Failed to convert String to Long. value: "+str); }
        return null;
    }

    protected String getUserId(String auth0Token) throws Exception {
        if(auth0Token==null)
            throw new IllegalArgumentException("auth0Token must be specified.");

        UserProfile profile = createProfile(auth0Token);
        
        ProviderType providerType = profile.getProviderTypeEnum();
        if(providerType == null) {
            logger.error(String.format("Unsupported provider detected in Auth0 JWT token. provider: %s, token:%s", profile.getProviderType(), auth0Token));
            throw new APIRuntimeException(HttpServletResponse.SC_UNAUTHORIZED, "Unsupported provider.");        
        }

        Long userId = null;
        try {
            userId = userDao.getUserId(profile);
        } catch (Exception e) {
            logger.error(String.format("Failed to gain User ID from Auth0 JWT token. error: %s, token: %s", e.getMessage(), auth0Token), e);
            throw new APIRuntimeException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Received unexpected data from the remote ID provider.");
        }
        if(userId==null)
            throw new APIRuntimeException(HttpServletResponse.SC_UNAUTHORIZED, "User is not registered");
        
        return String.valueOf(userId);

    }

    protected UserProfile createProfile(String auth0Token) throws Exception {
        Auth0Client auth0 = getAuth0Client();
        if(auth0==null)
            throw new IllegalStateException("Auth0Client is not specified.");
        
        UserProfile profile = new UserProfile();
        profile.applyJWTClaims(auth0.verifyToken(auth0Token));
        return profile;
    }
    
    protected String refresh(String refreshToken) throws Exception {
        if(refreshToken==null)
            throw new IllegalArgumentException("refreshToken must be specified.");
        if(getAuth0Client()==null)
            throw new IllegalStateException("Auth0Client is not specified.");
        
        Auth0Credential cred = getAuth0Client().refresh(refreshToken);
        if(cred==null || cred.getIdToken()==null)
            throw new Exception("Failed to refresh token. refresh-token: "+refreshToken);
        return createJWTToken(cred.getIdToken());
    }
    
    // adjust protocol part in the requested url with referer url.
    protected String createRedirectURL(HttpServletRequest request) {
        if(request==null)
            return null;
        
        String url = request.getRequestURL().toString();
        String referer = request.getHeader("Referer");
        if(referer==null || referer.length()==0)
            return url;
        try {
            String proto = new URL(referer).getProtocol();
            return proto + "://" + url.replaceFirst("^.+://", "");
        } catch (MalformedURLException e) {
            logger.error("Failed to create redirect url. base:"+url+", referer:"+referer, e);
        }
        return url;
    }
    
    /**
     * Returns true if given jwt has same issuer as this domain (which is the case for Apprio Platform JWT).
     * @param token
     * @return
     */
    protected boolean isIssuerSameDomain(String token) {
        try {
            Map<String, Object> claims = Utils.parseJWTClaims(token);
            String issuer = String.valueOf(claims.get(JWTToken.CLAIM_ISSUER));
            return issuer.equalsIgnoreCase(new JWTToken().createIssuerFor(authDomain));
        } catch (Exception e) {
            throw new APIRuntimeException(e);
        }
    }
    
    protected void updateLastLoginDate(Authorization auth) {
        if(auth==null)
            throw new IllegalArgumentException("auth must be specified.");
        Long userId = extractUserId(auth);
        if(userId==null)
            return;
        User user = new User();
        user.setId(new TCID(userId));
        try {
            userDao.updateLastLoginDate(user);
        } catch (Exception e) {
            logger.error("Failed to record the last login date for " + userId, e);
        }
    }

    protected void processTCCookies(Authorization auth, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if(response==null)
            throw new IllegalArgumentException("response must be specified.");

        Integer maxAge = this.cookieExpirySeconds;
        
        boolean rememberMe = request!=null ? getRememberMe(request) : false;
        if(rememberMe)
            maxAge = MAX_COOKIE_EXPIRY_SECONDS;
        Cookie tcjwt = createCookie("tcjwt", auth.getExternalToken(), maxAge);
        response.addCookie(tcjwt);
        
        if(auth!=null && auth.getToken()!=null) {
            Long userId = extractUserId(auth);
            Cookie tcsso = createCookie("tcsso", userDao.generateSSOToken(userId), maxAge);
            response.addCookie(tcsso);
        }
    }
    
    protected void deleteTCCookies(HttpServletResponse response)  {

        if(response==null)
            throw new IllegalArgumentException("response must be specified.");

        Cookie tcjwt = createCookie("tcjwt", null, 0);
        response.addCookie(tcjwt);
        
        Cookie tcsso = createCookie("tcsso", null, 0);
        response.addCookie(tcsso);
    }

    protected Long extractUserId(Authorization auth) {
        if(auth==null || auth.getToken()==null)
            return null;
        try {
            return Long.parseLong((String)Utils.parseJWTClaims(auth.getToken()).get("userId"));
        } catch (Exception e) {
            logger.error("Failed to extract userId from JWT. token: "+auth.getToken(), e);
            return null;
        }
    }

    protected boolean getRememberMe(HttpServletRequest request) {
        if(request==null)
            throw new IllegalArgumentException("request must be specified.");
        Cookie[] cookies = request.getCookies();
        if(cookies!=null) {
            for(int i=0; i<cookies.length; i++) {
                if(cookies[i].getName().toLowerCase().equals("rememberme")) {
                    return Boolean.valueOf(cookies[i].getValue());
                }
            }
        }
        return false;
    }
    
    protected Cookie createCookie(String name, String value, Integer maxAge) {
        Cookie cookie = new Cookie(name, value);
        if(maxAge!=null) {
            cookie.setMaxAge(maxAge);
        }
        cookie.setDomain("."+getAuthDomain());
        cookie.setPath("/");
        return cookie;
    }
}

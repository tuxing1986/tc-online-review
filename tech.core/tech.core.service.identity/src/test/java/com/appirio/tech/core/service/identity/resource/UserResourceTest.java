package com.appirio.tech.core.service.identity.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.api.v3.exception.APIRuntimeException;
import com.appirio.tech.core.api.v3.request.PostPutRequest;
import com.appirio.tech.core.api.v3.response.ApiResponse;
import com.appirio.tech.core.api.v3.response.Result;
import com.appirio.tech.core.service.identity.dao.UserDAO;
import com.appirio.tech.core.service.identity.representation.User;
import com.appirio.tech.core.service.identity.util.Constants;
import com.appirio.tech.core.service.identity.util.idgen.SequenceDAO;


public class UserResourceTest {
	
	@Test
	public void testCreateObject() throws Exception {
		
		TCID id = new TCID(12345678L);
		String handle = "johndoe";
		String email = "johndoe@example.com";
				
		// Creating mock: User - always validated
		User user = Mockito.mock(User.class);
		Mockito.when(user.getId()).thenReturn(null);
		Mockito.when(user.getHandle()).thenReturn(handle);
		Mockito.when(user.getEmail()).thenReturn(email);
		Mockito.when(user.validate()).thenReturn(null);
		
		
		// Creating mock: PostPutRequest - give mock user
		PostPutRequest param = Mockito.mock(PostPutRequest.class);
		Mockito.when(param.getParamObject(User.class)).thenReturn(user);

		// Creating mock: UserDAO - always judge that there's no duplication in input data.
		UserDAO userDao = Mockito.mock(UserDAO.class);
		Mockito.when(userDao.register(user)).thenReturn(id);

		// Creating mock: Other
		SequenceDAO seqDao = Mockito.mock(SequenceDAO.class);

		// Test
		UserResource testee = new UserResource(userDao, seqDao);

		// Checking result
		ApiResponse resp = testee.createObject(null, param, null);
		Assert.assertNotNull(resp);
		
		Result result = resp.getResult();
		Assert.assertNotNull(result);
		
		Assert.assertEquals(HttpServletResponse.SC_OK, (int)result.getStatus());
		Assert.assertTrue(result.getSuccess());
		Assert.assertEquals(user, result.getContent());
	}
	
	@Test
	public void testCreateObject_DuplicatedHandleInput() throws Exception {
		
		String handle = "johndoe";
		
		// Creating mock: User - always validated
		User user = Mockito.mock(User.class);
		Mockito.when(user.getHandle()).thenReturn(handle);
		Mockito.when(user.validate()).thenReturn(null);
		
		// Creating mock: PostPutRequest - give mock user
		PostPutRequest param = Mockito.mock(PostPutRequest.class);
		Mockito.when(param.getParamObject(User.class)).thenReturn(user);
		
		// Creating mock: UserDAO - always judge that the handle is duplicated.
		UserDAO userDao = Mockito.mock(UserDAO.class);
		Mockito.when(userDao.handleExists(handle)).thenReturn(true); // duplicated!

		// Creating mock: Other
		SequenceDAO seqDao = Mockito.mock(SequenceDAO.class);
		
		// Test
		UserResource testee = new UserResource(userDao, seqDao);
		
		try {
			testee.createObject(null, param, null);
			fail("Exception should be thrown in the previous step.");
		} catch (APIRuntimeException e) {
			assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getHttpStatus());
			assertEquals(String.format(Constants.MSG_TEMPLATE_DUPLICATED_HANDLE, handle), e.getMessage());
		}
	}
	
	@Test
	public void testCreateObject_DuplicatedEmailInput() throws Exception {
		
		String handle = "johndoe";
		String email = "johndoe@example.com";
		
		// Creating mock: User - always validated
		User user = Mockito.mock(User.class);
		Mockito.when(user.getHandle()).thenReturn(handle);
		Mockito.when(user.getEmail()).thenReturn(email);
		Mockito.when(user.validate()).thenReturn(null);
		
		// Creating mock: PostPutRequest - give mock user
		PostPutRequest param = Mockito.mock(PostPutRequest.class);
		Mockito.when(param.getParamObject(User.class)).thenReturn(user);
		
		// Creating mock: UserDAO - always judge that the handle is duplicated.
		UserDAO userDao = Mockito.mock(UserDAO.class);
		Mockito.when(userDao.handleExists(handle)).thenReturn(false); // not duplicated for handle
		Mockito.when(userDao.findUserByEmail(email)).thenReturn(user); // duplicated!

		// Creating mock: Other
		SequenceDAO seqDao = Mockito.mock(SequenceDAO.class);
		
		// Test
		UserResource testee = new UserResource(userDao, seqDao);
		
		try {
			testee.createObject(null, param, null);
			fail("Exception should be thrown in the previous step.");
		} catch (APIRuntimeException e) {
			assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getHttpStatus());
			assertEquals(String.format(Constants.MSG_TEMPLATE_DUPLICATED_EMAIL, email), e.getMessage());
		}
	}
	
/*
	public ApiResponse createObject(
			@Auth(required=false) AuthUser authUser,
			@Valid PostPutRequest postRequest,
			@Context HttpServletRequest request) throws Exception {
		
		User user = (User)postRequest.getParamObject(User.class);
		String error = user.validate();
		if(error!=null)
			error = validateHandle(user.getHandle(), userDao);
		if(error!=null)
			error = validateEmail(user.getEmail(), userDao);
        if(error!=null) {
        	throw new APIRuntimeException(HttpServletResponse.SC_BAD_REQUEST, error);
        }
        
		user.setActive(false);
		userDao.register(user);
		
		return ApiResponseFactory.createResponse(user);
	}
 */
	
	
	/*
	private static final UserDAO userDao = mock(UserDAO.class);

    @ClassRule
    public static final ResourceTestRule RULE = ResourceTestRule.builder()
    		// 0.8-rc5
    		//.setTestContainerFactory(new GrizzlyTestContainerFactory())
            .addResource(new UserResource(userDao))
            .build();

    private final User user = new User();
    
    @Before
    public void setup() {
    	user.setId(new TCID("1"));
        when(userDao.findUserById(Long.parseLong("1"))).thenReturn(user);
    }
    
    @Test
    public void testGetUser() {
        assertThat(RULE.getJerseyTest().target("/v3/users/1").request()
        		.get(User.class))
                .isEqualToComparingFieldByField(user);
        verify(userDao).findUserById(Long.parseLong("1"));
    }
    */
}

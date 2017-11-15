package com.appirio.api.core.tests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.appirio.automation.api.ApiObjectMapper;
import com.appirio.automation.api.UserAuthProvider;
import com.appirio.automation.api.config.AuthenticationConfiguration;
import com.appirio.automation.api.config.EnvironmentConfiguration;
import com.appirio.automation.api.config.UserConfiguration;
import com.appirio.automation.api.exception.AutomationException;
import com.appirio.automation.api.exception.DefaultRequestProcessorException;
import com.appirio.automation.api.exception.EntityAlreadyExistsException;
import com.appirio.automation.api.exception.InvalidEntityException;
import com.appirio.automation.api.exception.InvalidRequestException;
import com.appirio.automation.api.model.Filter;
import com.appirio.automation.api.model.User;
import com.appirio.automation.api.model.User.Param;
import com.appirio.automation.api.model.UserInfo;
import com.appirio.automation.api.service.AuthenticationService;
import com.appirio.automation.api.service.UserService;
import com.appirio.automation.api.util.ApiUtil;
import com.appirio.automation.api.util.NewApiUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
/**
 * Test class containing tests to test APIs for user creation,activation,reset token and reset password
 * Author : Munmun Mathur
 * Created Date : 2015/06/19
 * History :
 * Revision : 2015/07/07 Formatted exception handling and added logger
 * Revision : 2015/07/08 Added tests for negative test scenarios
 * Revision : 2015/07/21 Made the review changes
 * Revision : 2015/07/29 Applied changes done in shared automation lib
 * Revision : 2015/09/21 Updated code to create a new user using social account details
 * 			  and added code to generate a new social account user id.
 * Anjali - 12/24/2015 - Refactored to use Jackson API and new request processor. 
 */

public class UserTest {
	//AuthenticationService  authService;
	final static UserService userService = new UserService();
	final static Logger logger = Logger.getLogger(UserTest.class);
	JsonNode rootNode = null;
	int iterationCount=0;
	private static NewApiUtil newApiUtil;
	protected static List<User> usersList ;
	protected static Map<String,User> usersMap  = new HashMap<String,User>();
	protected static Map<String,String> userIdNameMap  = new HashMap<String,String>();
	@BeforeClass
	public void setUp() throws Exception {
			logger.info("Entering set up phase to initialise the configurations.");
			newApiUtil  =  new NewApiUtil();
			UserAuthProvider.initialize();
			//EnvironmentConfiguration.initialize();
			//AuthenticationConfiguration.initialize();
			//UserConfiguration.initialize();
			//authService = new AuthenticationService();
			logger.info("Read user.json file UserConfiguration.getUserDataFile() " +UserConfiguration.getUserDataFile());
			String userJson = newApiUtil.getFile(UserConfiguration.getUserDataFile());
			logger.debug(" User json " + userJson);
			rootNode = ApiObjectMapper.MAPPER.readTree(userJson); 
			usersList = NewApiUtil.toUsers(rootNode.path("userCreate").toString());
			
			//String jsonFile = getFile("user.json");
			//rootNode = ApiUtil.readJsonFromFile(jsonFile);
	}
	
	
	/**
	 * Tests the api to create a new user by passing user details as a JSON object and
	 * JWT token as header 
	 */
	/*@Test(priority = 1, testName = "Create User", description = "Test create user")
	public void testCreateUser() {
		for (int i = 0; i < 10; i++) {
		logger.info("UserTest:testCreateUser:Testing 'Create user' api endpoint.");
		System.out.println("*********Test execution count : "+i+"*************");
		JsonNode userNode = rootNode.get("userCreate");
		Iterator<JsonNode> elements = userNode.elements();
		JsonNode jsonUserObject = null;
		iterationCount=0;
		//Iterate over set of user parameters one by one
		while(elements.hasNext()) {
			jsonUserObject = elements.next();
			UserInfo userInfo = userService.createUser(jsonUserObject);
			Assert.assertNotNull(userInfo);
			iterationCount++;	
		}
		Assert.assertEquals(iterationCount, userNode.size());
	}

	*/
	/**
	 * Tests the api to create a new user by passing user details as a JSON object and
	 * JWT token as header 
	 */
	@Test(priority = 1, testName = "Create User", description = "Test create user")
	public void createUsers() {
		logger.info("UserTest:testCreateUser:Testing 'Create user' api endpoint.");
		JsonNode userNode = rootNode.get("userCreate");
		//handle:user
		usersMap = NewApiUtil.createUsers2(usersList,rootNode.path("activateUser").path("filter").toString());
		userIdNameMap = NewApiUtil.getUserIdNameMap(usersMap);
		Assert.assertEquals(usersMap.size(), userNode.size());
	}

	
	/**
	 * Tests the api to activate a newly created user by passing activation code as parameter and
	 * JWT token as header 
	 */
	@Test(priority = 2, testName = "Activate User PUT", description = "Test Activate User PUT")
	public void testActivateUserPUT() {
		logger.info("UserTest:testActivateUserPUT:Testing 'Activate user' api endpoint.");
		JsonNode userNode = rootNode.get("userCreate");
		JsonNode activateUser = rootNode.get("activateUser");
		Iterator<JsonNode> elements = userNode.elements();
		JsonNode jsonUserObject = null;
		//iterationCount=0;
		for (User u : usersList) {
			User createdUser = userService.createUser(u, UserAuthProvider.login());
			if(createdUser == null ) throw new InvalidRequestException("Cannot create a user.");
			String activationCode=createdUser.param.getCredential().getActivationCode();
			String userAtivationCode = activateUser.path("filter").toString().replaceAll("@activationCode", activationCode);
			Filter filter = NewApiUtil.toFilters(userAtivationCode);
			createdUser= userService.activateUser(filter.toString(), UserAuthProvider.login());
			Assert.assertNotNull(createdUser);
//			UserInfo activeUserInfo = userService.activateUser(activationCode);
//			Assert.assertNotNull(activeUserInfo);
		}
		//Iterate over set of user parameters one by one
//		while(elements.hasNext()) {
//			jsonUserObject = elements.next();
//			UserInfo userInfo = userService.createUser(jsonUserObject);
//			if(userInfo == null ) throw new InvalidRequestException("Cannot create a user.");
//			String activationCode=userInfo.getActivationCode();
//			UserInfo activeUserInfo = userService.activateUser(activationCode);
//			Assert.assertNotNull(activeUserInfo);
//			iterationCount++;
//			
//		}
//		Assert.assertEquals(iterationCount, userNode.size());
			
	}
	
	/**
	 * Tests the api to get details of an user by user id and passing JWT token as header 
	 **/
	@Test(priority = 3, testName = "GET user by ID", description = "Test GET user by id api")
	public void testUserByIdGET() {
		logger.info("UserTest:testUserByIdGET:Testing 'Get user by ID' api endpoint.");
		JsonNode userNode = rootNode.get("userCreate");
		Iterator<JsonNode> elements = userNode.elements();
		JsonNode jsonUserObject = null;
		iterationCount=0;
		for (User u : usersList) {
			User createdUser = userService.createUser(u, UserAuthProvider.login());
			if(createdUser == null ) throw new InvalidRequestException("Cannot create a user.");
			String userId=createdUser.param.getId();
			UserInfo getUserInf = userService.getUser(userId);
			Assert.assertNotNull(getUserInf);
		}
		//Iterate over set of user parameters one by one
//		while(elements.hasNext()) {
//			jsonUserObject = elements.next();
//			UserInfo userInfo = userService.createUser(jsonUserObject);
//			if(userInfo == null ) throw new InvalidRequestException("Cannot create a user.");
//			String userId=userInfo.getUserId();
//			UserInfo getUserInf = userService.getUser(userId);
//			Assert.assertNotNull(getUserInf);
//			iterationCount++;
//			
//		}
//		Assert.assertEquals(iterationCount, userNode.size());
	}
	
	/**
	 * Tests the api to get reset token for an user by user id and passing JWT token as header 
	 **/
	//@Test(priority = 4, testName = "Reset token GET", description = "Test reset token GET api")
	public void testResetTokenGET() {
		logger.info("UserTest:testResetTokenGET:Testing 'Reset token' api endpoint.");
		JsonNode userNode = rootNode.get("userCreate");
		Iterator<JsonNode> elements = userNode.elements();
		JsonNode jsonUserObject = null;
		iterationCount=0;
		//Iterate over set of user parameters one by one
		while(elements.hasNext()) {
			jsonUserObject = elements.next();
			UserInfo userInfo = userService.createUser(jsonUserObject);
			if(userInfo == null ) throw new InvalidRequestException("Cannot create a user.");
			String email = jsonUserObject.path("param").path("email").textValue();
			UserInfo resetUserInf = userService.resetUserToken(email);
			Assert.assertNotNull(resetUserInf.getResetToken());
			iterationCount++;
		}	
		Assert.assertEquals(iterationCount, userNode.size());
	}
	
	/**
	 * Tests the api to reset password for an user by passing reset token as parameter and 
	 * JWT token as header 
	 **/
	//@Test(priority = 5, testName = "Reset password PUT", description = "Test reset password PUT api")
	public void testResetPasswordPUT() {
		logger.info("UserTest:testResetPasswordPUT:Testing 'Reset password' api endpoint.");
		JsonNode userNode = rootNode.get("userCreate");
		JsonNode userResetPassword = rootNode.get("userResetPwd");
		Iterator<JsonNode> elements = userNode.elements();
		JsonNode jsonUserObject = null;
		UserInfo userInfo=null;
		iterationCount=0;
		//Iterate over set of user parameters one by one
		while(elements.hasNext()) {
			jsonUserObject = elements.next();
			userInfo = userService.createUser(jsonUserObject);
			if(userInfo == null ) throw new InvalidRequestException("Cannot create a user.");
			String email = userInfo.getEmail();
			UserInfo resetUserInf = userService.resetUserToken(email);
			System.out.println(resetUserInf);
			String resetToken=resetUserInf.getResetToken();
			((ObjectNode)userResetPassword.path("param").path("credential")).put("resetToken",resetToken);
			((ObjectNode)userResetPassword.path("param")).put("email",email);
			UserInfo resetUserPwdInf = userService.resetUserPassword(userResetPassword.toString());
			Assert.assertNotNull(resetUserPwdInf);
			iterationCount++;
			
		}	
		Assert.assertEquals(iterationCount, userNode.size());
	}
	
	/**
	 * Tests the 'Create user' api when invalid email id is passed as parameter and 
	 * JWT token as header 
	 * @throws Exception 
	 **/
	@Test(priority = 6, testName = "Invalid user email", description = "Test create user api with invalid user email",
			expectedExceptions=AutomationException.class)
	public void testCreateUserInvalidEmail() throws Exception{
		logger.info("UserTest:testCreateUserInvalidEmail:Testing create user api with invalid user email");
		
//		JsonNode userNode = rootNode.get("userInvalidEmail");
//		Iterator<JsonNode> elements = userNode.elements();
//		JsonNode jsonUserObject = null;
		List<User> createUsersWithSocialAccount = NewApiUtil.toUsers(rootNode.path("userInvalidEmail").toString());
		for (User u : createUsersWithSocialAccount) {
			User createdUser = userService.createUser(u, UserAuthProvider.login());
		}
		//Iterate over set of user parameters one by one
//		while(elements.hasNext()) {
//			jsonUserObject = elements.next();
//			UserInfo userInfo = userService.createUser(jsonUserObject);
//		}
		
		
		
		
	}
	
	/**
	 * Tests the 'Create user' api when invalid password is passed as parameter and 
	 * JWT token as header 
	 * @throws Exception 
	 **/
	@Test(priority = 7, testName = "password", description = "Test create user api with invalid user password",
			expectedExceptions=AutomationException.class)
	public void testCreateUserInvalidPassword() throws Exception{
		logger.info("UserTest:testCreateUserInvalidPassword:Testing create user api with invalid user password");
//		JsonNode userNode = rootNode.get("userInvalidPassword");
//		Iterator<JsonNode> elements = userNode.elements();
//		JsonNode jsonUserObject = null;
		List<User> createUsersWithSocialAccount = NewApiUtil.toUsers(rootNode.path("userInvalidPassword").toString());
		for (User u : createUsersWithSocialAccount) {
			User createdUser = userService.createUser(u, UserAuthProvider.login());
		//Iterate over set of user parameters one by one
//		while(elements.hasNext()) {
//			jsonUserObject = elements.next();
//			UserInfo userInfo = userService.createUser(jsonUserObject);
		}
		
	}
	
	/**
	 * Tests the api to validate user handle by passing handle as parameter and JWT token as header 
	 * The data passed to this test also contains some values to handle negative scenarios. 
	 **/
	@Test(priority = 8, testName = "Validate user handle GET", description = "Test validate user handle GET api")
	public void testValidateHandle() {
		logger.info("UserTest:testValidateHandle:Testing 'Validate handle' api endpoint.");
		JsonNode userNode = rootNode.get("userHandleValid");
		Iterator<JsonNode> elements = userNode.elements();
		JsonNode jsonUserObject = null;
		SoftAssert softAssert = new SoftAssert();
		iterationCount=0;
		//Iterate over set of user parameters one by one
		while(elements.hasNext()) {
			jsonUserObject = elements.next();
			String handle = jsonUserObject.path("param").path("handle").textValue();
			boolean validationResult = userService.isUserHandleValid(handle);
			softAssert.assertTrue(validationResult);
			iterationCount++;
			
		}	
		Assert.assertEquals(iterationCount, userNode.size());
	}
	
	/**
	 * Tests the api to validate user email by passing email as parameter. Here, email of length greater than 100 is passed as test data.
	 **/
	@Test(priority = 9, testName = "Validate user email GET", description = "Test validate user email GET api",
			expectedExceptions = InvalidEntityException.class)
	public void testValidateEmailInvalidLength() {
		logger.info("UserTest:testValidateEmailInvalidLength:Testing 'Validate email' api endpoint by passing email of length greater than 100 as test data");
		JsonNode userNode = rootNode.get("validateEmailInvalidLength");
		String email = userNode.path("param").path("email").textValue();
		boolean validationResult = userService.isUserEmailValid(email);
	}
	
	/**
	 * Tests the api to validate user email by passing email as parameter. Here, invalid email is passed as test data.
	 **/
	@Test(priority = 10, testName = "Validate user email GET", description = "Test validate user email GET api",
			expectedExceptions = InvalidEntityException.class)
	public void testValidateEmailInvalidEmail() {
		logger.info("UserTest:testValidateEmailInvalidEmail:Testing 'Validate email' api endpoint by passing invalid email id as test data");
		JsonNode userNode = rootNode.get("validateEmailInvalidEmail");
		String email = userNode.path("param").path("email").textValue();
		boolean validationResult = userService.isUserEmailValid(email);
	}
	
	/**
	 * Tests the api to validate user email by passing email as parameter. Here, already registered email id is passed as test data.
	 **/
	@Test(priority = 11, testName = "Validate user email GET", description = "Test validate user email GET api",
			expectedExceptions = EntityAlreadyExistsException.class)
	public void testValidateEmailAlreadyTaken() {
		logger.info("UserTest:testValidateEmailAlreadyTaken:Testing 'Validate email' api endpoint by passing already registered email id as test data");
		JsonNode userNode = rootNode.get("validateEmailAlreadyTaken");
		String email = userNode.path("param").path("email").textValue();
		boolean validationResult = userService.isUserEmailValid(email);
	}
	
	/**
	 * Tests the api to validate user email by passing email as parameter. Here, an email id, which is not registered, is passed as test data.
	 **/
	@Test(priority = 12, testName = "Validate user email GET", description = "Test validate user email GET api")
	public void testValidateEmailAvailableEmail() {
		logger.info("UserTest:testValidateEmailAlreadyTaken:Testing 'Validate email' api endpoint by passing an email id which is not registered as test data");
		JsonNode userNode = rootNode.get("validateEmailAvailableEmail");
		String email = userNode.path("param").path("email").textValue();
		boolean validationResult = userService.isUserEmailValid(email);
		Assert.assertTrue(validationResult);
	}
	
	/**
	 * Tests the api to validate user handle by passing handle as parameter. Here, a handle with invalid length is passed as test data.
	 **/
	@Test(priority = 13, testName = "Validate user handle GET", description = "Test validate user handle GET api",
			expectedExceptions = InvalidEntityException.class)
	public void testValidateHandleInvalidLength() {
		logger.info("UserTest:testValidateHandleInvalidLength:Testing 'Validate handle' api endpoint by passing handle of invalid length as test data");
		JsonNode userNode = rootNode.get("userHandleInvalidLenght");
		String handle = userNode.path("param").path("handle").textValue();
		boolean validationResult = userService.isUserHandleValid(handle);
	}
	
	/**
	 * Tests the api to validate user handle by passing handle as parameter. Here, a handle with invalid format is passed as test data. 
	 **/
	@Test(priority = 14, testName = "Validate user handle GET", description = "Test validate user handle GET api",
			expectedExceptions = InvalidEntityException.class)
	public void testValidateHandleInvalidFormat() {
		logger.info("UserTest:testValidateHandleInvalidFormat:Testing 'Validate handle' api endpoint by passing handle of invalid format as test data");
		JsonNode userNode = rootNode.get("userHandleInvalidFormat");
		String handle = userNode.path("param").path("handle").textValue();
		boolean validationResult = userService.isUserHandleValid(handle);
	}
	
	/**
	 * Tests the api to validate user handle by passing handle as parameter. Here, an invalid handle is passed as test data. 
	 **/
	@Test(priority = 15, testName = "Validate user handle GET", description = "Test validate user handle GET api",
			expectedExceptions = InvalidEntityException.class)
	public void testValidateHandleInvalidHandle() {
		logger.info("UserTest:testValidateHandleInvalidHandle:Testing 'Validate handle' api endpoint by passing invalid handle as test data");
		JsonNode userNode = rootNode.get("userHandleInvalidHandle");
		String handle = userNode.path("param").path("handle").textValue();
		boolean validationResult = userService.isUserHandleValid(handle);
	}
	
	/**
	 * Tests the api to validate user handle by passing handle as parameter. Here, an already registered handle is passed as test data. 
	 **/
	@Test(priority = 16, testName = "Validate user handle GET", description = "Test validate user handle GET api",
			expectedExceptions = EntityAlreadyExistsException.class)
	public void testValidateHandleAlreadyTaken() {
		logger.info("UserTest:testValidateHandleAlreadyTaken:Testing 'Validate handle' api endpoint by passing already registered handle as test data");
		JsonNode userNode = rootNode.get("userHandleAlreadyTaken");
		String handle = userNode.path("param").path("handle").textValue();
		boolean validationResult = userService.isUserHandleValid(handle);
	}
	
	/**
	 * Tests the api to validate user social account by passing social account details as parameter. Here, the details of an already
	 * registered social account is passed as test data. 
	 **/
	@Test(priority = 17, testName = "Validate user social account GET", description = "Test validate user social account GET api",
			expectedExceptions = EntityAlreadyExistsException.class)
	public void testValidateSocialAccountAlreadyTaken() {
		logger.info("UserTest:testValidateSocialAccountAlreadyTaken:Testing 'Validate social account' api endpoint by passing details of an already registered social account as test data");
		JsonNode userNode = rootNode.get("userSocialAccountAlreadyTaken");
		boolean validationResult = userService.isUserSocialAccountValid(userNode);
	}
	
	/**
	 * Tests the api to validate user social account by passing social account details as parameter. Here, the details of a new
	 * social account is passed as test data, which id not already registered.
	 **/
	@Test(priority = 18, testName = "Validate user social account GET", description = "Test validate user social account GET api")
	public void testValidateSocialAccountAvailable() {
		logger.info("UserTest:testValidateSocialAccountAvailable:Testing 'Validate social account' api endpoint by passing the details of a new social account as test data");
		JsonNode userNode = rootNode.get("userSocialAccountAvailable");
		Iterator<JsonNode> elements = userNode.elements();
		JsonNode jsonUserObject = null;
		SoftAssert softAssert = new SoftAssert();
		iterationCount=0;
		//Iterate over set of user parameters one by one
		while(elements.hasNext()) {
			jsonUserObject = elements.next();
  			boolean validationResult = userService.isUserSocialAccountValid(jsonUserObject);
			softAssert.assertTrue(validationResult);
			iterationCount++;
		}	
		Assert.assertEquals(iterationCount, userNode.size());
	}
	
	/**
	 * Tests the api to create a new user by passing user social account details as a JSON object and
	 * JWT token as header 
	 * @throws Exception 
	 */
	@Test(priority = 19, testName = "Create User with social account", description = "Test create user with social account")
	public void testCreateUserSocialAccount() throws Exception {
		logger.info("UserTest:testCreateUserSocialAccount:Testing 'Create user with social account' api endpoint.");
		JsonNode userNode = rootNode.get("userCreateSocialAccount");
		Iterator<JsonNode> elements = userNode.elements();
		JsonNode jsonUserObject = null;
		String socialUserId;
		UserInfo userInfo;
		String socialProvider;
		SoftAssert softAssert = new SoftAssert();
		iterationCount=0;
		
		//Iterate over set of user parameters one by one
		List<User> createUsersWithSocialAccount = NewApiUtil.toUsers(rootNode.path("userCreateSocialAccount").toString());
		for (User u : createUsersWithSocialAccount) {
		while(elements.hasNext()) {
			jsonUserObject = elements.next();
			socialUserId = ApiUtil.generateRandomString(10, "alphabetic");
			socialProvider= jsonUserObject.path("param").path("profile").path("providerType").textValue();
			//Add values for parameters 'socialUserId' and 'socialProvider' to be used to check if the social account is valid
			((ObjectNode)jsonUserObject.path("param")).put("socialUserId",socialUserId);
			((ObjectNode)jsonUserObject.path("param")).put("socialProvider",socialProvider);
			if(userService.isUserSocialAccountValid(jsonUserObject)){
				//Remove parameters 'socialUserId' and 'socialProvider' as these are not needed while creating a user using the social account
				((ObjectNode)jsonUserObject.path("param")).remove("socialUserId");
				((ObjectNode)jsonUserObject.path("param")).remove("socialProvider");
				//Add parameter 'userId' with its value set to the social user id generated
				((ObjectNode)jsonUserObject.path("param").path("profile")).put("userId",socialUserId);
				
				User createdUser = userService.createUser(u, UserAuthProvider.login());
				softAssert.assertNotNull(createdUser);
				break;
				}
//	 			userInfo = userService.createUser(jsonUserObject);
//	 			softAssert.assertNotNull(userInfo);
			}
		
			iterationCount++;	
		}
		Assert.assertEquals(iterationCount, userNode.size());
	}
	
}
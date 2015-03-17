package com.appirio.tech.core.service.identity.representation;

import static com.appirio.tech.core.service.identity.util.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

public class UserTest {

	@Test
	public void testValidate() {
		User user = new User();
		
		// Create Mock with dummy validators.
		User mock = Mockito.spy(user);
		Mockito.when(mock.validateHandle()).thenReturn(null);
		Mockito.when(mock.validateEmail()).thenReturn(null);
		Mockito.when(mock.validateFirstName()).thenReturn(null);
		Mockito.when(mock.validateLastName()).thenReturn(null);
		Mockito.when(mock.validatePassoword()).thenReturn(null);
		
		// test
		assertNull(mock.validate());
		
		// verifying that all validators are invoked.
		Mockito.verify(mock).validate();
		Mockito.verify(mock).validateHandle();
		Mockito.verify(mock).validateEmail();
		Mockito.verify(mock).validateFirstName();
		Mockito.verify(mock).validateLastName();
		Mockito.verify(mock).validatePassoword();
	}
	
	@Test
	public void testValidateName() {

		User testee = new User();
		
		// First Name
		assertNull("First name should not be mandatory", testee.validateFirstName());
		testee.setFirstName("");
		assertNull("First name should not be mandatory", testee.validateFirstName());

		testee.setFirstName(genText(MAX_LENGTH_FIRST_NAME));
		assertNull("First name should receive "+MAX_LENGTH_FIRST_NAME+" length string.",
				testee.validateFirstName());
		
		testee.setFirstName(genText(MAX_LENGTH_FIRST_NAME+1));
		assertEquals(
				String.format(MSG_TEMPALTE_INVALID_MAX_LENGTH, "first name", MAX_LENGTH_FIRST_NAME),
				testee.validateFirstName());
		
		
		// Last Name
		assertNull("Last name should not be mandatory", testee.validateLastName());
		testee.setLastName("");
		assertNull("Last name should not be mandatory", testee.validateLastName());

		testee.setLastName(genText(MAX_LENGTH_LAST_NAME));
		assertNull("Last name should receive "+MAX_LENGTH_LAST_NAME+" length string.",
				testee.validateLastName());
		
		testee.setLastName(genText(MAX_LENGTH_LAST_NAME+1));
		assertEquals(
				String.format(MSG_TEMPALTE_INVALID_MAX_LENGTH, "last name", MAX_LENGTH_LAST_NAME),
				testee.validateLastName());
		
	}
	
	@Test
	public void testValidateHandle() {
		
		User testee = new User();

		// Mandatory
		assertEquals(
				String.format(MSG_TEMPALTE_MANDATORY, "Handle"),
				testee.validateHandle());
		testee.setHandle("");
		assertEquals(
				String.format(MSG_TEMPALTE_MANDATORY, "Handle"),
				testee.validateHandle());
		
		// Length
		testee.setHandle(genText(MIN_LENGTH_HANDLE));
		assertNull(testee.validateHandle());
		
		testee.setHandle(genText(MAX_LENGTH_HANDLE));
		assertNull(testee.validateHandle());
		
		testee.setHandle(genText(MAX_LENGTH_HANDLE+1));
		assertEquals(
				String.format(MSG_TEMPALTE_INVALID_MINMAX_LENGTH, "handle", MIN_LENGTH_HANDLE, MAX_LENGTH_HANDLE),
				testee.validateHandle());
		testee.setHandle(genText(MIN_LENGTH_HANDLE-1));
		assertEquals(
				String.format(MSG_TEMPALTE_INVALID_MINMAX_LENGTH, "handle", MIN_LENGTH_HANDLE, MAX_LENGTH_HANDLE),
				testee.validateHandle());

		// Contains blank
		String handle = genText(MIN_LENGTH_HANDLE) + " " + genText(MAX_LENGTH_HANDLE-(MIN_LENGTH_HANDLE+1));
		assertTrue(handle.length() <= MAX_LENGTH_HANDLE);
		testee.setHandle(handle);
		assertEquals(
				MSG_TEMPALTE_INVALID_HANDLE_CONTAINS_SPACE,
				testee.validateHandle());
		
		// Contains forbidden char
		String[] INVALID_CHARS = "!\"#$%&'()='^~\\|@`;+:*,<>/?".split("");
		for(int i=0; i<INVALID_CHARS.length; i++) {
			if(INVALID_CHARS[i].length()==0)
				continue;
			handle = genText(MIN_LENGTH_HANDLE) + INVALID_CHARS[i];
			assertTrue(handle.length() <= MAX_LENGTH_HANDLE);
			testee.setHandle(handle);
			assertEquals(
					MSG_TEMPALTE_INVALID_HANDLE_CONTAINS_FORBIDDEN_CHARS,
					testee.validateHandle());
		}
		
		// Contains only punctuation
		handle = HANDLE_PUNCTUATION;
		assertTrue(handle.length() >= MIN_LENGTH_HANDLE);
		assertTrue(handle.length() <= MAX_LENGTH_HANDLE);
		testee.setHandle(handle);
		assertEquals(
				MSG_TEMPALTE_INVALID_HANDLE_CONTAINS_ONLY_PUNCTUATION,
				testee.validateHandle());
		
		// Starts with "admin"
		String[] ADMIN_PATTERNS = new String[]{
			"admin","Admin","ADMIN","aDmin"
		};
		for(int i=0; i<ADMIN_PATTERNS.length; i++) {
			if(ADMIN_PATTERNS[i].length()==0)
				continue;
			handle = ADMIN_PATTERNS[i] + genText(MIN_LENGTH_HANDLE);
			assertTrue(handle.length() <= MAX_LENGTH_HANDLE);
			testee.setHandle(handle);
			assertEquals(
					MSG_TEMPALTE_INVALID_HANDLE_STARTS_WITH_ADMIN,
					testee.validateHandle());
		}
	}
	
	@Test
	public void testValidateEmail() {
		
		User testee = new User();
		
		// Mandatory
		assertEquals(
				String.format(MSG_TEMPALTE_MANDATORY, "Email address"),
				testee.validateEmail());
		testee.setEmail("");
		assertEquals(
				String.format(MSG_TEMPALTE_MANDATORY, "Email address"),
				testee.validateEmail());

		String suffix = "@example.com";
		String prefix = genText(MAX_LENGTH_EMAIL-suffix.length());
		String testEmail = prefix+suffix;
		assertEquals(MAX_LENGTH_EMAIL, testEmail.length());
		testee.setEmail(testEmail);
		assertNull("Email address should receive "+MAX_LENGTH_EMAIL+" length string.",
				testee.validateEmail());

		testee.setEmail(genText(MAX_LENGTH_EMAIL+1));
		assertEquals(
				String.format(MSG_TEMPALTE_INVALID_MAX_LENGTH, "email address", MAX_LENGTH_EMAIL),
				testee.validateEmail());
	}
	
	@Test
	public void testValidatePassoword() {
		User testee = new User();

		// Mandatory
		assertEquals(
				String.format(MSG_TEMPALTE_MANDATORY, "Password"),
				testee.validatePassoword());
		Credential cred = new Credential();
		testee.setCredential(cred);
		assertEquals(
				String.format(MSG_TEMPALTE_MANDATORY, "Password"),
				testee.validatePassoword());
		cred.setPassword("");
		assertEquals(
				String.format(MSG_TEMPALTE_MANDATORY, "Password"),
				testee.validatePassoword());

		// Min length
		String sym = "1", num = "?";
		String symNum = sym + num;
		String passwd = genText(MIN_LENGTH_PASSWORD-symNum.length()) + symNum;
		assertEquals(MIN_LENGTH_PASSWORD, passwd.length());
		testee.getCredential().setPassword(passwd);
		assertNull(testee.validatePassoword());
		
		testee.getCredential().setPassword(
				genText(MIN_LENGTH_PASSWORD-1-symNum.length()) + symNum);
		assertEquals(
				String.format(MSG_TEMPALTE_INVALID_MINMAX_LENGTH, "passowrd", MIN_LENGTH_PASSWORD, MAX_LENGTH_PASSWORD),
				testee.validatePassoword());

		// Max length
		passwd = genText(MAX_LENGTH_PASSWORD-symNum.length()) + symNum;
		assertEquals(MAX_LENGTH_PASSWORD, passwd.length());
		testee.getCredential().setPassword(passwd);
		assertNull(testee.validatePassoword());

		testee.getCredential().setPassword(
				genText(MAX_LENGTH_PASSWORD+1-symNum.length()) + symNum);
		assertEquals(
				String.format(MSG_TEMPALTE_INVALID_MINMAX_LENGTH, "passowrd", MIN_LENGTH_PASSWORD, MAX_LENGTH_PASSWORD),
				testee.validatePassoword());

		// Weakness
		String cap = "A";
		String[] WEAK_PASSWD_PATTERNS = new String[] {
				genText(MIN_LENGTH_PASSWORD),
				genText(MIN_LENGTH_PASSWORD).toLowerCase() + sym,
				genText(MIN_LENGTH_PASSWORD).toLowerCase() + num,
				genText(MIN_LENGTH_PASSWORD).toLowerCase() + cap,
				genText(MIN_LENGTH_PASSWORD).toUpperCase() + sym,
				genText(MIN_LENGTH_PASSWORD).toUpperCase() + num,
				genText(MIN_LENGTH_PASSWORD).toUpperCase() + cap.toLowerCase(),
				
		};
		for(int i=0; i<WEAK_PASSWD_PATTERNS.length; i++) {
			testee.getCredential().setPassword(WEAK_PASSWD_PATTERNS[i]);
			assertEquals(MSG_TEMPALTE_INVALID_PASSWORD, testee.validatePassoword());
		}
		
		String[] STRONG_PASSWD_PATTERNS = new String[] {
				genText(MIN_LENGTH_PASSWORD).toLowerCase() + sym + num,
				genText(MIN_LENGTH_PASSWORD).toLowerCase() + sym + cap,
				genText(MIN_LENGTH_PASSWORD).toLowerCase() + num + cap,
				genText(MIN_LENGTH_PASSWORD).toUpperCase() + sym + num,
				genText(MIN_LENGTH_PASSWORD).toUpperCase() + sym + cap.toLowerCase(),
				genText(MIN_LENGTH_PASSWORD).toUpperCase() + num + cap.toLowerCase(),
		};
		for(int i=0; i<STRONG_PASSWD_PATTERNS.length; i++) {
			testee.getCredential().setPassword(STRONG_PASSWD_PATTERNS[i]);
			assertNull(testee.validatePassoword());
		}
		

	}
	
	@Test
	public void testChangeStatus() {
		User testee = new User();
		
		// Initial status
		assertEquals(false, testee.isActive());
		assertNull(testee.getStatus());
		
		// Activated
		testee.setActive(true);
		assertEquals(true, testee.isActive());
		assertEquals(User.INTERNAL_STATUS_ACTIVE, testee.getStatus());
		
		// In-activated by changing status
		testee.setStatus(User.INTERNAL_STATUS_INACTIVE);
		assertEquals(false, testee.isActive());
		assertEquals(User.INTERNAL_STATUS_INACTIVE, testee.getStatus());
		
		// Activated by changing status
		testee.setStatus(User.INTERNAL_STATUS_ACTIVE);
		assertEquals(true, testee.isActive());
		assertEquals(User.INTERNAL_STATUS_ACTIVE, testee.getStatus());

		// In-activated
		testee.setActive(false);
		assertEquals(false, testee.isActive());
		assertEquals(User.INTERNAL_STATUS_INACTIVE, testee.getStatus());

		testee.setStatus(null);
		assertEquals(false, testee.isActive());
		assertNull(testee.getStatus());
	}

	public static String genText(int len) {
		if(len<=0)
			return "";
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<len;i++)
			sb.append("a");
		return sb.toString();
	}

}

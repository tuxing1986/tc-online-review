package com.appirio.tech.core.service.identity.representation;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserTest {

	@Test
	public void test() {
		User user = new User();
		assertEquals(false, user.isActive());
		assertEquals(User.INTERNAL_STATUS_INACTIVE, user.getStatus());
		
		user.setActive(true);
		assertEquals(true, user.isActive());
		assertEquals(User.INTERNAL_STATUS_ACTIVE, user.getStatus());
		
		user.setStatus(User.INTERNAL_STATUS_INACTIVE);
		assertEquals(false, user.isActive());
		assertEquals(User.INTERNAL_STATUS_INACTIVE, user.getStatus());
		
		user.setStatus(User.INTERNAL_STATUS_ACTIVE);
		assertEquals(true, user.isActive());
		assertEquals(User.INTERNAL_STATUS_ACTIVE, user.getStatus());
	}

}

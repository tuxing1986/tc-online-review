package com.appirio.tech.core.permissions.util;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.auth.AuthUser;
import com.appirio.tech.core.permissions.PermissionsContext;
import com.appirio.tech.core.permissions.PermissionsEvaluator;
import com.appirio.tech.core.permissions.util.ExpressionParser.Expression;

public class PermissionsUtilTest {

	@Test
	public void testIsExpressionTrue() {

		Expression expr = new Expression("ownerId", "=", "<authUser>.userId");

		final AuthUser testUser = new AuthUser() {
			{
				setUserId(new TCID("test-user"));
			}
		};

		final TestResource res = new TestResource("test-user", "foo-id", "foo");

		PermissionsContext<TestResource> context = new PermissionsContext<TestResource>(Collections.emptyList(), testUser, null, new PermissionsEvaluator()) {

		};
		
		// test 1: positive test using the AuthUser from context
		assertThat(PermissionsUtil.isExpressionTrue(context, res, expr)).isTrue();

		// test 2: negative test using the AuthUser from context
		res.setOwnerId("not-test-user");
		assertThat(PermissionsUtil.isExpressionTrue(context, res, expr)).isFalse();

		// test 3: illegal reference object
		expr = new Expression("ownerId", "=", "<foo>.userId");
		try {
			PermissionsUtil.isExpressionTrue(context, res, expr);
			assertThat(true).isEqualTo(false);
		} catch (IllegalStateException ie) {
			// expected condition
		}

		// test 4: positive literal
		res.setOwnerId("test-user");
		expr = new Expression("ownerId", "=", "'test-user'");
		assertThat(PermissionsUtil.isExpressionTrue(context, res, expr)).isTrue();

		// test 5: negative literal
		expr = new Expression("ownerId", "=", "'not-test-user'");
		assertThat(PermissionsUtil.isExpressionTrue(context, res, expr)).isFalse();

		// test 6: positive literal no quotes
		res.setOwnerId("test-user");
		expr = new Expression("ownerId", "=", "test-user");
		assertThat(PermissionsUtil.isExpressionTrue(context, res, expr)).isTrue();

		// test 7: negative literal no quotes
		expr = new Expression("ownerId", "=", "not-test-user");
		assertThat(PermissionsUtil.isExpressionTrue(context, res, expr)).isFalse();
	}
	
	@Test
	public void testHasProperty() {
		TestResource res = new TestResource("o", "i", "n");
		assertThat(PermissionsUtil.hasProperty(res, "ownerId")).isTrue();
		
		assertThat(PermissionsUtil.hasProperty(res, "foo")).isFalse();
	}
	
	@Test
	public void testGetId() {
		TestResource res = new TestResource("o", "i", "n");
		assertThat(PermissionsUtil.getId(res)).isEqualTo(res.getId());
	}

	// must be public for the javabean property code in PermissionsUtil to work
	public static class TestResource {
		private String ownerId;
		private String id;
		private String name;

		public TestResource(String ownerId, String id, String name) {
			this.ownerId = ownerId;
			this.id = id;
			this.name = name;
		}

		public String getOwnerId() {
			return ownerId;
		}

		public void setOwnerId(String ownerId) {
			this.ownerId = ownerId;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
}

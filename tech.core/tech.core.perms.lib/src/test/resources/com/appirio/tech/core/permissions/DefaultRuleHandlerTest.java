package com.appirio.tech.core.permissions;

import java.util.Collections;

import org.junit.Test;

import com.appirio.tech.core.auth.AuthUser;
import com.appirio.tech.core.permissions.PermissionRule.RuleType;

import static org.fest.assertions.api.Assertions.assertThat;

public class DefaultRuleHandlerTest {

	@Test
	public void testHandlesRule() {
		DefaultRuleHandler<TestResource> handler = new DefaultRuleHandler<>();

		TestResource res = new TestResource("test-owner", "test-id", "test-user");
		PermissionsContext<TestResource> context = new PermissionsContext<>(Collections.emptyList(), new AuthUser(), null, new PermissionsEvaluator());
		
		// positive tests
		assertThat(handler.handlesRule(new PermissionRule(RuleType.APPLY_TO_ALL, null), context, res)).isTrue();
		assertThat(handler.handlesRule(new PermissionRule(RuleType.ID, null), context, res)).isTrue();
		assertThat(handler.handlesRule(new PermissionRule(RuleType.EXPR, "ownerId=<authUser>.userId"), context, res)).isTrue();
		
		// negative tests
		assertThat(handler.handlesRule(new PermissionRule(RuleType.CUSTOM, "blah"), context, res)).isFalse();
		assertThat(handler.handlesRule(new PermissionRule(RuleType.EXPR, "foo=<authUser>.userId"), context, res)).isFalse();
	}
	
	@Test
	public void testEvaluateRule() {
		DefaultRuleHandler<TestResource> handler = new DefaultRuleHandler<>();

		TestResource res = new TestResource("test-owner", "test-id", "test-user");
		PermissionsContext<TestResource> context = new PermissionsContext<>(Collections.emptyList(), new AuthUser(), null, new PermissionsEvaluator());
		
		assertThat(handler.evaluateRule(new PermissionRule(RuleType.APPLY_TO_ALL, null), context, res)).isTrue();
		assertThat(handler.evaluateRule(new PermissionRule(RuleType.ID, res.getId()), context, res)).isTrue();
		assertThat(handler.evaluateRule(new PermissionRule(RuleType.ID, "not-test-id"), context, res)).isFalse();
		assertThat(handler.evaluateRule(new PermissionRule(RuleType.EXPR, "ownerId='test-owner'"), context, res)).isTrue();
		assertThat(handler.evaluateRule(new PermissionRule(RuleType.EXPR, "ownerId='not-test-owner'"), context, res)).isFalse();
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

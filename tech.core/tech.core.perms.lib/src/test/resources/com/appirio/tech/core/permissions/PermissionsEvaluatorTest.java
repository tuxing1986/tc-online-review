package com.appirio.tech.core.permissions;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.auth.AuthUser;
import com.appirio.tech.core.permissions.PermissionRule.RuleType;
import com.google.common.collect.ImmutableList;

public class PermissionsEvaluatorTest {

	@Test
	public void testIsAllowed() {

		final PermissionsEvaluator eval = new PermissionsEvaluator();

		AuthUser user = new AuthUser() {
			{
				setUserId(new TCID("test-user"));
			}
		};

		List<PermissionsPolicy> policies = new ArrayList<PermissionsPolicy>();
		PermissionsPolicy permPol1 = new PermissionsPolicy();

		Policy pol1 = new Policy().putPermission(Permission.PERM_CREATE,
				new PermissionRules().addAllowRule(new PermissionRule(RuleType.APPLY_TO_ALL, null)));
		permPol1.setPolicy(pol1);

		policies.add(permPol1);

		PermissionsContext<?> context = new PermissionsContext<>(policies, user, ImmutableList.of(new DefaultRuleHandler<>()),
				new PermissionsEvaluator());

		// test 1: disallowed create permission
		assertThat(eval.isAllowed(new PermissionsContext<>(Collections.emptyList(), user, ImmutableList.of(new DefaultRuleHandler<>()),
				new PermissionsEvaluator()), null, Permission.PERM_CREATE)).isFalse();

		// test 2: allowed create permission
		assertThat(eval.isAllowed(context, null, Permission.PERM_CREATE)).isTrue();

		// test 3: disallow because deny precedence
		pol1.putPermission(Permission.PERM_CREATE, new PermissionRules().addDenyRule(new PermissionRule(RuleType.APPLY_TO_ALL, null)));
		assertThat(eval.isAllowed(context, null, Permission.PERM_CREATE)).isFalse();
	}
}

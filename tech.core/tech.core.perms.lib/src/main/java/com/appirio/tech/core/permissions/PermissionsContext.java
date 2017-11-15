package com.appirio.tech.core.permissions;

import java.util.List;

import com.appirio.tech.core.auth.AuthUser;

/**
 * Contains contextual information for permissions enforcement
 */
public class PermissionsContext<T> {

	private final List<PermissionsPolicy> policies;
	private final AuthUser authUser;
	private final List<RuleHandler<T>> ruleHandlers;
	private final PermissionsEvaluator evaluator;

	protected PermissionsContext(List<PermissionsPolicy> policies, AuthUser authUser, List<RuleHandler<T>> ruleHandlers,
			PermissionsEvaluator evaluator) {
		this.policies = policies;
		this.authUser = authUser;
		this.ruleHandlers = ruleHandlers;
		this.evaluator = evaluator;
	}

	public List<PermissionsPolicy> getPolicies() {
		return policies;
	}

	public AuthUser getAuthUser() {
		return authUser;
	}

	public List<RuleHandler<T>> getRuleHandlers() {
		return ruleHandlers;
	}

	public PermissionsEvaluator getEvaluator() {
		return evaluator;
	}

}

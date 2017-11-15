package com.appirio.tech.core.permissions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appirio.tech.core.api.v3.exception.APIRuntimeException;
import com.appirio.tech.core.auth.AuthUser;
import com.google.common.collect.ImmutableList;

public class PermissionsContextFactory {

	private static final Logger logger = LoggerFactory.getLogger(PermissionsContextFactory.class);

	public static <T> PermissionsContext<T> buildContext(PolicyServiceClient client, AuthUser user, String resourceType) {
		return buildContext(client, user, null, resourceType, ImmutableList.of(new DefaultRuleHandler<T>()), new PermissionsEvaluator());
	}
	
	public static <T> PermissionsContext<T> buildContext(PolicyServiceClient client, AuthUser user, String resourceType,
            PermissionsEvaluator evaluator) {
	    return buildContext(client, user, null, resourceType, ImmutableList.of(new DefaultRuleHandler<T>()), evaluator);
	}

    /**
     * Loads policies and builds a context object for permissions validation.
     * 
     * @param client
     *            A client to the policies service
     * @param user
     *            A valid user
     * @param forUserId
     *            (Optional) the user id of a different user to load policies
     *            for. This is intended for system user usage.
     * @param resourceType
     *            The type of resource to retrieve polices for (e.g, work,
     *            thread, collaboration, etc.)
     * @param ruleHandlers
     *            Permissions rule handlers for the given resource type
     * @param evaluator
     *            A permissions evaluator class
     * @return The context
     */
	public static <T> PermissionsContext<T> buildContext(PolicyServiceClient client, AuthUser user, String forUserId, String resourceType,
			List<RuleHandler<T>> ruleHandlers, PermissionsEvaluator evaluator) {
		try {
			List<PermissionsPolicy> policies = client.retrievePoliciesForResource(user.getToken(), resourceType, forUserId);
			return new PermissionsContext<T>(policies, user, ruleHandlers, evaluator);
		} catch (Exception e) {
			logger.error("Unable to retrieve security policies", e);
			throw new APIRuntimeException(500, "Unable to contact policies service", e);
		}
	}

}

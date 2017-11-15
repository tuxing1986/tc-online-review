package com.appirio.tech.core.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appirio.tech.core.api.v3.exception.APIRuntimeException;

public class PermissionsEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(PermissionsEvaluator.class);

    public <T> void evaluateAction(PermissionsContext<T> context, T resourceObject, String action) {
        evaluateAction(context, resourceObject, context.getRuleHandlers(), action);
    }

    public <T> void evaluateAction(PermissionsContext<T> context, T resourceObject, Collection<RuleHandler<T>> ruleHandlers,
            String action) {
        if (!isAllowed(context, resourceObject, ruleHandlers, action)) {
            throw new APIRuntimeException(403, "Action " + action + " is not allowed");
        }
    }

    public <T> boolean isAllowed(PermissionsContext<T> context, T resourceObject, String action) {
        return isAllowed(context, resourceObject, context.getRuleHandlers(), action);
    }

    public <T> boolean isAllowed(PermissionsContext<T> context, T resourceObject, Collection<RuleHandler<T>> ruleHandlers, String action) {
        logger.debug("checking permission {} on resource {}", action, resourceObject);

        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        if (resourceObject == null && !Permission.PERM_CREATE.equals(action)) {
            throw new IllegalArgumentException("resourceObject is required for all permissions except create");
        }
        if (ruleHandlers == null) {
            throw new IllegalArgumentException("ruleHandlers is required");
        }

        if (context.getPolicies() == null || context.getPolicies().isEmpty()) {
            logger.debug("no policies found");
            return false;
        }

        // pull out the applicable rules for the given action from the policies
        List<PermissionRule> allowRules = new ArrayList<>();
        List<PermissionRule> denyRules = new ArrayList<>();
        context.getPolicies().forEach(pol -> {
            logger.debug("found policy {}", pol.getId());
            final PermissionRules rules = pol.getPolicy().getPermissions().get(action);
            if (rules != null) {
                if (rules.getDenyRules() != null) {
                    denyRules.addAll(rules.getDenyRules());
                }

                if (rules.getAllowRules() != null) {
                    allowRules.addAll(rules.getAllowRules());
                }
            }
        });
        
        logger.debug("found {} deny rules and {} allow rules", denyRules.size(), allowRules.size());

        if (allowRules.isEmpty()) {
            logger.debug("No allow rules found for {}", action);
            return false;
        }

        // first check if there is an explicit denial
        if (!denyRules.isEmpty()) {
            for (RuleHandler<T> handler : ruleHandlers) {
                for (PermissionRule rule : denyRules) {
                    logger.debug("checking deny rule {}", rule);
                    if (handler.handlesRule(rule, context, resourceObject) && handler.isDenied(rule, context, resourceObject)) {
                        logger.debug("Action {} is explicitly denied", action);
                        return false;
                    }
                }
            }
        }

        // check for an explicit allow
        for (RuleHandler<T> handler : ruleHandlers) {
            for (PermissionRule rule : allowRules) {
                logger.debug("checking allow rule {}", rule);
                if (handler.handlesRule(rule, context, resourceObject) && handler.isAllowed(rule, context, resourceObject)) {
                    logger.debug("Action {} is allowed", action);
                    return true;
                }
            }
        }

        return false;
    }

}

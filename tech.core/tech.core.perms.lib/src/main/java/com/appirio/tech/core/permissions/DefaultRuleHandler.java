package com.appirio.tech.core.permissions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appirio.tech.core.permissions.PermissionRule.RuleType;
import com.appirio.tech.core.permissions.util.ExpressionParser;
import com.appirio.tech.core.permissions.util.PermissionsUtil;
import com.appirio.tech.core.permissions.util.ExpressionParser.Expression;

public class DefaultRuleHandler<T> implements RuleHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRuleHandler.class);
    
	@Override
	public boolean handlesRule(PermissionRule rule, PermissionsContext<T> context, T resourceObject) {
	    logger.debug("checking whether rule is handled");
		if (RuleType.APPLY_TO_ALL.equals(rule.getRuleType()) || RuleType.ID.equals(rule.getRuleType())) {
		    logger.debug("rule is handled for apply-to-all/id rule type");
			return true;
		} else if (RuleType.EXPR.equals(rule.getRuleType())) {
			if (resourceObject == null) {
				throw new IllegalArgumentException("Expression rule type requires a resource object");
			}
			final Expression expr = ExpressionParser.parse(rule.getRule());
			logger.debug("rule is an expression rule: {}", expr);
			final boolean handled = PermissionsUtil.hasProperty(resourceObject, expr.getCondition());
			logger.debug("does resource have property {}? = {}", expr.getCondition(), handled);
			return handled;
		}
		
		return false;
	}

	@Override
	public boolean isDenied(PermissionRule rule, PermissionsContext<T> context, T resourceObject) {
		return evaluateRule(rule, context, resourceObject);
	}

	@Override
	public boolean isAllowed(PermissionRule rule, PermissionsContext<T> context, T resourceObject) {
		return evaluateRule(rule, context, resourceObject);
	}
	
	protected boolean evaluateRule(PermissionRule rule, PermissionsContext<T> context, T resourceObject) {
	    logger.debug("evaluating rule {}", rule);
		if (RuleType.APPLY_TO_ALL.equals(rule.getRuleType())) {
		    logger.debug("rule applies to all: rule evaluates to true");
			return true;
		} else if (RuleType.ID.equals(rule.getRuleType())) {
			final String id = PermissionsUtil.getId(resourceObject);
			// here the rule value is interpreted as the literal id for comparison
			final boolean matches = id != null && id.equals(rule.getRule());
			logger.debug("ID match rule. resource id = {}, rule id = {}, match? = {}", id, rule.getRule(), matches);
			return matches;
		} else if (RuleType.EXPR.equals(rule.getRuleType())) {
			final Expression expr = ExpressionParser.parse(rule.getRule());
			final boolean isTrue = PermissionsUtil.isExpressionTrue(context, resourceObject, expr);
			logger.debug("expression evaluation = {}", isTrue);
			return isTrue;
		}
		
		return false;
	}

	
	
}

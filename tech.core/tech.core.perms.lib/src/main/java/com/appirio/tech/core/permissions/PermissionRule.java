package com.appirio.tech.core.permissions;

import com.google.common.base.MoreObjects;

/**
 * A rule that indicates how the permission is applied.
 */
public class PermissionRule {
	
	/**
	 * The type of rule:
	 * EXPR - expression. Currently a simple expression of field=value
	 * ID - this rule applies to a single ID
	 * APPLY_TO_ALL - this rule applies to instances
	 * CUSTOM - a custom rule implemented by the resource's service 
	 */
	public enum RuleType {
		EXPR, ID, APPLY_TO_ALL, CUSTOM
	}

	private RuleType ruleType;
	private String rule;
	
	public PermissionRule() {
		
	}
	
	public PermissionRule(RuleType ruleType, String rule) {
		this.ruleType = ruleType;
		this.rule = rule;
	}

	public RuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}

	public PermissionRule withRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
		return this;
	}

	/**
	 * The rule value
	 */
	public String getRule() {
		return rule;
	}

	/**
	 * The rule value
	 */
	public void setRule(String rule) {
		this.rule = rule;
	}

	public PermissionRule withRule(String rule) {
		this.rule = rule;
		return this;
	}
	
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("ruleType", ruleType).add("rule", rule).toString();
    }
}

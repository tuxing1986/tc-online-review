package com.appirio.tech.core.permissions;

/**
 * Interface for logic that implements evaluation of permission rules.
 *
 * @param <T> The type of resource this handler accepts
 */
public interface RuleHandler<T> {

    /**
     * Determines if this handler can process the given rule.
     * 
     * @param rule
     *            The rule to check
     * @param resourceObject
     *            The resource object to check. This may only be null when
     *            evaluating the create permission.
     * @return True if the rule can be handled; otherwise, false
     */
	public boolean handlesRule(PermissionRule rule, PermissionsContext<T> context, T resourceObject);
	
	/**
     * Determines if the given rule denies access.
     * 
     * @param rule
     *            The rule to check
     * @param resourceObject
     *            The resource object to check. This may only be null when
     *            evaluating the create permission.
     * @return True if access is denied; otherwise, false
     */
	public boolean isDenied(PermissionRule rule, PermissionsContext<T> context, T resourceObject);
	
	/**
     * Determines if the given rule grants access.
     * 
     * @param rule
     *            The rule to check
     * @param resourceObject
     *            The resource object to check. This may only be null when
     *            evaluating the create permission.
     * @return True if access is granted; otherwise, false
     */
	public boolean isAllowed(PermissionRule rule, PermissionsContext<T> context, T resourceObject);
	
}

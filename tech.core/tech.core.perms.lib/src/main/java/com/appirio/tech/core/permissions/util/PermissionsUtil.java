package com.appirio.tech.core.permissions.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.api.v3.exception.APIRuntimeException;
import com.appirio.tech.core.permissions.PermissionsContext;
import com.appirio.tech.core.permissions.util.ExpressionParser.Expression;

public class PermissionsUtil {

	private static final Logger logger = LoggerFactory.getLogger(PermissionsUtil.class);

	// support for condition value with a reference object (currently only
	// authUser) format is <authUser>.field for example, <authUser>.userId
	private static final Pattern REFERENCE_OBJ_PATTERN = Pattern.compile("^\\<(\\w+)\\>\\.(\\w+)$");
	// literals should be enclosed in single quotes
	private static final Pattern LITERAL_VALUE_PATTERN = Pattern.compile("^'([^']+)'$");

    /**
     * Evaluates the given resource to determine if an expression evaluates to
     * true.
     * 
     * @param context
     *            Contextual information with permission rules
     * @param resourceObject
     *            The resource to evaluate
     * @param expression
     *            The expression to evaluate
     * @return True if the expression is true; otherwise, false
     */
	public static boolean isExpressionTrue(PermissionsContext<?> context, Object resourceObject, Expression expression) {
	    if (resourceObject == null) {
	        throw new IllegalArgumentException("Resource object is required");
	    }
	    
		logger.debug("evaluating expression {}", expression);

		final Matcher exprMatcher = REFERENCE_OBJ_PATTERN.matcher(expression.getConditionValue());
		
		Object value;
		if (exprMatcher.matches()) {
			final String refObj = exprMatcher.group(1);
			if (!refObj.equals("authUser")) {
				logger.error("unsupported policy expression: {}", expression);
				throw new IllegalStateException("unsupported reference object in condition value");
			}
			value = getPropertyValue(context.getAuthUser(), exprMatcher.group(2), expression);
		} else {
			final Matcher literalMatcher = LITERAL_VALUE_PATTERN.matcher(expression.getConditionValue());
			value = literalMatcher.matches() ? literalMatcher.group(1) : expression.getConditionValue();
		}
		
		if (value != null && value instanceof TCID) {
			value = value.toString();
		}
		
		Object resourceValue = getPropertyValue(resourceObject, expression.getCondition(), expression);
		
		if (resourceValue != null && resourceValue instanceof TCID) {
			resourceValue = resourceValue.toString();
		}
		
		logger.debug("resource value = {}; expression value = {}", resourceValue, value);

		return resourceValue == null ? value == null : resourceValue.equals(value);
	}
	
	public static boolean hasProperty(Object bean, String property) {
		final boolean readable = PropertyUtils.isReadable(bean, property);
		if (!readable) {
			logger.warn("bean of type {} has no property {}", bean.getClass().getSimpleName(), property);
		}
		
		return readable;
	}
	
	public static String getId(Object bean) {
		try {
			final Object id = PropertyUtils.getSimpleProperty(bean, "id");
			// use toString in case the id is a TCID
			return id == null ? null : id.toString();
		} catch (Exception e) {
			logger.error("Unable to retrieve id property from bean", e);
			throw new APIRuntimeException(e);
		}
	}
	
	private static Object getPropertyValue(Object bean, String property, Expression expression) {
		try {
			return PropertyUtils.getProperty(bean, property);
		} catch (Exception e) {
			logger.error("Unable to get property value from expession {}. This is a misconfigured policy.", expression, e);
			throw new IllegalStateException("Invalid security policy configuration", e);
		}
	}
}

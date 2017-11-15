package com.appirio.tech.core.permissions.util;

public class ExpressionParser {

	public static Expression parse(String exprString) {
		// just simple = supported now
		int pos = exprString.indexOf('=');
		if (pos == -1) {
			throw new IllegalArgumentException("Invalid expression. Only equals expressions allowed");
		}
		return new Expression(exprString.substring(0, pos), "=", exprString.substring(pos+1));
	}
	
	public static class Expression {
		private final String condition;
		private final String operator;
		private final String conditionValue;

		Expression(String condition, String operator, String conditionValue) {
			this.condition = condition;
			this.operator = operator;
			this.conditionValue = conditionValue;
		}

		public String getCondition() {
			return condition;
		}

		public String getOperator() {
			return operator;
		}

		public String getConditionValue() {
			return conditionValue;
		}

		public String toString() {
			return condition + operator + conditionValue;
		}
	}
}

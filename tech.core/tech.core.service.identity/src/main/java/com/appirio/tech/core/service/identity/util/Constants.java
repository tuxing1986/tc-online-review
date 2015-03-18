package com.appirio.tech.core.service.identity.util;

import java.util.regex.Pattern;

public class Constants {

	public static final int MAX_LENGTH_EMAIL = 100;
	
	public static final int MAX_LENGTH_HANDLE = 15;

	public static final int MIN_LENGTH_HANDLE = 2;
	
    public static final int MAX_LENGTH_PASSWORD = 30;

    public static final int MIN_LENGTH_PASSWORD = 7;

	public static final int MAX_LENGTH_FIRST_NAME = 64;
	
	public static final int MAX_LENGTH_LAST_NAME = 64;

	public static final String ALPHABET_ALPHA_UPPER_EN = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static final String ALPHABET_ALPHA_LOWER_EN = "abcdefghijklmnopqrstuvwxyz";

	public static final String ALPHABET_ALPHA_EN = ALPHABET_ALPHA_LOWER_EN + ALPHABET_ALPHA_UPPER_EN;
	
	public static final String ALPHABET_WHITESPACE_EN = " \t\r\n";
	
	public static final String ALPHABET_DIGITS_EN = "0123456789";

	/*
    public static final String EMAIL_REGEX = "\\b(^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@([A-Za-z0-9-])+((\\.com)"
            + "|(\\.net)|(\\.org)|(\\.info)|(\\.edu)|(\\.mil)|(\\.gov)|(\\.biz)|(\\.ws)|(\\.us)|(\\.tv)|(\\.cc)"
            + "|(\\.aero)|(\\.arpa)|(\\.coop)|(\\.int)|(\\.jobs)|(\\.museum)|(\\.name)|(\\.pro)|(\\.travel)|(\\.nato)"
            + "|(\\..{2,3})|(\\.([A-Za-z0-9-])+\\..{2,3}))$)\\b";
    */
    public static final String EMAIL_REGEX = "(^[\\+_A-Za-z0-9-]+(\\.[\\+_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,}$))";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static final Pattern LOWER_CASE_PATTERN = Pattern.compile("[a-z]");

    public static final Pattern UPPER_CASE_PATTERN = Pattern.compile("[A-Z]");

    public static final Pattern SYMBOL_PATTERN = Pattern.compile("\\p{Punct}");

    public static final Pattern NUMBER_PATTERN = Pattern.compile("\\d");

	public final static String HANDLE_PUNCTUATION = "-_.{}[]";

	public final static String HANDLE_ALPHABET = ALPHABET_ALPHA_EN + ALPHABET_DIGITS_EN + HANDLE_PUNCTUATION;
	
	public static final String MSG_TEMPALTE_MANDATORY = "%s is required";
	
	public static final String MSG_TEMPALTE_INVALID_MAX_LENGTH = "Maximum length of %s is %d";
	
	public static final String MSG_TEMPALTE_INVALID_MINMAX_LENGTH = "Length of %s in character should be between %d and %d";

	public static final String MSG_TEMPALTE_INVALID_HANDLE_CONTAINS_SPACE = "The handle may not contain a space";
	
	public static final String MSG_TEMPALTE_INVALID_HANDLE_CONTAINS_FORBIDDEN_CHARS = "The handle may contain only letters, numbers and " + HANDLE_PUNCTUATION;

	public static final String MSG_TEMPALTE_INVALID_HANDLE_CONTAINS_ONLY_PUNCTUATION = "The handle may not contain only punctuation.";

	public static final String MSG_TEMPALTE_INVALID_HANDLE_STARTS_WITH_ADMIN = "Please choose another handle, not starting with admin.";

	public static final String MSG_TEMPLATE_DUPLICATED_HANDLE = "Handle '%s' has already been taken";
	
	public static final String MSG_TEMPALTE_INVALID_EMAIL = "Email address is invalid";

	public static final String MSG_TEMPLATE_DUPLICATED_EMAIL = "Email '%s' has already been registered, please use another one.";

	public static final String MSG_TEMPALTE_INVALID_PASSWORD = "Password is too week";
	
}

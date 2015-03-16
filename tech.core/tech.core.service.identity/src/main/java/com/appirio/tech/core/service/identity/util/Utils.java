package com.appirio.tech.core.service.identity.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.api.v3.exception.APIRuntimeException;
import com.topcoder.security.GeneralSecurityException;
import com.topcoder.security.Util;

public class Utils {
	
	static final Logger logger = Logger.getLogger(Utils.class);

	public static String encodePassword(String password, String alias) {
		try {
			return Util.encodePassword(password, alias);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e.getMessage(), e); //TODO:
		}
	}
	
	public static String decodePassword(String password, String alias) {
		try {
			return Util.decodePassword(password, alias);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e.getMessage(), e); //TODO:
		}
	}

	public static final int MAX_EMAIL_LENGTH = 100;
	
	public static final int MAX_HANDLE_LENGTH = 15;

	public static final int MIN_HANDLE_LENGTH = 2;
	
    public static final int MAX_PASSWORD_LENGTH = 30;

    public static final int MIN_PASSWORD_LENGTH = 7;

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

    private static final Pattern LOWER_CASE_PATTERN = Pattern.compile("[a-z]");

    private static final Pattern UPPER_CASE_PATTERN = Pattern.compile("[A-Z]");

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("\\p{Punct}");

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d");

	public final static String HANDLE_PUNCTUATION = "-_.{}[]";

	public final static String HANDLE_ALPHABET = ALPHABET_ALPHA_EN + ALPHABET_DIGITS_EN + HANDLE_PUNCTUATION;
	
	public static String validateHandle(String handle)  {

		final int handleLen = handle.length();
        if (handleLen > MAX_HANDLE_LENGTH || handleLen < MIN_HANDLE_LENGTH) {
            return "Length of handle in character should be between " + MIN_HANDLE_LENGTH
                + " and" + MAX_HANDLE_LENGTH;
        }
		if (handle.contains(" ")) {
			return "Handle may not contain a space";
		}
		if (!containsOnly(handle, HANDLE_ALPHABET, false)) {
			return "The handle may contain only letters, numbers and " + HANDLE_PUNCTUATION;
		}
		if (containsOnly(handle, HANDLE_PUNCTUATION, false)) {
			return "The handle may not contain only punctuation.";
		}
		if (handle.toLowerCase().trim().startsWith("admin")) {
			return "Please choose another handle, not starting with admin.";
		}
		if (checkInvalidHandle(handle)) {
			return "The handle you entered is not valid.";
		}
		return null;
	}
	
    public static String validateEmail(String email) {
        
        if (email.length() > MAX_EMAIL_LENGTH) {
            return "Maxiumum lenght of email address is " + MAX_EMAIL_LENGTH;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            return "Email address is invalid";
        }
        return null;
    }
	
    public static String validatePassword(String password){
    	if (password==null || password.length()==0)
            return "Password is required";
        
        final int passwordLen = password.length();
        if (passwordLen > MAX_PASSWORD_LENGTH || passwordLen < MIN_PASSWORD_LENGTH)
            return "Length of password should be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH;

        // length OK, check password strength.
        int strength = calculatePasswordStrength(password);
        switch (strength) {
        case -1:
            return "Password cannot end with a number";
        case 1:
        case 2:
        	return "Password is too weak";
        default:
            break;
        }
        return null;
    }
    
    public static int calculatePasswordStrength(String password) {
        int result = 0;
        password = password.trim();

        final int len = password.length();
        if (password.substring(len - 1).matches("\\d")) {
            return -1;
        }

        // Check if it has lower case characters.
        Matcher matcher = LOWER_CASE_PATTERN.matcher(password);
        if (matcher.find()) {
            result++;
        }

        // Check if it has upper case character.
        matcher = UPPER_CASE_PATTERN.matcher(password);
        if (matcher.find()) {
            result++;
        }

        // Check if it has punctuation symbol
        matcher = SYMBOL_PATTERN.matcher(password);
        if (matcher.find()) {
            result++;
        }

        // Check if it has number.
        matcher = NUMBER_PATTERN.matcher(password);
        if (matcher.find()) {
            result++;
        }
        return result;
    }
    
    private static final Pattern[] INVALID_HANDLE_PATTERNS = new Pattern[] {Pattern.compile("(.*?)es"),
        Pattern.compile("(.*?)s"), Pattern.compile("_*(.*?)_*")};
    
    private static boolean checkInvalidHandle(String handle) {
        if (checkExactMatch(handle)) {
            return true;
        }
        // check each pattern rule
        for (int i = 0; i < INVALID_HANDLE_PATTERNS.length; i++) {
            if (checkAgainstPattern(handle, INVALID_HANDLE_PATTERNS[i])) {
                return true;
            }
        }
        // check invalid word after removing some leading/trailing numbers
        return checkLeadingTrailingNumbers(handle);
    }
    
    private static boolean checkAgainstPattern(String handle, Pattern pattern) {
        Matcher matcher = pattern.matcher(handle);
        if (matcher.matches()) {
            String extractedHandle = matcher.group(1);
            if (!extractedHandle.equals(handle) && extractedHandle.length() > 0) {
                return checkExactMatch(extractedHandle);
            }
        }
        return false;
    }

	
    private static boolean checkLeadingTrailingNumbers(String handle) {
        int head = 0;
        // find heading and trailing digits count
        while (head < handle.length() && Character.isDigit(handle.charAt(head))) {
            head++;
        }
        if (head >= handle.length()) {
            head = handle.length() - 1;
        }
        int tail = handle.length() - 1;
        while (tail >= 0 && Character.isDigit(handle.charAt(tail))) {
            tail--;
        }
        if (tail < 0) {
            tail = 0;
        }
        // remove all possible heading and trailing digits
        for (int i = 0; i <= head; i++) {
            for (int j = handle.length(); j > tail && j > i; j--) {
                String extractedHandle = handle.substring(i, j);
                if (checkExactMatch(extractedHandle)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean checkExactMatch(String handle) {
    	return false; // true if handle is invalid
    }

    
	public static boolean containsOnly(String string, String alphabet, boolean wsAllowed) {
		int n = string.length();
		for (int i = 0; i < n; ++i) {
			char ch = string.charAt(i);
			int foundAt = alphabet.indexOf(ch);
			if (foundAt < 0) {
				if (wsAllowed) {
					if (ALPHABET_WHITESPACE_EN.indexOf(ch) >= 0) continue;
				}
				return false;
			}
		}
		return true;
	}
	
	public static Long toRawValue(TCID id) {
		if(id==null)
			return null;
		try {
			return Long.parseLong(id.getId());
		} catch (NumberFormatException e) {
			throw new APIRuntimeException("TCID["+id.getId()+"] can't be converted to long value.");
		}
	}
	public static boolean isValid(TCID id) {
		if(id==null)
			return false;
		try {
			toRawValue(id);
			return true;
		} catch (Exception e) {
			logger.debug("Utils#isValid() got an exception: "+e.getMessage(), e);
			return false;
		}
	}
}

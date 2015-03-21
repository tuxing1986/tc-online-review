package com.appirio.tech.core.service.identity.util;

import static com.appirio.tech.core.service.identity.util.Constants.ALPHABET_WHITESPACE_EN;
import static com.appirio.tech.core.service.identity.util.Constants.EMAIL_PATTERN;
import static com.appirio.tech.core.service.identity.util.Constants.HANDLE_ALPHABET;
import static com.appirio.tech.core.service.identity.util.Constants.HANDLE_PUNCTUATION;
import static com.appirio.tech.core.service.identity.util.Constants.LOWER_CASE_PATTERN;
import static com.appirio.tech.core.service.identity.util.Constants.MAX_LENGTH_EMAIL;
import static com.appirio.tech.core.service.identity.util.Constants.MAX_LENGTH_HANDLE;
import static com.appirio.tech.core.service.identity.util.Constants.MAX_LENGTH_PASSWORD;
import static com.appirio.tech.core.service.identity.util.Constants.MIN_LENGTH_HANDLE;
import static com.appirio.tech.core.service.identity.util.Constants.MIN_LENGTH_PASSWORD;
import static com.appirio.tech.core.service.identity.util.Constants.NUMBER_PATTERN;
import static com.appirio.tech.core.service.identity.util.Constants.SYMBOL_PATTERN;
import static com.appirio.tech.core.service.identity.util.Constants.UPPER_CASE_PATTERN;

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


	public static String validateHandle(String handle)  {

		final int handleLen = handle.length();
        if (handleLen > MAX_LENGTH_HANDLE || handleLen < MIN_LENGTH_HANDLE) {
            return "Length of handle in character should be between " + MIN_LENGTH_HANDLE
                + " and" + MAX_LENGTH_HANDLE;
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
        
        if (email.length() > MAX_LENGTH_EMAIL) {
            return "Maxiumum lenght of email address is " + MAX_LENGTH_EMAIL;
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
        if (passwordLen > MAX_LENGTH_PASSWORD || passwordLen < MIN_LENGTH_PASSWORD)
            return "Length of password should be between " + MIN_LENGTH_PASSWORD + " and " + MAX_LENGTH_PASSWORD;

        // length OK, check password strength.
        int strength = calculatePasswordStrength(password);
        switch (strength) {
        case 0:
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

package com.appirio.tech.core.service.identity.util.auth;

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.api.v3.util.jwt.InvalidTokenException;
import com.appirio.tech.core.api.v3.util.jwt.JWTToken;
import com.appirio.tech.core.auth.AuthUser;

public class OneTimeToken extends JWTToken {
	
	public OneTimeToken(String token, String domain, String secret) {
		super(token, secret);
		if(!isValidIssuerFor(domain))
			throw new InvalidTokenException(token, "Valid credentials are required.", null);
	}
	
	public AuthUser getAuthUser() {
    	final TCID uid = new TCID(getUserId());
        return new AuthUser() {
        	@Override public TCID getUserId() { return uid; }
        };
    }
}
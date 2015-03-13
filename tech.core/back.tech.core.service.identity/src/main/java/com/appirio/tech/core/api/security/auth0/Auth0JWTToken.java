package com.appirio.tech.core.api.security.auth0;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * {@link AbstractAuthenticationToken} from Auth0 jwt token.
 */
@SuppressWarnings("serial")
public class Auth0JWTToken extends AbstractAuthenticationToken {

	private final String jwt;
	private Auth0UserDetails principal;

	public Auth0JWTToken(String jwt) {
		super(null);
		this.jwt = jwt;
		setAuthenticated(false);
	}

	public String getJwt() {
		return jwt;
	}

	public Object getCredentials() {
		return null;
	}

	public Object getPrincipal() {
		return principal;
	}

	public void setPrincipal(Auth0UserDetails principal) {
		this.principal = principal;
	}

	@SuppressWarnings("unchecked")
	@Override
    public Collection<GrantedAuthority> getAuthorities() {
		return (Collection<GrantedAuthority>) principal.getAuthorities();
     }

}

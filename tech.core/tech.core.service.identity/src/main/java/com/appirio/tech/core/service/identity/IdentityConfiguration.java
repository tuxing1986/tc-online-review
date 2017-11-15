package com.appirio.tech.core.service.identity;

import io.dropwizard.db.DataSourceFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.appirio.tech.core.api.v3.dropwizard.APIBaseConfiguration;
import com.appirio.tech.core.service.identity.util.auth.Auth0Client;
import com.appirio.tech.core.service.identity.util.auth.ServiceAccountAuthenticatorFactory;
import com.appirio.tech.core.service.identity.util.cache.CacheServiceFactory;
import com.appirio.tech.core.service.identity.util.event.EventSystemFactory;
import com.appirio.tech.core.service.identity.util.ldap.LDAPServiceFactory;
import com.appirio.tech.core.service.identity.util.shiro.Shiro;
import com.appirio.tech.core.service.identity.util.store.AuthDataStoreFactory;
import com.appirio.tech.core.service.identity.util.zendesk.ZendeskFactory;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IdentityConfiguration extends APIBaseConfiguration {

	@Valid
	@JsonProperty
	private CacheServiceFactory cache = new CacheServiceFactory();
	
	@Valid
	@JsonProperty
	private AuthDataStoreFactory authStore = new AuthDataStoreFactory();
	
	@Valid
	@JsonProperty
	private DataSourceFactory database = new DataSourceFactory();

	@Valid
	@JsonProperty
	private Auth0Client auth0 = new Auth0Client();
	
	@Valid
	@NotNull
	@JsonProperty
	private EventSystemFactory eventSystem = new EventSystemFactory();
	
	@Valid
	@JsonProperty
	private LDAPServiceFactory ldap = new LDAPServiceFactory();
	
	@Valid
	@NotNull
	@JsonProperty
	private Shiro shiroSettings;
	
	@Valid
	@NotNull
	@JsonProperty
	private DataSourceFactory authorizationDatabase = new DataSourceFactory();

	@JsonProperty	
	private ServiceAccountAuthenticatorFactory serviceAccount;
	
	@Valid
	@JsonProperty
	private ZendeskFactory zendesk = new ZendeskFactory();

	@JsonProperty
	private Map<String, Object> context = new LinkedHashMap<String, Object>();
		
	
	public DataSourceFactory getDataSourceFactory() {
		return database;
	}
	
	public CacheServiceFactory getCache() {
		return cache;
	}

	public AuthDataStoreFactory getAuthStore() {
		return authStore;
	}
	
	public Auth0Client getAuth0() {
		return auth0;
	}
	
	public LDAPServiceFactory getLdap() {
		return ldap;
	}
	
	public EventSystemFactory getEventSystem() {
		return eventSystem;
	}

	public Shiro getShiroSettings() {
		return shiroSettings;
	}

	public DataSourceFactory getAuthorizationDatabase() {
		return authorizationDatabase;
	}

	public ServiceAccountAuthenticatorFactory getServiceAccount() {
		return serviceAccount;
	}
	
	public ZendeskFactory getZendesk() {
		return this.zendesk;
	}

	public Map<String, Object> getContext() {
		return context;
	}
}

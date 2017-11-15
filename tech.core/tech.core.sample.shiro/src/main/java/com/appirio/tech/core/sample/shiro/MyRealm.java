package com.appirio.tech.core.sample.shiro;

import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class MyRealm extends AuthorizingRealm {
	
	    @Override
	    protected AuthenticationInfo doGetAuthenticationInfo (AuthenticationToken token) {
			return null;
	    }

	    @Override
	    protected AuthorizationInfo doGetAuthorizationInfo (PrincipalCollection principals) {
	         //final String username = "user2";
	    	 SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
	    	 Set<String> roles = new HashSet<String>();
	    	 roles.add("Copilot");
	    	 authorizationInfo.setRoles(roles);
	         return authorizationInfo;
	    }
}

package com.appirio.tech.core.sample.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

import com.appirio.tech.core.sample.representation.ShiroRepresentation;


@Path("/shiro")
@Produces(MediaType.APPLICATION_JSON)
public class SampleShiroResource  {
	private final String iniConfig;
	
	public SampleShiroResource(String iniConfig) {
		this.iniConfig = iniConfig;	
	}
	
	@GET
	public ShiroRepresentation sayMessage() {
		boolean isPermitted;
		
		Subject currentUser = SecurityUtils.getSubject();
		
		if (currentUser.hasRole("Copilot")) {
            isPermitted = true;
        } else {
            isPermitted = false;
        }
		
		
		Object userId = "user2";
        String realmName = "samplerealm";
        PrincipalCollection principals = new SimplePrincipalCollection(userId, realmName);
        Subject currentUser1 = new Subject.Builder(SecurityUtils.getSecurityManager()).principals(principals).buildSubject();
        
        if (currentUser1.hasRole("Copilot")) {
            isPermitted = true;
        } else {
            isPermitted = false;
        }
        
		return new ShiroRepresentation(iniConfig, currentUser1.toString(), isPermitted);
	}
}

package com.appirio.tech.core.service.identity.util.ldap;

import org.apache.log4j.Logger;

import com.topcoder.security.ldap.LDAPClient;
import com.topcoder.security.ldap.LDAPClientException;
import com.topcoder.security.ldap.LDAPConstants;

public class LDAPService {

	Logger logger = Logger.getLogger(LDAPService.class);
	
	public LDAPService() {
	}
	
	/**
	 * CAUTION: Properties configured with this method are shared by all instances of LDAPService in the same VM.
	 * @param host
	 * @param port
	 * @param bindDN
	 * @param bindPassword
	 */
	public LDAPService(String host, int port, String bindDN, String bindPassword) {
		LDAPConstants.HOST = host;
		LDAPConstants.PORT = port;
		LDAPConstants.BIND_DN = bindDN;
		LDAPConstants.BIND_PASSWORD = bindPassword;
	}
	
	public void registerMember(Long userId, String handle, String password) {
		registerMember(userId, handle, password, MemberStatus.UNACTIVATED);
	}
	
	public void registerMember(Long userId, String handle, String password, MemberStatus status) {
		if(userId == null)
			throw new IllegalArgumentException("Invalid parameter: userId must be specified.");
		if(handle == null || handle.length()==0)
			throw new IllegalArgumentException("Invalid parameter: handle must be specified.");
		
		LDAPClient ldapClient = new LDAPClient();
        try {
            ldapClient.connect();
        	MemberStatus s = status!=null ? status : MemberStatus.UNACTIVATED;
            ldapClient.addTopCoderMemberProfile(userId, handle, password, s.toInternalLabel());
            logger.info("[LDAP] User " + userId + " has been registered. (handle=\"" + handle + "\", status=\"" + s.toInternalLabel());
        } catch (LDAPClientException e) {
        	logger.error(e);
        	throw new RuntimeException("Failed to register member. (id:"+userId+",handle:"+handle, e);
        } finally {
            try {
            	if(ldapClient.isConnected())
            		ldapClient.disconnect();
            } catch (Exception e) {
                logger.error(e);
            }
        }

	}
}

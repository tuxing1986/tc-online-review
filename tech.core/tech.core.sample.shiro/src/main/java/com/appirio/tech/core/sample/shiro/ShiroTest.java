package com.appirio.tech.core.sample.shiro;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ShiroTest{

    private static final transient Logger log = LoggerFactory.getLogger(ShiroTest.class);

    public static void main(String[] args) {

    	// Setup Apache Shiro Security Manager
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        
        // Get current logged in User
        //Subject currentUser = SecurityUtils.getSubject();
        
        // Creating a subject manually using realm
        Object userId = "user2";
        String realmName = "myRealm";
        PrincipalCollection principals = new SimplePrincipalCollection(userId, realmName);
        Subject currentUser = new Subject.Builder().principals(principals).buildSubject();
        
        //currentUser.runAs(principals);
        //currentUser.releaseRunAs();
        
        log.info("User is " + currentUser.toString());
        
        if (currentUser.hasRole("Copilot")) {
            log.info("User1 has role Copilot");
        } else {
            log.info("User1 is not Copilot");
        }

        // Check whether the currentUser is Authenticated
        if (!currentUser.isAuthenticated()) {
            UsernamePasswordToken token = new UsernamePasswordToken("user1", "member123");
            token.setRememberMe(true);
            try {
            	// try to login 
                currentUser.login(token);
            } catch (UnknownAccountException uae) {
                log.info("There is no user with username of " + token.getPrincipal());
            } catch (IncorrectCredentialsException ice) {
                log.info("Password for account " + token.getPrincipal() + " was incorrect!");
            } catch (LockedAccountException lae) {
                log.info("The account for username " + token.getPrincipal() + " is locked.  " +
                        "Please contact your administrator to unlock it.");
            }
            catch (AuthenticationException ae) {
                //unexpected condition
            }
        }
        else {
        	// Current User is authenticated
        	log.info("User [" + currentUser.getPrincipal() + "] already logged in.");
        }
        
        log.info("User [" + currentUser.getPrincipal() + "] logged in successfully.");
       
        if (currentUser.hasRole("Member")) {
            log.info("User1 has role Member");
        } else {
            log.info("User1 is not Member");
        }

        if (currentUser.isPermitted("update")) {
            log.info("You can update.");
        } else {
            log.info("Sorry, You are not permitted to update.");
        }

        
        if (currentUser.isPermitted("Challenge:delete:C1")) {
            log.info("You are permitted to delete Challenge C1");
        } else {
            log.info("Sorry, you aren't allowed to delete C1 Challenge");
        }

        // logout
        currentUser.logout();

        System.exit(0);
    }
}


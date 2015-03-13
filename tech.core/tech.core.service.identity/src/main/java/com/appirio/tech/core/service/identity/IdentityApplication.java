/**
 * 
 */
package com.appirio.tech.core.service.identity;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import org.skife.jdbi.v2.DBI;

import com.appirio.tech.core.api.v3.dropwizard.APIApplication;
import com.appirio.tech.core.api.v3.util.jdbi.TCIDArgumentFactory;
import com.appirio.tech.core.service.identity.ctrl.CallbackWebflowCtrl;
import com.appirio.tech.core.service.identity.ctrl.LoginWebflowCtrl;
import com.appirio.tech.core.service.identity.dao.UserDAO;
import com.appirio.tech.core.service.identity.resource.UserResource;
import com.appirio.tech.core.service.identity.util.idgen.SequenceDAO;
import com.appirio.tech.core.service.identity.util.ldap.LDAPService;

/**
 * Identity Service Application
 * Created for quick WebFlow authentication
 * 
 * @author sudo
 *
 */
public class IdentityApplication extends APIApplication<IdentityConfiguration> {

	@Override
	public void initialize(Bootstrap<IdentityConfiguration> bootstrap) {
		super.initialize(bootstrap);
		/**
		 * Temporary assigning /pub servlet instead of pure html
		 */
		bootstrap.addBundle(new AssetsBundle("/pub", "/pub"));
		bootstrap.addBundle(new ViewBundle());
	}
	
	@Override
	public void run(IdentityConfiguration configuration, Environment environment) throws Exception {
		super.run(configuration, environment);
		
		final DBIFactory factory = new DBIFactory();
		final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "common_oltp");
		jdbi.registerArgumentFactory(new TCIDArgumentFactory());
		
		final UserDAO userDao = jdbi.onDemand(UserDAO.class);
		
		final SequenceDAO seqDao = jdbi.onDemand(SequenceDAO.class);
		userDao.setSequenceDao(seqDao);
		
		LDAPService ldapService = new LDAPService();
		userDao.setLdapService(ldapService);
		
		// users endpoint
		UserResource userResource = new UserResource(userDao, seqDao);
		environment.jersey().register(userResource);
		
		// login endpoint
		LoginWebflowCtrl ctrl = new LoginWebflowCtrl();
		environment.jersey().register(ctrl);
		// callback endpoint
		CallbackWebflowCtrl callbackWebflowCtrl = new CallbackWebflowCtrl();
		environment.jersey().register(callbackWebflowCtrl);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		new IdentityApplication().run(args);
	}

}

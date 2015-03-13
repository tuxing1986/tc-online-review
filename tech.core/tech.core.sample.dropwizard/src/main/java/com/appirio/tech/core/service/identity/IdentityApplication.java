/**
 * 
 */
package com.appirio.tech.core.service.identity;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import com.appirio.tech.core.api.v3.dropwizard.APIApplication;
import com.appirio.tech.core.api.v3.dropwizard.APIBaseConfiguration;

/**
 * Identity Service Application
 * Created for quick WebFlow authentication
 * 
 * @author sudo
 *
 */
public class IdentityApplication extends APIApplication {

	@Override
	public void initialize(Bootstrap<APIBaseConfiguration> bootstrap) {
		super.initialize(bootstrap);
		/**
		 * Temporary assigning /pub servlet instead of pure html
		 */
		bootstrap.addBundle(new AssetsBundle("/pub", "/public"));
		bootstrap.addBundle(new ViewBundle());
	}
	
	@Override
	public void run(APIBaseConfiguration configuration, Environment environment) throws Exception {
		super.run(configuration, environment);
		environment.jersey().setUrlPattern("/v3a/*");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		new IdentityApplication().run(args);
	}

}

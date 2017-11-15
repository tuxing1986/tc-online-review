/**
 * 
 */
package com.appirio.tech.core.sample.app;

import org.apache.log4j.Logger;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import com.appirio.tech.core.api.v3.dropwizard.APIApplication;
import com.appirio.tech.core.api.v3.dropwizard.APIBaseConfiguration;

/**
 * @author sudo
 *
 */
public class SampleApplication extends APIApplication<APIBaseConfiguration> {

	private static final Logger logger = Logger.getLogger(SampleApplication.class);
	
	@Override
	public void initialize(Bootstrap<APIBaseConfiguration> bootstrap) {
		super.initialize(bootstrap);
		bootstrap.addBundle(new AssetsBundle("/pub", "/sample"));
		bootstrap.addBundle(new ViewBundle());
	}
	
	@Override
	public void run(APIBaseConfiguration configuration, Environment environment) throws Exception {
		super.run(configuration, environment);

		/*
		// login endpoint
		LoginWebflowCtrl ctrl = new LoginWebflowCtrl();
		environment.jersey().register(ctrl);
		*/
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		new SampleApplication().run(args);
	}

}

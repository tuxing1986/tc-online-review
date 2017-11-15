package com.appirio.tech.core.sample.app;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;

import com.appirio.tech.core.api.v3.dropwizard.APIApplication;
import com.appirio.tech.core.sample.resource.SampleShiroResource;

public class SampleShiroApplication extends APIApplication<ShiroConfiguration>{
	
	@Override
	public void initialize(Bootstrap<ShiroConfiguration> bootstrap) {
		super.initialize(bootstrap);
		bootstrap.addBundle(new AssetsBundle("/pub", "/pub"));
		bootstrap.addBundle(new ViewBundle());
	}
	
	@Override
	public void run(ShiroConfiguration configuration, Environment environment) throws Exception {
		super.run(configuration, environment);
		
		Factory<SecurityManager> factory = new IniSecurityManagerFactory(configuration.getIniConfig());
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
  
        final SampleShiroResource resource = new SampleShiroResource(configuration.getIniConfig());
        environment.jersey().register(resource);	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		new SampleShiroApplication().run(args);
	}

}
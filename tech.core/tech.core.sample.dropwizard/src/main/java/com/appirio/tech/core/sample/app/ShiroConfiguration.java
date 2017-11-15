package com.appirio.tech.core.sample.app;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.appirio.tech.core.api.v3.dropwizard.APIBaseConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ShiroConfiguration extends APIBaseConfiguration {
		@Valid
		@NotNull
		@JsonProperty
		private String iniConfig = new String();
		
		@Valid
		@NotNull
		@JsonProperty
		private boolean useShiroAuthorization;
		
		public String getIniConfig() {
			return iniConfig;
		}

		public boolean isUseShiroAuthorization() {
			return useShiroAuthorization;
		}

}

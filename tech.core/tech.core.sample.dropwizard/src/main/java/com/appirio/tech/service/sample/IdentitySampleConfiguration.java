/**
 * 
 */
package com.appirio.tech.service.sample;

import io.dropwizard.Configuration;

import org.hibernate.validator.constraints.NotEmpty;

import com.appirio.tech.service.sample.core.Template;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author sudo
 * 
 */
public class IdentitySampleConfiguration extends Configuration {
	@NotEmpty
	private String template;

	@NotEmpty
	private String defaultName = "Stranger";

	@JsonProperty
	public String getTemplate() {
		return template;
	}

	@JsonProperty
	public void setTemplate(String template) {
		this.template = template;
	}

	@JsonProperty
	public String getDefaultName() {
		return defaultName;
	}

	@JsonProperty
	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	public Template buildTemplate() {
		return new Template(template, defaultName);
	}

}

package com.appirio.tech.core.service.identity;

import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.appirio.tech.core.api.v3.dropwizard.APIBaseConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IdentityConfiguration extends APIBaseConfiguration {

	@Valid
	@NotNull
	@JsonProperty
	private DataSourceFactory database = new DataSourceFactory();

	public DataSourceFactory getDataSourceFactory() {
		return database;
	}
}

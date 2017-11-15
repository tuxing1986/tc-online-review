package com.appirio.tech.core.permissions;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.appirio.tech.core.api.v3.exception.APIRuntimeException;
import com.appirio.tech.core.api.v3.response.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.WebResource;

public class PolicyServiceClient {

	private static final TypeReference<List<PermissionsPolicy>> POLICY_LIST_TYPE = new TypeReference<List<PermissionsPolicy>>() {
	}; 

	private final ObjectMapper objectMapper;
	private final WebResource rootResource;

    /**
     * Builds a client instance.
     * 
     * @param mapper
     *            A mapper that can serialize/deserialze PermissionsPolicy.
     * @param policiesResource
     *            A jersey client resource reference to /v3/policies. This is
     *            constructed from a jersey client instance. To get a client
     *            instance from dropwizard, use the JerseyClientBuilder class.
     *            See http://www.dropwizard.io/0.7.1/docs/manual/client.html for
     *            details.
     */
	public PolicyServiceClient(ObjectMapper mapper, WebResource policiesResource) {
		this.objectMapper = mapper;
		this.rootResource = policiesResource;
	}

	private WebResource.Builder buildRequest(WebResource resource, String jwt) {
		return resource.accept(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "Bearer " + jwt);
	}

	public List<PermissionsPolicy> retrievePoliciesForResource(String jwt, String resource, String userId) throws IOException {
		if (Strings.isNullOrEmpty(resource)) {
			throw new IllegalArgumentException("resource is required");
		}

		WebResource res = rootResource.queryParam("resource", resource);
		if (!Strings.isNullOrEmpty(userId)) {
		    res = res.queryParam("userId", userId);
		}
		ApiResponse response = buildRequest(res, jwt).get(ApiResponse.class);
		if (!response.getResult().getSuccess()) {
			throw new APIRuntimeException(response.getResult().getStatus(), response.getResult().getContent().toString());
		}

		return objectMapper.convertValue(response.getResult().getContent(), POLICY_LIST_TYPE);
	}
	
	public String createPolicy(String jwt, PermissionsPolicy policy) throws IOException {
		ApiResponse response = buildRequest(rootResource, jwt).post(ApiResponse.class, buildV3Wrapper(policy));
		if (!response.getResult().getSuccess()) {
			throw new APIRuntimeException(response.getResult().getStatus(), response.getResult().getContent().toString());
		}
		
		// expecting id to be returned
		return (String)response.getResult().getContent();
	}
	
	public PermissionsPolicy updatePolicy(String jwt, PermissionsPolicy policy) throws IOException {
		WebResource res = rootResource.path(policy.getId());
		ApiResponse response = buildRequest(res, jwt).put(ApiResponse.class, buildV3Wrapper(policy));
		if (!response.getResult().getSuccess()) {
			throw new APIRuntimeException(response.getResult().getStatus(), response.getResult().getContent().toString());
		}
		
		return objectMapper.convertValue(response.getResult().getContent(), PermissionsPolicy.class);
	}
	
	private Map<String, Object> buildV3Wrapper(Object obj) {
		return ImmutableMap.of("param", obj);
	}

	// manual testing
//	public static void main(String[] args) throws Exception {
//
//		final ObjectMapper mapper = new ObjectMapper();
//		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		mapper.enable(SerializationFeature.INDENT_OUTPUT);
//		// joda datetime handling
//		JodaModule jodaModule = new JodaModule();
//		jodaModule.addSerializer(DateTime.class,
//				new DateTimeSerializer(new JacksonJodaDateFormat(ISODateTimeFormat.dateTime().withZoneUTC())));
//		mapper.registerModule(jodaModule);
//		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//
//		final String jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJUb3Bjb2RlciBVc2VyIiwiQ29ubmVjdCBDb3BpbG90Il0sImlzcyI6Imh0dHBzOi8vYXBpLnRvcGNvZGVyLWRldi5jb20iLCJoYW5kbGUiOiJqYW1lc2RldjMiLCJleHAiOjE0NTQ0MzcwMjAsInVzZXJJZCI6IjQwMTQxMzk0IiwiaWF0IjoxNDU0NDM2NDIwLCJlbWFpbCI6ImphbWVzK2RldjNAYXBwaXJpby5jb20iLCJqdGkiOiJhMjkxYjQyNy02NTA1LTQwOWQtYTliYS03ZTZjYTVkZmJhZWUifQ.HrSLDWm8mQDgIEPvE2liIGOf3EN4JAEtK7Q7qzSrC1E";
//		PolicyServiceClient client = new PolicyServiceClient(mapper, "http://localhost:8080/v3/policies", null);
//
//		List<PermissionsPolicy> policies = client.retrievePoliciesForResource(jwt, "work1");
//
//		System.out.println(policies.size() + " resource policies");
//
//	}

}

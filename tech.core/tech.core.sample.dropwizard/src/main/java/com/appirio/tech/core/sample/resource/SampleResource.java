/**
 * 
 */
package com.appirio.tech.core.sample.resource;

import io.dropwizard.auth.Auth;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.api.v3.metadata.CountableMetadata;
import com.appirio.tech.core.api.v3.metadata.Metadata;
import com.appirio.tech.core.api.v3.request.FieldSelector;
import com.appirio.tech.core.api.v3.request.OrderByQuery;
import com.appirio.tech.core.api.v3.request.PostPutRequest;
import com.appirio.tech.core.api.v3.request.QueryParameter;
import com.appirio.tech.core.api.v3.request.SortOrder;
import com.appirio.tech.core.api.v3.request.annotation.APIFieldParam;
import com.appirio.tech.core.api.v3.request.annotation.APIQueryParam;
import com.appirio.tech.core.api.v3.resource.DDLResource;
import com.appirio.tech.core.api.v3.resource.GetResource;
import com.appirio.tech.core.api.v3.response.ApiResponse;
import com.appirio.tech.core.api.v3.response.ApiResponseFactory;
import com.appirio.tech.core.auth.AuthUser;
import com.appirio.tech.core.sample.exception.StorageException;
import com.appirio.tech.core.sample.representation.Sample;
import com.appirio.tech.core.sample.storage.InMemoryUserStorage;
import com.codahale.metrics.annotation.Timed;

/**
 * @author sudo
 *
 */
@Path("apisamples")
@Produces(MediaType.APPLICATION_JSON)
public class SampleResource implements GetResource<Sample>, DDLResource<Sample> {

	private InMemoryUserStorage storage = InMemoryUserStorage.instance();
	
	@Override
	@GET
	@Path("/{resourceId}")
	@Timed
	public ApiResponse getObject(
			@Auth AuthUser authUser,
			@PathParam("resourceId") TCID recordId,
			@APIFieldParam(repClass = Sample.class) FieldSelector selector,
			@Context HttpServletRequest request)
			throws Exception {

		for(Sample user : storage.getUserList()) {
			if(user.getId().equals(recordId)) {
				return ApiResponseFactory.createFieldSelectorResponse(user, selector);
			}
		}
		return null;
	}

	@Override
	@GET
	@Timed
	public ApiResponse getObjects(
			@Auth AuthUser authUser,
			@APIQueryParam(repClass = Sample.class) QueryParameter query,
			@Context HttpServletRequest request) throws Exception {
		
		//obtain filtered list from storage
		List<Sample> resultList = storage.getFilteredUserList(query.getFilter());
		
		//sort the list on order_by
		if(query.getOrderByQuery().getOrderByField()!=null) {
			sortList(query.getOrderByQuery(), resultList);
		}
		
		//handle limit and offset
		if(query.getLimitQuery().getOffset() != null) {
			resultList = resultList.subList(query.getLimitQuery().getOffset(), resultList.size());
		}
		if(query.getLimitQuery().getLimit() != null && query.getLimitQuery().getLimit()<resultList.size()) {
			resultList = resultList.subList(0, query.getLimitQuery().getLimit());
		}
		
		//return with additional attributes
		return ApiResponseFactory.createFieldSelectorResponse(resultList, getMetadata(request, query), query.getSelector());
	}


	@Override
	@POST
	@Timed
	public ApiResponse createObject(
			@Auth AuthUser authUser,
			@Valid PostPutRequest<Sample> postRequest,
			@Context HttpServletRequest request)
			throws Exception {
		Sample object = postRequest.getParam();
		object.setCreatedAt(new DateTime());
		object.setModifiedAt(new DateTime());
		return ApiResponseFactory.createResponse(storage.insertUser(object).getId());
	}

	@Override
	@PUT
	@Path("/{resourceId}")
	@Timed
	public ApiResponse updateObject(
			@Auth AuthUser authUser,
			@PathParam("resourceId") String resourceId,
			@Valid PostPutRequest<Sample> putRequest,
			@Context HttpServletRequest request) throws Exception {
		Sample object = putRequest.getParam();
		object.setModifiedAt(new DateTime());
		storage.updateUser(object);
		return ApiResponseFactory.createResponse(object.getId());
	}

	@Override
	@DELETE
	@Path("/{resourceId}")
	@Timed
	public ApiResponse deleteObject(
			@Auth AuthUser authUser,
			@PathParam("resourceId") String resourceId,
			@Context HttpServletRequest request)
			throws Exception {
		storage.deleteUser(new TCID(resourceId));
		return ApiResponseFactory.createResponse(new TCID(resourceId));
	}

	public Metadata getMetadata(HttpServletRequest request, QueryParameter query) throws Exception {
		CountableMetadata metadata = new CountableMetadata();
		metadata.setTotalCount(storage.getFilteredUserList(query.getFilter()).size());
		//populateFieldInfo(metadata);
		return metadata;
	}

	/**
	 * Sorts the given resultList from query's orderby.
	 * 
	 * @param query
	 * @param resultList
	 */
	private void sortList(OrderByQuery orderQuery, List<Sample> resultList) {
		String orderField = orderQuery.getOrderByField();
		final Boolean order = SortOrder.ASC_NULLS_FIRST==orderQuery.getSortOrder() ? true : false;
		if(orderField.equals("handle")) {
			Collections.sort(resultList, new Comparator<Sample>() {
				public int compare(Sample o1, Sample o2) {
					int result = o1.getHandle().compareTo(o2.getHandle());
					return order?result:-1*result;
				}
			});
		}else if(orderField.equals("email")) {
			Collections.sort(resultList, new Comparator<Sample>() {
				public int compare(Sample o1, Sample o2) {
					int result = o1.getEmail().compareTo(o2.getEmail());
					return order?result:-1*result;
				}
			});
		}else if(orderField.equals("firstName")) {
			Collections.sort(resultList, new Comparator<Sample>() {
				public int compare(Sample o1, Sample o2) {
					int result = o1.getFirstName().compareTo(o2.getFirstName());
					return order?result:-1*result;
				}
			});
		}else if(orderField.equals("lastName")) {
			Collections.sort(resultList, new Comparator<Sample>() {
				public int compare(Sample o1, Sample o2) {
					int result = o1.getLastName().compareTo(o2.getLastName());
					return order?result:-1*result;
				}
			});
		}else {
			throw new StorageException("Unknown field on order by :" + orderField);
		}
	}
}

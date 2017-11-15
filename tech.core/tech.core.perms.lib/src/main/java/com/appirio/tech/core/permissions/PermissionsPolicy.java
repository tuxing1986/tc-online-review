package com.appirio.tech.core.permissions;


import org.joda.time.DateTime;

/**
 * 
 */
public class PermissionsPolicy {
	
//	public enum SubjectType {
//		USER, ROLE, USER_GROUP, TENANT
//	}
	
	private String id;
	private String resource;
	private Policy policy;
	private String subjectId;
	private String subjectType;
	private String createdBy;
	private String modifiedBy;
	private DateTime createdAt;
	private DateTime modifiedAt;

	public PermissionsPolicy() {
		
	}
	
	public PermissionsPolicy(String subjectId, String subjectType, String resource, Policy policy) {
		this.subjectId = subjectId;
		this.subjectType = subjectType;
		this.resource = resource;
		this.policy = policy;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public Policy getPolicy() {
		return policy;
	}

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public DateTime getModifiedAt() {
		return modifiedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
	}

	public DateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(DateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public void setModifiedAt(DateTime modifiedAt) {
		this.modifiedAt = modifiedAt;
	}
	
	

}

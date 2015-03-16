/**
 * 
 */
package com.appirio.tech.sample.model;

import java.util.List;

import com.appirio.tech.core.api.v3.model.AbstractIdResource;
import com.appirio.tech.core.api.v3.model.annotation.ApiMapping;

/**
 * @author sudo
 *
 */
public class Note extends AbstractIdResource {

	public static final String RESOURCE_PATH = "notes";

	private User createdBy;
	private String content;
	private List<Note> childNotes;
	
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@ApiMapping(queryDefault=false)
	public List<Note> getChildNotes() {
		return childNotes;
	}

	public void setChildNotes(List<Note> childNotes) {
		this.childNotes = childNotes;
	}

	@ApiMapping(visible = false)
	public String getResourcePath() {
		return RESOURCE_PATH;
	}

}

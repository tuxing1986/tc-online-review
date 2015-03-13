package com.appirio.tech.core.service.identity.util.idgen;

public class Sequence {

	public Sequence() {
	}

	private String name;
	
	private Long nextVal;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getNextVal() {
		return nextVal;
	}

	public void setNextVal(Long nextVal) {
		this.nextVal = nextVal;
	}
}

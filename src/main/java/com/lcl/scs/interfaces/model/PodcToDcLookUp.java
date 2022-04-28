package com.lcl.scs.interfaces.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("PodcToDcLookUp")
public class PodcToDcLookUp {

	@Id
	@Field("_id")
	private String id;

	@Field("PODC_NAME")
	private String podcName;

	@Field("DC_NAME")
	private String dcName;

	@Field("TRANSIT_TIME")
	private Integer transitTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPodcName() {
		return podcName;
	}

	public void setPodcName(String podcName) {
		this.podcName = podcName;
	}

	public String getDcName() {
		return dcName;
	}

	public void setDcName(String dcName) {
		this.dcName = dcName;
	}

	public Integer getTransitTime() {
		return transitTime;
	}

	public void setTransitTime(Integer transitTime) {
		this.transitTime = transitTime;
	}

}

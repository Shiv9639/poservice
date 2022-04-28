package com.lcl.scs.email.csv.domain;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

import java.util.Date;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.lcl.scs.email.constants.WHReportConstants;
import com.lcl.scs.email.constants.WHReportConstants.WHReportHardCodeValuesForLpv;
import com.lcl.scs.email.constants.WHReportConstants.WHReportHeaderConstants;
import com.lcl.scs.email.constants.WHReportConstants.WHReportType;
import com.lcl.scs.util.DateFormater;

@Component
public class WHReportEntity {
	
	private String ownedBy;
	private String site;
	private Date date;
	private String quantity;
	private String dc;
	private String dept;
	private String whse;
	

	public String getOwnedBy() {
		return ownedBy;
	}

	public void setOwnedBy(String ownedBy) {
		this.ownedBy = ownedBy;
	}
	
	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getDC() {
		return dc;
	}

	public void setDC(String dc) {
		this.dc = dc;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	
	public String getWhse() {
		return whse;
	}

	public void setWhse(String whse) {
		this.whse = whse;
	}

	public static WHReportEntity parseCsvToWHReportEntity(CSVRecord csvRecord, WHReportType reportType) {
		WHReportEntity entity = new WHReportEntity();
		
		entity.setWhse(csvRecord.get(WHReportHeaderConstants.WHSE.getValue()));	
		
		entity.setDate(DateFormater
				.getDateFormatFromString(csvRecord.get(WHReportHeaderConstants.EXTRC_DT.getValue())));
		
		if (reportType == WHReportType.MAX_STORAGE_TYPE) {
			
			entity.setQuantity(csvRecord.get(WHReportHeaderConstants.RSV_LOCN_TOT.getValue()));
		}
		
		else if (reportType == WHReportType.PROJECTED_STORAGE_TYPE){
			
			entity.setQuantity(csvRecord.get(WHReportHeaderConstants.RSV_LOCN_USED.getValue()));
		}
		
		entity.setDC("D" + csvRecord.get(WHReportHeaderConstants.WHSE.getValue()));
		
		entity.setDept(csvRecord.get(WHReportHeaderConstants.DEPT.getValue()));
		
		return entity;

	}
	
	public String toLpvCsv(WHReportType reportType) {
		if (reportType == WHReportType.MAX_STORAGE_TYPE) {
			return of(WHReportHardCodeValuesForLpv.PROCESS_TYPE.getValue(),
					WHReportHardCodeValuesForLpv.NODE_TYPE.getValue(),
					Optional.ofNullable(getownedStringFromWhse()).orElse(StringUtils.EMPTY),
					Optional.ofNullable(getownedStringFromWhse()).orElse(StringUtils.EMPTY),
					WHReportHardCodeValuesForLpv.MEASURE_MAX.getValue(),
					Optional.ofNullable(date)
					.map(e -> DateFormater.getDateStringFromDate(e, WHReportConstants.WH_REPORT_DATE_FORMAT_FOR_CSV))
					.orElse(StringUtils.EMPTY),
					Optional.ofNullable(quantity).orElse(StringUtils.EMPTY),
					Optional.ofNullable(dc).orElse(StringUtils.EMPTY),
					Optional.ofNullable(dept).orElse(StringUtils.EMPTY))
					.map(s -> "\"" + s + "\"").collect(joining(","));
		}
		else
		return of(WHReportHardCodeValuesForLpv.PROCESS_TYPE.getValue(),
				WHReportHardCodeValuesForLpv.NODE_TYPE.getValue(),
				Optional.ofNullable(getownedStringFromWhse()).orElse(StringUtils.EMPTY),
				Optional.ofNullable(getownedStringFromWhse()).orElse(StringUtils.EMPTY),
				WHReportHardCodeValuesForLpv.MEASURE_PROJECTED.getValue(),
				Optional.ofNullable(date)
				.map(e -> DateFormater.getDateStringFromDate(e, WHReportConstants.WH_REPORT_DATE_FORMAT_FOR_CSV))
				.orElse(StringUtils.EMPTY),
				Optional.ofNullable(quantity).orElse(StringUtils.EMPTY),
				Optional.ofNullable(dc).orElse(StringUtils.EMPTY),
				Optional.ofNullable(dept).orElse(StringUtils.EMPTY))
				.map(s -> "\"" + s + "\"").collect(joining(","));
	}
	
	private String getownedStringFromWhse() {
		return "D" + whse + "-" + dept;
	}

}

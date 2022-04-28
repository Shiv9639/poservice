package com.lcl.scs.email.constants;


public class WHReportConstants {
	
	public static final String[] WHReportHeadersForLpv = { "Process Type", "Node Type", "Owned By", "Site", 
			"Measure", "Date", "Quantity", "DC", "Dept"};
	
	public enum WHReportType{
		MAX_STORAGE_TYPE,PROJECTED_STORAGE_TYPE
	}
	
	public static final String WH_REPORT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String WH_REPORT_DATE_FORMAT_FOR_CSV = "MM/dd/yyyy";

	public enum WHReportHardCodeValuesForLpv {
		PROCESS_TYPE("demandSupply"), NODE_TYPE("Capacity"), MEASURE_MAX("Max Storage C"), MEASURE_PROJECTED("Projected Storage C");

		private WHReportHardCodeValuesForLpv(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}
	}
	
	public enum WHReportHeaderConstants {
		WHSE("WHSE"), DEPT("DEPT"), EXTRC_DT("EXTRC_DT"), RSV_LOCN_TOT("RSV_LOCN_TOT"), RSV_LOCN_USED("RSV_LOCN_USED");

		private WHReportHeaderConstants(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}
	}

}

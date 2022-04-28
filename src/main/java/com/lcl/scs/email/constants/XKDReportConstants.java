package com.lcl.scs.email.constants;

import org.apache.commons.lang3.StringUtils;

public class XKDReportConstants {

	public static final String[] XKDReportHeadersForLpv = { "Shipment No", "Order Type", "Process Type",
			"Shipment Type", "Ship Mode", "Country of Origin", "Planned Ship Date", "From Loc", "To Loc", "Line No",
			"From", "To", "Ship From Owner", "Ship To Owner", "Quantity", "Line Carrier ID", "Trailer No", "ETD",
			"Creation Date", "Creation-Date", "Leg Ship From", "Leg Ship To", "Leg Ship From Owner",
			"Leg Ship To Owner", "Leg Carrier", "Leg Trailer No", "Leg Tracking Reference", "Leg Sequence",
			"Leg Ship Mode", "Operation Name", "Main Leg", "No of Deliveries", "Carrier", "Line Mode",
			"UDF Total Weight (LB)", "UDF Total Volume (CU FT)", "Packed Qty", "Equipment Type", "Service ID",
			"Carrier ID", "UDF Volume (CU FT)", "UDF Weight (LB)", "Ship State", "Vendor Name", "Leg ATA",
			"Container ATA", "Leg Actual Ship Date", "ATD", "Line Transaction Id", "Container ETA", "Leg ETA" };
	
	public static final String XKD_REPORT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public enum XKDReportHardCodeValuesForLpv {
		ORDER_TYPE("standardPO"), PROCESS_TYPE("supply"), SHIPMENT_TYPE("inboundShipment"), SHIP_MODE("Road"),
		COUNTRY_OF_ORIGIN("CAN"), LINE_NO("200"), SHIP_FROM_OWNER("0003018036"), SHIP_TO_OWNER("Loblaw"),
		LEG_SHIP_FROM("0003018036"), LEG_SHIP_FROM_OWNER("0003018036"), LEG_SHIP_TO_OWNER("Loblaw"),
		LEG_TRACKING_REFERENCE("200"), LEG_SEQUENCE("1"), LEG_SHIP_MODE("Road"), OPERATION_NAME("CreateShipment"),
		MAIN_LEG("True"), NO_OF_DELIVERIES("1"), LINE_MODE("Road"),FROM("0003018036"),

		// below values are required at LPV end as empty strings...

		FROM_LOC(StringUtils.EMPTY), TO_LOC(StringUtils.EMPTY), QUANTITY(StringUtils.EMPTY),
		CONTAINER_ETA(StringUtils.EMPTY), LEG_ETA(StringUtils.EMPTY), UDF_TOTAL_WEIGHT(StringUtils.EMPTY),
		UDF_TOTA_VOLUME(StringUtils.EMPTY), PACKED_QTY(StringUtils.EMPTY), EQUIPMENT_TYPE(StringUtils.EMPTY),
		SERVICE_ID(StringUtils.EMPTY), CARRIER_ID(StringUtils.EMPTY), UDF_VOLUME(StringUtils.EMPTY),
		UDF_WEIGHT(StringUtils.EMPTY), SHIP_STATE(StringUtils.EMPTY), VENDOR_NAME(StringUtils.EMPTY),
		LEG_ATA(StringUtils.EMPTY), CONTAINER_ATA(StringUtils.EMPTY);

		private XKDReportHardCodeValuesForLpv(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}
	}

	public enum YardReportHeaderConstants {
		PO("PO"), PODC("PODC"), TRAILER_IN_TRAILER("TRAILER_IN_TRAILER"), TRAILER_IN_CARRIER("TRAILER_IN_CARRIER"),
		TRAILER_IN_ARRIVEDATE("TRAILER_IN_ARRIVEDATE"), TRAILER_IN_DOCKEDDATE("TRAILER_IN_DOCKEDDATE"),
		TRAILER_OUT_DESTINATION("TRAILER_OUT_DESTINATION"), TRAILER_OUT_TRIP("TRAILER_OUT_TRIP"),
		TRAILER_OUT_TCLOAD("TRAILER_OUT_TCLOAD"), TRAILER_OUT_TRAILER("TRAILER_OUT_TRAILER"),
		TRAILER_OUT_CARRIER("TRAILER_OUT_CARRIER"), TRAILER_OUT_DRIVER("TRAILER_OUT_DRIVER"),
		TRAILER_OUT_LOADEDDATE("TRAILER_OUT_LOADEDDATE"), TRAILER_OUT_DISPATCHDATE("TRAILER_OUT_DISPATCHDATE"),
		TRAILER_OUT_DEPARTDATE("TRAILER_OUT_DEPARTDATE");

		private YardReportHeaderConstants(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}
	}
}
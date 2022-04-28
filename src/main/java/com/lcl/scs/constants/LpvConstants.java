package com.lcl.scs.constants;

public class LpvConstants {

	private LpvConstants() {
		super();
	}

	private static final String LPV_DELIVERY_END_POINT_VAIRABLE = System.getenv("BY_LPV_PO_DELIVERY_API_URL");
	private static final String LPV_PO_END_POINT_VAIRABLE = System.getenv("BY_LPV_PO_API_URL");
	private static final String LPV_XKD_SHIPMENT_END_POINT_VAIRABLE = System.getenv("BY_LPV_SHIPMENT_API_URL");
	private static final String LPV_CAPACITY_END_POINT_VARIABLE = System.getenv("BY_LPV_CAPACITY_API_URL");
	//token details from environment variables
	private static final String BY_TOKEN_URL_VAIRABLE = System.getenv("BY_TOKEN_URL");
	private static final String BY_TOKEN_CLIENT_ID_VAIRABLE = System.getenv("BY_TOKEN_CLIENT_ID");
	private static final String BY_TOKEN_CLIENT_SECRET_VAIRABLE = System.getenv("BY_TOKEN_CLIENT_SECRET");
	private static final String BY_TOKEN_GRANT_TYPE_VAIRABLE = System.getenv("BY_TOKEN_GRANT_TYPE");
	private static final String BY_TOKEN_SCOPE_VAIRABLE = System.getenv("BY_TOKEN_SCOPE");

	//corresponding enums to use in code for end points...
	public enum LpvEndpointUrl {
		
		LPV_DELIVERY_URL(LPV_DELIVERY_END_POINT_VAIRABLE), LPV_PO_URL(LPV_PO_END_POINT_VAIRABLE),
		LPV_XKD_SHIPMENT_URL(LPV_XKD_SHIPMENT_END_POINT_VAIRABLE), LPV_CAPACITY_URL(LPV_CAPACITY_END_POINT_VARIABLE);

		private LpvEndpointUrl(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}

	}
	
	//corresponding enum for token...
	public enum BY_TOKEN {
		
		URL(BY_TOKEN_URL_VAIRABLE),
		CLIENT_ID(BY_TOKEN_CLIENT_ID_VAIRABLE),
		CLIENT_SECRET(BY_TOKEN_CLIENT_SECRET_VAIRABLE),
		GRANT_TYPE(BY_TOKEN_GRANT_TYPE_VAIRABLE),
		SCOPE(BY_TOKEN_SCOPE_VAIRABLE)
		;

		private BY_TOKEN(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}

	}
	
	public enum EntityName {
		DELIVERY("deliveryMessage"),
		PURCHASE_ORDER("purchaseOrderMessage"),
		SHIPMENT("inboundShipmentMessage"),
		CAPACITY("locationMessageCapacity")
		;

		private EntityName(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}
	}

}

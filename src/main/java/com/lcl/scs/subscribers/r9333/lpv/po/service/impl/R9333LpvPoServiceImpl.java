package com.lcl.scs.subscribers.r9333.lpv.po.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonMergePatch;
import javax.json.JsonPatch;
import javax.json.JsonStructure;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lcl.scs.constants.LpvConstants.EntityName;
import com.lcl.scs.constants.LpvConstants.LpvEndpointUrl;

//import com.lcl.scs.email.service.impl.AutomatedLanesEmail;
import com.lcl.scs.subscribers.model.Subscribers;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvPoDetailInterface;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvReasonCode;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvReasonCodeTransaction;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvPoInterface;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvToken;

import com.lcl.scs.subscribers.r9333.lpv.po.service.LpvReasonCodeService;
import com.lcl.scs.subscribers.r9333.lpv.po.service.LpvPoInterfaceService;
import com.lcl.scs.subscribers.r9333.lpv.po.service.R9333LpvPoService;
import com.lcl.scs.util.DateFormater;
import com.lcl.scs.util.JSonStringFormatter;
import com.lcl.scs.util.logging.LoggingUtilities;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;

public class R9333LpvPoServiceImpl implements R9333LpvPoService {
	private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("EEE yyyy-MM-dd HH:mm:ss z");
	private static final SimpleDateFormat LPVDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	private static final SimpleDateFormat DATETIMEFORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final SimpleDateFormat DATEFORMATTER = new SimpleDateFormat("yyyyMMdd");
	private static final String EXPORT_PATH = System.getenv("FOLDER_NAME");
	private static final int MAX_LCT_CONNECTION = 100;
	private LpvPoInterfaceService lpvPoInterfaceService;
	private R9333LpvPoService r9333LpvPoService;
	private LpvReasonCodeService lpvReasonCodeService;
	private String mongodbDatabase;

	private String mongodbURI;
	private com.lcl.scs.subscribers.service.SubscribersService subscribersService;
	// private com.lcl.scs.email.service.impl.AutomatedLanesEmail
	// automatedLanesEmail;

	private String BY_TOKEN_URL;
	private String BY_TOKEN_CLIENT_ID;
	private String BY_TOKEN_CLIENT_SECRET;
	private String BY_TOKEN_GRANT_TYPE;
	private String BY_TOKEN_SCOPE;

	private List<LpvReasonCode> reasonCodeList = new ArrayList<LpvReasonCode>();

	HashMap<String, String> poTypes = new HashMap<String, String>();
	HashMap<String, LpvReasonCode> reasonCodeMap = new HashMap<String, LpvReasonCode>();

	private final String estTimeZone = "Canada/Eastern";

	private void generateLPVDeliveryCSVFile(LpvPoInterface po, LpvToken token) throws Exception {
		FileWriter out = null;
		try {
			out = new FileWriter(EXPORT_PATH + "/" + po.getTargetDelvCSVFileName());

			SimpleDateFormat lpvdateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

			CSVPrinter printer = new CSVPrinter(out,
					CSVFormat.DEFAULT.withHeader("Delivery No", "PO No", "PO Line", "Item", "Delivery Type",
							"Order Type", "Process Type", "Shipment Type", "Ship Mode", "Customer", "Creation Date",
							"Planned Ship Date", "Supplier", "Line No", "Line Mode", "Ship To", "PO Creation Date",
							"Ship From", "Shipping UOM", "Order UOM", "Country of Origin", "C3 Appointment",
							"Operation Name", "INCO1", "INCO2", "Buyer", "Del Canc Ind", "Del Closed Ind", "BOL No",
							"Container No", "Packing Slip No", "Ship Date", "System", "Transaction Id", "Pick Up Date",
							"Appointment Time", "Confirmed Qty", "UDF DC Pallet", "UDF Ven Pallet", "UDF_ERP_CRTN_DATE",
							"UDF_ERP_CHG_DATE", "UDF_PO_CLOSURE_DATE", "Order Qty", "Total Order Count",
							"Delivery Completion Ind","Unit Price", "PO Value" ));

			for (LpvPoDetailInterface poDetail : po.getPoDetails()) {

				printer.printRecord(po.getPurchaseOrderId(), po.getPurchaseOrderId(), poDetail.getPoLineId(),
						poDetail.getCustomerItemName(), "inboundDelivery", "standardPO", "supply", "inboundShipment",
						"Road", po.getCustomerName(),
						po.getErpCreationDate() != null ? lpvdateformat.format(po.getErpCreationDate()) : "",
						po.getUdfrequestedShipDate() != null ? lpvdateformat.format(po.getUdfrequestedShipDate()) : "",
						po.getSupplierName(), poDetail.getPoLineId(), "Road", poDetail.getShipToSiteName(),
						po.getErpCreationDate() != null ? lpvdateformat.format(po.getErpCreationDate()) : "",
						poDetail.getShipFromSiteName(), poDetail.getOrderUom(), poDetail.getOrderUom(), "CAN",
						po.getC3Appointment() != null ? lpvdateformat.format(po.getC3Appointment()) : "",
						"CreateDelivery", po.getIncoTerms1(), po.getIncoTerms2(), po.getBuyerName(),
						poDetail.getCancelInd(), po.getPoCloseInd(), "", "", "",
						// po.getUdfrequestedShipDate() != null ?
						// lpvdateformat.format(po.getUdfrequestedShipDate()) : "",
						"", po.getSourceErpSystem(), po.getErpTransactionId(),
						po.getUdfrequestedShipDate() != null ? lpvdateformat.format(po.getUdfrequestedShipDate()) : "",
						po.getC3Appointment() != null ? lpvdateformat.format(po.getC3Appointment()) : "",
						Math.round(poDetail.getConfirmedQuantity()), po.getUdfDCPallet(), po.getVanPallet(),
						po.getUdfErpCrtnDate() != null ? lpvdateformat.format(po.getUdfErpCrtnDate()) : "",
						po.getUdfErpChgDate() != null ? lpvdateformat.format(po.getUdfErpChgDate()) : "",
						po.getPoClosedDate() != null ? lpvdateformat.format(po.getPoClosedDate()) : "",
						Math.round(poDetail.getOrderQuantity()), po.getUdftotalReqCount(),
						poDetail.getDeliveryCompletionIndicator(),poDetail.getUnitPrice(),poDetail.getPoDollarValue(po));
			}

			out.flush();
			out.close();

			out = null;

			File file = new File(EXPORT_PATH + "/" + po.getTargetDelvCSVFileName());
			//Reason Code 10 : File size is 0 bytes
			if(file.length()==0L) {
				List<LpvReasonCode> lpvReasonCodeList = findReasonCode("10");
				LpvReasonCodeTransaction lpvReasonCodeTransaction = lpvReasonCodeService
						.publishLpvReasonCodeTransaction(lpvReasonCodeList, po);
				lpvReasonCodeService.saveLpvReasonCodeTransaction(lpvReasonCodeTransaction);
			}
			else {
			LpvPoAPIServiceImpl apiCall = new LpvPoAPIServiceImpl();
			apiCall.setLpvReasonCodeService(lpvReasonCodeService);
			apiCall.setR9333LpvPoService(getR9333LpvPoService());
			apiCall.postCSVFile(file, token, LpvEndpointUrl.LPV_DELIVERY_URL, EntityName.DELIVERY,po);
			if (!file.delete()) {
				throw new Exception("Failed to delete file " + po.getTargetDelvCSVFileName());
			}
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (out != null)
				out.close();
		}
	}

	private void generateLPVPOCSVFile(LpvPoInterface po, LpvToken token) throws Exception {
		FileWriter out = null;
		try {
			SimpleDateFormat lpvdateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			out = new FileWriter(EXPORT_PATH + "/" + po.getTargetPOCSVFileName());

			CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader("Order No", "Line No",
					"Supplier_code", "UDF From", "Ship From", "INCO1", "INCO2", "UDF Currency", "Ship To",
					"UDF Ship To", "ERP Order Type", "Item", "Material Group", "Purchase organization", "Buyer",
					"Order Quantity", "Unit Price", "Order Uom", "To", "PO deletion Indicator", "Confirmed Quantity",
					"UDF Total Conf Count", "UDF Total Req Count", "UDF Total GR Count","Order Type", "Process Type", "Customer Item Owner",
					"Customer", "PO Close Ind", "Creation Date", "UDF Requested Ship Date", "Need By Date",
					"Schedule No", "Scheduled Delivery Date", "Scheduled Quantity", "Scheduled Ship Date", "Supplier",
					"UDF From Site", "UDF Need By Date", "C3 Appointment", "Operation Name", "PO Canc Ind",
					"All Shipments", "UDF DC Pallet", "UDF Ven Pallet", "Source Erp System", "Transaction Id", "GR Qty",
					"UDF_ERP_CRTN_DATE", "UDF_ERP_CHG_DATE", "UDF_PO_CLOSURE_DATE", "Delivery Completion Ind",
					"UDF Line Ven Pallet"));

			for (LpvPoDetailInterface poDetail : po.getPoDetails()) {

				printer.printRecord(po.getPurchaseOrderId(), poDetail.getPoLineId(), po.getSupplierName(),
						po.getUdfFromSite(), poDetail.getShipFromSiteName(), po.getIncoTerms1(), po.getIncoTerms2(),
						po.getUdfcurrency(), poDetail.getShipTo(), po.getUdfshipToSiteName(), po.getErpOrderType(),
						poDetail.getCustomerItemName(), poDetail.getMaterialGroup(), po.getPurchasingOrg(),
						po.getBuyerName(), Math.round(poDetail.getOrderQuantity()), poDetail.getUnitPrice(),
						poDetail.getOrderUom(), poDetail.getShipTo(), po.getPoCloseInd(),
						poDetail.getConfirmedQuantity() > 0 ? poDetail.getConfirmedQuantity() : "",
						po.getUdftotalConfCount(), po.getUdftotalReqCount(), po.getTotalGrQty(),po.getPurchaseOrderType(),
						po.getProcessType(), poDetail.getCustomerItemOwnerName(), po.getCustomerName(),
						po.getPoCloseInd(),
						po.getErpCreationDate() != null ? lpvdateformat.format(po.getErpCreationDate()) : "",
						po.getUdfrequestedShipDate() != null ? lpvdateformat.format(po.getUdfrequestedShipDate()) : "",
						po.getNeedByDate() != null ? lpvdateformat.format(po.getNeedByDate()) : "",
						poDetail.getConfirmedQuantity() > 0 || poDetail.getGrQuantity() > 0 ? "0001" : "",
						(poDetail.getConfirmedQuantity() > 0 || poDetail.getGrQuantity() > 0)
								&& poDetail.getConfirmedDeliveryDate() != null
										? lpvdateformat.format(poDetail.getConfirmedDeliveryDate())
										: "",
						poDetail.getConfirmedQuantity() > 0 || poDetail.getGrQuantity() > 0
								? Math.round(poDetail.getConfirmedQuantity())
								: "",
						(poDetail.getConfirmedQuantity() > 0 || poDetail.getGrQuantity() > 0)
								&& poDetail.getConfirmedShipDate() != null
										? lpvdateformat.format(poDetail.getConfirmedShipDate())
										: "",
						po.getSupplierName(), poDetail.getUdfshipFromSiteName(),
						po.getUdfneedByDate() != null ? lpvdateformat.format(po.getUdfneedByDate()) : "",
						po.getC3Appointment() != null ? lpvdateformat.format(po.getC3Appointment()) : "",
						po.getOperationName(), poDetail.getCancelInd(), "0", po.getUdfDCPallet(), po.getVanPallet(),
						po.getSourceErpSystem(), po.getErpTransactionId(),
						poDetail.getGrQuantity() > 0 ? Math.round(poDetail.getGrQuantity()) : "",
						po.getUdfErpCrtnDate() != null ? lpvdateformat.format(po.getUdfErpCrtnDate()) : "",
						po.getUdfErpChgDate() != null ? lpvdateformat.format(po.getUdfErpChgDate()) : "",
						po.getPoClosedDate() != null ? lpvdateformat.format(po.getPoClosedDate()) : "",
						poDetail.getDeliveryCompletionIndicator(),(int)poDetail.getLineVendorPallet());
			}

			out.flush();
			out.close();

			out = null;

			File file = new File(EXPORT_PATH + "/" + po.getTargetPOCSVFileName());
			//Reason Code 10 : File size is 0 bytes
			if(file.length()==0L) {
				List<LpvReasonCode> lpvReasonCodeList = findReasonCode("10");
				LpvReasonCodeTransaction lpvReasonCodeTransaction = lpvReasonCodeService
						.publishLpvReasonCodeTransaction(lpvReasonCodeList, po);
				lpvReasonCodeService.saveLpvReasonCodeTransaction(lpvReasonCodeTransaction);
			}
			else {
			LpvPoAPIServiceImpl apiCall = new LpvPoAPIServiceImpl();
			apiCall.setLpvReasonCodeService(lpvReasonCodeService);
			apiCall.setR9333LpvPoService(getR9333LpvPoService());
			apiCall.postCSVFile(file, token, LpvEndpointUrl.LPV_PO_URL, EntityName.PURCHASE_ORDER,po);
			if (!file.delete()) {
				throw new Exception("Failed to delete file " + po.getTargetPOCSVFileName());
			}
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (out != null)
				out.close();
		}
	}

	public String getMongodbDatabase() {
		return mongodbDatabase;
	}

	public String getMongodbURI() {
		return mongodbURI;
	}

	public com.lcl.scs.subscribers.service.SubscribersService getSubscribersService() {
		return subscribersService;
	}


	public void processR9333Interface() throws Exception {

		MongoClient mongo = null;
		try {
//			Date lastRunTime = new Date();
			mongo = MongoClients.create(mongodbURI);
//			Subscribers subscriber = subscribersService.findByName("LPV-PO1");

//			LoggingUtilities.generateInfoLog("Last Run Time: " + DATEFORMAT.format(subscriber.getLastrun()));
//			LoggingUtilities.generateInfoLog("Inbound Collection: " + subscriber.getInboundcollection());
			MongoCollection<Document> reasonCodeCollection = mongo.getDatabase(mongodbDatabase)
					.getCollection("LpvReasonCodeCollection");

			MongoTemplate mongoTemplate = new MongoTemplate(mongo, mongodbDatabase);
			// mongoTemplate.find(query, entityClass);

//			Date processDate = new Date();

			FindIterable<Document> reasonCodes = reasonCodeCollection.find();

			for (Document code : reasonCodes) {
				LpvReasonCode lpvReasonCode = new LpvReasonCode();
				lpvReasonCode.setReasonCode((String) code.get("reasonCode"));
				lpvReasonCode.setReasonCodeDescription((String) code.get("reasonCodeDescription"));
				lpvReasonCode.setUserId("System");
				lpvReasonCode.setModifyDate(new Date());
				reasonCodeMap.put((String) code.get("reasonCode"), lpvReasonCode);
			}

			LpvTokenServiceImpl tokenService = new LpvTokenServiceImpl();
			tokenService.setBY_TOKEN_URL(BY_TOKEN_URL);
			tokenService.setBY_TOKEN_CLIENT_ID(BY_TOKEN_CLIENT_ID);
			tokenService.setBY_TOKEN_CLIENT_SECRET(BY_TOKEN_CLIENT_SECRET);
			tokenService.setBY_TOKEN_GRANT_TYPE(BY_TOKEN_GRANT_TYPE);
			tokenService.setBY_TOKEN_SCOPE(BY_TOKEN_SCOPE);
			LpvToken lpvToken = tokenService.getLpvToken();


			List<LpvPoInterface> poToBeExtract = lpvPoInterfaceService
					.findByProcessIndicatorOrderByPurchaseOrderIdAscIdAsc("N");
			if (poToBeExtract.size() > 0) {
				
				String fileDate = DATETIMEFORMATTER.format(new Date());
				ExecutorService taskExecutor = Executors.newFixedThreadPool(
						poToBeExtract.size() > MAX_LCT_CONNECTION ? MAX_LCT_CONNECTION : poToBeExtract.size());
				int i = 0;
				for (LpvPoInterface poFromDB : poToBeExtract) {
					// This is to avoid two or more iDocs for the same PO being extracted at the
					// same time
					// ------------------------------------------------------------------------------------------------
					if (i > 0) {
					
						LpvPoInterface previousPoFromDB = poToBeExtract.get(i - 1);
						if (previousPoFromDB.getPurchaseOrderId().equals(poFromDB.getPurchaseOrderId())
								&& !previousPoFromDB.getiDocSerialNumber().equals(poFromDB.getiDocSerialNumber())) {
							continue;
						}

					}
					// -------------------------------------------------------------------------------------------------

					if (i >= MAX_LCT_CONNECTION)
						break;
					else
						i++;
				
					taskExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								if (poFromDB.getOriginalFileName().isEmpty() || poFromDB.getOriginalFileName().trim().equals("")) {
									poFromDB.setProcessedCount(1);
								}
								else {
									poFromDB.setProcessedCount(poFromDB.getProcessedCount()+1);
								}
								// lcl_lct_dl_<ponumber/deliverynumber/node>_<unique ID>_YYYYMMDDHH24MISS
								String delvFileName = "lcl_lct_dl_" + poFromDB.getPurchaseOrderId() + "_"
										+ poFromDB.getiDocSerialNumber() + "_"
//										+ DATETIMEFORMATTER.format(poFromDB.getiDocCreateDate())
										+ fileDate + ".csv";

								// lcl_lct_po_<ponumber/deliverynumber/node>_<unique ID>_YYYYMMDDHH24MISS
								String poFileName = "lcl_lct_po_" + poFromDB.getPurchaseOrderId() + "_"
										+ poFromDB.getiDocSerialNumber() + "_"
//										+ DATETIMEFORMATTER.format(poFromDB.getiDocCreateDate())
										+ fileDate + ".csv";
							
								poFromDB.setTargetDelvCSVFileName(delvFileName);
								poFromDB.setTargetPOCSVFileName(poFileName);
								
								generateLPVPOCSVFile(poFromDB, lpvToken);
								generateLPVDeliveryCSVFile(poFromDB, lpvToken);

//				To be put back once the code is stable			
								poFromDB.setProcessIndicator("Y");
								poFromDB.setIdocProcessedTime(new Date());
							
								lpvPoInterfaceService.saveOrUpdateLpvPoInterface(poFromDB);							
							} catch (Exception ex) {
								LoggingUtilities.generateErrorLog(ex.getMessage());

							}
						}
					});
				}
				taskExecutor.shutdown();
				taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			}

			mongo.close();

//			To be put back once the code is stable	
//			subscriber.setLastrun(lastRunTime);
//			subscribersService.saveOrUpdateSubscribers(subscriber);
		} catch (Exception ex) {
			LoggingUtilities.generateErrorLog(ex.getMessage());
			throw ex;
		} finally {
			if (mongo != null)
				mongo.close();
		}
	}
	@Override
	public List<LpvReasonCode> findReasonCode(String reasonCode) {
		List<LpvReasonCode> reasonCodelist = new ArrayList<LpvReasonCode>();
		reasonCodelist.add(reasonCodeMap.get(reasonCode));
		return reasonCodelist;

	}

	public void setBY_TOKEN_CLIENT_ID(String bY_TOKEN_CLIENT_ID) {
		BY_TOKEN_CLIENT_ID = bY_TOKEN_CLIENT_ID;
	}

	public void setBY_TOKEN_CLIENT_SECRET(String bY_TOKEN_CLIENT_SECRET) {
		BY_TOKEN_CLIENT_SECRET = bY_TOKEN_CLIENT_SECRET;
	}

	public void setBY_TOKEN_GRANT_TYPE(String bY_TOKEN_GRANT_TYPE) {
		BY_TOKEN_GRANT_TYPE = bY_TOKEN_GRANT_TYPE;
	}

	public void setBY_TOKEN_SCOPE(String bY_TOKEN_SCOPE) {
		BY_TOKEN_SCOPE = bY_TOKEN_SCOPE;
	}

	public void setBY_TOKEN_URL(String bY_TOKEN_URL) {
		BY_TOKEN_URL = bY_TOKEN_URL;
	}


	public void setLpvReasonCodeService(LpvReasonCodeService lpvReasonCodeService) {
		this.lpvReasonCodeService = lpvReasonCodeService;
	}

	public void setLpvPoInterfaceService(LpvPoInterfaceService lpvPoInterfaceService) {
		this.lpvPoInterfaceService = lpvPoInterfaceService;
	}

	public void setMongodbDatabase(String mongodbDatabase) {
		this.mongodbDatabase = mongodbDatabase;
	}

	public void setMongodbURI(String mongodbURI) {
		this.mongodbURI = mongodbURI;
	}

	public void setSubscribersService(com.lcl.scs.subscribers.service.SubscribersService subscribersService) {
		this.subscribersService = subscribersService;
	}

	public void setR9333LpvPoService(R9333LpvPoService r9333LpvPoService) {
		this.r9333LpvPoService = r9333LpvPoService;
	}

	public R9333LpvPoService getR9333LpvPoService() {
		return r9333LpvPoService;
	}

	public LpvReasonCodeService getLpvReasonCodeService() {
		return lpvReasonCodeService;
	}

	
}

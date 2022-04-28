package com.lcl.scs.email.csv.domain;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.lcl.scs.email.constants.XKDReportConstants;
import com.lcl.scs.email.constants.XKDReportConstants.XKDReportHardCodeValuesForLpv;
import com.lcl.scs.email.constants.XKDReportConstants.YardReportHeaderConstants;
import com.lcl.scs.interfaces.service.PodcToDcLookUpService;
import com.lcl.scs.util.DateFormater;

@Component
public class YardReportEntity {
	
	private String shipmentNo;
	private Timestamp plannedShipDate;
	private String to;
	private String shipFromOwner;
	private String lineCarrierID;
	private String trailerNo;
	private Timestamp etd;
	private String legShipFrom;
	private String legShipTo;
	private String legShipFromOwner;
	private String legCarrier;
	private String legTrailerNo;
	private String carrier;
	private Timestamp legActualShipDate;
	private Timestamp atd;
	private Timestamp containerETA;
	private Timestamp legETA;
	private String LineTransactionId;

	public String getShipmentNo() {
		return shipmentNo;
	}

	public void setShipmentNo(String shipmentNo) {
		this.shipmentNo = shipmentNo;
	}

	public Timestamp getPlannedShipDate() {
		return plannedShipDate;
	}

	public void setPlannedShipDate(Timestamp plannedShipDate) {
		this.plannedShipDate = plannedShipDate;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getShipFromOwner() {
		return shipFromOwner;
	}

	public void setShipFromOwner(String shipFromOwner) {
		this.shipFromOwner = shipFromOwner;
	}

	public String getLineCarrierID() {
		return lineCarrierID;
	}

	public void setLineCarrierID(String lineCarrierID) {
		this.lineCarrierID = lineCarrierID;
	}

	public String getTrailerNo() {
		return trailerNo;
	}

	public void setTrailerNo(String trailerNo) {
		this.trailerNo = trailerNo;
	}

	public Timestamp getEtd() {
		return etd;
	}

	public void setEtd(Timestamp etd) {
		this.etd = etd;
	}

	public String getLegShipFrom() {
		return legShipFrom;
	}

	public void setLegShipFrom(String legShipFrom) {
		this.legShipFrom = legShipFrom;
	}

	public String getLegShipTo() {
		return legShipTo;
	}

	public void setLegShipTo(String legShipTo) {
		this.legShipTo = legShipTo;
	}

	public String getLegShipFromOwner() {
		return legShipFromOwner;
	}

	public void setLegShipFromOwner(String legShipFromOwner) {
		this.legShipFromOwner = legShipFromOwner;
	}

	public String getLegCarrier() {
		return legCarrier;
	}

	public void setLegCarrier(String legCarrier) {
		this.legCarrier = legCarrier;
	}

	public String getLegTrailerNo() {
		return legTrailerNo;
	}

	public void setLegTrailerNo(String legTrailerNo) {
		this.legTrailerNo = legTrailerNo;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public Timestamp getLegActualShipDate() {
		return legActualShipDate;
	}

	public void setLegActualShipDate(Timestamp legActualShipDate) {
		this.legActualShipDate = legActualShipDate;
	}

	public Timestamp getAtd() {
		return atd;
	}

	public void setAtd(Timestamp atd) {
		this.atd = atd;
	}

	public Timestamp getContainerETA() {
		return containerETA;
	}

	public void setContainerETA(Timestamp containerETA) {
		this.containerETA = containerETA;
	}

	public Timestamp getLegETA() {
		return legETA;
	}

	public void setLegETA(Timestamp legETA) {
		this.legETA = legETA;
	}

	public String getLineTransactionId() {
		return LineTransactionId;
	}

	public void setLineTransactionId(String lineTransactionId) {
		LineTransactionId = lineTransactionId;
	}

	public static YardReportEntity parseCsvToYardReportEntity(CSVRecord csvRecord) {
		YardReportEntity entity = new YardReportEntity();
		entity.setShipmentNo(csvRecord.get(YardReportHeaderConstants.PO.getValue()));
		entity.setPlannedShipDate(DateFormater
				.getTimestampFromString(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_LOADEDDATE.getValue())));
		entity.setTo(
				PodcToDcLookUpService.podcToDcMap.get().get(csvRecord.get(YardReportHeaderConstants.PODC.getValue())));
		entity.setLineCarrierID(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_CARRIER.getValue()));
		entity.setTrailerNo(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_TRAILER.getValue()));
		entity.setEtd(DateFormater
				.getTimestampFromString(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_LOADEDDATE.getValue())));
		entity.setLegShipTo(
				PodcToDcLookUpService.podcToDcMap.get().get(csvRecord.get(YardReportHeaderConstants.PODC.getValue())));
		entity.setLegCarrier(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_CARRIER.getValue()));
		entity.setLegTrailerNo(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_TRAILER.getValue()));
		entity.setCarrier(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_CARRIER.getValue()));
		entity.setLegActualShipDate(DateFormater
				.getTimestampFromString(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_DEPARTDATE.getValue())));
		entity.setAtd(DateFormater
				.getTimestampFromString(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_DEPARTDATE.getValue())));
		entity.setContainerETA(DateFormater
				.getTimestampFromString(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_DEPARTDATE.getValue())));
		entity.setEtd(DateFormater
				.getTimestampFromString(csvRecord.get(YardReportHeaderConstants.TRAILER_OUT_DEPARTDATE.getValue())));
		return entity;

	}

	public String toLpvCsv() {
		return of(Optional.ofNullable(shipmentNo).orElse(StringUtils.EMPTY),
				XKDReportHardCodeValuesForLpv.ORDER_TYPE.getValue(),
				XKDReportHardCodeValuesForLpv.PROCESS_TYPE.getValue(),
				XKDReportHardCodeValuesForLpv.SHIPMENT_TYPE.getValue(),
				XKDReportHardCodeValuesForLpv.SHIP_MODE.getValue(),
				XKDReportHardCodeValuesForLpv.COUNTRY_OF_ORIGIN.getValue(),
				Optional.ofNullable(plannedShipDate)
						.map(p -> DateFormater.getDateStringFromTimeStamp(p, XKDReportConstants.XKD_REPORT_DATE_FORMAT))
						.orElse(StringUtils.EMPTY),
				XKDReportHardCodeValuesForLpv.FROM_LOC.getValue(), XKDReportHardCodeValuesForLpv.TO_LOC.getValue(),
				XKDReportHardCodeValuesForLpv.LINE_NO.getValue(), XKDReportHardCodeValuesForLpv.FROM.getValue(),
				Optional.ofNullable(to).orElse(StringUtils.EMPTY), XKDReportHardCodeValuesForLpv.FROM.getValue(),
				XKDReportHardCodeValuesForLpv.SHIP_FROM_OWNER.getValue(),
				XKDReportHardCodeValuesForLpv.SHIP_TO_OWNER.getValue(),
				XKDReportHardCodeValuesForLpv.QUANTITY.getValue(),
				Optional.ofNullable(lineCarrierID).orElse(StringUtils.EMPTY),
				Optional.ofNullable(trailerNo).orElse(StringUtils.EMPTY),
				Optional.ofNullable(etd)
						.map(e -> DateFormater.getDateStringFromTimeStamp(e, XKDReportConstants.XKD_REPORT_DATE_FORMAT))
						.orElse(StringUtils.EMPTY),
				Optional.ofNullable(etd)
						.map(e -> DateFormater.getDateStringFromTimeStamp(e, XKDReportConstants.XKD_REPORT_DATE_FORMAT))
						.orElse(StringUtils.EMPTY),
				Optional.ofNullable(etd)
						.map(e -> DateFormater.getDateStringFromTimeStamp(e, XKDReportConstants.XKD_REPORT_DATE_FORMAT))
						.orElse(StringUtils.EMPTY),
				XKDReportHardCodeValuesForLpv.LEG_SHIP_FROM.getValue(),
				Optional.ofNullable(to).orElse(StringUtils.EMPTY),
				XKDReportHardCodeValuesForLpv.LEG_SHIP_FROM_OWNER.getValue(),
				XKDReportHardCodeValuesForLpv.LEG_SHIP_TO_OWNER.getValue(),
				Optional.ofNullable(legCarrier).orElse(StringUtils.EMPTY),
				Optional.ofNullable(legTrailerNo).orElse(StringUtils.EMPTY),
				XKDReportHardCodeValuesForLpv.LEG_TRACKING_REFERENCE.getValue(),
				XKDReportHardCodeValuesForLpv.LEG_SEQUENCE.getValue(),
				XKDReportHardCodeValuesForLpv.LEG_SHIP_MODE.getValue(),
				XKDReportHardCodeValuesForLpv.OPERATION_NAME.getValue(),
				XKDReportHardCodeValuesForLpv.MAIN_LEG.getValue(),
				XKDReportHardCodeValuesForLpv.NO_OF_DELIVERIES.getValue(),
				Optional.ofNullable(carrier).orElse(StringUtils.EMPTY),
				XKDReportHardCodeValuesForLpv.LINE_MODE.getValue(),
				XKDReportHardCodeValuesForLpv.UDF_TOTAL_WEIGHT.getValue(),
				XKDReportHardCodeValuesForLpv.UDF_TOTA_VOLUME.getValue(),
				XKDReportHardCodeValuesForLpv.PACKED_QTY.getValue(),
				XKDReportHardCodeValuesForLpv.EQUIPMENT_TYPE.getValue(),
				XKDReportHardCodeValuesForLpv.SERVICE_ID.getValue(),
				XKDReportHardCodeValuesForLpv.CARRIER_ID.getValue(),
				XKDReportHardCodeValuesForLpv.UDF_VOLUME.getValue(),
				XKDReportHardCodeValuesForLpv.UDF_WEIGHT.getValue(),
				XKDReportHardCodeValuesForLpv.SHIP_STATE.getValue(),
				XKDReportHardCodeValuesForLpv.VENDOR_NAME.getValue(), XKDReportHardCodeValuesForLpv.LEG_ATA.getValue(),
				XKDReportHardCodeValuesForLpv.CONTAINER_ATA.getValue(),
				Optional.ofNullable(legActualShipDate)
						.map(l -> DateFormater.getDateStringFromTimeStamp(l, XKDReportConstants.XKD_REPORT_DATE_FORMAT))
						.orElse(StringUtils.EMPTY),
				Optional.ofNullable(atd)
						.map(a -> DateFormater.getDateStringFromTimeStamp(a, XKDReportConstants.XKD_REPORT_DATE_FORMAT))
						.orElse(StringUtils.EMPTY),
				Optional.ofNullable(LineTransactionId).orElse(StringUtils.EMPTY),
				Optional.ofNullable(containerETA).map(c -> {
					Integer offsetHours = PodcToDcLookUpService.dcToTransitTimeMap.get().get(to);
					Timestamp OffsetTimeStamp = null;
					if (offsetHours != null) {
						// add offset hours
						OffsetTimeStamp = DateFormater.addHoursToTimeStamp(c, offsetHours);
					}
					return DateFormater
							.getDateStringFromTimeStamp(Objects.nonNull(OffsetTimeStamp) ? OffsetTimeStamp : c

									, XKDReportConstants.XKD_REPORT_DATE_FORMAT);
				}).orElse(StringUtils.EMPTY), 
				Optional.ofNullable(legETA).map(l -> {
					Integer offsetHours = PodcToDcLookUpService.dcToTransitTimeMap.get().get(to);
					Timestamp OffsetTimeStamp = null;
					if (offsetHours != null) {
						// add offset hours
						OffsetTimeStamp = DateFormater.addHoursToTimeStamp(l, offsetHours);
					}
					return DateFormater.getDateStringFromTimeStamp(
							Objects.nonNull(OffsetTimeStamp) ? OffsetTimeStamp : l,
							XKDReportConstants.XKD_REPORT_DATE_FORMAT);
				}).orElse(StringUtils.EMPTY))
								// join all of them with comma....
								.map(s -> "\"" + s + "\"").collect(joining(","));
	}

}

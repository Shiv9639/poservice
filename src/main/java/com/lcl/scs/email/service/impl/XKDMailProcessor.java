package com.lcl.scs.email.service.impl;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.lcl.scs.constants.LpvConstants.BY_TOKEN;
import com.lcl.scs.constants.LpvConstants.EntityName;
import com.lcl.scs.constants.LpvConstants.LpvEndpointUrl;
import com.lcl.scs.email.constants.XKDReportConstants;
import com.lcl.scs.email.csv.domain.YardReportEntity;
import com.lcl.scs.email.service.AbstractMailService;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvToken;
import com.lcl.scs.subscribers.r9333.lpv.po.service.impl.LpvPoAPIServiceImpl;
import com.lcl.scs.subscribers.r9333.lpv.po.service.impl.LpvTokenServiceImpl;
import com.lcl.scs.util.DateFormater;
import com.lcl.scs.util.logging.LoggingUtilities;

@Service
public class XKDMailProcessor extends AbstractMailService {

	private static final String LPV_REPORT_NAME_PREFIX = "LCL_shipment_LAYM_XD_ID_";
	private static final String LPV_REPORT_NAME_SUFIX = ".csv";

	public void processMessage(Message message) {
		String subject  =null;
		try {
			subject  = message.getSubject();
			String reportGeneratedDateTime = getDateTimeFromSubject(subject);
			Multipart multipart = (Multipart) message.getContent();
			// parse only if it has attachment.
			if (multipart.getCount() > 1)
				for (int index = 1; index < multipart.getCount(); index++) {
					LoggingUtilities.generateInfoLog("processing XKD Report");
					BodyPart bodyPart = multipart.getBodyPart(index);
					InputStream stream = bodyPart.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(stream));

					CSVParser csvParser = new CSVParser(br,
							CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
					Iterable<CSVRecord> csvRecords = csvParser.getRecords();
					List<YardReportEntity> yardReportEntityList = StreamSupport.stream(csvRecords.spliterator(), false)
							.map(csvRecord -> {
								YardReportEntity entity = YardReportEntity.parseCsvToYardReportEntity(csvRecord);
								entity.setLineTransactionId(reportGeneratedDateTime);
								return entity;
							}).collect(Collectors.toList());

					LoggingUtilities.generateInfoLog("XKD csv record number: " + yardReportEntityList.size());
					
					generateAndPostLPVDeliveryCSVFile(yardReportEntityList);
					csvParser.close();

				}
		} catch (Exception e) {
			LoggingUtilities.generateErrorLog("Exception while procssing LPV xkd Report. Mail Subject:" + subject);
			e.printStackTrace();
		}

	}

	private LpvToken getLpvToken() throws Exception {
		LoggingUtilities.generateInfoLog("generating LPV token");
		LpvTokenServiceImpl tokenService = new LpvTokenServiceImpl(BY_TOKEN.URL.getValue(),
				BY_TOKEN.CLIENT_ID.getValue(), BY_TOKEN.CLIENT_SECRET.getValue(), BY_TOKEN.GRANT_TYPE.getValue(),
				BY_TOKEN.SCOPE.getValue());
		return tokenService.getLpvToken();
	}

	private void generateAndPostLPVDeliveryCSVFile(List<YardReportEntity> yardReportEntityList)
			throws Exception {
		try {
			String csvHeaders = String.join(",", XKDReportConstants.XKDReportHeadersForLpv);

			String csvFileContent = concat(of(csvHeaders),
					yardReportEntityList.stream().map(YardReportEntity::toLpvCsv))
							.collect(joining(System.lineSeparator()));
			LoggingUtilities.generateInfoLog("Posting to LPV");
			postToLpvApi(csvFileContent);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private void postToLpvApi(String csvFileContent) throws Exception {
		LpvPoAPIServiceImpl apiCall = new LpvPoAPIServiceImpl();
		apiCall.postCSVFile(getLpvToken(), generateLpvCsvReportName(), csvFileContent.getBytes(),
				LpvEndpointUrl.LPV_XKD_SHIPMENT_URL, EntityName.SHIPMENT);
	}

	private static String generateLpvCsvReportName() {
		return StringUtils.join(LPV_REPORT_NAME_PREFIX, DateFormater.getCurrentDateTime_yyyyMMddhhmmss(),
				LPV_REPORT_NAME_SUFIX);
	}

	private String getDateTimeFromSubject(String subject) {
		// Subject : [EXT] LCL Yard Portal Report 2021-04-07-08-02-06
		// Extract date and time from the subject and process.
		Pattern pattern = Pattern.compile("(\\d{4})[-](\\d{2})[-](\\d{2})[-](\\d{2})[-](\\d{2})[-](\\d{2})");
		Matcher matcher = pattern.matcher(subject);
		if (matcher.find()) {
			return matcher.group();
		} else {
			return null;
		}
	}

}

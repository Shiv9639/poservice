package com.lcl.scs.email.service.impl;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
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
import com.lcl.scs.email.constants.WHReportConstants;
import com.lcl.scs.email.constants.WHReportConstants.WHReportType;
import com.lcl.scs.email.csv.domain.WHReportEntity;
import com.lcl.scs.email.service.AbstractMailService;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvToken;
import com.lcl.scs.subscribers.r9333.lpv.po.service.impl.LpvPoAPIServiceImpl;
import com.lcl.scs.subscribers.r9333.lpv.po.service.impl.LpvTokenServiceImpl;
import com.lcl.scs.util.DateFormater;
import com.lcl.scs.util.logging.LoggingUtilities;

@Service
public class WHMailProcessor extends AbstractMailService {

	private static final String LPV_WH_MAX_REPORT_NAME_PREFIX = "LCL_CAPACITY_WH_MAX_REPORT";
	private static final String LPV_WH_PROJECTED_REPORT_NAME_PREFIX = "LCL_CAPACITY_WH_PROJECTED_REPORT";
	private static final String LPV_WHREPORT_NAME_SUFIX = ".csv";

	public void processMessage(Message message) {
		String subject = null;
		try {
			subject = message.getSubject();
			Multipart multipart = (Multipart) message.getContent();
			// parse only if it has attachment.
			if (multipart.getCount() > 1)
				for (int index = 1; index < multipart.getCount(); index++) {
					LoggingUtilities.generateInfoLog("processing WH Report");
					BodyPart bodyPart = multipart.getBodyPart(index);
					InputStream stream = bodyPart.getInputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(stream));

					CSVParser csvParser = new CSVParser(br,
							CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
					Iterable<CSVRecord> csvRecords = csvParser.getRecords();
					List<WHReportEntity> whMaxReportEntityList = StreamSupport.stream(csvRecords.spliterator(), false)
							.map(csvRecord -> {
								WHReportEntity entity = WHReportEntity.parseCsvToWHReportEntity(csvRecord,
										WHReportType.MAX_STORAGE_TYPE);
								return entity;
							}).collect(Collectors.toList());

					List<WHReportEntity> whProjectedReportEntityList = StreamSupport
							.stream(csvRecords.spliterator(), false).map(csvRecord -> {
								WHReportEntity entity = WHReportEntity.parseCsvToWHReportEntity(csvRecord,
										WHReportType.PROJECTED_STORAGE_TYPE);
								return entity;
							}).collect(Collectors.toList());

					LoggingUtilities
							.generateInfoLog("Number of csv record to process: " + whMaxReportEntityList.size());

					generateAndPostLPVDeliveryCSVFile(whMaxReportEntityList, WHReportType.MAX_STORAGE_TYPE);

					generateAndPostLPVDeliveryCSVFile(whProjectedReportEntityList, WHReportType.PROJECTED_STORAGE_TYPE);

					csvParser.close();

				}
		} catch (Exception e) {
			LoggingUtilities.generateErrorLog("Exception while procssing LPV WH Report. Mail Subject:" + subject);
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

	private void generateAndPostLPVDeliveryCSVFile(List<WHReportEntity> whReportEntityList, WHReportType reportType)
			throws Exception {
		try {
			String csvHeaders = String.join(",", WHReportConstants.WHReportHeadersForLpv);
			if (reportType == WHReportType.MAX_STORAGE_TYPE) {
				String csvMaxFileContent = concat(of(csvHeaders),
						whReportEntityList.stream().map(e -> e.toLpvCsv(WHReportType.MAX_STORAGE_TYPE)))
								.collect(joining(System.lineSeparator()));

				LoggingUtilities.generateInfoLog("Posting Capacity Max Report to LPV");
				postToLpvApi(csvMaxFileContent, reportType);
			}

			else {
				String csvProjectedFileContent = concat(of(csvHeaders),
						whReportEntityList.stream().map(e -> e.toLpvCsv(WHReportType.PROJECTED_STORAGE_TYPE)))
								.collect(joining(System.lineSeparator()));

				LoggingUtilities.generateInfoLog("Posting Capacity Max Report to LPV");
				postToLpvApi(csvProjectedFileContent, reportType);
			}

		} catch (Exception ex) {
			throw ex;
		}
	}

	private void postToLpvApi(String csvFileContent, WHReportType reportType) throws Exception {
		LpvPoAPIServiceImpl apiCall = new LpvPoAPIServiceImpl();
		apiCall.postCSVFile(getLpvToken(), generateLpvCsvReportName(reportType), csvFileContent.getBytes(),
				LpvEndpointUrl.LPV_CAPACITY_URL, EntityName.CAPACITY);

	}

	private static String generateLpvCsvReportName(WHReportType reportType) {
		return reportType == WHReportType.MAX_STORAGE_TYPE
				? StringUtils.join(LPV_WH_MAX_REPORT_NAME_PREFIX, DateFormater.getCurrentDateTime_yyyyMMddhhmmss(),
						LPV_WHREPORT_NAME_SUFIX)
				: StringUtils.join(LPV_WH_PROJECTED_REPORT_NAME_PREFIX,
						DateFormater.getCurrentDateTime_yyyyMMddhhmmss(), LPV_WHREPORT_NAME_SUFIX);
	}

}

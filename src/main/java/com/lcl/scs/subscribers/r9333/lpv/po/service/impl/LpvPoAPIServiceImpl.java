package com.lcl.scs.subscribers.r9333.lpv.po.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.lcl.scs.constants.LpvConstants.EntityName;
import com.lcl.scs.constants.LpvConstants.LpvEndpointUrl;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvPoInterface;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvPoInterfaceResponse;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvReasonCode;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvReasonCodeTransaction;
import com.lcl.scs.subscribers.r9333.lpv.po.model.LpvToken;
import com.lcl.scs.subscribers.r9333.lpv.po.service.R9333LpvPoService;
import com.lcl.scs.util.logging.LoggingUtilities;

public class LpvPoAPIServiceImpl {
	private final String CONTENT_TYPE = "application/csv";
	private String fileName;
	private String authorization;
	private final String SENDER = "LOBLAWS.INC";
	private final String RECEIVERS = "LCT.GLOBAL";
	private final String BULK_FORMAT = "CSV";
	private final String ENTITY_VERSION = "BY-2020.1.0";
	private final String MODEL = "native";
	private final String RESPONSE_UTC_TIME_FORMATTER = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private final String OUTPUT_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss.SSS z";
	private com.lcl.scs.subscribers.r9333.lpv.po.service.LpvReasonCodeService lpvReasonCodeService;
	private R9333LpvPoService r9333LpvPoService;
//	public void postCSVFileSpring(File csvFile, LpvToken token) throws Exception{
//		try {
//			org.springframework.http.HttpHeaders headers=new org.springframework.http.HttpHeaders();
//	        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//	        
//	        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//	        body.add("file", csvFile);
//
//
//	        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//	        String serverUrl = "http://localhost:8082/spring-rest/fileserver/singlefileupload/";
//	        RestTemplate restTemplate = new RestTemplate();
//	        ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, requestEntity, String.class);
//		}catch(Exception ex) {
//			throw ex;
//		}
//	}

	private List<BasicNameValuePair> getLpvParams(String entityName) {
		List<BasicNameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("sender", SENDER));
		params.add(new BasicNameValuePair("receivers", RECEIVERS));
		params.add(new BasicNameValuePair("entity", entityName));
		params.add(new BasicNameValuePair("bulkFormat", BULK_FORMAT));
		params.add(new BasicNameValuePair("entityVersion", ENTITY_VERSION));
		params.add(new BasicNameValuePair("model", MODEL));
		return params;
	}

	public void postCSVFile(LpvToken token, String fileName, byte[] data, LpvEndpointUrl url, EntityName entityName)
			throws Exception {

		HttpClient client = HttpClientBuilder.create().build();

		HttpPost post = new HttpPost(url.getValue());
		post.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
		post.setHeader("File-Name", fileName);
		post.setHeader(HttpHeaders.AUTHORIZATION, token.getToken_type() + " " + token.getAccess_token());
		post.setEntity(new UrlEncodedFormEntity(getLpvParams(entityName.getValue())));
		post.setEntity(EntityBuilder.create().setBinary(data)
				.setContentType(org.apache.http.entity.ContentType.create("application/csv")).build());
		LoggingUtilities.generateInfoLog("Posting " + fileName + "to lpv");
		processLpvResponse(client.execute(post), url);
	}

	public void postCSVFile(File csvFile, LpvToken token, LpvEndpointUrl url, EntityName entityName, LpvPoInterface po) throws Exception {
		try {
			HttpClient client = HttpClientBuilder.create().build();

			HttpPost post = new HttpPost(url.getValue().toString().concat("&bulkId="+csvFile.getName().replace(".csv", "")));
			post.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
			post.setHeader("File-Name", csvFile.getName());
			post.setHeader(HttpHeaders.AUTHORIZATION, token.getToken_type() + " " + token.getAccess_token());

			post.setEntity(new UrlEncodedFormEntity(getLpvParams(entityName.getValue())));
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			ContentType contentType = org.apache.http.entity.ContentType.create("application/csv",
//					new BasicNameValuePair(MIME.CONTENT_DISPOSITION, "Binary"));
//
//			builder.setContentType(ContentType.DEFAULT_BINARY);
//			builder.addBinaryBody("file", csvFile, contentType, csvFile.getName());
//
//			HttpEntity entity = builder.build();

			// post.setEntity(entity);

			post.setEntity(EntityBuilder.create().setBinary(Files.readAllBytes(csvFile.toPath()))
					.setContentType(org.apache.http.entity.ContentType.create("application/csv")).build());

			HttpResponse response = client.execute(post);

			processLpvResponse(response, url,po);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private void processLpvResponse(HttpResponse response, LpvEndpointUrl url, LpvPoInterface po) throws IOException, Exception {
		LoggingUtilities.generateInfoLog("Got response from lpv.");
		if (response == null) {
			LoggingUtilities.generateErrorLog("Empty LCT API Response: ");
			throw new Exception("Invalid Response from " + url.getValue());
		}
		int code = response.getStatusLine().getStatusCode();
		HttpEntity responseEntity = response.getEntity();
		String responseStr = EntityUtils.toString(responseEntity);

		if (code == 202) {
			JSONObject responseJSON = new JSONObject(responseStr);

			LpvPoInterfaceResponse resp = new LpvPoInterfaceResponse();

			LoggingUtilities.generateInfoLog("LCT API Response: " + responseJSON);
			resp.setIngestionId(responseJSON.getString("ingestionId"));

			try {
				resp.setStatus(responseJSON.getString("status"));

				if (responseJSON.get("timeStamp") != null) {
					try {
						LocalDateTime utc_time = LocalDateTime.parse(responseJSON.get("timeStamp").toString(),
								DateTimeFormatter.ofPattern(RESPONSE_UTC_TIME_FORMATTER));
						ZoneId utc = ZoneId.of("UTC");
						ZoneId est = ZoneId.of("Canada/Eastern");
						ZonedDateTime utcZonedDateTime = utc_time.atZone(utc);
						ZonedDateTime estDateTime = utcZonedDateTime.withZoneSameInstant(est);
						resp.setTimeStamp(estDateTime);
					} catch (Exception ex) {
						LocalDateTime et = LocalDateTime.now();
						ZoneId est = ZoneId.of("Canada/Eastern");
						ZonedDateTime etTime = et.atZone(est);
						ZonedDateTime estDateTime = etTime.withZoneSameInstant(est);
						resp.setTimeStamp(estDateTime);
					}
				}

				resp.setMessage(responseJSON.getString("message"));

				LoggingUtilities.generateInfoLog("LCT API Message: "
						+ resp.getTimeStamp().format(DateTimeFormatter.ofPattern(OUTPUT_TIME_FORMATTER)) + ": "
						+ resp.getMessage());
			} catch (Exception ex) {
				LoggingUtilities.generateErrorLog("Response Code: " + code);
				LoggingUtilities.generateInfoLog("LCT API Response: " + responseStr);
				//Reason Code: 00 for successfully posted CSV file
				if(url.equals(LpvEndpointUrl.LPV_PO_URL)) {
					List<LpvReasonCode> lpvReasonCodeList = getR9333LpvPoService().findReasonCode("00");
					LpvReasonCodeTransaction lpvReasonCodeTransaction = getLpvReasonCodeService()
							.publishLpvReasonCodeTransaction(lpvReasonCodeList, po);
					getLpvReasonCodeService().saveLpvReasonCodeTransaction(lpvReasonCodeTransaction);
					}
			}
		} else {
			LoggingUtilities.generateErrorLog("Received failure from lpv");
			LoggingUtilities.generateErrorLog("Response Code: " + code);
			LoggingUtilities.generateInfoLog("LCT API Response: " + responseStr);
			// ReasonCode 99 :when any random exception is caught
			if(url.equals(LpvEndpointUrl.LPV_PO_URL)) {
			List<LpvReasonCode> lpvReasonCodeList = getR9333LpvPoService().findReasonCode("99");
			LpvReasonCodeTransaction lpvReasonCodeTransaction = getLpvReasonCodeService()
					.publishLpvReasonCodeTransaction(lpvReasonCodeList, po);
			getLpvReasonCodeService().saveLpvReasonCodeTransaction(lpvReasonCodeTransaction);
			}
			throw new Exception("Invalid Response from " + url.getValue());
		}
	}
	private void processLpvResponse(HttpResponse response, LpvEndpointUrl url) throws IOException, Exception {
		LoggingUtilities.generateInfoLog("Got response from lpv.");
		if (response == null) {
			LoggingUtilities.generateErrorLog("Empty LCT API Response: ");
			throw new Exception("Invalid Response from " + url.getValue());
		}
		int code = response.getStatusLine().getStatusCode();
		HttpEntity responseEntity = response.getEntity();
		String responseStr = EntityUtils.toString(responseEntity);

		if (code == 202) {
			JSONObject responseJSON = new JSONObject(responseStr);

			LpvPoInterfaceResponse resp = new LpvPoInterfaceResponse();

			LoggingUtilities.generateInfoLog("LCT API Response: " + responseJSON);
			resp.setIngestionId(responseJSON.getString("ingestionId"));

			try {
				resp.setStatus(responseJSON.getString("status"));

				if (responseJSON.get("timeStamp") != null) {
					try {
						LocalDateTime utc_time = LocalDateTime.parse(responseJSON.get("timeStamp").toString(),
								DateTimeFormatter.ofPattern(RESPONSE_UTC_TIME_FORMATTER));
						ZoneId utc = ZoneId.of("UTC");
						ZoneId est = ZoneId.of("Canada/Eastern");
						ZonedDateTime utcZonedDateTime = utc_time.atZone(utc);
						ZonedDateTime estDateTime = utcZonedDateTime.withZoneSameInstant(est);
						resp.setTimeStamp(estDateTime);
					} catch (Exception ex) {
						LocalDateTime et = LocalDateTime.now();
						ZoneId est = ZoneId.of("Canada/Eastern");
						ZonedDateTime etTime = et.atZone(est);
						ZonedDateTime estDateTime = etTime.withZoneSameInstant(est);
						resp.setTimeStamp(estDateTime);
					}
				}

				resp.setMessage(responseJSON.getString("message"));

				LoggingUtilities.generateInfoLog("LCT API Message: "
						+ resp.getTimeStamp().format(DateTimeFormatter.ofPattern(OUTPUT_TIME_FORMATTER)) + ": "
						+ resp.getMessage());
			} catch (Exception ex) {
				LoggingUtilities.generateErrorLog("Response Code: " + code);
				LoggingUtilities.generateInfoLog("LCT API Response: " + responseStr);
			}
		} else {
			LoggingUtilities.generateErrorLog("Received failure from lpv");
			LoggingUtilities.generateErrorLog("Response Code: " + code);
			LoggingUtilities.generateInfoLog("LCT API Response: " + responseStr);
			// ReasonCode 99 :when any random exception is caught
		}
	}
	public String getAuthorization() {
		return authorization;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public com.lcl.scs.subscribers.r9333.lpv.po.service.LpvReasonCodeService getLpvReasonCodeService() {
		return lpvReasonCodeService;
	}

	public void setLpvReasonCodeService(
			com.lcl.scs.subscribers.r9333.lpv.po.service.LpvReasonCodeService lpvReasonCodeService) {
		this.lpvReasonCodeService = lpvReasonCodeService;
	}

	public R9333LpvPoService getR9333LpvPoService() {
		return r9333LpvPoService;
	}

	public void setR9333LpvPoService(R9333LpvPoService r9333LpvPoService) {
		this.r9333LpvPoService = r9333LpvPoService;
	}

}

package com.lcl.scs.subscribers.r9333.lpv.po.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.lcl.scs.subscribers.r9333.lpv.po.service.LpvPoInterfaceService;
import com.lcl.scs.subscribers.r9333.lpv.po.service.LpvReasonCodeService;
import com.lcl.scs.subscribers.r9333.lpv.po.service.impl.R9333LpvPoServiceImpl;
import com.lcl.scs.util.logging.LoggingUtilities;

@Component
public class LPVR9333Scheduler {
	private static final SimpleDateFormat DATETIMEFORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// @Value("${spring.data.mongodb.uri}")
	private String mongodbURI = System.getenv("MongoDBURI");
	// @Value("${spring.data.mongodb.database}")
	private String mongodbDatabase = System.getenv("MongoDBDatabaseName");

	// @Value("${spring.data.by.token.url}")
	private final String BY_TOKEN_URL = System.getenv("BY_TOKEN_URL");
	// @Value("${spring.data.by.token.client.id}")
	private final String BY_TOKEN_CLIENT_ID = System.getenv("BY_TOKEN_CLIENT_ID");
	// @Value("${spring.data.by.token.client.secret}")
	private final String BY_TOKEN_CLIENT_SECRET = System.getenv("BY_TOKEN_CLIENT_SECRET");
	// @Value("${spring.data.by.token.grant.type}")
	private final String BY_TOKEN_GRANT_TYPE = System.getenv("BY_TOKEN_GRANT_TYPE");
	// @Value("${spring.data.by.token.token.scope}")
	private final String BY_TOKEN_SCOPE = System.getenv("BY_TOKEN_SCOPE");

	@Autowired
	private com.lcl.scs.subscribers.service.SubscribersService subscribersService;

	@Autowired
	private LpvPoInterfaceService lpvPoInterfaceService;
	
	@Autowired
	private LpvReasonCodeService lpvReasonCodeService;
	
	
	@Scheduled(fixedDelay = 1000 * 60 * 1)
	private void processLPVR9333Scheduler() {
		LoggingUtilities.generateInfoLog(DATETIMEFORMATTER.format(new Date()) + ": Start sending all new files");
		try {
			LoggingUtilities.generateInfoLog("MongoDB URI: " + mongodbURI);
			LoggingUtilities.generateInfoLog("MongoDB Database Name: " + mongodbDatabase);

			R9333LpvPoServiceImpl r9333ServiceImpl = new R9333LpvPoServiceImpl();
			r9333ServiceImpl.setSubscribersService(subscribersService);
			r9333ServiceImpl.setLpvPoInterfaceService(lpvPoInterfaceService);
			r9333ServiceImpl.setLpvReasonCodeService(lpvReasonCodeService);
			r9333ServiceImpl.setR9333LpvPoService(r9333ServiceImpl);
			r9333ServiceImpl.setMongodbDatabase(mongodbDatabase);
			r9333ServiceImpl.setMongodbURI(mongodbURI);

			r9333ServiceImpl.setBY_TOKEN_URL(BY_TOKEN_URL);
			r9333ServiceImpl.setBY_TOKEN_CLIENT_ID(BY_TOKEN_CLIENT_ID);
			r9333ServiceImpl.setBY_TOKEN_CLIENT_SECRET(BY_TOKEN_CLIENT_SECRET);
			r9333ServiceImpl.setBY_TOKEN_GRANT_TYPE(BY_TOKEN_GRANT_TYPE);
			r9333ServiceImpl.setBY_TOKEN_SCOPE(BY_TOKEN_SCOPE);
			
			r9333ServiceImpl.processR9333Interface();
		} catch (Exception ex) {
			LoggingUtilities.generateErrorLog("Failed to send all new files! " + ex.getMessage());
			ex.printStackTrace();
		}
		LoggingUtilities
				.generateInfoLog(DATETIMEFORMATTER.format(new Date()) + ": Finish sending all files to LCT");

	}
}

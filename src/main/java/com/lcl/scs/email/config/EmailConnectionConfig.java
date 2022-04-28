package com.lcl.scs.email.config;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class EmailConnectionConfig {

	@Value("${spring.mail.port}")
	private String port;
	@Value("${spring.mail.host}")
	private String host;
	@Value("${spring.mail.username}")
	private String userName;
	@Value("${spring.mail.password}")
	private String password;

	@Bean(name = "ltscsMailBox")
	protected Store createLtscsMailBoxconnection() throws MessagingException {
		Store emailStore;
		Properties properties = new Properties();
		properties.put(MailConfigurationConstants.HOST_KEY.getValue(), host);
		properties.put(MailConfigurationConstants.PORT_KEY.getValue(), port);
		properties.put(MailConfigurationConstants.PROTOCOL_KEY.getValue(),
				MailConfigurationConstants.PROTOCOL.getValue());
		Session emailSession = Session.getDefaultInstance(properties);
		emailStore = emailSession.getStore(MailConfigurationConstants.PROTOCOL.getValue());
		emailStore.connect(host, userName, password);
		return emailStore;
	}

	private enum MailConfigurationConstants {
		HOST_KEY("mail.smtp.host"), PORT_KEY("mail.smtp.port"), PROTOCOL_KEY("mail.store.protocol"), PROTOCOL("imaps");

		private String value;

		private MailConfigurationConstants(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

}

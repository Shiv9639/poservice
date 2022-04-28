package com.lcl.scs.email.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.lcl.scs.util.logging.LoggingUtilities;

@Service
public class AbstractMailService {

	@Autowired
	Store store;

	Folder ltscsMailBox;
	@Value("${email.sender.implementation}")
	private String emailSenderImplementaion;

	private static Map<String, String> emailSenderClassImplementationMap = new HashMap<>();

	private static final String METHOD_NAME_TO_PROCESS = "processMessage";
	
	@Value("${email.read.enable}")
	private String emailReadEnable;

	// default constructor.
	public AbstractMailService() {
		super();
	}

	@PostConstruct
	private void getEmailSenderImplementaion() {

		emailSenderClassImplementationMap = Pattern.compile("\\s*,\\s*").splitAsStream(emailSenderImplementaion.trim())
				.map(s -> s.split("::", 2)).collect(Collectors.toMap(a -> a[0], a -> a[1]));
	}

	private static Function<String, String> getEmailProcessingClass = senderEmail -> {
		return emailSenderClassImplementationMap.get(senderEmail);

	};

	private static Predicate<String> isEmailConfiguredforProcess = emailId -> emailSenderClassImplementationMap
			.containsKey(emailId);

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Scheduled(fixedDelay = 1000 * 60 * 1)
	public void processMail() {
		if (emailReadEnable != null && emailReadEnable.equals("Y")) {
			try {
				if (ltscsMailBox == null || !ltscsMailBox.isOpen()) {
					ltscsMailBox = store.getFolder("INBOX");
					ltscsMailBox.open(Folder.READ_WRITE);
				}
				Stream<Message> messages = Arrays
						.stream(ltscsMailBox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false)));
				messages.forEach(message -> {
					try {
						Address fromAddress = message.getFrom()[0];
						if (isEmailConfiguredforProcess.test(fromAddress.toString())) {
							// refer to properties file for implementation class.
							LoggingUtilities.generateInfoLog("got mail that needs to be processed.");
							Class t = Class.forName(getEmailProcessingClass.apply(fromAddress.toString()));
							Object instance = t.getDeclaredConstructor().newInstance();
							t.getDeclaredMethod(METHOD_NAME_TO_PROCESS, Message.class).invoke(instance, message);
							message.setFlag(Flags.Flag.SEEN, true);
							LoggingUtilities.generateInfoLog("done processing email.");
						}

					} catch (MessagingException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						LoggingUtilities.generateErrorLog(
								"Implementatio Class is not defined. Need to implement subclass to process email");
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						LoggingUtilities.generateErrorLog(
								"processMessage method declaration is not found in implementation class");
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}

				});

			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	protected void processMessage(Message message) {
		// should be implement by subclass
	}
}

package org.rhq.msg.sample.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "QueueName") })
public class MyMDB implements MessageListener {
	public MyMDB() {
		System.out.println("===> MyMDB init (QueueName)");
	}

	public void onMessage(Message message) {
		try {
			TextMessage textMessage = (TextMessage) message;
			System.out.println("===> MyMDB Received (QueueName): "
					+ textMessage.getText());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

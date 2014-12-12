package org.rhq.msg.sample.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import org.rhq.msg.common.SimpleBasicMessage;
import org.rhq.msg.mdb.RPCBasicMessageDrivenBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "QueueName") })
public class MyMDB extends RPCBasicMessageDrivenBean<SimpleBasicMessage, SimpleBasicMessage> {
    private final Logger log = LoggerFactory.getLogger(MyMDB.class);

    public MyMDB() {
        log.info("===> MyMDB init (QueueName)");
	}

    protected SimpleBasicMessage onBasicMessage(SimpleBasicMessage msg) {
        log.info("===> MyMDB Received (QueueName): {}", msg);
        SimpleBasicMessage response = new SimpleBasicMessage("ECHO! " + msg.getMessage());
        return response;
    };
}

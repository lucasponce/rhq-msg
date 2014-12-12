package org.rhq.msg.sample.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import org.rhq.msg.common.SimpleBasicMessage;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "QueueName"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "MyFilter = 'fnf'") })
public class MyMDB extends BasicMessageListener<SimpleBasicMessage> {
    private final Logger log = LoggerFactory.getLogger(MyMDB.class);

    protected void onBasicMessage(SimpleBasicMessage msg) {
        log.info("===> MDB received message [{}]", msg);
    };
}

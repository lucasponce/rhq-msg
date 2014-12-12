package org.rhq.msg.sample.mdb;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.ConnectionFactory;

import org.rhq.msg.common.SimpleBasicMessage;
import org.rhq.msg.mdb.BasicMessageDrivenBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "QueueName"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "MyFilter = 'fnf'") })
public class MyMDB extends BasicMessageDrivenBean<SimpleBasicMessage> {
    private final Logger log = LoggerFactory.getLogger(MyMDB.class);

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Override
    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    protected void onBasicMessage(SimpleBasicMessage msg) {
        log.info("===> MDB received message [{}]", msg);
    };
}

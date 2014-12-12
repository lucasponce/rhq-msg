package org.rhq.msg.sample.mdb;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.ConnectionFactory;

import org.rhq.msg.common.SimpleBasicMessage;
import org.rhq.msg.mdb.RPCBasicMessageDrivenBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MessageDriven(activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "QueueName"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "MyFilter = 'rpc'") })
public class MyRPCMDB extends RPCBasicMessageDrivenBean<SimpleBasicMessage, SimpleBasicMessage> {
    private final Logger log = LoggerFactory.getLogger(MyRPCMDB.class);

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Override
    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    protected SimpleBasicMessage onBasicMessage(SimpleBasicMessage msg) {
        log.info("===> MDB received incoming RPC message [{}]", msg);
        SimpleBasicMessage response = new SimpleBasicMessage("ECHO! " + msg.getMessage());
        log.info("===> MDB sending response RPC message [{}]", response);
        return response;
    };
}

package org.rhq.msg.mdb;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;

import org.rhq.msg.common.BasicMessage;
import org.rhq.msg.common.ConnectionContextFactory;
import org.rhq.msg.common.Endpoint;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.rhq.msg.common.consumer.ConsumerConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicMessageDrivenBean<T extends BasicMessage> extends BasicMessageListener<T> {
    private final Logger log = LoggerFactory.getLogger(BasicMessageDrivenBean.class);

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    public ConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    @Override
    public ConsumerConnectionContext getConsumerConnectionContext() {
        ConsumerConnectionContext ctx = null;
        try {
            // here we build a faux consumer connection context whose data will be duplicated in
            // a producer connection context so the response message can be sent. The endpoint we
            // use here is just a dummy one - it will be replaced with the JMS ReplyTo by the superclass.
            ConnectionContextFactory ccf = new ConnectionContextFactory(getConnectionFactory());
            ctx = ccf.createConsumerConnectionContext(Endpoint.TEMPORARY_QUEUE);
        } catch (Exception e) {
            log.error("Failed to build context - will not be able to respond to message", e);
        }
        return ctx;
    }
}
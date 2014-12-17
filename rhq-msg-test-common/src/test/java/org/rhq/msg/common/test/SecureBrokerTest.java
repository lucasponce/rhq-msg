package org.rhq.msg.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.jms.JMSSecurityException;

import org.junit.Test;
import org.rhq.msg.common.ConnectionContextFactory;
import org.rhq.msg.common.Endpoint;
import org.rhq.msg.common.Endpoint.Type;
import org.rhq.msg.common.MessageProcessor;
import org.rhq.msg.common.consumer.ConsumerConnectionContext;
import org.rhq.msg.common.producer.ProducerConnectionContext;

/**
 * Tests connecting to a secured broker.
 */
public class SecureBrokerTest {
    private static final String USER1_NAME = "user1";
    private static final String USER1_PASSWORD = "user1pw";

    @Test
    public void testLoginFailure() throws Exception {
        ConnectionContextFactory consumerFactory = null;

        SecureTCPEmbeddedBrokerWrapper broker = new SecureTCPEmbeddedBrokerWrapper();
        broker.start();

        try {
            String brokerURL = broker.getBrokerURL();
            consumerFactory = new ConnectionContextFactory(brokerURL); // unauthenticated

            // first check that this fails - we can't connect to a secured broker without logging in
            try {
                consumerFactory.createConsumerConnectionContext(new Endpoint(Type.QUEUE, "testq"));
                assert false : "Should not have been able to connect - we did not authenticate";
            } catch (JMSSecurityException expected) {
                // to be expected
            }
        } finally {
            // close everything
            consumerFactory.close();
            broker.stop();
        }
    }

    @Test
    public void testSecurity() throws Exception {
        ConnectionContextFactory consumerFactory = null;
        ConnectionContextFactory producerFactory = null;

        SecureTCPEmbeddedBrokerWrapper broker = new SecureTCPEmbeddedBrokerWrapper();
        broker.start();

        try {
            String brokerURL = broker.getBrokerURL();
            Endpoint endpoint = new Endpoint(Type.QUEUE, "testq");
            SpecificMessage specificMessage = new SpecificMessage("hello", null, "specific text");

            // mimic server-side
            consumerFactory = new ConnectionContextFactory(brokerURL, USER1_NAME, USER1_PASSWORD);
            ConsumerConnectionContext consumerContext = consumerFactory.createConsumerConnectionContext(endpoint);
            SimpleTestListener<SpecificMessage> listener = new SimpleTestListener<SpecificMessage>(SpecificMessage.class);
            MessageProcessor serverSideProcessor = new MessageProcessor();
            serverSideProcessor.listen(consumerContext, listener);

            // mimic client side
            producerFactory = new ConnectionContextFactory(brokerURL, USER1_NAME, USER1_PASSWORD);
            ProducerConnectionContext producerContext = producerFactory.createProducerConnectionContext(endpoint);
            MessageProcessor clientSideProcessor = new MessageProcessor();

            // send message
            clientSideProcessor.send(producerContext, specificMessage);

            // wait for the message to flow
            assertTrue(listener.waitForMessage(3));
            assertEquals("Should have received the message", listener.getReceivedMessage().getSpecific(), "specific text");

        } finally {
            // close everything
            producerFactory.close();
            consumerFactory.close();
            broker.stop();
        }
    }
}

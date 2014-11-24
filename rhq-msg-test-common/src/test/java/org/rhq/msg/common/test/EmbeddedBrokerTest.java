package org.rhq.msg.common.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.rhq.msg.common.BasicMessage;
import org.rhq.msg.common.Endpoint;
import org.rhq.msg.common.Endpoint.Type;
import org.junit.Assert;
import org.junit.Test;

/**
 * This test class shows usages of the different Embedded Broker Wrapper objects
 * as well as the convenience connections for both consumer and producer.
 */
public class EmbeddedBrokerTest {

    @Test
    public void testInternalVMBrokerQueue() throws Exception {
        internalTestBroker(new VMEmbeddedBrokerWrapper(), new Endpoint(Type.QUEUE, "testq"));
    }

    @Test
    public void testInternalVMBrokerTopic() throws Exception {
        internalTestBroker(new VMEmbeddedBrokerWrapper(), new Endpoint(Type.TOPIC, "testtopic"));
    }

    @Test
    public void testTCPBrokerQueue() throws Exception {
        internalTestBroker(new TCPEmbeddedBrokerWrapper(), new Endpoint(Type.QUEUE, "testq"));
    }

    @Test
    public void testTCPBrokerTopic() throws Exception {
        internalTestBroker(new TCPEmbeddedBrokerWrapper(), new Endpoint(Type.TOPIC, "testtopic"));
    }

    private void internalTestBroker(AbstractEmbeddedBrokerWrapper broker, Endpoint endpoint) throws Exception {
        broker.start();
        assert broker.getBroker().isBrokerStarted() : "Broker should have been started by now";

        try {
            String brokerURL = broker.getBrokerURL();

            // test that messages can flow to the given broker
            Map<String, String> details = new HashMap<String, String>();
            details.put("key1", "val1");
            details.put("secondkey", "secondval");
            BasicMessage basicMessage = new BasicMessage("Hello World!", details);

            CountDownLatch latch = new CountDownLatch(1);
            ArrayList<BasicMessage> receivedMessages = new ArrayList<BasicMessage>();
            ArrayList<String> errors = new ArrayList<String>();

            // start the consumer
            StoreAndLatchBasicMessageListener<BasicMessage> messageListener = new StoreAndLatchBasicMessageListener<BasicMessage>(latch, receivedMessages,
                    errors);
            ConsumerConnection consumerConnection = new ConsumerConnection(brokerURL, endpoint, messageListener);

            // start the producer
            ProducerConnection producerConnection = new ProducerConnection(brokerURL, endpoint);
            producerConnection.sendMessage(basicMessage.toJSON());

            // wait for the message to flow
            boolean gotMessage = latch.await(5, TimeUnit.SECONDS);
            if (!gotMessage) {
                errors.add("Timed out waiting for message - it never showed up");
            }

            // close everything
            producerConnection.close();
            consumerConnection.close();

            // make sure the message flowed properly
            Assert.assertTrue("Failed to send message properly: " + errors, errors.isEmpty());
            Assert.assertEquals("Didn't receive message: " + receivedMessages, receivedMessages.size(), 1);
            BasicMessage receivedBasicMessage = receivedMessages.get(0);
            Assert.assertEquals(receivedBasicMessage.getMessage(), basicMessage.getMessage());
            Assert.assertEquals(receivedBasicMessage.getDetails(), basicMessage.getDetails());
        } finally {
            broker.stop();
        }
    }

    @Test
    public void testSubClassingBasicMessage() throws Exception {
        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
        assert !broker.getBroker().isBrokerStarted() : "Broker should not have been started yet";
        broker.start();
        assert broker.getBroker().isBrokerStarted() : "Broker should have been started by now";

        try {
            String brokerURL = broker.getBrokerURL();
            Endpoint endpoint = new Endpoint(Type.QUEUE, "testq");

            // test that sending messages of a BasicMessage subclass type can flow
            Map<String, String> details = new HashMap<String, String>();
            details.put("key1", "val1");
            details.put("secondkey", "secondval");
            SpecificMessage specificMessage = new SpecificMessage("hello", details, "specific text");

            CountDownLatch latch = new CountDownLatch(1);
            ArrayList<SpecificMessage> receivedMessages = new ArrayList<SpecificMessage>();
            ArrayList<String> errors = new ArrayList<String>();

            // start the consumer listening for our subclass SpecificMessage objects
            StoreAndLatchBasicMessageListener<SpecificMessage> messageListener = new StoreAndLatchBasicMessageListener<SpecificMessage>(latch,
                    receivedMessages, errors, SpecificMessage.class);
            ConsumerConnection consumerConnection = new ConsumerConnection(brokerURL, endpoint, messageListener);

            // start the producer
            ProducerConnection producerConnection = new ProducerConnection(brokerURL, endpoint);
            producerConnection.sendMessage(specificMessage.toJSON());

            // wait for the message to flow
            boolean gotMessage = latch.await(5, TimeUnit.SECONDS);
            if (!gotMessage) {
                errors.add("Timed out waiting for message - it never showed up");
            }

            // close everything
            producerConnection.close();
            consumerConnection.close();

            // make sure the message flowed properly
            Assert.assertTrue("Failed to send message properly: " + errors, errors.isEmpty());
            Assert.assertEquals("Didn't receive message: " + receivedMessages, receivedMessages.size(), 1);
            SpecificMessage receivedSpecificMessage = receivedMessages.get(0);
            Assert.assertEquals(receivedSpecificMessage.getMessage(), specificMessage.getMessage());
            Assert.assertEquals(receivedSpecificMessage.getDetails(), specificMessage.getDetails());
            Assert.assertEquals(receivedSpecificMessage.getSpecific(), specificMessage.getSpecific());
        } finally {
            broker.stop();
        }
    }

    @Test
    public void testSubClassingBasicMessageAndListener() throws Exception {
        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
        broker.start();

        try {
            String brokerURL = broker.getBrokerURL();
            Endpoint endpoint = new Endpoint(Type.QUEUE, "testq");

            // test that sending messages of a BasicMessage subclass type can flow
            Map<String, String> details = new HashMap<String, String>();
            details.put("key1", "val1");
            details.put("secondkey", "secondval");
            SpecificMessage specificMessage = new SpecificMessage("hello", details, "specific text");

            CountDownLatch latch = new CountDownLatch(1);
            ArrayList<SpecificMessage> receivedMessages = new ArrayList<SpecificMessage>();
            ArrayList<String> errors = new ArrayList<String>();

            // start the consumer listening for our subclass SpecificMessage objects
            SpecificMessageStoreAndLatchListener messageListener = new SpecificMessageStoreAndLatchListener(latch, receivedMessages, errors);
            ConsumerConnection consumerConnection = new ConsumerConnection(brokerURL, endpoint, messageListener);

            // start the producer
            ProducerConnection producerConnection = new ProducerConnection(brokerURL, endpoint);
            producerConnection.sendMessage(specificMessage.toJSON());

            // wait for the message to flow
            boolean gotMessage = latch.await(5, TimeUnit.SECONDS);
            if (!gotMessage) {
                errors.add("Timed out waiting for message - it never showed up");
            }

            // close everything
            producerConnection.close();
            consumerConnection.close();

            // make sure the message flowed properly
            Assert.assertTrue("Failed to send message properly: " + errors, errors.isEmpty());
            Assert.assertEquals("Didn't receive message: " + receivedMessages, receivedMessages.size(), 1);
            SpecificMessage receivedSpecificMessage = receivedMessages.get(0);
            Assert.assertEquals(receivedSpecificMessage.getMessage(), specificMessage.getMessage());
            Assert.assertEquals(receivedSpecificMessage.getDetails(), specificMessage.getDetails());
            Assert.assertEquals(receivedSpecificMessage.getSpecific(), specificMessage.getSpecific());
        } finally {
            broker.stop();
        }
    }
}

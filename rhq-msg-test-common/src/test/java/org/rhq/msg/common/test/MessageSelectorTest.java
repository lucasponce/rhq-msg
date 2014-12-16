package org.rhq.msg.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.rhq.msg.common.ConnectionContextFactory;
import org.rhq.msg.common.Endpoint;
import org.rhq.msg.common.Endpoint.Type;
import org.rhq.msg.common.MessageProcessor;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.rhq.msg.common.consumer.ConsumerConnectionContext;
import org.rhq.msg.common.producer.ProducerConnectionContext;

/**
 * Tests message selectors and filtering of messagings.
 */
public class MessageSelectorTest {
    @Test
    public void testFilter() throws Exception {
        ConnectionContextFactory consumerFactory = null;
        ConnectionContextFactory producerFactory = null;

        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
        broker.start();

        try {
            String brokerURL = broker.getBrokerURL();
            Endpoint endpoint = new Endpoint(Type.QUEUE, "testq");
            HashMap<String, String> myTestHeaderBoo = new HashMap<String, String>();
            HashMap<String, String> myTestHeaderOther = new HashMap<String, String>();
            myTestHeaderBoo.put("MyTest", "boo");
            myTestHeaderOther.put("MyTest", "Other");

            // mimic server-side
            consumerFactory = new ConnectionContextFactory(brokerURL);
            ConsumerConnectionContext consumerContext = consumerFactory.createConsumerConnectionContext(endpoint, "MyTest = 'boo'");
            TestListener listener = new TestListener();
            MessageProcessor serverSideProcessor = new MessageProcessor();
            serverSideProcessor.listen(consumerContext, listener);

            // mimic client side
            producerFactory = new ConnectionContextFactory(brokerURL);
            ProducerConnectionContext producerContext = producerFactory.createProducerConnectionContext(endpoint);
            MessageProcessor clientSideProcessor = new MessageProcessor();

            // send one that won't match the selector
            SpecificMessage specificMessage = new SpecificMessage("nope", null, "no match");
            clientSideProcessor.send(producerContext, specificMessage, myTestHeaderOther);

            // wait for the message to flow - we won't get it because our selector doesn't match
            listener.waitForMessage(3); // 3 seconds is plenty of time to realize we aren't getting it
            assertTrue("Should not have received the message", listener.getMessage() == null);

            // send one that will match the selector
            specificMessage = new SpecificMessage("hello", null, "specific text");
            clientSideProcessor.send(producerContext, specificMessage, myTestHeaderBoo);

            // wait for the message to flow - we should get it now
            listener.waitForMessage(3);
            assertEquals("Should have received the message", listener.getMessage().getSpecific(), "specific text");

        } finally {
            // close everything
            producerFactory.close();
            consumerFactory.close();
            broker.stop();
        }
    }

    private class TestListener extends BasicMessageListener<SpecificMessage> {
        private CountDownLatch latch = new CountDownLatch(1);
        public SpecificMessage message;

        public void waitForMessage(long secs) throws InterruptedException {
            latch.await(secs, TimeUnit.SECONDS);
        }

        public SpecificMessage getMessage() {
            SpecificMessage result = this.message;
            this.message = null;
            return result;
        }
        @Override
        protected void onBasicMessage(SpecificMessage message) {
            this.message = message;
            latch.countDown();
        }
    }
}

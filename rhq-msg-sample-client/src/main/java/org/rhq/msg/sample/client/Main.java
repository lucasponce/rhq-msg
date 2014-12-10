package org.rhq.msg.sample.client;

import org.rhq.msg.common.ConnectionContextFactory;
import org.rhq.msg.common.Endpoint;
import org.rhq.msg.common.MessageProcessor;
import org.rhq.msg.common.SimpleBasicMessage;
import org.rhq.msg.common.consumer.BasicMessageListener;
import org.rhq.msg.common.consumer.ConsumerConnectionContext;
import org.rhq.msg.common.producer.ProducerConnectionContext;

/**
 * A simple sample client used to show the API needed to consume and produce messages.
 *
 * @author Heiko W. Rupp
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Consumer consumer = new Consumer();
        Producer producer = new Producer();

        consumer.consume();
        producer.produce();

        consumer.cleanUp();
        producer.cleanUp();
    }

    private static class Consumer {
        ConnectionContextFactory cachedFactory;

        public void consume() throws Exception {
            Endpoint endpoint = new Endpoint(Endpoint.Type.QUEUE, "myqueue");
            ConnectionContextFactory factory = new ConnectionContextFactory("vm://mybroker");
            ConsumerConnectionContext context = factory.createConsumerConnectionContext(endpoint);
            BasicMessageListener<SimpleBasicMessage> listener = new BasicMessageListener<SimpleBasicMessage>() {
                @Override
                protected void onBasicMessage(SimpleBasicMessage msg) {
                    System.out.println(msg.getMessage());
                }
            };
            MessageProcessor processor = new MessageProcessor();
            processor.listen(context, listener);

            // remember this so we can clean up after ourselves later
            this.cachedFactory = factory;
        }

        public void cleanUp() throws Exception {
            this.cachedFactory.close();
        }
    }

    private static class Producer {
        ConnectionContextFactory cachedFactory;

        public void produce() throws Exception {
            Endpoint endpoint = new Endpoint(Endpoint.Type.QUEUE, "myqueue");
            ConnectionContextFactory factory = new ConnectionContextFactory("vm://mybroker");
            ProducerConnectionContext pc = factory.createProducerConnectionContext(endpoint);
            SimpleBasicMessage msg = new SimpleBasicMessage("hello from " + Main.class);
            MessageProcessor processor = new MessageProcessor();
            processor.send(pc, msg);

            // remember this so we can clean up after ourselves later
            this.cachedFactory = factory;
        }

        public void cleanUp() throws Exception {
            this.cachedFactory.close();
        }
    }
}

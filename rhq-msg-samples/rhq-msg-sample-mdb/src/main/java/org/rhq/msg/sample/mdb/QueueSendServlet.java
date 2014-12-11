package org.rhq.msg.sample.mdb;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rhq.msg.common.ConnectionContextFactory;
import org.rhq.msg.common.Endpoint;
import org.rhq.msg.common.MessageProcessor;
import org.rhq.msg.common.SimpleBasicMessage;
import org.rhq.msg.common.producer.ProducerConnectionContext;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class QueueSendServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final static String CONN_FACTORY = "/ConnectionFactory";
    private final static String QUEUE_NAME = "QueueName";
    private final static String FULL_JNDI_QUEUE_NAME = "java:/queue/" + QUEUE_NAME;

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userMessage = request.getParameter("jmsMessageFNF");
        if (userMessage != null) {
            fireAndForget(request, response, userMessage);
        } else {
            userMessage = request.getParameter("jmsMessageRPC");
            if (userMessage != null) {
                rpc(request, response, userMessage);
            } else {
                throw new ServletException("Don't know what to send!");
            }
        }
    }

    protected void fireAndForget(HttpServletRequest request, HttpServletResponse response, String userMessage) {
        try {
            PrintWriter out = response.getWriter();
            InitialContext ctx = new InitialContext();
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup(CONN_FACTORY);
            QueueConnection qcon = qconFactory.createQueueConnection();
            QueueSession qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = (Queue) ctx.lookup(FULL_JNDI_QUEUE_NAME);
            QueueSender qsender = qsession.createSender(queue);
            TextMessage msg = qsession.createTextMessage();
            qcon.start();

            msg.setText(new SimpleBasicMessage(userMessage).toJSON());
            qsender.send(msg);
            qcon.close();

            out.println("<h1>Queue Sender Servlet</h1>");
            out.println("<p>Message Sent [" + userMessage + "]</p>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void rpc(HttpServletRequest request, HttpServletResponse response, String userMessage) {
        try {
            InitialContext ctx = new InitialContext();
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup(CONN_FACTORY);

            ConnectionContextFactory ccf = new ConnectionContextFactory(qconFactory);
            ProducerConnectionContext pcc = ccf.createProducerConnectionContext(new Endpoint(Endpoint.Type.QUEUE, QUEUE_NAME));

            SimpleBasicMessage msg = new SimpleBasicMessage(userMessage);
            ListenableFuture<SimpleBasicMessage> future = new MessageProcessor().sendRPC(pcc, msg, SimpleBasicMessage.class);
            Futures.addCallback(future, new SimpleFutureCallback());

            PrintWriter out = response.getWriter();
            out.println("<h1>Queue Sender Servlet</h1>");
            out.println("<p>Message Sent [" + userMessage + "]. Check server logs for response.</p>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SimpleFutureCallback implements FutureCallback<SimpleBasicMessage> {
        @Override
        public void onSuccess(SimpleBasicMessage result) {
            log("SUCCESS! Got response from MDB: " + result);
        }

        @Override
        public void onFailure(Throwable t) {
            log("FAILURE! Did not get response from MDB", t);
        }
    }
}

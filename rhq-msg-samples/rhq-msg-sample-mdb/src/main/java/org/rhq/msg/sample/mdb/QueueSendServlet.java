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

public class QueueSendServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final static String CONN_FACTORY = "/ConnectionFactory";
    private final static String QUEUE_NAME = "java:/queue/QueueName";

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            PrintWriter out = response.getWriter();
            InitialContext ctx = new InitialContext();
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup(CONN_FACTORY);
            QueueConnection qcon = qconFactory.createQueueConnection();
            QueueSession qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = (Queue) ctx.lookup(QUEUE_NAME);
            QueueSender qsender = qsession.createSender(queue);
            TextMessage msg = qsession.createTextMessage();
            qcon.start();

            String userMessage = request.getParameter("jmsMessage");
            msg.setText(userMessage);
            qsender.send(msg);
            qcon.close();

            out.println("<h1>Queue Sender Servlet</h1>");
            out.println("<p>Message Sent [" + userMessage + "]</p>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package org.rhq.msg.common;

import org.rhq.msg.common.Endpoint.Type;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;


public class EndpointTest {
    @Test
    public void endpointEquality() {
        Endpoint q1 = new Endpoint(Type.QUEUE, "foo");
        Endpoint q1Dup = new Endpoint(Type.QUEUE, "foo");
        Endpoint t1 = new Endpoint(Type.TOPIC, "foo");
        Endpoint t1Dup = new Endpoint(Type.TOPIC, "foo");
        Endpoint q2 = new Endpoint(Type.QUEUE, "bar");
        Endpoint t2 = new Endpoint(Type.TOPIC, "bar");
        assertTrue(q1.equals(q1Dup));
        assertTrue(t1.equals(t1Dup));

        assertFalse(q1.equals(q2));
        assertFalse(q1.equals(t1));
        assertFalse(q1.equals(t2));

        assertFalse(t1.equals(t2));
        assertFalse(t1.equals(q1));
        assertFalse(t1.equals(q2));
    }
}

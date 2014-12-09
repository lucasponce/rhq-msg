package org.rhq.msg.common;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BasicMessageTest {

    // tests a minimal basic record with no details
    @Test
    public void simpleConversion() {
        BasicMessage arec = new BasicMessage("my msg");
        String json = arec.toJSON();
        System.out.println(json);
        assertNotNull("missing JSON", json);

        BasicMessage arec2 = BasicMessage.fromJSON(json, BasicMessage.class);
        assertNotNull("JSON conversion failed", arec2);
        assertNotSame(arec, arec2);
        assertEquals(arec.getMessage(), arec2.getMessage());
        assertEquals(arec.getDetails(), arec2.getDetails());
    }

    // test a full basic record with several details
    @Test
    public void fullConversion() {
        Map<String,String> details = new HashMap<String,String>();
        details.put("key1", "val1");
        details.put("secondkey", "secondval");

        BasicMessage arec = new BasicMessage("my msg", details);
        arec.setMessageId(new MessageId("12345"));
        arec.setCorrelationId(new MessageId("67890"));
        String json = arec.toJSON();
        System.out.println(json);
        assertNotNull("missing JSON", json);

        BasicMessage arec2 = BasicMessage.fromJSON(json, BasicMessage.class);
        assertNotNull("JSON conversion failed", arec2);
        assertNotSame(arec, arec2);
        assertNull("Message ID should not be encoded in JSON", arec2.getMessageId());
        assertNull("Correlation ID should not be encoded in JSON", arec2.getCorrelationId());
        assertEquals("my msg", arec2.getMessage());
        assertEquals(2, arec2.getDetails().size());
        assertEquals("val1", arec2.getDetails().get("key1"));
        assertEquals("secondval", arec2.getDetails().get("secondkey"));
        assertEquals(arec.getMessage(), arec2.getMessage());
        assertEquals(arec.getDetails(), arec2.getDetails());
    }

    @Test
    public void testUnmodifiableDetails() {
        Map<String, String> details = new HashMap<String, String>();
        details.put("key1", "val1");

        BasicMessage msg = new BasicMessage("a", details);

        try {
            msg.getDetails().put("key1", "CHANGE!");
            assert false : "Should not have been able to change the details map";
        } catch (UnsupportedOperationException expected) {
            // to be expected
        }

        // make sure it didn't change and its still the same
        assertEquals("val1", msg.getDetails().get("key1"));
    }
}

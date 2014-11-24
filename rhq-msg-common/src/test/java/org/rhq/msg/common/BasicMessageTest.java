package org.rhq.msg.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class BasicMessageTest {

    // tests a minimal basic record with no details
    @Test
    public void simpleConversion() {
        BasicMessage arec = new BasicMessage("my msg");
        String json = arec.toJSON();
        System.out.println(json);
        Assert.assertNotNull(json, "missing JSON");

        BasicMessage arec2 = BasicMessage.fromJSON(json, BasicMessage.class);
        Assert.assertNotNull("JSON conversion failed", arec2);
        Assert.assertNotSame(arec, arec2);
        Assert.assertEquals(arec.getMessage(), arec2.getMessage());
        Assert.assertEquals(arec.getDetails(), arec2.getDetails());
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
        Assert.assertNotNull(json, "missing JSON");

        BasicMessage arec2 = BasicMessage.fromJSON(json, BasicMessage.class);
        Assert.assertNotNull("JSON conversion failed", arec2);
        Assert.assertNotSame(arec, arec2);
        Assert.assertNull("Message ID should not be encoded in JSON", arec2.getMessageId());
        Assert.assertNull("Correlation ID should not be encoded in JSON", arec2.getCorrelationId());
        Assert.assertEquals(arec2.getMessage(), "my msg");
        Assert.assertEquals(arec2.getDetails().size(), 2);
        Assert.assertEquals(arec2.getDetails().get("key1"), "val1");
        Assert.assertEquals(arec2.getDetails().get("secondkey"), "secondval");
        Assert.assertEquals(arec.getMessage(), arec2.getMessage());
        Assert.assertEquals(arec.getDetails(), arec2.getDetails());
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
        Assert.assertEquals(msg.getDetails().get("key1"), "val1");
    }
}

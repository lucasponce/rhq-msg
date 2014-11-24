package org.rhq.msg.common;

import org.junit.Assert;
import org.junit.Test;

public class MessageIdTest {

    @Test
    public void messageIdEquality() {
        MessageId one = new MessageId("msg1");
        MessageId oneDup = new MessageId("msg1");
        MessageId two = new MessageId("msg2");
        Assert.assertEquals(one, oneDup);
        Assert.assertFalse(one.equals(two));
    }
}

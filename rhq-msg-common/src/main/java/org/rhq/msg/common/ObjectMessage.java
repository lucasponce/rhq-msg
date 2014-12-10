package org.rhq.msg.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * A message that contains a complex object, which gets serialized into JSON.
 *
 * @author Heiko W. Rupp
 */
public class ObjectMessage extends BasicMessage {
    @Expose
    private String message; // the object in JSON form

    public ObjectMessage(Object object) {
        super();
        Gson gson = new GsonBuilder().create();
        String msg = gson.toJson(object);
        setMessage(msg);
    }

    /**
     * The simple JSON message string.
     *
     * @return message string as a JSON string
     */
    public String getMessage() {
        return message;
    }

    /**
     * Allow subclasses to set the JSON message
     *
     * @param msg
     *            the JSON message
     */
    protected void setMessage(String msg) {
        this.message = msg;
    }
}

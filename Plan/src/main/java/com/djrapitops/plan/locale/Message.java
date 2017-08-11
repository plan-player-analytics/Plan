package main.java.com.djrapitops.plan.locale;

import java.io.Serializable;

/**
 * Represents a Message that can be modified.
 *
 * @author Rsl1122
 * @since 3.6.2
 */
public class Message {

    private final String message;

    public Message(String message) {
        this.message = message;
    }

    public String parse(Serializable... p) {
        String returnValue = this.message;
        for (int i = 0; i < p.length; i++) {
            returnValue = returnValue.replace("REPLACE" + i, p[i].toString());
        }
        return returnValue;
    }

    @Override
    public String toString() {
        return message;
    }

    public String parse() {
        return toString();
    }

    public String[] toArray() {
        return message.split("\\\\");
    }

    public String[] toArray(Serializable... p) {
        return parse().split("\\\\");
    }
}

package main.java.com.djrapitops.plan.locale;

import java.io.Serializable;

/**
 * Represents a Message that can be modified.
 *
 * @author Rsl1122
 * @since 3.6.2
 */
public class Message {

    private final String content;

    public Message(String content) {
        this.content = content;
    }

    public String parse(Serializable... p) {
        String returnValue = this.content;
        for (int i = 0; i < p.length; i++) {
            returnValue = returnValue.replace("REPLACE" + i, p[i].toString());
        }
        return returnValue;
    }

    public String[] toArray() {
        return content.split("\\\\");
    }

    public String[] toArray(Serializable... p) {
        return parse(p).split("\\\\");
    }

    public String parse() {
        return toString();
    }

    @Override
    public String toString() {
        return content;
    }
}

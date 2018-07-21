package com.djrapitops.plan.system.settings.locale;

import com.djrapitops.plugin.utilities.Verify;
import org.apache.commons.text.StringSubstitutor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
        Verify.nullCheck(p);

        Map<String, Serializable> replaceMap = new HashMap<>();

        for (int i = 0; i < p.length; i++) {
            replaceMap.put(String.valueOf(i), p[i].toString());
        }

        StringSubstitutor sub = new StringSubstitutor(replaceMap);

        return sub.replace(content);
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

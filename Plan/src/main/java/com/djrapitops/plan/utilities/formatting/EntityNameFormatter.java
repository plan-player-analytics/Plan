package com.djrapitops.plan.utilities.formatting;

import com.djrapitops.plugin.utilities.Format;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class EntityNameFormatter implements Formatter<String> {

    @Override
    public String apply(String name) {
        return new Format(name).removeNumbers().removeSymbols().capitalize().toString();
    }
}
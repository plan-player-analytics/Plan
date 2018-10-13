package com.djrapitops.plan.utilities.formatting;

import com.djrapitops.plugin.utilities.Format;
import org.apache.commons.text.TextStringBuilder;

import java.util.Arrays;

/**
 * Formatter for Item names, that capitalizes each part and separates them with spaces instead of underscores.
 *
 * @author Rsl1122
 */
public class ItemNameFormatter implements Formatter<String> {

    @Override
    public String apply(String name) {
        String[] parts = name.split("_");
        TextStringBuilder builder = new TextStringBuilder();
        builder.appendWithSeparators(Arrays.stream(parts).map(part -> new Format(part).capitalize()).iterator(), " ");
        return builder.toString();
    }
}
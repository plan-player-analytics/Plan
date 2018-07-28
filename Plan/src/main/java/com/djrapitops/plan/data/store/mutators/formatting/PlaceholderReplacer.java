package com.djrapitops.plan.data.store.mutators.formatting;

import com.djrapitops.plan.data.store.PlaceholderKey;
import com.djrapitops.plan.data.store.containers.DataContainer;
import org.apache.commons.text.StringSubstitutor;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Formatter for replacing ${placeholder} values inside strings.
 *
 * @author Rsl1122
 */
public class PlaceholderReplacer extends HashMap<String, Serializable> implements Formatter<String> {

    public <T> void addPlaceholderFrom(DataContainer container, PlaceholderKey<T> key) {
        if (!container.supports(key)) {
            return;
        }
        put(key.getPlaceholder(), container.getSupplier(key).get().toString());
    }

    public void addAllPlaceholdersFrom(DataContainer container, PlaceholderKey... keys) {
        for (PlaceholderKey key : keys) {
            addPlaceholderFrom(container, key);
        }
    }

    public <T> void addPlaceholderFrom(DataContainer container, Formatter<T> formatter, PlaceholderKey<T> key) {
        if (!container.supports(key)) {
            return;
        }
        put(key.getPlaceholder(), formatter.apply(container.getSupplier(key).get()));
    }

    public <T> void addAllPlaceholdersFrom(DataContainer container, Formatter<T> formatter, PlaceholderKey<T>... keys) {
        for (PlaceholderKey<T> key : keys) {
            addPlaceholderFrom(container, formatter, key);
        }
    }

    @Override
    public String apply(String string) {
        StringSubstitutor sub = new StringSubstitutor(this);
        sub.setEnableSubstitutionInVariables(true);
        return sub.replace(string);
    }
}
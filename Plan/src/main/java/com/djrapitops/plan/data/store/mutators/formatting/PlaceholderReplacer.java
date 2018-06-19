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

    public void addPlaceholderFrom(DataContainer container, PlaceholderKey key) {
        if (!container.supports(key)) {
            return;
        }
        put(key.getPlaceholder(), container.get(key).get().toString());
    }

    public void addAllPlaceholdersFrom(DataContainer container, PlaceholderKey... keys) {
        for (PlaceholderKey key : keys) {
            addPlaceholderFrom(container, key);
        }
    }

    @Override
    public String apply(String string) {
        StringSubstitutor sub = new StringSubstitutor(this);
        sub.setEnableSubstitutionInVariables(true);
        return sub.replace(string);
    }
}
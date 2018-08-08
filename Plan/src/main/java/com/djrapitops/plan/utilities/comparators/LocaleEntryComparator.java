package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.system.locale.Message;
import com.djrapitops.plan.system.locale.lang.Lang;

import java.util.Comparator;
import java.util.Map;

/**
 * Compares Locale Map Entries and sorts them alphabetically according to the Enum Names.
 *
 * @author Rsl1122
 * @since 3.6.2
 */
public class LocaleEntryComparator implements Comparator<Map.Entry<Lang, Message>> {

    @Override
    public int compare(Map.Entry<Lang, Message> o1, Map.Entry<Lang, Message> o2) {
        return String.CASE_INSENSITIVE_ORDER.compare(o1.getKey().getIdentifier(), o2.getKey().getIdentifier());
    }
}

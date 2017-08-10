package main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.locale.Message;
import main.java.com.djrapitops.plan.locale.Msg;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Map;

/**
 * Compares Locale Map Entries and sorts them alphabetically according to the Enum Names.
 *
 * @since 3.6.2
 * @author Rsl1122
 */
public class LocaleEntryComparator implements Comparator<Map.Entry<Msg, Message>> {

    @Override
    public int compare(Map.Entry<Msg, Message> o1, Map.Entry<Msg, Message> o2) {
        return StringUtils.compare(o1.getKey().name(), o2.getKey().name());
    }
}

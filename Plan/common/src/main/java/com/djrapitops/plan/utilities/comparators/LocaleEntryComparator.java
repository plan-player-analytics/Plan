/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.settings.locale.Message;
import com.djrapitops.plan.settings.locale.lang.Lang;

import java.util.Comparator;
import java.util.Map;

/**
 * Compares Locale Map Entries and sorts them alphabetically according to the Enum Names.
 *
 * @author AuroraLS3
 */
public class LocaleEntryComparator implements Comparator<Map.Entry<Lang, Message>> {

    @Override
    public int compare(Map.Entry<Lang, Message> o1, Map.Entry<Lang, Message> o2) {
        return String.CASE_INSENSITIVE_ORDER.compare(o1.getKey().getIdentifier(), o2.getKey().getIdentifier());
    }
}

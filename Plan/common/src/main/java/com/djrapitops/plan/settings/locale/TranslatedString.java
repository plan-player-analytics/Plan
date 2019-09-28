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
package com.djrapitops.plan.settings.locale;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility for translating String.
 * <p>
 * Improves performance by avoiding a double for-each loop since this class can be considered final in the lambda
 * expression in {@link Locale#replaceLanguageInHtml(String)}.
 *
 * @author Rsl1122
 */
class TranslatedString {

    private String translating;

    TranslatedString(String translating) {
        this.translating = translating;
    }

    public void translate(String replace, String with) {
        translating = StringUtils.replace(translating, replace, with);
    }

    @Override
    public String toString() {
        return translating;
    }

    public int length() {
        return translating.length();
    }
}
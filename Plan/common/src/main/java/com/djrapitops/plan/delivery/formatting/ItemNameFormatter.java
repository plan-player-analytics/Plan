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
package com.djrapitops.plan.delivery.formatting;

import com.djrapitops.plugin.utilities.Format;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.util.Arrays;

/**
 * Formatter for Item names, that capitalizes each part and separates them with spaces instead of underscores.
 *
 * @author AuroraLS3
 */
public class ItemNameFormatter implements Formatter<String> {

    @Override
    public String apply(String name) {
        String[] parts = StringUtils.split(name, '_');
        TextStringBuilder builder = new TextStringBuilder();
        builder.appendWithSeparators(Arrays.stream(parts).map(part -> new Format(part).capitalize()).iterator(), " ");
        return builder.toString();
    }
}
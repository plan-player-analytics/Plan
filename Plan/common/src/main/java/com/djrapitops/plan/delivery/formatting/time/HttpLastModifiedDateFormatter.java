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
package com.djrapitops.plan.delivery.formatting.time;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Formats timestamps to the Last-Modified header time format.
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Last-Modified">Documentation for the header</a>
 *
 * @author AuroraLS3
 */
public class HttpLastModifiedDateFormatter extends DateFormatter {

    public HttpLastModifiedDateFormatter(PlanConfig config, Locale locale) {
        super(config, locale);
    }

    @Override
    public String apply(Long epochMs) {
        return format(epochMs, "E, dd MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.ENGLISH, TimeZone.getTimeZone(ZoneId.of("UTC")));
    }

}

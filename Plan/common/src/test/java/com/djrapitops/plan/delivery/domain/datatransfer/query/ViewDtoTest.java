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
package com.djrapitops.plan.delivery.domain.datatransfer.query;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewDtoTest {

    private static ViewDto view(String afterTime, String beforeTime) {
        return new Gson().fromJson("{\"afterDate\":\"01/11/2026\",\"afterTime\":\"" + afterTime +
                "\",\"beforeDate\":\"01/11/2026\",\"beforeTime\":\"" + beforeTime + "\",\"servers\":[]}", ViewDto.class);
    }

    @Test
    void beforeIncludesWholeMinute() throws ParseException {
        ViewDto view = view("12:30", "12:30");
        assertEquals(TimeUnit.MINUTES.toMillis(1L) - 1L, view.getBeforeEpochMs() - view.getAfterEpochMs());
    }

    @Test
    void viewTimesUseGregorianCalendarRegardlessOfDefaultLocale() throws ParseException {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("th-TH"));
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(view("12:30", "12:30").getAfterEpochMs());
            assertEquals(2026, calendar.get(Calendar.YEAR));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}

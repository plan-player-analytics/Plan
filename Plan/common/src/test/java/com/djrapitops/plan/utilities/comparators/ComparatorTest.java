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

import com.djrapitops.plan.delivery.rendering.json.graphs.line.Point;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.settings.locale.Message;
import com.djrapitops.plan.settings.locale.lang.HelpLang;
import com.djrapitops.plan.settings.locale.lang.Lang;
import com.djrapitops.plan.utilities.java.Lists;
import org.junit.jupiter.api.Test;
import utilities.RandomData;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComparatorTest {

    @Test
    void pointComparator() {
        List<Point> points = RandomData.randomPoints();

        List<Long> expected = points.stream().map(Point::getX).map(i -> (long) (double) i)
                .sorted(Long::compare).collect(Collectors.toList());

        points.sort(new PointComparator());

        List<Long> result = points.stream().map(Point::getX).map(i -> (long) (double) i).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    @Test
    void sessionDataComparator() {
        List<FinishedSession> sessions = RandomData.randomSessions();

        List<Long> expected = sessions.stream().map(FinishedSession::getStart)
                .sorted(Long::compare).collect(Collectors.toList());
        Collections.reverse(expected);

        sessions.sort(new SessionStartComparator());
        List<Long> result = Lists.map(sessions, FinishedSession::getStart);

        assertEquals(expected, result);
    }

    @Test
    void tpsComparator() {
        List<TPS> tpsList = RandomData.randomTPS();

        List<Long> expected = tpsList.stream().map(TPS::getDate)
                .sorted(Long::compare).collect(Collectors.toList());

        tpsList.sort(new TPSComparator());
        List<Long> result = tpsList.stream().map(TPS::getDate).collect(Collectors.toList());

        assertEquals(expected, result);
    }

    @Test
    void stringLengthComparator() {
        List<Integer> result = Stream.of(
                RandomData.randomString(10),
                RandomData.randomString(3),
                RandomData.randomString(4),
                RandomData.randomString(20),
                RandomData.randomString(7),
                RandomData.randomString(4),
                RandomData.randomString(86),
                RandomData.randomString(6)
        )
                .sorted(new StringLengthComparator())
                .map(String::length)
                .collect(Collectors.toList());

        List<Integer> expected = Arrays.asList(86, 20, 10, 7, 6, 4, 4, 3);
        assertEquals(expected, result);
    }

    @Test
    void localeEntryComparator() {
        Map<Lang, Message> messageMap = new HashMap<>();
        messageMap.put(HelpLang.SERVERS, new Message(RandomData.randomString(10)));
        messageMap.put(HelpLang.SERVER, new Message(RandomData.randomString(10)));
        messageMap.put(HelpLang.DB_RESTORE, new Message(RandomData.randomString(10)));

        List<Lang> result = messageMap.entrySet().stream()
                .sorted(new LocaleEntryComparator())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Lang> expected = Arrays.asList(
                HelpLang.DB_RESTORE,
                HelpLang.SERVER,
                HelpLang.SERVERS
        );
        assertEquals(expected, result);
    }

    @Test
    void geoInfoComparator() {
        List<GeoInfo> geoInfos = RandomData.randomGeoInfo();

        List<Long> expected = geoInfos.stream().map(GeoInfo::getDate)
                .sorted(Long::compare).collect(Collectors.toList());
        Collections.reverse(expected);

        geoInfos.sort(new GeoInfoComparator());
        List<Long> result = geoInfos.stream().map(GeoInfo::getDate).collect(Collectors.toList());
        assertEquals(expected, result);
    }
}

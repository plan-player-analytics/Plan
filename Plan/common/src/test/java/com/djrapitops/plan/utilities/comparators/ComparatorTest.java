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
import com.djrapitops.plan.utilities.java.Lists;
import org.junit.jupiter.api.Test;
import utilities.RandomData;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComparatorTest {

    @Test
    void pointComparator() {
        List<Point> points = RandomData.randomPoints();

        List<Long> expected = points.stream().map(Point::getX).map(i -> (long) (double) i)
                .sorted(Long::compare).toList();

        points.sort(new PointComparator());

        List<Long> result = points.stream().map(Point::getX).map(i -> (long) (double) i).toList();
        assertEquals(expected, result);
    }

    @Test
    void sessionDataComparator() {
        List<FinishedSession> sessions = RandomData.randomSessions();

        List<Long> expected = sessions.stream().map(FinishedSession::start)
                .sorted(Long::compare)
                .sorted(Comparator.reverseOrder())
                .toList();

        sessions.sort(new SessionStartComparator());
        List<Long> result = Lists.map(sessions, FinishedSession::start);

        assertEquals(expected, result);
    }

    @Test
    void tpsComparator() {
        List<TPS> tpsList = RandomData.randomTPS();

        List<Long> expected = tpsList.stream().map(TPS::date)
                .sorted(Long::compare).toList();

        tpsList.sort(new TPSComparator());
        List<Long> result = tpsList.stream().map(TPS::date).toList();

        assertEquals(expected, result);
    }

    @Test
    void geoInfoComparator() {
        List<GeoInfo> geoInfos = RandomData.randomGeoInfo();

        List<Long> expected = geoInfos.stream().map(GeoInfo::date)
                .sorted(Long::compare)
                .sorted(Comparator.reverseOrder())
                .toList();

        geoInfos.sort(new GeoInfoComparator());
        List<Long> result = geoInfos.stream().map(GeoInfo::date).toList();
        assertEquals(expected, result);
    }
}

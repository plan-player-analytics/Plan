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
package com.djrapitops.plan.delivery.web.resolver.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link URIPath} behavior.
 *
 * @author AuroraLS3
 */
class URIPathTest {

    @Test
    void fullTargetForRoot() {
        URIPath target = new URIPath("/");
        String expected = "/";
        String result = target.asString();
        assertEquals(expected, result);
    }

    @Test
    void fullTarget() {
        URIPath target = new URIPath("/example/target");
        String expected = "/example/target";
        String result = target.asString();
        assertEquals(expected, result);
    }

    @ParameterizedTest(name = "URIPath {0} gets part {1}: {2}")
    @CsvSource({
            "/", "0", " ",
            "/example/target", "0", "example",
            "/example/target", "1", "target",
            "/example/target/", "2", " ",
    })
    void partGettingWorksProperly(String targetURI, int partNumber, String expectedPart) {
        URIPath target = new URIPath(targetURI);
        Optional<String> expected = Optional.ofNullable(expectedPart.trim());
        Optional<String> result = target.getPart(partNumber);
        assertEquals(expected, result);
    }

    @Test
    void noPart() {
        URIPath target = new URIPath("/example/target");
        Optional<String> expected = Optional.empty();
        Optional<String> result = target.getPart(2);
        assertEquals(expected, result);
    }

    @Test
    void omitRoot() {
        URIPath target = new URIPath("/").omitFirst();
        String expected = "";
        String result = target.asString();
        assertEquals(expected, result);
    }

    @Test
    void omitRootPart() {
        URIPath target = new URIPath("/").omitFirst();
        Optional<String> expected = Optional.empty();
        Optional<String> result = target.getPart(0);
        assertEquals(expected, result);
    }

    @Test
    void omitFirstPart() {
        URIPath target = new URIPath("/example/target").omitFirst();
        Optional<String> expected = Optional.of("target");
        Optional<String> result = target.getPart(0);
        assertEquals(expected, result);
    }

    @Test
    void omitFirstFullTarget() {
        URIPath target = new URIPath("/example/target").omitFirst();
        String expected = "/target";
        String result = target.asString();
        assertEquals(expected, result);
    }

    @ParameterizedTest(name = "URIPath {0} removes parts before part {1}: {2}")
    @CsvSource({
            "/example/target", "1", "/target",
            "/example/target/", "2", "/",
            "/example/target", "2", " ",
            "/example/target", "0", "/example/target",
            "/example/target/", "1", "/target/",
            " ", "1", " "
    })
    void partsAreRemoved(String test, int removeThisMany, String expected) {
        String result = URIPath.removePartsBefore(test.trim(), removeThisMany);
        assertEquals(expected.trim(), result);
    }

}
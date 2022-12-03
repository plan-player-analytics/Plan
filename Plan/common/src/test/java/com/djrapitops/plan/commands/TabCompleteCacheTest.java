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
package com.djrapitops.plan.commands;

import com.djrapitops.plan.gathering.ServerSensor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.RandomData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TabCompleteCacheTest {

    @Mock
    ServerSensor<?> serverSensor;

    @InjectMocks
    TabCompleteCache underTest;

    @Test
    @DisplayName("Tab completion limit check: {limit} + 1 results returns empty")
    void tooManyMatchesGetsEmptyTabCompletionOneOver() {
        Collection<String> searchList = RandomData.pickMultiple(101, () -> RandomData.randomString(100));
        List<String> matches = underTest.findMatches(searchList, null);
        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("Tab completion limit check: {limit} results returns empty")
    void tooManyMatchesGetsEmptyTabCompletionAtLimit() {
        Collection<String> searchList = RandomData.pickMultiple(100, () -> RandomData.randomString(100));
        List<String> matches = underTest.findMatches(searchList, null);
        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("Tab completion limit check: {limit} - 1 results returns results")
    void tooManyMatchesGetsResultsTabCompletionOneUnder() {
        Collection<String> searchList = RandomData.pickMultiple(99, () -> RandomData.randomString(100));
        List<String> matches = underTest.findMatches(searchList, null);
        assertEquals(99, matches.size());
    }

    @Test
    @DisplayName("Tab completion empty search string returns results")
    void emptyStringReturnsAllResults() {
        Collection<String> searchList = RandomData.pickMultiple(99, () -> RandomData.randomString(100));
        List<String> matches = underTest.findMatches(searchList, "");
        assertEquals(99, matches.size());
    }

    @Test
    @DisplayName("Tab completion matches starts")
    void searchGetsAllStarts() {
        Collection<String> searchList = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            searchList.add("start-" + i);
            searchList.add("nope-" + i);
        }

        List<String> matches = underTest.findMatches(searchList, "start-");
        assertEquals(25, matches.size());
    }

    @Test
    @DisplayName("Tab completion coverage")
    void tabCompletionCommonMethodCoverage() {
        assertTrue(underTest.getMatchingServerIdentifiers("").isEmpty());
        assertTrue(underTest.getMatchingPlayerIdentifiers("").isEmpty());
        assertTrue(underTest.getMatchingUserIdentifiers("").isEmpty());
        assertTrue(underTest.getMatchingBackupFilenames("").isEmpty());
    }

    @Test
    @DisplayName("Tab completion online players are searched")
    void tabCompletionOnlinePlayersAreListed() {
        List<String> playerNames = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            playerNames.add("start-" + i);
        }
        when(serverSensor.getOnlinePlayerNames()).thenReturn(playerNames);

        List<String> matches = underTest.getMatchingPlayerIdentifiers("start-");
        assertEquals(25, matches.size());
    }

}
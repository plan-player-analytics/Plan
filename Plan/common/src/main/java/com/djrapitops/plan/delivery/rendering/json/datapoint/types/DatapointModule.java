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
package com.djrapitops.plan.delivery.rendering.json.datapoint.types;

import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

/**
 * Dagger module for registering {@code Set<Datapoint<?>> for use in constructors so that individual ones don't need to be injected.}
 *
 * @author AuroraLS3
 */
@Module
@SuppressWarnings("java:S1452")
public interface DatapointModule {

    @Binds
    @IntoSet
    Datapoint<?> bindPlaytime(Playtime playtime);

    @Binds
    @IntoSet
    Datapoint<?> bindAfkTime(AfkTime afkTime);

    @Binds
    @IntoSet
    Datapoint<?> bindAfkTimePercentage(AfkTimePercentage afkTimePercentage);

    @Binds
    @IntoSet
    Datapoint<?> bindServerOccupied(ServerOccupied serverOccupied);

    @Binds
    @IntoSet
    Datapoint<?> bindWorldPie(WorldPie worldPie);

    @Binds
    @IntoSet
    Datapoint<?> bindMostActiveGameMode(MostActiveGameMode mostActiveGameMode);

    @Binds
    @IntoSet
    Datapoint<?> bindMostActiveWorld(MostActiveWorld mostActiveWorld);

    @Binds
    @IntoSet
    Datapoint<?> bindServerPie(ServerPie serverPie);

    @Binds
    @IntoSet
    Datapoint<?> bindUniquePlayers(UniquePlayersCount uniquePlayersCount);

    @Binds
    @IntoSet
    Datapoint<?> bindNewPlayers(NewPlayers newPlayers);

    @Binds
    @IntoSet
    Datapoint<?> bindRegularPlayers(RegularPlayers regularPlayers);

    @Binds
    @IntoSet
    Datapoint<?> bindSessionCount(SessionCount sessionCount);

    @Binds
    @IntoSet
    Datapoint<?> bindSessionLengthAverage(SessionLengthAverage sessionLengthAverage);

    @Binds
    @IntoSet
    Datapoint<?> bindDeaths(Deaths deaths);

    @Binds
    @IntoSet
    Datapoint<?> bindPlaytimePerPlayerAverage(PlaytimePerPlayerAverage playtimePerPlayerAverage);

    @Binds
    @IntoSet
    Datapoint<?> bindPlayerOnlinePeak(PlayersOnlinePeak playersOnlinePeak);

    @Binds
    @IntoSet
    Datapoint<?> bindPlayersOnline(PlayersOnline playersOnline);

    @Binds
    @IntoSet
    Datapoint<?> bindUptimeCurrent(UptimeCurrent uptimeCurrent);

    @Binds
    @IntoSet
    Datapoint<?> bindMobKills(MobKills mobKills);

    @Binds
    @IntoSet
    Datapoint<?> bindPlayerKills(PlayerKills playerKills);

    @Binds
    @IntoSet
    Datapoint<?> bindTPSAverage(TPSAverage tpsAverage);

    @Binds
    @IntoSet
    Datapoint<?> bindCPUAverage(CPUAverage cpuAverage);

    @Binds
    @IntoSet
    Datapoint<?> bindMSPTAverage(MSPTAverage msptaverage);

    @Binds
    @IntoSet
    Datapoint<?> bindPlayersOnlineAverage(PlayersOnlineAverage playersOnlineAverage);

    @Binds
    @IntoSet
    Datapoint<?> bindRAMAverage(RAMAverage ramAverage);

    @Binds
    @IntoSet
    Datapoint<?> bindEntitiesAverage(EntitiesAverage entitiesAverage);

    @Binds
    @IntoSet
    Datapoint<?> bindChunksAverage(ChunksAverage chunksAverage);

    @Binds
    @IntoSet
    Datapoint<?> bindUniquePlayersAveragePerDay(UniquePlayersPerDayAverage uniquePlayersPerDayAverage);
}

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
package com.djrapitops.plan.gathering.importing.data;

import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Fuzzlemann
 */
public class ServerImportData {

    private List<TPS> tpsData;

    private ServerImportData(List<TPS> tpsData) {
        this.tpsData = tpsData;
    }

    public static ServerImportDataBuilder builder() {
        return new ServerImportDataBuilder();
    }

    public List<TPS> getTpsData() {
        return tpsData;
    }

    public void setTpsData(List<TPS> tpsData) {
        this.tpsData = tpsData;
    }

    public static final class ServerImportDataBuilder {
        private final List<TPS> tpsData = new ArrayList<>();

        private ServerImportDataBuilder() {
            /* Private Constructor */
        }

        public ServerImportDataBuilder tpsData(long date, double ticksPerSecond, int players, double cpuUsage, long usedMemory, int entityCount, int chunksLoaded) {
            TPS tps = TPSBuilder.get()
                    .date(date)
                    .tps(ticksPerSecond)
                    .playersOnline(players)
                    .usedCPU(cpuUsage)
                    .usedMemory(usedMemory)
                    .entities(entityCount)
                    .chunksLoaded(chunksLoaded)
                    .toTPS();
            this.tpsData.add(tps);
            return this;
        }

        public ServerImportDataBuilder tpsData(TPS... tpsData) {
            this.tpsData.addAll(Arrays.asList(tpsData));
            return this;
        }

        public ServerImportDataBuilder tpsData(Collection<TPS> tpsData) {
            this.tpsData.addAll(tpsData);
            return this;
        }

        public ServerImportData build() {
            return new ServerImportData(tpsData);
        }
    }
}

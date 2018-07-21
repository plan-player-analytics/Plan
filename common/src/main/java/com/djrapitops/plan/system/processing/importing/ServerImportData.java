/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.importing;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;

import java.util.*;

/**
 * @author Fuzzlemann
 * @since 4.0.0
 */
public class ServerImportData {

    private Map<String, Integer> commandUsages;
    private List<TPS> tpsData;

    private ServerImportData(Map<String, Integer> commandUsages, List<TPS> tpsData) {
        this.commandUsages = commandUsages;
        this.tpsData = tpsData;
    }

    public static ServerImportDataBuilder builder() {
        return new ServerImportDataBuilder();
    }

    public Map<String, Integer> getCommandUsages() {
        return commandUsages;
    }

    public void setCommandUsages(Map<String, Integer> commandUsages) {
        this.commandUsages = commandUsages;
    }

    public List<TPS> getTpsData() {
        return tpsData;
    }

    public void setTpsData(List<TPS> tpsData) {
        this.tpsData = tpsData;
    }

    public static final class ServerImportDataBuilder {
        private final Map<String, Integer> commandUsages = new HashMap<>();
        private final List<TPS> tpsData = new ArrayList<>();

        private ServerImportDataBuilder() {
            /* Private Constructor */
        }

        public ServerImportDataBuilder commandUsage(String command, Integer usages) {
            this.commandUsages.put(command, usages);
            return this;
        }

        public ServerImportDataBuilder commandUsages(Map<String, Integer> commandUsages) {
            this.commandUsages.putAll(commandUsages);
            return this;
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
            return new ServerImportData(commandUsages, tpsData);
        }
    }
}

/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Processes 60s average of a TPS list.
 *
 * @author Rsl1122
 */
public class TPSInsertProcessor extends Processor<List<TPS>> {

    public TPSInsertProcessor(List<TPS> object) {
        super(object);
    }

    @Override
    public void process() {
        List<TPS> history = object;
        final long lastDate = history.get(history.size() - 1).getDate();
        final double averageTPS = MathUtils.round(MathUtils.averageDouble(history.stream().map(TPS::getTicksPerSecond)));
        final int averagePlayersOnline = (int) MathUtils.averageInt(history.stream().map(TPS::getPlayers));
        final double averageCPUUsage = MathUtils.round(MathUtils.averageDouble(history.stream().map(TPS::getCPUUsage)));
        final long averageUsedMemory = MathUtils.averageLong(history.stream().map(TPS::getUsedMemory));
        final int averageEntityCount = (int) MathUtils.averageInt(history.stream().map(TPS::getEntityCount));
        final int averageChunksLoaded = (int) MathUtils.averageInt(history.stream().map(TPS::getChunksLoaded));

        TPS tps = new TPS(lastDate, averageTPS, averagePlayersOnline, averageCPUUsage, averageUsedMemory, averageEntityCount, averageChunksLoaded);
        try {
            MiscUtils.getIPlan().getDB().getTpsTable().insertTPS(tps);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}
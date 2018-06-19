/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.file.export;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.UUID;

/**
 * Task that exports a single Analysis page if it is in ResponseCache.
 *
 * @author Rsl1122
 */
public class AnalysisExport extends SpecificExport {

    private final UUID serverUUID;
    private final String serverName;

    public AnalysisExport(UUID serverUUID, String serverName) {
        super("ServerPageExport:" + serverName);
        this.serverUUID = serverUUID;
        this.serverName = serverName;
    }

    @Override
    public void run() {
        try {
            if (Check.isBukkitAvailable() && ConnectionSystem.getInstance().isServerAvailable()) {
                return;
            }

            exportAvailableServerPage(serverUUID, serverName);
        } catch (IOException e) {
            Log.toLog(this.getClass(), e);
        } finally {
            try {
                this.cancel();
            } catch (ConcurrentModificationException | IllegalArgumentException ignore) {
            }
        }
    }
}

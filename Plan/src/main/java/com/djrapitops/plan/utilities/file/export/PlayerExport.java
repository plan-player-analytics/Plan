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
 * Task for exporting a single player page.
 *
 * @author Rsl1122
 */
public class PlayerExport extends SpecificExport {

    private final UUID uuid;
    private final String name;

    public PlayerExport(UUID uuid, String name) {
        super("PlayerPageExport:" + name);
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            if (Check.isBukkitAvailable() && ConnectionSystem.getInstance().isServerAvailable()) {
                return;
            }

            exportAvailablePlayerPage(uuid, name);
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

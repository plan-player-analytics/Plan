package com.djrapitops.plan.system.importing;

import com.djrapitops.plan.system.importing.importers.OfflinePlayerImporter;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ImportSystem implementation for Bukkit.
 *
 * @author Rsl1122
 */
@Singleton
public class BukkitImportSystem extends ImportSystem {

    private final OfflinePlayerImporter offlinePlayerImporter;

    @Inject
    public BukkitImportSystem(
            OfflinePlayerImporter offlinePlayerImporter
    ) {
        this.offlinePlayerImporter = offlinePlayerImporter;
    }

    @Override
    void registerImporters() {
        registerImporter(offlinePlayerImporter);
    }
}
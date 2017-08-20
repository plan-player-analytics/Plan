package main.java.com.djrapitops.plan.data.handling.importing;

import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;

import java.util.UUID;

/**
 * Imports all players who have not joined since Plan was installed.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
@Deprecated
public class OfflinePlayerImporter extends Importer {

    public OfflinePlayerImporter() {
        super.setInfo("Import all players who have not joined since Plan was installed.");
    }

    @Override
    public HandlingInfo importData(UUID uuid, String... args) {
        return null;
    }
}

package main.java.com.djrapitops.plan.data.handling.importing;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;

/**
 * Imports all players who have not joined since Plan was installed.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class OfflinePlayerImporter extends Importer {

    public OfflinePlayerImporter() {
        super.setInfo("Import all players who have not joined since Plan was installed.");
    }
    
    @Override
    public HandlingInfo importData(UUID uuid, String... args) {
        return null;
    }
}

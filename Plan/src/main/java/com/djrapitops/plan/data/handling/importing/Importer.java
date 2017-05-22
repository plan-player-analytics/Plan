package main.java.com.djrapitops.plan.data.handling.importing;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.database.Database;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Rsl1122
 */
public abstract class Importer {

    public Importer() {

    }

    public boolean importData(Collection<UUID> uuids) {
        Plan plan = Plan.getInstance();
        DataCacheHandler handler = plan.getHandler();
        Database db = plan.getDB();
        Set<UUID> saved;
        try {
            saved = db.getSavedUUIDs();
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
        List<UUID> unSaved = new ArrayList<>(uuids);
        unSaved.removeAll(saved);
        for (UUID uuid : unSaved) {
            OfflinePlayer player = getOfflinePlayer(uuid);
            handler.newPlayer(player);
        }
        for (UUID uuid : uuids) {
            handler.addToPool(importData(uuid));
        }
        return true;
    }

    public abstract HandlingInfo importData(UUID uuid);
}

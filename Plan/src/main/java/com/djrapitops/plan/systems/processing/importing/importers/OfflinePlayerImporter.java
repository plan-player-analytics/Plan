/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.importing.importers;

import main.java.com.djrapitops.plan.systems.processing.importing.ServerImportData;
import main.java.com.djrapitops.plan.systems.processing.importing.UserImportData;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * @author Fuzzlemann
 * @since 4.0.0
 */
public class OfflinePlayerImporter extends Importer {

    @Override
    public List<String> getNames() {
        return Arrays.asList("offline", "offlineplayer");
    }

    @Override
    public ServerImportData getServerImportData() {
        return null;
    }

    @Override
    public List<UserImportData> getUserImportData() {
        List<UserImportData> dataList = new Vector<>();

        Arrays.stream(Bukkit.getOfflinePlayers()).parallel().forEach(player -> {
            UserImportData.UserImportDataBuilder builder = UserImportData.builder();
            builder.name(player.getName())
                    .uuid(player.getUniqueId())
                    .op(player.isOp())
                    .registered(player.getFirstPlayed())
                    .banned(player.isBanned());
            dataList.add(builder.build());
        });

        return dataList;
    }
}

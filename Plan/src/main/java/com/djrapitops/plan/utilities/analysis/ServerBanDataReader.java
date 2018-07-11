package com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plan.data.plugin.BanData;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.*;

public class ServerBanDataReader {

    public Set<UUID> readBanDataForContainer(DataContainer container) {
        return readBanData(
                container.getValue(AnalysisKeys.PLAYERS_MUTATOR)
                        .map(PlayersMutator::uuids)
                        .orElse(new ArrayList<>())
        );
    }

    public Set<UUID> readBanData(Collection<UUID> uuids) {
        List<BanData> banPlugins = HookHandler.getInstance().getBanDataSources();

        Set<UUID> banned = new HashSet<>();
        for (BanData banPlugin : banPlugins) {
            try {
                banned.addAll(banPlugin.filterBanned(uuids));
            } catch (Exception | NoSuchMethodError | NoClassDefFoundError | NoSuchFieldError e) {
                Log.toLog("PluginData caused exception: " + banPlugin.getClass().getName(), e);
            }
        }
        return banned;
    }

}

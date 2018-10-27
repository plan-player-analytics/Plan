/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.vault;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.pluginbridge.plan.FakeOfflinePlayer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for Vault Economy.
 *
 * @author Rsl1122
 */
class VaultEcoData extends PluginData {

    private final Economy econ;

    private final DataCache dataCache;
    private final Formatter<Double> decimalFormatter;

    VaultEcoData(
            Economy econ,
            DataCache dataCache,
            Formatter<Double> decimalFormatter
    ) {
        super(ContainerSize.THIRD, "Economy (" + econ.getName() + ")");
        this.dataCache = dataCache;
        this.decimalFormatter = decimalFormatter;
        setPluginIcon(Icon.called("money-bill-wave").of(Color.GREEN).build());
        this.econ = econ;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        String name = dataCache.getName(uuid);
        if (name == null) {
            return inspectContainer;
        }
        OfflinePlayer p = new FakeOfflinePlayer(uuid, name);
        inspectContainer.addValue(getWithIcon("Balance", Icon.called("money-bill-wave").of(Color.GREEN)), econ.format(econ.getBalance(p)));

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        List<FakeOfflinePlayer> offlinePlayers = Optional.ofNullable(analysisData)
                .flatMap(c -> c.getValue(AnalysisKeys.PLAYERS_MUTATOR))
                .map(mutator -> mutator.all().stream().map(FakeOfflinePlayer::new)
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());

        Map<UUID, String> balances = new HashMap<>();
        double totalBalance = 0.0;
        for (FakeOfflinePlayer p : offlinePlayers) {
            double bal = econ.getBalance(p);
            totalBalance += bal;
            balances.put(p.getUniqueId(), econ.format(bal));
        }
        analysisContainer.addValue(getWithIcon("Server Balance", Icon.called("money-bill-wave").of(Color.GREEN)), decimalFormatter.apply(totalBalance));
        analysisContainer.addPlayerTableValues(getWithIcon("Balance", Icon.called("money-bill-wave")), balances);

        return analysisContainer;
    }
}
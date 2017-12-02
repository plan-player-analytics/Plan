/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.vault;

import com.djrapitops.plugin.utilities.Verify;
import com.djrapitops.pluginbridge.plan.FakeOfflinePlayer;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.PlayerProfile;
import main.java.com.djrapitops.plan.data.ServerProfile;
import main.java.com.djrapitops.plan.data.element.AnalysisContainer;
import main.java.com.djrapitops.plan.data.element.InspectContainer;
import main.java.com.djrapitops.plan.data.plugin.ContainerSize;
import main.java.com.djrapitops.plan.data.plugin.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.analysis.Analysis;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for Vault Economy.
 *
 * @author Rsl1122
 */
public class VaultEcoData extends PluginData {

    private final Economy econ;

    public VaultEcoData(Economy econ) {
        super(ContainerSize.THIRD, "Economy (" + econ.getName() + ")");
        super.setIconColor("green");
        super.setPluginIcon("money");
        this.econ = econ;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) throws Exception {
        String name = Plan.getInstance().getDataCache().getName(uuid);
        if (name == null) {
            return inspectContainer;
        }
        OfflinePlayer p = new FakeOfflinePlayer(uuid, name);
        inspectContainer.addValue(getWithIcon("Balance", "money", "green"), econ.format(econ.getBalance(p)));

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) throws Exception {
        ServerProfile serverProfile = Analysis.getServerProfile();

        List<PlayerProfile> profiles = collection.stream()
                .map(serverProfile::getPlayer)
                .filter(Verify::notNull)
                .collect(Collectors.toList());

        Map<UUID, String> balances = new HashMap<>();
        double totalBalance = 0.0;
        for (PlayerProfile profile : profiles) {
            double bal = econ.getBalance(profile);
            totalBalance += bal;
            balances.put(profile.getUuid(), econ.format(bal));
        }
        analysisContainer.addValue(getWithIcon("Server Balance", "money", "green"), FormatUtils.cutDecimals(totalBalance));
        analysisContainer.addPlayerTableValues(getWithIcon("Balance", "money"), balances);

        return analysisContainer;
    }
}
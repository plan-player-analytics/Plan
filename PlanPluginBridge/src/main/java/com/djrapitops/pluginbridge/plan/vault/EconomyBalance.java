package com.djrapitops.pluginbridge.plan.vault;

import com.djrapitops.pluginbridge.plan.FakeOfflinePlayer;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

import java.io.Serializable;
import java.util.UUID;

import static org.bukkit.Bukkit.getOfflinePlayer;

/**
 * PluginData class for Vault-plugin.
 * <p>
 * Registered to the plugin by VaultHook
 * <p>
 * Gives Total Balance Double as value.
 *
 * @author Rsl1122
 * @see VaultHook
 * @since 3.1.0
 */
public class EconomyBalance extends PluginData {

    private final Economy econ;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param econ Economy given by Vault.
     */
    public EconomyBalance(Economy econ) {
        super("Vault", "balance", AnalysisType.DOUBLE_TOTAL, AnalysisType.DOUBLE_AVG);
        this.econ = econ;
        super.setAnalysisOnly(false);
        super.setIcon("money");
        super.setPrefix("Balance: ");
        super.setSuffix(" " + FormatUtils.removeNumbers(econ.format(0)));
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        OfflinePlayer p = new FakeOfflinePlayer(uuid, getNameOf(uuid));
        if (this.econ.hasAccount(p)) {
            return parseContainer(modifierPrefix, Double.toString(this.econ.getBalance(p)));
        }
        return parseContainer(modifierPrefix, "0");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (this.econ.hasAccount(p)) {
            return this.econ.getBalance(p);
        }
        return -1;
    }

}

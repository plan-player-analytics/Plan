package com.djrapitops.pluginbridge.plan.vault;

import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import net.milkbowl.vault.economy.Economy;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;

/**
 * PluginData class for Vault-plugin.
 *
 * Registered to the plugin by VaultHook
 *
 * Gives Total Balance Double as value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see VaultHook
 */
public class EconomyBalance extends PluginData {

    private Economy econ;

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
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (this.econ.hasAccount(p)) {
            return parseContainer(modifierPrefix, this.econ.getBalance(p) + "");
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

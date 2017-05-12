package main.java.com.djrapitops.plan.data.additional.vault;

import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import net.milkbowl.vault.economy.Economy;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Rsl1122
 */
public class EconomyBalance extends PluginData {

    private Economy econ;

    public EconomyBalance(Economy econ) {
        super("Vault", "balance", AnalysisType.DOUBLE_TOTAL, AnalysisType.DOUBLE_AVG);
        this.econ = econ;
        super.setAnalysisOnly(false);
        super.setIcon("money");
        super.setPrefix("Balance: ");
        super.setSuffix(" "+FormatUtils.removeNumbers(econ.format(0)));
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (p.hasPlayedBefore()) {
            parseContainer(modifierPrefix, this.econ.getBalance(p)+"");
        }
        return "";
    }

    @Override
    public Serializable getValue(UUID uuid) {
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (p.hasPlayedBefore()) {
            return this.econ.getBalance(p);
        }
        return -1;
    }

}

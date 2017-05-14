package main.java.com.djrapitops.plan.data.additional.factions;

import com.massivecraft.factions.entity.MPlayer;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;

/**
 * PluginData class for Factions-plugin.
 *
 * Registered to the plugin by FactionsHook
 *
 * Gives Power Integer as value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see FactionsHook
 */
public class FactionsPower extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public FactionsPower() {
        super("Factions", "power", AnalysisType.DOUBLE_AVG);
        super.setAnalysisOnly(false);
        super.setIcon("bolt");
        super.setPrefix("Power: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        MPlayer mPlayer = MPlayer.get(uuid);
        return parseContainer(modifierPrefix, FormatUtils.cutDecimals(mPlayer.getPower()));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        MPlayer mPlayer = MPlayer.get(uuid);
        return mPlayer.getPower();
    }

}

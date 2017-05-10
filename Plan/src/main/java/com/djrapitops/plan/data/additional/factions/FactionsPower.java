package main.java.com.djrapitops.plan.data.additional.factions;

import com.massivecraft.factions.entity.MPlayer;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;

/**
 *
 * @author Rsl1122
 */
public class FactionsPower extends PluginData {

    public FactionsPower() {
        super("Factions", "power", AnalysisType.DOUBLE_AVG);
        super.setAnalysisOnly(false);
        super.setPrefix("Power: ");
        super.setSuffix(" / %maxpower%");
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

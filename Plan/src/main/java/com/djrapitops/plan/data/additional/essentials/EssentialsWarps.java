package main.java.com.djrapitops.plan.data.additional.essentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Warps;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;

/**
 *
 * @author Rsl1122
 */
public class EssentialsWarps extends PluginData {

    private Essentials essentials;

    public EssentialsWarps(Essentials essentials) {
        super("Essentials", "warps", AnalysisType.HTML);
        this.essentials = essentials;
        super.setIcon("map-marker");
        super.setPrefix("Warps: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifier, UUID uuid) {
        Warps warps = essentials.getWarps();
        if (!warps.isEmpty()) {
            return parseContainer("", warps.getList().toString());
        }
        return parseContainer("", "No Warps.");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        Warps warps = essentials.getWarps();
        if (!warps.isEmpty()) {
            return warps.getList().toString();
        }
        return "No Warps.";
    }

}

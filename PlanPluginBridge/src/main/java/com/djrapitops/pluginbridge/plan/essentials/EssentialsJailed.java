package com.djrapitops.pluginbridge.plan.essentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;

/**
 * PluginData class for Essentials-plugin.
 *
 * Registered to the plugin by EssentialsHook
 *
 * Gives Jailed boolean value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see EssentialsHook
 */
public class EssentialsJailed extends PluginData {

    private final Essentials essentials;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param essentials Instance of Essentials plugin.
     */
    public EssentialsJailed(Essentials essentials) {
        super("Essentials", "jailed", AnalysisType.BOOLEAN_PERCENTAGE, AnalysisType.BOOLEAN_TOTAL);
        this.essentials = essentials;
        super.setIcon("ban");
        super.setAnalysisOnly(false);
        super.setPrefix("Jailed: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifier, UUID uuid) {
        User user = essentials.getUser(uuid);
        if (user != null) {
            return parseContainer(modifier, user.isJailed() ? "Yes" : "No");
        }
        return parseContainer(modifier, "No");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        User user = essentials.getUser(uuid);
        return user != null && user.isJailed();
    }

}

package main.java.com.djrapitops.plan.data.additional.essentials;

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
 * Gives Muted boolean value.
 * 
 * @author Rsl1122
 * @since 3.1.0
 * @see EssentialsHook
 */
public class EssentialsMuted extends PluginData {

    private Essentials essentials;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param essentials Instance of Essentials plugin.
     */
    public EssentialsMuted(Essentials essentials) {
        super("Essentials", "muted", AnalysisType.BOOLEAN_PERCENTAGE, AnalysisType.BOOLEAN_TOTAL);
        this.essentials = essentials;
        super.setIcon("bell-slash-o");
        super.setAnalysisOnly(false);
        super.setPrefix("Muted: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifier, UUID uuid) {
        User user = essentials.getUser(uuid);
        if (user != null) {
            return parseContainer("", user.isMuted() ? "Yes" : "No");
        }
        return parseContainer(modifier, "No");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        User user = essentials.getUser(uuid);
        return user != null && user.isMuted();
    }

}

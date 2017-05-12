package main.java.com.djrapitops.plan.data.additional.essentials;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;

/**
 *
 * @author Rsl1122
 */
public class EssentialsMuted extends PluginData {

    private Essentials essentials;
    
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
            return parseContainer("", user.isMuted()? "Yes" : "No");
        }
        return "";
    }

    @Override
    public Serializable getValue(UUID uuid) {
        User user = essentials.getUser(uuid);
        return user != null && user.isMuted();
    }
    
}

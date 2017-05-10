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
public class EssentialsJailed extends PluginData {

    private Essentials essentials;
    
    public EssentialsJailed(Essentials essentials) {
        super("Essentials", "jailed", AnalysisType.BOOLEAN_PERCENTAGE, AnalysisType.BOOLEAN_TOTAL);
        this.essentials = essentials;
        super.setAnalysisOnly(false);
        super.setPrefix("Jailed: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifier, UUID uuid) {
        User user = essentials.getUser(uuid);
        if (user != null) {
            return parseContainer(modifier, user.isJailed() ? "Yes" : "No");
        }
        return "";
    }

    @Override
    public Serializable getValue(UUID uuid) {
        User user = essentials.getUser(uuid);
        return user != null && user.isJailed();
    }
    
}

package main.java.com.djrapitops.plan.data.additional.essentials;

import main.java.com.djrapitops.plan.data.additional.Hook;
import com.earth2me.essentials.Essentials;
import java.util.List;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.additional.essentials.EssentialsJailed;
import main.java.com.djrapitops.plan.data.additional.essentials.EssentialsMuted;
import main.java.com.djrapitops.plan.data.additional.essentials.EssentialsWarps;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class EssentialsHook extends Hook {

    private Essentials ess;
    private List<String> warps;

    /**
     * Hooks to Essentials plugin
     *
     */
    public EssentialsHook() throws NoClassDefFoundError{
        super("com.earth2me.essentials.Essentials");
        if (super.isEnabled()) {
            ess = getPlugin(Essentials.class);
            API planAPI = Plan.getPlanAPI();
            planAPI.addPluginDataSource(new EssentialsJailed(ess));
            planAPI.addPluginDataSource(new EssentialsMuted(ess));
            planAPI.addPluginDataSource(new EssentialsWarps(ess));
        }
    }
}

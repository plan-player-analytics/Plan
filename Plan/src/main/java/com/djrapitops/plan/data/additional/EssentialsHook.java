package main.java.com.djrapitops.plan.data.additional;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class EssentialsHook extends Hook {

    private final Plan plugin;
    private Essentials ess;
    private List<String> warps;

    /**
     * Hooks to Essentials plugin
     *
     * @param plugin
     */
    public EssentialsHook(Plan plugin) {
        super("com.earth2me.essentials.Essentials");
        this.plugin = plugin;
        if (super.isEnabled()) {
            ess = getPlugin(Essentials.class);
        }
    }

    /**
     * Grabs information not provided by Player class or Plan from Essentials.
     * isEnabled() should be called before this method.
     *
     * @param uuid UUID of player
     * @return HashMap with boolean, int and string values: JAILED boolean,
     * MUTED boolean
     */
    public HashMap<String, Serializable> getEssentialsData(UUID uuid) {
        HashMap<String, Serializable> essData = new HashMap<>();
        User user = ess.getUser(uuid);
        if (user != null) {
            essData.put("JAILED", user.isJailed());
            essData.put("MUTED", user.isMuted());
        } else {
            essData.put("JAILED", false);
            essData.put("MUTED", false);
        }
        return essData;
    }

    /**
     * @return Warp list
     */
    public List<String> getWarps() {
        return (ArrayList<String>) ess.getWarps().getList();
    }

}

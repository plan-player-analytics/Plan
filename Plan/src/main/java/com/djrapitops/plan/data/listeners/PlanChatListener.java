package main.java.com.djrapitops.plan.data.listeners;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handlers.BasicInfoHandler;
import main.java.com.djrapitops.plan.data.handlers.DemographicsHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 *
 * @author Rsl1122
 */
public class PlanChatListener implements Listener {

    private final Plan plugin;
    private final DataCacheHandler handler;
    private final DemographicsHandler demographicsHandler;
    private final BasicInfoHandler basicInfoH;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public PlanChatListener(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        demographicsHandler = handler.getDemographicsHandler();
        basicInfoH = handler.getBasicInfoHandler();
    }

    /**
     * ChatEvent listener.
     *
     * @param event Fired Event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        DBCallableProcessor chatProcessor = new DBCallableProcessor() {
            @Override
            public void process(UserData data) {
                basicInfoH.addNickname(p.getDisplayName(), data);
                demographicsHandler.handleChatEvent(event, data);
            }
        };
        handler.getUserDataForProcessing(chatProcessor, p.getUniqueId());
    }
}

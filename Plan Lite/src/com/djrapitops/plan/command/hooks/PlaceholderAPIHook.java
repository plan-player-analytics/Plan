package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import com.djrapitops.plan.command.utils.DataUtils;
import com.google.common.base.Optional;
import java.util.HashMap;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends EZPlaceholderHook {

    private final Plan plan;

    public PlaceholderAPIHook(Plan plan) {
        super(plan, "plan");
        this.plan = plan;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        HashMap<String, DataPoint> data = DataFormatUtils.removeExtraDataPoints(DataUtils.getData(true, player.getDisplayName()));
        String key = identifier.toUpperCase();
        if (Optional.of(data.get(key)).isPresent()) {
            return data.get(key).data();
        } else {
            plan.logToFile("PlaceholderAPIHOOK\nFailed to get data\n" + player.getDisplayName() + "\n" + key);
        }
        return null;
    }

    @Override
    public boolean hook() {
        return super.hook();
    }

}

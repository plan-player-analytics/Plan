package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import com.djrapitops.plan.command.utils.DataUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends EZPlaceholderHook implements Hook {

    private final List<String> placeholders;
    private final Plan plan;

    public PlaceholderAPIHook(Plan plan, String[] placeholders) {
        super(plan, "plan");
        this.plan = plan;
        this.placeholders = new ArrayList<>();
        this.placeholders.addAll(Arrays.asList(placeholders));
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        HashMap<String, String> data = DataFormatUtils.removeExtraDataPoints(DataUtils.getData(true, player.getDisplayName()));
        String key = identifier.toUpperCase();
        if (data.get(key) != null) {
            return data.get(key);
        } else {
            plan.logToFile("PlaceholderAPIHOOK\nFailed to get data\n" + player.getDisplayName() + "\n" + key);
        }
        return null;
    }

    @Override
    public HashMap<String, String> getData(String playerName) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        Player player = Bukkit.getPlayer(UUIDFetcher.getUUIDOf(playerName));
        for (String placeholder : placeholders) {
            if (placeholder.length() > 0 && placeholder.contains("%") || placeholder.contains("{")) {
                String key = ("" + placeholder.subSequence(1, placeholder.length() - 1)).toUpperCase();
                data.put("PHA-" + key.toUpperCase(), PlaceholderAPI.setPlaceholders(player, placeholder));
            }
        }
        return data;
    }

    @Override
    public HashMap<String, String> getAllData(String player) throws Exception {
        return getData(player);
    }

    public void setPlaceholders(String[] placeholders) {
        this.placeholders.clear();
        this.placeholders.addAll(Arrays.asList(placeholders));
    }

    @Override
    public boolean hook() {
        return super.hook();
    }

}

package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import java.util.HashMap;
import me.edge209.OnTime.OnTimeAPI;

public class OnTimeHook implements Hook {

    private Plan plugin;

    public OnTimeHook(Plan plugin) throws Exception {
        this.plugin = plugin;
        if (OnTimeAPI.data.LASTLOGIN == null) {
            throw new Exception("Ontime not installed.");
        }
    }

    @Override
    public HashMap<String, String> getData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        try {
            data.put("ONT-LAST LOGIN", "" + OnTimeAPI.getPlayerTimeData(player, OnTimeAPI.data.LASTLOGIN));
            data.put("ONT-TOTAL PLAY", "" + OnTimeAPI.getPlayerTimeData(player, OnTimeAPI.data.TOTALPLAY));
            data.put("ONT-TOTAL VOTES", "" + OnTimeAPI.getPlayerTimeData(player, OnTimeAPI.data.TOTALVOTE));
            data.put("ONT-TOTAL REFERRED", "" + OnTimeAPI.getPlayerTimeData(player, OnTimeAPI.data.TOTALREFER));
        } catch (NoClassDefFoundError e) {
            plugin.logToFile("ONTIME HOOK ERROR"
                    + "\nOntimeHook enabled but failing, could not get data."
                    + "\n" + e
                    + "\n" + e.getMessage());

        }
        return data;
    }

    @Override
    public HashMap<String, String> getAllData(String player) throws Exception {
        return getData(player);
    }
}

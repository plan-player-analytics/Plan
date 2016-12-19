package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.api.Hook;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;
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
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        try {
            data.put("ONT-LAST LOGIN", new DataPoint("" + OnTimeAPI.getPlayerTimeData(player, OnTimeAPI.data.LASTLOGIN), DataType.DEPRECATED));
            data.put("ONT-TOTAL PLAY", new DataPoint("" + OnTimeAPI.getPlayerTimeData(player, OnTimeAPI.data.TOTALPLAY), DataType.TIME));
            data.put("ONT-TOTAL VOTES", new DataPoint("" + OnTimeAPI.getPlayerTimeData(player, OnTimeAPI.data.TOTALVOTE), DataType.AMOUNT));
            data.put("ONT-TOTAL REFERRED", new DataPoint("" + OnTimeAPI.getPlayerTimeData(player, OnTimeAPI.data.TOTALREFER), DataType.AMOUNT));
        } catch (NoClassDefFoundError e) {
            plugin.logToFile("ONTIME HOOK ERROR"
                    + "\nOntimeHook enabled but failing, could not get data."
                    + "\n" + e
                    + "\n" + e.getMessage());

        }
        return data;
    }

    @Override
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        return getData(player);
    }
}

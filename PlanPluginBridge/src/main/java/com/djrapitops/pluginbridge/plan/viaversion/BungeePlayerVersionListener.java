/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plugin.api.utility.log.Log;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import us.myles.ViaVersion.api.ViaAPI;

import java.util.UUID;

/**
 * Class responsible for listening join events for Version protocol.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class BungeePlayerVersionListener implements Listener {

    private ViaAPI viaAPI;

    public BungeePlayerVersionListener(ViaAPI viaAPI) {
        this.viaAPI = viaAPI;
    }

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int playerVersion = viaAPI.getPlayerVersion(uuid);
        Processing.submitNonCritical(() -> {
            try {
                new ProtocolTable((SQLDB) Database.getActive()).saveProtocolVersion(uuid, playerVersion);
            } catch (DBOpException e) {
                Log.toLog(this.getClass(), e);
            }
        });
    }
}

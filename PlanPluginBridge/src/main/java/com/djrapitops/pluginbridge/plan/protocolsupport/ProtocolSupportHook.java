/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.protocolsupport;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.pluginbridge.plan.Hook;
import com.djrapitops.pluginbridge.plan.viaversion.ProtocolTable;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook for ProtocolSupport plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class ProtocolSupportHook extends Hook {

    private final Plan plugin;
    private final Processing processing;
    private final DBSystem dbSystem;

    @Inject
    public ProtocolSupportHook(
            Plan plugin,
            Processing processing,
            DBSystem dbSystem
    ) {
        super("protocolsupport.ProtocolSupport");
        this.plugin = plugin;
        this.processing = processing;
        this.dbSystem = dbSystem;
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (!enabled) {
            return;
        }
        ProtocolTable protocolTable = new ProtocolTable((SQLDB) dbSystem.getDatabase());
        try {
            protocolTable.createTable();
        } catch (DBException e) {
            throw new DBOpException("Failed to create Protocol table", e);
        }
        plugin.registerListener(new PlayerVersionListener(processing, protocolTable));
        handler.addPluginDataSource(new ProtocolSupportData(protocolTable));
    }
}
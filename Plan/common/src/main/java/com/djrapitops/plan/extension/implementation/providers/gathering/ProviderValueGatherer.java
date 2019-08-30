/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.extension.implementation.providers.gathering;

import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.DataProviderExtractor;
import com.djrapitops.plan.extension.implementation.TabInformation;
import com.djrapitops.plan.extension.implementation.providers.DataProviders;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.StorePluginTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreTabInformationTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.RemoveInvalidResultsTransaction;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.storage.database.DBSystem;

import java.util.UUID;

/**
 * Object that can be called to place data about players to the database.
 *
 * @author Rsl1122
 */
public class ProviderValueGatherer {

    private final CallEvents[] callEvents;
    private final DataProviderExtractor extractor;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    private DataProviders dataProviders;
    private BooleanProviderValueGatherer booleanGatherer;
    private NumberProviderValueGatherer numberGatherer;
    private DoubleAndPercentageProviderValueGatherer doubleAndPercentageGatherer;
    private StringProviderValueGatherer stringGatherer;
    private TableProviderValueGatherer tableGatherer;
    private GroupProviderValueGatherer groupGatherer;


    public ProviderValueGatherer(
            DataExtension extension,
            DataProviderExtractor extractor,
            DBSystem dbSystem,
            ServerInfo serverInfo
    ) {
        this.callEvents = extension.callExtensionMethodsOn();
        this.extractor = extractor;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;

        String pluginName = extractor.getPluginName();
        UUID serverUUID = serverInfo.getServerUUID();
        Database database = dbSystem.getDatabase();
        dataProviders = extractor.getDataProviders();
        booleanGatherer = new BooleanProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
        numberGatherer = new NumberProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
        doubleAndPercentageGatherer = new DoubleAndPercentageProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
        stringGatherer = new StringProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
        tableGatherer = new TableProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
        groupGatherer = new GroupProviderValueGatherer(
                pluginName, extension, serverUUID, database, dataProviders
        );
    }

    public void disableMethodFromUse(MethodWrapper method) {
        dataProviders.removeProviderWithMethod(method);
    }

    public boolean canCallEvent(CallEvents event) {
        if (event == CallEvents.MANUAL) {
            return true;
        }
        for (CallEvents accepted : callEvents) {
            if (event == accepted) {
                return true;
            }
        }
        return false;
    }

    public String getPluginName() {
        return extractor.getPluginName();
    }

    public void storeExtensionInformation() {
        String pluginName = extractor.getPluginName();
        Icon pluginIcon = extractor.getPluginIcon();

        long time = System.currentTimeMillis();
        UUID serverUUID = serverInfo.getServerUUID();

        Database database = dbSystem.getDatabase();
        database.executeTransaction(new StoreIconTransaction(pluginIcon));
        database.executeTransaction(new StorePluginTransaction(pluginName, time, serverUUID, pluginIcon));
        for (TabInformation tab : extractor.getPluginTabs()) {
            database.executeTransaction(new StoreIconTransaction(tab.getTabIcon()));
            database.executeTransaction(new StoreTabInformationTransaction(pluginName, serverUUID, tab));
        }

        database.executeTransaction(new RemoveInvalidResultsTransaction(pluginName, serverUUID, extractor.getInvalidatedMethods()));
    }

    public void updateValues(UUID playerUUID, String playerName) {
        Conditions conditions = booleanGatherer.gatherBooleanDataOfPlayer(playerUUID, playerName);
        numberGatherer.gatherNumberDataOfPlayer(playerUUID, playerName, conditions);
        doubleAndPercentageGatherer.gatherDoubleDataOfPlayer(playerUUID, playerName, conditions);
        stringGatherer.gatherStringDataOfPlayer(playerUUID, playerName, conditions);
        tableGatherer.gatherTableDataOfPlayer(playerUUID, playerName, conditions);
        groupGatherer.gatherGroupDataOfPlayer(playerUUID, playerName, conditions);
    }

    public void updateValues() {
        Conditions conditions = booleanGatherer.gatherBooleanDataOfServer();
        numberGatherer.gatherNumberDataOfServer(conditions);
        doubleAndPercentageGatherer.gatherDoubleDataOfServer(conditions);
        stringGatherer.gatherStringDataOfServer(conditions);
        tableGatherer.gatherTableDataOfServer(conditions);
    }
}
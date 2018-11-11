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
package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.processing.processors.info.InfoProcessors;
import com.djrapitops.plan.system.processing.processors.player.PlayerProcessors;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Factory for creating Runnables to run with {@link com.djrapitops.plan.system.processing.Processing}.
 *
 * @author Rsl1122
 */
@Singleton
public class Processors {

    private final PlayerProcessors playerProcessors;
    private final InfoProcessors infoProcessors;

    private final Lazy<DBSystem> dbSystem;

    @Inject
    public Processors(
            PlayerProcessors playerProcessors,
            InfoProcessors infoProcessors,

            Lazy<DBSystem> dbSystem
    ) {
        this.playerProcessors = playerProcessors;
        this.infoProcessors = infoProcessors;
        this.dbSystem = dbSystem;
    }

    public TPSInsertProcessor tpsInsertProcessor(List<TPS> tpsList) {
        return new TPSInsertProcessor(tpsList, dbSystem.get().getDatabase());
    }

    public CommandProcessor commandProcessor(String command) {
        return new CommandProcessor(command, dbSystem.get().getDatabase());
    }

    public PlayerProcessors player() {
        return playerProcessors;
    }

    public InfoProcessors info() {
        return infoProcessors;
    }
}
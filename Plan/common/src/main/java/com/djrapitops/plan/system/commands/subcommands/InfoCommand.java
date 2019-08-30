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
package com.djrapitops.plan.system.commands.subcommands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.settings.locale.lang.CommandLang;
import com.djrapitops.plan.system.settings.locale.lang.GenericLang;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.access.queries.objects.ServerQueries;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;

import javax.inject.Inject;

/**
 * This SubCommand is used to view the version and the database type in use.
 *
 * @author Rsl1122
 */
public class InfoCommand extends CommandNode {

    private final PlanPlugin plugin;
    private final Locale locale;
    private final DBSystem dbSystem;
    private final VersionCheckSystem versionCheckSystem;

    @Inject
    public InfoCommand(
            PlanPlugin plugin,
            Locale locale,
            DBSystem dbSystem,
            VersionCheckSystem versionCheckSystem
    ) {
        super("info", Permissions.INFO.getPermission(), CommandType.CONSOLE);

        this.plugin = plugin;
        this.locale = locale;
        this.dbSystem = dbSystem;
        this.versionCheckSystem = versionCheckSystem;

        setShortHelp(locale.get(CmdHelpLang.INFO).toString());
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        String yes = locale.getString(GenericLang.YES);
        String no = locale.getString(GenericLang.NO);

        Database database = dbSystem.getDatabase();

        String updateAvailable = versionCheckSystem.isNewVersionAvailable() ? yes : no;
        String proxyAvailable = database.query(ServerQueries.fetchProxyServerInformation()).isPresent() ? yes : no;


        String[] messages = {
                locale.getString(CommandLang.HEADER_INFO),
                "",
                locale.getString(CommandLang.INFO_VERSION, plugin.getVersion()),
                locale.getString(CommandLang.INFO_UPDATE, updateAvailable),
                locale.getString(CommandLang.INFO_DATABASE, database.getType().getName() + " (" + database.getState().name() + ")"),
                locale.getString(CommandLang.INFO_PROXY_CONNECTION, proxyAvailable),
                "",
                ">"
        };
        sender.sendMessage(messages);
    }

}

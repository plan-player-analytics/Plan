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
package com.djrapitops.plan.commands.subcommands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.ManageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;

/**
 * This manage SubCommand is used to swap to a different database and reload the
 * plugin if the connection to the new database can be established.
 *
 * @author Rsl1122
 */
public class ManageHotSwapCommand extends CommandNode {

    private final PlanPlugin plugin;
    private final Locale locale;
    private final DBSystem dbSystem;
    private final PlanConfig config;
    private final ErrorLogger errorLogger;

    @Inject
    public ManageHotSwapCommand(PlanPlugin plugin, Locale locale, DBSystem dbSystem, PlanConfig config, ErrorLogger errorLogger) {
        super("hotswap", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.plugin = plugin;
        this.locale = locale;
        this.dbSystem = dbSystem;
        this.config = config;
        this.errorLogger = errorLogger;

        setArguments("<DB>");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_HOTSWAP));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        String dbName = args[0].toLowerCase();

        boolean isCorrectDB = DBType.exists(dbName);
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, dbName)));

        Verify.isFalse(dbName.equals(dbSystem.getDatabase().getType().getConfigName()),
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_SAME_DB)));

        try {
            Database database = dbSystem.getActiveDatabaseByName(dbName);
            database.init();

            if (database.getState() == Database.State.CLOSED) {
                return;
            }
        } catch (Exception e) {
            errorLogger.log(L.ERROR, this.getClass(), e);
            sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            return;
        }

        try {
            config.set(DatabaseSettings.TYPE, dbName);
            config.save();
        } catch (IOException e) {
            errorLogger.log(L.ERROR, this.getClass(), e);
            return;
        }
        plugin.reloadPlugin(true);
    }
}

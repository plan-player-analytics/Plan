package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.Condition;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.utilities.Verify;

/**
 * This manage SubCommand is used to swap to a different database and reload the
 * plugin if the connection to the new database can be established.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageHotSwapCommand extends CommandNode {

    private final Plan plugin;

    public ManageHotSwapCommand(Plan plugin) {
        super("hotswap", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);
        setShortHelp(Locale.get(Msg.CMD_USG_MANAGE_HOTSWAP).toString());
        setArguments("<DB>");
        setInDepthHelp(Locale.get(Msg.CMD_HELP_MANAGE_HOTSWAP).toArray());
        this.plugin = plugin;

    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
            return;
        }
        String dbName = args[0].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(dbName) || "mysql".equals(dbName);

        if (!Condition.isTrue(isCorrectDB, Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + dbName, sender)) {
            return;
        }

        if (Condition.isTrue(dbName.equals(Database.getActive().getConfigName()), Locale.get(Msg.MANAGE_FAIL_SAME_DB).toString(), sender)) {
            return;
        }

        try {
            final Database database = DBSystem.getActiveDatabaseByName(dbName);

            // If DB is null return
            if (!Condition.isTrue(Verify.notNull(database), Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString(), sender)) {
                Log.error(dbName + " was null!");
                return;
            }

            if (!database.isOpen()) {
                return;
            }
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
            return;
        }

        Settings.DB_TYPE.set(dbName);

        Settings.save();
        plugin.reloadPlugin(true);
    }
}

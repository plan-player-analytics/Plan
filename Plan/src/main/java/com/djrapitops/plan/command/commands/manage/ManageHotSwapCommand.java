package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
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

    private final PlanPlugin plugin;

    public ManageHotSwapCommand(PlanPlugin plugin) {
        super("hotswap", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);
        setShortHelp(Locale.get(Msg.CMD_USG_MANAGE_HOTSWAP).toString());
        setArguments("<DB>");
        setInDepthHelp(Locale.get(Msg.CMD_HELP_MANAGE_HOTSWAP).toArray());
        this.plugin = plugin;

    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString()));

        String dbName = args[0].toLowerCase();

        boolean isCorrectDB = Verify.equalsOne(dbName, "sqlite", "mysql");
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + dbName));

        Verify.isFalse(dbName.equals(Database.getActive().getConfigName()),
                () -> new IllegalArgumentException(Locale.get(Msg.MANAGE_FAIL_SAME_DB).toString()));

        try {
            final Database database = DBSystem.getActiveDatabaseByName(dbName);

            Verify.nullCheck(database, NullPointerException::new);

            if (!database.isOpen()) {
                return;
            }
        } catch (NullPointerException e) {
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
            return;
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

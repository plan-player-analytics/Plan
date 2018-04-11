package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.FatalDBException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.Condition;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

/**
 * This manage SubCommand is used to clear a database of all data.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageClearCommand extends CommandNode {

    private final Plan plugin;

    public ManageClearCommand(Plan plugin) {
        super("clear", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);
        setShortHelp(Locale.get(Msg.CMD_USG_MANAGE_CLEAR).toString());
        setArguments("<DB>", "[-a]");
        setInDepthHelp(Locale.get(Msg.CMD_HELP_MANAGE_CLEAR).toArray());
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

        if (!Condition.isTrue(Verify.contains("-a", args), Locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(Locale.get(Msg.MANAGE_NOTIFY_REMOVE).parse(args[0])), sender)) {
            return;
        }

        try {
            Database database = DBSystem.getActiveDatabaseByName(dbName);
            runClearTask(sender, database);
        } catch (DBInitException e) {
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
        }
    }

    private void runClearTask(ISender sender, Database database) {
        RunnableFactory.createNew(new AbsRunnable("DBClearTask") {
            @Override
            public void run() {
                try {
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_START).parse());

                    database.remove().everything();

                    long now = MiscUtils.getTime();
                    SessionCache.clear();
                    plugin.getServer().getOnlinePlayers().forEach(
                            player -> SessionCache.getInstance().cacheSession(player.getUniqueId(),
                                    new Session(now, player.getWorld().getName(), player.getGameMode().name()))
                    );
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_CLEAR_SUCCESS).toString());
                } catch (FatalDBException e) {
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString()
                            + " Error was fatal, so all information may not have been removed.");
                    Log.toLog(this.getClass(), e);
                } catch (DBException e) {
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                    Log.toLog(this.getClass(), e);
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}

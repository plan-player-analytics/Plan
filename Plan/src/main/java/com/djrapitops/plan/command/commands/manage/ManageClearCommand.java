package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.SessionCache;
import main.java.com.djrapitops.plan.utilities.Condition;
import main.java.com.djrapitops.plan.utilities.ManageUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.sql.SQLException;

/**
 * This manage subcommand is used to clear a database of all data.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageClearCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageClearCommand(Plan plugin) {
        super("clear",
                CommandType.PLAYER_OR_ARGS,
                Permissions.MANAGE.getPermission(),
                Locale.get(Msg.CMD_USG_MANAGE_CLEAR).toString(),
                "<DB> [-a]");

        this.plugin = plugin;

    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_MANAGE_CLEAR).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
            return true;
        }

        String dbName = args[0].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(dbName) || "mysql".equals(dbName);

        if (!Condition.isTrue(isCorrectDB, Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + dbName, sender)) {
            return true;
        }

        if (!Condition.isTrue(Verify.contains("-a", args), Locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(Locale.get(Msg.MANAGE_NOTIFY_REMOVE).parse(args[0])), sender)) {
            return true;
        }

        try {
            Database database = ManageUtils.getDB(plugin, dbName);
            runClearTask(sender, database);
        } catch (DatabaseInitException e) {
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
        }
        return true;
    }

    private void runClearTask(ISender sender, Database database) {
        RunnableFactory.createNew(new AbsRunnable("DBClearTask") {
            @Override
            public void run() {
                try {
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_START).parse());

                    database.removeAllData();

                    DataCache dataCache = plugin.getDataCache();
                    long now = MiscUtils.getTime();
                    SessionCache.clear();
                    plugin.getServer().getOnlinePlayers().forEach(
                            player -> dataCache.cacheSession(player.getUniqueId(),
                                    new Session(now, player.getWorld().getName(), player.getGameMode().name()))
                    );
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_CLEAR_SUCCESS).toString());
                } catch (SQLException e) {
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                    Log.toLog(this.getClass().getSimpleName() + "/" + this.getTaskName(), e);
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}

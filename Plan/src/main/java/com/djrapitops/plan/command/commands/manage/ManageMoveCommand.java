package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.ManageUtils;

/**
 * This manage subcommand is used to move all data from one database to another.
 * <p>
 * Destination database will be cleared.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageMoveCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageMoveCommand(Plan plugin) {
        super("move",
                CommandType.CONSOLE_WITH_ARGUMENTS,
                Permissions.MANAGE.getPermission(),
                Locale.get(Msg.CMD_USG_MANAGE_MOVE).toString(),
                "<fromDB> <toDB> [-a]");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(args.length >= 2, Locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(this.getArguments()), sender)) {
            return true;
        }

        String fromDB = args[0].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(fromDB) || "mysql".equals(fromDB);

        if (!Check.isTrue(isCorrectDB, Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + fromDB, sender)) {
            return true;
        }

        String toDB = args[1].toLowerCase();
        isCorrectDB = "sqlite".equals(toDB) || "mysql".equals(toDB);

        if (!Check.isTrue(isCorrectDB, Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + toDB, sender)) {
            return true;
        }

        if (!Check.isTrue(!Verify.equalsIgnoreCase(fromDB, toDB), Locale.get(Msg.MANAGE_FAIL_SAME_DB).toString(), sender)) {
            return true;
        }

        if (!Check.isTrue(Verify.contains("-a", args), Locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(Locale.get(Msg.MANAGE_NOTIFY_REMOVE).parse(args[1])), sender)) {
            return true;
        }

        try {
            final Database fromDatabase = ManageUtils.getDB(plugin, fromDB);
            final Database toDatabase = ManageUtils.getDB(plugin, toDB);

            runMoveTask(fromDatabase, toDatabase, sender);
        } catch (Exception e) {
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
        }
        return true;
    }

    private void runMoveTask(final Database fromDatabase, final Database toDatabase, ISender sender) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("DBMoveTask") {
            @Override
            public void run() {
                try {
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_START).parse());

                    ManageUtils.clearAndCopy(toDatabase, fromDatabase);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_MOVE_SUCCESS).toString());
                    boolean movedToCurrentDatabase = Verify.equalsIgnoreCase(toDatabase.getConfigName(), plugin.getDB().getConfigName());
                    Check.isTrue(!movedToCurrentDatabase, Locale.get(Msg.MANAGE_INFO_CONFIG_REMINDER).toString(), sender);
                } catch (Exception e) {
                    Log.toLog(this.getClass().getName() + " " + getTaskName(), e);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}

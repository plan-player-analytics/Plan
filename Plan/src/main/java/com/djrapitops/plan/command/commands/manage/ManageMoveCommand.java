package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import java.util.Collection;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.ManageUtils;

/**
 * This manage subcommand is used to move all data from one database to another.
 *
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
        super("move", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_MOVE + "", Phrase.ARG_MOVE + "");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(args.length >= 2, Phrase.COMMAND_REQUIRES_ARGUMENTS.parse(Phrase.USE_MOVE + ""), sender)) {
            return true;
        }

        String fromDB = args[0].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(fromDB) || "mysql".equals(fromDB);

        if (!Check.isTrue(isCorrectDB, Phrase.MANAGE_ERROR_INCORRECT_DB + fromDB, sender)) {
            return true;
        }

        String toDB = args[1].toLowerCase();
        isCorrectDB = "sqlite".equals(toDB) || "mysql".equals(toDB);

        if (!Check.isTrue(isCorrectDB, Phrase.MANAGE_ERROR_INCORRECT_DB + toDB, sender)) {
            return true;
        }
        if (!Check.isTrue(!Verify.equalsIgnoreCase(fromDB, toDB), Phrase.MANAGE_ERROR_SAME_DB + "", sender)) {
            return true;
        }
        if (!Check.isTrue(Verify.contains("-a", args), Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.parse(Phrase.WARN_REMOVE.parse(args[1])), sender)) {
            return true;
        }

        final Database fromDatabase = ManageUtils.getDB(plugin, fromDB);

        if (!Check.isTrue(Verify.notNull(fromDatabase), Phrase.MANAGE_DATABASE_FAILURE + "", sender)) {
            Log.error(fromDB + " was null!");
            return true;
        }

        final Database toDatabase = ManageUtils.getDB(plugin, toDB);

        if (!Check.isTrue(Verify.notNull(toDatabase), Phrase.MANAGE_DATABASE_FAILURE + "", sender)) {
            Log.error(toDB + " was null!");
            return true;
        }

        runMoveTask(fromDatabase, toDatabase, sender);
        return true;
    }

    private void runMoveTask(final Database fromDatabase, final Database toDatabase, ISender sender) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("DBMoveTask") {
            @Override
            public void run() {
                try {
                    final Collection<UUID> uuids = ManageUtils.getUUIDS(fromDatabase);
                    if (Check.isTrue(Verify.isEmpty(uuids), Phrase.MANAGE_ERROR_NO_PLAYERS + " (" + fromDatabase.getName() + ")", sender)) {
                        return;
                    }
                    sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                    if (ManageUtils.clearAndCopy(toDatabase, fromDatabase, uuids)) {
                        sender.sendMessage(Phrase.MANAGE_MOVE_SUCCESS + "");
                        boolean movedToCurrentDatabase = Verify.equalsIgnoreCase(toDatabase.getConfigName(), plugin.getDB().getConfigName());

                        Check.isTrue(!movedToCurrentDatabase, Phrase.MANAGE_DB_CONFIG_REMINDER + "", sender);
                    } else {
                        sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL + "");
                    }
                } catch (Exception e) {
                    Log.toLog(this.getClass().getName() + " " + getTaskName(), e);
                    sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL.toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}

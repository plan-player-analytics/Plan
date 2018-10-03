package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.UUID;

/**
 * This manage subcommand is used to remove a single player's data from the
 * database.
 *
 * @author Rsl1122
 */
@Singleton
public class ManageRemoveCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final Database database;
    private final UUIDUtility uuidUtility;
    private final ErrorHandler errorHandler;

    @Inject
    public ManageRemoveCommand(
            Locale locale,
            Processing processing,
            Database database,
            UUIDUtility uuidUtility,
            ErrorHandler errorHandler
    ) {
        super("remove|delete", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.processing = processing;
        this.database = database;
        this.uuidUtility = uuidUtility;
        this.errorHandler = errorHandler;

        setArguments("<player>", "[-a]");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_REMOVE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_REMOVE));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.MANAGE);

        if (playerName == null) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_NO_PERMISSION));
            return;
        }

        runRemoveTask(playerName, sender, args);
    }

    private void runRemoveTask(String playerName, Sender sender, String[] args) {
        processing.submitCritical(() -> {
            try {
                UUID uuid = uuidUtility.getUUIDOf(playerName);

                if (uuid == null) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_VALID));
                    return;
                }

                if (!database.check().isPlayerRegistered(uuid)) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_KNOWN));
                    return;
                }

                if (!Verify.contains("-a", args)) {
                    sender.sendMessage(locale.getString(ManageLang.CONFIRMATION, locale.getString(ManageLang.CONFIRM_REMOVAL, database.getName())));
                    return;
                }

                sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

                database.remove().player(uuid);

                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (DBOpException e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }
}

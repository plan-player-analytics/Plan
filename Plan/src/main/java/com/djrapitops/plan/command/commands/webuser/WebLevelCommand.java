package com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;

import javax.inject.Inject;

/**
 * Subcommand for info about permission levels.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebLevelCommand extends CommandNode {

    private final Locale locale;

    @Inject
    public WebLevelCommand(Locale locale) {
        super("level", Permissions.MANAGE_WEB.getPerm(), CommandType.CONSOLE);

        this.locale = locale;

        setShortHelp(locale.getString(CmdHelpLang.WEB_LEVEL));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        sender.sendMessage(locale.getArray(CommandLang.WEB_PERMISSION_LEVELS));
    }

}

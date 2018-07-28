package com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.Msg;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.settings.ColorScheme;

/**
 * Subcommand for info about permission levels.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebLevelCommand extends CommandNode {

    private final PlanPlugin plugin;
    private final Locale locale;

    public WebLevelCommand(PlanPlugin plugin) {
        super("level", Permissions.MANAGE_WEB.getPerm(), CommandType.CONSOLE);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.WEB_LEVEL));
        this.plugin = plugin;
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        ColorScheme cs = plugin.getColorScheme();
        String sCol = cs.getSecondaryColor();
        String cmdFooter = locale.get(Msg.CMD_CONSTANT_FOOTER).parse();

        String[] messages = new String[]{
                cmdFooter,
                sCol + "0: Access all pages",
                sCol + "1: Access '/players' and all inspect pages",
                sCol + "2: Access inspect page with the same username as the webuser",
                sCol + "3+: No permissions",
                cmdFooter
        };

        sender.sendMessage(messages);
    }

}

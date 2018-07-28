package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

public class DisableCommand extends CommandNode {

    private final Locale locale;

    public DisableCommand(PlanPlugin plugin) {
        super("disable", "plan.reload", CommandType.ALL);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.get(CmdHelpLang.DISABLE).toString());
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        PlanPlugin.getInstance().onDisable();
        sender.sendMessage(
                "§aPlan systems are now disabled. " +
                        "You can still use /planbungee reload to restart the plugin."
        );
    }
}

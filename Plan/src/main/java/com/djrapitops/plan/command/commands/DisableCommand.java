package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

public class DisableCommand extends CommandNode {

    private final Locale locale;

    public DisableCommand(PlanPlugin plugin) {
        super("disable", "plan.reload", CommandType.ALL);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.DISABLE));
        setInDepthHelp(locale.getArray(DeepHelpLang.DISABLE));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        PlanPlugin.getInstance().onDisable();
        sender.sendMessage(locale.getString(CommandLang.DISABLE_DISABLED));
    }
}

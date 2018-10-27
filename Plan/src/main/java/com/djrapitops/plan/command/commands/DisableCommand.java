package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;

import javax.inject.Inject;

public class DisableCommand extends CommandNode {

    private final Locale locale;
    private final PlanPlugin plugin;

    @Inject
    public DisableCommand(PlanPlugin plugin, Locale locale) {
        super("disable", "plan.reload", CommandType.ALL);

        this.plugin = plugin;
        this.locale = locale;

        setShortHelp(locale.getString(CmdHelpLang.DISABLE));
        setInDepthHelp(locale.getArray(DeepHelpLang.DISABLE));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        plugin.onDisable();
        sender.sendMessage(locale.getString(CommandLang.DISABLE_DISABLED));
    }
}

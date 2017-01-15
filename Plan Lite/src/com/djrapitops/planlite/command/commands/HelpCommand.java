package com.djrapitops.planlite.command.commands;

import com.djrapitops.planlite.Phrase;
import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.PlanLiteCommand;
import com.djrapitops.planlite.command.CommandType;
import com.djrapitops.planlite.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpCommand extends SubCommand {

    private final PlanLite plugin;
    private final PlanLiteCommand command;

    public HelpCommand(PlanLite plugin, PlanLiteCommand command) {
        super("help,?", "planlite.?", "Show command list.", CommandType.CONSOLE);

        this.plugin = plugin;
        this.command = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();
        
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor + " Player Analytics Lite");

        for (SubCommand command : this.command.getCommands()) {
            if (command.getName().equalsIgnoreCase(getName())) {
                continue;
            }

            if (!sender.hasPermission(command.getPermission())) {
                continue;
            }

            if (!(sender instanceof Player) && command.getCommandType() == CommandType.PLAYER) {
                continue;
            }

            sender.sendMessage(" "+Phrase.BALL+oColor + " /planlite " + command.getFirstName() + tColor + " " + command.getUsage());
        }
        
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString());

        return true;
    }

}

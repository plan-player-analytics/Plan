package main.java.com.djrapitops.plan.command.commands.webuser;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;

/**
 * Subcommand for info about permission levels.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebLevelCommand extends SubCommand {

    private final Plan plugin;

    public WebLevelCommand(Plan plugin) {
        super("check", CommandType.CONSOLE, Permissions.MANAGE_WEB.getPerm(), "Info about permission levels.");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        ColorScheme cs = plugin.getColorScheme();
        String sCol = cs.getSecondaryColor();
        String cmdBall = Phrase.CMD_BALL.parse();
        String[] messages = new String[]{
                Phrase.CMD_FOOTER.parse(),
                cmdBall + sCol + "0: Access all pages",
                cmdBall + sCol + "1: Access '/players' and all inspect pages",
                cmdBall + sCol + "2: Access inspect page with the same username as the webuser",
                cmdBall + sCol + "3+: No permissions",
                Phrase.CMD_FOOTER.parse()
        };
        sender.sendMessage(messages);
        return true;
    }

}

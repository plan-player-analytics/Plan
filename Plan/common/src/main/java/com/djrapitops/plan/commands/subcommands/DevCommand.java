/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.utilities.chat.ChatFormatter;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.utilities.Verify;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Command used for testing functions that are too difficult to unit test.
 *
 * @author Rsl1122
 */
public class DevCommand extends CommandNode {

    private final Locale locale;

    @Inject
    public DevCommand(Locale locale) {
        super("dev", "plan.*", CommandType.PLAYER_OR_ARGS);

        this.locale = locale;

        setShortHelp(locale.get(CmdHelpLang.DEV).toString());
        setArguments("<feature>");
    }

    @Override
    public void onCommand(Sender sender, String cmd, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        sender.sendMessage(" |space");
        sender.sendMessage("§l §r|fat space");
        sender.sendMessage("        |space");
        sender.sendMessage("§l        §r|fat space");

        Object actual = sender.getSender();

        try {
            Method method = actual.getClass().getMethod("sendMessage", String.class);
//            int indent = new Random().nextInt(25);
            String msg = new TextStringBuilder().appendWithSeparators(args, " ").toString();
//            method.invoke(actual, "With indent: " + indent);
//            method.invoke(actual, ChatFormatter.leftPad(msg, indent));
//            method.invoke(actual, "Centered:");
            method.invoke(actual, ChatFormatter.center(msg));
            method.invoke(actual, "Table:");
            String[] split = StringUtils.split(msg, ':');
            int columnCount = split[0].length() - split[0].replace("-", "").length();
            method.invoke(actual, ChatFormatter.columns(columnCount, split, "-"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

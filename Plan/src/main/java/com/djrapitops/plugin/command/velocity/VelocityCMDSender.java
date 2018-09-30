package com.djrapitops.plugin.command.velocity;

import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SenderType;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.format.TextDecoration;

/**
 * Class that wraps velocity's CommandSource into an ISender.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class VelocityCMDSender implements ISender {

    private final CommandSource cs;

    public VelocityCMDSender(CommandSource cs) {
        this.cs = cs;
    }

    @Override
    public String getName() {
        if(cs instanceof Player) {
            return ((Player) cs).getUsername();
        }
        return "Unknown";
    }

    @Override
    public void sendMessage(String string) {
        cs.sendMessage(TextComponent.of(string));
    }

    @Override
    public void sendLink(String pretext, String linkMsg, String url) {
        TextComponent message = TextComponent.of(pretext)
                .append(TextComponent.of(linkMsg)
                        .decoration(TextDecoration.UNDERLINE, TextDecoration.State.TRUE))
                .clickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        cs.sendMessage(message);
    }

    @Override
    public void sendLink(String linkMsg, String url) {
        sendLink("", linkMsg, url);
    }

    @Override
    public boolean hasPermission(String string) {
        return cs.hasPermission(string);
    }

    @Override
    public void sendMessage(String[] strings) {
        for (int i = 1; i < strings.length; i++) {
            sendMessage(strings[i]);
        }
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public SenderType getSenderType() {
        return cs instanceof Player ? SenderType.PLAYER : SenderType.CONSOLE;
    }

    @Override
    public CommandSource getSender() {
        return cs;
    }

}

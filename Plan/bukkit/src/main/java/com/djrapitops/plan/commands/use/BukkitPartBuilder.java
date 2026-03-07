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
package com.djrapitops.plan.commands.use;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.text.TextStringBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class BukkitPartBuilder implements MessageBuilder {

    private final BukkitPartBuilder previous;
    private final ComponentBuilder part;
    private BukkitCMDSender sender;

    public BukkitPartBuilder(BukkitCMDSender sender) {
        this((BukkitPartBuilder) null);
        this.sender = sender;
    }

    public BukkitPartBuilder(BukkitPartBuilder previous) {
        this.part = new ComponentBuilder("");
        this.previous = previous;
    }

    @Override
    public MessageBuilder addPart(String text) {
        BukkitPartBuilder nextPart = new BukkitPartBuilder(this);
        try {
            nextPart.part.appendLegacy(text);
        } catch (NoSuchMethodError oldVersion) { // not supported in 1.8
            nextPart.part.append(ChatColor.translateAlternateColorCodes('\u00a7', text));
        }
        return nextPart;
    }

    @Override
    public MessageBuilder newLine() {
        part.append("\n");
        return new BukkitPartBuilder(this);
    }

    @Override
    public MessageBuilder link(String url) {
        part.event(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return this;
    }

    @Override
    public MessageBuilder command(String command) {
        part.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    @Override
    public MessageBuilder hover(String text) {
        part.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
        return this;
    }

    @Override
    public MessageBuilder hover(String... lines) {
        return hover(Arrays.asList(lines));
    }

    @Override
    public MessageBuilder hover(Collection<String> lines) {
        ComponentBuilder hoverMsg = new ComponentBuilder("");
        hoverMsg.append(new TextStringBuilder().appendWithSeparators(lines, "\n").get());
        part.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMsg.create()));
        return this;
    }

    @Override
    public MessageBuilder indent(int amount) {
        for (int i = 0; i < amount; i++) {
            part.append(" ");
        }
        return this;
    }

    @Override
    public MessageBuilder tabular(CharSequence charSequence) {
        addPart(sender.getFormatter().table(charSequence.toString(), ":"));
        return this;
    }

    @Override
    public void send() {
        if (sender != null) {
            sender.sender.spigot().sendMessage(part.create());
        } else if (previous != null) {
            try {
                previous.part.append(part.create());
            } catch (NoSuchMethodError oldVersion) { // not supported in 1.8
                sendOld();
                return;
            }
            previous.send();
        }
    }

    // Compatibility for 1.8
    private void sendOld() {
        List<BaseComponent> components = new ArrayList<>();

        // Jumping through a bunch of hoops to implement recursive append function dynamically
        BukkitPartBuilder current = this;
        while (current.sender == null) {
            components.addAll(0, Arrays.asList(current.part.create()));
            current = current.previous;
        }

        // CommandSender#spigot#sendMessage(BaseComponent[]) is not supported on 1.8
        CommandSender commandSender = current.sender.sender;
        if (commandSender instanceof Player) {
            ((Player) commandSender).spigot().sendMessage(components.toArray(new BaseComponent[0]));
        } else {
            commandSender.sendMessage(BaseComponent.toLegacyText(components.toArray(new BaseComponent[0])));
        }
    }
}

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

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.text.TextStringBuilder;

import java.util.Arrays;
import java.util.Collection;

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
        // appendLegacy cannot be used as it was added after 1.8
        nextPart.part.append(TextComponent.fromLegacyText(text));
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
        hoverMsg.append(new TextStringBuilder().appendWithSeparators(lines, "\n").build());
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
            previous.part.append(part.create());
            previous.send();
        }
    }
}

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
package com.djrapitops.plan.command.use;

import com.djrapitops.plan.commands.use.MessageBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.apache.commons.text.TextStringBuilder;

import java.util.Arrays;
import java.util.Collection;

class BungeePartBuilder implements MessageBuilder {

    private final BungeePartBuilder previous;
    private final ComponentBuilder part;
    private BungeeCMDSender sender;

    public BungeePartBuilder(BungeeCMDSender sender) {
        this((BungeePartBuilder) null);
        this.sender = sender;
    }

    public BungeePartBuilder(BungeePartBuilder previous) {
        this.part = new ComponentBuilder("");
        this.previous = previous;
    }

    @Override
    public MessageBuilder addPart(String text) {
        BungeePartBuilder nextPart = new BungeePartBuilder(this);
        nextPart.part.appendLegacy(text);
        return nextPart;
    }

    @Override
    public MessageBuilder newLine() {
        part.append("\n");
        return new BungeePartBuilder(this);
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
        part.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(text)));
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
        part.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverMsg.create())));
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
            sender.sender.sendMessage(part.create());
        } else if (previous != null) {
            previous.part.append(part.create());
            previous.send();
        }
    }
}

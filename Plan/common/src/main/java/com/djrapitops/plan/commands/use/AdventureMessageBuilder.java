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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.apache.commons.text.TextStringBuilder;

import java.util.Collection;

/**
 * Shared Adventure {@link MessageBuilder} shared by Velocity and Sponge.
 */
public class AdventureMessageBuilder implements MessageBuilder {

    private final CMDSender sender;
    private final Audience audience;
    private final TextComponent.Builder builder;

    // Store reference to previous component to properly add hover & click events
    private Component previousComponent;

    AdventureMessageBuilder(CMDSender sender, Audience audience) {
        this.sender = sender;
        this.audience = audience;
        builder = Component.text();
    }

    @Override
    public MessageBuilder addPart(String content) {
        if (previousComponent != null) {
            builder.append(previousComponent);
        }
        previousComponent = Component.text(content);
        return this;
    }

    @Override
    public MessageBuilder newLine() {
        if (previousComponent != null) {
            builder.append(previousComponent);
        }
        previousComponent = Component.text("\n");
        return this;
    }

    @Override
    public MessageBuilder link(String url) {
        previousComponent = previousComponent.clickEvent(ClickEvent.openUrl(url));
        return this;
    }

    @Override
    public MessageBuilder command(String command) {
        previousComponent = previousComponent.clickEvent(ClickEvent.runCommand(command));
        return this;
    }

    @Override
    public MessageBuilder hover(String s) {
        previousComponent = previousComponent.hoverEvent(HoverEvent.showText(Component.text(s)));
        return this;
    }

    @Override
    public MessageBuilder hover(String... strings) {
        TextComponent.Builder hoverText = Component.text();
        for (String string : strings) {
            hoverText.append(Component.text(string));
        }
        previousComponent = previousComponent.hoverEvent(HoverEvent.showText(hoverText.build()));
        return this;
    }

    @Override
    public MessageBuilder hover(Collection<String> lines) {
        TextComponent.Builder hoverText = Component.text();
        hoverText.append(Component.text(new TextStringBuilder().appendWithSeparators(lines, "\n").get()));
        previousComponent = previousComponent.hoverEvent(HoverEvent.showText(hoverText.build()));
        return this;
    }

    @Override
    public MessageBuilder indent(int amount) {
        for (int i = 0; i < amount; i++) {
            if (previousComponent != null) {
                builder.append(previousComponent);
            }
            previousComponent = Component.text(" ");
        }
        return this;
    }

    @Override
    public MessageBuilder tabular(CharSequence charSequence) {
        return addPart(sender.getFormatter().table(charSequence.toString(), ":"));
    }

    @Override
    public void send() {
        if (previousComponent != null) {
            builder.append(previousComponent);
        }
        audience.sendMessage(builder.build());
    }
}

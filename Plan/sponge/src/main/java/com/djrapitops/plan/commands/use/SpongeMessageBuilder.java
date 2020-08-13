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

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public class SpongeMessageBuilder implements MessageBuilder {

    private final SpongeCMDSender sender;
    private final Text.Builder builder;

    public SpongeMessageBuilder(SpongeCMDSender sender) {
        this.sender = sender;
        builder = Text.builder();
    }

    @Override
    public MessageBuilder addPart(String s) {
        builder.append(Text.of(s));
        return this;
    }

    @Override
    public MessageBuilder newLine() {
        builder.append(Text.of('\n'));
        return this;
    }

    @Override
    public MessageBuilder link(String url) {
        try {
            builder.onClick(TextActions.openUrl(new URL(url)));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("'" + url + "' is not a valid URL");
        }
        return this;
    }

    @Override
    public MessageBuilder command(String command) {
        builder.onClick(TextActions.runCommand(command.charAt(0) == '/' ? command.substring(1) : command));
        return this;
    }

    @Override
    public MessageBuilder hover(String message) {
        builder.onHover(TextActions.showText(Text.of(message)));
        return this;
    }

    @Override
    public MessageBuilder hover(String... strings) {
        builder.onHover(TextActions.showText(Text.of((Object[]) strings)));
        return this;
    }

    @Override
    public MessageBuilder hover(Collection<String> collection) {
        builder.onHover(TextActions.showText(Text.of(collection.toArray())));
        return this;
    }

    @Override
    public MessageBuilder indent(int amount) {
        for (int i = 0; i < amount; i++) {
            builder.append(Text.of(' '));
        }
        return this;
    }

    @Override
    public MessageBuilder tabular(CharSequence charSequence) {
        sender.getFormatter().table(charSequence.toString(), ":");
        return this;
    }

    @Override
    public void send() {
        sender.source.sendMessage(builder.build());
    }
}

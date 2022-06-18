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

import java.util.Collection;

public class NukkitMessageBuilder implements MessageBuilder {

    private final NukkitCMDSender sender;
    private final StringBuilder builder;

    public NukkitMessageBuilder(NukkitCMDSender sender) {
        this.sender = sender;
        builder = new StringBuilder();
    }

    @Override
    public MessageBuilder addPart(String part) {
        builder.append(part);
        return this;
    }

    @Override
    public MessageBuilder newLine() {
        builder.append("\n");
        return this;
    }

    @Override
    public MessageBuilder link(String link) {
        return addPart(link);
    }

    @Override
    public MessageBuilder command(String s) {
        return this;
    }

    @Override
    public MessageBuilder hover(String s) {
        return this;
    }

    @Override
    public MessageBuilder hover(String... strings) {
        return this;
    }

    @Override
    public MessageBuilder hover(Collection<String> collection) {
        return this;
    }

    @Override
    public MessageBuilder indent(int amount) {
        builder.append(" ".repeat(Math.max(0, amount)));
        return this;
    }

    @Override
    public MessageBuilder tabular(CharSequence charSequence) {
        addPart(sender.getFormatter().table(charSequence.toString(), ":"));
        return this;
    }

    @Override
    public void send() {
        sender.sender.sendMessage(builder.toString());
    }
}

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
import java.util.function.Consumer;

public class ConsoleMessageBuilder implements MessageBuilder {

    private final StringBuilder stringBuilder;
    private final Consumer<String> messageBus;

    public ConsoleMessageBuilder(Consumer<String> messageBus) {
        this.messageBus = messageBus;
        this.stringBuilder = new StringBuilder();
    }

    @Override
    public MessageBuilder addPart(String msg) {
        stringBuilder.append(msg);
        return this;
    }

    @Override
    public MessageBuilder newLine() {
        return addPart("\n");
    }

    @Override
    public MessageBuilder link(String address) {
        return addPart(address);
    }

    @Override
    public MessageBuilder command(String command) {
        return this;
    }

    @Override
    public MessageBuilder hover(String text) {
        return this;
    }

    @Override
    public MessageBuilder hover(String... text) {
        return this;
    }

    @Override
    public MessageBuilder hover(Collection<String> text) {
        return this;
    }

    @Override
    public MessageBuilder indent(int spaces) {
        for (int i = 0; i < spaces; i++) {
            stringBuilder.append(' ');
        }
        return this;
    }

    @Override
    public MessageBuilder tabular(CharSequence columnSeparator) {
        return this;
    }

    @Override
    public void send() {
        messageBus.accept(stringBuilder.toString());
    }
}

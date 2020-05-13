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
import java.util.function.UnaryOperator;

public interface MessageBuilder {

    MessageBuilder addPart(String msg);

    default MessageBuilder addEach(Iterable<String> iterable) {
        for (String part : iterable) {
            addPart(part);
        }
        return this;
    }

    MessageBuilder newLine();

    MessageBuilder link(String address);

    MessageBuilder command(String command);

    MessageBuilder hover(String text);

    MessageBuilder hover(String... text);

    MessageBuilder hover(Collection<String> text);

    MessageBuilder indent(int spaces);

    MessageBuilder tabular(CharSequence columnSeparator);

    default MessageBuilder apply(UnaryOperator<MessageBuilder> operation) {
        return operation.apply(this);
    }

    void send();

}

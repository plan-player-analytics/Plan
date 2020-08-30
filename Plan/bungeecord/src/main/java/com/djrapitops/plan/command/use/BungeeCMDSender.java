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

import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.commands.use.ChatFormatter;
import com.djrapitops.plan.commands.use.ConsoleChatFormatter;
import com.djrapitops.plan.commands.use.MessageBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class BungeeCMDSender implements CMDSender {

    CommandSender sender;

    public BungeeCMDSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public Optional<String> getPlayerName() {
        return Optional.empty();
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public Optional<UUID> getUUID() {
        return Optional.empty();
    }

    @Override
    public MessageBuilder buildMessage() {
        return new BungeePartBuilder(this);
    }

    @Override
    public void send(String message) {
        sender.sendMessage(new TextComponent(message));
    }

    @Override
    public ChatFormatter getFormatter() {
        return new ConsoleChatFormatter();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BungeeCMDSender that = (BungeeCMDSender) o;
        return sender.equals(that.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender);
    }
}

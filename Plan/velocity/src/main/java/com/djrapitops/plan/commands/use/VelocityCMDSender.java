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

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class VelocityCMDSender implements CMDSender {

    final CommandSource commandSource;

    public VelocityCMDSender(CommandSource commandSource) {
        this.commandSource = commandSource;
    }

    @Override
    public MessageBuilder buildMessage() {
        return new VelocityMessageBuilder(this);
    }

    @Override
    public Optional<String> getPlayerName() {
        return Optional.empty();
    }

    @Override
    public boolean hasPermission(String permission) {
        return commandSource.hasPermission(permission);
    }

    @Override
    public Optional<UUID> getUUID() {
        return Optional.empty();
    }

    @Override
    public void send(String message) {
        commandSource.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Override
    public ChatFormatter getFormatter() {
        return new ConsoleChatFormatter();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VelocityCMDSender that = (VelocityCMDSender) o;
        return commandSource.equals(that.commandSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandSource);
    }
}

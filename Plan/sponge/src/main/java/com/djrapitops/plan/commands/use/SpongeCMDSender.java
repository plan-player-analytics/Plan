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
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.service.permission.Subject;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SpongeCMDSender implements CMDSender {

    final Subject subject;
    final Audience audience;

    public <T extends Subject & Audience> SpongeCMDSender(T source) {
        this(source, source);
    }

    public SpongeCMDSender(Subject subject, Audience audience) {
        this.subject = subject;
        this.audience = audience;
    }

    @Override
    public MessageBuilder buildMessage() {
        return new AdventureMessageBuilder(this, audience);
    }

    @Override
    public Optional<String> getPlayerName() {
        return Optional.empty();
    }

    @Override
    public boolean hasPermission(String permission) {
        return subject.hasPermission(permission);
    }

    @Override
    public Optional<UUID> getUUID() {
        return Optional.empty();
    }

    @Override
    public void send(String text) {
        audience.sendMessage(Identity.nil(), LegacyComponentSerializer.legacySection().deserialize(text));
    }

    @Override
    public ChatFormatter getFormatter() {
        return new ConsoleChatFormatter();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpongeCMDSender that = (SpongeCMDSender) o;
        return subject.identifier().equals(that.subject.identifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject.identifier());
    }
}

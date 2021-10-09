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
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.permission.Subject;

import java.util.Optional;
import java.util.UUID;

public class SpongePlayerCMDSender extends SpongeCMDSender {

    public <T extends Subject & Audience> SpongePlayerCMDSender(T source) {
        this(source, source);
    }

    public SpongePlayerCMDSender(Subject subject, Audience audience) {
        super(subject, audience);
    }

    @Override
    public Optional<String> getPlayerName() {
        return subject.friendlyIdentifier();
    }

    @Override
    public Optional<UUID> getUUID() {
        if (subject instanceof ServerPlayer) {
            return Optional.of(((ServerPlayer) subject).uniqueId());
        }
        return Optional.empty();
    }

    @Override
    public ChatFormatter getFormatter() {
        return new PlayerChatFormatter();
    }

    @Override
    public boolean supportsChatEvents() {
        return true;
    }
}

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
package net.playeranalytics.plan.gathering.listeners.events.mixin;

import com.djrapitops.plan.commands.use.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.playeranalytics.plan.commands.FabricCommandManager;
import net.playeranalytics.plan.commands.use.FabricMessageBuilder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(ServerCommandSource.class)
public abstract class ServerCommandSourceMixin implements CMDSender {

    @Override
    public boolean isPlayer() {
        return getPlayer().isPresent();
    }

    @Override
    public boolean supportsChatEvents() {
        return isPlayer();
    }

    @Shadow
    public abstract void sendFeedback(Supplier<Text> supplier, boolean broadcastToOps);

    @Shadow
    @Nullable
    public abstract Entity getEntity();

    @Override
    public MessageBuilder buildMessage() {
        return new FabricMessageBuilder((ServerCommandSource) (Object) this);
    }

    @Override
    public Optional<String> getPlayerName() {
        return getPlayer().map(ServerPlayerEntity::getGameProfile).map(GameProfile::getName);
    }

    @Override
    public boolean hasPermission(String permission) {
        return FabricCommandManager.checkPermission((ServerCommandSource) (Object) this, permission);
    }

    @Override
    public Optional<UUID> getUUID() {
        return getPlayer().map(Entity::getUuid);
    }

    @Override
    public void send(String message) {
        this.sendFeedback(() -> Text.literal(message), false);
    }

    @Override
    public ChatFormatter getFormatter() {
        return isConsole() ? new ConsoleChatFormatter() : new PlayerChatFormatter();
    }

    private boolean isConsole() {
        return getEntity() == null;
    }

    private Optional<ServerPlayerEntity> getPlayer() {
        if (getEntity() instanceof ServerPlayerEntity player) {
            return Optional.of(player);
        }
        return Optional.empty();
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(isConsole()) + getUUID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ServerCommandSourceMixin other)) return false;

        return isConsole() == other.isConsole()
                && getUUID().equals(other.getUUID());
    }
}

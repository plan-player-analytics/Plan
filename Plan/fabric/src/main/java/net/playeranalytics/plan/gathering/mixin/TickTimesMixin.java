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
package net.playeranalytics.plan.gathering.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.LongStream;

/**
 * @author AuroraLS3
 */
@Mixin(MinecraftServer.class)
public class TickTimesMixin implements TickTimesAccess {

    @Shadow
    @Final
    public long[] tickTimes;

    public Optional<Double> getMsptAverage() {
        return Optional.ofNullable(tickTimes)
                .map(value -> {
                    if (value.length <= 0) return null;
                    OptionalDouble average = LongStream.of(value).filter(i -> i != 0L).average();
                    return average.isPresent() ? average.getAsDouble() : null;
                });
    }

}

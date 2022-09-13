/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.utilities.java.Reflection;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;

public class ReflectiveUnmappedLatencyFieldMethod implements PingMethod {

    private static MethodHandle pingField;
    private static MethodHandle getHandleMethod;

    private String reasonForUnavailability;

    private static void setMethods() throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException {
        MethodHandle[] methodHandles = PingMethodReflection.getMethods(
                Reflection.getCraftBukkitClass("entity.CraftPlayer"),
                getEntityPlayer(),
                "getHandle",
                "e"
        );
        getHandleMethod = methodHandles[0];
        pingField = methodHandles[1];
    }

    private static Class<?> getEntityPlayer() throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft.server.level.EntityPlayer");
        } catch (NullPointerException classLoaderError) {
            throw new ClassNotFoundException("net.minecraft.server.level.EntityPlayer");
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            setMethods();
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException | ClassNotFoundException |
                 IllegalArgumentException reflectiveEx) {
            reasonForUnavailability = reflectiveEx.toString();
            return false;
        }
        return pingField != null;
    }

    @Override
    public int getPing(Player player) {
        try {
            Object entityPlayer = getHandleMethod.invoke(player);
            return (int) pingField.invoke(entityPlayer);
        } catch (Exception ex) {
            return -1;
        } catch (Throwable throwable) {
            throw (Error) throwable;
        }
    }

    @Override
    public String getReasonForUnavailability() {
        return reasonForUnavailability;
    }
}

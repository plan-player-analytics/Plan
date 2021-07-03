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
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class ReflectiveLatencyFieldMethod implements PingMethod {

    private static MethodHandle pingField;
    private static MethodHandle getHandleMethod;

    private String reasonForUnavailability;

    @Override
    public boolean isAvailable() {
        MethodHandle localHandle = null;
        MethodHandle localPing = null;
        try {
            Class<?> craftPlayerClass = Reflection.getCraftBukkitClass("entity.CraftPlayer");
            Class<?> entityPlayer = Reflection.getMinecraftClass("EntityPlayer");

            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            Method getHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
            localHandle = lookup.unreflect(getHandleMethod);

            localPing = lookup.findGetter(entityPlayer, "latency", Integer.TYPE);
        } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException | IllegalArgumentException reflectiveEx) {
            reasonForUnavailability = reflectiveEx.toString();
            return false;
        }
        getHandleMethod = localHandle;
        pingField = localPing;
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

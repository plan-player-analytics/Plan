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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class PingMethodReflection {

    private PingMethodReflection() {
        /* Static utility class */
    }

    /**
     * Get methods for getting the ping value.
     *
     * @param craftPlayerClass Class for player
     * @param entityPlayer     Class for player entity
     * @param methodName       getHandle method
     * @param fieldName        Latency field name
     * @return [getHandle method, getPlayer method]
     * @throws NoSuchMethodException    Method doesn't exist
     * @throws IllegalAccessException   Method can't be accessed
     * @throws NoSuchFieldException     Field can't be accessed
     * @throws IllegalArgumentException Something else
     */
    public static MethodHandle[] getMethods(
            Class<?> craftPlayerClass,
            Class<?> entityPlayer,
            String methodName,
            String fieldName
    ) throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        Method handleMethod = craftPlayerClass.getDeclaredMethod(methodName);

        return new MethodHandle[]{
                lookup.unreflect(handleMethod),
                lookup.findGetter(entityPlayer, fieldName, Integer.TYPE)
        };
    }

}

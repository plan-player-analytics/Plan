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
package com.djrapitops.plan.utilities.java;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * An utility class that simplifies reflection in Bukkit plugins.
 * <p>
 * Modified Reflection utility from LagMonitor plugin.
 * <a href="https://github.com/games647/LagMonitor/blob/master/src/main/java/com/github/games647/lagmonitor/traffic/Reflection.java">original code</a>
 *
 * @author Kristian
 */
public final class Reflection {

    // Deduce the net.minecraft.server.v* package
    private static final String OBC_PREFIX = getOBCPrefix();
    private static final String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    private Reflection() {
        // Seal class
    }

    public static Optional<Object> getMinecraftServer() {
        Optional<Object> minecraftServerBeforeV1p17 = getMinecraftServerBeforeV1p17();
        if (minecraftServerBeforeV1p17.isPresent()) {return minecraftServerBeforeV1p17;}
        return getMinecraftServerAfterV1p17();
    }

    private static Optional<Object> getMinecraftServerBeforeV1p17() {
        try {
            Class<?> minecraftServerClass = Reflection.getMinecraftClass("MinecraftServer");
            Object minecraftServer = Reflection.getField(minecraftServerClass, "SERVER", minecraftServerClass).get(null);

            return Optional.ofNullable(minecraftServer);
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError e) {
            return Optional.empty();
        }
    }

    private static Optional<Object> getMinecraftServerAfterV1p17() {
        try {
            Class<?> minecraftServerClass = Reflection.getMinecraftClass("MinecraftServer");
            Class<?> craftServerClass = Reflection.getCraftBukkitClass("CraftServer");
            Object minecraftServer = Reflection.getField(craftServerClass, "console", minecraftServerClass).get(Bukkit.getServer());

            return Optional.ofNullable(minecraftServer);
        } catch (Exception | NoClassDefFoundError | NoSuchFieldError e) {
            return Optional.empty();
        }
    }

    private static String getOBCPrefix() {
        Server server = Bukkit.getServer();
        return server != null ? server.getClass().getPackage().getName() : Server.class.getPackage().getName();
    }

    /**
     * Retrieve a field accessor for a specific field type and name.
     *
     * @param target    - the target type.
     * @param name      - the name of the field, or NULL to ignore.
     * @param fieldType - a compatible field type.
     * @param <T>       Type of the field.
     * @return The field accessor.
     */
    public static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType) {
        return getField(target, name, fieldType, 0);
    }

    // Common method
    private static <T> FieldAccessor<T> getField(Class<?> target, String name, Class<T> fieldType, int index) {
        for (final Field field : target.getDeclaredFields()) {
            if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
                field.setAccessible(true);

                // A function for retrieving a specific field value
                return new FieldAccessor<>() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public T get(Object target) {
                        try {
                            return (T) field.get(target);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot access reflection.", e);
                        }
                    }

                    @Override
                    public void set(Object target, Object value) {
                        try {
                            field.set(target, value);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot access reflection.", e);
                        }
                    }

                    @Override
                    public boolean hasField(Object target) {
                        // target instanceof DeclaringClass
                        return field.getDeclaringClass().isAssignableFrom(target.getClass());
                    }
                };
            }
        }

        // Search in parent classes
        if (target.getSuperclass() != null) {
            return getField(target.getSuperclass(), name, fieldType, index);
        }

        throw new IllegalArgumentException("Cannot find field with type " + fieldType);
    }

    public static <T> FieldAccessor<T> findField(Class<?> target, Class<T> fieldType) {
        for (final Field field : target.getDeclaredFields()) {
            if (fieldType.isAssignableFrom(field.getType())) {

                return new FieldAccessor<>() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public T get(Object target) {
                        try {
                            if (!field.canAccess(target)) {
                                field.setAccessible(true);
                            }
                            return (T) field.get(target);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot access reflection.", e);
                        }
                    }

                    @Override
                    public void set(Object target, Object value) {
                        try {
                            field.set(target, value);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot access reflection.", e);
                        }
                    }

                    @Override
                    public boolean hasField(Object target) {
                        // target instanceof DeclaringClass
                        return field.getDeclaringClass().isAssignableFrom(target.getClass());
                    }
                };
            }
        }

        throw new IllegalArgumentException("Cannot find field with type " + fieldType);
    }

    /**
     * Retrieve a class in the net.minecraft.server.VERSION.* package.
     *
     * @param name - the name of the class, excluding the package.
     * @return The found class.
     * @throws IllegalArgumentException If the class doesn't exist.
     */
    public static Class<?> getMinecraftClass(String name) {
        try {
            return getCanonicalClass(NMS_PREFIX + '.' + name);
        } catch (IllegalArgumentException suppressed) {
            try {
                return getCanonicalClass("net.minecraft.server." + name);
            } catch (IllegalArgumentException e) {
                e.addSuppressed(suppressed);
                throw e;
            }
        }
    }

    /**
     * Retrieve a class in the org.bukkit.craftbukkit.VERSION.* package.
     *
     * @param name - the name of the class, excluding the package.
     * @return The found class
     * @throws IllegalArgumentException If the class doesn't exist.
     */
    public static Class<?> getCraftBukkitClass(String name) {
        return getCanonicalClass(OBC_PREFIX + '.' + name);
    }

    /**
     * Retrieve a class by its canonical name.
     *
     * @param canonicalName - the canonical name.
     * @return The class.
     */
    private static Class<?> getCanonicalClass(String canonicalName) {
        try {
            return Class.forName(canonicalName);
        } catch (ClassNotFoundException | NullPointerException e) {
            throw new IllegalArgumentException("Cannot find " + canonicalName, e);
        }
    }

    /**
     * An interface for retrieving the field content.
     *
     * @param <T> - field type.
     */
    public interface FieldAccessor<T> {

        /**
         * Retrieve the content of a field.
         *
         * @param target - the target object, or NULL for a static field.
         * @return The value of the field.
         */
        T get(Object target);

        /**
         * Set the content of a field.
         *
         * @param target - the target object, or NULL for a static field.
         * @param value  - the new value of the field.
         */
        void set(Object target, Object value);

        /**
         * Determine if the given object has this field.
         *
         * @param target - the object to test.
         * @return TRUE if it does, FALSE otherwise.
         */
        boolean hasField(Object target);
    }
}

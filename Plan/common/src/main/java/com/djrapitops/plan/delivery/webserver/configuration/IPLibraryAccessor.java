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
package com.djrapitops.plan.delivery.webserver.configuration;

import com.djrapitops.plan.exceptions.LibraryLoadingException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Access the IP library through reflection to use the proper classloader.
 *
 * @author AuroraLS3
 */
public class IPLibraryAccessor {

    private final ClassLoader libraryClassLoader;

    public IPLibraryAccessor(ClassLoader libraryClassLoader) {
        this.libraryClassLoader = libraryClassLoader;
    }

    private static Object getAddress(Method getAddress, @NotNull Object address) {
        try {
            return getAddress.invoke(address);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new LibraryLoadingException(e.toString());
        }
    }

    private static boolean isValid(Method isValid, @NotNull Object address) {
        try {
            return (boolean) isValid.invoke(address);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new LibraryLoadingException(e.toString());
        }
    }

    @NotNull
    private static Object construct(String accessAddress, Class<?> ipAddressStringClass) {
        try {
            return ipAddressStringClass.getConstructor(String.class)
                    .newInstance(accessAddress);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new LibraryLoadingException(e.toString());
        }
    }

    public boolean isAllowed(String accessAddress, List<String> allowedAddresses) {
        try {
            Class<?> ipAddressStringClass = libraryClassLoader.loadClass("inet.ipaddr.IPAddressString");
            Class<?> ipAddressClass = libraryClassLoader.loadClass("inet.ipaddr.IPAddress");
            Method getAddress = ipAddressStringClass.getMethod("getAddress");
            Method isValid = ipAddressStringClass.getMethod("isValid");

            Object accessingFrom = getAddress(getAddress, construct(accessAddress, ipAddressStringClass));
            return allowedAddresses.isEmpty() || allowedAddresses.stream()
                    .map(address -> construct(address, ipAddressStringClass))
                    .filter(address -> isValid(isValid, address))
                    .map(address -> getAddress(getAddress, address))
                    .anyMatch(range -> isWithin(range, accessingFrom, ipAddressClass));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new LibraryLoadingException(e.toString());
        }
    }

    public boolean isWithin(Object range, Object accessAddress, Class<?> ipAddressClass) {
        try {
            Method contains = ipAddressClass.getMethod("contains", ipAddressClass);
            return (boolean) contains.invoke(range, accessAddress);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new LibraryLoadingException(e.toString());
        }
    }
}

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
package com.djrapitops.plan.extension.implementation;

import com.djrapitops.plan.extension.extractor.ExtensionMethod;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In charge of tracking which extension methods got disabled due to an error.
 *
 * @author AuroraLS3
 */
public class ExtensionMethodErrorTracker {

    private static final Set<String> disabledExtensionPlayerGraphMethods = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private ExtensionMethodErrorTracker() {
        /* Static storage class. */
    }

    public static void clear() {
        disabledExtensionPlayerGraphMethods.clear();
    }

    public static boolean isDisabled(ExtensionWrapper extension, ExtensionMethod method) {
        String name = extension.getPluginName() + "." + method.getMethodName();
        return disabledExtensionPlayerGraphMethods.contains(name);
    }

    public static void errored(ExtensionWrapper extension, ExtensionMethod method) {
        String name = extension.getPluginName() + "." + method.getMethodName();
        disabledExtensionPlayerGraphMethods.add(name);
    }

}

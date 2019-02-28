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
package com.djrapitops.plan.extension;

/**
 * Interface to implement data extensions with.
 * <p>
 * The class implementing this interface should be annotated with {@link com.djrapitops.plan.extension.annotation.PluginInfo}.
 * <p>
 * If the extension is given to Plan API without the annotation it will be rejected.
 *
 * @author Rsl1122
 */
public interface DataExtension {
}

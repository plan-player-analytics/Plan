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
package com.djrapitops.plan.settings.config.paths.key;

import com.djrapitops.plan.delivery.domain.keys.Type;
import com.djrapitops.plan.settings.config.ConfigNode;

import java.util.function.Predicate;

/**
 * Represents a path to a config value.
 *
 * @author Rsl1122
 */
public abstract class Setting<T> {

    protected final String path;
    private final Predicate<T> validator;

    protected Setting(String path, Class<T> type) {
        this(path, type, Setting::nullValidator);
    }

    protected Setting(String path, Class<T> type, Predicate<T> validator) {
        // null validator has to be called before the actual validator to avoid possible null errors.
        this(path, Type.ofClass(type), ((Predicate<T>) Setting::nullValidator).and(validator));
    }

    protected Setting(String path, Type<T> type) {
        this(path, type, Setting::nullValidator);
    }

    protected Setting(String path, Type<T> type, Predicate<T> validator) {
        this.path = path;
        this.validator = validator;
    }

    public static <T> boolean nullValidator(T value) {
        return value != null;
    }

    public static boolean timeValidator(Number number) {
        return number.doubleValue() > 0;
    }

    /**
     * Used to get the String path of a the config setting.
     * <p>
     * Path separates nested levels with a dot.
     *
     * @return Example: Settings.WebServer.Enabled
     */
    public String getPath() {
        return path;
    }

    public abstract T getValueFrom(ConfigNode node);

    public boolean isValid(T value) {
        return validator.test(value);
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException(
                "Setting#toString should not be called, relies on old behavior. " +
                        "Use getValueFrom(ConfigNode) instead. " +
                        "(Called path: '" + path + "')"
        );
    }
}

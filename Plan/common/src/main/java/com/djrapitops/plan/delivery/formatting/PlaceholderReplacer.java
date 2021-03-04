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
package com.djrapitops.plan.delivery.formatting;

import org.apache.commons.text.StringSubstitutor;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Formatter for replacing ${placeholder} values inside strings.
 *
 * @author AuroraLS3
 */
public class PlaceholderReplacer extends HashMap<String, Serializable> implements Formatter<String> {

    @Override
    public String apply(String string) {
        StringSubstitutor sub = new StringSubstitutor(this);
        sub.setEnableSubstitutionInVariables(true);
        return sub.replace(string);
    }
}
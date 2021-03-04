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
package com.djrapitops.plan.settings.locale;

import com.djrapitops.plugin.utilities.Verify;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Message that can be modified by the caller.
 *
 * @author AuroraLS3
 */
public class Message {

    private final String content;

    public Message(String content) {
        this.content = content;
    }

    public String toString(Serializable... p) {
        Verify.nullCheck(p);

        Map<String, Serializable> replaceMap = new HashMap<>();

        for (int i = 0; i < p.length; i++) {
            replaceMap.put(String.valueOf(i), p[i].toString());
        }

        StringSubstitutor sub = new StringSubstitutor(replaceMap);

        return sub.replace(content);
    }

    public String[] toArray() {
        return StringUtils.split(content, '\\');
    }

    public String[] toArray(Serializable... p) {
        return toString(p).split(content, '\\');
    }

    @Override
    public String toString() {
        return content;
    }
}

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
package com.djrapitops.plan.commands.use;

import com.djrapitops.plan.utilities.dev.Untrusted;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Command argument mutation and access utility.
 *
 * @author AuroraLS3
 */
@Untrusted
public class Arguments {

    private final List<String> args;

    public Arguments(String argumentsAsString) {
        this(StringUtils.splitByWholeSeparatorPreserveAllTokens(argumentsAsString, " "));
    }

    public Arguments(String[] args) {
        this.args = Arrays.asList(args);
    }

    public Arguments(List<String> args) {
        this.args = args;
    }

    public Optional<String> get(int index) {
        return index < args.size() ? Optional.of(args.get(index)) : Optional.empty();
    }

    public Optional<Integer> getInteger(int index) {
        return get(index).map(Integer::parseInt);
    }

    public Optional<String> getAfter(String argumentIdentifier) {
        for (int i = 0; i < args.size(); i++) {
            String argument = args.get(i);
            if (argumentIdentifier.equals(argument)) {
                return get(i + 1);
            }
        }
        return Optional.empty();
    }

    public boolean contains(String argument) {
        return args.contains(argument);
    }

    public List<String> asList() {
        return args;
    }

    public Arguments removeFirst() {
        List<String> copy = new ArrayList<>(args);
        if (!copy.isEmpty()) copy.remove(0);
        return new Arguments(copy);
    }

    public String concatenate(String separator) {
        return new TextStringBuilder().appendWithSeparators(args, separator).build();
    }

    public boolean isEmpty() {
        return args.isEmpty() || args.get(0).isEmpty();
    }

    @Override
    public String toString() {
        return "Arguments{" +
                "args=" + args +
                '}';
    }
}

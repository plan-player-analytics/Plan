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
package com.djrapitops.plan.settings.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for parsing config values.
 *
 * @param <T> Type of the object being parsed.
 * @author AuroraLS3
 */
public interface ConfigValueParser<T> {

    static ConfigValueParser getParserFor(Class type) {
        if (String.class.isAssignableFrom(type)) {
            return new StringParser();
        } else if (List.class.isAssignableFrom(type)) {
            return new StringListParser();
        } else if (Boolean.class.isAssignableFrom(type)) {
            return new BooleanParser();
        } else if (Long.class.isAssignableFrom(type)) {
            return new LongParser();
        } else if (Double.class.isAssignableFrom(type)) {
            return new DoubleParser();
        } else if (Integer.class.isAssignableFrom(type)) {
            return new IntegerParser();
        }
        return toStringParser();
    }

    static ConfigValueParser toStringParser() {
        return new ConfigValueParser() {
            @Override
            public Object compose(String fromValue) {
                return fromValue;
            }

            @Override
            public String decompose(Object ofValue) {
                return ofValue.toString();
            }
        };
    }

    /**
     * Parse a String value in the config into the appropriate object.
     *
     * @param fromValue String value in the config.
     * @return Config value or null if it could not be parsed.
     */
    T compose(String fromValue);

    /**
     * Parse an object into a String value to save in the config.
     *
     * @param ofValue Value to save, not null.
     * @return String format to save in the config.
     * @throws IllegalArgumentException If null value is given.
     */
    String decompose(T ofValue);

    static IllegalArgumentException nullInvalidException() {
        return new IllegalArgumentException("Null value is not valid for saving");
    }

    class StringParser implements ConfigValueParser<String> {
        @Override
        public String compose(String fromValue) {
            boolean surroundedWithSingleQuotes = fromValue.startsWith("'") && fromValue.endsWith("'");
            boolean surroundedWithDoubleQuotes = fromValue.startsWith("\"") && fromValue.endsWith("\"");
            if (surroundedWithSingleQuotes || surroundedWithDoubleQuotes) {
                return fromValue.substring(1, fromValue.length() - 1);
            }
            return fromValue;
        }

        @Override
        public String decompose(String value) {
            if (value == null) throw nullInvalidException();

            boolean surroundedByQuotes = value.startsWith("'") || value.endsWith("'");
            boolean surroundedByDoubleQuotes = value.startsWith("\"") || value.endsWith("\"");
            boolean containsSpace = value.isEmpty() || value.contains(" ");
            boolean startsWithSpecialSymbol = value.startsWith("-") || value.startsWith("#") || value.startsWith("&");
            boolean containsDoubleQuotes = value.contains("\"");

            if (surroundedByDoubleQuotes || containsDoubleQuotes) {
                return "'" + value + "'";
            } else if (surroundedByQuotes || containsSpace || startsWithSpecialSymbol) {
                return '"' + value + '"';
            }
            return '"' + value + '"';
        }
    }

    class IntegerParser implements ConfigValueParser<Integer> {
        @Override
        public Integer compose(String fromValue) {
            if (NumberUtils.isParsable(fromValue)) {
                return NumberUtils.createInteger(fromValue);
            }
            return null;
        }

        @Override
        public String decompose(Integer ofValue) {
            if (ofValue == null) throw nullInvalidException();
            return Integer.toString(ofValue);
        }
    }

    class LongParser implements ConfigValueParser<Long> {
        @Override
        public Long compose(String fromValue) {
            try {
                return Long.parseLong(fromValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public String decompose(Long ofValue) {
            if (ofValue == null) throw nullInvalidException();
            return Long.toString(ofValue);
        }
    }

    class DoubleParser implements ConfigValueParser<Double> {
        @Override
        public Double compose(String fromValue) {
            try {
                return Double.parseDouble(fromValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public String decompose(Double ofValue) {
            if (ofValue == null) throw nullInvalidException();
            return Double.toString(ofValue);
        }
    }

    class BooleanParser implements ConfigValueParser<Boolean> {
        @Override
        public Boolean compose(String fromValue) {
            return Boolean.valueOf(fromValue);
        }

        @Override
        public String decompose(Boolean ofValue) {
            if (ofValue == null) throw nullInvalidException();
            return Boolean.toString(ofValue);
        }
    }

    class StringListParser implements ConfigValueParser<List<String>> {
        private static final StringParser STRING_PARSER = new StringParser();

        @Override
        public List<String> compose(String fromValue) {
            List<String> values = new ArrayList<>();
            for (String line : StringUtils.split(fromValue, "\n")) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                // Removes '- ' in front of the value.
                line = line.substring(2).trim();
                // Handle quotes around the string
                line = STRING_PARSER.compose(line);
                if (!line.isEmpty()) {
                    values.add(line);
                }
            }
            return values;
        }

        @Override
        public String decompose(List<String> ofValue) {
            if (ofValue == null) throw nullInvalidException();

            StringBuilder decomposedString = new StringBuilder();
            for (String value : ofValue) {
                decomposedString.append("\n- ").append(STRING_PARSER.decompose(value));
            }
            return decomposedString.toString();
        }
    }
}

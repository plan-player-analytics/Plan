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
package com.djrapitops.plan.utilities.logging;

import java.io.Serializable;
import java.util.*;

/**
 * Contains context for an error that might help debugging it.
 *
 * @author AuroraLS3
 */
public class ErrorContext implements Serializable {

    private final transient List<Object> related;
    private String whatToDo;

    private ErrorContext() {
        related = new ArrayList<>();
    }

    public static ErrorContext.Builder builder() {
        return new ErrorContext.Builder();
    }

    public Optional<String> getWhatToDo() {
        return Optional.ofNullable(whatToDo);
    }

    public Collection<String> toLines() {
        List<String> lines = new ArrayList<>();
        getWhatToDo().ifPresent(lines::add);
        for (Object o : related) {
            lines.add(Objects.toString(o));
        }
        return lines;
    }

    public void merge(ErrorContext context) {
        this.related.addAll(context.related);
        if (this.whatToDo == null && context.whatToDo != null) this.whatToDo = context.whatToDo;
    }

    public List<Object> getRelated() {
        return related;
    }

    public static class Builder {
        private final ErrorContext context;

        public Builder() {
            context = new ErrorContext();
        }

        public Builder whatToDo(String whatToDo) {
            context.whatToDo = whatToDo;
            return this;
        }

        public Builder related(Object related) {
            context.related.add(related);
            return this;
        }

        public Builder related(Object... related) {
            context.related.addAll(Arrays.asList(related));
            return this;
        }

        public ErrorContext build() {
            return context;
        }
    }

    @Override
    public String toString() {
        return "ErrorContext{" +
                "related=" + related +
                ", whatToDo='" + whatToDo + '\'' +
                '}';
    }
}

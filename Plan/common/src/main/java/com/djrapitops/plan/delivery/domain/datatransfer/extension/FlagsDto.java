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
package com.djrapitops.plan.delivery.domain.datatransfer.extension;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.djrapitops.plan.extension.statistic.Flags;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@JsonAdapter(FlagsDto.Adapter.class)
public record FlagsDto(Map<String, Boolean> valueByKey) {
    public FlagsDto(Flags flags) {
        this(flags.getValueByKey());
    }

    public static class Adapter extends TypeAdapter<FlagsDto> {

        @Override
        public void write(JsonWriter out, FlagsDto flags) throws IOException {
            if (flags == null) {
                out.nullValue();
                return;
            }

            boolean serializeNulls = out.getSerializeNulls();
            out.setSerializeNulls(true);
            try {
                out.beginObject();
                out.name("valueByKey");
                out.beginObject();
                for (Map.Entry<String, Boolean> entry : flags.valueByKey().entrySet()) {
                    out.name(entry.getKey());
                    if (entry.getValue() == null) {
                        out.nullValue();
                    } else {
                        out.value(entry.getValue());
                    }
                }
                out.endObject();
                out.endObject();
            } finally {
                out.setSerializeNulls(serializeNulls);
            }
        }

        @Override
        public FlagsDto read(JsonReader in) throws IOException {
            Map<String, Boolean> valueByKey = new HashMap<>();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if ("valueByKey".equals(name)) {
                    in.beginObject();
                    while (in.hasNext()) {
                        String flagName = in.nextName();
                        valueByKey.put(flagName, in.peek() == com.google.gson.stream.JsonToken.NULL ? readNull(in) : in.nextBoolean());
                    }
                    in.endObject();
                } else {
                    in.skipValue();
                }
            }
            in.endObject();
            return new FlagsDto(valueByKey);
        }

        private Boolean readNull(JsonReader in) throws IOException {
            in.nextNull();
            return null;
        }
    }
}
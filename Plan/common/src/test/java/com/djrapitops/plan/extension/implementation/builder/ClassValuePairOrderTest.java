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
package com.djrapitops.plan.extension.implementation.builder;

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.Conditional;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.StringProvider;
import com.djrapitops.plan.extension.builder.DataValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassValuePairOrderTest {

    @Test
    void providedConditionsComeBeforeConditions() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("condition")
            @StringProvider(text = "a")
            public String value() {
                return "";
            }
        }

        ExtDataBuilder builder = new ExtDataBuilder(new Extension());

        DataValue<Boolean> first = builder.valueBuilder("test")
                .buildBooleanProvidingCondition(false, "condition");
        DataValue<Boolean> second = builder.valueBuilder("test")
                .conditional(Extension.class.getMethod("value").getAnnotation(Conditional.class))
                .buildBoolean(false);

        builder.addValue(Boolean.class, second);
        builder.addValue(Boolean.class, first);

        List<ExtDataBuilder.ClassValuePair> expected = Arrays.asList(
                new ExtDataBuilder.ClassValuePair(Boolean.class, first),
                new ExtDataBuilder.ClassValuePair(Boolean.class, second)
        );
        List<ExtDataBuilder.ClassValuePair> result = builder.getValues();

        assertEquals(expected, result);
    }

    @Test
    void booleansComeFirst() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("condition")
            @StringProvider(text = "a")
            public String value() {
                return "";
            }
        }

        ExtDataBuilder builder = new ExtDataBuilder(new Extension());

        DataValue<Boolean> first = builder.valueBuilder("test")
                .buildBooleanProvidingCondition(false, "condition");
        DataValue<String> second = builder.valueBuilder("test")
                .conditional(Extension.class.getMethod("value").getAnnotation(Conditional.class))
                .buildString("e");

        builder.addValue(String.class, second);
        builder.addValue(String.class, second);
        builder.addValue(String.class, second);
        builder.addValue(Boolean.class, first);

        List<ExtDataBuilder.ClassValuePair> expected = Arrays.asList(
                new ExtDataBuilder.ClassValuePair(Boolean.class, first),
                new ExtDataBuilder.ClassValuePair(String.class, second),
                new ExtDataBuilder.ClassValuePair(String.class, second),
                new ExtDataBuilder.ClassValuePair(String.class, second)
        );
        List<ExtDataBuilder.ClassValuePair> result = builder.getValues();

        assertEquals(expected, result);
    }

    @Test
    void booleansWithConditionsComeFirst() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("condition")
            @StringProvider(text = "a")
            public String value() {
                return "";
            }

            @Conditional("condition2")
            @StringProvider(text = "a")
            public String value2() {
                return "";
            }
        }

        ExtDataBuilder builder = new ExtDataBuilder(new Extension());

        DataValue<Boolean> first = builder.valueBuilder("test")
                .buildBooleanProvidingCondition(false, "condition");
        DataValue<Boolean> second = builder.valueBuilder("test")
                .conditional(Extension.class.getMethod("value").getAnnotation(Conditional.class))
                .buildBooleanProvidingCondition(false, "condition2");
        DataValue<String> third = builder.valueBuilder("test")
                .conditional(Extension.class.getMethod("value2").getAnnotation(Conditional.class))
                .buildString("e");

        builder.addValue(String.class, third);
        builder.addValue(String.class, third);
        builder.addValue(String.class, third);
        builder.addValue(Boolean.class, second);
        builder.addValue(Boolean.class, first);

        List<ExtDataBuilder.ClassValuePair> expected = Arrays.asList(
                new ExtDataBuilder.ClassValuePair(Boolean.class, first),
                new ExtDataBuilder.ClassValuePair(Boolean.class, second),
                new ExtDataBuilder.ClassValuePair(String.class, third),
                new ExtDataBuilder.ClassValuePair(String.class, third),
                new ExtDataBuilder.ClassValuePair(String.class, third)
        );
        List<ExtDataBuilder.ClassValuePair> result = builder.getValues();

        assertEquals(expected, result);
    }

    @Test
    void booleansWithConditionsComeFirst2() throws NoSuchMethodException {
        @PluginInfo(name = "Extension")
        class Extension implements DataExtension {
            @Conditional("condition")
            @StringProvider(text = "a")
            public String value() {
                return "";
            }

            @Conditional("condition2")
            @StringProvider(text = "a")
            public String value2() {
                return "";
            }
        }

        ExtDataBuilder builder = new ExtDataBuilder(new Extension());

        DataValue<Boolean> first = builder.valueBuilder("test")
                .buildBooleanProvidingCondition(false, "condition");
        DataValue<Boolean> second = builder.valueBuilder("test")
                .conditional(Extension.class.getMethod("value").getAnnotation(Conditional.class))
                .buildBooleanProvidingCondition(false, "condition2");
        DataValue<String> third = builder.valueBuilder("test")
                .conditional(Extension.class.getMethod("value2").getAnnotation(Conditional.class))
                .buildString("e");

        builder.addValue(String.class, third);
        builder.addValue(String.class, third);
        builder.addValue(Boolean.class, first);
        builder.addValue(Boolean.class, second);
        builder.addValue(String.class, third);

        List<ExtDataBuilder.ClassValuePair> expected = Arrays.asList(
                new ExtDataBuilder.ClassValuePair(Boolean.class, first),
                new ExtDataBuilder.ClassValuePair(Boolean.class, second),
                new ExtDataBuilder.ClassValuePair(String.class, third),
                new ExtDataBuilder.ClassValuePair(String.class, third),
                new ExtDataBuilder.ClassValuePair(String.class, third)
        );
        List<ExtDataBuilder.ClassValuePair> result = builder.getValues();

        assertEquals(expected, result);
    }

}

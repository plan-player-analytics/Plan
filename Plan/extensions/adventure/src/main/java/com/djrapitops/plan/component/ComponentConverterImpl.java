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
package com.djrapitops.plan.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@SuppressWarnings("unused") // Accessed through Reflection
public class ComponentConverterImpl implements ComponentConverter {

    private static final char AMPERSAND = com.djrapitops.plan.component.Component.AMPERSAND;
    private static final char SECTION = com.djrapitops.plan.component.Component.SECTION;
    private final ComponentSerializer<Component, ?, String> legacyAdventureAmpersand;
    private final ComponentSerializer<Component, ?, String> legacyBungeeSection;

    public ComponentConverterImpl() {
        this.legacyAdventureAmpersand = makeAdventureLegacy(AMPERSAND);
        this.legacyBungeeSection = makeBungeeLegacy(SECTION);
    }

    private LegacyComponentSerializer makeAdventureLegacy(char character) {
        return LegacyComponentSerializer.builder()
                .character(character)
                .hexColors()
                .build();
    }

    private LegacyComponentSerializer makeBungeeLegacy(char character) {
        return LegacyComponentSerializer.builder()
                .character(character)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat() // Bungee
                .build();
    }

    @Override
    public String convert(ComponentImpl inputComponent, ComponentOperation outputOperation, char outputCharacter) {
        Component component = makeIntoComponent(inputComponent);

        return getSerializer(outputOperation, outputCharacter)
                .serialize(component);
    }

    private Component makeIntoComponent(ComponentImpl component) {
        ComponentOperation inputOperation = component.getInputOperation();
        String input = component.getInput();
        char inputCharacter = component.getInputCharacter();

        ComponentSerializer<Component, ? extends Component, String> serializer;
        if (inputOperation == ComponentOperation.AUTO_DETERMINE) {
            boolean isMM = false;
            try {
                isMM = MiniMessage.miniMessage().stripTags(input).length() != input.length();
            } catch (Exception ignored) {
                // MiniMessage may in some cases throw an exception, for example when it is given a legacy section.
            }

            if (isMM) {
                serializer = MiniMessage.miniMessage();
            } else if (input.contains(AMPERSAND + "#")) { // &#
                serializer = legacyAdventureAmpersand;
            } else if (input.contains(SECTION + "x" + SECTION)) { // §x§
                serializer = legacyBungeeSection;
            } else if (input.contains(Character.toString(SECTION))) { // §
                serializer = LegacyComponentSerializer.legacySection();
            } else {
                serializer = LegacyComponentSerializer.legacyAmpersand();
            }
        } else {
            serializer = getSerializer(inputOperation, inputCharacter);
        }

        return serializer.deserialize(input);
    }

    private ComponentSerializer<Component, ? extends Component, String> getSerializer(ComponentOperation operation, char character) {
        switch (operation) {
            case JSON:
                return GsonComponentSerializer.gson();
            case LEGACY:
                return LegacyComponentSerializer.legacy(character);
            case MINIMESSAGE:
                return MiniMessage.miniMessage();
            case ADVENTURE_LEGACY:
                if (character == AMPERSAND) {
                    return legacyAdventureAmpersand;
                }
                return makeAdventureLegacy(character);
            case BUNGEE_LEGACY:
                if (character == SECTION) {
                    return legacyBungeeSection;
                }
                return makeBungeeLegacy(character);
            case AUTO_DETERMINE:
            default:
                throw new IllegalStateException("Cannot get serializer for " + operation.name());
        }
    }

    @Override
    public String translate(String input, char inputCharacter, char outputCharacter) {
        Component component = LegacyComponentSerializer.legacy(inputCharacter).deserialize(input);
        return LegacyComponentSerializer.legacy(outputCharacter).serialize(component);
    }

}

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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A utility api class for dealing with rich and less-rich Minecraft message formats.
 *
 * @author Vankka
 * @see #getInstance()
 */
public interface ComponentService {

    /**
     * Obtain the instance of ComponentService.
     *
     * @return ComponentService implementation.
     * @throws NoClassDefFoundError  If Plan is not installed and this class can not be found or if older Plan version is installed.
     * @throws IllegalStateException If Plan is installed, but not enabled.
     */
    static ComponentService getInstance() {
        return Optional.ofNullable(ComponentService.Holder.service.get())
                .orElseThrow(() -> new IllegalStateException("ComponentService has not been initialised yet."));
    }

    /**
     * Translates ampersands into section signs for color codes. Defaults to {@link Component#AMPERSAND} to {@link Component#SECTION}.
     * Example: {@code &ctext} to {@code §ctext}.
     *
     * @param input the input color string with color codes using ampersands ({@code &})
     * @return the same input string with ampersands ({@code &}) for color codes replaced with section signs ({@code §})
     * @see #translateLegacy(String, char, char)
     */
    default String translateLegacy(String input) {
        return translateLegacy(input, Component.AMPERSAND, Component.SECTION);
    }

    /**
     * Translates ampersands into section signs for color codes. Example: {@code &ctext} to {@code §ctext}.
     *
     * @param input           the input color string with color codes using ampersands ({@code &})
     * @param inputCharacter  the input character for translation, usually {@code &}.
     * @param outputCharacter the output character for translation, usually {@code §}.
     * @return the same input string with input characters for color codes replaced with output characters
     * @see #translateLegacy(String)
     * @see Component#SECTION
     * @see Component#AMPERSAND
     */
    String translateLegacy(String input, char inputCharacter, char outputCharacter);

    /**
     * Converts the given input into a {@link Component}.
     * Converts an unknown format legacy/minimessage string into a component by attempting to guess the input format.
     *
     * @param unknown the unknown format input string
     * @return a {@link Component}
     */
    Component fromAutoDetermine(String unknown);

    /**
     * Converts the given input into a {@link Component}.
     * Input example {@code §ctext}.
     *
     * @param legacy the input legacy
     * @return a {@link Component}
     * @see #fromLegacy(String, char)
     */
    default Component fromLegacy(String legacy) {
        return fromLegacy(legacy, Component.SECTION);
    }

    /**
     * Converts the given input into a {@link Component}.
     * Input example {@code §ctext}.
     *
     * @param legacy    the input legacy
     * @param character the character to use as the color prefix, usually {@code §}.
     * @return a {@link Component}
     * @see #fromLegacy(String)
     * @see Component#SECTION
     * @see Component#AMPERSAND
     */
    Component fromLegacy(String legacy, char character);

    /**
     * Converts the given input into a {@link Component}.
     * Input example: {@code &#rrggbbtext}.
     *
     * @param adventureLegacy the input adventure legacy
     * @return a {@link Component}
     * @see #fromAdventureLegacy(String, char)
     */
    default Component fromAdventureLegacy(String adventureLegacy) {
        return fromAdventureLegacy(adventureLegacy, Component.AMPERSAND);
    }

    /**
     * Converts the given input into a {@link Component}.
     * Input example: {@code &#rrggbbtext}.
     *
     * @param adventureLegacy the input adventure legacy
     * @param character       the character to use as the color prefix, usually {@code &}.
     * @return a {@link Component}
     * @see #fromAdventureLegacy(String)
     * @see Component#SECTION
     * @see Component#AMPERSAND
     */
    Component fromAdventureLegacy(String adventureLegacy, char character);

    /**
     * Converts the given input into a {@link Component}.
     * Input example: {@code §x§r§r§g§g§b§btext}.
     *
     * @param bungeeLegacy the input bungee legacy
     * @return a {@link Component}
     */
    default Component fromBungeeLegacy(String bungeeLegacy) {
        return fromBungeeLegacy(bungeeLegacy, Component.SECTION);
    }

    /**
     * Converts the given input into a {@link Component}.
     * Input example: {@code §x§r§r§g§g§b§btext}.
     *
     * @param bungeeLegacy the input bungee legacy
     * @param character    the character to use as the color prefix, usually {@code §}.
     * @return a {@link Component}
     * @see Component#SECTION
     * @see Component#AMPERSAND
     */
    Component fromBungeeLegacy(String bungeeLegacy, char character);

    /**
     * Converts the given input into a {@link Component}.
     * Input example: {@code <red>text</red>}.
     *
     * @param miniMessage the input minimessage
     * @return a {@link Component}
     */
    Component fromMiniMessage(String miniMessage);

    /**
     * Converts the given input into a {@link Component}.
     * Input example: {@code {text:"text",color:"red"}} (standard Minecraft json).
     *
     * @param json the input json
     * @return a {@link Component}
     */
    Component fromJson(String json);

    /**
     * Singleton holder for ComponentService.
     */
    class Holder {
        static final AtomicReference<ComponentService> service = new AtomicReference<>();

        private Holder() {
            /* Static variable holder */
        }

        static void set(ComponentService service) {
            ComponentService.Holder.service.set(service);
        }
    }
}

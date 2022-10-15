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

/**
 * A Minecraft message component.
 *
 * @author Vankka
 */
public interface Component {

    char AMPERSAND = '&';
    char SECTION = '\u00A7';

    /**
     * Converts this component into a legacy string. Defaults to using {@link #SECTION}.
     *
     * @return the legacy string
     * @see ComponentService#fromLegacy(String)
     * @see #intoLegacy(char) 
     */
    default String intoLegacy() {
        return intoLegacy(SECTION);
    }
    
    /**
     * Converts this component into a legacy string.
     *
     * @param character the color prefix character to use
     * @return the legacy string
     * @see ComponentService#fromLegacy(String, char)
     * @see #intoLegacy()
     */
    String intoLegacy(char character);

    /**
     * Converts this component into an adventure legacy string. Defaults to using {@link #AMPERSAND}.
     *
     * @return the adventure legacy string
     * @see ComponentService#fromAdventureLegacy(String)
     * @see #intoAdventureLegacy(char)
     */
    default String intoAdventureLegacy() {
        return intoAdventureLegacy(AMPERSAND);
    }

    /**
     * Converts this component into an adventure legacy string.
     *
     * @param character the color prefix character to use
     * @return the adventure legacy string
     * @see ComponentService#fromAdventureLegacy(String, char)
     * @see #intoAdventureLegacy()
     */
    String intoAdventureLegacy(char character);

    /**
     * Converts this component into a bungee legacy string. Defaults to using {@link #SECTION}.
     *
     * @return the bungee legacy string
     * @see ComponentService#fromBungeeLegacy(String)
     * @see #intoAdventureLegacy()
     */
    default String intoBungeeLegacy() {
        return intoBungeeLegacy(SECTION);
    }

    /**
     * Converts this component into a bungee legacy string.
     *
     * @param character the color prefix character to use
     * @return the bungee legacy string
     * @see ComponentService#fromBungeeLegacy(String)
     * @see #intoAdventureLegacy()
     */
    String intoBungeeLegacy(char character);

    /**
     * Converts this component into a minimessage.
     *
     * @return the minimessage
     * @see ComponentService#fromMiniMessage(String)
     */
    String intoMiniMessage();

    /**
     * Converts this component into Minecraft's standard json format.
     *
     * @return the json
     * @see ComponentService#fromJson(String)
     */
    String intoJson();

}

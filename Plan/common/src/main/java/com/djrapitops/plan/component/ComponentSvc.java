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

import com.djrapitops.plan.exceptions.EnableException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation for {@link ComponentService}.
 *
 * @author Vankka
 */
@Singleton
public class ComponentSvc implements ComponentService {

    private final ComponentConverter converter;

    @Inject
    public ComponentSvc() {
        try {
            Class<?> clazz = Class.forName("com.djrapitops.plan.component.ComponentConverterImpl");
            this.converter = (ComponentConverter) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new EnableException("Could not initialize ComponentConverter", e);
        }
    }

    @Override
    public String translateLegacy(String input, char inputCharacter, char outputCharacter) {
        if (converter != null) {
            return converter.translate(input, inputCharacter, outputCharacter);
        }
        return input.replace(Component.AMPERSAND, Component.SECTION);
    }

    protected String convert(ComponentImpl component, ComponentOperation operation) {
        return convert(component, operation, Character.MIN_VALUE);
    }

    protected String convert(ComponentImpl component, ComponentOperation operation, char inputCharacter) {
        if (converter != null) {
            return converter.convert(component, operation, inputCharacter);
        }
        return component.getInput();
    }

    @Override
    public Component fromAutoDetermine(String unknown) {
        return new ComponentImpl(this, ComponentOperation.AUTO_DETERMINE, unknown);
    }

    @Override
    public Component fromLegacy(String legacy, char character) {
        return new ComponentImpl(this, ComponentOperation.LEGACY, legacy, character);
    }

    @Override
    public Component fromAdventureLegacy(String adventureLegacy, char character) {
        return new ComponentImpl(this, ComponentOperation.ADVENTURE_LEGACY, adventureLegacy, character);
    }

    @Override
    public Component fromBungeeLegacy(String bungeeLegacy, char character) {
        return new ComponentImpl(this, ComponentOperation.BUNGEE_LEGACY, bungeeLegacy, character);
    }

    @Override
    public Component fromMiniMessage(String miniMessage) {
        return new ComponentImpl(this, ComponentOperation.MINIMESSAGE, miniMessage);
    }

    @Override
    public Component fromJson(String json) {
        return new ComponentImpl(this, ComponentOperation.JSON, json);
    }

    public void register() {
        ComponentService.Holder.set(this);
    }
}

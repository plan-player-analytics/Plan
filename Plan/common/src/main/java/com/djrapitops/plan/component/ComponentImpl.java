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

public class ComponentImpl implements Component {

    private final ComponentOperation inputOperation;
    private final String input;
    private final char inputCharacter;

    public ComponentImpl(ComponentOperation inputOperation, String input) {
        this(inputOperation, input, Character.MIN_VALUE);
    }

    public ComponentImpl(ComponentOperation inputOperation, String input, char inputCharacter) {
        this.inputOperation = inputOperation;
        this.input = input;
        this.inputCharacter = inputCharacter;
    }

    public ComponentOperation getInputOperation() {
        return inputOperation;
    }

    public String getInput() {
        return input;
    }

    public char getInputCharacter() {
        return inputCharacter;
    }
}

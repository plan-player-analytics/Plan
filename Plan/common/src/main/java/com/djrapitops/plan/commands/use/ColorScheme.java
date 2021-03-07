/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 AuroraLS3
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.djrapitops.plan.commands.use;

import java.util.Arrays;
import java.util.List;

/**
 * Class that contains ChatColors for plugins.
 * <p>
 * Colors should be defined in MineCraft format, eg. "§1" "§a" "§o§3"
 *
 * @author AuroraLS3
 */
public class ColorScheme {

    private final List<String> colors;

    /**
     * Create a new ColorScheme.
     *
     * @param colors colors in MineCraft format, eg. "§1" "§a" "§o§3"
     */
    public ColorScheme(String... colors) {
        this(Arrays.asList(colors));
    }

    /**
     * Create a new ColorScheme.
     *
     * @param colors colors in MineCraft format, eg. "§1" "§a" "§o§3"
     */
    public ColorScheme(List<String> colors) {
        this.colors = colors;
    }

    /**
     * Get a color with a particular index.
     *
     * @param i Index of the color.
     * @return a color code, eg "§1" or empty string if index is out of bounds.
     */
    public String getColor(int i) {
        if (i < colors.size()) {
            return colors.get(i);
        }
        return "";
    }

    /**
     * Retrieve the first defined color.
     *
     * @return a color code, eg "§1" or empty string if index is out of bounds.
     */
    public String getMainColor() {
        return getColor(0);
    }

    /**
     * Retrieve the second defined color.
     *
     * @return a color code, eg "§1" or empty string if index is out of bounds.
     */
    public String getSecondaryColor() {
        return getColor(1);
    }

    /**
     * Retrieve the third defined color.
     *
     * @return a color code, eg "§1" or empty string if index is out of bounds.
     */
    public String getTertiaryColor() {
        return getColor(2);
    }

    /**
     * Retrieve the fourth defined color.
     *
     * @return a color code, eg "§1" or empty string if index is out of bounds.
     */
    public String getExtraColor() {
        return getColor(3);
    }
}

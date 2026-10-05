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
package com.djrapitops.plan.extension.graph;

/**
 * Represents series metadata presented to the user.
 * <p>
 * Series is a collection of numbers at the same index in {@link DataPoint}.
 * VALUES OF SAME SERIES OF DATA NEED TO BE AT SAME INDEX ALL THE TIME.
 * <ul>
 *     <li>seriesName: If null "series #n" will be shown instead - max 50 characters.</li>
 *     <li>unitLabel: If null "" or no unit will be shown - max 50 characters.</li>
 *     <li>formatType: If null no formatting will be applied.</li>
 *     <li>hexColor: If null, default color series will be used - max 9 characters, in format #000000 or #00000000 to #ffffff or #ffffffff.</li>
 * </ul>
 *
 * @author AuroraLS3
 */
public class SeriesMetadata {

    private final String seriesName;
    private final String unitLabel;
    private final GraphFormatType formatType;
    private final String hexColor;

    /**
     * Metadata for series in specific index of DataPoint
     *
     * @param seriesName name of the series (falls back to "series #n" if null)
     * @param unitLabel  Unit of measurement the number uses (can be null)
     * @param formatType Formatting to apply to the number (can be null)
     * @param hexColor   Hex color or one of {@link GraphColors}.
     */
    public SeriesMetadata(String seriesName, String unitLabel, GraphFormatType formatType, String hexColor) {
        this.seriesName = seriesName;
        this.unitLabel = unitLabel;
        this.formatType = formatType;
        this.hexColor = hexColor;
    }

    /**
     * Get a new builder for SeriesMetadata.
     *
     * @return new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getSeriesName() {
        return seriesName;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public GraphFormatType getFormatType() {
        return formatType;
    }

    public String getHexColor() {
        return hexColor;
    }

    /**
     * Builder for {@link SeriesMetadata} - get using {@link SeriesMetadata#builder()}.
     */
    public static class Builder {

        private String seriesName;
        private String unitLabel;
        private GraphFormatType formatType;
        private String hexColor;

        private Builder() {
        }

        public Builder seriesName(String seriesName) {
            this.seriesName = seriesName;
            return this;
        }

        public Builder unitLabel(String unitLabel) {
            this.unitLabel = unitLabel;
            return this;
        }

        public Builder formatType(GraphFormatType formatType) {
            this.formatType = formatType;
            return this;
        }

        public Builder hexColor(String hexColor) {
            this.hexColor = hexColor;
            return this;
        }

        public SeriesMetadata build() {
            return new SeriesMetadata(seriesName, unitLabel, formatType, hexColor);
        }
    }
}

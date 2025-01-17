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

import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.graph.DataPoint;
import com.djrapitops.plan.extension.graph.XAxisType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author AuroraLS3
 */
public class ExtensionGraphDto {

    private final String displayName;
    private final XAxisType xAxisType;
    private final int xAxisSoftMin;
    private final int xAxisSoftMax;
    private final int yAxisSoftMin;
    private final int yAxisSoftMax;
    private final int columnCount;
    private final List<String> unitNames = new ArrayList<>();
    private final List<FormatType> valueFormats = new ArrayList<>();
    private final List<String> seriesColors = new ArrayList<>();
    private final boolean supportsStacking;

    private final List<DataPoint> dataPoints = new ArrayList<>();

    public ExtensionGraphDto(
            String displayName,
            XAxisType xAxisType, int xAxisSoftMin, int xAxisSoftMax,
            int yAxisSoftMin, int yAxisSoftMax, int columnCount,
            boolean supportsStacking
    ) {
        this.displayName = displayName;
        this.xAxisType = xAxisType;
        this.xAxisSoftMin = xAxisSoftMin;
        this.xAxisSoftMax = xAxisSoftMax;
        this.yAxisSoftMin = yAxisSoftMin;
        this.yAxisSoftMax = yAxisSoftMax;
        this.columnCount = columnCount;
        this.supportsStacking = supportsStacking;
    }

    public void addPoint(DataPoint dataPoint) {
        dataPoints.add(dataPoint);
    }

    public String getDisplayName() {
        return displayName;
    }

    public XAxisType getxAxisType() {
        return xAxisType;
    }

    public int getxAxisSoftMin() {
        return xAxisSoftMin;
    }

    public int getxAxisSoftMax() {
        return xAxisSoftMax;
    }

    public int getyAxisSoftMin() {
        return yAxisSoftMin;
    }

    public int getyAxisSoftMax() {
        return yAxisSoftMax;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public List<String> getUnitNames() {
        return unitNames;
    }

    public List<FormatType> getValueFormats() {
        return valueFormats;
    }

    public List<String> getSeriesColors() {
        return seriesColors;
    }

    public boolean isSupportsStacking() {
        return supportsStacking;
    }

    public List<DataPoint> getDataPoints() {
        return dataPoints;
    }
}

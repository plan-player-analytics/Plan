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

import com.djrapitops.plan.extension.graph.GraphFormatType;
import com.djrapitops.plan.extension.graph.XAxisType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents extension graph point data and metadata for displaying a spline graph.
 *
 * @author AuroraLS3
 */
public record ExtensionGraphDto(
        int providerId,
        String graphTableName,
        String displayName,
        XAxisType xAxisType,
        Integer xAxisSoftMin,
        Integer xAxisSoftMax,
        int yAxisSoftMin,
        int yAxisSoftMax,
        int columnCount,
        List<String> seriesLabels,
        List<String> unitNames,
        List<GraphFormatType> valueFormats,
        List<String> seriesColors,
        boolean supportsStacking,
        List<Number[]> dataPoints
) {

    public ExtensionGraphDto(
            int providerId, String graphTableName, String displayName,
            XAxisType xAxisType, int xAxisSoftMin, int xAxisSoftMax,
            int yAxisSoftMin, int yAxisSoftMax, int columnCount,
            boolean supportsStacking
    ) {
        this(
                providerId,
                graphTableName,
                displayName,
                xAxisType,
                xAxisSoftMin == Integer.MIN_VALUE ? null : xAxisSoftMin,
                xAxisSoftMax == Integer.MIN_VALUE ? null : xAxisSoftMax,
                yAxisSoftMin,
                yAxisSoftMax,
                columnCount,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                supportsStacking,
                new ArrayList<>()
        );
    }
}

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
package com.djrapitops.plan.commands.use;

import com.djrapitops.plan.utilities.analysis.Maximum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public interface ChatFormatter {

    int getWidth(String part);

    default String table(String message, String separator) {
        String[] lines = StringUtils.split(message, '\n');
        List<String[]> rows = new ArrayList<>();
        Maximum.ForInteger rowWidth = new Maximum.ForInteger(0);
        for (String line : lines) {
            String[] row = line.split(separator);
            rowWidth.add(row.length);
            rows.add(row);
        }
        int columns = rowWidth.getMaxAndReset();
        int[] maxWidths = findMaxWidths(rows, columns);
        int compensates = getWidth(" ");

        StringBuilder table = new StringBuilder();
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                int width = getWidth(row[i]);
                table.append(row[i]);
                while (maxWidths[i] > width) {
                    table.append(" ");
                    width += compensates;
                }
            }
            table.append('\n');
        }
        return table.toString();
    }

    default List<String[]> tableAsParts(String message, String separator) {
        String[] lines = StringUtils.split(message, '\n');
        List<String[]> rows = new ArrayList<>();
        Maximum.ForInteger rowWidth = new Maximum.ForInteger(0);
        for (String line : lines) {
            String[] row = StringUtils.split(line, separator);
            rowWidth.add(row.length);
            rows.add(row);
        }
        int columns = rowWidth.getMaxAndReset();
        int[] maxWidths = findMaxWidths(rows, columns);
        int compensates = getWidth(" ");

        List<String[]> table = new ArrayList<>();
        for (String[] row : rows) {
            List<String> rowAsParts = new ArrayList<>();
            for (int i = 0; i < row.length; i++) {
                StringBuilder part = new StringBuilder(row[i]);
                int width = getWidth(row[i]);
                while (maxWidths[i] > width) {
                    part.append(" ");
                    width += compensates;
                }
                rowAsParts.add(part.toString());
            }
            table.add(rowAsParts.toArray(new String[0]));
        }
        return table;
    }

    private int[] findMaxWidths(List<String[]> rows, int columns) {
        int[] maxWidths = new int[columns];
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                int width = getWidth(row[i]) + 1;
                if (maxWidths[i] < width) {
                    maxWidths[i] = width;
                }
            }
        }
        return maxWidths;
    }

}

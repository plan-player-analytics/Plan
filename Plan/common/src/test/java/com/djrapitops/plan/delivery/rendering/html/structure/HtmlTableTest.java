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
package com.djrapitops.plan.delivery.rendering.html.structure;

import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Sanity tests for the functionality of refactored html table rendering code.
 */
class HtmlTableTest {

    @Test
    void coloredTableProducesSameHtmlAsOldCode() {
        // Produced by old code
        String expected = "<div class=\"scrollbar\"><table class=\"table table-striped\"><thead class=\"bg-amber\"><tr><th><i class=\" fa fa-test\"></i> Col 1</th><th><i class=\" fa fa-test\"></i> Col 2</th><th><i class=\" fa fa-test\"></i> Col 3</th></tr></thead><tbody><tr><td>1</td><td>2</td><td>three</td></tr></tbody></table></div>";

        Icon icon = Icon.called("test").build();
        String result = HtmlTable.fromExtensionTable(Table.builder()
                .columnOne("Col 1", icon)
                .columnTwo("Col 2", icon)
                .columnThree("Col 3", icon)
                .addRow("1", 2, "three")
                .build(), Color.AMBER).toHtml();
        assertEquals(expected, result);
    }

    @Test
    void coloredTableProducesSameHtmlAsOldCodeWhenEmpty() {
        // Produced by old code
        String expected = "<div class=\"scrollbar\"><table class=\"table table-striped\"><thead class=\"bg-amber\"><tr><th><i class=\" fa fa-test\"></i> Col 1</th><th><i class=\" fa fa-test\"></i> Col 2</th><th><i class=\" fa fa-test\"></i> Col 3</th></tr></thead><tbody><tr><td>No Data</td><td>-</td><td>-</td></tr></tbody></table></div>";

        Icon icon = Icon.called("test").build();
        String result = HtmlTable.fromExtensionTable(Table.builder()
                .columnOne("Col 1", icon)
                .columnTwo("Col 2", icon)
                .columnThree("Col 3", icon)
                .build(), Color.AMBER).toHtml();
        assertEquals(expected, result);
    }

    @Test
    void dynamicTableProducesSameHtmlAsOldCode() {
        // Produced by old code
        String expected = "<div class=\"table-responsive\"><table class=\"table table-bordered table-striped table-hover player-plugin-table dataTable\"><thead><tr><th><i class=\" fa fa-test\"></i> Col 1</th><th><i class=\" fa fa-test\"></i> Col 2</th><th><i class=\" fa fa-test\"></i> Col 3</th></tr></thead><tbody><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr>" +
                "<tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr>" +
                "<tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr><tr><td data-order=\"1\">1</td><td data-order=\"2\">2</td><td>three</td></tr></tbody></table></div>";

        Icon icon = Icon.called("test").build();
        Table.Factory buildingTable = Table.builder()
                .columnOne("Col 1", icon)
                .columnTwo("Col 2", icon)
                .columnThree("Col 3", icon);

        for (int i = 0; i < 60; i++) {
            buildingTable.addRow("1", 2, "three");
        }

        String result = HtmlTable.fromExtensionTable(buildingTable.build(), Color.AMBER).toHtml();
        assertEquals(expected, result);
    }

}
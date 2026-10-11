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
package utilities;

import com.djrapitops.plan.component.Component;
import com.djrapitops.plan.component.ComponentService;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ElementOrder;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.annotation.*;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.graph.*;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.statistic.Statistics;
import com.djrapitops.plan.extension.table.Table;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Utilities for testing extensions.
 *
 * @author AuroraLS3
 */
public class TestExtensions {

    @PluginInfo(name = "PlayerGraphExtension")
    public static class PlayerGraphExtension implements DataExtension {
        private Supplier<Optional<DataPoint>> getPoint = Optional::empty;
        private Supplier<List<DataPoint>> getPointHistory = List::of;
        private Supplier<List<SeriesMetadata>> getSeriesMetadata = List::of;

        public PlayerGraphExtension point(Supplier<Optional<DataPoint>> getPoint) {
            this.getPoint = getPoint;
            return this;
        }

        public PlayerGraphExtension pointHistory(Supplier<List<DataPoint>> getPointHistory) {
            this.getPointHistory = getPointHistory;
            return this;
        }

        public PlayerGraphExtension seriesMetadata(Supplier<List<SeriesMetadata>> getSeriesMetadata) {
            this.getSeriesMetadata = getSeriesMetadata;
            return this;
        }

        @GraphProvider(displayName = "playergraph", strategy = HistoryStrategy.ONLY_APPEND_MISSING)
        public PlayerGraphDataSource playergraph() {
            return new PlayerGraphDataSource() {
                @Override
                public Optional<DataPoint> getPoint(long currentTimestamp, UUID playerUUID, String playerName) {
                    return getPoint.get();
                }

                @Override
                public List<DataPoint> getPointHistory(long currentTimestamp, UUID playerUUID, String playerName) {
                    return getPointHistory.get();
                }

                @Override
                public List<SeriesMetadata> getSeriesMetadata() {
                    return getSeriesMetadata.get();
                }
            };
        }
    }

    @PluginInfo(name = "ServerGraphExtension")
    public static class ServerGraphExtension implements DataExtension {
        private Supplier<Optional<DataPoint>> getPoint = Optional::empty;
        private Supplier<List<DataPoint>> getPointHistory = List::of;
        private Supplier<List<SeriesMetadata>> getSeriesMetadata = List::of;

        public ServerGraphExtension point(Supplier<Optional<DataPoint>> getPoint) {
            this.getPoint = getPoint;
            return this;
        }

        public ServerGraphExtension pointHistory(Supplier<List<DataPoint>> getPointHistory) {
            this.getPointHistory = getPointHistory;
            return this;
        }

        public ServerGraphExtension seriesMetadata(Supplier<List<SeriesMetadata>> getSeriesMetadata) {
            this.getSeriesMetadata = getSeriesMetadata;
            return this;
        }

        @GraphProvider(displayName = "servergraph", strategy = HistoryStrategy.ONLY_APPEND_MISSING)
        public ServerGraphDataSource servergraph() {
            return new ServerGraphDataSource() {
                @Override
                public Optional<DataPoint> getPoint(long currentTimestamp) {
                    return getPoint.get();
                }

                @Override
                public List<DataPoint> getPointHistory(long currentTimestamp) {
                    return getPointHistory.get();
                }

                @Override
                public List<SeriesMetadata> getSeriesMetadata() {
                    return getSeriesMetadata.get();
                }
            };
        }
    }

    @PluginInfo(name = "StatisticsExtension")
    @TabInfo(tab = "CustomTab", elementOrder = ElementOrder.STATISTICS)
    public static class StatisticsExtension implements DataExtension {
        @StatisticsProvider(tableColor = Color.GREEN)
        public Statistics stats() {
            return Statistics.create()
                    .put("stat_one", 42L)
                    .put("group:stat_two", 100L);
        }

        @StatisticsProvider(tableColor = Color.BLUE)
        @Tab("CustomTab")
        public Statistics tabStats() {
            return Statistics.create()
                    .put("custom_stat", 999L);
        }
    }

    @PluginInfo(name = "PlayerStatisticsExtension")
    @TabInfo(tab = "CustomTab", elementOrder = ElementOrder.STATISTICS)
    public static class PlayerStatisticsExtension implements DataExtension {
        @StatisticsProvider(tableColor = Color.GREEN)
        public Statistics stats(UUID playerUUID) {
            if (TestConstants.PLAYER_ONE_UUID.equals(playerUUID)) {
                return Statistics.create()
                        .put("stat_one", 42L)
                        .put("group:stat_two", 100L);
            } else {
                return Statistics.create()
                        .put("stat_one", 10L);
            }
        }

        @StatisticsProvider(tableColor = Color.BLUE)
        @Tab("CustomTab")
        public Statistics tabStats(UUID playerUUID) {
            return Statistics.create()
                    .put("custom_stat", 999L);
        }
    }

    @PluginInfo(name = "ConditionalExtension")
    public static class ConditionalExtension implements DataExtension {

        public static boolean condition = true;

        @BooleanProvider(text = "a boolean", conditionName = "condition")
        public boolean isCondition(UUID playerUUID) {
            return condition;
        }

        @StringProvider(text = "Conditional Value")
        @Conditional("condition")
        public String conditionalValue(UUID playerUUID) {
            return "Conditional";
        }

        @StringProvider(text = "Reversed Conditional Value")
        @Conditional(value = "condition", negated = true)
        public String reversedConditionalValue(UUID playerUUID) {
            return "Reversed";
        }

        @StringProvider(text = "Unconditional")
        public String unconditional(UUID playerUUID) {
            return "unconditional";
        }

        @BooleanProvider(text = "a boolean", conditionName = "condition")
        public boolean isCondition() {
            return condition;
        }

        @StringProvider(text = "Conditional Value")
        @Conditional("condition")
        public String conditionalValue() {
            return "Conditional";
        }

        @StringProvider(text = "Reversed Conditional Value")
        @Conditional(value = "condition", negated = true)
        public String reversedConditionalValue() {
            return "Reversed";
        }

        @GroupProvider(text = "Conditional Group")
        @Conditional("condition")
        public String[] conditionalGroups(UUID playerUUID) {
            return new String[]{"Group"};
        }

        @StringProvider(text = "Unconditional")
        public String unconditional() {
            return "unconditional";
        }
    }

    @PluginInfo(name = "ConditionalExtension")
    public static class RemovingConditionalExtension implements DataExtension {

        public static boolean condition = true;

        @BooleanProvider(text = "a boolean", conditionName = "condition")
        public boolean isCondition(UUID playerUUID) {
            return condition;
        }

        @StringProvider(text = "Conditional Value")
        @Conditional("condition")
        public String conditionalValue(UUID playerUUID) {
            return "Conditional";
        }

        @BooleanProvider(text = "a boolean", conditionName = "condition")
        public boolean isCondition() {
            return condition;
        }

        @StringProvider(text = "Conditional Value")
        @Conditional("condition")
        public String conditionalValue() {
            return "Conditional";
        }
    }

    @PluginInfo(name = "ServerExtension")
    public static class ServerExtension implements DataExtension {
        @NumberProvider(text = "a number")
        public long value() {
            return 5L;
        }

        @BooleanProvider(text = "a boolean")
        public boolean boolVal() {
            return false;
        }

        @DoubleProvider(text = "a double")
        public double doubleVal() {
            return 0.5;
        }

        @PercentageProvider(text = "a percentage")
        public double percentageVal() {
            return 0.5;
        }

        @StringProvider(text = "a string")
        public String stringVal() {
            return "Something";
        }
    }

    @PluginInfo(name = "PlayerExtension")
    public static class PlayerExtension implements DataExtension {
        @NumberProvider(text = "a number", showInPlayerTable = true)
        public long value(UUID playerUUD) {
            return 5L;
        }

        @BooleanProvider(text = "a boolean", showInPlayerTable = true)
        public boolean boolVal(UUID playerUUID) {
            return false;
        }

        @DoubleProvider(text = "a double", showInPlayerTable = true)
        public double doubleVal(UUID playerUUID) {
            return 0.5;
        }

        @PercentageProvider(text = "a percentage", showInPlayerTable = true)
        public double percentageVal(UUID playerUUID) {
            return 0.5;
        }

        @StringProvider(text = "a string", showInPlayerTable = true)
        public String stringVal(UUID playerUUID) {
            return "Something";
        }

        @GroupProvider(text = "a group")
        public String[] groupVal(UUID playerUUID) {
            return new String[]{"Group"};
        }

        @ComponentProvider(text = "colored text")
        public Component componentVal(UUID playerUUID) {
            return ComponentService.getInstance().fromLegacy("&aTest", '&');
        }
    }

    @PluginInfo(name = "GraphExtension")
    public static class GraphExtension implements DataExtension {
        @GraphProvider(displayName = "test")
        public ServerGraphDataSource test() {
            return new ServerGraphDataSource() {
                @Override
                public Optional<DataPoint> getPoint(long currentTimestamp) {
                    return Optional.of(new DataPoint(System.currentTimeMillis(), 1));
                }

                @Override
                public List<SeriesMetadata> getSeriesMetadata() {
                    return List.of(new SeriesMetadata("value", "", GraphFormatType.NONE, "#222"));
                }
            };
        }

        @GraphProvider(displayName = "test")
        public PlayerGraphDataSource testPlayer() {
            return new PlayerGraphDataSource() {
                @Override
                public Optional<DataPoint> getPoint(long currentTimestamp, UUID playerUUID, String playerName) {
                    return Optional.of(new DataPoint(System.currentTimeMillis(), 1));
                }

                @Override
                public List<SeriesMetadata> getSeriesMetadata() {
                    return List.of(new SeriesMetadata("value", "", GraphFormatType.NONE, "#222"));
                }
            };
        }
    }

    @PluginInfo(name = "TableExtension")
    public static class TableExtension implements DataExtension {
        @TableProvider(tableColor = Color.AMBER)
        public Table table() {
            return createTestTable();
        }

        @TableProvider(tableColor = Color.AMBER)
        public Table playerTable(UUID playerUUID) {
            return createTestTable();
        }

        private Table createTestTable() {
            return Table.builder()
                    .columnOne("first", Icon.called("gavel").of(Color.AMBER).build())
                    .columnTwo("second", Icon.called("what").of(Color.BROWN).build()) // Colors are ignored
                    .columnThree("third", null)                  // Can handle improper icons
                    .columnFive("five", Icon.called("").build()) // Can handle null column in between and ignore the next column
                    .addRow("value", 3, 0.5, 400L)               // Can handle too many row values
                    .build();
        }
    }

    @PluginInfo(name = "ThrowingExtension")
    public static class ThrowingExtension implements DataExtension {
        @BooleanProvider(text = "a boolean")
        public boolean booleanMethod() {
            throw new IllegalArgumentException("Failed to catch");
        }

        @BooleanProvider(text = "a boolean")
        public boolean booleanPlayerMethod(UUID playerUUID) {
            throw new NotReadyException();
        }

        @StringProvider(text = "a string")
        public String stringMethod() {
            throw new NoSuchMethodError();
        }

        @NumberProvider(text = "a string")
        public long numberMethod() {
            throw new UnsupportedOperationException();
        }

        @GroupProvider(text = "group")
        public String[] groupMethod(UUID playerUUID) {
            throw new NoClassDefFoundError();
        }

        @DataBuilderProvider
        public ExtensionDataBuilder builder() {
            return newExtensionDataBuilder()
                    .addValue(String.class, () -> {
                        throw new NotReadyException();
                    });
        }

        @DataBuilderProvider
        public ExtensionDataBuilder builder2() {
            return newExtensionDataBuilder()
                    .addValue(String.class, () -> {
                        throw new NoClassDefFoundError();
                    });
        }

        @DataBuilderProvider
        public ExtensionDataBuilder builder3() {
            throw new NoSuchMethodError();
        }
    }

    @PluginInfo(
            name = "Component with click event",
            iconName = "bug",
            iconFamily = Family.SOLID,
            color = Color.RED
    )
    public static class ClickEventTestExtension implements DataExtension {
        @ComponentProvider(
                text = "Component with click event",
                description = "",
                iconName = "bug",
                iconFamily = Family.SOLID,
                iconColor = Color.RED
        )
        public Component clickEventComponent() {
            String json = "{\"text\":\"Click here\",\"color\":\"red\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"javascript:alert(document.cookie)\"}}";
            return ComponentService.getInstance().fromJson(json);
        }


        @ComponentProvider(
                text = "Component with click event",
                description = "",
                iconName = "bug",
                iconFamily = Family.SOLID,
                iconColor = Color.RED
        )
        public Component clickEventPlayerComponent(UUID playerUUID) {
            String json = "{\"text\":\"Click here\",\"color\":\"red\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"javascript:alert(document.cookie)\"}}";
            return ComponentService.getInstance().fromJson(json);
        }
    }
}

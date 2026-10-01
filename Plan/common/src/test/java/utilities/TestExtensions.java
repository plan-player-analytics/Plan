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

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.GraphProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.graph.DataPoint;
import com.djrapitops.plan.extension.graph.PlayerGraphDataSource;
import com.djrapitops.plan.extension.graph.SeriesMetadata;
import com.djrapitops.plan.extension.graph.ServerGraphDataSource;

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

        @GraphProvider(displayName = "graph")
        public PlayerGraphDataSource graph() {
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

        @GraphProvider(displayName = "graph")
        public ServerGraphDataSource graph() {
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
}

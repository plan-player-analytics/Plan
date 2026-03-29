package com.djrapitops.plan.delivery.rendering.json.datapoint.types;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.PieGraphFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.PieSlice;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Data point implementation for the Server Pie Graph.
 *
 * @author AuroraLS3
 */
@Singleton
public class ServerPie implements Datapoint<ServerPie.Content> {

    private final ServerPlaytimeForFilter serverPlaytimeForFilter;
    private final PieGraphFactory pieGraphFactory;

    @Inject
    public ServerPie(ServerPlaytimeForFilter serverPlaytimeForFilter, PieGraphFactory pieGraphFactory) {
        this.serverPlaytimeForFilter = serverPlaytimeForFilter;
        this.pieGraphFactory = pieGraphFactory;
    }

    @Override
    public Optional<Content> getValue(GenericFilter filter) {
        Map<String, Long> playtimeByServerName = serverPlaytimeForFilter.getPlaytimePerServer(filter);
        return Optional.of(new Content(pieGraphFactory.serverPreferencePie(playtimeByServerName).getSlices()));
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER_SERVER_PIE;
        }
        if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_SERVER_PIE;
        }
        return WebPermission.DATA_NETWORK_SERVER_PIE;
    }

    @Override
    public DatapointType getType() {
        return DatapointType.SERVER_PIE;
    }

    @SuppressWarnings("unused")
    public static class Content {
        private final List<PieSlice> slices;

        public Content(List<PieSlice> slices) {
            this.slices = slices;
        }

        public List<PieSlice> getSlices() {
            return slices;
        }
    }

}

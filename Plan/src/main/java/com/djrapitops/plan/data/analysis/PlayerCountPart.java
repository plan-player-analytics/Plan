package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.utilities.Verify;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Part responsible for counting players.
 * <p>
 * Total player count, op count
 * <p>
 * Placeholder values can be retrieved using the get method.
 * <p>
 * Contains following place-holders: activitytotal, ops
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class PlayerCountPart extends RawData<PlayerCountPart> {

    private final Set<UUID> uuids;
    private final Set<UUID> ops;

    public PlayerCountPart() {
        uuids = new HashSet<>();
        ops = new HashSet<>();
    }

    @Override
    public void analyse() {
        addValue("activitytotal", uuids.size());
        addValue("ops", ops.size());
    }

    public void addPlayer(UUID uuid) {
        Verify.nullCheck(uuid);
        uuids.add(uuid);
    }

    public void addPlayers(Collection<UUID> uuids) {
        Verify.nullCheck(uuids);
        this.uuids.addAll(uuids);
    }

    public void addOP(UUID uuid) {
        Verify.nullCheck(uuid);
        ops.add(uuid);
    }

    public Set<UUID> getUuids() {
        return uuids;
    }

    public int getPlayerCount() {
        return uuids.size();
    }

    public Set<UUID> getOps() {
        return ops;
    }
}

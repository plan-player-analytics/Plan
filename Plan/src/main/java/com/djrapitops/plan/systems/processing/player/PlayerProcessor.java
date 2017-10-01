/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.player;

import main.java.com.djrapitops.plan.systems.processing.Processor;

import java.util.UUID;

/**
 * Abstract Processor that takes UUID as a parameter.
 * <p>
 * Created to allow extending processors to use Generics.
 *
 * @author Rsl1122
 */
public abstract class PlayerProcessor extends Processor<UUID> {

    public PlayerProcessor(UUID uuid) {
        super(uuid);
    }

    protected UUID getUUID() {
        return object;
    }

    @Override
    public abstract void process();
}
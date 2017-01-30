
package main.java.com.djrapitops.plan.data.importing;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Rsl1122
 */
public interface Importer {
    public HashMap<UUID, Long> grabNumericData(Set<UUID> uuids);
    public boolean isEnabled();
}

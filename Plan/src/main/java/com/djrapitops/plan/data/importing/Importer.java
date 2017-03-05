
package main.java.com.djrapitops.plan.data.importing;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Rsl1122
 */
public interface Importer {

    /**
     *
     * @param uuids
     * @return
     */
    public HashMap<UUID, Long> grabNumericData(Set<UUID> uuids);

    /**
     *
     * @return
     */
    public boolean isEnabled();
}

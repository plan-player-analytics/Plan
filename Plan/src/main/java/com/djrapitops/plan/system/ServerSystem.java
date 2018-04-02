package com.djrapitops.plan.system;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.cache.CacheSystem;
import com.djrapitops.plan.system.database.DBSystem;

public interface ServerSystem {

    HookHandler getHookHandler();

    CacheSystem getCacheSystem();

    DBSystem getDatabaseSystem();
}

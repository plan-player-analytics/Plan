package com.djrapitops.plan.common.system;

import com.djrapitops.plan.common.data.plugin.HookHandler;
import com.djrapitops.plan.common.system.cache.CacheSystem;
import com.djrapitops.plan.common.system.database.DBSystem;

public interface ServerSystem {

    HookHandler getHookHandler();

    CacheSystem getCacheSystem();

    DBSystem getDatabaseSystem();
}

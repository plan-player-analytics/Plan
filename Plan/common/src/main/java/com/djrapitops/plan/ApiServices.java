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
package com.djrapitops.plan;

import com.djrapitops.plan.component.ComponentSvc;
import com.djrapitops.plan.delivery.web.ResolverSvc;
import com.djrapitops.plan.delivery.web.ResourceSvc;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.query.QuerySvc;
import com.djrapitops.plan.settings.ListenerSvc;
import com.djrapitops.plan.settings.SchedulerSvc;
import com.djrapitops.plan.settings.SettingsSvc;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Breaks up {@link PlanSystem} to be a smaller class.
 *
 * @author AuroraLS3
 */
@Singleton
public class ApiServices {

    private final ComponentSvc componentService;
    private final ResolverSvc resolverService;
    private final ResourceSvc resourceService;
    private final ExtensionSvc extensionService;
    private final QuerySvc queryService;
    private final ListenerSvc listenerService;
    private final SettingsSvc settingsService;
    private final SchedulerSvc schedulerService;

    @Inject
    public ApiServices(
            ComponentSvc componentService,
            ResolverSvc resolverService,
            ResourceSvc resourceService,
            ExtensionSvc extensionService,
            QuerySvc queryService,
            ListenerSvc listenerService,
            SettingsSvc settingsService,
            SchedulerSvc schedulerService
    ) {
        this.componentService = componentService;
        this.resolverService = resolverService;
        this.resourceService = resourceService;
        this.extensionService = extensionService;
        this.queryService = queryService;
        this.listenerService = listenerService;
        this.settingsService = settingsService;
        this.schedulerService = schedulerService;
    }

    public void register() {
        extensionService.register();
        componentService.register();
        resolverService.register();
        resourceService.register();
        listenerService.register();
        settingsService.register();
        schedulerService.register();
        queryService.register();
    }

    public void registerExtensions() {
        extensionService.registerExtensions();
    }

    public void disableExtensionDataUpdates() {
        extensionService.disableUpdates();
    }

    public ComponentSvc getComponentService() {
        return componentService;
    }

    public ResolverSvc getResolverService() {
        return resolverService;
    }

    public ResourceSvc getResourceService() {
        return resourceService;
    }

    public ExtensionSvc getExtensionService() {
        return extensionService;
    }

    public QuerySvc getQueryService() {
        return queryService;
    }

    public ListenerSvc getListenerService() {
        return listenerService;
    }

    public SettingsSvc getSettingsService() {
        return settingsService;
    }

    public SchedulerSvc getSchedulerService() {
        return schedulerService;
    }
}

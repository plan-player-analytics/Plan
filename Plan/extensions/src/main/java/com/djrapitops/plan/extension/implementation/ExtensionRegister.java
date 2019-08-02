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
package com.djrapitops.plan.extension.implementation;

import com.djrapitops.extension.*;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * In charge of registering built in {@link com.djrapitops.plan.extension.DataExtension} implementations.
 *
 * @author Rsl1122
 */
@Singleton
public class ExtensionRegister {

    private IllegalStateException registerException;
    
    @Inject
    public ExtensionRegister() {
        /* Required for dagger injection */
    }

    public void registerBuiltInExtensions() {
        // No need to catch exceptions here,
        // registerBuiltInExtensions method will not be called unless Plan has enabled properly
        ExtensionService extensionService = ExtensionService.getInstance();

        register(new AACExtensionFactory()::createExtension);
        register(new AdvancedAchievementsExtensionFactory()::createExtension);
        register(new AdvancedBanExtensionFactory()::createExtension);
        register(new ASkyBlockExtensionFactory()::createExtension);
        register(new BanManagerExtensionFactory()::createExtension);
        register(new CoreProtectExtensionFactory()::createExtension);
        register(new DiscordSRVExtensionFactory()::createExtension);
        registerEssentialsExtension(extensionService);
        register(new GriefPreventionExtensionFactory()::createExtension);
        register(new GriefPreventionSpongeExtensionFactory()::createExtension);
        register(new GriefPreventionPlusExtensionFactory()::createExtension);
        register(new McMMOExtensionFactory()::createExtension);
        registerMinigameLibExtensions(extensionService);
        register(new NucleusExtensionFactory()::createExtension);
        register(new NuVotifierExtensionFactory()::createExtension);
        register(new ProtocolSupportExtensionFactory()::createExtension);
        register(new RedProtectExtensionFactory()::createExtension);
        register(new SpongeEconomyExtensionFactory()::createExtension);
        register(new SuperbVoteExtensionFactory()::createExtension);
        register(new VaultExtensionFactory()::createExtension);

        if (registerException != null) throw registerException;
    }

    public void registerBukkitExtensions() {
        register(new ViaVersionBukkitExtensionFactory()::createExtension);
    }

    public void registerBungeeExtensions() {
        register(new ViaVersionBungeeExtensionFactory()::createExtension);
    }

    private void registerEssentialsExtension(ExtensionService extensionService) {
        EssentialsExtensionFactory essentials = new EssentialsExtensionFactory();
        essentials.createExtension()
                .flatMap(extensionService::register) // If the extension was registered this is present.
                .ifPresent(essentials::registerUpdateListeners);
    }

    private void registerMinigameLibExtensions(ExtensionService extensionService) {
        for (DataExtension minigame : new MinigameLibExtensionFactory().createExtensions()) {
            extensionService.register(minigame);
        }
    }

    private void register(Supplier<Optional<DataExtension>> extension) {
        ExtensionService extensionService = ExtensionService.getInstance();
        try {
            extension.get().ifPresent(extensionService::register);
        } catch (IllegalStateException e) {
            if (registerException == null) {
                registerException = e;
            } else {
                registerException.addSuppressed(e);
            }
        }
    }
}
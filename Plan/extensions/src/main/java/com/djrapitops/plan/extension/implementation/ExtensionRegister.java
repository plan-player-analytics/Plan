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
import com.djrapitops.plan.extension.extractor.ExtensionExtractor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * In charge of registering built in {@link com.djrapitops.plan.extension.DataExtension} implementations.
 *
 * @author Rsl1122
 */
@Singleton
public class ExtensionRegister {

    private IllegalStateException registerException;
    private Set<String> disabledExtensions;

    @Inject
    public ExtensionRegister() {
        /* Required for dagger injection */
    }

    public void registerBuiltInExtensions(Set<String> disabledExtensions) {
        this.disabledExtensions = disabledExtensions;
        // No need to catch exceptions here,
        // registerBuiltInExtensions method will not be called unless Plan has enabled properly
        ExtensionService extensionService = ExtensionService.getInstance();

        register(new AACExtensionFactory()::createExtension, AACExtensionFactory.class);
        register(new AdvancedAchievementsExtensionFactory()::createExtension, AdvancedAchievementsExtensionFactory.class);
        register(new AdvancedBanExtensionFactory()::createExtension, AdvancedBanExtensionFactory.class);
        register(new ASkyBlockExtensionFactory()::createExtension, ASkyBlockExtensionFactory.class);
        register(new BanManagerExtensionFactory()::createExtension, BanManagerExtensionFactory.class);
        register(new CoreProtectExtensionFactory()::createExtension, CoreProtectExtensionFactory.class);
        register(new DiscordSRVExtensionFactory()::createExtension, DiscordSRVExtensionFactory.class);
        registerEssentialsExtension(extensionService);
        register(new GriefPreventionExtensionFactory()::createExtension, GriefPreventionExtensionFactory.class);
        register(new GriefPreventionSpongeExtensionFactory()::createExtension, GriefPreventionSpongeExtensionFactory.class);
        register(new GriefPreventionPlusExtensionFactory()::createExtension, GriefPreventionPlusExtensionFactory.class);
        register(new McMMOExtensionFactory()::createExtension, McMMOExtensionFactory.class);
        registerMinigameLibExtensions(extensionService);
        register(new NucleusExtensionFactory()::createExtension, NucleusExtensionFactory.class);
        register(new NuVotifierExtensionFactory()::createExtension, NuVotifierExtensionFactory.class);
        register(new ProtocolSupportExtensionFactory()::createExtension, ProtocolSupportExtensionFactory.class);
        register(new RedProtectExtensionFactory()::createExtension, RedProtectExtensionFactory.class);
        register(new SpongeEconomyExtensionFactory()::createExtension, SpongeEconomyExtensionFactory.class);
        register(new SuperbVoteExtensionFactory()::createExtension, SuperbVoteExtensionFactory.class);
        register(new VaultExtensionFactory()::createExtension, VaultExtensionFactory.class);

        if (registerException != null) throw registerException;
    }

    public void registerBukkitExtensions() {
        register(new ViaVersionBukkitExtensionFactory()::createExtension, ViaVersionBukkitExtensionFactory.class);
    }

    public void registerBungeeExtensions() {
        register(new ViaVersionBungeeExtensionFactory()::createExtension, ViaVersionBungeeExtensionFactory.class);
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

    private void register(Supplier<Optional<DataExtension>> extension, Class factory) {
        ExtensionService extensionService = ExtensionService.getInstance();
        try {
            Optional<DataExtension> optional = extension.get();
            if (!optional.isPresent()) return;
            DataExtension dataExtension = optional.get();

            String extensionName = ExtensionExtractor.getPluginName(dataExtension.getClass());
            if (disabledExtensions.contains(extensionName)) return;

            extensionService.register(dataExtension);
        } catch (IllegalStateException | NoClassDefFoundError | IncompatibleClassChangeError e) {
            // Places all exceptions to one exception with plugin information so that they can be reported.
            if (registerException == null) {
                registerException = new IllegalStateException("One or more extensions failed to register:");
                registerException.setStackTrace(new StackTraceElement[0]);
            }
            IllegalStateException info = new IllegalStateException(factory.getSimpleName() + " ran into exception when creating Extension", e);
            info.setStackTrace(new StackTraceElement[0]);
            registerException.addSuppressed(info);
        }
    }
}
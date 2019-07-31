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
import java.util.function.Consumer;

/**
 * In charge of registering built in {@link com.djrapitops.plan.extension.DataExtension} implementations.
 *
 * @author Rsl1122
 */
@Singleton
public class ExtensionRegister {

    @Inject
    public ExtensionRegister() {
        /* Required for dagger injection */
    }

    public void registerBuiltInExtensions() {
        // No need to catch exceptions here,
        // registerBuiltInExtensions method will not be called unless Plan has enabled properly
        ExtensionService extensionService = ExtensionService.getInstance();

        Consumer<DataExtension> register = extensionService::register;
        new AACExtensionFactory().createExtension().ifPresent(register);
        new AdvancedAchievementsExtensionFactory().createExtension().ifPresent(register);
        new AdvancedBanExtensionFactory().createExtension().ifPresent(register);
        new ASkyBlockExtensionFactory().createExtension().ifPresent(register);
        new BanManagerExtensionFactory().createExtension().ifPresent(register);
        new CoreProtectExtensionFactory().createExtension().ifPresent(register);
        new DiscordSRVExtensionFactory().createExtension().ifPresent(register);
        registerEssentialsExtension(extensionService);
        new GriefPreventionExtensionFactory().createExtension().ifPresent(register);
        new GriefPreventionSpongeExtensionFactory().createExtension().ifPresent(register);
        new GriefPreventionPlusExtensionFactory().createExtension().ifPresent(register);
        new McMMOExtensionFactory().createExtension().ifPresent(register);
        registerMinigameLibExtensions(extensionService);
        new NucleusExtensionFactory().createExtension().ifPresent(register);
        new NuVotifierExtensionFactory().createExtension().ifPresent(register);
        new ProtocolSupportExtensionFactory().createExtension().ifPresent(register);
        new RedProtectExtensionFactory().createExtension().ifPresent(register);
        new SpongeEconomyExtensionFactory().createExtension().ifPresent(register);
        new SuperbVoteExtensionFactory().createExtension().ifPresent(register);
        new VaultExtensionFactory().createExtension().ifPresent(register);
    }

    public void registerBukkitExtensions() {
        // No need to catch exceptions here,
        // registerBuiltInExtensions method will not be called unless Plan has enabled properly
        ExtensionService extensionService = ExtensionService.getInstance();

        new ViaVersionBukkitExtensionFactory().createExtension().ifPresent(extensionService::register);
    }

    public void registerBungeeExtensions() {
        // No need to catch exceptions here,
        // registerBuiltInExtensions method will not be called unless Plan has enabled properly
        ExtensionService extensionService = ExtensionService.getInstance();

        new ViaVersionBungeeExtensionFactory().createExtension().ifPresent(extensionService::register);
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

}
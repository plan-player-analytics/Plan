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

import com.djrapitops.plan.extension.Caller;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.NotReadyException;
import net.playeranalytics.extension.aac.AACExtensionFactory;
import net.playeranalytics.extension.advancedachievements.AdvancedAchievementsExtensionFactory;
import net.playeranalytics.extension.advancedban.AdvancedBanExtensionFactory;
import net.playeranalytics.extension.askyblock.ASkyBlockExtensionFactory;
import net.playeranalytics.extension.authme.AuthMeExtensionFactory;
import net.playeranalytics.extension.banmanager.BanManagerExtensionFactory;
import net.playeranalytics.extension.bentobox.BentoBoxExtensionFactory;
import net.playeranalytics.extension.discordsrv.DiscordSRVExtensionFactory;
import net.playeranalytics.extension.dkbans.DKBansExtensionFactory;
import net.playeranalytics.extension.dkcoins.DKCoinsExtensionFactory;
import net.playeranalytics.extension.essentials.EssentialsExtensionFactory;
import net.playeranalytics.extension.cmi.CMIExtensionFactory;
import net.playeranalytics.extension.factions.FactionsExtensionFactory;
import net.playeranalytics.extension.factionsuuid.FactionsUUIDExtensionFactory;
import net.playeranalytics.extension.fastlogin.FastLoginExtensionFactory;
import net.playeranalytics.extension.floodgate.FloodgateExtensionFactory;
import net.playeranalytics.extension.griefdefender.GriefDefenderExtensionFactory;
import net.playeranalytics.extension.griefprevention.GriefPreventionExtensionFactory;
import net.playeranalytics.extension.griefprevention.sponge.GriefPreventionSpongeExtensionFactory;
import net.playeranalytics.extension.heroes.HeroesExtensionFactory;
import net.playeranalytics.extension.jobs.JobsExtensionFactory;
import net.playeranalytics.extension.kingdomsx.KingdomsXExtensionFactory;
import net.playeranalytics.extension.lands.LandsExtensionFactory;
import net.playeranalytics.extension.libertybans.LibertyBansExtensionFactory;
import net.playeranalytics.extension.litebans.LitebansExtensionFactory;
import net.playeranalytics.extension.logblock.LogBlockExtensionFactory;
import net.playeranalytics.extension.luckperms.LuckPermsExtensionFactory;
import net.playeranalytics.extension.marriagemaster.MarriageMasterExtensionFactory;
import net.playeranalytics.extension.mcmmo.McMMOExtensionFactory;
import net.playeranalytics.extension.minigameslib.MinigameLibExtensionFactory;
import net.playeranalytics.extension.mypet.MyPetExtensionFactory;
import net.playeranalytics.extension.nucleus.extension.NucleusExtensionFactory;
import net.playeranalytics.extension.nuvotifier.NuVotifierExtensionFactory;
import net.playeranalytics.extension.placeholderapi.PlaceholderAPIExtensionFactory;
import net.playeranalytics.extension.plotsquared.PlotSquaredExtensionFactory;
import net.playeranalytics.extension.protectionstones.ProtectionStonesExtensionFactory;
import net.playeranalytics.extension.protocolsupport.ProtocolSupportExtensionFactory;
import net.playeranalytics.extension.quests.QuestsExtensionFactory;
import net.playeranalytics.extension.react.ReactExtensionFactory;
import net.playeranalytics.extension.redprotect.RedProtectExtensionFactory;
import net.playeranalytics.extension.spongeeconomy.SpongeEconomyExtensionFactory;
import net.playeranalytics.extension.superbvote.SuperbVoteExtensionFactory;
import net.playeranalytics.extension.tebex.BuycraftExtensionFactory;
import net.playeranalytics.extension.towny.TownyExtensionFactory;
import net.playeranalytics.extension.vault.VaultExtensionFactory;
import net.playeranalytics.extension.viaversion.ViaVersionExtensionFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * In charge of registering built in {@link com.djrapitops.plan.extension.DataExtension} implementations.
 *
 * @author AuroraLS3
 */
@Singleton
public class ExtensionRegister {

    private final Predicate<String> isExtensionEnabledInConfig;
    private IllegalStateException registerException;
    private Set<String> disabledExtensions;
    private ExtensionService extensionService;

    @Inject
    public ExtensionRegister(@Named("isExtensionEnabled") Predicate<String> isExtensionEnabledInConfig) {
        /* Required for dagger injection */
        this.isExtensionEnabledInConfig = isExtensionEnabledInConfig;
    }

    public void registerBuiltInExtensions(Set<String> disabledExtensions) {
        this.disabledExtensions = disabledExtensions;
        extensionService = ExtensionService.getInstance();

        register(new AACExtensionFactory(), AACExtensionFactory::createExtension);
        register(new AdvancedAchievementsExtensionFactory(), AdvancedAchievementsExtensionFactory::createExtension);
        register(new AdvancedBanExtensionFactory(), AdvancedBanExtensionFactory::createExtension, AdvancedBanExtensionFactory::registerListener);
        register(new ASkyBlockExtensionFactory(), ASkyBlockExtensionFactory::createExtension);
        register(new AuthMeExtensionFactory(), AuthMeExtensionFactory::createExtension);
        register(new BanManagerExtensionFactory(), BanManagerExtensionFactory::createExtension);
        registerBentoBoxExtensions();
        register(new BuycraftExtensionFactory(), BuycraftExtensionFactory::createExtension);
        register(new DiscordSRVExtensionFactory(), DiscordSRVExtensionFactory::createExtension, DiscordSRVExtensionFactory::registerListener);
        register(new DKBansExtensionFactory(), DKBansExtensionFactory::createExtension, DKBansExtensionFactory::registerListener);
        register(new DKCoinsExtensionFactory(), DKCoinsExtensionFactory::createExtension, DKCoinsExtensionFactory::registerListener);
        register(new EssentialsExtensionFactory(), EssentialsExtensionFactory::createExtension, EssentialsExtensionFactory::registerUpdateListeners);
        register(new EssentialsExtensionFactory(), EssentialsExtensionFactory::createEcoExtension, EssentialsExtensionFactory::registerEconomyUpdateListeners);
        register(new CMIExtensionFactory(), CMIExtensionFactory::createCMIExtension, CMIExtensionFactory::registerCMIUpdateListeners);
        register(new FactionsExtensionFactory(), FactionsExtensionFactory::createExtension);
        register(new FactionsUUIDExtensionFactory(), FactionsUUIDExtensionFactory::createExtension, FactionsUUIDExtensionFactory::registerExpansion);
        register(new FastLoginExtensionFactory(), FastLoginExtensionFactory::createExtension);
        register(new FloodgateExtensionFactory(), FloodgateExtensionFactory::createExtension, FloodgateExtensionFactory::registerListener);
        register(new GriefDefenderExtensionFactory(), GriefDefenderExtensionFactory::createExtension);
        register(new GriefPreventionExtensionFactory(), GriefPreventionExtensionFactory::createExtension);
        register(new GriefPreventionSpongeExtensionFactory(), GriefPreventionSpongeExtensionFactory::createExtension);
        register(new HeroesExtensionFactory(), HeroesExtensionFactory::createExtension);
        register(new KingdomsXExtensionFactory(), KingdomsXExtensionFactory::createExtension);
        register(new JobsExtensionFactory(), JobsExtensionFactory::createExtension);
        register(new LandsExtensionFactory(), LandsExtensionFactory::createExtension);
        register(new LibertyBansExtensionFactory(), LibertyBansExtensionFactory::createExtension, LibertyBansExtensionFactory::registerListener);
        register(new LitebansExtensionFactory(), LitebansExtensionFactory::createExtension, LitebansExtensionFactory::registerEvents);
        register(new LogBlockExtensionFactory(), LogBlockExtensionFactory::createExtension);
        register(new LuckPermsExtensionFactory(), LuckPermsExtensionFactory::createExtension, LuckPermsExtensionFactory::registerListeners);
        register(new MarriageMasterExtensionFactory(), MarriageMasterExtensionFactory::createExtension);
        register(new McMMOExtensionFactory(), McMMOExtensionFactory::createExtension, McMMOExtensionFactory::registerExpansion);
        registerMany(new MinigameLibExtensionFactory(), MinigameLibExtensionFactory::createExtensions);
        register(new MyPetExtensionFactory(), MyPetExtensionFactory::createExtension);
        register(new NucleusExtensionFactory(), NucleusExtensionFactory::createExtension);
        register(new NuVotifierExtensionFactory(), NuVotifierExtensionFactory::createExtension);
        register(new PlaceholderAPIExtensionFactory(), PlaceholderAPIExtensionFactory::createExtension);
        register(new PlotSquaredExtensionFactory(), PlotSquaredExtensionFactory::createExtension);
        register(new ProtectionStonesExtensionFactory(), ProtectionStonesExtensionFactory::createExtension);
        register(new ProtocolSupportExtensionFactory(), ProtocolSupportExtensionFactory::createExtension);
        register(new QuestsExtensionFactory(), QuestsExtensionFactory::createExtension, QuestsExtensionFactory::registerListener);
        register(new ReactExtensionFactory(), ReactExtensionFactory::createExtension);
        register(new RedProtectExtensionFactory(), RedProtectExtensionFactory::createExtension);
        register(new SpongeEconomyExtensionFactory(), SpongeEconomyExtensionFactory::createExtension);
        register(new SuperbVoteExtensionFactory(), SuperbVoteExtensionFactory::createExtension);
        register(new TownyExtensionFactory(), TownyExtensionFactory::createExtension);
        registerMany(new VaultExtensionFactory(), VaultExtensionFactory::createExtensions);
        register(new ViaVersionExtensionFactory(), ViaVersionExtensionFactory::createExtension, ViaVersionExtensionFactory::registerListener);

        if (registerException != null) throw registerException;
    }

    private void registerBentoBoxExtensions() {
        BentoBoxExtensionFactory factory = new BentoBoxExtensionFactory();
        if (factory.isAvailable()) {
            for (DataExtension minigame : factory.createExtensions()) {
                register(minigame);
            }
        }
    }

    private void suppressException(Class<?> factory, Throwable e) {
        String factoryName = factory.getSimpleName();
        String extensionName = factoryName.replace("ExtensionFactory", "");

        if (!isExtensionEnabledInConfig.test(extensionName)) {
            return;
        }

        // Places all exceptions to one exception with plugin information so that they can be reported.
        if (registerException == null) {
            registerException = new IllegalStateException("One or more extensions failed to register:");
            registerException.setStackTrace(new StackTraceElement[0]);
        }
        IllegalStateException info = new IllegalStateException(factoryName + " failed to create Extension", e);
        removeUselessStackTraces(info);
        registerException.addSuppressed(info);
    }

    private void removeUselessStackTraces(Throwable t) {
        if (t == null) return;
        if (t instanceof ReflectiveOperationException
                || t instanceof NoClassDefFoundError
                || t instanceof NoSuchFieldError
                || t instanceof NoSuchMethodError
                || t instanceof IllegalStateException
        ) {
            t.setStackTrace(new StackTraceElement[0]);
        }
        removeUselessStackTraces(t.getCause());
    }

    private <T> void register(
            T factory,
            Function<T, Optional<DataExtension>> createExtension
    ) {
        try {
            // Creates the extension with factory and registers it
            createExtension.apply(factory).ifPresent(this::register);
        } catch (NotReadyException | UnsupportedOperationException ignore) {
            // This exception signals that the extension can not be registered right now (Intended fail).
        } catch (Exception | ExceptionInInitializerError | NoClassDefFoundError | IncompatibleClassChangeError e) {
            // Places all exceptions to one exception with plugin information so that they can be reported.
            suppressException(factory.getClass(), e);
        }
    }

    private <T> void registerMany(
            T factory,
            Function<T, Collection<DataExtension>> createExtension
    ) {
        try {
            // Creates the extension with factory and registers it
            createExtension.apply(factory).forEach(this::register);
        } catch (NotReadyException | UnsupportedOperationException ignore) {
            // This exception signals that the extension can not be registered right now (Intended fail).
        } catch (Exception | ExceptionInInitializerError | NoClassDefFoundError | IncompatibleClassChangeError e) {
            // Places all exceptions to one exception with plugin information so that they can be reported.
            suppressException(factory.getClass(), e);
        }
    }

    private <T> void register(
            T factory,
            Function<T, Optional<DataExtension>> createExtension,
            BiConsumer<T, Caller> registerListener
    ) {
        try {
            // Creates the extension with factory and registers it, then registers listener
            createExtension.apply(factory)
                    .flatMap(this::register)
                    .ifPresent(caller -> registerListener.accept(factory, caller));
        } catch (NotReadyException | UnsupportedOperationException ignore) {
            // This exception signals that the extension can not be registered right now (Intended fail).
        } catch (Exception | ExceptionInInitializerError | NoClassDefFoundError | IncompatibleClassChangeError e) {
            // Places all exceptions to one exception with plugin information so that they can be reported.
            suppressException(factory.getClass(), e);
        }
    }

    private Optional<Caller> register(DataExtension dataExtension) {
        String extensionName = dataExtension.getPluginName();
        if (disabledExtensions.contains(extensionName)) return Optional.empty();

        return extensionService.register(dataExtension);
    }
}

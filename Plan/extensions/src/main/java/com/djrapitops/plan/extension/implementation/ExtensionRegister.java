package com.djrapitops.plan.extension.implementation;

import com.djrapitops.extension.AdvancedAchievementsExtensionFactory;
import com.djrapitops.plan.extension.ExtensionService;

import javax.inject.Inject;
import javax.inject.Singleton;

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
        // this method will not be called unless Plan has enabled properly
        ExtensionService extensionService = ExtensionService.getInstance();

        new AdvancedAchievementsExtensionFactory().createExtension().ifPresent(extensionService::register);
    }

}
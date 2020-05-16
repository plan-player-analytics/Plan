package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.settings.locale.Locale;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Singleton
public class Confirmation {

    private final Cache<CMDSender, Consumer<Boolean>> awaiting;
    private final Locale locale;

    @Inject
    public Confirmation(
            Locale locale
    ) {
        this.locale = locale;
        awaiting = Caffeine.newBuilder()
                .expireAfterWrite(90, TimeUnit.SECONDS)
                .build();
    }

    public void confirm(CMDSender sender, Consumer<Boolean> confirmation) {
        awaiting.put(sender, confirmation);
    }

    public void onConfirm(CMDSender sender) {
        Consumer<Boolean> found = awaiting.getIfPresent(sender);
        if (found == null) throw new IllegalArgumentException("Confirmation expired, use the command again" /* TODO */);
        found.accept(true);
        awaiting.invalidate(sender);
    }

    public void onCancel(CMDSender sender) {
        Consumer<Boolean> found = awaiting.getIfPresent(sender);
        if (found == null) throw new IllegalArgumentException("Confirmation expired, use the command again" /* TODO */);
        found.accept(false);
        awaiting.invalidate(sender);
    }

    public void onAcceptCommand(CMDSender sender, Arguments arguments) {
        onConfirm(sender);
    }

    public void onCancelCommand(CMDSender sender, Arguments arguments) {
        onCancel(sender);
    }
}

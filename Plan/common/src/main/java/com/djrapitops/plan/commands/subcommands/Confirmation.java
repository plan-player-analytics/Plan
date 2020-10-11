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
package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
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
        if (awaiting.getIfPresent(sender) != null) onCancel(sender);
        awaiting.put(sender, confirmation);
    }

    public void onConfirm(CMDSender sender) {
        Consumer<Boolean> found = awaiting.getIfPresent(sender);
        if (found == null) throw new IllegalArgumentException(locale.getString(CommandLang.CONFIRM_EXPIRED));
        try {
            found.accept(true);
        } catch (RuntimeException e) {
            sender.send(locale.getString(CommandLang.CONFIRM_FAIL_ACCEPT, e));
            throw e;
        } finally {
            awaiting.invalidate(sender);
        }
    }

    public void onCancel(CMDSender sender) {
        Consumer<Boolean> found = awaiting.getIfPresent(sender);
        if (found == null) throw new IllegalArgumentException(locale.getString(CommandLang.CONFIRM_EXPIRED));
        try {
            found.accept(false);
        } catch (RuntimeException e) {
            sender.send(locale.getString(CommandLang.CONFIRM_FAIL_DENY, e));
            throw e;
        } finally {
            awaiting.invalidate(sender);
        }
    }

    public void onAcceptCommand(CMDSender sender, Arguments arguments) {
        onConfirm(sender);
    }

    public void onCancelCommand(CMDSender sender, Arguments arguments) {
        onCancel(sender);
    }
}

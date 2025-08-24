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

import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.MessageBuilder;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Singleton
public class Confirmation {

    private final Cache<CMDSender, Consumer<Boolean>> awaiting;

    private final String mainCommand;
    private final ColorScheme colors;
    private final Locale locale;

    @Inject
    public Confirmation(
            @Named("mainCommandName") String mainCommand,
            ColorScheme colors,
            Locale locale
    ) {
        this.mainCommand = mainCommand;
        this.colors = colors;
        this.locale = locale;
        awaiting = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    private void sendConfirmOptionMessages(CMDSender sender, String prompt) {
        MessageBuilder message = sender.buildMessage()
                .addPart(colors.getMainColor() + prompt).newLine();
        sendConfirmOptionMessages(sender, message);
    }

    private void sendConfirmOptionMessages(CMDSender sender, MessageBuilder message) {
        if (sender.supportsChatEvents()) {
            message
                    .addPart(colors.getTertiaryColor() + locale.getString(CommandLang.CONFIRM))
                    .addPart("§2§l[\u2714]").command("/" + mainCommand + " accept").hover(locale.getString(CommandLang.CONFIRM_ACCEPT))
                    .addPart(" ")
                    .addPart("§4§l[\u2718]").command("/" + mainCommand + " cancel").hover(locale.getString(CommandLang.CONFIRM_DENY))
                    .send();
        } else {
            message
                    .addPart(colors.getTertiaryColor() + locale.getString(CommandLang.CONFIRM)).addPart("§a/" + mainCommand + " accept")
                    .addPart(" ")
                    .addPart("§c/" + mainCommand + " cancel")
                    .send();
        }
    }

    public void confirm(CMDSender sender, String prompt, Consumer<Boolean> confirmation) {
        if (awaiting.getIfPresent(sender) != null) onCancel(sender);
        sendConfirmOptionMessages(sender, prompt);
        awaiting.put(sender, confirmation);
    }

    public void confirm(CMDSender sender, MessageBuilder message, Consumer<Boolean> confirmation) {
        if (awaiting.getIfPresent(sender) != null) onCancel(sender);
        sendConfirmOptionMessages(sender, message);
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

    public void onAcceptCommand(CMDSender sender) {
        onConfirm(sender);
    }

    public void onCancelCommand(CMDSender sender) {
        onCancel(sender);
    }
}

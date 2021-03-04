/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 AuroraLS3
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.djrapitops.plugin.command.nukkit;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.permission.Permission;
import cn.nukkit.permission.PermissionAttachment;
import cn.nukkit.permission.PermissionAttachmentInfo;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.command.SenderType;

import java.util.Map;

/**
 * AbstractPluiginFramework matching Nukkit implementation.
 *
 * @author AuroraLS3
 */
public class NukkitCMDSender implements Sender {

    private final CommandSender cs;

    public NukkitCMDSender(CommandSender cs) {
        this.cs = cs;
    }

    @Override
    public void sendMessage(String[] strings) {
        for (String string : strings) {
            sendMessage(string);
        }
    }

    @Override
    public void sendLink(String pretext, String linkText, String url) {
        cs.sendMessage(pretext + url);
    }

    @Override
    public SenderType getSenderType() {
        if (cs.isPlayer()) {
            return SenderType.PLAYER;
        } else {
            return SenderType.CONSOLE;
        }
    }

    @Override
    public Object getSender() {
        return cs;
    }

    @Override
    public void sendMessage(String message) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(message);
            return;
        }
        final int length = message.length();
        if (length > 60) {
            int i = 59;
            while (i < length && message.charAt(i) != ' ') {
                i++;
            }
            String shortened = message.substring(0, i);
            String lastCols = TextFormat.getLastColors(message);
            cs.sendMessage(shortened);
            String leftover = lastCols + message.substring(i);
            sendMessage(leftover);
        } else {
            if (TextFormat.clean(message).isEmpty()) {
                return;
            }
            cs.sendMessage(message);
        }
    }

    public void sendMessage(TextContainer message) {
        cs.sendMessage(message);
    }

    public Server getServer() {
        return cs.getServer();
    }

    @Override
    public String getName() {
        return cs.getName();
    }

    public boolean isPlayer() {
        return cs.isPlayer();
    }

    public boolean isPermissionSet(String name) {
        return cs.isPermissionSet(name);
    }

    public boolean isPermissionSet(Permission permission) {
        return cs.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String name) {
        return cs.hasPermission(name);
    }

    public boolean hasPermission(Permission permission) {
        return cs.hasPermission(permission);
    }

    public PermissionAttachment addAttachment(Plugin plugin) {
        return cs.addAttachment(plugin);
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name) {
        return cs.addAttachment(plugin, name);
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, Boolean value) {
        return cs.addAttachment(plugin, name, value);
    }

    public void removeAttachment(PermissionAttachment attachment) {
        cs.removeAttachment(attachment);
    }

    public void recalculatePermissions() {
        cs.recalculatePermissions();
    }

    public Map<String, PermissionAttachmentInfo> getEffectivePermissions() {
        return cs.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return cs.isOp();
    }

    public void setOp(boolean value) {
        cs.setOp(value);
    }
}
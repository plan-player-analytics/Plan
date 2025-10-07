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
package net.playeranalytics.plan.commands.use;

import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.commands.use.MessageBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.text.TextStringBuilder;

import java.net.URI;
import java.util.Collection;

public class FabricMessageBuilder implements MessageBuilder {

    private final ServerCommandSource sender;
    private final MutableText builder;
    private final FabricMessageBuilder previous;

    public FabricMessageBuilder(ServerCommandSource sender) {
        this(sender, null);
    }

    FabricMessageBuilder(ServerCommandSource sender, FabricMessageBuilder previous) {
        this.sender = sender;
        this.builder = Text.literal("");
        this.previous = previous;
    }

    @Override
    public MessageBuilder addPart(String s) {
        FabricMessageBuilder newBuilder = new FabricMessageBuilder(sender, this);
        newBuilder.builder.append(Text.of(s));
        return newBuilder;
    }

    @Override
    public MessageBuilder newLine() {
        builder.append(Text.of("\n"));
        return this;
    }

    @Override
    public MessageBuilder link(String url) {
        builder.styled(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create(url))));
        return this;
    }

    @Override
    public MessageBuilder command(String command) {
        builder.styled(style -> style.withClickEvent(new ClickEvent.RunCommand(command.charAt(0) == '/' ? command : '/' + command)));
        return this;
    }

    @Override
    public MessageBuilder hover(String message) {
        builder.styled(style -> style.withHoverEvent(new HoverEvent.ShowText(Text.literal(message))));
        return this;
    }

    @Override
    public MessageBuilder hover(String... lines) {
        builder.styled(style -> style.withHoverEvent(new HoverEvent.ShowText(Text.literal(new TextStringBuilder().appendWithSeparators(lines, "\n").toString()))));
        return this;
    }

    @Override
    public MessageBuilder hover(Collection<String> lines) {
        builder.styled(style -> style.withHoverEvent(new HoverEvent.ShowText(Text.literal(new TextStringBuilder().appendWithSeparators(lines, "\n").toString()))));
        return this;
    }

    @Override
    public MessageBuilder indent(int amount) {
        for (int i = 0; i < amount; i++) {
            builder.append(Text.of(" "));
        }
        return this;
    }

    @Override
    public MessageBuilder tabular(CharSequence charSequence) {
        addPart(((CMDSender) sender).getFormatter().table(charSequence.toString(), ":"));
        return this;
    }

    @Override
    public void send() {
        if (previous == null) {
            sender.sendFeedback(() -> builder, false);
        } else {
            previous.builder.append(builder);
            previous.send();
        }
    }
}

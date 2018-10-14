/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.discordsrv;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.core.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.core.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.core.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.apache.commons.text.TextStringBuilder;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PluginData for DiscordSRV plugin.
 *
 * @author Vankka
 */
class DiscordSRVData extends PluginData {

    private final Formatter<Long> timestampFormatter;

    DiscordSRVData(Formatter<Long> timestampFormatter) {
        super(ContainerSize.THIRD, "DiscordSRV");
        this.timestampFormatter = timestampFormatter;
        setPluginIcon(Icon.called("discord").of(Family.BRAND).build());
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        if (!DiscordSRV.isReady) {
            return inspectContainer;
        }

        String userId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(uuid);
        User user = userId != null ? DiscordUtil.getUserById(userId) : null;

        if (user == null) {
            return inspectContainer;
        }

        inspectContainer.addValue(
                getWithIcon("Username", Icon.called("user").of(Family.SOLID).of(Color.CYAN)),
                "@" + user.getName() + "#" + user.getDiscriminator()
        );
        inspectContainer.addValue(
                getWithIcon("Account creation date", Icon.called("plus").of(Family.SOLID).of(Color.BLUE)),
                timestampFormatter.apply(user.getCreationTime().toEpochSecond() * 1000L)
        );

        Member member = DiscordSRV.getPlugin().getMainGuild().getMember(user);

        if (member != null) {
            addMemberData(member, inspectContainer);
        }

        return inspectContainer;
    }

    private void addMemberData(Member member, InspectContainer inspectContainer) {
        String nickname = member.getNickname();

        inspectContainer.addValue(
                getWithIcon("Nickname", Icon.called("user-ninja").of(Family.SOLID).of(Color.ORANGE)),
                nickname != null ? nickname : "None"
        );
        inspectContainer.addValue(
                getWithIcon("Join Date", Icon.called("plus").of(Family.SOLID).of(Color.GREEN)),
                timestampFormatter.apply(member.getJoinDate().toEpochSecond() * 1000L)
        );

        List<String> roles = member.getRoles().stream().map(Role::getName).collect(Collectors.toList()); // Ordered list of role names
        if (!roles.isEmpty()) {
            inspectContainer.addValue(
                    getWithIcon("Roles", Icon.called("user-circle").of(Family.SOLID).of(Color.RED)),
                    new TextStringBuilder().appendWithSeparators(roles, ", ").build()
            );
        }
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        if (!DiscordSRV.isReady) {
            return analysisContainer;
        }

        int accountsLinked = DiscordSRV.getPlugin().getAccountLinkManager().getLinkedAccounts().size();
        int guildUsers = DiscordSRV.getPlugin().getMainGuild().getMembers().size();

        analysisContainer.addValue(
                getWithIcon("Accounts linked", Icon.called("link").of(Family.SOLID).of(Color.CYAN)),
                accountsLinked
        );
        analysisContainer.addValue(
                getWithIcon("Users in main guild", Icon.called("users").of(Family.SOLID).of(Color.GREEN)),
                guildUsers
        );
        analysisContainer.addValue(
                getWithIcon("Accounts linked / Total players", Icon.called("percentage").of(Family.SOLID).of(Color.TEAL)),
                calculatePercentage(accountsLinked, uuids.size()) + "%"
        );
        analysisContainer.addValue(
                getWithIcon("Accounts linked / Users in main guild", Icon.called("percentage").of(Family.SOLID).of(Color.LIGHT_GREEN)),
                calculatePercentage(accountsLinked, guildUsers) + "%"
        );

        return analysisContainer;
    }

    private double calculatePercentage(int input1, int input2) {
        if (input1 == 0 || input2 == 0)
            return 0D;

        return Math.round((double) input1 / input2 * 10000D) / 100D; // 2 decimals
    }
}

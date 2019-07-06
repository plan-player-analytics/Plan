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
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.data.store.mutators.PlayersOnlineResolver;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.utilities.html.graphs.Graphs;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Html that represents a server box on the network page.
 *
 * @author Rsl1122
 */
public class NetworkServerBox {

    private final Server server;
    private final int registeredPlayers;
    private final TPSMutator tpsMutator;

    private final Graphs graphs;

    public NetworkServerBox(
            Server server,
            int registeredPlayers,
            TPSMutator tpsMutator,
            Graphs graphs
    ) {
        this.server = server;
        this.registeredPlayers = registeredPlayers;
        this.tpsMutator = tpsMutator;
        this.graphs = graphs;
    }

    public String toHtml() {
        Optional<Integer> playersOnline = new PlayersOnlineResolver(tpsMutator).getOnlineOn(System.currentTimeMillis());
        int onlineCount = playersOnline.orElse(0);
        int maxCount = server.getMaxPlayers();

        String serverName = server.getName();
        String address = "../server/" + serverName;
        UUID serverUUID = server.getUuid();
        String htmlID = ThreadLocalRandom.current().nextInt(100) + serverUUID.toString().replace("-", "");

        String playersOnlineData = graphs.line().playersOnlineGraph(tpsMutator).toHighChartsSeries();

        return "<div class=\"col-xs-12 col-sm-12 col-md-6 col-lg-6\">" +
                "<div class=\"card\">" +
                "<div class=\"header\">" +
                "<div class=\"row clearfix\">" +
                "<div class=\"col-xs-12 col-sm-12\">" +
                "<h2><i class=\"col-light-green fa fa-server\"></i> " + serverName + "</h2>" +
                "</div></div></div>" +
                "<div class=\"panel panel-default\">" +
                "<div class=\"panel-heading\">" +
                "<div class=\"row\">" +
                "<div id=\"playerChart" + htmlID + "\" style=\"width: 100%; height: 300px;\"></div>" +
                "</div></div>" +
                "<div class=\"panel-body\">" +
                "<div class=\"row\">" +
                "<div class=\"col-md-8\">" +
                "<p><i class=\"fa fa-users\"></i> Registered Players " +
                "<span class=\"pull-right\">" + registeredPlayers + "</span></p>" +
                "<p><i class=\"col-blue fa fa-user\"></i> Players Online " +
                "<span class=\"pull-right\">" + onlineCount + " / " + maxCount + "</span></p>" +
                "</div>" +
                "<div class=\"col-md-4\">" +
                "<a href=\"" + address + "\"><button href=\"" + address + "\" type=\"button\" class=\"pull-right btn bg-light-green waves-effect\">" +
                "<i class=\"material-icons\">trending_up</i>" +
                "<span>ANALYSIS</span>" +
                "</button></a></div></div></div></div></div></div>" +
                "<script>" +
                "var playersOnlineSeries" + htmlID + " = {" +
                "name: 'Players Online'," +
                "data: " + playersOnlineData + "," +
                "type: 'areaspline'," +
                "color: '${playersGraphColor}'," +
                "tooltip: {" +
                "valueDecimals: 0" +
                "}};" +
                "</script>" +
                "<script>$(function () {setTimeout(" +
                "function() {" +
                "playersChartNoNav(playerChart" + htmlID + ", playersOnlineSeries" + htmlID + ");}, 1000);" +
                "})</script>";
    }
}
function displayError(element, error) {
    element.find('.d-sm-flex').after(`<div class="alert alert-danger" role="alert">Failed to load values: ` + error + '</div>')
}

function loadPlayerOverviewValues(json, error) {
    tab = $("#player-overview");
    if (error) {
        displayError(tab, error);
        return;
    }

    /* Player information card */
    data = json.info;
    element = $(tab).find("#data_player_info");

    $(element).find("#data_online").replaceWith(data.online ? `<i class="col-green fa fa-fw fa-circle"></i> Online` : '<i class="col-red fa fa-fw fa-circle"></i> Offline');
    $(element).find("#data_titles").replaceWith((data.operator ? `<i class="col-blue fab fa-fw fa-superpowers"></i> Operator` : '') + (data.banned ? `<i class="col-red fa fa-fw fa-gavel"></i> Banned` : ''));
    $(element).find("#data_kick_count").text(data.kick_count);
    $(element).find("#data_player_kills").text(data.player_kill_count);
    $(element).find("#data_mob_kills").text(data.mob_kill_count);
    $(element).find("#data_deaths").text(data.death_count);

    $(element).find("#data_playtime").text(data.playtime);
    $(element).find("#data_active_playtime").text(data.active_playtime);
    $(element).find("#data_afk_time").text(data.afk_time);
    $(element).find("#data_session_count").text(data.session_count);
    $(element).find("#data_longest_session_length").text(data.longest_session_length);
    $(element).find("#data_session_median").text(data.session_median);

    $(element).find("#data_activity_index").text(data.activity_index);
    $(element).find("#data_activity_index_group").text(data.activity_index_group);
    $(element).find("#data_favorite_server").text(data.favorite_server);
    $(element).find("#data_latest_join_address").text(data.latest_join_address);
    $(element).find("#data_average_ping").text(data.average_ping);
    $(element).find("#data_best_ping").text(data.best_ping);
    $(element).find("#data_worst_ping").text(data.worst_ping);

    $(element).find("#data_registered").text(data.registered);
    $(element).find("#data_last_seen").text(data.last_seen);

    $('#data_nicknames').replaceWith(createNicknameTableBody(json.nicknames));

    $('#data_connections').replaceWith(createConnectionsTableBody(json.connections));

    // Online activity
    data = json.online_activity;
    element = $(tab).find("#data_online_activity");

    $(element).find("#data_playtime_30d").text(data.playtime_30d);
    $(element).find("#data_playtime_7d").text(data.playtime_7d);
    $(element).find("#data_active_playtime_30d").text(data.active_playtime_30d);
    $(element).find("#data_active_playtime_7d").text(data.active_playtime_7d);
    $(element).find("#data_afk_time_30d").text(data.afk_time_30d);
    $(element).find("#data_afk_time_7d").text(data.afk_time_7d);
    $(element).find("#data_median_session_length_30d").text(data.median_session_length_30d);
    $(element).find("#data_median_session_length_7d").text(data.median_session_length_7d);
    $(element).find("#data_session_count_30d").text(data.session_count_30d);
    $(element).find("#data_session_count_7d").text(data.session_count_7d);
    $(element).find("#data_player_kills_30d").text(data.player_kill_count_30d);
    $(element).find("#data_player_kills_7d").text(data.player_kill_count_7d);
    $(element).find("#data_mob_kills_30d").text(data.mob_kill_count_30d);
    $(element).find("#data_mob_kills_7d").text(data.mob_kill_count_7d);
    $(element).find("#data_deaths_30d").text(data.death_count_30d);
    $(element).find("#data_deaths_7d").text(data.death_count_7d)
}

/* This function loads PvP & PvE tab */
function loadPvPPvEValues(json, error) {
    tab = $('#pvp-pve');
    if (error) {
        displayError(tab, error);
        return;
    }

    // as Numbers
    data = json.kill_data;
    element = $(tab).find('#data_numbers');

    $(element).find('#data_player_kills_total').text(data.player_kills_total);
    $(element).find('#data_player_kills_30d').text(data.player_kills_30d);
    $(element).find('#data_player_kills_7d').text(data.player_kills_7d);

    $(element).find('#data_player_deaths_total').text(data.player_deaths_total);
    $(element).find('#data_player_deaths_30d').text(data.player_deaths_30d);
    $(element).find('#data_player_deaths_7d').text(data.player_deaths_7d);

    $(element).find('#data_player_kdr_total').text(data.player_kdr_total);
    $(element).find('#data_player_kdr_30d').text(data.player_kdr_30d);
    $(element).find('#data_player_kdr_7d').text(data.player_kdr_7d);

    $(element).find('#data_mob_kills_total').text(data.mob_kills_total);
    $(element).find('#data_mob_kills_30d').text(data.mob_kills_30d);
    $(element).find('#data_mob_kills_7d').text(data.mob_kills_7d);

    $(element).find('#data_mob_deaths_total').text(data.mob_deaths_total);
    $(element).find('#data_mob_deaths_30d').text(data.mob_deaths_30d);
    $(element).find('#data_mob_deaths_7d').text(data.mob_deaths_7d);

    $(element).find('#data_mob_kdr_total').text(data.mob_kdr_total);
    $(element).find('#data_mob_kdr_30d').text(data.mob_kdr_30d);
    $(element).find('#data_mob_kdr_7d').text(data.mob_kdr_7d);

    $(element).find('#data_deaths_total').text(data.deaths_total);
    $(element).find('#data_deaths_30d').text(data.deaths_30d);
    $(element).find('#data_deaths_7d').text(data.deaths_7d);

    // Insights
    element = $(tab).find('#data_insights');

    $(element).find('#data_weapon_1st').text(data.weapon_1st);
    $(element).find('#data_weapon_2nd').text(data.weapon_2nd);
    $(element).find('#data_weapon_3rd').text(data.weapon_3rd);
}

function createNicknameTableBody(nicknames) {
    var table = '<tbody>';

    if (nicknames.length === 0) {
        table += `<tr><td>No Nicknames</td><td>-</td><td>-</td></tr>`
    }

    for (var i = 0; i < nicknames.length; i++) {
        var nickname = nicknames[i];
        table += '<tr><td>' + nickname.nickname + '</td>' +
            '<td>' + nickname.server + '</td>' +
            '<td>' + nickname.date + '</td></tr>'
    }

    table += '</tbody>';
    return table;
}

function createConnectionsTableBody(connections) {
    var table = '<tbody>';

    if (connections.length === 0) {
        table += `<tr><td>No Data</td><td>-</td></tr>`
    }

    for (var i = 0; i < connections.length; i++) {
        var connection = connections[i];
        table += '<tr><td>' + connection.geolocation + '</td>' +
            '<td>' + connection.date + '</td></tr>'
    }

    table += '</tbody>';
    return table;
}

// Lowercase due to locale translation: Server
function loadserverAccordion(json, error) {
    tab = $("#server-overview");
    if (error) {
        displayError(tab, error);
        return;
    }

    serverTable = tab.find("#tableSAccordion").find("tbody");

    var servers = json.servers;

    if (!servers.length) {
        serverTable.append(`<tr><td>No Data</td><td>-</td><td>-</td><td>-</td></tr>`)
    }

    var serversHtml = '';
    for (var i = 0; i < servers.length; i++) {
        var server = servers[i];
        var title = createserverAccordionTitle(i, server);
        var body = createserverAccordionBody(i, server);

        serversHtml += title + body;
    }

    serverTable.append(serversHtml);

    for (var i = 0; i < servers.length; i++) {
        $('#server_h_' + i).click(onOpenserver(i, servers));
    }
}

function onOpenserver(i, servers) {
    var opened = false;
    return function () {
        if (opened) {
            return;
        }
        setTimeout(function () {
            var server = servers[i];
            var worldSeries = {name: `World Playtime`, colorByPoint: true, data: server.world_pie_series};
            var gmSeries = server.gm_series;

            worldPie("worldpie_server_" + i, worldSeries, gmSeries);
        }, 250);
        opened = true;
    }
}

// Lowercase due to locale translation: Server
function createserverAccordionTitle(i, server) {
    return '<tr id="server_h_' + i + '" aria-controls="server_t_' + i + '" aria-expanded="false" class="clickable collapsed bg-light-green-outline" data-bs-target="#server_t_' + i + '" data-bs-toggle="collapse"><td>'
        + server.server_name +
        (server.operator ? ' <i class="fab fa-fw fa-superpowers"></i>' : '') +
        (server.banned ? ' <i class="fas fa-fw fa-gavel"></i>' : '') +
        '</td>'
        + '<td>' + server.playtime + '</td>'
        + '<td>' + server.registered + '</td>'
        + '<td>' + server.last_seen + '</td></tr>'
}

// Lowercase due to locale translation: Server
function createserverAccordionBody(i, server) {

    return `<tr class="collapse" data-bs-parent="#tableSAccordion" id="server_t_` + i + `">` +
        `<td colspan="4">` +
        `<div class="collapse row" data-bs-parent="#tableSAccordion" id="server_t_` + i + `">` +
        `<div class="col-xs-12  col-sm-12 col-md-6 col-lg-6">` +
        (server.operator ? `<p><i class="col-blue fab fa-fw fa-superpowers"></i> Operator</p>` : ``) +
        (server.banned ? `<p><i class="col-red fas fa-fw fa-gavel"></i> Banned</p>` : ``) +
        (server.operator || server.banned ? `<br>` : ``) +
        `<p><i class="col-teal far fa-fw fa-calendar-check"></i> Sessions<span class="float-end"><b>` + server.session_count + `</b></span></p>` +
        `<p><i class="col-green far fa-fw fa-clock"></i> Playtime<span class="float-end"><b>` + server.playtime + `</b></span></p>` +
        `<p><i class="col-grey far fa-fw fa-clock"></i> AFK Time<span class="float-end"><b>` + server.afk_time + `</b></span></p>` +
        `<p><i class="col-teal far fa-fw fa-clock"></i> Longest Session<span class="float-end"><b>` + server.longest_session_length + `</b></span></p>` +
        `<p><i class="col-teal far fa-fw fa-clock"></i> Session Median<span class="float-end"><b>` + server.session_median + `</b></span></p>` +
        `<br>` +
        `<p><i class="col-amber fa fa-fw fa-location-arrow"></i> Join Address<span class="float-end">` + server.join_address + `</span></p>` +
        `<br>` +
        `<p><i class="col-red fa fa-fw fa-crosshairs"></i> Player Kills<span class="float-end"><b>` + server.player_kills + `</b></span></p>` +
        `<p><i class="col-green fa fa-fw fa-crosshairs"></i> Mob Kills<span class="float-end"><b>` + server.mob_kills + `</b></span></p>` +
        `<p><i class=" fa fa-fw fa-skull"></i> Deaths<span class="float-end"><b>` + server.deaths + `</b></span></p>` +
        `</div><div class="col-xs-12  col-sm-12 col-md-6 col-lg-6">` +
        `<div id="worldpie_server_` + i + `" class="chart-pie"></div>` +
        `</div>` +
        `</div></td></tr>`
}
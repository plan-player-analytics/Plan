function loadPlayerOverviewValues(json, error) {
    if (error) {
        $("#player-overview").addClass("forbidden"); // TODO Figure out 403
        return;
    }

    tab = $("#player-overview");

    // Player information card
    data = json.info;
    element = $(tab).find("#data_player_info");

    $(element).find("#data_online").replaceWith(data.online ? '<i class="col-green fa fa-fw fa-circle"></i> Online' : '<i class="col-red fa fa-fw fa-circle"></i> Offline');
    $(element).find("#data_titles").replaceWith((data.operator ? '<i class="col-blue fab fa-fw fa-superpowers"></i> Operator' : '') + (data.banned ? '<i class="col-red fa fa-fw fa-gavel"></i> Banned' : ''));
    $(element).find("#data_kick_count").text(data.kick_count);
    $(element).find("#data_player_kills").text(data.player_kill_count);
    $(element).find("#data_mob_kills").text(data.mob_kill_count);
    $(element).find("#data_deaths").text(data.death_count);

    $(element).find("#data_playtime").text(data.playtime);
    $(element).find("#data_active_playtime").text(data.active_playtime);
    $(element).find("#data_afk_time").text(data.afk_time);
    $(element).find("#data_session_count").text(data.session_count);
    $(element).find("#data_longest_session_lenght").text(data.longest_session_lenght);
    $(element).find("#data_session_median").text(data.session_median);

    $(element).find("#data_activity_index").text(data.activity_index);
    $(element).find("#data_activity_index_group").text(data.activity_index_group);
    $(element).find("#data_favorite_server").text(data.favorite_server);
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
    $(element).find("#data_average_session_length_30d").text(data.average_session_length_30d);
    $(element).find("#data_average_session_length_7d").text(data.average_session_length_7d);
    $(element).find("#data_session_count_30d").text(data.session_count_30d);
    $(element).find("#data_session_count_7d").text(data.session_count_7d);
    $(element).find("#data_player_kills_30d").text(data.player_kill_count_30d);
    $(element).find("#data_player_kills_7d").text(data.player_kill_count_7d);
    $(element).find("#data_mob_kills_30d").text(data.mob_kill_count_30d);
    $(element).find("#data_mob_kills_7d").text(data.mob_kill_count_7d);
    $(element).find("#data_deaths_30d").text(data.death_count_30d);
    $(element).find("#data_deaths_7d").text(data.death_count_7d)
}

function createNicknameTableBody(nicknames) {
    var table = '<tbody>';

    if (nicknames.length === 0) {
        table += '<tr><td>No Nicknames</td><td>-</td><td>-</td></tr>'
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
        table += '<tr><td>No Connection Data</td><td>-</td></tr>'
    }

    for (var i = 0; i < connections.length; i++) {
        var connection = connections[i];
        table += '<tr><td>' + connection.geolocation + '</td>' +
            '<td>' + connection.date + '</td></tr>'
    }

    table += '</tbody>';
    return table;
}
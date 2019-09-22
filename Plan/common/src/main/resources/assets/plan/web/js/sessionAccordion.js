function loadSessionAccordion(json, error) {
    sessionTable = $("#sessions-overview").find("#tableAccordion").find("tbody");

    if (error) {
        sessionTable.append('<tr><td>Error: ' + error + '</td><td>-</td><td>-</td><td>-</td></tr>');
        return;
    }

    var sessions = json.sessions;

    if (!sessions.length) {
        sessionTable.append('<tr><td>No Data</td><td>-</td><td>-</td><td>-</td></tr>');
        return;
    }

    // sessions_per_page can be undefined (-> NaN) or higher than amount of sessions.
    var limit = json.sessions_per_page ? json.sessions_per_page : sessions.length;
    limit = Math.min(limit, sessions.length);

    var sessionsHtml = '';
    for (var i = 0; i < limit; i++) {
        var session = sessions[i];
        var title = createAccordionTitle(i, session);
        var body = createAccordionBody(i, session);
        sessionsHtml += title + body;
    }

    sessionTable.append(sessionsHtml);

    for (var i = 0; i < limit; i++) {
        $('#session_h_' + i).click(onOpenSession(i, sessions));
    }
}

function onOpenSession(i, sessions) {
    var opened = false;
    return function () {
        if (opened) {
            return;
        }
        setTimeout(function () {
            var session = sessions[i];
            var worldSeries = {name: 'World Playtime', colorByPoint: true, data: session.world_series};
            var gmSeries = session.gm_series;

            worldPie("worldpie_" + i, worldSeries, gmSeries, '#3A3B45');
        }, 250);
        opened = true;
    }
}

function loadPlayerKills(json, error) {
    if (error) {
        $('#playerKillTable').replaceWith('<p>Failed to load player kills: ' + error + '</p>');
        return;
    }
    $('#playerKillTable').replaceWith(createKillsTable(json.player_kills));
}

function loadPlayerDeaths(json, error) {
    if (error) {
        $('#playerDeathTable').replaceWith('<p>Failed to load player deaths: ' + error + '</p>');
        return;
    }
    $('#playerDeathTable').replaceWith(createKillsTable(json.player_deaths));
}

function createAccordionTitle(i, session) {
    return '<tr id="session_h_' + i + '" aria-controls="session_t_' + i + '" aria-expanded="false" class="clickable collapsed bg-teal" data-target="#session_t_' + i + '" data-toggle="collapse"><td>'
        + session.name + (session.first_session ? ' <i title="Registered (First session)" class="far fa-calendar-plus"></i>' : '') + '</td>'
        + '<td>' + session.start + '</td>'
        + '<td>' + session.length + '</td>'
        + '<td>' + (session.network_server ? session.network_server : session.most_used_world) + '</td></tr>'
}

function createAccordionBody(i, session) {
    return '<tr class="collapse" data-parent="#tableAccordion" id="session_t_' + i + '">' +
        '<td colspan="4">' +
        '<div class="collapse row" data-parent="#tableAccordion" id="session_t_' + i + '">' +
        '<div class="col-xs-12  col-sm-12 col-md-6 col-lg-6">' +
        '<p><i class="col-teal far fa-fw fa-clock"></i> Ended<span class="float-right"><b>' + session.end + '</b></span></p>' +
        '<p><i class="col-green far fa-fw fa-clock"></i> Length<span class="float-right"><b>' + session.length + '</b></span></p>' +
        '<p><i class="col-grey far fa-fw fa-clock"></i> Time AFK<span class="float-right"><b>' + session.afk_time + '</b></span></p>' +
        '<p><i class="col-green fa fa-fw fa-server"></i> Server<span class="float-right"><b>' + session.server_name + '</b></span></p><br>' +
        '<p><i class="col-red fa fa-fw fa-crosshairs"></i> Player Kills<span class="float-right"><b>' + session.player_kills.length + '</b></span></p>' +
        '<p><i class="col-green fa fa-fw fa-crosshairs"></i> Mob Kills<span class="float-right"><b>' + session.mob_kills + '</b></span></p>' +
        '<p><i class=" fa fa-fw fa-skull"></i> Deaths<span class="float-right"><b>' + session.deaths + '</b></span></p><hr>' +
        createKillsTable(session.player_kills) +
        '</div><div class="col-xs-12  col-sm-12 col-md-6 col-lg-6">' +
        '<div id="worldpie_' + i + '" class="chart-pie"></div>' +
        '<a href="/player/' + session.player_name + '" class="float-right btn bg-blue"><i class="fa fa-user"></i><span> Player Page</span></a>' +
        (session.network_server ? '<a href="/server/' + session.server_name + '" class="float-right btn bg-light-green mr-2"><i class="fa fa-server"></i><span> Server Page</span></a>' : '') +
        '</div>' +
        '</div></td></tr>'
}

function createKillsTable(player_kills) {
    var table = '<table class="table mb-0"><tbody>';

    if (player_kills.length === 0) {
        table += '<tr><td>None</td><td>-</td><td>-</td></tr>'
    }

    for (var i = 0; i < player_kills.length; i++) {
        var kill = player_kills[i];
        table += '<tr><td>' + kill.date + '</td>' +
            '<td>' + kill.killer +
            (kill.killer === kill.victim ? '<i class="fa fa-fw fa-skull-crossbones col-red"></i>' : '<i class="fa fa-fw fa-angle-right col-red"></i>') +
            kill.victim + '</td>' +
            '<td>' + kill.weapon + '</td></tr>'
    }

    table += '</tbody></table>';
    return table;
}
function loadSessionAccordion(json, error) {
    if (error) {
        $('#sessions-overview').addClass('forbidden'); // TODO Figure out 403
        return;
    }

    sessionTable = $("#sessions-overview").find("#tableAccordion").find("tbody");

    if (!sessionTable) throw new Error("Session table not found with ids #session-overview #tableAccordion");

    var sessions = json.sessions;
    for (var i = 0; i < sessions.length; i++) {
        var session = sessions[i];
        var title = createAccordionTitle(i, session);
        var body = createAccordionBody(i, session);

        var worldSeries = {name: 'World Playtime', colorByPoint: true, data: session.world_series};
        var gmSeries = session.gm_series;

        sessionTable.append(title);
        sessionTable.append(body);

        worldPie("worldpie_" + i, worldSeries, gmSeries);
    }
}

function createAccordionTitle(i, session) {
    return '<tr aria-controls="session_t_' + i + '" aria-expanded="false" class="clickable collapsed bg-teal" data-target="#session_t_' + i + '" data-toggle="collapse"><td>'
        + session.name + '</td>'
        + '<td>' + session.start + '</td>'
        + '<td>' + session.length + '</td>'
        + '<td>' + session.most_used_world + '</td></tr>'
}

function createAccordionBody(i, session) {


    return '<tr class="collapse" data-parent="#tableAccordion" id="session_t_' + i + '">' +
        '<td colspan="4">' +
        '<div class="collapse row" data-parent="#tableAccordion" id="session_t_' + i + '">' +
        '<div class="col-xs-12  col-sm-12 col-md-6 col-lg-6">' +
        '<p><i class="col-teal far fa-fw fa-clock"></i> Session Ended<span class="float-right"><b>' + session.end + '</b></span></p>' +
        '<p><i class="col-green far fa-fw fa-clock"></i> Session Length<span class="float-right"><b>' + session.length + '</b></span></p>' +
        '<p><i class="col-grey far fa-fw fa-clock"></i> AFK<span class="float-right"><b>' + session.afk_time + '</b></span></p>' +
        '<p><i class="col-green fa fa-fw fa-server"></i> Server<span class="float-right"><b>' + session.server_name + '</b></span></p><br>' +
        '<p><i class="col-red fa fa-fw fa-crosshairs"></i> Player Kills<span class="float-right"><b>' + session.player_kills.length + '</b></span></p>' +
        '<p><i class="col-green fa fa-fw fa-crosshairs"></i> Mob Kills<span class="float-right"><b>' + session.mob_kills + '</b></span></p>' +
        '<p><i class=" fa fa-fw fa-skull"></i> Deaths<span class="float-right"><b>' + session.deaths + '</b></span></p><hr>' +
        createKillsTable(session.player_kills) +
        '</div><div class="col-xs-12  col-sm-12 col-md-6 col-lg-6">' +
        '<div id="worldpie_' + i + '" class="chart-pie"></div>' +
        '<a target="_blank" href="/player/' + session.player_name + '"><button type="button" class="float-right btn bg-blue waves-effect\"><i class="fa fa-user"></i><span> Player Page</span></button></a>' +
        '</div>' +
        '</div></td></tr>'
}

function createKillsTable(player_kills) {
    var table = '<table class="table table-striped scrollbar"><tbody>';

    if (player_kills.length === 0) {
        table += '<td>No Kills</td><td>-</td><td>-</td>'
    }

    for (var i = 0; i < player_kills.length; i++) {
        var kill = player_kills[i];
        table += '<td>' + kill.date + '</td>' +
            '<td>' + kill.killer + '<i class="fa fa-fw fa-angle-right col-red"></i>' + kill.victim + '</td>' +
            '<td>' + kill.weapon + '</td>'
    }

    table += '</tbody></table>';
    return table;
}
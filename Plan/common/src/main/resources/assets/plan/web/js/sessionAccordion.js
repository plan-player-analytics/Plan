function loadSessionAccordion(json, error) {
    const sessionTable = document.querySelector('#sessions-overview #tableAccordion tbody');

    if (error) {
        sessionTable.innerHTML = `<tr><td>Error: ${error}</td><td>-</td><td>-</td><td>-</td></tr>`;
        return;
    }

    const sessions = json.sessions;

    if (!sessions.length) {
        sessionTable.innerHTML = `<tr><td>No Data</td><td>-</td><td>-</td><td>-</td></tr>`;
        return;
    }

    // sessions_per_page can be undefined (-> NaN) or higher than amount of sessions.
    let limit = json.sessions_per_page ? json.sessions_per_page : sessions.length;
    limit = Math.min(limit, sessions.length);

    let sessionsHtml = '';
    for (let i = 0; i < limit; i++) {
        const session = sessions[i];
        const title = createAccordionTitle(i, session);
        const body = createAccordionBody(i, session);
        sessionsHtml += title + body;
    }
    sessionTable.innerHTML = sessionsHtml;

    for (let i = 0; i < limit; i++) {
        document.getElementById(`session_h_${i}`).addEventListener('click', onOpenSession(i, sessions));
    }
}

function onOpenSession(i, sessions) {
    let opened = false;
    return function () {
        if (opened) {
            return;
        }
        setTimeout(function () {
            const session = sessions[i];
            const worldSeries = {name: `World Playtime`, colorByPoint: true, data: session.world_series};
            const gmSeries = session.gm_series;

            worldPie(`worldpie_${i}`, worldSeries, gmSeries);
        }, 250);
        opened = true;
    }
}

function loadPlayerKills(json, error) {
    if (error) {
        $('#playerKillTable').replaceWith(`<p>Failed to load player kills: ${error}</p>`);
        return;
    }
    $('#playerKillTable').replaceWith(createKillsTable(json.player_kills));
}

function loadPlayerdeaths(json, error) {
    if (error) {
        $('#playerDeathTable').replaceWith(`<p>Failed to load player deaths: ${error}</p>`);
        return;
    }
    $('#playerDeathTable').replaceWith(createKillsTable(json.player_deaths));
}

function createAccordionTitle(i, session) {
    let style = session.start.includes("Online") ? 'bg-teal' : 'bg-teal-outline';
    return `<tr id="session_h_${i}" aria-controls="session_t_${i}" aria-expanded="false" 
                class="clickable collapsed ${style}" data-target="#session_t_${i}" data-toggle="collapse">
                <td>${session.name}${session.first_session ? ` <i title="Registered (First session)" class="far fa-calendar-plus"></i>` : ``}</td>
                <td>${session.start}</td><td>${session.length}</td>
                <td>${session.network_server ? session.network_server : session.most_used_world}</td>
            </tr>`
}

function createAccordionBody(i, session) {
    return `<tr class="collapse" data-parent="#tableAccordion" id="session_t_${i}">
                <td colspan="4">
                    <div class="collapse row" data-parent="#tableAccordion" id="session_t_${i}">
                        <div class="col-xs-12  col-sm-12 col-md-6 col-lg-6">
                            <p><i class="col-teal far fa-fw fa-clock"></i> Ended<span class="float-end"><b>${session.end}</b></span></p>
                            <p><i class="col-green far fa-fw fa-clock"></i> Length<span class="float-end"><b>${session.length}</b></span></p>
                            <p><i class="col-grey far fa-fw fa-clock"></i> AFK Time<span class="float-end"><b>${session.afk_time}</b></span></p>
                            <p><i class="col-green fa fa-fw fa-server"></i> Server<span class="float-end"><b>${session.server_name}</b></span></p>
                            ${session.avg_ping ? `<p><i class="col-amber fa fa-fw fa-signal"></i> Average Ping<span class="float-end"><b>` + session.avg_ping + `</b></span></p>` : ``}
                            <br>
                            <p><i class="col-red fa fa-fw fa-crosshairs"></i> Player Kills<span class="float-end"><b>${session.player_kills.length}</b></span></p>
                            <p><i class="col-green fa fa-fw fa-crosshairs"></i> Mob Kills<span class="float-end"><b>${session.mob_kills}</b></span></p>
                            <p><i class=" fa fa-fw fa-skull"></i> Deaths<span class="float-end"><b>${session.deaths}</b></span></p>
                            <hr>
                            ${createKillsTable(session.player_kills)}
                        </div>
                        <div class="col-xs-12  col-sm-12 col-md-6 col-lg-6">
                            <div id="worldpie_${i}" class="chart-pie"></div>
                            <a href="${session.network_server ? `./player/` : `../player/`}${session.player_uuid}" class="float-end btn bg-blue">
                                <i class="fa fa-user"></i><span> Player Page</span>
                            </a>
                            ${session.network_server ? `<a href="./server/${session.server_url_name}" class="float-end btn bg-light-green mr-2">
                                <i class="fa fa-server"></i><span> Server Analysis</span>
                            </a>` : ``}
                        </div>
                    </div>
                </td>
            </tr>`;
}

function createKillsTable(player_kills) {
    let table = '<table class="table mb-0"><tbody>';

    if (!player_kills.length) {
        table += `<tr><td>None</td><td>-</td><td>-</td></tr>`
    }

    for (const kill of player_kills) {
        table += `<tr>
                    <td>${kill.date}</td>
                    <td>${kill.killer} ${
            kill.killer === kill.victim
                ? '<i class="fa fa-fw fa-skull-crossbones col-red"></i>'
                : '<i class="fa fa-fw fa-angle-right col-red"></i>'
        } ${kill.victim}</td>
                    <td>${kill.weapon}</td>
                </tr>`
    }

    table += '</tbody></table>';
    return table;
}

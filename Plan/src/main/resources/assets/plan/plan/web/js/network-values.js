const trend_up_good = "<span class=\"badge bg-success\"><i class=\"fa fa-caret-up\"></i> ";
const trend_up_bad = "<span class=\"badge bg-danger\"><i class=\"fa fa-caret-up\"></i> ";
const trend_down_bad = "<span class=\"badge bg-danger\"><i class=\"fa fa-caret-down\"></i> ";
const trend_down_good = "<span class=\"badge bg-success\"><i class=\"fa fa-caret-down\"></i> ";
const trend_same = "<span class=\"badge bg-warning\"><i class=\"fa fa-caret-right\"></i> ";

const trend_end = "</span>";

function trend(trend) {
    if (!trend) {
        return trend_same + '?' + trend_end;
    }
    switch (trend.direction) {
        case '+':
            return (trend.reversed ? trend_up_bad : trend_up_good) + trend.text + trend_end;
        case '-':
            return (trend.reversed ? trend_down_good : trend_down_bad) + trend.text + trend_end;
        default:
            return trend_same + trend.text + trend_end;
    }
}

function smallTrend(trend) {
    if (!trend) {
        return ' <i class="text-warning fa fa-caret-right"></i>';
    }
    switch (trend.direction) {
        case '+':
            trend_color = trend.reversed ? 'text-danger' : 'text-success';
            return ` <i class="${trend_color} fa fa-caret-up" title="${trend.text}"></i>`;
        case '-':
            trend_color = trend.reversed ? 'text-success' : 'text-danger';
            return ` <i class="${trend_color} fa fa-caret-down" title="${trend.text}"></i>`;
        default:
            return ` <i class="text-warning fa fa-caret-right" title="${trend.text}"></i>`;
    }
}

function displayError(element, error) {
    insertElementAfterElement(element.querySelector('.d-sm-flex'), () => {
        const alert = document.createElement('div');
        alert.classList.add('alert', 'alert-danger');
        alert.setAttribute('role', 'alert');
        alert.innerText = `Failed to load values: ${error}`;
        return alert;
    })
}

/* This function loads Network Overview tab */
function loadNetworkOverviewValues(json, error) {
    const tab = document.getElementById('network-overview');
    if (error) {
        displayError(tab, error);
        return;
    }

    // Last 7 days
    let data = json.players;
    let element = tab.querySelector('#data_players');

    element.querySelector('#data_unique_players_1d').innerText = data.unique_players_1d;
    element.querySelector('#data_unique_players_7d').innerText = data.unique_players_7d;
    element.querySelector('#data_unique_players_30d').innerText = data.unique_players_30d;
    element.querySelector('#data_new_players_1d').innerText = data.new_players_1d;
    element.querySelector('#data_new_players_7d').innerText = data.new_players_7d;
    element.querySelector('#data_new_players_30d').innerText = data.new_players_30d;

    // Server As Numbers
    data = json.numbers;
    element = tab.querySelector('#data_numbers');

    element.querySelector('#data_total').innerText = data.total_players;
    element.querySelector('#data_regular').innerText = data.regular_players;
    element.querySelector('#data_online').innerText = data.online_players;

    element.querySelector('#data_last_peak_date').innerText = data.last_peak_date;
    element.querySelector('#data_last_peak_players').innerText = data.last_peak_players;
    element.querySelector('#data_best_peak_date').innerText = data.best_peak_date;
    element.querySelector('#data_best_peak_players').innerText = data.best_peak_players;

    element.querySelector('#data_playtime').innerText = data.playtime;
    element.querySelector('#data_player_playtime').innerText = data.player_playtime;
    element.querySelector('#data_session_length_avg').innerText = data.session_length_avg;
    element.querySelector('#data_sessions').innerText = data.sessions;

    // Week Comparison
    data = json.weeks;
    element = tab.querySelector('#data_weeks');

    element.querySelector('#data_start').innerText = data.start;
    element.querySelector('#data_midpoint').innerText = data.midpoint;
    element.querySelector('#data_midpoint2').innerText = data.midpoint;
    element.querySelector('#data_end').innerText = data.end;

    element.querySelector('#data_unique_before').innerText = data.unique_before;
    element.querySelector('#data_unique_after').innerText = data.unique_after;
    element.querySelector('#data_unique_trend').innerHTML = trend(data.unique_trend);
    element.querySelector('#data_new_before').innerText = data.new_before;
    element.querySelector('#data_new_after').innerText = data.new_after;
    element.querySelector('#data_new_trend').innerHTML = trend(data.new_trend);
    element.querySelector('#data_regular_before').innerText = data.regular_before;
    element.querySelector('#data_regular_after').innerText = data.regular_after;
    element.querySelector('#data_regular_trend').innerHTML = trend(data.regular_trend);

    element.querySelector('#data_average_playtime_before').innerText = data.average_playtime_before;
    element.querySelector('#data_average_playtime_after').innerText = data.average_playtime_after;
    element.querySelector('#data_average_playtime_trend').innerHTML = trend(data.average_playtime_trend);
    element.querySelector('#data_sessions_before').innerText = data.sessions_before;
    element.querySelector('#data_sessions_after').innerText = data.sessions_after;
    element.querySelector('#data_sessions_trend').innerHTML = trend(data.sessions_trend);
    element.querySelector('#data_session_length_average_before').innerText = data.session_length_average_before;
    element.querySelector('#data_session_length_average_after').innerText = data.session_length_average_after;
    element.querySelector('#data_session_length_average_trend').innerHTML = trend(data.session_length_average_trend);

}

/* This function loads Online Activity Overview tab */
function loadOnlineActivityOverviewValues(json, error) {
    const tab = document.getElementById('online-activity-overview');
    if (error) {
        displayError(tab, error);
        return;
    }

    // Online Activity as Numbers
    let data = json.numbers;
    let element = tab.querySelector('#data_numbers');

    element.querySelector('#data_unique_players_30d').innerHTML = data.unique_players_30d + smallTrend(data.unique_players_30d_trend);
    element.querySelector('#data_unique_players_7d').innerText = data.unique_players_7d;
    element.querySelector('#data_unique_players_24h').innerText = data.unique_players_24h;

    element.querySelector('#data_unique_players_30d_avg').innerHTML = data.unique_players_30d_avg + smallTrend(data.unique_players_30d_avg_trend);
    element.querySelector('#data_unique_players_7d_avg').innerText = data.unique_players_7d_avg;
    element.querySelector('#data_unique_players_24h_avg').innerText = data.unique_players_24h_avg;

    element.querySelector('#data_new_players_30d').innerHTML = data.new_players_30d + smallTrend(data.new_players_30d_trend);
    element.querySelector('#data_new_players_7d').innerText = data.new_players_7d;
    element.querySelector('#data_new_players_24h').innerText = data.new_players_24h;

    element.querySelector('#data_new_players_30d_avg').innerHTML = data.new_players_30d_avg + smallTrend(data.new_players_30d_avg_trend);
    element.querySelector('#data_new_players_7d_avg').innerText = data.new_players_7d_avg;
    element.querySelector('#data_new_players_24h_avg').innerText = data.new_players_24h_avg;

    element.querySelector('#data_new_players_retention_30d').innerText = '(' + data.new_players_retention_30d + '/' + data.new_players_30d + ') ' + data.new_players_retention_30d_perc;
    element.querySelector('#data_new_players_retention_7d').innerText = '(' + data.new_players_retention_7d + '/' + data.new_players_7d + ') ' + data.new_players_retention_7d_perc;
    element.querySelector('#data_new_players_retention_24h').innerHTML = '(' + data.new_players_retention_24h + '/' + data.new_players_24h + ') ' + data.new_players_retention_24h_perc + ' <i class="far fa-fw fa-eye"></i>';

    element.querySelector('#data_playtime_30d').innerHTML = data.playtime_30d + smallTrend(data.playtime_30d_trend);
    element.querySelector('#data_playtime_7d').innerText = data.playtime_7d;
    element.querySelector('#data_playtime_24h').innerText = data.playtime_24h;

    element.querySelector('#data_playtime_30d_avg').innerHTML = data.playtime_30d_avg + smallTrend(data.playtime_30d_avg_trend);
    element.querySelector('#data_playtime_7d_avg').innerText = data.playtime_7d_avg;
    element.querySelector('#data_playtime_24h_avg').innerText = data.playtime_24h_avg;

    element.querySelector('#data_session_length_30d_avg').innerHTML = data.session_length_30d_avg + smallTrend(data.session_length_30d_trend);
    element.querySelector('#data_session_length_7d_avg').innerText = data.session_length_7d_avg;
    element.querySelector('#data_session_length_24h_avg').innerText = data.session_length_24h_avg;

    element.querySelector('#data_sessions_30d').innerHTML = data.sessions_30d + smallTrend(data.sessions_30d_trend);
    element.querySelector('#data_sessions_7d').innerText = data.sessions_7d;
    element.querySelector('#data_sessions_24h').innerText = data.sessions_24h;

    // Insights
    data = json.insights;
    element = tab.querySelector('#data_insights');

    element.querySelector('#data_players_first_join_avg').innerHTML = data.players_first_join_avg + smallTrend(data.players_first_join_trend);
    element.querySelector('#data_first_session_length_avg').innerHTML = data.first_session_length_avg + smallTrend(data.first_session_length_trend);
    element.querySelector('#data_lone_joins').innerHTML = data.lone_joins + smallTrend(data.lone_joins_trend);
    element.querySelector('#data_lone_new_joins').innerHTML = data.lone_new_joins + smallTrend(data.lone_new_joins_trend);
}

/* This function loads Sessions tab */
function loadSessionValues(json, error) {
    const tab = document.getElementById('sessions-overview');
    if (error) {
        displayError(tab, error);
        return;
    }

    // Insights
    let data = json.insights;
    let element = tab.querySelector('#data_insights');

    element.querySelector('#data_total_playtime').innerText = data.total_playtime;
    element.querySelector('#data_afk_time').innerText = data.afk_time;
    element.querySelector('#data_afk_time_perc').innerText = data.afk_time_perc
}

/* This function loads Playerbase Overview tab */
function loadPlayerbaseOverviewValues(json, error) {
    const tab = document.getElementById('playerbase-overview');
    if (error) {
        displayError(tab, error);
        return;
    }

    // Trends
    let data = json.trends;
    let element = tab.querySelector('#data_trends');

    element.querySelector('#data_total_players_then').innerText = data.total_players_then;
    element.querySelector('#data_total_players_now').innerText = data.total_players_now;
    element.querySelector('#data_total_players_trend').innerHTML = trend(data.total_players_trend);
    element.querySelector('#data_regular_players_then').innerText = data.regular_players_then;
    element.querySelector('#data_regular_players_now').innerText = data.regular_players_now;
    element.querySelector('#data_regular_players_trend').innerHTML = trend(data.regular_players_trend);
    element.querySelector('#data_playtime_avg_then').innerText = data.playtime_avg_then;
    element.querySelector('#data_playtime_avg_now').innerText = data.playtime_avg_now;
    element.querySelector('#data_playtime_avg_trend').innerHTML = trend(data.playtime_avg_trend);
    element.querySelector('#data_afk_then').innerText = data.afk_then;
    element.querySelector('#data_afk_now').innerText = data.afk_now;
    element.querySelector('#data_afk_trend').innerHTML = trend(data.afk_trend);
    element.querySelector('#data_regular_playtime_avg_then').innerText = data.regular_playtime_avg_then;
    element.querySelector('#data_regular_playtime_avg_now').innerText = data.regular_playtime_avg_now;
    element.querySelector('#data_regular_playtime_avg_trend').innerHTML = trend(data.regular_playtime_avg_trend);
    element.querySelector('#data_regular_session_avg_then').innerText = data.regular_session_avg_then;
    element.querySelector('#data_regular_session_avg_now').innerText = data.regular_session_avg_now;
    element.querySelector('#data_regular_session_avg_trend').innerHTML = trend(data.regular_session_avg_trend);
    element.querySelector('#data_regular_afk_then').innerText = data.regular_afk_avg_then;
    element.querySelector('#data_regular_afk_now').innerText = data.regular_afk_avg_now;
    element.querySelector('#data_regular_afk_trend').innerHTML = trend(data.regular_afk_avg_trend);

    // Insights
    data = json.insights;
    element = tab.querySelector('#data_insights');

    element.querySelector('#data_new_to_regular').innerHTML = data.new_to_regular + smallTrend(data.new_to_regular_trend);
    element.querySelector('#data_regular_to_inactive').innerHTML = data.regular_to_inactive + smallTrend(data.regular_to_inactive_trend);
}

// Lowercase due to locale translation: Server
function loadservers(json, error) {
    if (error) {
        displayError(document.getElementById('servers-tab'), error);
        return;
    }

    const servers = json.servers;

    if (!servers || !servers.length) {
        let elements = document.getElementsByClassName('nav-servers');
        for (let i = 0; i < elements.length; i++) { elements[i].style.display = 'none'; }
        document.getElementById('game-server-warning').classList.remove('hidden');
        document.getElementById('data_server_list').innerHTML =
            `<div class="card shadow mb-4"><div class="card-body"><p>No servers found in the database.</p><p>It appears that Plan is not installed on any game servers or not connected to the same database. See <a href="https://github.com/plan-player-analytics/Plan/wiki">wiki</a> for Network tutorial.</p></div></div>`
        document.getElementById('quick_view_players_online').innerText = `No server to display online activity for.`;
        return;
    }

    let navServersHtml = '';
    let serversHtml = '';
    for (let i = 0; i < servers.length; i++) {
        navServersHtml += addserverToNav(servers[i]);
        serversHtml += createnetworkserverBox(i, servers[i]);
    }

    document.getElementById("navSrvContainer").innerHTML = navServersHtml;
    document.getElementById("data_server_list").innerHTML = serversHtml;

    for (let i = 0; i < servers.length; i++) {
        document.getElementById(`server_quick_view_${i}`)
            .addEventListener('click', onViewserver(i, servers));
    }
    onViewserver(0, servers)(); // Open first server.
}

// Lowercase due to locale translation: Server
function addserverToNav(server) {
    return `<a class="collapse-item nav-button" href="server/${server.name}"><i class="fas fa-fw fa-server col-light-green"></i> ${server.name}</a>`;
}

// Lowercase due to locale translation: Network
function createnetworkserverBox(i, server) {
    return `<div class="card shadow mb-4">
                <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                    <h6 class="m-0 fw-bold col-black">
                        <i class="fas fa-fw fa-server col-light-green"></i> ${server.name}
                    </h6>
                    <div class="mb-0 col-lg-6">
                        <p class="mb-1"><i class="fa fa-fw fa-users col-black"></i> Registered Players<span class="float-end"><b>${server.players}</b></span></p>
                        <p class="mb-0"><i class="fa fa-fw fa-user col-blue"></i> Players Online<span class="float-end"><b>${server.online}</b></span></p>
                    </div>
                </div>
                <div class="d-flex align-items-center justify-content-between">
                    <a class="btn col-light-green ms-2" href="server/${server.name}">
                        <i class="fa fa-fw fa-chart-line"></i> Server Analysis
                    </a>
                    <button class="btn bg-blue my-2 me-2" id="server_quick_view_${i}">
                        Quick view <i class="fa fa-fw fa-caret-square-right"></i>
                    </button>
                </div>
            </div>`;
}

// Lowercase due to locale translation: Server
function onViewserver(i, servers) {
    return function () {
        setTimeout(function () {
            const server = servers[i];
            const playersOnlineSeries = {
                name: s.name.playersOnline,
                type: s.type.areaSpline,
                tooltip: s.tooltip.zeroDecimals,
                data: server.playersOnline,
                color: v.colors.playersOnline,
                yAxis: 0
            };
            document.querySelector('.data_server_name').innerText = server.name
            playersChart('quick_view_players_online', playersOnlineSeries, 2);

            const quickView = document.getElementById('data_quick_view');

            quickView.querySelector('#data_last_peak_date').innerText = server.last_peak_date;
            quickView.querySelector('#data_last_peak_players').innerText = server.last_peak_players;
            quickView.querySelector('#data_best_peak_date').innerText = server.best_peak_date;
            quickView.querySelector('#data_best_peak_players').innerText = server.best_peak_players;

            quickView.querySelector('#data_unique').innerText = server.unique_players;
            quickView.querySelector('#data_new').innerText = server.new_players;
            quickView.querySelector('#data_avg_tps').innerText = server.avg_tps;
            quickView.querySelector('#data_low_tps_spikes').innerText = server.low_tps_spikes;
            quickView.querySelector('#data_downtime').innerText = server.downtime;
        }, 0);
    }
}

function loadPlayersOnlineGraph(json, error) {
    if (json) {
        const series = {
            playersOnline: {
                name: s.name.playersOnline, type: s.type.areaSpline, tooltip: s.tooltip.zeroDecimals,
                data: json.playersOnline, color: v.colors.playersOnline, yAxis: 0
            }
        };
        playersChart('playersOnlineChart', series.playersOnline, 2);
    } else if (error) {
        document.getElementById('playersOnlineChart').innerText = `Failed to load graph data: ${error}`;
    }
}

function loadUniqueAndNewGraph(json, error) {
    if (json) {
        const uniquePlayers = {
            name: s.name.uniquePlayers, type: s.type.spline, tooltip: s.tooltip.zeroDecimals,
            data: json.uniquePlayers, color: v.colors.playersOnline
        };
        const newPlayers = {
            name: s.name.newPlayers, type: s.type.spline, tooltip: s.tooltip.zeroDecimals,
            data: json.newPlayers, color: v.colors.newPlayers
        };
        dayByDay('uniqueChart', [uniquePlayers, newPlayers]);
    } else if (error) {
        document.getElementById('uniqueChart').innerText = `Failed to load graph data: ${error}`;
    }
}

function loadHourlyUniqueAndNewGraph(json, error) {
    if (json) {
        const uniquePlayers = {
            name: s.name.uniquePlayers, type: s.type.spline, tooltip: s.tooltip.zeroDecimals,
            data: json.uniquePlayers, color: v.colors.playersOnline
        };
        const newPlayers = {
            name: s.name.newPlayers, type: s.type.spline, tooltip: s.tooltip.zeroDecimals,
            data: json.newPlayers, color: v.colors.newPlayers
        };
        dayByDay('hourlyUniqueChart', [uniquePlayers, newPlayers]);
    } else if (error) {
        document.getElementById('uniqueChart').innerText = `Failed to load graph data: ${error}`;
    }
}

// Lowercase due to locale translation: Server
function loadserverPie(json, error) {
    if (json) {
        serverPieSeries = {
            name: 'Server Playtime',
            colorByPoint: true,
            colors: json.server_pie_colors,
            data: json.server_pie_series_30d
        };
        serverPie('serverPie', serverPieSeries);
    } else if (error) {
        document.getElementById('serverPie').innerText = `Failed to load graph data: ${error}`;
    }
}

function loadActivityGraphs(json, error) {
    if (json) {
        activityPie('activityPie', {
            name: s.name.unit_players, colorByPoint: true, data: json.activity_pie_series
        });
        stackChart('activityStackGraph', json.activity_labels, json.activity_series, s.name.unit_players);
    } else if (error) {
        const errorMessage = `Failed to load graph data: ${error}`;
        document.getElementById('activityPie').innerText = errorMessage;
        document.getElementById('activityStackGraph').innerText = errorMessage;
    }
}

function loadGeolocationGraph(json, error) {
    if (json) {
        const geolocationSeries = {
            name: s.name.unit_players,
            type: 'map',
            mapData: Highcharts.maps['custom/world'],
            data: json.geolocation_series,
            joinBy: ['iso-a3', 'code']
        };
        const geolocationBarSeries = {
            color: json.colors.bars,
            name: s.name.unit_players,
            data: json.geolocation_bar_series.map(function (bar) {
                return bar.value
            })
        };
        const geolocationBarCategories = json.geolocation_bar_series.map(function (bar) {
            return bar.label
        });
        worldMap('worldMap', json.colors.low, json.colors.high, geolocationSeries);
        horizontalBarChart('countryBarChart', geolocationBarCategories, [geolocationBarSeries], s.name.unit_players);
        if (!json.geolocations_enabled) {
            document.getElementById('geolocation-warning').classList.remove('hidden');
        }
    } else if (error) {
        const errorMessage = `Failed to load graph data: ${error}`;
        document.getElementById('worldMap').innerText = errorMessage;
        document.getElementById('countryBarChart').innerText = errorMessage;
    }
}

function loadJoinAddressPie(json, error) {
    if (json) {
        const series = {
            name: 'Used IP Addresses',
            colorByPoint: true,
            colors: json.colors,
            data: json.slices
        };
        joinAddressPie('joinAddressPie', series);
    } else if (error) {
        document.getElementById('joinAddressPie').innerText = `Failed to load graph data: ${error}`;
    }
}
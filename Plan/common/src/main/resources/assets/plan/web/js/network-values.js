var trend_up_good = "<span class=\"badge badge-success\"><i class=\"fa fa-caret-up\"></i> ";
var trend_up_bad = "<span class=\"badge badge-danger\"><i class=\"fa fa-caret-up\"></i> ";
var trend_down_bad = "<span class=\"badge badge-danger\"><i class=\"fa fa-caret-down\"></i> ";
var trend_down_good = "<span class=\"badge badge-success\"><i class=\"fa fa-caret-down\"></i> ";
var trend_same = "<span class=\"badge badge-warning\"><i class=\"fa fa-caret-right\"></i> ";

var trend_end = "</span>";

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
            return ' <i class="' + trend_color + ' fa fa-caret-up" title="' + trend.text + '"></i>';
        case '-':
            trend_color = trend.reversed ? 'text-success' : 'text-danger';
            return ' <i class="' + trend_color + ' fa fa-caret-down" title="' + trend.text + '"></i>';
        default:
            return ' <i class="text-warning fa fa-caret-right" title="' + trend.text + '"></i>';
    }
}

function displayError(element, error) {
    element.find('.d-sm-flex').after('<div class="alert alert-danger" role="alert">Failed to load values: ' + error + '</div>')
}

/* This function loads Network Overview tab */
function loadNetworkOverviewValues(json, error) {
    tab = $('#network-overview');
    if (error) {
        displayError(tab, error);
        return;
    }

    // Last 7 days
    data = json.players;
    element = $(tab).find('#data_players');

    $(element).find('#data_unique_players_1d').text(data.unique_players_1d);
    $(element).find('#data_unique_players_7d').text(data.unique_players_7d);
    $(element).find('#data_unique_players_30d').text(data.unique_players_30d);
    $(element).find('#data_new_players_1d').text(data.new_players_1d);
    $(element).find('#data_new_players_7d').text(data.new_players_7d);
    $(element).find('#data_new_players_30d').text(data.new_players_30d);

    // Server As Numbers
    data = json.numbers;
    element = $(tab).find('#data_numbers');

    $(element).find('#data_total').text(data.total_players);
    $(element).find('#data_regular').text(data.regular_players);
    $(element).find('#data_online').text(data.online_players);

    $(element).find('#data_last_peak_date').text(data.last_peak_date);
    $(element).find('#data_last_peak_players').text(data.last_peak_players);
    $(element).find('#data_best_peak_date').text(data.best_peak_date);
    $(element).find('#data_best_peak_players').text(data.best_peak_players);

    $(element).find('#data_playtime').text(data.playtime);
    $(element).find('#data_player_playtime').text(data.player_playtime);
    $(element).find('#data_session_length_avg').text(data.session_length_avg);
    $(element).find('#data_sessions').text(data.sessions);

    // Week Comparison
    data = json.weeks;
    element = $(tab).find('#data_weeks');

    $(element).find('#data_start').text(data.start);
    $(element).find('#data_midpoint').text(data.midpoint);
    $(element).find('#data_midpoint2').text(data.midpoint);
    $(element).find('#data_end').text(data.end);

    $(element).find('#data_unique_before').text(data.unique_before);
    $(element).find('#data_unique_after').text(data.unique_after);
    $(element).find('#data_unique_trend').replaceWith(trend(data.unique_trend));
    $(element).find('#data_new_before').text(data.new_before);
    $(element).find('#data_new_after').text(data.new_after);
    $(element).find('#data_new_trend').replaceWith(trend(data.new_trend));
    $(element).find('#data_regular_before').text(data.regular_before);
    $(element).find('#data_regular_after').text(data.regular_after);
    $(element).find('#data_regular_trend').replaceWith(trend(data.regular_trend));

    $(element).find('#data_average_playtime_before').text(data.average_playtime_before);
    $(element).find('#data_average_playtime_after').text(data.average_playtime_after);
    $(element).find('#data_average_playtime_trend').replaceWith(trend(data.average_playtime_trend));
    $(element).find('#data_sessions_before').text(data.sessions_before);
    $(element).find('#data_sessions_after').text(data.sessions_after);
    $(element).find('#data_sessions_trend').replaceWith(trend(data.sessions_trend));
    $(element).find('#data_session_length_average_before').text(data.session_length_average_before);
    $(element).find('#data_session_length_average_after').text(data.session_length_average_after);
    $(element).find('#data_session_length_average_trend').replaceWith(trend(data.session_length_average_trend));

}

/* This function loads Online Activity Overview tab */
function loadOnlineActivityOverviewValues(json, error) {
    tab = $('#online-activity-overview');
    if (error) {
        displayError(tab, error);
        return;
    }

    // Online Activity as Numbers
    data = json.numbers;
    element = $(tab).find('#data_numbers');

    $(element).find('#data_unique_players_30d').replaceWith('<td>' + data.unique_players_30d + smallTrend(data.unique_players_30d_trend) + '</td>');
    $(element).find('#data_unique_players_7d').text(data.unique_players_7d);
    $(element).find('#data_unique_players_24h').text(data.unique_players_24h);

    $(element).find('#data_unique_players_30d_avg').replaceWith('<td>' + data.unique_players_30d_avg + smallTrend(data.unique_players_30d_avg_trend) + '</td>');
    $(element).find('#data_unique_players_7d_avg').text(data.unique_players_7d_avg);
    $(element).find('#data_unique_players_24h_avg').text(data.unique_players_24h_avg);

    $(element).find('#data_new_players_30d').replaceWith('<td>' + data.new_players_30d + smallTrend(data.new_players_30d_trend) + '</td>');
    $(element).find('#data_new_players_7d').text(data.new_players_7d);
    $(element).find('#data_new_players_24h').text(data.new_players_24h);

    $(element).find('#data_new_players_30d_avg').replaceWith('<td>' + data.new_players_30d_avg + smallTrend(data.new_players_30d_avg_trend) + '</td>');
    $(element).find('#data_new_players_7d_avg').text(data.new_players_7d_avg);
    $(element).find('#data_new_players_24h_avg').text(data.new_players_24h_avg);

    $(element).find('#data_new_players_retention_30d').text('(' + data.new_players_retention_30d + '/' + data.new_players_30d + ') ' + data.new_players_retention_30d_perc);
    $(element).find('#data_new_players_retention_7d').text('(' + data.new_players_retention_7d + '/' + data.new_players_7d + ') ' + data.new_players_retention_7d_perc);
    $(element).find('#data_new_players_retention_24h').replaceWith(`<td title="This value is a prediction based on previous players.">(` + data.new_players_retention_24h + '/' + data.new_players_24h + ') ' + data.new_players_retention_24h_perc + '  <i class="far fa-fw fa-eye"></i></td>');

    $(element).find('#data_playtime_30d').replaceWith('<td>' + data.playtime_30d + smallTrend(data.playtime_30d_trend) + '</td>');
    $(element).find('#data_playtime_7d').text(data.playtime_7d);
    $(element).find('#data_playtime_24h').text(data.playtime_24h);

    $(element).find('#data_playtime_30d_avg').replaceWith('<td>' + data.playtime_30d_avg + smallTrend(data.playtime_30d_avg_trend) + '</td>');
    $(element).find('#data_playtime_7d_avg').text(data.playtime_7d_avg);
    $(element).find('#data_playtime_24h_avg').text(data.playtime_24h_avg);

    $(element).find('#data_session_length_30d_avg').replaceWith('<td>' + data.session_length_30d_avg + smallTrend(data.session_length_30d_trend) + '</td>');
    $(element).find('#data_session_length_7d_avg').text(data.session_length_7d_avg);
    $(element).find('#data_session_length_24h_avg').text(data.session_length_24h_avg);

    $(element).find('#data_sessions_30d').replaceWith('<td>' + data.sessions_30d + smallTrend(data.sessions_30d_trend) + '</td>');
    $(element).find('#data_sessions_7d').text(data.sessions_7d);
    $(element).find('#data_sessions_24h').text(data.sessions_24h);

    // Insights
    data = json.insights;
    element = $(tab).find('#data_insights');

    $(element).find('#data_players_first_join_avg').replaceWith(data.players_first_join_avg + smallTrend(data.players_first_join_trend));
    $(element).find('#data_first_session_length_avg').replaceWith(data.first_session_length_avg + smallTrend(data.first_session_length_trend));
    $(element).find('#data_lone_joins').replaceWith(data.lone_joins + smallTrend(data.lone_joins_trend));
    $(element).find('#data_lone_new_joins').replaceWith(data.lone_new_joins + smallTrend(data.lone_new_joins_trend))
}

/* This function loads Sessions tab */
function loadSessionValues(json, error) {
    tab = $('#sessions-overview');
    if (error) {
        displayError(tab, error);
        return;
    }

    // Insights
    data = json.insights;
    element = $(tab).find('#data_insights');

    $(element).find('#data_most_active_gamemode').text(data.most_active_gamemode);
    $(element).find('#data_most_active_gamemode_perc').text(data.most_active_gamemode_perc);
    $(element).find('#data_server_occupied').text(data.server_occupied);
    $(element).find('#data_server_occupied_perc').text(data.server_occupied_perc);
    $(element).find('#data_total_playtime').text(data.total_playtime);
    $(element).find('#data_afk_time').text(data.afk_time);
    $(element).find('#data_afk_time_perc').text(data.afk_time_perc)
}

/* This function loads Playerbase Overview tab */
function loadPlayerbaseOverviewValues(json, error) {
    tab = $('#playerbase-overview');
    if (error) {
        displayError(tab, error);
        return;
    }

    // Trends
    data = json.trends;
    element = $(tab).find('#data_trends');

    $(element).find('#data_total_players_then').text(data.total_players_then);
    $(element).find('#data_total_players_now').text(data.total_players_now);
    $(element).find('#data_total_players_trend').replaceWith(trend(data.total_players_trend));
    $(element).find('#data_regular_players_then').text(data.regular_players_then);
    $(element).find('#data_regular_players_now').text(data.regular_players_now);
    $(element).find('#data_regular_players_trend').replaceWith(trend(data.regular_players_trend));
    $(element).find('#data_playtime_avg_then').text(data.playtime_avg_then);
    $(element).find('#data_playtime_avg_now').text(data.playtime_avg_now);
    $(element).find('#data_playtime_avg_trend').replaceWith(trend(data.playtime_avg_trend));
    $(element).find('#data_afk_then').text(data.afk_then);
    $(element).find('#data_afk_now').text(data.afk_now);
    $(element).find('#data_afk_trend').replaceWith(trend(data.afk_trend));
    $(element).find('#data_regular_playtime_avg_then').text(data.regular_playtime_avg_then);
    $(element).find('#data_regular_playtime_avg_now').text(data.regular_playtime_avg_now);
    $(element).find('#data_regular_playtime_avg_trend').replaceWith(trend(data.regular_playtime_avg_trend));
    $(element).find('#data_regular_session_avg_then').text(data.regular_session_avg_then);
    $(element).find('#data_regular_session_avg_now').text(data.regular_session_avg_now);
    $(element).find('#data_regular_session_avg_trend').replaceWith(trend(data.regular_session_avg_trend));
    $(element).find('#data_regular_afk_then').text(data.regular_afk_avg_then);
    $(element).find('#data_regular_afk_now').text(data.regular_afk_avg_now);
    $(element).find('#data_regular_afk_trend').replaceWith(trend(data.regular_afk_avg_trend));

    // Insights
    data = json.insights;
    element = $(tab).find('#data_insights');

    $(element).find('#data_new_to_regular').replaceWith(data.new_to_regular + smallTrend(data.new_to_regular_trend));
    $(element).find('#data_regular_to_inactive').replaceWith(data.regular_to_inactive + smallTrend(data.regular_to_inactive_trend))
}

function loadservers(servers, error) {
    if (error) {
        displayError($('#servers-tab'), error);
        return;
    }

    if (!servers || !servers.length) {
        $('#data_server_list').replaceWith(
            `<div class="card shadow mb-4"><div class="card-body"><p>No servers found in the database.</p><p>It appears that Plan is not installed on any game servers or not connected to the same database. See <a href="https://github.com/plan-player-analytics/Plan/wiki">wiki</a> for Network tutorial.</p></div></div>`
        );
        $('#quick_view_players_online').text(`No server to display online activity for.`);
        return;
    }

    var serversHtml = '';
    for (var i = 0; i < servers.length; i++) {
        serversHtml += createnetworkserverBox(i, servers[i]);
    }
    $("#data_server_list").replaceWith(serversHtml);

    for (var i = 0; i < servers.length; i++) {
        $('#server_quick_view_' + i).click(onViewserver(i, servers));
    }
    onViewserver(0, servers)(); // Open first server.
}

function createnetworkserverBox(i, server) {
    return `<div class="card shadow mb-4">` +
        `<div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">` +
        `<h6 class="m-0 font-weight-bold col-black"><i class="fas fa-fw fa-server col-light-green"></i> ` + server.name + `</h6>` +
        `<div class="mb-0 col-lg-6">` +
        `<p class="mb-1"><i class="fa fa-fw fa-users col-black"></i> Registered Players` +
        `<span class="float-right"><b>` + server.players + `</b></span></p>` +
        `<p class="mb-0"><i class="fa fa-fw fa-user col-blue"></i> Players Online` +
        `<span class="float-right"><b>` + server.online + `</b></span></p>` +
        `</div>` + // /column
        `</div>` + // /header
        `<div class="d-flex align-items-center justify-content-between">` +
        `<a class="btn col-light-green ml-2" href="server/` + server.name + `"><i class="fa fa-fw fa-chart-line"></i> Server Analysis</a>` +
        `<button class="btn bg-blue my-2 mr-2" id="server_quick_view_` + i + `">Quick view <i class="fa fa-fw fa-caret-square-right"></i></button>` +
        `</div>` + // /buttons
        `</div>` // /card
}

function onViewserver(i, servers) {
    return function () {
        setTimeout(function () {
            var server = servers[i];
            var playersOnlineSeries = {
                name: s.name.playersOnline,
                type: s.type.areaSpline,
                tooltip: s.tooltip.zeroDecimals,
                data: server.playersOnline,
                color: v.colors.playersOnline,
                yAxis: 0
            };
            $('.data_server_name').text(server.name);
            playersChart('quick_view_players_online', playersOnlineSeries, 2);

            var quickView = $('#data_quick_view');

            quickView.find('#data_last_peak_date').text(server.last_peak_date);
            quickView.find('#data_last_peak_players').text(server.last_peak_players);
            quickView.find('#data_best_peak_date').text(server.best_peak_date);
            quickView.find('#data_best_peak_players').text(server.best_peak_players);

            quickView.find('#data_unique').text(server.unique_players);
            quickView.find('#data_new').text(server.new_players);
            quickView.find('#data_avg_tps').text(server.avg_tps);
            quickView.find('#data_low_tps_spikes').text(server.low_tps_spikes);
            quickView.find('#data_downtime').text(server.downtime);
        }, 0);
    }
}
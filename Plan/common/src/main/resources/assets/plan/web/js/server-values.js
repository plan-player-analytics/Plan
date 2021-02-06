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
    element.find('.d-sm-flex').after(`<div class="alert alert-danger" role="alert">Failed to load values: ` + error + '</div>')
}

/* This function loads Server Overview tab */
function loadserverOverviewValues(json, error) {
    const tab = document.getElementById('server-overview');

    if (error) {
        displayError($('#server-overview'), error);
        return;
    }

    // Last 7 days
    let data = json.last_7_days;
    let element = tab.querySelector('#data_7_days');

    element.querySelector('#data_unique').innerText = data.unique_players;
    element.querySelector('#data_unique_day').innerText = data.unique_players_day;
    element.querySelector('#data_new').innerText = data.new_players;
    element.querySelector('#data_retention').innerText = '(' + data.new_players_retention + '/' + data.new_players + ')';
    element.querySelector('#data_retention_perc').innerText = data.new_players_retention_perc;

    element.querySelector('#data_avg_tps').innerText = data.average_tps;
    element.querySelector('#data_low_tps_spikes').innerText = data.low_tps_spikes;
    element.querySelector('#data_downtime').innerText = data.downtime;

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
    element.querySelector('#data_sessions').innerText = data.sessions;

    element.querySelector('#data_player_kills').innerText = data.player_kills;
    element.querySelector('#data_mob_kills').innerText = data.mob_kills;
    element.querySelector('#data_deaths').innerText = data.deaths;

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

    element.querySelector('#data_player_kills_before').innerText = data.player_kills_before;
    element.querySelector('#data_player_kills_after').innerText = data.player_kills_after;
    element.querySelector('#data_player_kills_trend').innerHTML = trend(data.player_kills_trend);
    element.querySelector('#data_mob_kills_before').innerText = data.mob_kills_before;
    element.querySelector('#data_mob_kills_after').innerText = data.mob_kills_after;
    element.querySelector('#data_mob_kills_trend').innerHTML = trend(data.mob_kills_trend);
    element.querySelector('#data_deaths_before').innerText = data.deaths_before;
    element.querySelector('#data_deaths_after').innerText = data.deaths_after;
    element.querySelector('#data_deaths_trend').innerHTML = trend(data.deaths_trend);
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
    $(element).find('#data_first_session_length_median').replaceWith(data.first_session_length_median + smallTrend(data.first_session_length_median_trend));
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
    $(element).find('#data_server_occupied').text("~" + data.server_occupied);
    $(element).find('#data_server_occupied_perc').text(data.server_occupied_perc);
    $(element).find('#data_total_playtime').text(data.total_playtime);
    $(element).find('#data_afk_time').text(data.afk_time);
    $(element).find('#data_afk_time_perc').text(data.afk_time_perc)
}

/* This function loads PvP & PvE tab */
function loadPvPPvEValues(json, error) {
    tab = $('#pvp-pve');
    if (error) {
        displayError(tab, error);
        return;
    }

    // as Numbers
    data = json.numbers;
    element = $(tab).find('#data_numbers');

    $(element).find('#data_player_kills_total').text(data.player_kills_total);
    $(element).find('#data_player_kills_30d').text(data.player_kills_30d);
    $(element).find('#data_player_kills_7d').text(data.player_kills_7d);

    $(element).find('#data_player_kdr_avg').text(data.player_kdr_avg);
    $(element).find('#data_player_kdr_avg_30d').text(data.player_kdr_avg_30d);
    $(element).find('#data_player_kdr_avg_7d').text(data.player_kdr_avg_7d);

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
    data = json.insights;
    element = $(tab).find('#data_insights');

    $(element).find('#data_weapon_1st').text(data.weapon_1st);
    $(element).find('#data_weapon_2nd').text(data.weapon_2nd);
    $(element).find('#data_weapon_3rd').text(data.weapon_3rd);
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

/* This function loads Performance tab */
function loadPerformanceValues(json, error) {
    tab = $('#performance');
    if (error) {
        displayError(tab, error);
        return;
    }

    // as Numbers
    data = json.numbers;
    element = $(tab).find('#data_numbers');

    $(element).find('#data_low_tps_spikes_30d').text(data.low_tps_spikes_30d);
    $(element).find('#data_low_tps_spikes_7d').text(data.low_tps_spikes_7d);
    $(element).find('#data_low_tps_spikes_24h').text(data.low_tps_spikes_24h);
    $(element).find('#data_server_downtime_30d').text(data.server_downtime_30d);
    $(element).find('#data_server_downtime_7d').text(data.server_downtime_7d);
    $(element).find('#data_server_downtime_24h').text(data.server_downtime_24h);
    $(element).find('#data_tps_30d').text(data.tps_30d);
    $(element).find('#data_tps_7d').text(data.tps_7d);
    $(element).find('#data_tps_24h').text(data.tps_24h);
    $(element).find('#data_cpu_30d').text(data.cpu_30d);
    $(element).find('#data_cpu_7d').text(data.cpu_7d);
    $(element).find('#data_cpu_24h').text(data.cpu_24h);
    $(element).find('#data_ram_30d').text(data.ram_30d);
    $(element).find('#data_ram_7d').text(data.ram_7d);
    $(element).find('#data_ram_24h').text(data.ram_24h);
    $(element).find('#data_entities_30d').text(data.entities_30d);
    $(element).find('#data_entities_7d').text(data.entities_7d);
    $(element).find('#data_entities_24h').text(data.entities_24h);
    $(element).find('#data_chunks_30d').text(data.chunks_30d);
    $(element).find('#data_chunks_7d').text(data.chunks_7d);
    $(element).find('#data_chunks_24h').text(data.chunks_24h);
    $(element).find('#data_max_disk_30d').text(data.max_disk_30d);
    $(element).find('#data_max_disk_7d').text(data.max_disk_7d);
    $(element).find('#data_max_disk_24h').text(data.max_disk_24h);
    $(element).find('#data_min_disk_30d').text(data.min_disk_30d);
    $(element).find('#data_min_disk_7d').text(data.min_disk_7d);
    $(element).find('#data_min_disk_24h').text(data.min_disk_24h);

    // Insights
    data = json.insights;
    element = $(tab).find('#data_insights');

    $(element).find('#data_low_tps_players').text(data.low_tps_players);
    $(element).find('#data_low_tps_entities').text(data.low_tps_entities);
    $(element).find('#data_low_tps_chunks').text(data.low_tps_chunks);
    $(element).find('#data_low_tps_cpu').text(data.low_tps_cpu);

    dates = data.low_disk_space_dates;
    dateString = '';
    for (i in dates) {
        dateString += (dates[i] + '<br>')
    }

    $(element).find('#data_low_disk_space_dates').replaceWith(
        dateString
    )
}
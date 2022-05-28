const loadedFilters = [];
const queryState = {
    filterCount: 0,
    filters: [],
    view: {
        afterDate: null,
        afterTime: null,
        beforeDate: null,
        beforeTime: null,
        servers: []
    },
    invalidFormFields: {
        ids: [],
        setAsInvalid: function (id) {
            if (this.ids.includes(id)) return;
            this.ids.push(id);
            queryState.updateQueryButton();
        },
        setAsValid: function (id) {
            this.ids = this.ids.filter(invalidID => invalidID !== id);
            queryState.updateQueryButton();
        },
    },
    updateQueryButton: function () {
        const queryButton = document.getElementById('query-button');
        if (this.invalidFormFields.ids.length === 0) {
            queryButton.removeAttribute('disabled');
            queryButton.classList.remove('disabled');
        } else {
            queryButton.setAttribute('disabled', 'true');
            queryButton.classList.add('disabled');
        }
    }
}

let timestamp = undefined;

let serverMap = {};

function loadView(json) {
    queryState.view = json.view;

    document.getElementById('viewFromDateField').setAttribute('placeholder', json.view.afterDate);
    document.getElementById('viewFromTimeField').setAttribute('placeholder', json.view.afterTime);
    document.getElementById('viewToDateField').setAttribute('placeholder', json.view.beforeDate);
    document.getElementById('viewToTimeField').setAttribute('placeholder', json.view.beforeTime);

    // Load server selector or hide it
    if (json.view.servers.length >= 2) {
        let options = ``;
        for (let server of json.view.servers) {
            if (server.proxy) continue;
            serverMap[server.serverUUID] = server;
            options += `<option data-plan-server-uuid="${server.serverUUID}">${server.serverName}</option>`
        }
        const serverSelector = document.getElementById("server-selector");
        serverSelector.innerHTML = options;

        serverSelector.addEventListener('click', () => {
            queryState.view.servers = [];
            if (serverSelector.selectedOptions.length !== serverSelector.options.length) {
                for (const option of serverSelector.selectedOptions) {
                    queryState.view.servers.push(serverMap[option.getAttribute('data-plan-server-uuid')]);
                }
            }
            document.getElementById("serverDropdown").innerText = queryState.view.servers.length
                ? `using data of ${queryState.view.servers.length} server(s)`
                : "using data of all servers"
        })
    } else {
        document.getElementById("serverDropdown").classList.add("hidden");
    }

    const playersOnlineSeries = {
        name: 'Players Online', type: 'areaspline', tooltip: {valueDecimals: 0},
        data: json.viewPoints, color: '#9E9E9E', yAxis: 0
    }
    graphs.push(Highcharts.stockChart('viewChart', {
        rangeSelector: {
            selected: 3,
            buttons: linegraphButtons
        },
        yAxis: {
            softMax: 2,
            softMin: 0
        },
        title: {text: ''},
        plotOptions: {
            areaspline: {
                fillOpacity: 0.4
            }
        },
        series: [playersOnlineSeries],
        xAxis: {
            events: {
                afterSetExtremes: function (event) {
                    if (this) {
                        const afterDate = Highcharts.dateFormat('%d/%m/%Y', this.min);
                        const afterTime = Highcharts.dateFormat('%H:%M', this.min);
                        const beforeDate = Highcharts.dateFormat('%d/%m/%Y', this.max);
                        const beforeTime = Highcharts.dateFormat('%H:%M', this.max);
                        document.getElementById('viewFromDateField').value = afterDate;
                        document.getElementById('viewFromTimeField').value = afterTime;
                        document.getElementById('viewToDateField').value = beforeDate;
                        document.getElementById('viewToTimeField').value = beforeTime;
                        const dontUpdateGraph = true;
                        setViewOption('viewFromDateField', 'afterDate', isValidDate, correctDate, dontUpdateGraph);
                        setViewOption('viewFromTimeField', 'afterTime', isValidTime, correctTime, dontUpdateGraph);
                        setViewOption('viewToDateField', 'beforeDate', isValidDate, correctDate, dontUpdateGraph);
                        setViewOption('viewToTimeField', 'beforeTime', isValidTime, correctTime, dontUpdateGraph);
                    }
                }
            }
        }
    }));
}

function loadFilters(json) {
    loadedFilters.push(...json.filters);

    let filterElements = '';
    for (let i = 0; i < loadedFilters.length; i++) {
        filterElements += createFilterSelector('#filters', i, loadedFilters[i]);
    }
    document.getElementById('filter-dropdown').innerHTML = filterElements;
}

function addFilter(parentSelector, filterIndex) {
    const id = "f" + queryState.filterCount;
    const filter = createFilter(loadedFilters[filterIndex], id);
    queryState.filters.push(filter);
    const inserting = document.createElement("template");
    inserting.innerHTML = filter.render(queryState.filterCount);
    document.querySelector(parentSelector).appendChild(inserting.content);
    queryState.filterCount++;
}

function removeFilter(filterIndex) {
    document.getElementById(filterIndex).remove();
    queryState.filters = queryState.filters.filter(f => f.id !== filterIndex);
}

function createFilterSelector(parent, index, filter) {
    return `<a class="dropdown-item" href="javascript:void(0)" onclick="addFilter('${parent}', ${index})">${getReadableFilterName(filter)}</a>`;
}

function isValidDate(value) {
    if (!value) return true;
    const d = value.match(
        /^(0\d|\d{2})[\/|\-]?(0\d|\d{2})[\/|\-]?(\d{4,5})$/
    );
    if (!d) return false;
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/Date
    const parsedDay = Number(d[1]);
    const parsedMonth = Number(d[2]) - 1; // 0=January, 11=December
    const parsedYear = Number(d[3]);
    return new Date(parsedYear, parsedMonth, parsedDay);
}

function correctDate(value) {
    const d = value.match(
        /^(0\d|\d{2})[\/|\-]?(0\d|\d{2})[\/|\-]?(\d{4,5})$/
    );
    if (!d) return value;

    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/Date
    const parsedDay = Number(d[1]);
    const parsedMonth = Number(d[2]) - 1; // 0=January, 11=December
    const parsedYear = Number(d[3]);
    const date = d ? new Date(parsedYear, parsedMonth, parsedDay) : null;

    const day = `${date.getDate()}`;
    const month = `${date.getMonth() + 1}`;
    const year = `${date.getFullYear()}`;
    return (
        (day.length === 1 ? `0${day}` : day) +
        "/" +
        (month.length === 1 ? `0${month}` : month) +
        "/" +
        year
    );
}

function isValidTime(value) {
    if (!value) return true;
    const regex = /^[0-2][0-9]:[0-5][0-9]$/;
    return regex.test(value);
}

function correctTime(value) {
    const d = value.match(/^(0\d|\d{2}):?(0\d|\d{2})$/);
    if (!d) return value;
    let hour = Number(d[1]);
    while (hour > 23) hour--;
    let minute = Number(d[2]);
    while (minute > 59) minute--;
    return (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute);
}

function setViewOption(
    elementId,
    propertyName,
    isValidFunction,
    correctionFunction,
    dontUpdateGraph
) {
    const view = queryState.view;
    const element = document.getElementById(elementId);
    let value = element.value;

    value = correctionFunction.apply(element, [value]);
    element.value = value;

    const isValid = isValidFunction.apply(element, [value]);
    if (isValid) {
        element.classList.remove("is-invalid");
        view[propertyName] = value;
        queryState.invalidFormFields.setAsValid(elementId);
        if (!dontUpdateGraph) updateViewGraph();
    } else {
        element.classList.add("is-invalid");
        queryState.invalidFormFields.setAsInvalid(elementId);
    }
}

function setFilterOption(
    id,
    elementId,
    propertyName,
    isValidFunction,
    correctionFunction,
    dontUpdateGraph
) {
    const query = queryState.filters.find(function (f) {
        return f.id === id;
    });
    const element = document.getElementById(elementId);
    let value = element.value;

    value = correctionFunction.apply(element, [value]);
    element.value = value;

    const isValid = isValidFunction.apply(element, [value]);
    if (isValid) {
        element.classList.remove("is-invalid");
        query[propertyName] = value;
        queryState.invalidFormFields.setAsValid(elementId);
    } else {
        element.classList.add("is-invalid");
        queryState.invalidFormFields.setAsInvalid(elementId);
    }
}

function updateViewGraph() {
    function parseTime(dateString, timeString) {
        const d = dateString.match(
            /^(0\d|\d{2})[\/|\-]?(0\d|\d{2})[\/|\-]?(\d{4,5})$/
        );
        const t = timeString.match(/^(0\d|\d{2}):?(0\d|\d{2})$/);

        // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/Date
        const parsedDay = Number(d[1]);
        const parsedMonth = Number(d[2]) - 1; // 0=January, 11=December
        const parsedYear = Number(d[3]);
        let hour = Number(t[1]);
        let minute = Number(t[2]);
        const date = new Date(parsedYear, parsedMonth, parsedDay, hour, minute);
        return date.getTime() - (date.getTimezoneOffset() * 60000);
    }

    const graph = graphs[0];

    const min = parseTime(queryState.view.afterDate, queryState.view.afterTime);
    const max = parseTime(queryState.view.beforeDate, queryState.view.beforeTime);
    for (const axis of graph.xAxis) {
        axis.setExtremes(min, max);
    }
}

let query = [];

function performNextQuery() {
    for (let filter of queryState.filters) {
        query.push(filter.toObject());
    }
    runQuery();
}

function getQueryAddress() {
    if (timestamp) return `./v1/query?timestamp=${timestamp}`;

    const encodedQuery = encodeURIComponent(JSON.stringify(query));
    const encodedView = encodeURIComponent(JSON.stringify(queryState.view));
    return `./v1/query?q=${encodedQuery}&view=${encodedView}`;
}

function runQuery() {
    const queryButton = document.getElementById('query-button');
    queryButton.setAttribute('disabled', 'true');
    queryButton.classList.add('disabled');

    if (timestamp) {
        document.querySelector('#content .tab').innerHTML =
            `<div class="page-loader">
            <div class="loader-container">
            <span class="loader"></span>
            <p class="loader-text">Loading..</p>
            </div>
        </div>`;
    } else {
        const icon = document.createElement('template');
        icon.innerHTML = '<i class="fa fa-cog fa-spin"></i>'
        queryButton.querySelector('.fa').replaceWith(icon.content);
    }

    jsonRequest(getQueryAddress(), function (json, error) {
        const previousPath = document.getElementById('result-path');
        if (previousPath) previousPath.remove();
        if (json) {
            if (json.data) {
                displayResults(json);
                displayResultPath(json);
            } else if (json.path) {
                // filters resulted in 0 players matched
                displayResultPath(json);
                // Reset query
                queryButton.removeAttribute('disabled');
                queryButton.classList.remove('disabled');
                const icon = document.createElement('template');
                icon.innerHTML = '<i class="fa fa-search"></i>'
                queryButton.querySelector('.fa').replaceWith(icon.content);
                query.splice(0, query.length);
            } else {
                // Cached query expired
                window.history.replaceState({}, '', `${location.pathname}?error=${encodeURIComponent('Cached query has expired')}`);
                location.reload();
            }
        } else if (error) {
            window.history.replaceState({}, '', `${location.pathname}?error=${encodeURIComponent(error)}`);
            location.reload();
        }
    });
}

function displayResultPath(json) {
    const gotResults = Boolean(json.data);
    let pathHtml = ``;
    for (let i = 0; i < json.path.length; i++) {
        const step = json.path[i];
        pathHtml += `<p class="m-0">`;
        for (let j = 0; j < i * 4; j++) {
            pathHtml += "&nbsp;";
        }
        pathHtml += `<i class="fa fa-fw fa-filter"></i> ${getReadableFilterName(step)} matched ${step.size} players</p>`
    }

    insertElementBefore('.tab .row .card', () => {
        const element = document.createElement('div');
        element.id = "result-path"
        element.classList.add("alert", gotResults ? "alert-success" : "alert-warning", "shadow");
        element.innerHTML = pathHtml;
        return element;
    });
    window.scrollTo(0, 0); // Scroll to top
}

function displayResults(json) {
    displayDataResultScreen(json.data.players.data.length, json.view ? json.view : {});

    // Set URL so that the query result can be shared
    window.history.replaceState({}, '', `${location.pathname}?timestamp=${json.timestamp}`);

    /* Player table */
    $('.player-table').DataTable({
        responsive: true,
        deferRender: true,
        columns: json.data.players.columns,
        data: json.data.players.data,
        order: [[5, "desc"]]
    });

    if ('undefined' !== typeof nightmode && nightmode == true) {
        document.querySelector('.table').classList.add('table-dark');
    }

    const activityIndexHeader = document.querySelector("#DataTables_Table_0 thead th:nth-of-type(2)");
    const lastSeenHeader = document.querySelector("#DataTables_Table_0 thead th:nth-of-type(6)");
    activityIndexHeader.innerHTML += ` (${json.view.beforeDate})`
    lastSeenHeader.innerHTML += ` (view)`

    // Activity graphs
    const activity_data = json.data.activity;
    activityPie('activityPie', {
        name: 'Players', colorByPoint: true, data: activity_data.activity_pie_series
    });
    stackChart('activityStackGraph', activity_data.activity_labels, activity_data.activity_series, 'Players');
    document.querySelector("#activity-date").innerHTML = json.view.beforeDate;

    // Geolocations
    const geolocation_data = json.data.geolocation;
    const geolocationSeries = {
        name: 'Players',
        type: 'map',
        mapData: Highcharts.maps['custom/world'],
        data: geolocation_data.geolocation_series,
        joinBy: ['iso-a3', 'code']
    };
    const geolocationBarSeries = {
        color: geolocation_data.colors.bars,
        name: 'Players',
        data: geolocation_data.geolocation_bar_series.map(function (bar) {
            return bar.value
        })
    };
    const geolocationBarCategories = geolocation_data.geolocation_bar_series.map(function (bar) {
        return bar.label
    });
    worldMap('worldMap', geolocation_data.colors.low, geolocation_data.colors.high, geolocationSeries);
    horizontalBarChart('countryBarChart', geolocationBarCategories, [geolocationBarSeries], 'Players');

    const session_data = json.data.sessions;

    document.querySelector("#data_total_playtime").innerHTML = session_data.total_playtime;
    document.querySelector("#data_average_playtime").innerHTML = session_data.average_playtime;
    document.querySelector("#data_total_afk_playtime").innerHTML = session_data.total_afk_playtime;
    document.querySelector("#data_average_afk_playtime").innerHTML = session_data.average_afk_playtime;
    document.querySelector("#data_total_active_playtime").innerHTML = session_data.total_active_playtime;
    document.querySelector("#data_average_active_playtime").innerHTML = session_data.average_active_playtime;
    document.querySelector("#data_total_sessions").innerHTML = session_data.total_sessions;
    document.querySelector("#data_average_sessions").innerHTML = session_data.average_sessions;
    document.querySelector("#data_average_session_length").innerHTML = session_data.average_session_length;
}

function displayDataResultScreen(resultCount, view) {
    const afterDate = queryState.view.afterDate ? queryState.view.afterDate : view.afterDate;
    const beforeDate = queryState.view.beforeDate ? queryState.view.beforeDate : view.beforeDate;
    const afterTime = queryState.view.afterTime ? queryState.view.afterTime : view.afterTime;
    const beforeTime = queryState.view.beforeTime ? queryState.view.beforeTime : view.beforeTime;

    graphs.splice(0, graphs.length); // Remove view graph from state as it is removed from DOM

    document.querySelector('#content .tab').innerHTML =
        `<div class="container-fluid mt-4">
            <div class="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 class="h3 mb-0 text-gray-800"><i class="sidebar-toggler fa fa-fw fa-bars" onclick="toggleSidebar()"></i>Plan &middot;
                    Query Results</h1>
                <p class="mb-0 text-gray-800">(matched ${resultCount} players)</p>
            </div>
            
            <div class="row">
                <div class="col-xs-12 col-sm-12 col-lg-12">
                    <div class="card shadow mb-4">
                        <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                            <h6 class="m-0 fw-bold col-black" title=" ${afterDate} ${afterTime} - ${beforeDate} ${beforeTime}"><i
                                    class="fas fa-fw fa-users col-black"></i>
                                View: ${afterDate} - ${beforeDate}, 
${view.servers.length ? "using data of servers: " + view.servers.map(server => server.serverName).join(', ') : "using data of all servers"}</h6>
                        </div>
                        <table class="table table-bordered table-striped table-hover player-table" style="width: 100%">
                            <tr>
                                <td>Loading..</td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
            
            <div class="row">
                <div class="col-xl-8 col-lg-8 col-sm-12">
                    <div class="card shadow mb-4">
                        <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                            <h6 class="m-0 fw-bold col-black"><i
                                    class="fas fa-fw fa-chart-line col-amber"></i>
                                Activity of matched players</h6>
                        </div>
                        <div class="chart-area" id="activityStackGraph"></div>
                    </div>
                </div>
                <div class="col-xl-4 col-lg-4 col-sm-12">
                    <div class="card shadow mb-4">
                        <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                            <h6 class="m-0 fw-bold col-black"><i
                                    class="fa fa-fw fa-users col-amber"></i>
                                Activity on <span id="activity-date"></span></h6>
                        </div>
                        <div class="chart-area" id="activityPie"></div>
                    </div>
                </div>
            </div>
            
            <div class="row">
                <div class="col-xl-3 col-lg-3 col-sm-12">
                    <div class="card shadow mb-4">
                        <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                            <h6 class="m-0 fw-bold col-black"><i class="col-teal far fa-calendar"></i> Sessions within view</h6>
                        </div>
                        <div class="card-body" id="data_players">
                            <p><i class="col-teal far fa-fw fa-calendar-check"></i> Sessions<span
                                    class="float-end"><b id="data_total_sessions"></b></span></p>
                            <p><i class="col-teal far fa-fw fa-calendar-check"></i> Average Sessions / Player<span
                                    class="float-end"><b id="data_average_sessions"></b></span></p>
                            <p><i class="col-teal far fa-fw fa-clock"></i> Average Session Length<span
                                    class="float-end" id="data_average_session_length"></span></p>
                            <hr>
                            <p><i class="col-green far fa-fw fa-clock"></i> Playtime<span
                                    class="float-end" id="data_total_playtime"></span></p>
                            <p><i class="col-green far fa-fw fa-clock"></i> Active Playtime<span
                                    class="float-end" id="data_total_active_playtime"></span></p>
                            <p><i class="col-grey far fa-fw fa-clock"></i> AFK Time<span
                                    class="float-end" id="data_total_afk_playtime"></span></p>
                            <hr>
                            <p><i class="col-green far fa-fw fa-clock"></i> Average Playtime / Player<span
                                    class="float-end" id="data_average_playtime"></span></p>
                            <p><i class="col-green far fa-fw fa-clock"></i> Average Active Playtime / Player<span
                                    class="float-end" id="data_average_active_playtime"></span></p>
                            <p><i class="col-grey far fa-fw fa-clock"></i> Average AFK Time / Player<span
                                    class="float-end" id="data_average_afk_playtime"></span></p>
                        </div>
                    </div>
                </div>
                
                <div class="col-xl-9 col-lg-9 col-sm-12">
                    <div class="card shadow mb-4">
                        <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                            <h6 class="m-0 fw-bold col-black"><i
                                    class="fas fa-fw fa-globe col-green"></i>
                                Geolocations</h6>
                        </div>
                        <div class="chart-area row" style="height: 100%;">
                            <div class="col-xs-12 col-sm-12 col-md-3 col-lg-3">
                                <div id="countryBarChart"></div>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-9 col-lg-9">
                                <div id="worldMap"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>`;
}

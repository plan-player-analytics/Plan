let filterCount = 0;

/* {
    id: "DOM id",
    options...
}*/
let timestamp = undefined;
let filterView = {
    afterDate: null,
    afterTime: null,
    beforeDate: null,
    beforeTime: null
};
let filterQuery = [];

const InvalidEntries = {
    ids: [],
    setAsInvalid: function (id) {
        if (this.ids.includes(id)) return;
        this.ids.push(id);
        this.updateQueryButton();
    },
    setAsValid: function (id) {
        this.ids = this.ids.filter(invalidID => invalidID !== id);
        this.updateQueryButton();
    },
    updateQueryButton: function () {
        const queryButton = document.getElementById('query-button');
        if (this.ids.length === 0) {
            queryButton.removeAttribute('disabled');
            queryButton.classList.remove('disabled');
        } else {
            queryButton.setAttribute('disabled', 'true');
            queryButton.classList.add('disabled');
        }
    }
}

function loadFilters(json) {
    filters.push(...json.filters);

    filterView = json.view;

    document.getElementById('viewFromDateField').setAttribute('placeholder', json.view.afterDate);
    document.getElementById('viewFromTimeField').setAttribute('placeholder', json.view.afterTime);
    document.getElementById('viewToDateField').setAttribute('placeholder', json.view.beforeDate);
    document.getElementById('viewToTimeField').setAttribute('placeholder', json.view.beforeTime);

    const s = {
        name: {playersOnline: 'Players Online'},
        tooltip: {zeroDecimals: {valueDecimals: 0}},
        type: {areaSpline: 'areaspline'}
    };

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
                        setFilterOption('view', 'viewFromDateField', 'afterDate', isValidDate, correctDate, dontUpdateGraph);
                        setFilterOption('view', 'viewFromTimeField', 'afterTime', isValidTime, correctTime, dontUpdateGraph);
                        setFilterOption('view', 'viewToDateField', 'beforeDate', isValidDate, correctDate, dontUpdateGraph);
                        setFilterOption('view', 'viewToTimeField', 'beforeTime', isValidTime, correctTime, dontUpdateGraph);
                    }
                }
            }
        }
    }));

    let filterElements = '';
    for (let i = 0; i < filters.length; i++) {
        filterElements += createFilterSelector('#filters', i, filters[i]);
    }
    document.getElementById('filter-dropdown').innerHTML = filterElements;
}

class Filter {
    constructor(kind) {
        this.kind = kind;
    }

    render(filterCount) {
        return 'Unimplemented render function'
    }

    toObject() {
        return {kind: this.kind}
    }
}

class MultipleChoiceFilter extends Filter {
    constructor(
        id, kind, label, options
    ) {
        super(kind);
        this.id = id;
        this.label = label;
        this.options = options;
    }

    render(filterCount) {
        const select = filterCount === 0 ? "of Players who " : "and ";
        let html =
            `<div id="${this.id}" class="mt-2 input-group input-row">` +
            `<div class="col-12"><label for="${this.id}">${select}${this.label}:</label>` +
            `<button class="filter-remover btn btn-outline-secondary float-right"
                onclick="removeFilter('${this.id}')"><i class="far fa-fw fa-trash-alt"></i></button>` +
            `<select class="form-control" multiple>`;

        for (const option of this.options.options) {
            html += `<option>${option}</option>`;
        }

        html += `</select></div></div>`;
        return html;
    }

    toObject() {
        let selected = [];
        for (let option of document.querySelector('#' + this.id + " select").selectedOptions) {
            selected.push(option.text);
        }
        selected = JSON.stringify(selected);

        return {
            kind: this.kind,
            parameters: {selected}
        }
    }
}

class ActivityIndexFilter extends MultipleChoiceFilter {
    constructor(
        id, options
    ) {
        super(id, "activityIndexNow", `are in Activity Groups`, options);
    }
}

class BannedFilter extends MultipleChoiceFilter {
    constructor(
        id, options
    ) {
        super(id, "banned", `are`, options);
    }
}

class OperatorsFilter extends MultipleChoiceFilter {
    constructor(
        id, options
    ) {
        super(id, "operators", `are`, options);
    }
}

class PluginGroupsFilter extends MultipleChoiceFilter {
    constructor(
        id, plugin, group, options
    ) {
        super(id, `pluginGroups: ${plugin} ${group}`, `are in ${plugin}'s ${group} Groups`, options);
    }
}

class BetweenDateFilter extends Filter {
    constructor(id, kind, label, options) {
        super(kind);
        this.id = id;
        this.label = label;
        this.afterDate = options.after[0];
        this.afterTime = options.after[1];
        this.beforeDate = options.before[0];
        this.beforeTime = options.before[1];
    }

    render(filterCount) {
        const id = this.id;
        const select = filterCount === 0 ? "of Players who " : "and ";
        return (
            `<div id="${id}">` +
            `<label class="ml-2 mt-0 mb-0">${select}${this.label}:</label>` +
            `<div class="mt-2 input-group input-row">` +
            `<div class="col-3"><div class="input-group mb-2">` +
            `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-calendar"></i></div></div>` +
            `<input id="${id}-afterdate" onkeyup="setFilterOption('${id}', '${id}-afterdate', 'afterDate', isValidDate, correctDate)" class="form-control" placeholder="${this.afterDate}" type="text">` +
            `</div></div>` +
            `<div class="col-2"><div class="input-group mb-2">` +
            `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-clock"></i></div></div>` +
            `<input id="${id}-aftertime" onkeyup="setFilterOption('${id}', '${id}-aftertime', 'afterTime', isValidTime, correctTime)" class="form-control" placeholder="${this.afterTime}" type="text">` +
            `</div></div>` +
            `<div class="col-auto"><label class="mt-2 mb-0" for="inlineFormCustomSelectPref">&</label></div>` +
            `<div class="col-3"><div class="input-group mb-2">` +
            `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-calendar"></i></div></div>` +
            `<input id="${id}-beforedate" onkeyup="setFilterOption('${id}', '${id}-beforedate', 'beforeDate', isValidDate, correctDate)" class="form-control" placeholder="${this.beforeDate}" type="text">` +
            `</div></div>` +
            `<div class="col-2"><div class="input-group mb-2">` +
            `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-clock"></i></div></div>` +
            `<input id="${id}-beforetime" onkeyup="setFilterOption('${id}', '${id}-beforetime', 'beforeTime', isValidTime, correctTime)" class="form-control" placeholder="${this.beforeTime}" type="text">` +
            `</div></div>` +
            `<button class="filter-remover btn btn-outline-secondary float-right"
                style="position: absolute;right: 0.8rem;"
                onclick="removeFilter('${this.id}')"><i class="far fa-fw fa-trash-alt"></i></button>` +
            `</div></div>`
        );
    }

    toObject() {
        return {
            kind: this.kind,
            parameters: {
                afterDate: this.afterDate,
                afterTime: this.afterTime,
                beforeDate: this.beforeDate,
                beforeTime: this.beforeTime
            }
        }
    }
}

class PlayedBetweenFilter extends BetweenDateFilter {
    constructor(id, options) {
        super(id, "playedBetween", "Played between", options);
    }
}

class RegisteredBetweenFilter extends BetweenDateFilter {
    constructor(id, options) {
        super(id, "registeredBetween", "Registered between", options);
    }
}

function addFilter(parentSelector, filterIndex) {
    const id = "f" + filterCount;
    const filter = createFilter(filters[filterIndex], id);
    filterQuery.push(filter);
    document.querySelector(parentSelector).innerHTML +=
        filter.render(filterCount);
    filterCount++;
}

function removeFilter(filterIndex) {
    document.getElementById(filterIndex).remove();
    filterQuery = filterQuery.filter(f => f.id !== filterIndex);
}

function createFilter(filter, id) {
    switch (filter.kind) {
        case "activityIndexNow":
            return new ActivityIndexFilter(id, filter.options);
        case "banned":
            return new BannedFilter(id, filter.options);
        case "operators":
            return new OperatorsFilter(id, filter.options);
        case "pluginGroups":
            return new PluginGroupsFilter(id, filter.plugin, filter.group, filter.options);
        case "playedBetween":
            return new PlayedBetweenFilter(id, filter.options);
        case "registeredBetween":
            return new RegisteredBetweenFilter(id, filter.options);
        default:
            throw new Error("Unsupported filter kind: '" + filter.kind + "'");
    }
}

function createFilterSelector(parent, index, filter) {
    return `<a class="dropdown-item" href="javascript:void(0)" onclick="addFilter('${parent}', ${index})">${filter.kind}</a>`;
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

function setFilterOption(
    id,
    elementId,
    propertyName,
    isValidFunction,
    correctionFunction,
    dontUpdateGraph
) {
    const query = id === 'view' ? filterView : filterQuery.find(function (f) {
        return f.id === id;
    });
    const element = document.getElementById(elementId);
    let value = element.value;

    value = correctionFunction.apply(element, [value]);
    element.value = value;

    const isValid = isValidFunction.apply(element, [value]);
    if (isValid) {
        element.classList.remove("is-invalid");
        query[propertyName] = value; // Updates either the query or filterView properties
        InvalidEntries.setAsValid(elementId);
        if (id === 'view' && !dontUpdateGraph) updateViewGraph();
    } else {
        element.classList.add("is-invalid");
        InvalidEntries.setAsInvalid(elementId);
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

    const min = parseTime(filterView.afterDate, filterView.afterTime);
    const max = parseTime(filterView.beforeDate, filterView.beforeTime);
    for (const axis of graph.xAxis) {
        axis.setExtremes(min, max);
    }
}

let query = [];

function performQuery() {
    for (let filter of filterQuery) {
        query.push(filter.toObject());
    }
    runQuery();
}

function getQueryAddress() {
    if (timestamp) return `./v1/query?timestamp=${timestamp}`;

    const encodedQuery = encodeURIComponent(JSON.stringify(query));
    const encodedView = encodeURIComponent(JSON.stringify(filterView));
    return `./v1/query?q=${encodedQuery}&view=${encodedView}`;
}

function runQuery() {
    const queryButton = document.getElementById('query-button');
    queryButton.setAttribute('disabled', 'true');
    queryButton.classList.add('disabled');

    // document.querySelector('#content .tab').innerHTML =
    //     `<div class="page-loader">
    //         <span class="loader"></span>
    //         <p class="loader-text">Loading..</p>
    //     </div>`;

    jsonRequest(getQueryAddress(), function (json, error) {
        const previousPath = document.getElementById('result-path');
        if (previousPath) previousPath.remove();
        console.log(json);
        if (json) {
            if (json.data) {
                renderResults(json);
            } else if (json.path) {
                // filters resulted in 0 players matched
                renderResultPath(json);
                // Reset query
                queryButton.removeAttribute('disabled');
                queryButton.classList.remove('disabled');
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

function renderResultPath(json) {
    let pathHtml = ``;
    for (let i = 0; i < json.path.length; i++) {
        const step = json.path[i];
        pathHtml += `<p class="m-0">`;
        for (let j = 0; j < i * 4; j++) {
            pathHtml += "&nbsp;";
        }
        pathHtml += `<i class="fa fa-fw fa-filter"></i> ${step.kind} matched ${step.size} players</p>`
    }

    const placeBefore = document.querySelector('.tab .row .card');
    const element = document.createElement('div');
    element.id = "result-path"
    element.classList.add("alert", "alert-warning", "shadow");
    element.innerHTML = pathHtml
    placeBefore.insertAdjacentElement('beforebegin', element);
    window.scrollTo(0, 0); // Scroll to top
}

function renderResults(json) {
    renderDataResultScreen(json.data.players.data.length, json.view ? json.view : {});

    // Set URL so that the query result can be shared
    window.history.replaceState({}, '', `${location.pathname}?timestamp=${json.timestamp}`);

    // Player table
    $('.player-table').DataTable({
        responsive: true,
        columns: json.data.players.columns,
        data: json.data.players.data,
        order: [[5, "desc"]]
    });
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

function renderDataResultScreen(resultCount, view) {
    const afterDate = filterView.afterDate ? filterView.afterDate : view.afterDate;
    const beforeDate = filterView.beforeDate ? filterView.beforeDate : view.beforeDate;
    const afterTime = filterView.afterTime ? filterView.afterTime : view.afterTime;
    const beforeTime = filterView.beforeTime ? filterView.beforeTime : view.beforeTime;
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
                            <h6 class="m-0 font-weight-bold col-black" title=" ${afterDate} ${afterTime} - ${beforeDate} ${beforeTime}"><i
                                    class="fas fa-fw fa-users col-black"></i>
                                View: ${afterDate} - ${beforeDate}</h6>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-bordered table-striped table-hover player-table dataTable">
                                <tr>
                                    <td>Loading..</td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="row">
                <div class="col-xl-8 col-lg-8 col-sm-12">
                    <div class="card shadow mb-4">
                        <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                            <h6 class="m-0 font-weight-bold col-black"><i
                                    class="fas fa-fw fa-chart-line col-amber"></i>
                                Activity of matched players</h6>
                        </div>
                        <div class="chart-area" id="activityStackGraph"></div>
                    </div>
                </div>
                <div class="col-xl-4 col-lg-4 col-sm-12">
                    <div class="card shadow mb-4">
                        <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                            <h6 class="m-0 font-weight-bold col-black"><i
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
                            <h6 class="m-0 font-weight-bold col-black"><i class="col-teal far fa-calendar"></i> Sessions within view</h6>
                        </div>
                        <div class="card-body" id="data_players">
                            <p><i class="col-teal far fa-fw fa-calendar-check"></i> Sessions<span
                                    class="float-right"><b id="data_total_sessions"></b></span></p>
                            <p><i class="col-teal far fa-fw fa-calendar-check"></i> Average Sessions / Player<span
                                    class="float-right"><b id="data_average_sessions"></b></span></p>
                            <p><i class="col-teal far fa-fw fa-clock"></i> Average Session Length<span
                                    class="float-right" id="data_average_session_length"></span></p>
                            <hr>
                            <p><i class="col-green far fa-fw fa-clock"></i> Playtime<span
                                    class="float-right" id="data_total_playtime"></span></p>
                            <p><i class="col-green far fa-fw fa-clock"></i> Active Playtime<span
                                    class="float-right" id="data_total_active_playtime"></span></p>
                            <p><i class="col-grey far fa-fw fa-clock"></i> AFK Playtime<span
                                    class="float-right" id="data_total_afk_playtime"></span></p>
                            <hr>
                            <p><i class="col-green far fa-fw fa-clock"></i> Average Playtime / Player<span
                                    class="float-right" id="data_average_playtime"></span></p>
                            <p><i class="col-green far fa-fw fa-clock"></i> Average Active Playtime / Player<span
                                    class="float-right" id="data_average_active_playtime"></span></p>
                            <p><i class="col-grey far fa-fw fa-clock"></i> Average AFK Playtime / Player<span
                                    class="float-right" id="data_average_afk_playtime"></span></p>
                        </div>
                    </div>
                </div>
                
                <div class="col-xl-9 col-lg-9 col-sm-12">
                    <div class="card shadow mb-4">
                        <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                            <h6 class="m-0 font-weight-bold col-black"><i
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
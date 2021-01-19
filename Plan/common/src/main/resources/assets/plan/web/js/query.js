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
const filterQuery = [];

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
            `<div class="col-12"><label for="exampleFormControlSelect2">${select}${this.label}:</label>` +
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
            `<label class="ml-2 mt-0 mb-0">${select}${this.label}:</label>` +
            `<div id="${id}" class="mt-2 input-group input-row">` +
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
            `</div>`
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
    $(parentSelector).append(filter.render(filterCount));
    filterCount++;
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
    return `<a class="dropdown-item" href="#" onclick="addFilter('${parent}', ${index})">${filter.kind}</a>`;
}

function isValidDate(value) {
    if (!value) return true;
    const date = value.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4,5})$/);
    return date ? new Date(date[3], date[2] - 1, date[1]) : null;
}

function correctDate(value) {
    const d = value.match(
        /^(0\d{1}|\d{2})[\/|\-]?(0\d{1}|\d{2})[\/|\-]?(\d{4,5})$/
    );
    if (!d) return value;

    const date = d ? new Date(d[3], d[2] - 1, d[1]) : null;
    const day = "" + (date.getUTCDate() + 1);
    const month = "" + (date.getUTCMonth() + 1);
    const year = "" + date.getUTCFullYear();
    return (
        (day.length === 1 ? "0" + day : day) +
        "/" +
        (month.length === 1 ? "0" + month : month) +
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
    const d = value.match(/^(\d{2}):?(\d{2})$/);
    if (!d) return value;
    let hour = d[1];
    while (hour > 23) hour--;
    let minute = d[2];
    while (minute > 59) minute--;
    return hour + ":" + minute;
}

function setFilterOption(
    id,
    elementId,
    propertyName,
    isValidFunction,
    correctionFunction
) {
    const query = id === 'view' ? filterView : filterQuery.find(function (f) {
        return f.id === id;
    });
    const element = $(`#${elementId}`);
    let value = element.val();

    value = correctionFunction.apply(element, [value]);
    element.val(value);

    const isValid = isValidFunction.apply(element, [value]);
    if (isValid) {
        element.removeClass("is-invalid");
        query[propertyName] = value;
    } else {
        element.addClass("is-invalid");
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
    const queryButton = document.querySelector('#query-button');
    queryButton.setAttribute('disabled', 'true');
    queryButton.classList.add('disabled');

    document.querySelector('#content .tab').innerHTML =
        `<div class="page-loader">
            <span class="loader"></span>
            <p class="loader-text">Loading..</p>
        </div>`;

    jsonRequest(getQueryAddress(), function (json, error) {
        if (!json.data) {
            window.history.replaceState({}, '', `${location.pathname}?error=${error ? error : 'Query result expired'}`);
            location.reload();
        }

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
    });
}

function renderDataResultScreen(resultCount, view) {
    const afterDate = filterView.afterDate ? filterView.afterDate : view.afterDate;
    const beforeDate = filterView.beforeDate ? filterView.beforeDate : view.beforeDate;
    const afterTime = filterView.afterTime ? filterView.afterTime : view.afterTime;
    const beforeTime = filterView.beforeTime ? filterView.beforeTime : view.beforeTime;
    document.querySelector('#content .tab').innerHTML =
        `<div class="container-fluid mt-4">
            <div class="d-sm-flex align-items-center justify-content-between mb-4">
                <h1 class="h3 mb-0 text-gray-800"><i class="sidebar-toggler fa fa-fw fa-bars"></i>Plan &middot;
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
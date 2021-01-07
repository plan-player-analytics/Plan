let filterCount = 0;

/* {
    id: "DOM id",
    options...
}*/
const filterQuery = [];

class Filter {
    constructor(kind) {
        this.kind = kind;
    }

    render(filterCount) {
        return 'Unimplemented render function'
    }

    updateParameters() {

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

    updateParameters() {
        let selected = "";
        for (let option of document.querySelector('#' + filter.id + " select").selectedOptions) {
            selected += option.text + ',';
        }
        selected = selected.substr(0, selected.length - 1); // Remove trailing comma
        this.parameters = {selected};
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
        id, plugin, options
    ) {
        super(id, "pluginGroups", `are in ${plugin} Groups`, options);
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

    updateParameters() {
        this.parameters = {
            dateAfter: this.afterDate,
            timeAfter: this.afterTime,
            dateBefore: this.beforeDate,
            timeBefore: this.beforeTime
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
            return new PluginGroupsFilter(id, filter.options);
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
    const query = filterQuery.find(function (f) {
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

function performQuery() {
    for (filter of filterQuery) filter.updateParameters();

    jsonRequest(`./v1/query?q=${encodeURIComponent(JSON.stringify(filterQuery))}`, function (json, error) {
        console.log(filterQuery);
        console.log(json);
        console.error(error);
    });
}
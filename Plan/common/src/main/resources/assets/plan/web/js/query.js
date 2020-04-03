var filterCount = 0;

/* {
    id: "DOM id",
    options...
}*/
var filterQuery = [];

function addFilter(parentSelector, filterIndex) {
    const id = "f" + filterCount;
    $(parentSelector).append(createElement(filters[filterIndex], id));
    filterCount++;
}

function createElement(filter, id) {
    switch (filter.kind) {
        case "activityIndexNow":
            return createMultipleChoiceSelector(
                id,
                `are in Activity Groups`,
                filter.options
            );
        case "banned":
            return createMultipleChoiceSelector(id, `are`, filter.options);
        case "operators":
            return createMultipleChoiceSelector(id, `are`, filter.options);
        case "pluginGroups":
            return createMultipleChoiceSelector(
                id,
                `are in ${filter.options.plugin} Groups`,
                filter.options
            );
        case "playedBetween":
            return createBetweenSelector(id, "Played between", filter.options);
        case "registeredBetween":
            return createBetweenSelector(id, "Registered between", filter.options);
        default:
            throw new Error("Unsupported filter kind: '" + filter.kind + "'");
    }
}

function createFilterSelector(parent, index, filter) {
    return `<a class="dropdown-item" href="#" onclick="addFilter('${parent}', ${index})">${filter.kind}</a>`;
}

function createBetweenSelector(id, label, options) {
    const query = {
        id: id,
        afterDate: options.after[0],
        afterTime: options.after[1],
        beforeDate: options.before[0],
        beforeTime: options.before[1],
    };
    filterQuery.push(query);

    const select = filterCount === 0 ? "of Players who " : "and ";
    return (
        `<label class="ml-2 mt-0 mb-0">${select}${label}:</label>` +
        `<div id="${id}" class="mt-2 input-group input-row">` +
        `<div class="col-3"><div class="input-group mb-2">` +
        `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-calendar"></i></div></div>` +
        `<input id="${id}-afterdate" onkeyup="setFilterOption('${id}', '${id}-afterdate', 'afterDate', 'isValidDate', 'correctDate')" class="form-control" placeholder="${query.afterDate}" type="text">` +
        `</div></div>` +
        `<div class="col-2"><div class="input-group mb-2">` +
        `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-clock"></i></div></div>` +
        `<input id="${id}-aftertime" onkeyup="setFilterOption('${id}', '${id}-aftertime', 'afterTime', 'isValidTime', 'correctTime')" class="form-control" placeholder="${query.afterTime}" type="text">` +
        `</div></div>` +
        `<div class="col-auto"><label class="mt-2 mb-0" for="inlineFormCustomSelectPref">&</label></div>` +
        `<div class="col-3"><div class="input-group mb-2">` +
        `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-calendar"></i></div></div>` +
        `<input id="${id}-beforedate" onkeyup="setFilterOption('${id}', '${id}-beforedate', 'beforeDate', 'isValidDate', 'correctDate')" class="form-control" placeholder="${query.beforeDate}" type="text">` +
        `</div></div>` +
        `<div class="col-2"><div class="input-group mb-2">` +
        `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-clock"></i></div></div>` +
        `<input id="${id}-beforetime" onkeyup="setFilterOption('${id}', '${id}-beforetime', 'beforeTime', 'isValidTime', 'correctTime')" class="form-control" placeholder="${query.beforeTime}" type="text">` +
        `</div></div>` +
        `</div>`
    );
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
    const regex = /^[0-2][0-9]\:[0-5][0-9]$/;
    return regex.test(value);
}

function correctTime(value) {
    const d = value.match(/^(\d{2})\:?(\d{2})$/);
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

    console.log(element, value, id, elementId);

    value = window[correctionFunction].apply(element, [value]);
    element.val(value);

    const isValid = window[isValidFunction].apply(element, [value]);
    if (isValid) {
        element.removeClass("is-invalid");
        query[propertyName] = value;
    } else {
        element.addClass("is-invalid");
    }
}

function createMultipleChoiceSelector(id, label, options) {
    var select = filterCount === 0 ? "of Players who " : "and ";
    var html =
        `<div id="${id}" class="mt-2 input-group input-row">` +
        `<div class="col-12"><label for="exampleFormControlSelect2">${select}${label}:</label>` +
        `<select class="form-control" multiple>`;

    for (var option of options.options) {
        html += `<option>${option}</option>`;
    }

    html += `</select></div> </div>`;
    return html;
}

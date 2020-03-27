var filterCount = 0;

function addFilter(parentSelector, filterIndex) {
    $(parentSelector).append(createElement(filters[filterIndex]));
    filterCount++;
}

function createElement(filter) {
    switch (filter.kind) {
        case "activityIndexNow":
            return createMultipleChoiceSelector(`are in Activity Groups`, filter.options);
        case "banned":
            return createMultipleChoiceSelector(`are`, filter.options);
        case "operators":
            return createMultipleChoiceSelector(`are`, filter.options);
        case "pluginGroups":
            return createMultipleChoiceSelector(`are in ${filter.options.plugin} Groups`, filter.options);
        case "playedBetween":
            return createBetweenSelector("Played between", filter.options);
        case "registeredBetween":
            return createBetweenSelector("Registered between", filter.options);
        default:
            throw new Error("Unsupported filter kind: '" + filter.kind + "'")
    }
}

function createFilterSelector(parent, index, filter) {
    return `<a class="dropdown-item" href="#" onclick="addFilter('${parent}', ${index})">${filter.kind}</a>`;
}

function createBetweenSelector(label, options) {
    var select = filterCount === 0 ? "of Players who " : "and ";
    return `<label class="ml-2 mt-0 mb-0">${select}${label}:</label>` +
        `<div class="mt-2 input-group input-row">` +

        `<div class="col-3"><div class="input-group mb-2">` +
        `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-calendar"></i></div></div>` +
        `<input class="form-control" placeholder="${options.after[0]}" type="text">` +
        `</div></div>` +

        `<div class="col-2"><div class="input-group mb-2">` +
        `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-clock"></i></div></div>` +
        `<input class="form-control" placeholder="${options.after[1]}" type="text">` +
        `</div></div>` +

        `<div class="col-auto"><label class="mt-2 mb-0" for="inlineFormCustomSelectPref">&</label></div>` +

        `<div class="col-3"><div class="input-group mb-2">` +
        `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-calendar"></i></div></div>` +
        `<input class="form-control" placeholder="${options.before[0]}" type="text">` +
        `</div></div>` +

        `<div class="col-2"><div class="input-group mb-2">` +
        `<div class="input-group-prepend"><div class="input-group-text"><i class="far fa-clock"></i></div></div>` +
        `<input class="form-control" placeholder="${options.before[1]}" type="text">` +
        `</div></div>` +
        `</div>`;
}

function createMultipleChoiceSelector(label, options) {
    var select = filterCount === 0 ? "of Players who " : "and ";
    var html = `<div class="mt-2 input-group input-row">` +
        `<div class="col-12">
        <label for="exampleFormControlSelect2">${select}${label}:</label>` +
        `<select class="form-control" multiple>`;

    for (var option of options.options) {
        html += `<option>${option}</option>`
    }

    html += `</select></div> </div>`;
    return html;
}
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
            `<div id="${this.id}" class="mt-2 input-group input-row">
                <div class="col-12">
                    <label for="${this.id}">${select}${this.label}:</label>
                    <button class="filter-remover btn btn-outline-secondary float-right"
                        onclick="removeFilter('${this.id}')"><i class="far fa-fw fa-trash-alt"></i></button>
                    <select class="form-control" multiple style="margin-bottom: 0.5rem;">`;

        for (const option of this.options.options) {
            html += `<option>${option}</option>`;
        }

        html += `</select></div></div>`;
        return html;
    }

    toObject() {
        let selected = [];
        for (let option of document.querySelector(`#${this.id} select`).options) {
            if (option.selected) selected.push(option.text);
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

class JoinAddressFilter extends MultipleChoiceFilter {
    constructor(
        id, options
    ) {
        super(id, "joinAddresses", `joined with address`, options);
    }
}

class PluginGroupsFilter extends MultipleChoiceFilter {
    constructor(
        id, kind, options
    ) {
        super(id, kind, `are in ${options.plugin}'s ${options.group} Groups`, options);
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
            `<div id="${id}">
                <label class="ml-2 mt-0 mb-0">${select}${this.label}:</label>
                <div class="mt-2 input-group input-row">
                    <div class="col-3">
                        <div class="input-group mb-2">
                            <div class="input-group-prepend"><div class="input-group-text">
                                <i class="far fa-calendar"></i>
                            </div></div>
                            <input id="${id}-afterdate" class="form-control" placeholder="${this.afterDate}" type="text"
                                onkeyup="setFilterOption('${id}', '${id}-afterdate', 'afterDate', isValidDate, correctDate)">
                        </div>
                    </div>
                    <div class="col-2">
                        <div class="input-group mb-2">
                            <div class="input-group-prepend"><div class="input-group-text">
                                <i class="far fa-clock"></i>
                            </div></div>
                            <input id="${id}-aftertime" class="form-control" placeholder="${this.afterTime}" type="text"
                                onkeyup="setFilterOption('${id}', '${id}-aftertime', 'afterTime', isValidTime, correctTime)">
                        </div>
                    </div>
                    <div class="col-auto">
                        <label class="mt-2 mb-0" for="inlineFormCustomSelectPref">&</label>
                    </div>
                    <div class="col-3">
                        <div class="input-group mb-2">
                            <div class="input-group-prepend"><div class="input-group-text">
                                <i class="far fa-calendar"></i>
                            </div></div>
                            <input id="${id}-beforedate" class="form-control" placeholder="${this.beforeDate}" type="text"
                                onkeyup="setFilterOption('${id}', '${id}-beforedate', 'beforeDate', isValidDate, correctDate)">
                        </div>
                    </div>
                    <div class="col-2">
                        <div class="input-group mb-2">
                            <div class="input-group-prepend"><div class="input-group-text">
                                <i class="far fa-clock"></i>
                            </div></div>
                            <input id="${id}-beforetime"  class="form-control" placeholder="${this.beforeTime}" type="text"
                                onkeyup="setFilterOption('${id}', '${id}-beforetime', 'beforeTime', isValidTime, correctTime)">
                        </div>
                    </div>
                    <button class="filter-remover btn btn-outline-secondary float-right"
                        style="position: absolute;right: 0.8rem;"
                        onclick="removeFilter('${this.id}')"><i class="far fa-fw fa-trash-alt"></i></button>
                </div>
            </div>`
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

function createFilter(filter, id) {
    if (filter.kind.startsWith("pluginGroups-")) {
        return new PluginGroupsFilter(id, filter.kind, filter.options);
    }
    switch (filter.kind) {
        case "activityIndexNow":
            return new ActivityIndexFilter(id, filter.options);
        case "banned":
            return new BannedFilter(id, filter.options);
        case "operators":
            return new OperatorsFilter(id, filter.options);
        case "joinAddresses":
            return new JoinAddressFilter(id, filter.options);
        case "playedBetween":
            return new PlayedBetweenFilter(id, filter.options);
        case "registeredBetween":
            return new RegisteredBetweenFilter(id, filter.options);
        default:
            throw new Error("Unsupported filter kind: '" + filter.kind + "'");
    }
}

function getReadableFilterName(filter) {
    if (filter.kind.startsWith("pluginGroups-")) {
        return "Group: " + filter.kind.substring(13);
    }
    switch (filter.kind) {
        case "allPlayers":
            return "All players"
        case "activityIndexNow":
            return "Current activity group";
        case "banned":
            return "Ban status";
        case "operators":
            return "Operator status";
        case "joinAddresses":
            return "Join Addresses";
        case "playedBetween":
            return "Played between";
        case "registeredBetween":
            return "Registered between";
        default:
            return filter.kind;
    }
}
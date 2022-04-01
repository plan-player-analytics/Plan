import {doGetRequest} from "./backendConfiguration";

export const fetchServerOverview = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/serverOverview?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPlayersOnlineGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=playersOnline&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchDayByDayGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=dayByDay&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchHourByHourGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=hourByHour&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchServerCalendarGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=serverCalendar&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPunchCardGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=punchCard&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}
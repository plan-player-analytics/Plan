import {doGetRequest} from "./backendConfiguration";

export const fetchServerOverview = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/serverOverview?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchOnlineActivityOverview = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/onlineOverview?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchSessions = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/sessions?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPlayersOnlineGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=playersOnline&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchDayByDayGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=uniqueAndNew&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchHourByHourGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=hourlyUniqueAndNew&server=${identifier}&timestamp=${timestamp}`;
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

export const fetchWorldPie = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=worldPie&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}
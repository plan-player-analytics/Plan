import {doGetRequest} from "./backendConfiguration";


export const fetchServerIdentity = async (identifier) => {
    const url = `/v1/serverIdentity?server=${identifier}`;
    return doGetRequest(url);
}

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

export const fetchPlayerbaseOverview = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/playerbaseOverview?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchSessionOverview = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/sessionsOverview?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPvpPve = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/playerVersus?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchSessions = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/sessions?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchKills = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/kills?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPlayers = async (identifier) => {
    const timestamp = Date.now();
    const url = identifier ? `/v1/players?server=${identifier}&timestamp=${timestamp}` : `/v1/players?timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPingTable = async (identifier) => {
    const timestamp = Date.now();
    const url = identifier ? `/v1/pingTable?server=${identifier}&timestamp=${timestamp}` : `/v1/pingTable?timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPlayersOnlineGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=playersOnline&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPlayerbaseDevelopmentGraph = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=activity&server=${identifier}&timestamp=${timestamp}`;
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

export const fetchGeolocations = async (identifier) => {
    const timestamp = Date.now();
    const url = `/v1/graph?type=geolocation&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

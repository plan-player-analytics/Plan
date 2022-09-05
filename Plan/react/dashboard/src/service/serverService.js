import {doGetRequest} from "./backendConfiguration";


export const fetchServerIdentity = async (timestamp, identifier) => {
    const url = `/v1/serverIdentity?server=${identifier}`;
    return doGetRequest(url);
}

export const fetchServerOverview = async (timestamp, identifier) => {
    const url = `/v1/serverOverview?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchOnlineActivityOverview = async (timestamp, identifier) => {
    const url = `/v1/onlineOverview?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPlayerbaseOverview = async (timestamp, identifier) => {
    const url = `/v1/playerbaseOverview?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchSessionOverview = async (timestamp, identifier) => {
    const url = `/v1/sessionsOverview?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPvpPve = async (timestamp, identifier) => {
    const url = `/v1/playerVersus?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPerformanceOverview = async (timestamp, identifier) => {
    const url = `/v1/performanceOverview?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchExtensionData = async (timestamp, identifier) => {
    const url = `/v1/extensionData?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchSessions = async (timestamp, identifier) => {
    const url = `/v1/sessions?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchKills = async (timestamp, identifier) => {
    const url = `/v1/kills?server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPlayers = async (timestamp, identifier) => {
    const url = identifier ? `/v1/players?server=${identifier}&timestamp=${timestamp}` : `/v1/players?timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPingTable = async (timestamp, identifier) => {
    const url = identifier ? `/v1/pingTable?server=${identifier}&timestamp=${timestamp}` : `/v1/pingTable?timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPlayersOnlineGraph = async (timestamp, identifier) => {
    const url = identifier ? `/v1/graph?type=playersOnline&server=${identifier}&timestamp=${timestamp}` :
        `/v1/graph?type=playersOnline&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPlayerbaseDevelopmentGraph = async (timestamp, identifier) => {
    const url = identifier ? `/v1/graph?type=activity&server=${identifier}&timestamp=${timestamp}` :
        `/v1/graph?type=activity&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchDayByDayGraph = async (timestamp, identifier) => {
    const url = identifier ? `/v1/graph?type=uniqueAndNew&server=${identifier}&timestamp=${timestamp}` :
        `/v1/graph?type=uniqueAndNew&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchHourByHourGraph = async (timestamp, identifier) => {
    const url = identifier ? `/v1/graph?type=hourlyUniqueAndNew&server=${identifier}&timestamp=${timestamp}` :
        `/v1/graph?type=hourlyUniqueAndNew&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchServerCalendarGraph = async (timestamp, identifier) => {
    const url = `/v1/graph?type=serverCalendar&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPunchCardGraph = async (timestamp, identifier) => {
    const url = `/v1/graph?type=punchCard&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchWorldPie = async (timestamp, identifier) => {
    const url = `/v1/graph?type=worldPie&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchGeolocations = async (timestamp, identifier) => {
    const url = `/v1/graph?type=geolocation&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchOptimizedPerformance = async (timestamp, identifier) => {
    const url = `/v1/graph?type=optimizedPerformance&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchPingGraph = async (timestamp, identifier) => {
    const url = `/v1/graph?type=aggregatedPing&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchJoinAddressPie = async (timestamp, identifier) => {
    const url = `/v1/graph?type=joinAddressPie&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

export const fetchJoinAddressByDay = async (timestamp, identifier) => {
    const url = `/v1/graph?type=joinAddressByDay&server=${identifier}&timestamp=${timestamp}`;
    return doGetRequest(url);
}

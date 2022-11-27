import {doGetRequest, staticSite} from "./backendConfiguration";

export const fetchServerIdentity = async (timestamp, identifier) => {
    let url = `/v1/serverIdentity?server=${identifier}`;
    if (staticSite) url = `/data/serverIdentity-${identifier}.json`;
    return doGetRequest(url);
}

export const fetchServerOverview = async (timestamp, identifier) => {
    let url = `/v1/serverOverview?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/serverOverview-${identifier}.json`;
    return doGetRequest(url);
}

export const fetchOnlineActivityOverview = async (timestamp, identifier) => {
    let url = `/v1/onlineOverview?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/onlineOverview-${identifier}.json`;
    return doGetRequest(url);
}

export const fetchPlayerbaseOverview = async (timestamp, identifier) => {
    let url = `/v1/playerbaseOverview?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/playerbaseOverview-${identifier}.json`;
    return doGetRequest(url);
}

export const fetchSessionOverview = async (timestamp, identifier) => {
    let url = `/v1/sessionsOverview?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/sessionsOverview-${identifier}.json`;
    return doGetRequest(url);
}

export const fetchPvpPve = async (timestamp, identifier) => {
    let url = `/v1/playerVersus?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/playerVersus-${identifier}.json`;
    return doGetRequest(url);
}

export const fetchPerformanceOverview = async (timestamp, identifier) => {
    let url = `/v1/performanceOverview?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/performanceOverview-${identifier}.json`;
    return doGetRequest(url);
}

export const fetchExtensionData = async (timestamp, identifier) => {
    let url = `/v1/extensionData?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/extensionData-${identifier}.json`;
    return doGetRequest(url);
}

export const fetchSessions = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchSessionsServer(timestamp, identifier);
    } else {
        return await fetchSessionsNetwork(timestamp);
    }
}

const fetchSessionsServer = async (timestamp, identifier) => {
    let url = `/v1/sessions?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/sessions-${identifier}.json`;
    return doGetRequest(url);
}

const fetchSessionsNetwork = async (timestamp) => {
    let url = `/v1/sessions?timestamp=${timestamp}`;
    if (staticSite) url = `/data/sessions.json`;
    return doGetRequest(url);
}

export const fetchKills = async (timestamp, identifier) => {
    let url = `/v1/kills?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/kills-${identifier}.json`;
    return doGetRequest(url);
}

export const fetchPlayers = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchPlayersServer(timestamp, identifier);
    } else {
        return await fetchPlayersNetwork(timestamp);
    }
}
const fetchPlayersServer = async (timestamp, identifier) => {
    let url = `/v1/players?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/players-${identifier}.json`;
    return doGetRequest(url);
}

const fetchPlayersNetwork = async (timestamp) => {
    let url = `/v1/players?timestamp=${timestamp}`;
    if (staticSite) url = `/data/players.json`;
    return doGetRequest(url);
}

export const fetchPingTable = async (timestamp, identifier) => {
    let url = `/v1/pingTable?server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/pingTable-${identifier}.json`;
    return doGetRequest(url);
}

export const fetchPlayersOnlineGraph = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchPlayersOnlineGraphServer(timestamp, identifier);
    } else {
        return await fetchPlayersOnlineGraphNetwork(timestamp);
    }
}

const fetchPlayersOnlineGraphServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=playersOnline&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-playersOnline_${identifier}.json`;
    return doGetRequest(url);
}

const fetchPlayersOnlineGraphNetwork = async (timestamp) => {
    let url = `/v1/graph?type=playersOnline&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-playersOnline.json`;
    return doGetRequest(url);
}

export const fetchPlayerbaseDevelopmentGraph = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchPlayerbaseDevelopmentGraphServer(timestamp, identifier);
    } else {
        return await fetchPlayerbaseDevelopmentGraphNetwork(timestamp);
    }
}

const fetchPlayerbaseDevelopmentGraphServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=activity&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-activity_${identifier}.json`;
    return doGetRequest(url);
}

const fetchPlayerbaseDevelopmentGraphNetwork = async (timestamp) => {
    let url = `/v1/graph?type=activity&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-activity.json`;
    return doGetRequest(url);
}

export const fetchDayByDayGraph = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchDayByDayGraphServer(timestamp, identifier);
    } else {
        return await fetchDayByDayGraphNetwork(timestamp);
    }
}

const fetchDayByDayGraphServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=uniqueAndNew&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-uniqueAndNew_${identifier}.json`;
    return doGetRequest(url);
}

const fetchDayByDayGraphNetwork = async (timestamp) => {
    let url = `/v1/graph?type=uniqueAndNew&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-uniqueAndNew.json`;
    return doGetRequest(url);
}

export const fetchHourByHourGraph = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchHourByHourGraphServer(timestamp, identifier);
    } else {
        return await fetchHourByHourGraphNetwork(timestamp);
    }
}

const fetchHourByHourGraphServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=hourlyUniqueAndNew&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-hourlyUniqueAndNew_${identifier}.json`;
    return doGetRequest(url);
}

const fetchHourByHourGraphNetwork = async (timestamp) => {
    let url = `/v1/graph?type=hourlyUniqueAndNew&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-hourlyUniqueAndNew.json`;
    return doGetRequest(url);
}

export const fetchServerCalendarGraph = async (timestamp, identifier) => {
    let url = `/v1/graph?type=serverCalendar&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-serverCalendar_${identifier}.json`;
    return doGetRequest(url);
}

export const fetchPunchCardGraph = async (timestamp, identifier) => {
    let url = `/v1/graph?type=punchCard&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-punchCard_${identifier}.json`;
    return doGetRequest(url);
}

export const fetchWorldPie = async (timestamp, identifier) => {
    let url = `/v1/graph?type=worldPie&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-worldPie_${identifier}.json`;
    return doGetRequest(url);
}

export const fetchGeolocations = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchGeolocationsServer(timestamp, identifier);
    } else {
        return await fetchGeolocationsNetwork(timestamp);
    }
}

const fetchGeolocationsServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=geolocation&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-geolocation_${identifier}.json`;
    return doGetRequest(url);
}

const fetchGeolocationsNetwork = async (timestamp) => {
    let url = `/v1/graph?type=geolocation&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-geolocation.json`;
    return doGetRequest(url);
}

export const fetchOptimizedPerformance = async (timestamp, identifier, after) => {
    let url = `/v1/graph?type=optimizedPerformance&server=${identifier}&timestamp=${timestamp}&after=${after}`;
    if (staticSite) url = `/data/graph-optimizedPerformance_${identifier}.json`;
    return doGetRequest(url);
}

export const fetchPingGraph = async (timestamp, identifier) => {
    let url = `/v1/graph?type=aggregatedPing&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-aggregatedPing_${identifier}.json`;
    return doGetRequest(url);
}

export const fetchJoinAddressPie = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchJoinAddressPieServer(timestamp, identifier);
    } else {
        return await fetchJoinAddressPieNetwork(timestamp);
    }
}

const fetchJoinAddressPieServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=joinAddressPie&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-joinAddressPie_${identifier}.json`;
    return doGetRequest(url);
}

const fetchJoinAddressPieNetwork = async (timestamp) => {
    let url = `/v1/graph?type=joinAddressPie&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-joinAddressPie.json`;
    return doGetRequest(url);
}

export const fetchJoinAddressByDay = async (timestamp, identifier) => {
    if (identifier) {
        return await fetchJoinAddressByDayServer(timestamp, identifier);
    } else {
        return await fetchJoinAddressByDayNetwork(timestamp);
    }
}

const fetchJoinAddressByDayServer = async (timestamp, identifier) => {
    let url = `/v1/graph?type=joinAddressByDay&server=${identifier}&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-joinAddressByDay_${identifier}.json`;
    return doGetRequest(url);
}

const fetchJoinAddressByDayNetwork = async (timestamp) => {
    let url = `/v1/graph?type=joinAddressByDay&timestamp=${timestamp}`;
    if (staticSite) url = `/data/graph-joinAddressByDay.json`;
    return doGetRequest(url);
}
